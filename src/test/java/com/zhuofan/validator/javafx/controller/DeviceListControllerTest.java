package com.zhuofan.validator.javafx.controller;

import com.zhuofan.validator.spring.MainApplication;
import com.zhuofan.validator.spring.utils.SpringUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest(classes = { MainApplication.class })
class DeviceListControllerTest {

    @Resource
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    public void beforeEach() {
        SpringUtils.setApplicationContext(applicationContext);
    }

    @Test
    void search() {
        DeviceListController controller = new DeviceListController();
        controller.search(null);
    }

}