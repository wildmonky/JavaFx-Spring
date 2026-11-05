package com.zhuofan.validator.javafx.controller.workOrder;

import com.dlsc.gemsfx.ExpandingTextArea;
import com.zhuofan.validator.spring.model.AutomaticMaintenanceWorkOrderModel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.File;

@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Controller
public class MaintenanceWorkOrderController extends Control {

    public GridPane mainGrid;
    /**
     * 执行调度
     */
    public TextField dispatchingAuthority;
    /**
     * 检修设备类型
     */
    public TextField maintenanceEquipType;
    /**
     * 申请单号
     */
    public TextField orderCode;
    /**
     * 申请单位
     */
    public TextField orderUnit;
    /**
     * 申请提交时间
     */
    public TextField orderCommitTime;
    /**
     * 申请负责人
     */
    public ComboBox<String> orderRequester;
    /**
     * 联系电话
     */
    public TextField orderRequestPhone;
    /**
     * 检修类型
     */
    public TextField maintenanceType;
    /**
     * 许可方式
     */
    public TextField allowType;
    /**
     * 值班员
     */
    public TextField dutyOperator;
    /**
     * 值班员联系电话
     */
    public TextField dutyOperatorPhone;
    /**
     * 工作地点
     */
    public ComboBox<String> workLocation;
    /**
     * 停电/检修设备
     */
    public TextField maintenanceEquipments;
    /**
     * 工作内容
     */
    public ExpandingTextArea workContent;
    /**
     * 安自要求
     */
    public ExpandingTextArea azRequirement;
    /**
     * 保护要求
     */
    public ExpandingTextArea protectRequirement;
    /**
     * 通信要求
     */
    public ExpandingTextArea communicateRequirement;
    /**
     * 自动化要求
     */
    public ExpandingTextArea automaticRequirement;
    /**
     * 其它要求
     */
    public ExpandingTextArea otherRequirement;
    /**
     * 申请工作开始时间
     */
    public TextField workStartTime;
    /**
     * 申请工作结束时间
     */
    public TextField workEndTime;
    /**
     * 申请工作时长
     */
    public TextField workDuration;
    /**
     * 是否影响自动化
     */
    public TextField isImpactAuto;
    /**
     * 使用移动介质
     */
    public TextField isUsingMobileMedia;
    /**
     * 远程拨号登录
     */
    public TextField remoteCallLogin;
    /**
     * 外来设备接入
     */
    public TextField externalMediaAccess;
    /**
     * 自动化并网新设备
     */
    public TextField autoNewEquip;
    /**
     * 自动化定检设备
     */
    public TextField autoMaintenanceEquip;
    /**
     * 自动化反措设备
     */
    public TextField autoAntiAccidentEquip;
    /**
     * 自动化消缺设备
     */
    public TextField autoEliminationEquip;
    /**
     * 涉及供电可靠性的影响
     */
    public ExpandingTextArea impact;
    /**
     * 其他影响
     */
    public ExpandingTextArea otherImpact;
    /**
     * 附件
     */
    public TextField file;
    /**
     * 申请备注
     */
    public ExpandingTextArea comment;

    @Getter
    private File selectedFile;

    @FXML
    public void initialize() {

        file.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("文件列表");
            FileChooser.ExtensionFilter excelExtensionFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
            fileChooser.getExtensionFilters().add(excelExtensionFilter);
            this.selectedFile = fileChooser.showOpenDialog(file.getScene().getWindow());
            if (selectedFile == null) {
                file.setText(null);
                return;
            }

            int maxLength = 28;
            String name = selectedFile.getName();
            if (name.length() >= maxLength) {
                name = name.substring(0, maxLength + 1) + "...";
            }
            file.setText(name);
        });
    }

    public AutomaticMaintenanceWorkOrderModel getData() {
        AutomaticMaintenanceWorkOrderModel model = new AutomaticMaintenanceWorkOrderModel();
        model.setDispatchingAuthority(dispatchingAuthority.getText());
        model.setMaintenanceEquipType(maintenanceEquipType.getText());
        model.setOrderCode(orderCode.getText());
        model.setOrderUnit(orderUnit.getText());
        model.setOrderCommitTime(orderCommitTime.getText());
        model.setOrderRequester(orderRequester.getValue());
        model.setOrderRequestPhone(orderRequestPhone.getText());
        model.setMaintenanceType(maintenanceType.getText());
        model.setAllowType(allowType.getText());
        model.setDutyOperator(dutyOperator.getText());
        model.setDutyOperatorPhone(dutyOperatorPhone.getText());
        model.setWorkLocation(workLocation.getValue());
        model.setMaintenanceEquipments(maintenanceEquipments.getText());
        model.setWorkContent(workContent.getText());
        model.setAzRequirement(azRequirement.getText());
        model.setProtectRequirement(protectRequirement.getText());
        model.setCommunicateRequirement(communicateRequirement.getText());
        model.setAutomaticRequirement(automaticRequirement.getText());
        model.setOtherRequirement(otherRequirement.getText());
        model.setWorkStartTime(workStartTime.getText());
        model.setWorkEndTime(workEndTime.getText());
        model.setWorkDuration(workDuration.getText());
        model.setIsImpactAuto(isImpactAuto.getText());
        model.setIsUsingMobileMedia(isUsingMobileMedia.getText());
        model.setRemoteCallLogin(remoteCallLogin.getText());
        model.setExternalMediaAccess(externalMediaAccess.getText());
        model.setAutoNewEquip(autoNewEquip.getText());
        model.setAutoMaintenanceEquip(autoMaintenanceEquip.getText());
        model.setAutoAntiAccidentEquip(autoAntiAccidentEquip.getText());
        model.setAutoEliminationEquip(autoEliminationEquip.getText());
        model.setImpact(impact.getText());
        model.setOtherImpact(otherImpact.getText());
        model.setFile(selectedFile);
        model.setComment(comment.getText());
        return model;
    }

}
