package com.zhuofan.validator.spring.mapper;

import com.zhuofan.validator.spring.entity.SubstationNote;
import com.zhuofan.validator.spring.model.SubstationNoteModel;

import java.util.List;

public interface SubstationNoteMapper extends CommonMapper<SubstationNote>{

    List<SubstationNoteModel> searchSubstationNotes(SubstationNoteModel note);

}
