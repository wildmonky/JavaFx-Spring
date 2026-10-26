package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.DeviceInfoSignal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SignalModel extends DeviceInfoSignal {

    private String deviceName;

    public SignalModel() {}

    public SignalModel(DeviceInfoSignal signal) {
        super();
        if (signal == null) {
            return;
        }
        BeanUtils.copyProperties(signal, this);
    }

}
