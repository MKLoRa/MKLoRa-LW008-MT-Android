package com.moko.lw008.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.lib.loraiot.IoTDMConstants;
import com.moko.lib.loraiot.Urls;
import com.moko.lib.loraiot.dialog.LoginDialog;
import com.moko.lib.loraiot.dialog.LogoutDialog;
import com.moko.lib.loraiot.entity.CommonResp;
import com.moko.lib.loraiot.entity.LoginEntity;
import com.moko.lib.loraiot.utils.IoTDMSPUtils;
import com.moko.lib.loraui.dialog.BottomDialog;
import com.moko.lw008.R;
import com.moko.lw008.databinding.Lw008ActivityConnSettingBinding;
import com.moko.lw008.utils.ToastUtils;
import com.moko.support.lw008.LoRaLW008MokoSupport;
import com.moko.support.lw008.OrderTaskAssembler;
import com.moko.support.lw008.entity.OrderCHAR;
import com.moko.support.lw008.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import okhttp3.RequestBody;

public class LoRaConnSettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private Lw008ActivityConnSettingBinding mBind;

    private boolean mReceiverTag = false;
    private ArrayList<String> mModeList;
    private ArrayList<String> mRegionsList;
    private ArrayList<String> mMessageTypeList;
    private ArrayList<String> mMaxRetransmissionTimesList;
    private ArrayList<String> mServerRegionsList;
    private ArrayList<String> mServerPlatformList;
    private int mSelectedMode;
    private int mSelectedRegion;
    private int mSelectedMessageType;
    private int mSelectedServerRegion;
    private int mSelectedPlatform;
    private int mSelectedCh1;
    private int mSelectedCh2;
    private int mSelectedDr;
    private int mSelectedDr1;
    private int mSelectedDr2;
    private int mSelectedMaxRetransmissionTimes;
    private int mMaxCH;
    private int mMaxDR;
    private boolean savedParamsError;

    private String mRemoteDevEUI = "";
    private String mRemoteAPPEUI = "70b3d57ed0026b87";
    private String mRemoteAPPKEY = "";

    private String[] mRegionsArray = {"AS923", "AU915", "CN470", "CN779", "EU433", "EU868", "KR920", "IN865", "US915", "RU864"};
    private String[] mServerRegionsArray = {"AS923", "EU868", "US915 FSB1", "US915 FSB2", "AU915 FSB1", "AU915 FSB2"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = Lw008ActivityConnSettingBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        mModeList = new ArrayList<>();
        mModeList.add("ABP");
        mModeList.add("OTAA");

        mServerPlatformList = new ArrayList<>();
        mServerPlatformList.add("Third Party NS");
        mServerPlatformList.add("MOKO IoT DM");
        mSelectedPlatform = 0;

        mRegionsList = new ArrayList<>(Arrays.asList(mRegionsArray));
        mServerRegionsList = new ArrayList<>(Arrays.asList(mServerRegionsArray));
        mSelectedServerRegion = 1;
        mSelectedRegion = 5;
        mMessageTypeList = new ArrayList<>();
        mMessageTypeList.add("Unconfirmed");
        mMessageTypeList.add("Confirmed");
        mMaxRetransmissionTimesList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            mMaxRetransmissionTimesList.add(String.valueOf(i));
        }
        mBind.cbAdvanceSetting.setOnCheckedChangeListener(this);
        mBind.cbAdr.setOnCheckedChangeListener(this);
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
            orderTasks.add(OrderTaskAssembler.getMacAddress());
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
                                            mBind.tvUploadMode.setText(mModeList.get(mode - 1));
                                            mSelectedMode = mode - 1;
                                            if (mode == 1) {
                                                mBind.llModemAbp.setVisibility(View.VISIBLE);
                                                mBind.llModemOtaa.setVisibility(View.GONE);
                                            } else {
                                                mBind.llModemAbp.setVisibility(View.GONE);
                                                mBind.llModemOtaa.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        break;
                                    case KEY_CHIP_MAC:
                                        if (length > 0) {
                                            byte[] macBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            String mac = MokoUtils.bytesToHexString(macBytes);
                                            mRemoteDevEUI = String.format("%sffff%s", mac.substring(0, 6), mac.substring(6, 12));
                                            mRemoteAPPKEY = String.format("2b7e151628aed2a6abf7%s", mac);
                                        }
                                        break;
                                    case KEY_LORA_DEV_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etDevEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_EUI:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etAppEui.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_KEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etAppKey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_DEV_ADDR:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etDevAddr.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_APP_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etAppSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_NWK_SKEY:
                                        if (length > 0) {
                                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                                            mBind.etNwkSkey.setText(MokoUtils.bytesToHexString(rawDataBytes));
                                        }
                                        break;
                                    case KEY_LORA_REGION:
                                        if (length > 0) {
                                            final int region = value[4] & 0xFF;
                                            mSelectedRegion = region;
                                            mBind.tvRegion.setText(mRegionsList.get(region));
                                            initCHDRRange();
                                            initDutyCycle();
                                        }
                                        break;
                                    case KEY_LORA_MESSAGE_TYPE:
                                        if (length > 0) {
                                            final int messageType = value[4] & 0xFF;
                                            mSelectedMessageType = messageType;
                                            mBind.tvMessageType.setText(mMessageTypeList.get(messageType));
                                            mBind.rlMaxRetransmissionTimes.setVisibility(mSelectedMessageType == 0 ? View.GONE : View.VISIBLE);
                                        }
                                        break;
                                    case KEY_LORA_CH:
                                        if (length > 1) {
                                            final int ch1 = value[4] & 0xFF;
                                            final int ch2 = value[5] & 0xFF;
                                            mSelectedCh1 = ch1;
                                            mSelectedCh2 = ch2;
                                            mBind.tvCh1.setText(String.valueOf(ch1));
                                            mBind.tvCh2.setText(String.valueOf(ch2));
                                        }
                                        break;
                                    case KEY_LORA_DUTYCYCLE:
                                        if (length > 0) {
                                            final int dutyCycleEnable = value[4] & 0xFF;
                                            mBind.cbDutyCycle.setChecked(dutyCycleEnable == 1);
                                        }
                                        break;
                                    case KEY_LORA_DR:
                                        if (length > 0) {
                                            final int dr = value[4] & 0xFF;
                                            mSelectedDr = dr;
                                            mBind.tvDr.setText(String.valueOf(dr));
                                        }
                                        break;
                                    case KEY_LORA_MAX_RETRANSMISSION_TIMES:
                                        if (length > 0) {
                                            final int times = value[4] & 0xFF;
                                            mSelectedMaxRetransmissionTimes = times - 1;
                                            mBind.tvMaxRetransmissionTimes.setText(mMaxRetransmissionTimesList.get(mSelectedMaxRetransmissionTimes));
                                        }
                                        break;
                                    case KEY_LORA_ADR_ACK_LIMIT:
                                        if (length > 0) {
                                            mBind.etAdrAckLimit.setText(String.valueOf(value[4] & 0xFF));
                                        }
                                        break;
                                    case KEY_LORA_ADR_ACK_DELAY:
                                        if (length > 0) {
                                            mBind.etAdrAckDelay.setText(String.valueOf(value[4] & 0xFF));
                                        }
                                        break;
                                    case KEY_LORA_UPLINK_STRATEGY:
                                        if (length > 0) {
                                            final int adr = value[4] & 0xFF;
                                            mBind.cbAdr.setChecked(adr == 1);
                                            mBind.llAdrOptions.setVisibility(mBind.cbAdr.isChecked() ? View.GONE : View.VISIBLE);
                                            final int dr1 = value[6] & 0xFF;
                                            mSelectedDr1 = dr1;
                                            final int dr2 = value[7] & 0xFF;
                                            mSelectedDr2 = dr2;
                                            mBind.tvDr1.setText(String.valueOf(dr1));
                                            mBind.tvDr2.setText(String.valueOf(dr2));
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
        if (EventBus.getDefault().isRegistered(this))
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
        EventBus.getDefault().unregister(this);
        setResult(RESULT_OK);
        finish();
    }

    public void selectMode(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mModeList, mSelectedMode);
        bottomDialog.setListener(value -> {
            mBind.tvUploadMode.setText(mModeList.get(value));
            mSelectedMode = value;
            if (value == 0) {
                mBind.llModemAbp.setVisibility(View.VISIBLE);
                mBind.llModemOtaa.setVisibility(View.GONE);
            } else {
                mBind.llModemAbp.setVisibility(View.GONE);
                mBind.llModemOtaa.setVisibility(View.VISIBLE);
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
                mBind.cbAdr.setChecked(true);
                mSelectedRegion = value;
                mBind.tvRegion.setText(mRegionsList.get(value));
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
            mBind.tvMessageType.setText(mMessageTypeList.get(value));
            mSelectedMessageType = value;
            mBind.rlMaxRetransmissionTimes.setVisibility(mSelectedMessageType == 0 ? View.GONE : View.VISIBLE);
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectServerRegion(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mServerRegionsList, mSelectedServerRegion);
        bottomDialog.setListener(value -> {
            if (mSelectedServerRegion != value) {
                mBind.cbAdr.setChecked(true);
                mSelectedServerRegion = value;
                mBind.tvServerRegion.setText(mServerRegionsList.get(value));
                initCHDRRange();
                updateCHDR();
                initDutyCycle();
            }
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectServerPlatform(View view) {
        if (isWindowLocked()) return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mServerPlatformList, mSelectedPlatform);
        bottomDialog.setListener(value -> {
            mSelectedPlatform = value;
            mBind.tvServerPlatform.setText(mServerPlatformList.get(value));
            if (mSelectedPlatform == 0) {
                mBind.llModemParams.setVisibility(View.VISIBLE);
                mBind.llGatewayId.setVisibility(View.GONE);
                mBind.tvDevEUI.setVisibility(View.GONE);
                mBind.rlCh.setVisibility(View.GONE);
                mBind.rlRegion.setVisibility(View.VISIBLE);
                mBind.rlServerRegion.setVisibility(View.GONE);
            } else {
                // MK IoT DM
                mBind.llModemParams.setVisibility(View.GONE);
                mBind.llGatewayId.setVisibility(View.VISIBLE);
                mBind.tvDevEUI.setVisibility(View.VISIBLE);
                mBind.rlRegion.setVisibility(View.GONE);
                mBind.rlServerRegion.setVisibility(View.VISIBLE);

            }
            initCHDRRange();
            updateCHDR();
            initDutyCycle();
            if (mSelectedPlatform == 0) return;
            mBind.tvDevEUI.setText(String.format("DevEUI:%s", mRemoteDevEUI.toUpperCase()));
            mAccount = IoTDMSPUtils.getStringValue(this, IoTDMConstants.SP_LOGIN_ACCOUNT, "");
            mPassword = IoTDMSPUtils.getStringValue(this, IoTDMConstants.SP_LOGIN_PASSWORD, "");
            if (TextUtils.isEmpty(mAccount))
                mBind.llAccount.setVisibility(View.GONE);
            else
                mBind.tvAccount.setText(String.format("Account:%s", mAccount));
            if (TextUtils.isEmpty(mPassword))
                mBind.llAccount.setVisibility(View.GONE);
            else
                mBind.llAccount.setVisibility(View.VISIBLE);
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    private ArrayList<String> mCHList;
    private ArrayList<String> mDRList;

    private void initCHDRRange() {
        mCHList = new ArrayList<>();
        mDRList = new ArrayList<>();
        if (mSelectedPlatform == 0) {
            if (mSelectedRegion == 1) {
                // AU915
                mMaxCH = 63;
                mMaxDR = 6;
            } else if (mSelectedRegion == 2) {
                // CN470
                mMaxCH = 95;
                mMaxDR = 5;
            } else if (mSelectedRegion == 8) {
                // US915
                mMaxCH = 63;
                mMaxDR = 4;
            } else if (mSelectedRegion == 0 || mSelectedRegion == 9) {
                // AS923、RU864
                mMaxCH = 1;
                mMaxDR = 5;
            } else {
                // CN779、EU443、EU868、KR920、IN865
                mMaxCH = 2;
                mMaxDR = 5;
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
                mBind.rlCh.setVisibility(View.VISIBLE);
            } else {
                mBind.rlCh.setVisibility(View.GONE);
            }
            if (mSelectedRegion == 0 || mSelectedRegion == 1 || mSelectedRegion == 8) {
                // AS923,US915,AU915
                mBind.rlDr.setVisibility(View.GONE);
            } else {
                mBind.rlDr.setVisibility(View.VISIBLE);
            }
        } else {
            int minDR = 0;
            if (mSelectedServerRegion == 0) {
                // AS923
                mMaxCH = 1;
                minDR = 2;
                mMaxDR = 5;
                mBind.rlCh.setVisibility(View.GONE);
                mBind.rlDr.setVisibility(View.GONE);
            } else if (mSelectedServerRegion == 1) {
                // EU868
                mMaxCH = 2;
                mMaxDR = 5;
                mBind.rlCh.setVisibility(View.GONE);
                mBind.rlDr.setVisibility(View.VISIBLE);
            } else if (mSelectedServerRegion == 2 || mSelectedServerRegion == 3) {
                // US915
                mMaxCH = 63;
                mMaxDR = 4;
                mBind.rlCh.setVisibility(View.VISIBLE);
                mBind.rlDr.setVisibility(View.GONE);
            } else if (mSelectedServerRegion == 4 || mSelectedServerRegion == 5) {
                // AU915
                mMaxCH = 63;
                minDR = 2;
                mMaxDR = 6;
                mBind.rlCh.setVisibility(View.VISIBLE);
                mBind.rlDr.setVisibility(View.GONE);
            }
            for (int i = 0; i <= mMaxCH; i++) {
                mCHList.add(String.valueOf(i));
            }
            for (int i = minDR; i <= mMaxDR; i++) {
                mDRList.add(String.valueOf(i));
            }
        }
    }

    private void updateCHDR() {
        if (mSelectedPlatform == 0) {
            if (mSelectedRegion == 1 || mSelectedRegion == 8) {
                // AU915、US915
                mSelectedCh1 = 8;
                mSelectedCh2 = 15;
                mSelectedDr = 0;
            } else if (mSelectedRegion == 2) {
                // CN470
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
                mSelectedDr = 0;
            } else if (mSelectedRegion == 0 || mSelectedRegion == 9) {
                // AS923、RU864
                mSelectedCh1 = 0;
                mSelectedCh2 = 1;
                mSelectedDr = 0;
            } else {
                // CN779、EU443、EU868、KR920、IN865
                mSelectedCh1 = 0;
                mSelectedCh2 = 2;
                mSelectedDr = 0;
            }
            if (mSelectedRegion == 0 || mSelectedRegion == 1) {
                mSelectedDr1 = 2;
                mSelectedDr2 = 2;
            } else {
                mSelectedDr1 = 0;
                mSelectedDr2 = 0;
            }
        } else {
            mSelectedDr = 0;
            if (mSelectedServerRegion == 2 || mSelectedServerRegion == 4) {
                // US915 FSB1、AU915 FSB1
                mSelectedCh1 = 0;
                mSelectedCh2 = 7;
            } else if (mSelectedServerRegion == 3 || mSelectedServerRegion == 5) {
                // US915 FSB2、AU915 FSB2
                mSelectedCh1 = 8;
                mSelectedCh2 = 15;
            }
            if (mSelectedServerRegion == 0 || mSelectedServerRegion > 1) {
                // AS923,US915,AU915
                mBind.rlDr.setVisibility(View.GONE);
            } else {
                mBind.rlDr.setVisibility(View.VISIBLE);
            }
            if (mSelectedServerRegion == 0 || mSelectedServerRegion == 4 || mSelectedServerRegion == 5) {
                mSelectedDr1 = 2;
                mSelectedDr2 = 2;
            } else {
                mSelectedDr1 = 0;
                mSelectedDr2 = 0;
            }
        }
        mBind.tvCh1.setText(String.valueOf(mSelectedCh1));
        mBind.tvCh2.setText(String.valueOf(mSelectedCh2));
        mBind.tvDr.setText(String.valueOf(mSelectedDr));
        mBind.tvDr1.setText(String.valueOf(mSelectedDr1));
        mBind.tvDr2.setText(String.valueOf(mSelectedDr2));
    }


    private void initDutyCycle() {
        if (mSelectedPlatform == 0) {
            if (mSelectedRegion == 3 || mSelectedRegion == 4
                    || mSelectedRegion == 5 || mSelectedRegion == 9) {
                mBind.cbDutyCycle.setChecked(false);
                // CN779,EU433,EU868 and RU864
                mBind.llDutyCycle.setVisibility(View.VISIBLE);
            } else {
                mBind.llDutyCycle.setVisibility(View.GONE);
            }
        } else {
            if (mSelectedServerRegion == 1) {
                // EU868
                mBind.cbDutyCycle.setChecked(false);
                mBind.llDutyCycle.setVisibility(View.VISIBLE);
            } else {
                mBind.llDutyCycle.setVisibility(View.GONE);
            }
        }
    }


    public void selectCh1(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        bottomDialog.setDatas(mCHList, mSelectedCh1);
        bottomDialog.setListener(value -> {
            mSelectedCh1 = value;
            mBind.tvCh1.setText(mCHList.get(value));
            if (mSelectedCh1 > mSelectedCh2) {
                mSelectedCh2 = mSelectedCh1;
                mBind.tvCh2.setText(mCHList.get(value));
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
            mBind.tvCh2.setText(ch2List.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    public void selectDr1(View view) {
        if (isWindowLocked())
            return;
        BottomDialog bottomDialog = new BottomDialog();
        if ((mSelectedPlatform == 0 && mSelectedRegion <= 1)
                || (mSelectedPlatform == 1 && (mSelectedServerRegion == 0 || mSelectedServerRegion >= 4))) {
            bottomDialog.setDatas(mDRList, mSelectedDr1 - 2);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value + 2;
                mBind.tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    mBind.tvDr2.setText(mDRList.get(value));
                }
            });
        } else {
            bottomDialog.setDatas(mDRList, mSelectedDr1);
            bottomDialog.setListener(value -> {
                mSelectedDr1 = value;
                mBind.tvDr1.setText(mDRList.get(value));
                if (mSelectedDr1 > mSelectedDr2) {
                    mSelectedDr2 = mSelectedDr1;
                    mBind.tvDr2.setText(mDRList.get(value));
                }
            });
        }
        bottomDialog.show(getSupportFragmentManager());
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
            mBind.tvDr2.setText(dr2List.get(value));
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
            mBind.tvDr.setText(mDRList.get(value));
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
            mBind.tvMaxRetransmissionTimes.setText(mMaxRetransmissionTimesList.get(value));
        });
        bottomDialog.show(getSupportFragmentManager());
    }

    private ArrayList<OrderTask> mOrderTasks;

    public void onSave(View view) {
        if (isWindowLocked()) return;
        mOrderTasks = getOrderTasks();
        if (mOrderTasks == null) {
            ToastUtils.showToast(this, "Para error!");
            return;
        }
        if (mSelectedPlatform == 1) {
            loginServer();
            return;
        }
        showSyncingProgressDialog();
        LoRaLW008MokoSupport.getInstance().sendOrder(mOrderTasks.toArray(new OrderTask[]{}));
    }

    public void onLogout(View view) {
        if (isWindowLocked()) return;
        LogoutDialog dialog = new LogoutDialog();
        dialog.setOnLogoutClicked(() -> {
            mPassword = "";
            IoTDMSPUtils.setStringValue(this, IoTDMConstants.SP_LOGIN_PASSWORD, "");
            mBind.llAccount.setVisibility(View.GONE);
        });
        dialog.show(getSupportFragmentManager());
    }

    private void loginServer() {
        mGatewayId = mBind.etGatewayId.getText().toString();
        if (!TextUtils.isEmpty(mGatewayId)) {
            if (mGatewayId.length() != 16) {
                ToastUtils.showToast(this, "length must be 8 bytes!");
                return;
            }
        }
        // 登录
        mAccount = IoTDMSPUtils.getStringValue(this, IoTDMConstants.SP_LOGIN_ACCOUNT, "");
        mPassword = IoTDMSPUtils.getStringValue(this, IoTDMConstants.SP_LOGIN_PASSWORD, "");
        int env = IoTDMSPUtils.getIntValue(this, IoTDMConstants.SP_LOGIN_ENV, 0);
        if (TextUtils.isEmpty(mAccount) || TextUtils.isEmpty(mPassword)) {
            LoginDialog dialog = new LoginDialog();
            dialog.setOnLoginClicked(this::login);
            dialog.show(getSupportFragmentManager());
            return;
        }
        login(mAccount, mPassword, env);
    }


    @Nullable
    private ArrayList<OrderTask> getOrderTasks() {
        String adrAckLimitStr = mBind.etAdrAckLimit.getText().toString();
        String adrAckDelayStr = mBind.etAdrAckDelay.getText().toString();
        if (TextUtils.isEmpty(adrAckLimitStr)) {
            return null;
        }
        int adrAckLimit = Integer.parseInt(adrAckLimitStr);
        if (adrAckLimit < 1 || adrAckLimit > 255) {
            return null;
        }
        if (TextUtils.isEmpty(adrAckDelayStr)) {
            return null;
        }
        int adrAckDelay = Integer.parseInt(adrAckDelayStr);
        if (adrAckDelay < 1 || adrAckDelay > 255) {
            return null;
        }
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        savedParamsError = false;
        if (mSelectedPlatform == 0) {
            if (mSelectedMode == 0) {
                String devEui = mBind.etDevEui.getText().toString();
                String appEui = mBind.etAppEui.getText().toString();
                String devAddr = mBind.etDevAddr.getText().toString();
                String appSkey = mBind.etAppSkey.getText().toString();
                String nwkSkey = mBind.etNwkSkey.getText().toString();
                if (devEui.length() != 16) {
                    return null;
                }
                if (appEui.length() != 16) {
                    return null;
                }
                if (devAddr.length() != 8) {
                    return null;
                }
                if (appSkey.length() != 32) {
                    return null;
                }
                if (nwkSkey.length() != 32) {
                    return null;
                }
                orderTasks.add(OrderTaskAssembler.setLoraDevEUI(devEui));
                orderTasks.add(OrderTaskAssembler.setLoraAppEUI(appEui));
                orderTasks.add(OrderTaskAssembler.setLoraDevAddr(devAddr));
                orderTasks.add(OrderTaskAssembler.setLoraAppSKey(appSkey));
                orderTasks.add(OrderTaskAssembler.setLoraNwkSKey(nwkSkey));
            } else {
                String devEui = mBind.etDevEui.getText().toString();
                String appEui = mBind.etAppEui.getText().toString();
                String appKey = mBind.etAppKey.getText().toString();
                if (devEui.length() != 16) {
                    return null;
                }
                if (appEui.length() != 16) {
                    return null;
                }
                if (appKey.length() != 32) {
                    return null;
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
            // 保存并连接
            orderTasks.add(OrderTaskAssembler.setLoraRegion(mSelectedRegion));
            if (mSelectedRegion == 1 || mSelectedRegion == 2 || mSelectedRegion == 8) {
                // US915,AU915,CN470
                orderTasks.add(OrderTaskAssembler.setLoraCH(mSelectedCh1, mSelectedCh2));
            }
            if (mSelectedRegion == 3 || mSelectedRegion == 4
                    || mSelectedRegion == 5 || mSelectedRegion == 9) {
                // CN779,EU433,EU868 and RU864
                orderTasks.add(OrderTaskAssembler.setLoraDutyCycleEnable(mBind.cbDutyCycle.isChecked() ? 1 : 0));
            }
            if (mSelectedRegion != 0 && mSelectedRegion != 1 && mSelectedRegion != 8) {
                // AS923,US915,AU915
                orderTasks.add(OrderTaskAssembler.setLoraDR(mSelectedDr));
            }
        } else {
            orderTasks.add(OrderTaskAssembler.setLoraDevEUI(mRemoteDevEUI));
            orderTasks.add(OrderTaskAssembler.setLoraAppEUI(mRemoteAPPEUI));
            orderTasks.add(OrderTaskAssembler.setLoraAppKey(mRemoteAPPKEY));
            orderTasks.add(OrderTaskAssembler.setLoraUploadMode(2));
            orderTasks.add(OrderTaskAssembler.setLoraMessageType(mSelectedMessageType));
            if (mSelectedMessageType == 1) {
                orderTasks.add(OrderTaskAssembler.setLoraMaxRetransmissionTimes(mSelectedMaxRetransmissionTimes + 1));
            }
            if (mSelectedServerRegion == 0)
                mSelectedRegion = 0;
            else if (mSelectedServerRegion == 1)
                mSelectedRegion = 5;
            else if (mSelectedServerRegion == 2 || mSelectedServerRegion == 3)
                mSelectedRegion = 8;
            else if (mSelectedServerRegion == 4 || mSelectedServerRegion == 5)
                mSelectedRegion = 1;
            // 保存并连接
            orderTasks.add(OrderTaskAssembler.setLoraRegion(mSelectedRegion));
            if (mSelectedServerRegion > 1) {
                // US915,AU915
                orderTasks.add(OrderTaskAssembler.setLoraCH(mSelectedCh1, mSelectedCh2));
            }
            if (mSelectedServerRegion == 1) {
                // EU868
                orderTasks.add(OrderTaskAssembler.setLoraDutyCycleEnable(mBind.cbDutyCycle.isChecked() ? 1 : 0));
                orderTasks.add(OrderTaskAssembler.setLoraDR(mSelectedDr));
            }
        }
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckLimit(adrAckLimit));
        orderTasks.add(OrderTaskAssembler.setLoraAdrAckDelay(adrAckDelay));
        // 数据发送次数默认为1
        orderTasks.add(OrderTaskAssembler.setLoraUplinkStrategy(mBind.cbAdr.isChecked() ? 1 : 0, 1, mSelectedDr1, mSelectedDr2));
        return orderTasks;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_advance_setting) {
            mBind.llAdvancedSetting.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        } else if (buttonView.getId() == R.id.cb_adr) {
            mBind.llAdrOptions.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        }
    }

    private static final String DEVICE_PROFILE_TYPE = "008";
    private static final String APPLICATION_NAME = "LW008";
    // LW001-BG PRO(L) 10
    // LW001-BG PRO(M) 20
    // LW004-PB 30
    // LW005-MP 40
    // LW006 45
    // LW007-PIR 50
    // LW008-MT 60
    private static final String PRODUCT_MODEL = "60";
    private String mDeviceProfileSearch;
    private String mAccount;
    private String mPassword;

    private String mGatewayId;

    private void login(String account, String password, int envValue) {
        LoginEntity entity = new LoginEntity();
        entity.username = account;
        entity.password = password;
        entity.source = 1;
        if (envValue == 0)
            Urls.setCloudEnv(getApplicationContext());
        else
            Urls.setTestEnv(getApplicationContext());
        RequestBody body = RequestBody.create(Urls.JSON, new Gson().toJson(entity));
        OkGo.<String>post(Urls.loginApi(getApplicationContext()))
                .upRequestBody(body)
                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        showLoadingProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        Type type = new TypeToken<CommonResp<JsonObject>>() {
                        }.getType();
                        CommonResp<JsonObject> commonResp = new Gson().fromJson(response.body(), type);
                        if (commonResp.code != 200) {
                            ToastUtils.showToast(LoRaConnSettingActivity.this, commonResp.msg);
                            LoginDialog dialog = new LoginDialog();
                            dialog.setOnLoginClicked((account1, password1, env) -> login(account1, password1, env));
                            dialog.show(getSupportFragmentManager());
                            return;
                        }
                        mAccount = account;
                        IoTDMSPUtils.setStringValue(LoRaConnSettingActivity.this, IoTDMConstants.SP_LOGIN_ACCOUNT, account);
                        IoTDMSPUtils.setStringValue(LoRaConnSettingActivity.this, IoTDMConstants.SP_LOGIN_PASSWORD, password);
                        IoTDMSPUtils.setIntValue(LoRaConnSettingActivity.this, IoTDMConstants.SP_LOGIN_ENV, envValue);
                        // add header
                        String accessToken = commonResp.data.get("access_token").getAsString();
                        HttpHeaders headers = new HttpHeaders();
                        headers.put("Authorization", accessToken);
                        OkGo.getInstance().addCommonHeaders(headers);

                        if (mSelectedServerRegion == 0) {
                            mDeviceProfileSearch = String.format("AS923_%s", DEVICE_PROFILE_TYPE);
                        } else if (mSelectedServerRegion == 1) {
                            mDeviceProfileSearch = String.format("EU868_%s", DEVICE_PROFILE_TYPE);
                        } else if (mSelectedServerRegion == 2) {
                            mDeviceProfileSearch = String.format("US915_0_%s", DEVICE_PROFILE_TYPE);
                        } else if (mSelectedServerRegion == 3) {
                            mDeviceProfileSearch = String.format("US915_1_%s", DEVICE_PROFILE_TYPE);
                        } else if (mSelectedServerRegion == 4) {
                            mDeviceProfileSearch = String.format("AU915_0_%s", DEVICE_PROFILE_TYPE);
                        } else if (mSelectedServerRegion == 5) {
                            mDeviceProfileSearch = String.format("AU915_1_%s", DEVICE_PROFILE_TYPE);
                        }
                        syncDevices();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        ToastUtils.showToast(LoRaConnSettingActivity.this, R.string.request_error);
                        LoginDialog dialog = new LoginDialog();
                        dialog.setOnLoginClicked((account12, password12, env) -> login(account12, password12, env));
                        dialog.show(getSupportFragmentManager());
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingProgressDialog();
                    }
                });
    }

    private void syncDevices() {
        String devName = String.format("%s_%s", APPLICATION_NAME, mRemoteDevEUI.substring(12).toUpperCase());
        String gwName = "";
        if (!TextUtils.isEmpty(mGatewayId)) {
            gwName = String.format("%s_%s", mAccount, mGatewayId.substring(12).toUpperCase());
        }
        OkGo.<String>post(Urls.syncGatewayApi(getApplicationContext()))
                .params("devEui", mRemoteDevEUI)
                .params("model", PRODUCT_MODEL)
                .params("applicationIdFull", APPLICATION_NAME)
                .params("devName", devName)
                .params("devDesc", mAccount)
                .params("gwId", mGatewayId)
                .params("gwName", gwName)
                .params("gwSearch", gwName)
                .params("gwDesc", mAccount)
                .params("joinEui", mRemoteAPPEUI)
                .params("nwkKey", mRemoteAPPKEY)
                .params("devProfilesSearch", mDeviceProfileSearch)

                .execute(new StringCallback() {

                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        showLoadingProgressDialog();
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        JsonObject object = new Gson().fromJson(response.body(), JsonObject.class);
                        int code = object.get("code").getAsInt();
                        String msg = object.get("msg").getAsString();
                        if (code != 200) {
                            ToastUtils.showToast(LoRaConnSettingActivity.this, msg);
                            return;
                        }
//                        ToastUtils.showToast(LoRaConnSettingActivity.this, "Sync Success");
                        showSyncingProgressDialog();
                        LoRaLW008MokoSupport.getInstance().sendOrder(mOrderTasks.toArray(new OrderTask[]{}));
                    }

                    @Override
                    public void onError(Response<String> response) {
                        ToastUtils.showToast(LoRaConnSettingActivity.this, R.string.request_error);
                    }

                    @Override
                    public void onFinish() {
                        dismissLoadingProgressDialog();
                    }
                });
    }

}
