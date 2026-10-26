package com.zhuofan.validator.javafx.service.device;

import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.service.DeviceService;
import jakarta.annotation.Resource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class SaveService extends Service<Boolean> {

    @Resource
    private DeviceService deviceService;

    @Setter
    private DeviceInfo deviceInfo;

    @Override
    protected Task<Boolean> createTask() {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (deviceInfo == null) {
                    return false;
                }
                return deviceService.save(deviceInfo);
            }
        };
    }


}
