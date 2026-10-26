package com.zhuofan.validator.spring.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@TableName("\"DEVICE_INFO\"")
public class DeviceInfo {

    @TableId("ID")
    private String id;

    @TableField("PARENT_ID")
    private String parentId;

    @TableField("NAME")
    private String name;

    // 根据电压、设备类型查询对应设备
    @TableField("VOLTAGE")
    private String voltage;

    @TableField("TYPE")
    private String type;

    @TableField(value = "SIGNAL_TEMPLATE_ID", updateStrategy = FieldStrategy.ALWAYS)
    private String signalTemplateId;

    @TableField("PATTERN_ID")
    private String patternId;

    /**
     * 间隔
     */
    private String bayName;

    @TableField(exist = false)
    private Boolean checkResult;

    @TableField(exist = false)
    private String checkResultMessage;

    @TableField(exist = false)
    private List<DeviceInfo> children;

    public DeviceInfo() {}

    public DeviceInfo(String voltage, String name, String type) {
        this.voltage = voltage;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo device = (DeviceInfo) o;
        return Objects.equals(id, device.id) && Objects.equals(name, device.name) && Objects.equals(voltage, device.voltage) && Objects.equals(type, device.type) && Objects.equals(patternId, device.patternId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, voltage, type, patternId);
    }
}
