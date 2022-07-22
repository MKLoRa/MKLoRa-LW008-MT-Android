package com.moko.support.lw008.service;

import com.moko.support.lw008.entity.DeviceInfo;

public interface DeviceInfoParseable<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
