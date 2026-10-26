package com.zhuofan.validator.javafx.service.device;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.mapper.SignalTemplateMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JavaFxDeviceService {

    @Resource
    private SignalTemplateMapper templateMapper;

    public List<SignalTemplate> findSignalTemplates(SignalTemplate param) {
        if (param == null) {
            return templateMapper.selectList(Wrappers.emptyWrapper());
        }
        return templateMapper.selectList(Wrappers.<SignalTemplate>lambdaQuery()
                .eq(param.getId() != null, SignalTemplate::getId, param.getId())
                .like(StringUtils.isNotBlank(param.getName()), SignalTemplate::getName, param.getName())
        );
    }


}
