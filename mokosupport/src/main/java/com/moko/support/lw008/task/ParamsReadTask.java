package com.moko.support.lw008.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.lw008.entity.OrderCHAR;
import com.moko.support.lw008.entity.ParamsKeyEnum;

import java.util.Arrays;

public class ParamsReadTask extends OrderTask {
    public byte[] data;

    public ParamsReadTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        switch (key) {
//            case KEY_CLOSE:
//            case KEY_RESTART:
//            case KEY_RESET:
            case KEY_VOLTAGE:
            case KEY_BUTTON_CLOSE_ENABLE:
            case KEY_LOW_POWER_PERCENT:
            case KEY_LOW_POWER_REPORT_ENABLE:
            case KEY_ADV_NAME:
            case KEY_ADV_INTERVAL:
            case KEY_ADV_TX_POWER:
            case KEY_ADV_TIMEOUT:
            case KEY_PASSWORD_VERIFY_ENABLE:
//            case KEY_PASSWORD:
            case KEY_TIME_ZONE:
            case KEY_TIME_UTC:
            case KEY_ACC_WAKEUP_THRESHOLD:
            case KEY_ACC_WAKEUP_DURATION:
            case KEY_ACC_MOTION_THRESHOLD:
            case KEY_ACC_MOTION_DURATION:
            case KEY_ACC_MOTION_END_TIMEOUT:

            case KEY_START_ADV:
            case KEY_MAC:
            case KEY_PCBA_STATUS:
            case KEY_SELFTEST_STATUS:
            case KEY_SHUTDOWN_PAYLOAD_ENABLE:
            case KEY_DEVICE_MODE:
            case KEY_HEARTBEAT_INTERVAL:
            case KEY_PERIODIC_MODE_REPORT_INTERVAL:
            case KEY_PERIODIC_MODE_POS_STRATEGY:
            case KEY_TIME_MODE_REPORT_TIME_POINT:
            case KEY_TIME_MODE_POS_STRATEGY:
            case KEY_MOTION_MODE_EVENT:
            case KEY_MOTION_MODE_START_POS_STRATEGY:
            case KEY_MOTION_MODE_START_NUMBER:
            case KEY_MOTION_MODE_TRIP_POS_STRATEGY:
            case KEY_MOTION_MODE_TRIP_REPORT_INTERVAL:
            case KEY_MOTION_MODE_END_POS_STRATEGY:
            case KEY_MOTION_MODE_END_NUMBER:
            case KEY_MOTION_MODE_END_REPORT_INTERVAL:


            case KEY_DOWN_LINK_POS_STRATEGY:
            case KEY_MAN_DOWN_DETECTION_ENABLE:
            case KEY_MAN_DOWN_POS_STRATEGY:
            case KEY_MAN_DOWN_DETECTION_TIMEOUT:
            case KEY_MAN_DOWN_REPORT_INTERVAL:
            case KEY_MAN_DOWN_START_EVENT_NOTIFY_ENABLE:
            case KEY_MAN_DOWN_END_EVENT_NOTIFY_ENABLE:
            case KEY_VIBRATION_INTENSITY:
            case KEY_ALARM_TYPE:
            case KEY_ALARM_EXIT_PRESS_DURATION:
            case KEY_ALARM_SOS_START_EVENT_NOTIFY_ENABLE:
            case KEY_ALARM_SOS_END_EVENT_NOTIFY_ENABLE:
            case KEY_ALARM_SOS_POS_STRATEGY:
            case KEY_ALARM_SOS_REPORT_INTERVAL:
            case KEY_ALARM_SOS_TRIGGER_MODE:
            case KEY_ALARM_ALERT_START_EVENT_NOTIFY_ENABLE:
            case KEY_ALARM_ALERT_END_EVENT_NOTIFY_ENABLE:
            case KEY_ALARM_ALERT_POS_STRATEGY:
            case KEY_ALARM_ALERT_TRIGGER_MODE:

            case KEY_GPS_POS_TIMEOUT:
            case KEY_GPS_PDOP_LIMIT:
            case KEY_BLE_POS_TIMEOUT:
            case KEY_BLE_POS_MAC_NUMBER:
            case KEY_FILTER_RSSI:
            case KEY_FILTER_RELATIONSHIP:
            case KEY_FILTER_MAC_PRECISE:
            case KEY_FILTER_MAC_REVERSE:
            case KEY_FILTER_MAC_RULES:
            case KEY_FILTER_NAME_PRECISE:
            case KEY_FILTER_NAME_REVERSE:
//            case KEY_FILTER_NAME_RULES:
            case KEY_FILTER_RAW_DATA:
            case KEY_FILTER_IBEACON_ENABLE:
            case KEY_FILTER_IBEACON_MAJOR_RANGE:
            case KEY_FILTER_IBEACON_MINOR_RANGE:
            case KEY_FILTER_IBEACON_UUID:
            case KEY_FILTER_MKIBEACON_ENABLE:
            case KEY_FILTER_MKIBEACON_MAJOR_RANGE:
            case KEY_FILTER_MKIBEACON_MINOR_RANGE:
            case KEY_FILTER_MKIBEACON_UUID:
            case KEY_FILTER_MKIBEACON_ACC_ENABLE:
            case KEY_FILTER_MKIBEACON_ACC_MAJOR_RANGE:
            case KEY_FILTER_MKIBEACON_ACC_MINOR_RANGE:
            case KEY_FILTER_MKIBEACON_ACC_UUID:
            case KEY_FILTER_EDDYSTONE_UID_ENABLE:
            case KEY_FILTER_EDDYSTONE_UID_NAMESPACE:
            case KEY_FILTER_EDDYSTONE_UID_INSTANCE:
            case KEY_FILTER_EDDYSTONE_URL_ENABLE:
            case KEY_FILTER_EDDYSTONE_URL:
            case KEY_FILTER_EDDYSTONE_TLM_ENABLE:
            case KEY_FILTER_EDDYSTONE_TLM_VERSION:
            case KEY_FILTER_BXP_ACC:
            case KEY_FILTER_BXP_TH:
            case KEY_FILTER_OTHER_ENABLE:
            case KEY_FILTER_OTHER_RELATIONSHIP:
            case KEY_FILTER_OTHER_RULES:

            case KEY_LORA_REGION:
            case KEY_LORA_MODE:
            case KEY_NETWORK_STATUS:
            case KEY_LORA_DEV_EUI:
            case KEY_LORA_APP_EUI:
            case KEY_LORA_APP_KEY:
            case KEY_LORA_DEV_ADDR:
            case KEY_LORA_APP_SKEY:
            case KEY_LORA_NWK_SKEY:
            case KEY_LORA_MESSAGE_TYPE:
            case KEY_LORA_CH:
            case KEY_LORA_DR:
            case KEY_LORA_UPLINK_STRATEGY:
            case KEY_LORA_DUTYCYCLE:
            case KEY_LORA_TIME_SYNC_INTERVAL:
            case KEY_LORA_NETWORK_CHECK_INTERVAL:
            case KEY_LORA_MAX_RETRANSMISSION_TIMES:
            case KEY_LORA_ADR_ACK_LIMIT:
            case KEY_LORA_ADR_ACK_DELAY:
                createGetConfigData(key.getParamsKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{
                (byte) 0xED,
                (byte) 0x00,
                (byte) configKey,
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void getFilterName() {
        data = new byte[]{
                (byte) 0xEE,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_FILTER_NAME_RULES.getParamsKey(),
                (byte) 0x00
        };
        response.responseValue = data;
    }

    private int packetCount;
    private int packetIndex;
    private int remainPack;
    private int dataLength;
    private int dataOrigin;
    private byte[] dataBytes;

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        if (header == 0xED)
            return true;
        final int cmd = value[2] & 0xFF;
        packetCount = value[3] & 0xFF;
        packetIndex = value[4] & 0xFF;
        final int length = value[5] & 0xFF;
        ParamsKeyEnum keyEnum = ParamsKeyEnum.fromParamKey(cmd);
        switch (keyEnum) {
            case KEY_FILTER_NAME_RULES:
                dataLength += length;
                byte[] responseData = Arrays.copyOfRange(value, 6, 6 + length);
                break;
        }
        return false;
    }
}
