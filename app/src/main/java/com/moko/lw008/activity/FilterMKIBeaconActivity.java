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

public class FilterMKIBeaconActivity extends BaseActivity {

    @BindView(R2.id.cb_ibeacon)
    CheckBox cbIbeacon;
    @BindView(R2.id.et_ibeacon_uuid)
    EditText etIbeaconUuid;
    @BindView(R2.id.et_ibeacon_major_min)
    EditText etIbeaconMajorMin;
    @BindView(R2.id.et_ibeacon_major_max)
    EditText etIbeaconMajorMax;
    @BindView(R2.id.et_ibeacon_minor_min)
    EditText etIbeaconMinorMin;
    @BindView(R2.id.et_ibeacon_minor_max)
    EditText etIbeaconMinorMax;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_filter_mkibeacon);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getFilterMKIBeaconEnable());
        orderTasks.add(OrderTaskAssembler.getFilterMKIBeaconUUID());
        orderTasks.add(OrderTaskAssembler.getFilterMKIBeaconMajorRange());
        orderTasks.add(OrderTaskAssembler.getFilterMKIBeaconMinorRange());
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
                                    case KEY_FILTER_MKIBEACON_UUID:
                                    case KEY_FILTER_MKIBEACON_MAJOR_RANGE:
                                    case KEY_FILTER_MKIBEACON_MINOR_RANGE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_FILTER_MKIBEACON_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(FilterMKIBeaconActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_FILTER_MKIBEACON_UUID:
                                        if (length > 0) {
                                            String uuid = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                            etIbeaconUuid.setText(String.valueOf(uuid));
                                        }
                                        break;
                                    case KEY_FILTER_MKIBEACON_MAJOR_RANGE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            if (enable == 1) {
                                                int majorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                                int majorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                                etIbeaconMajorMin.setText(String.valueOf(majorMin));
                                                etIbeaconMajorMax.setText(String.valueOf(majorMax));
                                            }
                                        }
                                        break;
                                    case KEY_FILTER_MKIBEACON_MINOR_RANGE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            if (enable == 1) {
                                                int minorMin = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                                int minorMax = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                                etIbeaconMinorMin.setText(String.valueOf(minorMin));
                                                etIbeaconMinorMax.setText(String.valueOf(minorMax));
                                            }
                                        }
                                        break;
                                    case KEY_FILTER_MKIBEACON_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            cbIbeacon.setChecked(enable == 1);
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
        final String uuid = etIbeaconUuid.getText().toString();
        final String majorMin = etIbeaconMajorMin.getText().toString();
        final String majorMax = etIbeaconMajorMax.getText().toString();
        final String minorMin = etIbeaconMinorMin.getText().toString();
        final String minorMax = etIbeaconMinorMax.getText().toString();
        if (!TextUtils.isEmpty(uuid)) {
            int length = uuid.length();
            if (length % 2 != 0) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(majorMin) && !TextUtils.isEmpty(majorMax)) {
            if (Integer.parseInt(majorMin) > 65535) {
                return false;
            }
            if (Integer.parseInt(majorMax) > 65535) {
                return false;
            }
            if (Integer.parseInt(majorMax) < Integer.parseInt(majorMin)) {
                return false;
            }
        } else if (!TextUtils.isEmpty(majorMin) && TextUtils.isEmpty(majorMax)) {
            return false;
        } else if (TextUtils.isEmpty(majorMin) && !TextUtils.isEmpty(majorMax)) {
            return false;
        }
        if (!TextUtils.isEmpty(minorMin) && !TextUtils.isEmpty(minorMax)) {
            if (Integer.parseInt(minorMin) > 65535) {
                return false;
            }
            if (Integer.parseInt(minorMax) > 65535) {
                return false;
            }
            if (Integer.parseInt(minorMax) < Integer.parseInt(minorMin)) {
                return false;
            }
        } else if (!TextUtils.isEmpty(minorMin) && TextUtils.isEmpty(minorMax)) {
            return false;
        } else if (TextUtils.isEmpty(minorMin) && !TextUtils.isEmpty(minorMax)) {
            return false;
        }
        return true;
    }


    private void saveParams() {
        final String uuid = etIbeaconUuid.getText().toString();
        final String majorMinStr = etIbeaconMajorMin.getText().toString();
        final String majorMaxStr = etIbeaconMajorMax.getText().toString();
        final String minorMinStr = etIbeaconMinorMin.getText().toString();
        final String minorMaxStr = etIbeaconMinorMax.getText().toString();
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconUUID(uuid));
        if (TextUtils.isEmpty(majorMinStr) && TextUtils.isEmpty(majorMaxStr))
            orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconMajorRange(0, 0, 0));
        else {
            final int majorMin = Integer.parseInt(majorMinStr);
            final int majorMax = Integer.parseInt(majorMaxStr);
            orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconMajorRange(1, majorMin, majorMax));
        }
        if (TextUtils.isEmpty(minorMinStr) && TextUtils.isEmpty(minorMaxStr))
            orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconMinorRange(0, 0, 0));
        else {
            final int minorMin = Integer.parseInt(minorMinStr);
            final int minorMax = Integer.parseInt(minorMaxStr);
            orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconMinorRange(1, minorMin, minorMax));
        }
        orderTasks.add(OrderTaskAssembler.setFilterMKIBeaconEnable(cbIbeacon.isChecked() ? 1 : 0));
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
