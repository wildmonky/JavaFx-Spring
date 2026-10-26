package com.zhuofan.validator.spring.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("SIGNAL_TEMPLATE")
public class SignalTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String name;

}
