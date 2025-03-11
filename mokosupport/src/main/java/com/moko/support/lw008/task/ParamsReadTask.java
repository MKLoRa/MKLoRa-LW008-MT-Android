package com.moko.support.lw008.task;

import android.text.TextUtils;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.lw008.LoRaLW008MokoSupport;
import com.moko.support.lw008.entity.OrderCHAR;
import com.moko.support.lw008.entity.ParamsKeyEnum;

import java.util.Arrays;
import java.util.Objects;

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
        createGetConfigData(key.getParamsKey());
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
    private int dataLength;
    private byte[] dataBytes;
    private StringBuilder dataSb;

    @Override
    public boolean parseValue(byte[] value) {
        final int header = value[0] & 0xFF;
        final int flag = value[1] & 0xFF;
        if (header == 0xEE && flag == 0x00) {
            // 分包读取时特殊处理
            final int cmd = value[2] & 0xFF;
            packetCount = value[3] & 0xFF;
            packetIndex = value[4] & 0xFF;
            final int length = value[5] & 0xFF;
            if (packetIndex == 0) {
                // 第一包
                dataLength = 0;
                dataSb = new StringBuilder();
            }
            ParamsKeyEnum keyEnum = ParamsKeyEnum.fromParamKey(cmd);
            if (Objects.requireNonNull(keyEnum) == ParamsKeyEnum.KEY_FILTER_NAME_RULES) {
                if (length > 0) {
                    dataLength += length;
                    byte[] responseData = Arrays.copyOfRange(value, 6, 6 + length);
                    dataSb.append(MokoUtils.bytesToHexString(responseData));
                }
                if (packetIndex == (packetCount - 1)) {
                    if (!TextUtils.isEmpty(dataSb.toString()))
                        dataBytes = MokoUtils.hex2bytes(dataSb.toString());
                    byte[] responseValue = new byte[4 + dataLength];
                    responseValue[0] = (byte) 0xED;
                    responseValue[1] = (byte) 0x00;
                    responseValue[2] = (byte) cmd;
                    responseValue[3] = (byte) dataLength;
                    for (int i = 0; i < dataLength; i++) {
                        responseValue[4 + i] = dataBytes[i];
                    }
                    dataSb = null;
                    dataBytes = null;
                    // 最后一包
                    orderStatus = 1;
                    response.responseValue = responseValue;
                    LoRaLW008MokoSupport.getInstance().pollTask();
                    LoRaLW008MokoSupport.getInstance().executeTask();
                    LoRaLW008MokoSupport.getInstance().orderResult(response);
                }
            }
            return false;
        }
        return true;
    }
}
