package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.DeviceInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceInfoModel extends DeviceInfo {

    private String signalTemplateName;

}