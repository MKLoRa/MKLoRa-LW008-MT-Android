package com.moko.lw008.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lw008.R;
import com.moko.lw008.R2;
import com.moko.lw008.dialog.BottomDialog;
import com.moko.lw008.dialog.LoadingMessageDialog;
import com.moko.lw008.utils.ToastUtils;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManDownDetectionActivity extends BaseActivity {


    @BindView(R2.id.cb_man_down_detection)
    CheckBox cbManDownDetection;
    @BindView(R2.id.et_man_down_detection_timeout)
    EditText etManDownDetectionTimeout;
    @BindView(R2.id.tv_man_down_pos_strategy)
    TextView tvManDownPosStrategy;
    @BindView(R2.id.et_man_down_report_interval)
    EditText etManDownReportInterval;
    @BindView(R2.id.cb_man_down_on_start)
    CheckBox cbManDownOnStart;
    @BindView(R2.id.cb_man_down_on_end)
    CheckBox cbManDownOnEnd;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;

    private ArrayList<String> mValues;
    private int mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_man_down_detection);
        ButterKnife.bind(this);
        mValues = new ArrayList<>();
        mValues.add("BLE");
        mValues.add("GPS");
        mValues.add("BLE&GPS");
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getManDownDetectionEnable());
        orderTasks.add(OrderTaskAssembler.getManDownDetectionTimeout());
        orderTasks.add(OrderTaskAssembler.getManDownPosStrategy());
        orderTasks.add(OrderTaskAssembler.getManDownReportInterval());
        orderTasks.add(OrderTaskAssembler.getManDownStartEventNotifyEnable());
        orderTasks.add(OrderTaskAssembler.getManDownEndEventNotifyEnable());
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
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
                                    case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                    case KEY_MAN_DOWN_POS_STRATEGY:
                                    case KEY_MAN_DOWN_REPORT_INTERVAL:
                                    case KEY_MAN_DOWN_START_EVENT_NOTIFY_ENABLE:
                                    case KEY_MAN_DOWN_END_EVENT_NOTIFY_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_MAN_DOWN_DETECTION_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(ManDownDetectionActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Saved Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MAN_DOWN_DETECTION_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            cbManDownDetection.setChecked(enable == 1);
                                        }
                                        break;
                                    case KEY_MAN_DOWN_DETECTION_TIMEOUT:
                                        if (length > 0) {
                                            byte[] timeoutBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            int timeout = MokoUtils.toInt(timeoutBytes);
                                            etManDownDetectionTimeout.setText(String.valueOf(timeout));
                                        }
                                        break;
                                    case KEY_MAN_DOWN_POS_STRATEGY:
                                        if (length > 0) {
                                            int strategy = value[4] & 0xFF;
                                            mSelected = strategy;
                                            tvManDownPosStrategy.setText(mValues.get(mSelected));
                                        }
                                        break;
                                    case KEY_MAN_DOWN_REPORT_INTERVAL:
                                        if (length > 0) {
                                            byte[] intervalBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            int interval = MokoUtils.toInt(intervalBytes);
                                            etManDownReportInterval.setText(String.valueOf(interval));
                                        }
                                        break;
                                    case KEY_MAN_DOWN_START_EVENT_NOTIFY_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            cbManDownOnStart.setChecked(enable == 1);
                                        }
                                        break;
                                    case KEY_MAN_DOWN_END_EVENT_NOTIFY_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            cbManDownOnEnd.setChecked(enable == 1);
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


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }


    public void onBack(View view) {
        backHome();
    }

    @Override
    public void onBackPressed() {
        backHome();
    }

    private void backHome() {
        setResult(RESULT_OK);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Para error!");
        }
    }

    private boolean isValid() {
        final String timeoutStr = etManDownDetectionTimeout.getText().toString();
        if (TextUtils.isEmpty(timeoutStr))
            return false;
        final int timeout = Integer.parseInt(timeoutStr);
        if (timeout < 1 || timeout > 120)
            return false;
        final String intervalStr = etManDownReportInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        final int interval = Integer.parseInt(intervalStr);
        if (interval < 10 || interval > 600)
            return false;
        return true;

    }

    private void saveParams() {
        final String timeoutStr = etManDownDetectionTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        final String intervalStr = etManDownReportInterval.getText().toString();
        final int interval = Integer.parseInt(intervalStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setManDownDetectionTimeout(timeout));
        orderTasks.add(OrderTaskAssembler.setManDownReportInterval(interval));
        orderTasks.add(OrderTaskAssembler.setManDownPosStrategy(mSelected));
        orderTasks.add(OrderTaskAssembler.setManDownStartEventNotifyEnable(cbManDownOnStart.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setManDownEndEventNotifyEnable(cbManDownOnEnd.isChecked() ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setManDownDetectionEnable(cbManDownDetection.isChecked() ? 1 : 0));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void selectPosStrategy(View view) {
        if (isWindowLocked())
            return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mValues, mSelected);
        dialog.setListener(value -> {
            mSelected = value;
            tvManDownPosStrategy.setText(mValues.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }
}
