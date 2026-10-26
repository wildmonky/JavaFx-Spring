package com.zhuofan.validator.javafx.service.device;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.mapper.DeviceInfoMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceInfoService {

    @Resource
    private DeviceInfoMapper mapper;

    public List<DeviceInfo> searchDeviceInfo(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return mapper.selectList(Wrappers.emptyWrapper());
        }

        return mapper.selectList(Wrappers.<DeviceInfo>lambdaQuery()
                .eq(deviceInfo.getId() != null, DeviceInfo::getId, deviceInfo.getId())
                .like(StringUtils.isNotBlank(deviceInfo.getName()), DeviceInfo::getName, deviceInfo.getName())
        );
    }

}
