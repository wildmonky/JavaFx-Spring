package com.zhuofan.validator.spring.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.spring.entity.Substation;
import com.zhuofan.validator.spring.entity.SubstationNote;
import com.zhuofan.validator.spring.mapper.SubstationMapper;
import com.zhuofan.validator.spring.mapper.SubstationNoteMapper;
import com.zhuofan.validator.spring.model.SubstationModel;
import com.zhuofan.validator.spring.model.SubstationNoteModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SubstationNoteService {

    @Resource
    private SubstationMapper substationMapper;

    @Resource
    private SubstationNoteMapper substationNoteMapper;

    public boolean removeSubstation(Substation substation) {
        if (substation == null) {
            return true;
        }

        substationMapper.delete(Wrappers.<Substation>lambdaQuery()
                .eq(substation.getId() != null, Substation::getId, substation.getId())
                .eq(StringUtils.isNotBlank(substation.getName()),Substation::getName, substation.getName())
        );
        return true;
    }

    public boolean saveSubstation(Substation substation) {
        if (substation == null) {
            return false;
        }

        return substationMapper.insertOrUpdate(substation);
    }

    public List<SubstationModel> searchSubstations(Substation substation) {
        if (substation == null) {
            return toSubstationModel(substationMapper.selectList(Wrappers.emptyWrapper()));
        }

        return toSubstationModel(substationMapper.selectList(Wrappers.<Substation>lambdaQuery()
                .eq(StringUtils.isNotBlank(substation.getId()), Substation::getId, substation.getId())
                .like(StringUtils.isNotBlank(substation.getName()), Substation::getName, substation.getName())
                .like(StringUtils.isNotBlank(substation.getVoltage()), Substation::getVoltage, substation.getVoltage())
        ));
    }

    public List<SubstationModel> toSubstationModel(List<Substation> substations) {
        if (CollectionUtils.isEmpty(substations)) {
            return Collections.emptyList();
        }

        return substations.stream().map(SubstationModel::new).toList();
    }

    public boolean saveSubstationNote(SubstationNote note) {
        if (note == null) {
            return false;
        }

        return substationNoteMapper.insertOrUpdate(note);
    }

    public boolean removeSubstationNote(SubstationNote note) {
        if (note == null) {
            return true;
        }

        substationNoteMapper.delete(Wrappers.<SubstationNote>lambdaQuery()
                .eq(StringUtils.isNotBlank(note.getId()), SubstationNote::getId, note.getId())
        );
        return true;
    }

    public List<SubstationNoteModel> searchSubstationNotes(SubstationNoteModel note) {
        return substationNoteMapper.searchSubstationNotes(note);
    }

}
