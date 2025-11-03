package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.entity.AlarmRule;
import com.zhuofan.validator.spring.mapper.AlarmRuleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class AlarmRuleService {

    @Resource
    private AlarmRuleMapper alarmRuleMapper;

    public  boolean saveBatch(Collection<AlarmRule> list, int batchSize) {
        int size = list.size();
        int idxLimit = Math.min(batchSize, size);
        int i = 1;
        List<AlarmRule> batchList = new ArrayList<>();
        for (AlarmRule element : list) {
            batchList.add(element);
            if (i == idxLimit) {
                alarmRuleMapper.insertBatchSomeColumn(batchList);
                batchList.clear();
                idxLimit = Math.min(idxLimit + batchSize, size);
            }
            i++;
        }
        return true;
    }

}
