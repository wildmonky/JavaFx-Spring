package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.SignalTemplateDetail;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SignalTemplateDetailModel extends SignalTemplateDetail {

    public SignalTemplateDetailModel() {
        super();
    }

    public SignalTemplateDetailModel(SignalTemplateDetail detail) {
        if (detail == null) {
            return;
        }
        BeanUtils.copyProperties(detail, this);

    }


}
