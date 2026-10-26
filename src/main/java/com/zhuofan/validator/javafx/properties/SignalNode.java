package com.zhuofan.validator.javafx.properties;

import com.zhuofan.validator.spring.model.SignalModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Slf4j
@Getter
public class SignalNode extends SignalModel {

    private StringProperty deviceNameProperty = new SimpleStringProperty();

    private StringProperty typeProperty = new SimpleStringProperty();

    private StringProperty templateProperty = new SimpleStringProperty();

    private StringProperty signalProperty = new SimpleStringProperty();

    private StringProperty bayNameProp = new SimpleStringProperty();

    private StringProperty normalStatusProp = new SimpleStringProperty();

    private StringProperty remoteSignalingTypeProp = new SimpleStringProperty();

    public SignalNode() {
        super();
        // 变更监听
        typeProperty.addListener((observable, oldValue, newValue) -> {
            log.info("name changed: {} ---> {}", oldValue, newValue);
        });
    }

    public SignalNode(SignalModel signalModel) {
        super();
        BeanUtils.copyProperties(signalModel, this);

        deviceNameProperty.set(signalModel.getDeviceName());
        typeProperty.set(signalModel.getSignalType());
        templateProperty.set(signalModel.getSignalTemplate());
        signalProperty.set(signalModel.getSignal());
        bayNameProp.set(signalModel.getBayName());
        normalStatusProp.set(signalModel.getNormalStatus());
        remoteSignalingTypeProp.set(signalModel.getRemoteSignalingType());
    }

}
