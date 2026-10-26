package com.zhuofan.validator.spring.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@TableName("SIGNAL_TEMPLATE_DETAIL")
public class SignalTemplateDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String templateId;

    @ExcelProperty("规范信息描述")
    private String desc;

    @ExcelProperty("类型名称")
    private String type;

    @ExcelProperty("规范信息举例")
    private String sample;

    @ExcelProperty("规范信息内容")
    private String content;

    /**
     * 信号常态
     */
    @ExcelProperty("信号常态")
    private String normalStatus;

    /**
     * 遥信类型：单点遥信、双位置遥信
     */
    @ExcelProperty("遥信类型")
    private String remoteSignalingType;

}
