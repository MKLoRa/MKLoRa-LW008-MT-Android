package com.moko.lw008.activity;


import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.lw008.R;
import com.moko.lw008.R2;
import com.moko.lw008.dialog.ChangePasswordDialog;
import com.moko.lw008.dialog.LoadingMessageDialog;
import com.moko.lw008.entity.TxPowerEnum;
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
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BleSettingsActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private final String FILTER_ASCII = "[ -~]*";

    @BindView(R2.id.et_adv_name)
    EditText etAdvName;
    @BindView(R2.id.et_adv_timeout)
    EditText etAdvTimeout;
    @BindView(R2.id.iv_login_mode)
    ImageView ivLoginMode;
    @BindView(R2.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R2.id.tv_tx_power_value)
    TextView tvTxPowerValue;
    @BindView(R2.id.tv_change_password)
    TextView tvChangePassword;
    private boolean savedParamsError;
    private boolean mPasswordVerifyEnable;
    private boolean mPasswordVerifyDisable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_ble_settings);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        InputFilter inputFilter = (source, start, end, dest, dstart, dend) -> {
            if (!(source + "").matches(FILTER_ASCII)) {
                return "";
            }

            return null;
        };
        etAdvName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), inputFilter});
        sbTxPower.setOnSeekBarChangeListener(this);
        showSyncingProgressDialog();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAdvName());
        orderTasks.add(OrderTaskAssembler.getAdvTxPower());
        orderTasks.add(OrderTaskAssembler.getAdvTimeout());
        orderTasks.add(OrderTaskAssembler.getPasswordVerifyEnable());
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
                                    case KEY_ADV_NAME:
                                    case KEY_ADV_TIMEOUT:
                                    case KEY_ADV_TX_POWER:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_PASSWORD_VERIFY_ENABLE:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(BleSettingsActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ADV_NAME:
                                        if (length > 0) {
                                            etAdvName.setText(new String(Arrays.copyOfRange(value, 4, 4 + length)));
                                        }
                                        break;
                                    case KEY_ADV_TIMEOUT:
                                        if (length > 0) {
                                            int timeout = value[4] & 0xFF;
                                            etAdvTimeout.setText(String.valueOf(timeout));
                                        }
                                        break;
                                    case KEY_PASSWORD_VERIFY_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            mPasswordVerifyEnable = enable == 1;
                                            mPasswordVerifyDisable = enable == 0;
                                            ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
                                            tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
                                        }
                                        break;
                                    case KEY_ADV_TX_POWER:
                                        if (length > 0) {
                                            int txPower = value[4];
                                            int progress = TxPowerEnum.fromTxPower(txPower).ordinal();
                                            sbTxPower.setProgress(progress);
                                            tvTxPowerValue.setText(String.format("%ddBm", txPower));
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
        final String advTimeoutStr = etAdvTimeout.getText().toString();
        if (TextUtils.isEmpty(advTimeoutStr))
            return false;
        final int timeout = Integer.parseInt(advTimeoutStr);
        if (timeout < 1 || timeout > 60) {
            return false;
        }
        return true;
    }


    private void saveParams() {
        final String advName = etAdvName.getText().toString();
        final String timeoutStr = etAdvTimeout.getText().toString();
        final int timeout = Integer.parseInt(timeoutStr);
        final int progress = sbTxPower.getProgress();
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        savedParamsError = false;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAdvName(advName));
        orderTasks.add(OrderTaskAssembler.setAdvTimeout(timeout));
        if (txPowerEnum != null) {
            orderTasks.add(OrderTaskAssembler.setAdvTxPower(txPowerEnum.getTxPower()));
        }
        orderTasks.add(OrderTaskAssembler.setPasswordVerifyEnable(mPasswordVerifyEnable ? 1 : 0));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onChangePassword(View view) {
        if (isWindowLocked())
            return;
        if (mPasswordVerifyDisable)
            return;
        final ChangePasswordDialog dialog = new ChangePasswordDialog(this);
        dialog.setOnPasswordClicked(password -> {
            showSyncingProgressDialog();
            LoRaLW008MokoSupport.getInstance().sendOrder(OrderTaskAssembler.changePassword(password));
        });
        dialog.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override

            public void run() {
                runOnUiThread(() -> dialog.showKeyboard());
            }
        }, 200);
    }

    public void onChangeLoginMode(View view) {
        if (isWindowLocked())
            return;
        mPasswordVerifyEnable = !mPasswordVerifyEnable;
        ivLoginMode.setImageResource(mPasswordVerifyEnable ? R.drawable.lw008_ic_checked : R.drawable.lw008_ic_unchecked);
        tvChangePassword.setVisibility(mPasswordVerifyEnable ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
        if (txPowerEnum == null)
            return;
        int txPower = txPowerEnum.getTxPower();
        tvTxPowerValue.setText(String.format("%ddBm", txPower));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
