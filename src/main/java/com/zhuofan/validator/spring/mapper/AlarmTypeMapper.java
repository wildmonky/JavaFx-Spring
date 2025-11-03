package com.zhuofan.validator.spring.mapper;

import com.zhuofan.validator.spring.entity.AlarmType;
import com.zhuofan.validator.spring.model.AlarmTypeRuleModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AlarmTypeMapper extends CommonMapper<AlarmType> {

    List<AlarmTypeRuleModel> selectListAndInfoRule(Set<String> ids);

    List<AlarmTypeRuleModel> selectListAndInfoRuleWhenDeviceEmpty();

    List<AlarmType> selectByDevice(String id);

    int deleteIds(Collection<String> ids);

}
