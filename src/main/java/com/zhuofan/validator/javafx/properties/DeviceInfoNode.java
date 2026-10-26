package com.zhuofan.validator.javafx.properties;

import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class DeviceInfoNode extends DeviceInfoModel {

    private StringProperty nameProp = new SimpleStringProperty();

    private StringProperty voltageLevelProp = new SimpleStringProperty();

    private StringProperty typeProp = new SimpleStringProperty();

    private StringProperty templateIdProp = new SimpleStringProperty();

    private StringProperty templateNameProp = new SimpleStringProperty();

    private StringProperty bayNameProp = new SimpleStringProperty();

    public DeviceInfoNode() {
        super();
        // 变更监听
        nameProp.addListener((observable, oldValue, newValue) -> {
            log.info("name changed: {} ---> {}", oldValue, newValue);
        });
    }

    public DeviceInfoNode(DeviceInfoModel deviceInfo) {
        super();
        super.setId(deviceInfo.getId());
        super.setParentId(deviceInfo.getParentId());
        super.setPatternId(deviceInfo.getPatternId());
        super.setSignalTemplateId(deviceInfo.getSignalTemplateId());
        setName(deviceInfo.getName());
        setVoltage(deviceInfo.getVoltage());
        setType(deviceInfo.getType());
        setTemplateId(deviceInfo.getSignalTemplateId());
        setTemplateName(deviceInfo.getSignalTemplateName());
        setBayName(deviceInfo.getBayName());
    }

    public void setName(String name) {
        super.setName(name);
        nameProp.setValue(name);
    }

    public void setVoltage(String voltage) {
        super.setVoltage(voltage);
        voltageLevelProp.setValue(voltage);
    }

    public void setType(String type) {
        super.setType(type);
        typeProp.setValue(type);
    }

    public void setTemplateId(String templateId) {
        super.setSignalTemplateId(templateId);
        templateIdProp.setValue(templateId);
    }

    public void setTemplateName(String templateName) {
        super.setSignalTemplateName(templateName);
        templateNameProp.setValue(templateName);
    }

    public void setBayName(String bayName) {
        super.setBayName(bayName);
        bayNameProp.setValue(bayName);
    }

}
