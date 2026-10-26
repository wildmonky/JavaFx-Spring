package com.zhuofan.validator.spring.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("SUBSTATION")
public class Substation {

    @TableId
    private String name;

    private String voltage;

}
