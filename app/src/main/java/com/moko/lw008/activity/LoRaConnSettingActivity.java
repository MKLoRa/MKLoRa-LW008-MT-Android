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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

public class LoRaConnSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {


    @BindView(R2.id.et_dev_eui)
    EditText etDevEui;
    @BindView(R2.id.et_app_eui)
    EditText etAppEui;
    @BindView(R2.id.et_app_key)
    EditText etAppKey;
    @BindView(R2.id.ll_modem_otaa)
    LinearLayout llModemOtaa;
    @BindView(R2.id.et_dev_addr)
    EditText etDevAddr;
    @BindView(R2.id.et_nwk_skey)
    EditText etNwkSkey;
    @BindView(R2.id.et_app_skey)
    EditText etAppSkey;
    @BindView(R2.id.ll_modem_abp)
    LinearLayout llModemAbp;
    @BindView(R2.id.tv_ch_1)
    TextView tvCh1;
    @BindView(R2.id.tv_ch_2)
    TextView tvCh2;
    @BindView(R2.id.cb_adr)
    CheckBox cbAdr;
    @BindView(R2.id.tv_upload_mode)
    TextView tvUploadMode;
    @BindView(R2.id.tv_region)
    TextView tvRegion;
    @BindView(R2.id.tv_message_type)
    TextView tvMessageType;
    @BindView(R2.id.ll_advanced_setting)
    LinearLayout llAdvancedSetting;
    @BindView(R2.id.cb_advance_setting)
    CheckBox cbAdvanceSetting;
    @BindView(R2.id.cb_duty_cycle)
    CheckBox cbDutyCycle;
    @BindView(R2.id.rl_ch)
    RelativeLayout rlCH;
    @BindView(R2.id.ll_duty_cycle)
    LinearLayout llDutyCycle;
    @BindView(R2.id.tv_dr)
    TextView tvDr;
    @BindView(R2.id.rl_dr)
    RelativeLayout rlDr;
    @BindView(R2.id.tv_dr_1)
    TextView tvDr1;
    @BindView(R2.id.tv_dr_2)
    TextView tvDr2;
    @BindView(R2.id.ll_adr_options)
    LinearLayout llAdrOptions;
    @BindView(R2.id.tv_max_retransmission_times)
    TextView tvMaxRetransmissionTimes;
    @BindView(R2.id.rl_max_retransmission_times)
    RelativeLayout rlMaxRetransmissionTimes;
    @BindView(R2.id.et_adr_ack_limit)
    EditText etAdrAckLimit;
    @BindView(R2.id.et_adr_ack_delay)
    EditText etAdrAckDelay;

