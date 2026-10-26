package com.zhuofan.validator.javafx.properties;

import com.zhuofan.validator.spring.entity.SignalTemplateDetail;
import com.zhuofan.validator.spring.model.SignalTemplateDetailModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.print.DocFlavor;

@Getter
@Setter
public class SignalTemplateDetailProperty extends SignalTemplateDetailModel {

    private StringProperty descProp;

    private StringProperty typeProp;

    private StringProperty sampleProp;

    private StringProperty contentProp;

    private StringProperty normalStatusProp;

    private StringProperty remoteSignalingTypeProp;

    public SignalTemplateDetailProperty(SignalTemplateDetail model) {
        if (model == null) {
            return;
        }
        BeanUtils.copyProperties(model, this);
        this.descProp = new SimpleStringProperty(this.getDesc());
        this.descProp.addListener((observe, oldVal, newVal) -> {
            setDesc(newVal);
        }) ;

        this.typeProp = new SimpleStringProperty(this.getType());
        this.typeProp.addListener((observe, oldVal, newVal) -> {
            setType(newVal);
        }) ;

        this.sampleProp = new SimpleStringProperty(this.getSample());
        this.sampleProp.addListener((observe, oldVal, newVal) -> {
            setSample(newVal);
        }) ;

        this.contentProp = new SimpleStringProperty(this.getContent());
        this.contentProp.addListener((observe, oldVal, newVal) -> {
            setContent(newVal);
        }) ;

        this.normalStatusProp = new SimpleStringProperty(this.getNormalStatus());
        this.normalStatusProp.addListener((observe, oldVal, newVal) -> {
            setNormalStatus(newVal);
        }) ;

        this.remoteSignalingTypeProp = new SimpleStringProperty(this.getRemoteSignalingType());
        this.remoteSignalingTypeProp.addListener((observe, oldVal, newVal) -> {
            setRemoteSignalingType(newVal);
        }) ;
    }

}