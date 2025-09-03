package com.zhuofan.validator.spring.mapper;

import com.zhuofan.validator.spring.entity.AlarmRule;

import java.util.List;

public interface AlarmRuleMapper extends CommonMapper<AlarmRule> {

    List<AlarmRule> selectByDevice(String deviceId);

}
