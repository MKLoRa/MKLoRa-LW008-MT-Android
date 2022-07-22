package com.moko.lw008.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw008.R;
import com.moko.lw008.R2;
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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AxisSettingActivity extends BaseActivity {

    @BindView(R2.id.et_wakeup_threshold)
    EditText etWakeupThreshold;
    @BindView(R2.id.et_wakeup_duration)
    EditText etWakeupDuration;
    @BindView(R2.id.et_motion_threshold)
    EditText etMotionThreshold;
    @BindView(R2.id.et_motion_duration)
    EditText etMotionDuration;
    private boolean mReceiverTag = false;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_axis_setting);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAccWakeupThreshold());
        orderTasks.add(OrderTaskAssembler.getAccWakeupDuration());
        orderTasks.add(OrderTaskAssembler.getAccMotionThreshold());
        orderTasks.add(OrderTaskAssembler.getAccMotionDuration());
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
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
                                    case KEY_ACC_WAKEUP_THRESHOLD:
                                    case KEY_ACC_WAKEUP_DURATION:
                                    case KEY_ACC_MOTION_THRESHOLD:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_ACC_MOTION_DURATION:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(AxisSettingActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Saved Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ACC_WAKEUP_THRESHOLD:
                                        if (length > 0) {
                                            int threshold = value[4] & 0xFF;
                                            etWakeupThreshold.setText(String.valueOf(threshold));

                                        }
                                        break;
                                    case KEY_ACC_WAKEUP_DURATION:
                                        if (length > 0) {
                                            int duration = value[4] & 0xFF;
                                            etWakeupDuration.setText(String.valueOf(duration));
                                        }
                                        break;
                                    case KEY_ACC_MOTION_THRESHOLD:
                                        if (length > 0) {
                                            int threshold = value[4] & 0xFF;
                                            etMotionThreshold.setText(String.valueOf(threshold));

                                        }
                                        break;
                                    case KEY_ACC_MOTION_DURATION:
                                        if (length > 0) {
                                            int duration = value[4] & 0xFF;
                                            etMotionDuration.setText(String.valueOf(duration));
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
        final String wakeUpThresholdStr = etWakeupThreshold.getText().toString();
        if (TextUtils.isEmpty(wakeUpThresholdStr))
            return false;
        final int wakeUpThreshold = Integer.parseInt(wakeUpThresholdStr);
        if (wakeUpThreshold < 1 || wakeUpThreshold > 20)
            return false;
        final String wakeUpDurationStr = etWakeupDuration.getText().toString();
        if (TextUtils.isEmpty(wakeUpDurationStr))
            return false;
        final int wakeUpDuration = Integer.parseInt(wakeUpDurationStr);
        if (wakeUpDuration < 1 || wakeUpDuration > 10)
            return false;
        final String motionThresholdStr = etMotionThreshold.getText().toString();
        if (TextUtils.isEmpty(motionThresholdStr))
            return false;
        final int motionThreshold = Integer.parseInt(motionThresholdStr);
        if (motionThreshold < 10 || motionThreshold > 250)
            return false;
        final String motionDurationStr = etMotionDuration.getText().toString();
        if (TextUtils.isEmpty(motionDurationStr))
            return false;
        final int motionDuration = Integer.parseInt(motionDurationStr);
        if (motionDuration < 1 || motionDuration > 50)
            return false;
        return true;

    }

    private void saveParams() {
        final String wakeUpThresholdStr = etWakeupThreshold.getText().toString();
        final int wakeUpThreshold = Integer.parseInt(wakeUpThresholdStr);
        final String wakeUpDurationStr = etWakeupDuration.getText().toString();
        final int wakeUpDuration = Integer.parseInt(wakeUpDurationStr);
        final String motionThresholdStr = etMotionThreshold.getText().toString();
        final int motionThreshold = Integer.parseInt(motionThresholdStr);
        final String motionDurationStr = etMotionDuration.getText().toString();
        final int motionDuration = Integer.parseInt(motionDurationStr);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAccWakeupThreshold(wakeUpThreshold));
        orderTasks.add(OrderTaskAssembler.setAccWakeupDuration(wakeUpDuration));
        orderTasks.add(OrderTaskAssembler.setAccMotionThreshold(motionThreshold));
        orderTasks.add(OrderTaskAssembler.setAccMotionDuration(motionDuration));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
