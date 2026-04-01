package com.moko.lw008.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lib.loraui.dialog.BottomDialog;
import com.moko.lib.loraui.utils.ToastUtils;
import com.moko.lw008.AppConstants;
import com.moko.lw008.databinding.Lw008ActivitySelftestBinding;
import com.moko.lw008.utils.SPUtiles;
import com.moko.lw008.utils.Utils;
import com.moko.support.lw008.LoRaLW008MokoSupport;
import com.moko.support.lw008.OrderTaskAssembler;
import com.moko.support.lw008.entity.OrderCHAR;
import com.moko.support.lw008.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelfTestActivity extends BaseActivity {

    private Lw008ActivitySelftestBinding mBind;

    private ArrayList<String> mValues;
    private String mVersion;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008ActivitySelftestBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        mVersion = SPUtiles.getStringValue(this, AppConstants.SP_KEY_FIREWARE_VERSION, "V1.0.0");
        showSyncingProgressDialog();
        mBind.tvSelftestStatus.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getSelfTestStatus());
            orderTasks.add(OrderTaskAssembler.getPCBAStatus());
            if (!Utils.isNewFunction(mVersion, "V1.0.9"))
                orderTasks.add(OrderTaskAssembler.getBatteryInfo());
            else {
                orderTasks.add(OrderTaskAssembler.getCondition1VoltageThreshold());
                orderTasks.add(OrderTaskAssembler.getCondition1MinSampleInterval());
                orderTasks.add(OrderTaskAssembler.getCondition1SampleTimes());
                orderTasks.add(OrderTaskAssembler.getCondition2VoltageThreshold());
                orderTasks.add(OrderTaskAssembler.getCondition2MinSampleInterval());
                orderTasks.add(OrderTaskAssembler.getCondition2SampleTimes());
            }
            LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
        if (Utils.isNewFunction(mVersion, "V1.0.9")) {
            mBind.ivSave.setVisibility(View.VISIBLE);
            mBind.llBatteryInfo.setVisibility(View.GONE);
            mBind.clBatteryReset.setVisibility(View.GONE);
            mBind.clLoraLowPowerCondition.setVisibility(View.VISIBLE);
            mBind.clGpsLowPowerCondition.setVisibility(View.VISIBLE);
            mValues = new ArrayList<>();
            for (int i = 44; i <= 64; i++) {
                mValues.add(MokoUtils.getDecimalFormat("0.##").format(i * 0.05f));
            }
            mBind.tvCondition1VoltageThreshold.setTag(0);
            mBind.tvCondition1VoltageThreshold.setOnClickListener(v -> {
                showThresholdDialog(v);
            });
            mBind.tvCondition2VoltageThreshold.setTag(0);
            mBind.tvCondition2VoltageThreshold.setOnClickListener(v -> {
                showThresholdDialog(v);
            });
        }
    }

    private void showThresholdDialog(View v) {
        if (isWindowLocked()) return;
        int selected = (int) v.getTag();
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, selected);
        dialog.setListener(value -> {
            ((TextView) v).setText(mValues.get(value));
            v.setTag(value);
        });
        dialog.show(getSupportFragmentManager());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (!MokoConstants.ACTION_CURRENT_DATA.equals(action))
            EventBus.getDefault().cancelEventDelivery(event);
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length >= 4) {
                            int header = value[0] & 0xFF;// 0xED
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xED)
                                return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            if (flag == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_CONDITION_1_VOLTAGE_THRESHOLD:
                                    case KEY_CONDITION_1_MIN_SAMPLE_INTERVAL:
                                    case KEY_CONDITION_1_SAMPLE_TIMES:
                                    case KEY_CONDITION_2_VOLTAGE_THRESHOLD:
                                    case KEY_CONDITION_2_MIN_SAMPLE_INTERVAL:
                                        savedParamsError |= result != 1;
                                        break;
                                    case KEY_CONDITION_2_SAMPLE_TIMES:
                                        savedParamsError |= result != 1;
                                        if (savedParamsError)
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        else
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        break;
                                    case KEY_BATTERY_RESET:
                                        if (result == 1) {
                                            AlertMessageDialog dialog = new AlertMessageDialog();
                                            dialog.setMessage("Reset Successfully！");
                                            dialog.setConfirm("OK");
                                            dialog.setCancelGone();
                                            dialog.show(getSupportFragmentManager());
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_SELFTEST_STATUS:
                                        if (length > 0) {
                                            int status = value[4] & 0xFF;
                                            mBind.tvSelftestStatus.setVisibility(status == 0 ? View.VISIBLE : View.GONE);
                                            if ((status & 0x01) == 0x01)
                                                mBind.tvGpsStatus.setVisibility(View.VISIBLE);
                                            if ((status & 0x02) == 0x02)
                                                mBind.tvAxisStatus.setVisibility(View.VISIBLE);
                                            if ((status & 0x04) == 0x04)
                                                mBind.tvFlashStatus.setVisibility(View.VISIBLE);
                                        }
                                        break;
                                    case KEY_PCBA_STATUS:
                                        if (length > 0) {
                                            mBind.tvPcbaStatus.setText(String.valueOf(value[4] & 0xFF));
                                        }
                                        break;
                                    case KEY_BATTERY_INFO:
                                        if (length == 40) {
                                            int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
                                            mBind.tvRuntime.setText(String.format("%d s", runtime));
                                            int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
                                            mBind.tvAdvTimes.setText(String.format("%d times", advTimes));
                                            int flashTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
                                            mBind.tvFlashTimes.setText(String.format("%d times", flashTimes));
                                            int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
                                            mBind.tvAxisDuration.setText(String.format("%d ms", axisDuration));
                                            int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
                                            mBind.tvBleFixDuration.setText(String.format("%d ms", bleFixDuration));
                                            int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
                                            mBind.tvWifiFixDuration.setText(String.format("%d ms", wifiFixDuration));
                                            int gpsFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
                                            mBind.tvGpsFixDuration.setText(String.format("%d s", gpsFixDuration));
                                            int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
                                            mBind.tvLoraTransmissionTimes.setText(String.format("%d times", loraTransmissionTimes));
                                            int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
                                            mBind.tvLoraPower.setText(String.format("%d mAS", loraPower));
                                            String batteryConsumeStr = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(Arrays.copyOfRange(value, 40, 44)) * 0.001f);
                                            mBind.tvBatteryConsume.setText(String.format("%s mAH", batteryConsumeStr));
                                        }
                                        break;
                                    case KEY_CONDITION_1_VOLTAGE_THRESHOLD:
                                        int thresholdNon = value[4] & 0xFF;
                                        int selectedNon = thresholdNon - 44;
                                        mBind.tvCondition1VoltageThreshold.setText(mValues.get(selectedNon));
                                        mBind.tvCondition1VoltageThreshold.setTag(selectedNon);
                                        break;
                                    case KEY_CONDITION_1_MIN_SAMPLE_INTERVAL:
                                        int intervalNon = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etCondition1MinSampleInterval.setText(String.valueOf(intervalNon));
                                        break;
                                    case KEY_CONDITION_1_SAMPLE_TIMES:
                                        int timesNon = value[4] & 0xFF;
                                        mBind.etCondition1SampleTimes.setText(String.valueOf(timesNon));
                                        break;
                                    case KEY_CONDITION_2_VOLTAGE_THRESHOLD:
                                        int threshold = value[4] & 0xFF;
                                        int selected = threshold - 44;
                                        mBind.tvCondition2VoltageThreshold.setText(mValues.get(selected));
                                        mBind.tvCondition2VoltageThreshold.setTag(selected);
                                        break;
                                    case KEY_CONDITION_2_MIN_SAMPLE_INTERVAL:
                                        int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 4 + length));
                                        mBind.etCondition2MinSampleInterval.setText(String.valueOf(interval));
                                        break;
                                    case KEY_CONDITION_2_SAMPLE_TIMES:
                                        int times = value[4] & 0xFF;
                                        mBind.etCondition2SampleTimes.setText(String.valueOf(times));
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        });
    }

    public void onBatteryReset(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning！");
        dialog.setMessage("Are you sure to reset battery?");
        dialog.setConfirm("OK");
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setBatteryReset());
            orderTasks.add(OrderTaskAssembler.getBatteryInfo());
            LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(mBind.etCondition1MinSampleInterval.getText())) return false;
        String intervalNonStr = mBind.etCondition1MinSampleInterval.getText().toString();
        int intervalNon = Integer.parseInt(intervalNonStr);
        if (intervalNon < 1 || intervalNon > 1440)
            return false;
        if (TextUtils.isEmpty(mBind.etCondition1SampleTimes.getText())) return false;
        String timesNonStr = mBind.etCondition1SampleTimes.getText().toString();
        int timesNon = Integer.parseInt(timesNonStr);
        if (timesNon < 1 || timesNon > 100)
            return false;
        if (TextUtils.isEmpty(mBind.etCondition2MinSampleInterval.getText())) return false;
        String intervalStr = mBind.etCondition2MinSampleInterval.getText().toString();
        int interval = Integer.parseInt(intervalStr);
        if (interval < 1 || interval > 1440)
            return false;
        if (TextUtils.isEmpty(mBind.etCondition2SampleTimes.getText())) return false;
        String timesStr = mBind.etCondition2SampleTimes.getText().toString();
        int times = Integer.parseInt(timesStr);
        if (times < 1 || times > 100)
            return false;
        return true;
    }

    private void saveParams() {
        savedParamsError = false;
        int thresholdNon = (int) mBind.tvCondition1VoltageThreshold.getTag() + 44;
        int intervalNon = Integer.parseInt(mBind.etCondition1MinSampleInterval.getText().toString());
        int timesNon = Integer.parseInt(mBind.etCondition1SampleTimes.getText().toString());
        int threshold = (int) mBind.tvCondition2VoltageThreshold.getTag() + 44;
        int interval = Integer.parseInt(mBind.etCondition2MinSampleInterval.getText().toString());
        int times = Integer.parseInt(mBind.etCondition2SampleTimes.getText().toString());

        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setCondition1VoltageThreshold(thresholdNon));
        orderTasks.add(OrderTaskAssembler.setCondition1MinSampleInterval(intervalNon));
        orderTasks.add(OrderTaskAssembler.setCondition1SampleTimes(timesNon));
        orderTasks.add(OrderTaskAssembler.setCondition2VoltageThreshold(threshold));
        orderTasks.add(OrderTaskAssembler.setCondition2MinSampleInterval(interval));
        orderTasks.add(OrderTaskAssembler.setCondition2SampleTimes(times));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        finish();
    }
}
