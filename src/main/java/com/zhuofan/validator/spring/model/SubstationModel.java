package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.Substation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
public class SubstationModel extends Substation {

    private String displayName;

    /** 电压 */
    private String voltage;

    public SubstationModel(Substation substation) {
        super();
        BeanUtils.copyProperties(substation, this);
    }

    public String getDisplayName() {
        return getVoltage() + getName();
    }

}
