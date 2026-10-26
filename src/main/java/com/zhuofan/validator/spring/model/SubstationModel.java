package com.zhuofan.validator.spring.model;

import com.zhuofan.validator.spring.entity.Substation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubstationModel extends Substation {

    private String name;

    /** 电压 */
    private String voltage;

}
