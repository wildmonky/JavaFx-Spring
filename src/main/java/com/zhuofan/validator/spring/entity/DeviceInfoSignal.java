package com.zhuofan.validator.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("DEVICE_INFO_SIGNAL")
public class DeviceInfoSignal {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * {@link DeviceInfo#getId()}
     */
    private String deviceInfoId;

    private String deviceNameInSignal;

    private String signalType;

    private String signalTemplate;

    private String signal;

    /**
     * 间隔
     */
    private String bayName;

    /**
     * 信号常态
     */
    private String normalStatus;

    /**
     * 遥信类型：单点遥信、双位置遥信
     */
    private String remoteSignalingType;

}
