package com.moko.support.lw008.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {

    //// 系统相关参数
    KEY_CLOSE(0x10),
    KEY_REBOOT(0x11),
    KEY_RESET(0x12),
    // 时间同步
    KEY_TIME_UTC(0x13),
    // 时区
    KEY_TIME_ZONE(0x14),
    // 工作模式选择
    KEY_DEVICE_MODE(0x15),
    // 指示灯开关
    KEY_INDICATOR_STATUS(0x16),
    // 设备心跳间隔
    KEY_HEARTBEAT_INTERVAL(0x17),
    // 厂家信息
    KEY_MANUFACTURER(0x18),
    // 关机信息上报
    KEY_SHUTDOWN_PAYLOAD_ENABLE(0x19),
    // 离线定位功能开关
    KEY_OFFLINE_LOCATION_ENABLE(0x1A),
    // 低电触发心跳开关
    KEY_LOW_POWER_PAYLOAD_ENABLE(0x1B),
    // 低电百分比
//    KEY_LOW_POWER_PERCENT(0x1C),
    // 芯片温度
    KEY_CHIP_TEMP(0x1D),
    // 读取当前系统时区时间
    KEY_SYSTEM_TIME(0x1E),
    // 读取当前需求版本
    KEY_DEMAND_VERSION(0x1F),
    // 电池电量
    KEY_BATTERY_POWER(0x20),
    // 芯片MAC
    KEY_CHIP_MAC(0x21),
    // 产测状态
    KEY_PCBA_STATUS(0x22),
    // 自检状态
    KEY_SELFTEST_STATUS(0x23),
    // 电池信息信息
    KEY_BATTERY_INFO(0x25),
    // 电池信息清除
    KEY_BATTERY_RESET(0x26),


    //// 蓝牙相关参数
    // 登录是否需要密码
    KEY_PASSWORD_VERIFY_ENABLE(0x30),
    KEY_PASSWORD(0x31),
    KEY_ADV_TIMEOUT(0x32),
    KEY_ADV_TX_POWER(0x33),
    KEY_ADV_NAME(0x34),

    //// 模式相关参数
    // 定期模式定位策略
    KEY_PERIODIC_MODE_POS_STRATEGY(0x40),
    // 定期模式上报间隔
    KEY_PERIODIC_MODE_REPORT_INTERVAL(0x41),
    // 定时模式定位策略
    KEY_TIME_MODE_POS_STRATEGY(0x42),
    // 定时模式时间点
    KEY_TIME_MODE_REPORT_TIME_POINT(0x43),
    // 运动模式事件
    KEY_MOTION_MODE_EVENT(0x44),
    // 运动开始定位上报次数
    KEY_MOTION_MODE_START_NUMBER(0x45),
    // 运动开始定位策略
    KEY_MOTION_MODE_START_POS_STRATEGY(0x46),
    // 运动中定位间隔
    KEY_MOTION_MODE_TRIP_REPORT_INTERVAL(0x47),
    // 运动中定位策略
    KEY_MOTION_MODE_TRIP_POS_STRATEGY(0x48),
    // 运动结束判断时间
    KEY_MOTION_MODE_END_TIMEOUT(0x49),
    // 运动结束定位次数
    KEY_MOTION_MODE_END_NUMBER(0x4A),
    // 运动结束定位间隔
    KEY_MOTION_MODE_END_REPORT_INTERVAL(0x4B),
    // 运动结束定位策略
    KEY_MOTION_MODE_END_POS_STRATEGY(0x4C),

    //// 定位参数
    // WIFI定位数据格式
    KEY_WIFI_POS_DATA_TYPE(0x50),
    // WIFI定位超时时间
    KEY_WIFI_POS_TIMEOUT(0x51),
    // WIFI定位成功BSSID数量
    KEY_WIFI_POS_BSSID_NUMBER(0x52),
    // 蓝牙定位超时时间
    KEY_BLE_POS_TIMEOUT(0x53),
    // 蓝牙定位成功MAC数量
    KEY_BLE_POS_MAC_NUMBER(0x54),
    // RSSI过滤规则
    KEY_FILTER_RSSI(0x55),
    // 蓝牙扫描PHY选择
    KEY_FILTER_BLE_SCAN_PHY(0x56),
    // 广播内容过滤逻辑
    KEY_FILTER_RELATIONSHIP(0x57),
    // 精准过滤MAC开关
    KEY_FILTER_MAC_PRECISE(0x58),
    // 反向过滤MAC开关
    KEY_FILTER_MAC_REVERSE(0x59),
    // MAC过滤规则
    KEY_FILTER_MAC_RULES(0x5A),
    // 精准过滤ADV Name开关
    KEY_FILTER_NAME_PRECISE(0x5B),
    // 反向过滤ADV Name开关
    KEY_FILTER_NAME_REVERSE(0x5C),
    // NAME过滤规则
    KEY_FILTER_NAME_RULES(0x5D),
    // 过滤设备类型开关
    KEY_FILTER_RAW_DATA(0x5E),
    // iBeacon类型过滤开关
    KEY_FILTER_IBEACON_ENABLE(0x5F),
    // iBeacon类型Major范围
    KEY_FILTER_IBEACON_MAJOR_RANGE(0x60),
    // iBeacon类型Minor范围
    KEY_FILTER_IBEACON_MINOR_RANGE(0x61),
    // iBeacon类型UUID
    KEY_FILTER_IBEACON_UUID(0x62),
    // BXP-iBeacon类型过滤开关
    KEY_FILTER_BXP_IBEACON_ENABLE(0x63),
    // BXP-iBeacon类型Major范围
    KEY_FILTER_BXP_IBEACON_MAJOR_RANGE(0x64),
    // BXP-iBeacon类型Minor范围
    KEY_FILTER_BXP_IBEACON_MINOR_RANGE(0x65),
    // BXP-iBeacon类型UUID
    KEY_FILTER_BXP_IBEACON_UUID(0x66),
    // BXP-Tag开关类型过滤开关
    KEY_FILTER_BXP_TAG_ENABLE(0x67),
    // 精准过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_PRECISE(0x68),
    // 反向过滤BXP-Tag开关
    KEY_FILTER_BXP_TAG_REVERSE(0x69),
    // BXP-Tag过滤规则
    KEY_FILTER_BXP_TAG_RULES(0x6A),
    // eddystone-UID类型过滤开关
    KEY_FILTER_EDDYSTONE_UID_ENABLE(0x6B),
    // eddystone-UID类型Namespace
    KEY_FILTER_EDDYSTONE_UID_NAMESPACE(0x6C),
    // eddystone-UID类型Instance
    KEY_FILTER_EDDYSTONE_UID_INSTANCE(0x6D),
    // eddystone-URL类型过滤开关
    KEY_FILTER_EDDYSTONE_URL_ENABLE(0x6E),
    // eddystone-URL类型URL
    KEY_FILTER_EDDYSTONE_URL(0x6F),
    // eddystone-TLM类型过滤开关
    KEY_FILTER_EDDYSTONE_TLM_ENABLE(0x70),
    // eddystone- TLM类型TLMVersion
    KEY_FILTER_EDDYSTONE_TLM_VERSION(0x71),
    // BeaconX Pro-ACC设备过滤开关
    KEY_FILTER_BXP_ACC(0x72),
    // BeaconX Pro-T&H设备过滤开关
    KEY_FILTER_BXP_TH(0x73),
    // Unknown设备过滤开关
    KEY_FILTER_OTHER_ENABLE(0x74),
    // 3组unknown过滤规则逻辑
    KEY_FILTER_OTHER_RELATIONSHIP(0x75),
    // unknown类型过滤规则
    KEY_FILTER_OTHER_RULES(0x76),
    // GPS定位超时时间（L76版本）
    KEY_GPS_POS_TIMEOUT_L76C(0x77),
    // GPS位置精度因子PDOP（L76版本）
    KEY_GPS_PDOP_LIMIT_L76C(0x78),
    // GPS极限上传模式（L76版本）
    KEY_GPS_EXTREME_MODE_L76C(0x79),
    // GPS定位超时时间（LR1110版本）
    KEY_GPS_POS_TIMEOUT(0x7A),
    // GPS搜星数量（LR1110版本）
    KEY_GPS_POS_SATELLITE_THRESHOLD(0x7B),
    // GPS定位数据格式（LR1110版本）
    KEY_GPS_POS_DATA_TYPE(0x7C),
    // GPS定位星座（LR1110版本）
    KEY_GPS_POS_SYSTEM(0x7D),
    // 定位方式选择（LR1110版本）
    KEY_GPS_POS_AUTONMOUS_AIDING_ENABLE(0x7E),
    // 辅助定位经纬度（LR1110版本）
    KEY_GPS_POS_AUXILIARY_LAT_LON(0x7F),
    // 星历开始更新事件开关
    KEY_GPS_POS_EPHEMERIS_START_NOTIFY_ENABLE(0x80),
    // 星历更新结束事件开关
    KEY_GPS_POS_EPHEMERIS_END_NOTIFY_ENABLE(0x81),
    // BXP-Device类型过滤开关
    KEY_FILTER_BXP_DEVICE(0x82),
    // BXP-Button类型过滤开关
    KEY_FILTER_BXP_BUTTON_ENABLE(0x83),
    // BXP-Button类型过滤规则
    KEY_FILTER_BXP_BUTTON_RULES(0x84),
    // 蓝牙定位机制选择
    KEY_BLE_POS_MECHANISM(0x85),

    //// LoRaWAN参数
    // LoRaWAN网络状态
    KEY_LORA_NETWORK_STATUS(0x90),
    // 频段
    KEY_LORA_REGION(0x91),
    // 入网类型
    KEY_LORA_MODE(0x92),
    KEY_LORA_DEV_EUI(0x93),
    KEY_LORA_APP_EUI(0x94),
    KEY_LORA_APP_KEY(0x95),
    KEY_LORA_DEV_ADDR(0x96),
    KEY_LORA_APP_SKEY(0x97),
    KEY_LORA_NWK_SKEY(0x98),
    // 上行数据类型
    KEY_LORA_MESSAGE_TYPE(0x99),
    // CH
    KEY_LORA_CH(0x9A),
    // 入网DR
    KEY_LORA_DR(0x9B),
    // 数据发送策略
    KEY_LORA_UPLINK_STRATEGY(0x9C),
    // DUTYCYCLE
    KEY_LORA_DUTYCYCLE(0x9D),
    // 同步间隔
    KEY_LORA_TIME_SYNC_INTERVAL(0x9E),
    // 网络检查间隔
    KEY_LORA_NETWORK_CHECK_INTERVAL(0x9F),
    KEY_LORA_ADR_ACK_LIMIT(0xA0),
    KEY_LORA_ADR_ACK_DELAY(0xA1),
    // 数据的最大传输次数
    KEY_LORA_MAX_RETRANSMISSION_TIMES(0xA2),

    //// 辅助功能参数
    // 下行请求定位策略
    KEY_DOWN_LINK_POS_STRATEGY(0xB0),
    // 三轴唤醒条件
    KEY_ACC_WAKEUP_CONDITION(0xB1),
    // 运动检测判断
    KEY_ACC_MOTION_CONDITION(0xB2),
    // 震动检测使能
    KEY_SHOCK_DETECTION_ENABLE(0xB3),
    // 震动检测阈值
    KEY_ACC_SHOCK_THRESHOLD(0xB4),
    // 震动上发间隔
    KEY_SHOCK_REPORT_INTERVAL(0xB5),
    // 震动次数判断间隔
    KEY_SHOCK_TIMEOUT(0xB6),
    // 闲置功能使能
    KEY_MAN_DOWN_DETECTION_ENABLE(0xB7),
    // 闲置超时时间
    KEY_MAN_DOWN_DETECTION_TIMEOUT(0xB8),
    // 闲置清除
    KEY_MAN_DOWN_IDLE_RESET(0xB9),
    // 活动记录使能
    KEY_ACTIVE_STATE_COUNT_ENABLE(0xBA),
    // 活动判定间隔
    KEY_ACTIVE_STATE_TIMEOUT(0xBB),

    //// 存储协议
    // 读取存储的数据
    KEY_READ_STORAGE_DATA(0xC0),
    KEY_CLEAR_STORAGE_DATA(0xC1),
    KEY_SYNC_ENABLE(0xC2),

    ;

    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
