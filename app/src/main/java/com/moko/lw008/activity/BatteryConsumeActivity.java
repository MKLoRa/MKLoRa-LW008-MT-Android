package com.moko.lw008.activity;


import android.os.Bundle;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lib.loraui.dialog.AlertMessageDialog;
import com.moko.lw008.databinding.Lw008ActivityBatteryConsumeBinding;
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

public class BatteryConsumeActivity extends BaseActivity {

    private Lw008ActivityBatteryConsumeBinding mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008ActivityBatteryConsumeBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        mBind.tvAdvTimes.postDelayed(() -> {
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getBatteryInfoNew());
            orderTasks.add(OrderTaskAssembler.getBatteryInfoAll());
            orderTasks.add(OrderTaskAssembler.getBatteryInfoLast());
            LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }, 500);
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
                                    case KEY_BATTERY_RESET_NEW:
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
                                    case KEY_BATTERY_INFO_NEW:
                                        if (length == 48) {
                                            int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
                                            mBind.tvRuntime.setText(String.format("%d s", runtime));
                                            int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
                                            mBind.tvAdvTimes.setText(String.format("%d times", advTimes));
                                            int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
                                            mBind.tvAxisDuration.setText(String.format("%d s", axisDuration));
                                            int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
                                            mBind.tvBleFixDuration.setText(String.format("%d s", bleFixDuration));
                                            int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
                                            mBind.tvWifiFixDuration.setText(String.format("%d s", wifiFixDuration));
                                            int gpsL76FixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
                                            mBind.tvGpsL76FixDuration.setText(String.format("%d s", gpsL76FixDuration));
                                            int gpsLrFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
                                            mBind.tvGpsLrFixDuration.setText(String.format("%d s", gpsLrFixDuration));
                                            int staticUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
                                            mBind.tvStaticPosPayload.setText(String.format("%d times", staticUploadNum));
                                            int moveUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
                                            mBind.tvMotionPosPayload.setText(String.format("%d times", moveUploadNum));
                                            int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 40, 44));
                                            mBind.tvLoraTransmissionTimes.setText(String.format("%d times", loraTransmissionTimes));
                                            int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 44, 48));
                                            mBind.tvLoraPower.setText(String.format("%d mAS", loraPower));
                                            String batteryConsumeStr = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(Arrays.copyOfRange(value, 48, 52)) * 0.001f);
                                            mBind.tvBatteryConsume.setText(String.format("%s mAH", batteryConsumeStr));
                                        }
                                        break;
                                    case KEY_BATTERY_INFO_ALL:
                                        if (length == 48) {
                                            int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
                                            mBind.tvRuntimeAll.setText(String.format("%d s", runtime));
                                            int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
                                            mBind.tvAdvTimesAll.setText(String.format("%d times", advTimes));
                                            int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
                                            mBind.tvAxisDurationAll.setText(String.format("%d s", axisDuration));
                                            int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
                                            mBind.tvBleFixDurationAll.setText(String.format("%d s", bleFixDuration));
                                            int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
                                            mBind.tvWifiFixDurationAll.setText(String.format("%d s", wifiFixDuration));
                                            int gpsL76FixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
                                            mBind.tvGpsL76FixDurationAll.setText(String.format("%d s", gpsL76FixDuration));
                                            int gpsLrFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
                                            mBind.tvGpsLrFixDurationAll.setText(String.format("%d s", gpsLrFixDuration));
                                            int staticUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
                                            mBind.tvStaticPosPayloadAll.setText(String.format("%d times", staticUploadNum));
                                            int moveUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
                                            mBind.tvMotionPosPayloadAll.setText(String.format("%d times", moveUploadNum));
                                            int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 40, 44));
                                            mBind.tvLoraTransmissionTimesAll.setText(String.format("%d times", loraTransmissionTimes));
                                            int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 44, 48));
                                            mBind.tvLoraPowerAll.setText(String.format("%d mAS", loraPower));
                                            String batteryConsumeStr = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(Arrays.copyOfRange(value, 48, 52)) * 0.001f);
                                            mBind.tvBatteryConsumeAll.setText(String.format("%s mAH", batteryConsumeStr));
                                        }
                                        break;
                                    case KEY_BATTERY_INFO_LAST:
                                        if (length == 48) {
                                            int runtime = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 8));
                                            mBind.tvRuntimeLast.setText(String.format("%d s", runtime));
                                            int advTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 8, 12));
                                            mBind.tvAdvTimesLast.setText(String.format("%d times", advTimes));
                                            int axisDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 12, 16));
                                            mBind.tvAxisDurationLast.setText(String.format("%d s", axisDuration));
                                            int bleFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 16, 20));
                                            mBind.tvBleFixDurationLast.setText(String.format("%d s", bleFixDuration));
                                            int wifiFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 20, 24));
                                            mBind.tvWifiFixDurationLast.setText(String.format("%d s", wifiFixDuration));
                                            int gpsL76FixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 24, 28));
                                            mBind.tvGpsL76FixDurationLast.setText(String.format("%d s", gpsL76FixDuration));
                                            int gpsLrFixDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 28, 32));
                                            mBind.tvGpsLrFixDurationLast.setText(String.format("%d s", gpsLrFixDuration));
                                            int staticUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 32, 36));
                                            mBind.tvStaticPosPayloadLast.setText(String.format("%d times", staticUploadNum));
                                            int moveUploadNum = MokoUtils.toInt(Arrays.copyOfRange(value, 36, 40));
                                            mBind.tvMotionPosPayloadLast.setText(String.format("%d times", moveUploadNum));
                                            int loraTransmissionTimes = MokoUtils.toInt(Arrays.copyOfRange(value, 40, 44));
                                            mBind.tvLoraTransmissionTimesLast.setText(String.format("%d times", loraTransmissionTimes));
                                            int loraPower = MokoUtils.toInt(Arrays.copyOfRange(value, 44, 48));
                                            mBind.tvLoraPowerLast.setText(String.format("%d mAS", loraPower));
                                            String batteryConsumeStr = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(Arrays.copyOfRange(value, 48, 52)) * 0.001f);
                                            mBind.tvBatteryConsumeLast.setText(String.format("%s mAH", batteryConsumeStr));
                                        }
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
            orderTasks.add(OrderTaskAssembler.setBatteryResetNew());
            orderTasks.add(OrderTaskAssembler.getBatteryInfoNew());
            orderTasks.add(OrderTaskAssembler.getBatteryInfoAll());
            orderTasks.add(OrderTaskAssembler.getBatteryInfoLast());
            LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
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
