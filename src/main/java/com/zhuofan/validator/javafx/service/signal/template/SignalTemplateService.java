package com.zhuofan.validator.javafx.service.signal.template;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.javafx.properties.SignalTemplateDetailProperty;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.entity.SignalTemplateDetail;
import com.zhuofan.validator.spring.mapper.SignalTemplateDetailMapper;
import com.zhuofan.validator.spring.mapper.SignalTemplateMapper;
import com.zhuofan.validator.spring.model.SignalTemplateDetailModel;
import com.zhuofan.validator.spring.model.SignalTemplateModel;
import com.zhuofan.validator.spring.utils.CollectionUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SignalTemplateService {

    @Resource
    private SignalTemplateMapper templateMapper;

    @Resource
    private SignalTemplateDetailMapper detailMapper;

    public void removeTemplateByName(String templateName) {
        if (StringUtils.isBlank(templateName)) {
            return;
        }

        templateMapper.delete(Wrappers.<SignalTemplate>lambdaQuery().eq(SignalTemplate::getName, templateName));
    }

    public void removeTemplateDetailById(String detailId) {
        if (StringUtils.isBlank(detailId)) {
            return;
        }

        detailMapper.delete(Wrappers.<SignalTemplateDetail>lambdaQuery()
                .eq(SignalTemplateDetail::getId, detailId)
        );
    }

    public Map<SignalTemplate, List<SignalTemplateDetail>> importDataToDb(File file) {
        if (file == null) {
            return Collections.emptyMap();
        }

        Map<SignalTemplate, List<SignalTemplateDetail>> dataMap = new HashMap<>();
        Map<String, SignalTemplate> sheetMap = new HashMap<>();

        try {
            // 1. POI 解析excel文件,提取数据
            ExcelReader reader = EasyExcel.read().file(file).head(SignalTemplateDetail.class).registerReadListener(new ReadListener<SignalTemplateDetail>() {
                @Override
                public void invoke(SignalTemplateDetail data, AnalysisContext context) {
                    String sheetName = context.readSheetHolder().getSheetName();
                    SignalTemplate signalTemplate = sheetMap.computeIfAbsent(sheetName, s -> {
                        SignalTemplate template = new SignalTemplate();
                        template.setName(s);
                        return template;
                    });
                    List<SignalTemplateDetail> details = dataMap.computeIfAbsent(signalTemplate, t -> new ArrayList<>());
                    details.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 2. 保存数据，每个sheet读取完都会调用，注意清空重复数据
//                    importData(dataMap);
                }
            }).build();
            List<ReadSheet> readSheets = reader.excelExecutor().sheetList();
            reader.read(readSheets);
            importDataToDb(dataMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dataMap;
    }

    /**
     * 导入数据
     */
    public void importDataToDb(Map<SignalTemplate, List<SignalTemplateDetail>> dataMap) {
        List<SignalTemplate> templateList = dataMap.keySet().stream().toList();
        List<List<SignalTemplate>> split = CollectionUtils.split(templateList, 500);
        for (List<SignalTemplate> templates : split) {
            Map<String, SignalTemplate> map = templates.stream().collect(Collectors.toMap(SignalTemplate::getName, e -> e));
            List<String> names = templates.stream().map(SignalTemplate::getName).toList();
            Map<String, String> existedMap = templateMapper.selectList(Wrappers.<SignalTemplate>lambdaQuery().in(SignalTemplate::getName, names))
                    .stream().collect(Collectors.toMap(SignalTemplate::getName, SignalTemplate::getId));

            if (map.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, String> entry : existedMap.entrySet()) {
                String name = entry.getKey();
                String id = entry.getValue();

                SignalTemplate signalTemplate = map.get(name);
                if (signalTemplate == null) {
                    continue;
                }

                signalTemplate.setId(id);
            }
        }
        templateMapper.insertOrUpdate(templateList, 500);
        List<SignalTemplateDetail> saveDetails = new ArrayList<>();
        for (Map.Entry<SignalTemplate, List<SignalTemplateDetail>> entry : dataMap.entrySet()) {
            SignalTemplate template = entry.getKey();
            if (template.getId() == null) {
                continue;
            }
            List<SignalTemplateDetail> details = entry.getValue();
            details.forEach(e -> e.setTemplateId(template.getId()));
            saveDetails.addAll(details);
        }

        detailMapper.insert(saveDetails, 500);
//        dataMap.clear();
    }

    public boolean saveTemplate(SignalTemplate param) {
        if (param == null) {
            return false;
        }

        return templateMapper.insertOrUpdate(param);
    }

    public boolean saveDetail(List<SignalTemplateDetail> param) {
        if (org.springframework.util.CollectionUtils.isEmpty(param)) {
            log.warn("传入参数为空");
            return false;
        }

        detailMapper.insertOrUpdate(param);
        return true;
    }

    public List<SignalTemplateDetailProperty> searchDetails(SignalTemplateDetailModel param) {
        if (param == null) {
            return Collections.emptyList();
        }

        return detailMapper.selectList(Wrappers.<SignalTemplateDetail>lambdaQuery()
                .eq(param.getTemplateId() != null, SignalTemplateDetail::getTemplateId, param.getTemplateId())
                .like(StringUtils.isNotBlank(param.getType()), SignalTemplateDetail::getType, param.getType())
                .like(StringUtils.isNotBlank(param.getSample()), SignalTemplateDetail::getSample, param.getSample())
        ).stream().map(SignalTemplateDetailProperty::new).toList();
    }

    public List<SignalTemplateModel> searchTemplateByName(String templateName) {
        return templateMapper.selectList(Wrappers.<SignalTemplate>lambdaQuery()
                .like(StringUtils.isNotBlank(templateName), SignalTemplate::getName, templateName)
        ).stream().map(SignalTemplateModel::new).toList();
    }

}
