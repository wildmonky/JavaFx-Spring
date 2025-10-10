package com.zhuofan.validator.javafx.service;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildServiceTest {

    @Test
    void replaceNumberAfterFAnd7() {
        List<String> strList = Arrays.asList("10kV F1线701开关事故总",
                "10kV F1线701开关保护动作",
                "10kV F1线701开关重合闸动作",
                "10kV F1线701开关过负荷告警",
                "10kV F1线701开关弹簧未储能",
                "10kV F1线701开关控制回路断线",
                "10kV F1线701开关直流电源故障",
                "10kV F1线701开关交流电源故障"
        );
        List<String> list = BuildService.replaceNumberAfterFAnd7(strList, 1, 26);
        for (String str : list) {
            System.out.println(str);
        }
    }

}