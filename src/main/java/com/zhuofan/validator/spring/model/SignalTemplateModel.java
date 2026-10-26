package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.SignalTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignalTemplateModel extends SignalTemplate {

    public SignalTemplateModel() {
        super();
    }

    public SignalTemplateModel(SignalTemplate template) {
        super();
        super.setId(template.getId());
        super.setName(template.getName());
    }

}
