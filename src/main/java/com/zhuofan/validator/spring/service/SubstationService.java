package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.mapper.SubstationMapper;
import com.zhuofan.validator.spring.model.SubstationModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SubstationService {

    @Resource
    private SubstationMapper substationMapper;

    public List<SubstationModel> findSubstations(SubstationModel substationModel) {
        return Collections.emptyList();
    }

}