    private boolean mReceiverTag = false;
    private ArrayList<String> mModeList;
    private ArrayList<String> mRegionsList;
    private ArrayList<String> mMessageTypeList;
    private ArrayList<String> mMaxRetransmissionTimesList;
    private int mSelectedMode;
    private int mSelectedRegion;
    private int mSelectedMessageType;
    private int mSelectedCh1;
    private int mSelectedCh2;
    private int mSelectedDr;
    private int mSelectedDr1;
    private int mSelectedDr2;
    private int mSelectedMaxRetransmissionTimes;
    private int mMaxCH;
    private int mMaxDR;
    private boolean savedParamsError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lw008_activity_conn_setting);
        ButterKnife.bind(this);
        mModeList = new ArrayList<>();
        mModeList.add("ABP");
        mModeList.add("OTAA");
        mRegionsList = new ArrayList<>();
        mRegionsList.add("AS923");
        mRegionsList.add("AU915");
        mRegionsList.add("CN470");
        mRegionsList.add("CN779");
        mRegionsList.add("EU433");
        mRegionsList.add("EU868");
        mRegionsList.add("KR920");
        mRegionsList.add("IN865");
        mRegionsList.add("US915");
        mRegionsList.add("RU864");
        mMessageTypeList = new ArrayList<>();
        mMessageTypeList.add("Unconfirmed");
        mMessageTypeList.add("Confirmed");
        mMaxRetransmissionTimesList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            mMaxRetransmissionTimesList.add(String.valueOf(i));
        }
        cbAdvanceSetting.setOnCheckedChangeListener(this);
        cbAdr.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!LoRaLW008MokoSupport.getInstance().isBluetoothOpen()) {
            LoRaLW008MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            List<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getLoraUploadMode());
            orderTasks.add(OrderTaskAssembler.getLoraDevEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppEUI());
            orderTasks.add(OrderTaskAssembler.getLoraAppKey());
            orderTasks.add(OrderTaskAssembler.getLoraDevAddr());
            orderTasks.add(OrderTaskAssembler.getLoraAppSKey());
            orderTasks.add(OrderTaskAssembler.getLoraNwkSKey());
            orderTasks.add(OrderTaskAssembler.getLoraRegion());
            orderTasks.add(OrderTaskAssembler.getLoraMessageType());
            orderTasks.add(OrderTaskAssembler.getLoraCH());
            orderTasks.add(OrderTaskAssembler.getLoraDutyCycleEnable());
            orderTasks.add(OrderTaskAssembler.getLoraDR());
            orderTasks.add(OrderTaskAssembler.getLoraMaxRetransmissionTimes());
            orderTasks.add(OrderTaskAssembler.getLoraAdrAckLimit());
            orderTasks.add(OrderTaskAssembler.getLoraAdrAckDelay());
            orderTasks.add(OrderTaskAssembler.getLoraUplinkStrategy());
            LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
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
                                    case KEY_LORA_MODE:
                                    case KEY_LORA_DEV_EUI:
                                    case KEY_LORA_APP_EUI:
                                    case KEY_LORA_APP_KEY:
                                    case KEY_LORA_DEV_ADDR:
                                    case KEY_LORA_APP_SKEY:
                                    case KEY_LORA_NWK_SKEY:
                                    case KEY_LORA_REGION:
                                    case KEY_LORA_MESSAGE_TYPE:
                                    case KEY_LORA_CH:
                                    case KEY_LORA_DR:
                                    case KEY_LORA_DUTYCYCLE:
                                    case KEY_LORA_MAX_RETRANSMISSION_TIMES:
                                    case KEY_LORA_ADR_ACK_LIMIT:
                                    case KEY_LORA_ADR_ACK_DELAY:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        break;
                                    case KEY_LORA_UPLINK_STRATEGY:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(LoRaConnSettingActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            showSyncingProgressDialog();
                                            LoRaLW008MokoSupport.getInstance().sendOrder(OrderTaskAssembler.restart());
                                        }
                                        break;
                                    case KEY_REBOOT:
                                        if (result != 1) {
                                            savedParamsError = true;
                                        }
                                        if (savedParamsError) {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Save Successfully！");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_LORA_MODE:
                                        if (length > 0) {
                                            final int mode = value[4];
                                            tvUploadMode.setText(mModeList.get(mode - 1));
                                            mSelectedMode = mode - 1;
                                            if (mode == 1) {
                                                llModemAbp.setVisibility(View.VISIBLE);
                                                llModemOtaa.setVisibility(View.GONE);
                                            } else {
                                                llModemAbp.setVisibility(View.GONE);
                                                llModemOtaa.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        break;
                                    case KEY_LORA_DEV_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etDevEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etAppEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_KEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etAppKey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_DEV_ADDR:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etDevAddr.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etAppSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_NWK_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            etNwkSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_REGION:
                                        if (length > 0) {
                                            final int region = value[4] & 0xFF;
                                            mSelectedRegion = region;
                                            tvRegion.setText(mRegionsList.get(region));
                                            initCHDRRange();
                                            initDutyCycle();
                                        }
                                        break;
                                    case KEY_LORA_MESSAGE_TYPE:
                                        if (length > 0) {
                                            final int messageType = value[4] & 0xFF;
                                            mSelectedMessageType = messageType;
                                            tvMessageType.setText(mMessageTypeList.get(messageType));
                                            rlMaxRetransmissionTimes.setVisibility(mSelectedMessageType == 0 ? View.GONE : View.VISIBLE);
                                        }
                                        break;
                                    case KEY_LORA_CH:
                                        if (length > 1) {
                                            final int ch1 = value[4] & 0xFF;
                                            final int ch2 = value[5] & 0xFF;
                                            mSelectedCh1 = ch1;
                                            mSelectedCh2 = ch2;
                                            tvCh1.setText(String.valueOf(ch1));
                                            tvCh2.setText(String.valueOf(ch2));
                                        }
                                        break;
                                    case KEY_LORA_DUTYCYCLE:
                                        if (length > 0) {
                                            final int dutyCycleEnable = value[4] & 0xFF;
                                            cbDutyCycle.setChecked(dutyCycleEnable == 1);
                                        }
                                        break;
                                    case KEY_LORA_DR:
                                        if (length > 0) {
                                            final int dr = value[4] & 0xFF;
                                            mSelectedDr = dr;
                                            tvDr.setText(String.valueOf(dr));
                                        }
                                        break;
                                    case KEY_LORA_MAX_RETRANSMISSION_TIMES:
                                        if (length > 0) {
                                            final int times = value[4] & 0xFF;
                                            mSelectedMaxRetransmissionTimes = times - 1;
                                            tvMaxRetransmissionTimes.setText(mMaxRetransmissionTimesList.get(mSelectedMaxRetransmissionTimes));
                                        }
                                        break;
                                    case KEY_LORA_ADR_ACK_LIMIT:
                                        if (length > 0) {
                                            etAdrAckLimit.setText(String.valueOf(value[4] & 0xFF));
                                        }
                                        break;
                                    case KEY_LORA_ADR_ACK_DELAY:
                                        if (length > 0) {
                                            etAdrAckDelay.setText(String.valueOf(value[4] & 0xFF));
                                        }
                                        break;
                                    case KEY_LORA_UPLINK_STRATEGY:
                                        if (length > 0) {
                                            final int adr = value[4] & 0xFF;
                                            cbAdr.setChecked(adr == 1);
                                            llAdrOptions.setVisibility(cbAdr.isChecked() ? View.GONE : View.VISIBLE);
                                            final int dr1 = value[6] & 0xFF;
                                            mSelectedDr1 = dr1;
                                            final int dr2 = value[7] & 0xFF;
                                            mSelectedDr2 = dr2;
                                            tvDr1.setText(String.valueOf(dr1));
                                            tvDr2.setText(String.valueOf(dr2));
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

    public void selectMode(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mModeList, mSelectedMode);
        bottomDialog.setListener(value -> {
            tvUploadMode.setText(mModeList.get(value));
            mSelectedMode = value;
            if (value == 0) {
                llModemAbp.setVisibility(View.VISIBLE);
                llModemOtaa.setVisibility(View.GONE);
            } else {
                llModemAbp.setVisibility(View.GONE);
                llModemOtaa.setVisibility(View.VISIBLE);
            }

        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectRegion(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mRegionsList, mSelectedRegion);
        bottomDialog.setListener(value -> {
            if (mSelectedRegion != value) {
                cbAdr.setChecked(true);
                mSelectedRegion = value;
                tvRegion.setText(mRegionsList.get(value));
                initCHDRRange();
                updateCHDR();
                initDutyCycle();
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectMessageType(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mMessageTypeList, mSelectedMessageType);
        bottomDialog.setListener(value -> {
            tvMessageType.setText(mMessageTypeList.get(value));
            mSelectedMessageType = value;
            rlMaxRetransmissionTimes.setVisibility(mSelectedMessageType == 0 ? View.GONE : View.VISIBLE);
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    private void updateCHDR() {
        switch (mSelectedRegion) {
            case 1:
            case 8:
                // AU915、US915
                mSelectedCh1 = 8;
                mSelectedCh2 = 15;
                mSelectedDr = 0;
                break;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // CN779、EU443、EU868、KR920、IN865
                mSelectedCh1 = 0;
                mSelectedCh2 = 2;
                mSelectedDr = 0;
                break;
            case 2:
                // CN470
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
                mSelectedDr = 0;
                break;
            case 0:
            case 9:
                // AS923、RU864
                mSelectedCh1 = 0;
                mSelectedCh2 = 1;
                mSelectedDr = 0;
                break;
        }
        if (mSelectedRegion == 0 || mSelectedRegion == 1) {
            mSelectedDr1 = 2;
            mSelectedDr2 = 2;
        } else {
            mSelectedDr1 = 0;
            mSelectedDr2 = 0;
        }
        tvCh1.setText(String.valueOf(mSelectedCh1));
        tvCh2.setText(String.valueOf(mSelectedCh2));
        tvDr.setText(String.valueOf(mSelectedDr));
        tvDr1.setText(String.valueOf(mSelectedDr1));
        tvDr2.setText(String.valueOf(mSelectedDr2));
    }

    private ArrayList<String> mCHList;
    private ArrayList<String> mDRList;

    private void initCHDRRange() {
        mCHList = new ArrayList<>();
        mDRList = new ArrayList<>();
        switch (mSelectedRegion) {
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                // CN779、EU443、EU868、KR920、IN865
                mMaxCH = 2;
                mMaxDR = 5;
                break;
            case 1:
                // AU915
                mMaxCH = 63;
                mMaxDR = 6;
                break;
            case 2:
                // CN470
                mMaxCH = 95;
                mMaxDR = 5;
                break;
            case 8:
                // US915
                mMaxCH = 63;
                mMaxDR = 4;
                break;
            case 0:
            case 9:
                // AS923、RU864
                mMaxCH = 1;
                mMaxDR = 5;
                break;
        }
        for (int i = 0; i <= mMaxCH; i++) {
            mCHList.add(String.valueOf(i));
        }
        int minDR = 0;
        if (mSelectedRegion == 0 || mSelectedRegion == 1) {
            // AS923,AU915
            minDR = 2;
        }
        for (int i = minDR; i <= mMaxDR; i++) {
            mDRList.add(String.valueOf(i));
        }
        if (mSelectedRegion == 1 || mSelectedRegion == 2 || mSelectedRegion == 8) {
            // US915,AU915,CN470
            rlCH.setVisibility(View.VISIBLE);
        } else {
            rlCH.setVisibility(View.GONE);
        }
        if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 8) {
            // AS923,US915,AU915
            rlDr.setVisibility(View.GONE);
        } else {
            rlDr.setVisibility(View.VISIBLE);
        }
    }

    private void initDutyCycle() {
        if (mSelectedRegion == 3 || mSelectedRegion == 4
                || mSelectedRegion == 5 || mSelectedRegion == 9) {
            cbDutyCycle.setChecked(false);
            // CN779,EU433,EU868 and RU864
            llDutyCycle.setVisibility(View.VISIBLE);
        } else {
            llDutyCycle.setVisibility(View.GONE);
        }
    }


    public void selectCh1(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mCHList, mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh1 = value;
            tvCh1.setText(mCHList.get(value));
            if (mSelectedCh1 > mSelectedCh2) {
                mSelectedCh2 = mSelectedCh1;
                tvCh2.setText(mCHList.get(value));
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectCh2(View view) {
        if (isWindowLocked())
            return;
        final ArrayList<String> ch2List = new ArrayList<>();
        for (int i = mSelectedCh1; i <= mMaxCH; i++) {
            ch2List.add(i + "");
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(ch2List, mSelectedCh2 - mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh2 = value + mSelectedCh1;
            tvCh2.setText(ch2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr1(View view) {
        if (isWindowLocked())
            return;
        if (mSelectedRegion == 0 || mSelectedRegion == 1) {
            BottomDialog bottomDialog = new BottomDialog();
            bottomDialog.setDatas(mDRList, mSelectedDr1 - 2);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value + 2;
                tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    tvDr2.setText(mDRList.get(value));
                }
            });
            bottomDialog.show(getSupportFragmentManager());
        } else {
            BottomDialog bottomDialog = new BottomDialog();
            bottomDialog.setDatas(mDRList, mSelectedDr1);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value;
                tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    tvDr2.setText(mDRList.get(value));
                }
            });
            bottomDialog.show(getSupportFragmentManager());
        }
    }

    public void selectDr2(View view) {
        if (isWindowLocked())
            return;
        final ArrayList<String> dr2List = new ArrayList<>();
        for (int i = mSelectedDr1; i <= mMaxDR; i++) {
            dr2List.add(i + "");
        }
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(dr2List, mSelectedDr2 - mSelectedDr1);
        bottomDialog.setListener(value -> {
            mSelectedDr2 = value + mSelectedDr1;
            tvDr2.setText(dr2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDrForJoin(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mDRList, mSelectedDr);
        bottomDialog.setListener(value -> {
            mSelectedDr = value;
            tvDr.setText(mDRList.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectMaxRetransmissionTimes(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mMaxRetransmissionTimesList, mSelectedMaxRetransmissionTimes);
        bottomDialog.setListener(value -> {
            mSelectedMaxRetransmissionTimes = value;
            tvMaxRetransmissionTimes.setText(mMaxRetransmissionTimesList.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        String adrAckLimitStr = etAdrAckLimit.getText().toString();
        String adrAckDelayStr = etAdrAckDelay.getText().toString();
        if (TextUtils.isEmpty(adrAckLimitStr)) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        int adrAckLimit = Integer.parseInt(adrAckLimitStr);
        if (adrAckLimit < 1 || adrAckLimit > 100) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        if (TextUtils.isEmpty(adrAckDelayStr)) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        int adrAckDelay = Integer.parseInt(adrAckDelayStr);
        if (adrAckDelay < 1 || adrAckDelay > 100) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (mSelectedMode == 0) {
            String devEui = etDevEui.getText().toString();
            String appEui = etAppEui.getText().toString();
            String devAddr = etDevAddr.getText().toString();
            String appSkey = etAppSkey.getText().toString();
            String nwkSkey = etNwkSkey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (devAddr.length() != 8) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appSkey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (nwkSkey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEUI(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEUI(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraDevAddr(devAddr));
            orderTasks.add(OrderTaskAssembler.setLoraAppSKey(appSkey));
            orderTasks.add(OrderTaskAssembler.setLoraNwkSKey(nwkSkey));
        } else {
            String devEui = etDevEui.getText().toString();
            String appEui = etAppEui.getText().toString();
            String appKey = etAppKey.getText().toString();
            if (devEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appEui.length() != 16) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            if (appKey.length() != 32) {
                ToastUtils.showToast(this, "Para error!");
                return;
            }
            orderTasks.add(OrderTaskAssembler.setLoraDevEUI(devEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppEUI(appEui));
            orderTasks.add(OrderTaskAssembler.setLoraAppKey(appKey));
        }
        orderTasks.add(OrderTaskAssembler.setLoraUploadMode(mSelectedMode + 1));
        orderTasks.add(OrderTaskAssembler.setLoraMessageType(mSelectedMessageType));
        if (mSelectedMessageType == 1) {
            orderTasks.add(OrderTaskAssembler.setLoraMaxRetransmissionTimes(mSelectedMaxRetransmissionTimes + 1));
        }
        savedParamsError = false;
        // 保存并连接
        orderTasks.add(OrderTaskAssembler.setLoraRegion(mSelectedRegion));
        if (mSelectedRegion == 1 || mSelectedRegion == 2 || mSelectedRegion == 8) {
            // US915,AU915,CN470
            orderTasks.add(OrderTaskAssembler.setLoraCH(mSelectedCh1, mSelectedCh2));
        }
        if (mSelectedRegion == 3 || mSelectedRegion == 4
                || mSelectedRegion == 5 || mSelectedRegion == 9) {
            // CN779,EU433,EU868 and RU864
            orderTasks.add(OrderTaskAssembler.setLoraDutyCycleEnable(cbDutyCycle.isChecked() ? 1 : 0));
        }
        if (mSelectedRegion != 0 && mSelectedRegion != 1 && mSelectedRegion != 8) {
            // AS923,US915,AU915
            orderTasks.add(OrderTaskAssembler.setLoraDR(mSelectedDr));
        }
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckLimit(adrAckLimit));
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckDelay(adrAckDelay));
        // 数据发送次数默认为1
        orderTasks.add(OrderTaskAssembler.setLoraUplinkStrategy(cbAdr.isChecked() ? 1 : 0, 1, mSelectedDr1, mSelectedDr2));
        LoRaLW008MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        showSyncingProgressDialog();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_advance_setting) {
            llAdvancedSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (buttonView.getId() == R.id.cb_adr) {
            llAdrOptions.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }
    }
}
