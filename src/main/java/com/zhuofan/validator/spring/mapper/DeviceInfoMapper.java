package com.zhuofan.validator.spring.mapper;

import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.model.DeviceInfoModel;

import java.util.List;

public interface DeviceInfoMapper extends CommonMapper<DeviceInfo> {

   List<DeviceInfoModel> selectDeviceInfos(DeviceInfoModel deviceInfoModel);

}
