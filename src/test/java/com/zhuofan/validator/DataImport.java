package com.zhuofan.validator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhuofan.validator.spring.MainApplication;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.mapper.DeviceInfoMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

@SpringBootTest(classes = MainApplication.class)
public class DataImport {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Test
    public void importDeviceInfo() throws IOException {
        ClassPathResource resource = new ClassPathResource("importdata/device_info.json");
        Gson gson = new Gson();
        // 1. 使用 TypeToken 捕获泛型类型
        Type listType = new TypeToken<List<DeviceInfo>>(){}.getType();

        List<DeviceInfo> list = gson.fromJson(new InputStreamReader(resource.getInputStream()), listType);
        saveDeviceInfos(null, list);
    }

    public void saveDeviceInfos(DeviceInfo root, List<DeviceInfo> children) {
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        if (root != null) {
            for (DeviceInfo child : children) {
                child.setParentId(root.getId());
            }
        }

        deviceInfoMapper.insert(children);

        for (DeviceInfo child : children) {
            List<DeviceInfo> childNodes = child.getChildren();
            if (CollectionUtils.isEmpty(childNodes)) {
                continue;
            }

            saveDeviceInfos(child, childNodes);
        }
    }


}
