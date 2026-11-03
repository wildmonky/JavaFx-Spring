package com.zhuofan.validator.spring.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.fesod.sheet.annotation.write.style.ContentRowHeight;
import org.apache.fesod.sheet.annotation.write.style.ContentStyle;
import org.apache.fesod.sheet.enums.BooleanEnum;

import java.io.File;

@Getter
@Setter
@EqualsAndHashCode
@ContentRowHeight(-1)
public class AutomaticMaintenanceWorkOrderModel {

    /**
     * 执行调度
     */
    private String dispatchingAuthority;

    /**
     * 检修设备类型
     */
    private String maintenanceEquipType;

    /**
     * 申请单号
     */
    private String orderCode;

    /**
     * 申请单位
     */
    private String orderUnit;

    /**
     * 申请提交时间
     */
    private String orderCommitTime;

    /**
     * 申请负责人
     */
    private String orderRequester;

    /**
     * 联系电话
     */
    private String orderRequestPhone;

    /**
     * 检修类型
     */
    private String maintenanceType;

    /**
     * 许可方式
     */
    private String allowType;

    /**
     * 值班员
     */
    private String dutyOperator;

    /**
     * 值班员联系电话
     */
    private String dutyOperatorPhone;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 停电/检修设备
     */
    private String maintenanceEquipments;

    /**
     * 工作内容
     */
    private String workContent;

    /**
     * 安自要求
     */
    private String azRequirement;

    /**
     * 保护要求
     */
    private String protectRequirement;

    /**
     * 通信要求
     */
    private String communicateRequirement;

    /**
     * 自动化要求
     */
    @ContentStyle(wrapped = BooleanEnum.TRUE)
    private String automaticRequirement;

    /**
     * 其它要求
     */
    private String otherRequirement;

    /**
     * 申请工作开始时间
     */
    private String workStartTime;

    /**
     * 申请工作结束时间
     */
    private String workEndTime;

    /**
     * 申请工作时长
     */
    private String workDuration;

    /**
     * 是否影响自动化
     */
    private String isImpactAuto;

    /**
     * 使用移动介质
     */
    private String isUsingMobileMedia;

    /**
     * 远程拨号登录
     */
    private String remoteCallLogin;

    /**
     * 外来设备接入
     */
    private String externalMediaAccess;

    /**
     * 自动化并网新设备
     */
    private String autoNewEquip;

    /**
     * 自动化定检设备
     */
    private String autoMaintenanceEquip;

    /**
     * 自动化反措设备
     */
    private String autoAntiAccidentEquip;

    /**
     * 自动化消缺设备
     */
    private String autoEliminationEquip;

    /**
     * 涉及供电可靠性的影响
     */
    private String impact;

    /**
     * 其他影响
     */
    private String otherImpact;

    /**
     * 附件
     */
    private File file;

    /**
     * 申请备注
     */
    private String comment;

}
