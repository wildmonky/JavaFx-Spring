package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.entity.AlarmType;
import com.zhuofan.validator.spring.mapper.AlarmTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class AlarmTypeService {

    @Resource
    private AlarmTypeMapper alarmTypeMapper;

    public  boolean saveBatch(Collection<AlarmType> list, int batchSize) {
        int size = list.size();
        int idxLimit = Math.min(batchSize, size);
        int i = 1;
        List<AlarmType> batchList = new ArrayList<>();
        for (AlarmType element : list) {
            batchList.add(element);
            if (i == idxLimit) {
                alarmTypeMapper.insertBatchSomeColumn(batchList);
                batchList.clear();
                idxLimit = Math.min(idxLimit + batchSize, size);
            }
            i++;
        }
        return true;
    }


}
