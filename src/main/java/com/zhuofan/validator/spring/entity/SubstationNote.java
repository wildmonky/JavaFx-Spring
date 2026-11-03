package com.zhuofan.validator.spring.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@TableName("SUBSTATION_NOTE")
public class SubstationNote {

    @TableId
    private String id;

    private String category;

    /**
     * 计划时间
     */
    private LocalDate planDate;

    /**
     * 待办内容
     */
    private String content;

    /**
     * 备注
     */
    private String comment;

    /**
     * 状态：待办、
     */
    private String status;

}
