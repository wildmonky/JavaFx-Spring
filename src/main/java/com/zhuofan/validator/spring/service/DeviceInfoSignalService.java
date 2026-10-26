package com.zhuofan.validator.spring.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.entity.DeviceInfoSignal;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.entity.SignalTemplateDetail;
import com.zhuofan.validator.spring.mapper.*;
import com.zhuofan.validator.spring.model.SignalModel;
import com.zhuofan.validator.spring.model.TreeNode;
import jakarta.annotation.Resource;
import javafx.collections.FXCollections;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceInfoSignalService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private DeviceInfoSignalMapper mapper;

    @Resource
    private DeviceInfoSignalMapper signalMapper;

    @Resource
    private SignalTemplateMapper signalTemplateMapper;

    @Resource
    private SignalTemplateDetailMapper signalTemplateDetailMapper;

    public List<TreeNode<SignalModel>> searchSignalTreeByDeviceInfoIds(List<String> deviceInfoIds) {
        if (CollectionUtils.isEmpty(deviceInfoIds)) {
            return Collections.emptyList();
        }

        Map<String, String> deviceInfoMap = deviceInfoMapper.selectList(Wrappers.<DeviceInfo>lambdaQuery().in(DeviceInfo::getId, deviceInfoIds)).stream().collect(Collectors.toMap(DeviceInfo::getId, DeviceInfo::getName));
        List<DeviceInfoSignal> deviceInfoSignals = mapper.selectList(Wrappers.<DeviceInfoSignal>lambdaQuery().in(DeviceInfoSignal::getDeviceInfoId, deviceInfoIds));

        return buildTree(deviceInfoMap, deviceInfoSignals);
    }

    public List<TreeNode<SignalModel>> searchSignalTreeByDeviceInfoIdsAndSignal(List<String> deviceInfoIds, SignalModel deviceInfoSignal) {
        if (CollectionUtils.isEmpty(deviceInfoIds)) {
            return Collections.emptyList();
        }

        Map<String, String> deviceInfoMap = deviceInfoMapper.selectList(Wrappers.<DeviceInfo>lambdaQuery().in(DeviceInfo::getId, deviceInfoIds)).stream().collect(Collectors.toMap(DeviceInfo::getId, DeviceInfo::getName));
        List<DeviceInfoSignal> deviceInfoSignals = mapper.selectList(Wrappers.<DeviceInfoSignal>lambdaQuery()
                .in(DeviceInfoSignal::getDeviceInfoId, deviceInfoIds)
                .like(deviceInfoSignal != null && StringUtils.isNotBlank(deviceInfoSignal.getSignalType()), DeviceInfoSignal::getSignalType, deviceInfoSignal.getSignalType())
                .like(deviceInfoSignal != null && StringUtils.isNotBlank(deviceInfoSignal.getSignalTemplate()), DeviceInfoSignal::getSignalTemplate, deviceInfoSignal.getSignalTemplate())
        );

        return buildTree(deviceInfoMap, deviceInfoSignals);
    }

    public List<TreeNode<SignalModel>> buildTree(Map<String, String> deviceInfoMap, List<DeviceInfoSignal> deviceInfoSignals) {
        List<TreeNode<SignalModel>> reList = new ArrayList<>();

        Map<String, List<DeviceInfoSignal>> deviceSignalMap = deviceInfoSignals.stream().collect(Collectors.groupingBy(DeviceInfoSignal::getDeviceInfoId));

        for (Map.Entry<String, String> entry : deviceInfoMap.entrySet()) {
            String deviceId = entry.getKey();
            List<DeviceInfoSignal> signals = deviceSignalMap.get(deviceId);

            TreeNode<SignalModel> child = new TreeNode<>();

            SignalModel deviceNode = new SignalModel();
            deviceNode.setDeviceInfoId(entry.getKey());
            deviceNode.setDeviceName(entry.getValue());
            child.setSelf(deviceNode);
            child.setChildren(FXCollections.observableArrayList());

            if (signals != null) {
                for (DeviceInfoSignal deviceInfoSignal : signals) {
                    TreeNode<SignalModel> node = new TreeNode<>();
                    node.setParent(child);
                    node.setSelf(new SignalModel(deviceInfoSignal));
                    child.getChildren().add(node);
                }
            }

            reList.add(child);
        }

        return reList;
    }

    /**
     * 检查设备对应的信号是否存在，如果不存在则创建
     *
     * @param deviceIdCheckList 待检查的设备信息id
     * @return 创建的设备信号列表
     */
    public List<DeviceInfoSignal> checkSignalAndBuildSignalWhenNonExisted(List<String> deviceIdCheckList) {
        if (CollectionUtils.isEmpty(deviceIdCheckList)) {
            return Collections.emptyList();
        }

        // 删除旧信号
        signalMapper.delete(Wrappers.<DeviceInfoSignal>lambdaQuery().in(DeviceInfoSignal::getDeviceInfoId, deviceIdCheckList));

        List<String> existedSignalDeviceIds = signalMapper.selectList(Wrappers.<DeviceInfoSignal>lambdaQuery()
                .select(DeviceInfoSignal::getDeviceInfoId)
                .in(DeviceInfoSignal::getDeviceInfoId, deviceIdCheckList)
                .groupBy(DeviceInfoSignal::getDeviceInfoId)
        ).stream().map(DeviceInfoSignal::getDeviceInfoId).toList();

        List<String> deviceIdsWithoutSignal = deviceIdCheckList.stream().filter(id -> !existedSignalDeviceIds.contains(id)).toList();
        if (CollectionUtils.isEmpty(deviceIdsWithoutSignal)) {
            return Collections.emptyList();
        }

        List<DeviceInfo> deviceInfos = deviceInfoMapper.selectList(Wrappers.<DeviceInfo>lambdaQuery().in(DeviceInfo::getId, deviceIdsWithoutSignal));
        if (deviceInfos.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceInfoSignal> reList = new ArrayList<>();
        for (DeviceInfo deviceInfo : deviceInfos) {
            reList.addAll(buildSignalWithDevice(deviceInfo));
        }

        List<List<DeviceInfoSignal>> split = com.zhuofan.validator.spring.utils.CollectionUtils.split(reList, 300);
        if (!split.isEmpty()) {
            for (List<DeviceInfoSignal> signals : split) {
                signalMapper.insertBatchSomeColumn(signals);
            }
        }

        return reList;
    }

    public List<DeviceInfoSignal> buildSignalWithDevice(DeviceInfo deviceInfo) {

        String signalTemplateId = deviceInfo.getSignalTemplateId();
        if (StringUtils.isBlank(signalTemplateId)) {
            return Collections.emptyList();
        }

        SignalTemplate signalTemplate = signalTemplateMapper.selectById(signalTemplateId);
        if (signalTemplate == null) {
            return Collections.emptyList();
        }

        List<SignalTemplateDetail> signalTemplateDetails = signalTemplateDetailMapper.selectList(Wrappers.<SignalTemplateDetail>lambdaQuery().eq(SignalTemplateDetail::getTemplateId, signalTemplateId));
        if (signalTemplateDetails.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceInfoSignal> reList = new ArrayList<>();
        for (SignalTemplateDetail template : signalTemplateDetails) {
            DeviceInfoSignal signal = new DeviceInfoSignal();
            signal.setDeviceNameInSignal(deviceInfo.getName());
            signal.setSignalType(template.getType());
            signal.setDeviceInfoId(deviceInfo.getId());
            signal.setSignalTemplate(template.getDesc());
            signal.setSignal(deviceInfo.getVoltage() + deviceInfo.getName() + template.getContent());
            signal.setBayName(deviceInfo.getBayName());
            signal.setNormalStatus(template.getNormalStatus());
            signal.setRemoteSignalingType(template.getRemoteSignalingType());
            reList.add(signal);
        }

        return reList;
    }

    public boolean saveDeviceInfoSignal(DeviceInfoSignal deviceInfoSignal) {
        if (deviceInfoSignal == null) {
            return false;
        }

        return signalMapper.insertOrUpdate(deviceInfoSignal);
    }

    public void exportDeviceSignals(Collection<String> deviceInfoIds, OutputStream outputStream) {

        if (CollectionUtils.isEmpty(deviceInfoIds)) {
            return;
        }

        List<DeviceInfo> deviceInfos = deviceInfoMapper.selectList(Wrappers.<DeviceInfo>lambdaQuery().in(DeviceInfo::getId, deviceInfoIds));
        if (CollectionUtils.isEmpty(deviceInfoIds)) {
            return;
        }
        Map<String, DeviceInfo> deviceInfoIdMap = deviceInfos.stream().collect(Collectors.toMap(DeviceInfo::getId, e -> e));

        List<DeviceInfoSignal> deviceInfoSignals = signalMapper.selectList(Wrappers.<DeviceInfoSignal>lambdaQuery().in(DeviceInfoSignal::getDeviceInfoId, deviceInfoIds));
        Map<String, List<DeviceInfoSignal>> deviceSignalMap = deviceInfoSignals.stream().collect(Collectors.groupingBy(DeviceInfoSignal::getDeviceInfoId));

        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            for (Map.Entry<String, DeviceInfo> entry : deviceInfoIdMap.entrySet()) {
                DeviceInfo deviceInfo = entry.getValue();
                XSSFSheet sheet = workbook.createSheet(deviceInfo.getName());

                int rowIndex = 0;
                // 首行
                XSSFRow row = sheet.createRow(rowIndex++);
                row.createCell(0, CellType.STRING).setCellValue("点号");
                row.createCell(1, CellType.STRING).setCellValue("信号名称");
                row.createCell(2, CellType.STRING).setCellValue("间隔");
                row.createCell(3, CellType.STRING).setCellValue("信号常态");
                row.createCell(4, CellType.STRING).setCellValue("遥信类型");

                // 设置列宽
                sheet.setColumnWidth(0, 8 * 256);
                sheet.setColumnWidth(1, 90 * 256);
                sheet.setColumnWidth(2, 30 * 256);
                sheet.setColumnWidth(3, 15 * 256);
                sheet.setColumnWidth(4, 20 * 256);

                List<DeviceInfoSignal> signals = deviceSignalMap.get(entry.getKey());
                if (CollectionUtils.isEmpty(signals)) {
                    continue;
                }

                for (DeviceInfoSignal signal : signals) {
                    XSSFRow dataRow = sheet.createRow(rowIndex++);
                    dataRow.createCell(0);
                    dataRow.createCell(1).setCellValue(signal.getSignal());
                    dataRow.createCell(2).setCellValue(signal.getBayName());
                    dataRow.createCell(3).setCellValue(signal.getNormalStatus());
                    dataRow.createCell(4).setCellValue(signal.getRemoteSignalingType());
                }
            }

            workbook.write(outputStream);
        } catch (Exception ignore) {}
    }

}
