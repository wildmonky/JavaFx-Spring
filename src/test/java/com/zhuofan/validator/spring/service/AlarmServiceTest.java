package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.MainApplication;
import com.zhuofan.validator.spring.model.DeviceSCADAInputInfo;
import com.zhuofan.validator.spring.model.ProgressModel;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MainApplication.class)
class AlarmServiceTest {

    @Resource
    private AlarmService alarmService;

    @Test
    void check() throws FileNotFoundException {
        Path checkFilePath = Paths.get("C:\\Users\\15259\\Desktop\\项目\\2025-08-01 变电站遥测信息规范检测\\解析结果（110kV清石站）-20250929.xlsx");
        FileInputStream fileInputStream = new FileInputStream(checkFilePath.toFile());
        AtomicReference<ProgressModel> progressModelAtomicReference = new AtomicReference<>();
        Map<AlarmService.SheetInfo, List<DeviceSCADAInputInfo>> check = alarmService.check(fileInputStream, progressModelAtomicReference);
    }
}