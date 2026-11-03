package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.SubstationNote;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SubstationNoteModel extends SubstationNote {

    public SubstationNoteModel() {
        super();
    }

    public SubstationNoteModel(SubstationNote note) {
        super();
        BeanUtils.copyProperties(note, this);
    }

}
