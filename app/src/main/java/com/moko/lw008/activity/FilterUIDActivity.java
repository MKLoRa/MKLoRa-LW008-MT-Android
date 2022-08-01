package com.moko.lw008.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
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
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilterUIDActivity extends BaseActivity {


    @BindView(R2.id.cb_uid)
    CheckBox cbUid;
    @BindView(R2.id.et_uid_namespace)
    EditText etUidNamespace;
    @BindView(R2.id.et_uid_instance_id)
    EditText etUidInstanceId;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_filter_uid);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidEnable());
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidNamespace());
        orderTasks.add(OrderTaskAssembler.getFilterEddystoneUidInstance());
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
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
                                    case KEY_FILTER_EDDYSTONE_UID_NAMESPACE:
                                    case KEY_FILTER_EDDYSTONE_UID_INSTANCE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_FILTER_EDDYSTONE_UID_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(FilterUIDActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_FILTER_EDDYSTONE_UID_NAMESPACE:
                                        if (length > 0) {
                                            String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                            etUidNamespace.setText(String.valueOf(uuid));
                                        }
                                        break;
                                    case KEY_FILTER_EDDYSTONE_UID_INSTANCE:
                                        if (length > 0) {
                                            String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                            etUidInstanceId.setText(String.valueOf(uuid));
                                        }
                                        break;
                                    case KEY_FILTER_EDDYSTONE_UID_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            cbUid.setChecked(enable == 1);
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
        final String namespace = etUidNamespace.getText().toString();
        final String instanceId = etUidInstanceId.getText().toString();
        if (!TextUtils.isEmpty(namespace)) {
            int length = namespace.length();
            if (length % 2 != 0) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(instanceId)) {
            int length = instanceId.length();
            if (length % 2 != 0) {
                return false;
            }
        }
        return true;
    }


    private void saveParams() {
        final String namespace = etUidNamespace.getText().toString();
        final String instanceId = etUidInstanceId.getText().toString();
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDNamespace(namespace));
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDInstance(instanceId));
        orderTasks.add(OrderTaskAssembler.setFilterEddystoneUIDEnable(cbUid.isChecked() ? 1 : 0));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
