
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.zhuofan.validator.spring.MainApplication;
import com.zhuofan.validator.spring.entity.AlarmRegex;
import com.zhuofan.validator.spring.entity.AlarmRule;
import com.zhuofan.validator.spring.entity.AlarmType;
import com.zhuofan.validator.spring.entity.Device;
import com.zhuofan.validator.spring.enums.DeviceTypeEnum;
import com.zhuofan.validator.spring.mapper.AlarmRegexMapper;
import com.zhuofan.validator.spring.mapper.AlarmRuleMapper;
import com.zhuofan.validator.spring.mapper.AlarmTypeMapper;
import com.zhuofan.validator.spring.mapper.DeviceMapper;
import com.zhuofan.validator.spring.model.AlarmTypeRuleModel;
import com.zhuofan.validator.spring.service.AlarmRuleService;
import com.zhuofan.validator.spring.service.AlarmTypeService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest(classes = { MainApplication.class })
public class AlarmInfoCheckTest {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private AlarmRegexMapper deviceSCADARegexMapper;

    @Resource
    private AlarmTypeMapper deviceSCADAInfoTypeMapper;
    @Resource
    private AlarmTypeService deviceSCADAInfoTypeService;


    @Resource
    private AlarmRuleMapper deviceSCADAInfoRuleMapper;

    @Resource
    private AlarmRuleService deviceSCADAInfoRuleService;

    @Resource
    private DataSourceTransactionManager dataSourceTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    private String inputFilePath = "C:\\Users\\15259\\Desktop\\项目\\2025-08-01 变电站遥测信息规范检测\\规则检测\\规范信息导入表格.xlsx";

    private final Map<RegexTransfer, String> regexTransferMap = new HashMap<>();



    /**
     * 母线 规则 导入
     */
    @Test
    public void generatrixRuleInput() {
        String[] voltages = {"500kV", "220kV", "110kV", "66kV", "35kV", "20kV", "10kV"};
//        String[] voltages = {"66kV", "35kV", "20kV", "10kV"};
        clearInput(voltages, DeviceTypeEnum.GENERATRIX);
        for (String voltage : voltages) {

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 11,new String[]{voltage+"母线"}, 2);

            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage  + "#×母线");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.GENERATRIX.getDesc());

            device.setPatternId(simpleRule("^#\\d+?(?:母线|(?=母差))|#\\d+$", ""));

            deviceMapper.insert(device);
            saveDataToDB(device, new String[]{voltage + "#×母线"}, result);

            if (Objects.equals("220kV", voltage) || Objects.equals("110kV", voltage) || Objects.equals("10kV", voltage)) {
                clearInput(new String[]{voltage}, DeviceTypeEnum.DOUBLE_GENERATRIX);
                result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 11,new String[]{ voltage+"xMxM母线" }, 2);
                device = new Device();
                device.setId(deviceMapper.nextSeqId() + "");
                device.setName(voltage + "xMxM母线");
                device.setVoltage(voltage);
                device.setType(DeviceTypeEnum.DOUBLE_GENERATRIX.getDesc());
                device.setPatternId(simpleRule("^#\\d+母线#\\d+母线|母联|分段$", ""));
                deviceMapper.insert(device);
                saveDataToDB(device, new String[]{ voltage + "#×母线#×母线", voltage + "×M×M"}, result);
            }
        }

    }

    /**
     * 主变规则导入
     */
    @Test
    public void masterTransformerRuleInput() {

        String[] voltages = {"500kV", "220kV", "110kV",""};

        clearInput(voltages, DeviceTypeEnum.MAIN_TRANSFORMER);
        for (String voltage : voltages) {
            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 5, 5, new String[]{voltage + "主变"},2);

            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage+"#x主变");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.MAIN_TRANSFORMER.getDesc());

            device.setPatternId(simpleRule("^#\\d+主变$", ""));

            deviceMapper.insert(device);

            saveDataToDB(device, new String[]{"#×主变"}, result);
        }
    }

    /**
     * 线路规则导入
     */
    @Test
    public void lineRuleInput() {
        String[] voltages = {"500kV", "220kV", "110kV", "35kV", "20kV","10kV"};

        clearInput(voltages, DeviceTypeEnum.LINE);
        for (String voltage : voltages) {

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 11, new String[]{voltage + "线路"}, 2);

            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage+"xx线");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.LINE.getDesc());

            device.setPatternId(simpleRule("^.+线$", ""));

            deviceMapper.insert(device);
            saveDataToDB(device, new String[]{voltage+ (("20kV".equals(voltage) || "10kV".equals(voltage)) ? "××馈线" :"××线")}, result);
        }
    }

    /**
     * 开关规则导入
     */
    @Test
    public void switchRuleInput() {

        String[] voltages = {"500kV", "220kV", "110kV", "35kV", "20kV", "10kV"};
        clearInput(voltages, DeviceTypeEnum.SWITCH);
        for (String voltage : voltages) {

            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage+"xx开关间隔");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.SWITCH.getDesc());


            // "^\\d+开关(?:间隔){0,1}$"
            device.setPatternId(simpleRule("^.+开关(?=间隔|)(?!开关)$", ""));

            deviceMapper.insert(device);

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0, new String[]{voltage + "开关"}, 2);
            saveDataToDB(device, new String[]{voltage+"××开关"}, result);
        }
    }

    /**
     * 线高抗 规则导入
     */
    @Test
    public void lineHighResistanceRuleInput() {
        clearInput(new String[]{"500kV"}, DeviceTypeEnum.LINE_HIGH_RESISTANCE);
        Device device = new Device();
        device.setId(deviceMapper.nextSeqId() + "");
        device.setName("500kVxx线高抗");
        device.setVoltage("500kV");
        device.setType(DeviceTypeEnum.LINE_HIGH_RESISTANCE.getDesc());

        device.setPatternId(simpleRule("^\\d*线高抗$", ""));

        deviceMapper.insert(device);

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"500kV高抗"}, 2);
        saveDataToDB(device, new String[]{"500kV××线高抗"}, result);
    }

    /**
     * 电容器组/电抗器组 规则导入
     */
    @Test
    public void capacitanceAndReactanceRuleInput() {
        String[] voltages = new String[]{"66kV", "35kV", "20kV", "10kV"};
        clearInput(voltages, DeviceTypeEnum.CAPACITANCE_AND_REACTANCE);
        for (String voltage : voltages) {
            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage + "#x电容器组/电抗器组");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.CAPACITANCE_AND_REACTANCE.getDesc());

            if (voltage.contains("66") || voltage.contains("35")) {
                device.setPatternId(simpleRule("^(?:#\\d+低压电(?:容|抗)器|AVC(?:#\\d+低压电容器|))$",""));
            } else {
                device.setPatternId(simpleRule("^(?:#\\d+电(?:容|抗)器组|AVC(?:#\\d+电容器组|))$",""));
            }

            deviceMapper.insert(device);

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{ voltage+"电容|电抗" }, 2);
            saveDataToDB(device, new String[]{voltage+"#×电容器组/电抗器组"}, result);
        }
    }

    /**
     * 曲折变/接地变 规则导入
     */
    @Test
    public void zigzagTransformAndGroundTransformerRuleInput() {
        String[] voltages = new String[]{"20kV", "10kV"};
        clearInput(voltages, DeviceTypeEnum.ZIGZAG_AND_GROUND_TRANSFORMER);
        for (String voltage : voltages) {
            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage + "#x曲折变/接地变");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.ZIGZAG_AND_GROUND_TRANSFORMER.getDesc());

            device.setPatternId(simpleRule("^#\\d*(?:曲折|接地)变$", ""));

            deviceMapper.insert(device);

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{voltage+"曲折变|接地变"}, 2);
            saveDataToDB(device, new String[]{voltage+"#×曲折变/接地变"}, result);
        }
    }

    /**
     * 消弧线圈 规则导入
     */
    @Test
    public void petersonCoilRuleInput() {
        clearInput(null, DeviceTypeEnum.PETERSON_COIL);
        Device device = new Device();
        device.setId(deviceMapper.nextSeqId() + "");
        device.setName("××消弧线圈");
        device.setVoltage("10kV");
        device.setType(DeviceTypeEnum.PETERSON_COIL.getDesc());

        device.setPatternId(simpleRule("^#\\d+消弧线圈$", ""));

        deviceMapper.insert(device);

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0, new String[]{"消弧线圈"}, 2);
        saveDataToDB(device, new String[]{"××消弧线圈"}, result);
    }

    /**
     * 小电阻 规则导入
     */
    @Test
    public void resistorInput() {
        clearInput(null, DeviceTypeEnum.SMALL_RESISTOR);
        Device device = new Device();
        device.setId(deviceMapper.nextSeqId() + "");
        device.setName("××小电阻");
        device.setVoltage("10kV");
        device.setType(DeviceTypeEnum.SMALL_RESISTOR.getDesc());

        device.setPatternId(simpleRule("^#\\d+小电阻$", ""));

        deviceMapper.insert(device);

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0, new String[]{"小电阻"}, 2);
        saveDataToDB(device, new String[]{"××小电阻"}, result);
    }

    /**
     * 站用变 规则导入
     */
    @Test
    public void stationTransformerRuleInput() {
        String[] voltages = new String[]{"110kV", "35kV", "10kV"};
        clearInput(voltages, DeviceTypeEnum.STATION_TRANSFORMER);
        for (String voltage : voltages) {
            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage + "站用变");
            device.setVoltage(voltage);
            device.setType(DeviceTypeEnum.STATION_TRANSFORMER.getDesc());

            device.setPatternId(simpleRule("^(?:#\\d*|)站用变$", ""));

            deviceMapper.insert(device);

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0, new String[]{voltage + "站用变"}, 2);
            saveDataToDB(device, new String[]{voltage+"站用变", voltage+"#×站用变", "#×站用变"}, result);
        }


    }

    /**
     * 站用电 规则导入
     */
    @Test
    public void stationPowerRuleInput() {
        clearInput(null, DeviceTypeEnum.STATION_POWER);
        String[] voltages = new String[]{"380V"};
        for (String voltage : voltages) {
            Device device = new Device();
            device.setId(deviceMapper.nextSeqId() + "");
            device.setName(voltage + "站用电");
            device.setVoltage(null);
            device.setType(DeviceTypeEnum.STATION_POWER.getDesc());

            device.setPatternId(simpleRule("^站用电$", ""));

            deviceMapper.insert(device);

            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{voltage+"站用电"}, 2);
            saveDataToDB(device, new String[]{ voltage+"#×站用电", voltage }, result);
        }
    }

    @Test
    public void publicRuleInput() {
        Map<String, RegexPattern> devicePatternMap = new HashMap<>();
        devicePatternMap.put("SH直流系统", new RegexPattern(null,"^(#\\d+)?直流系统$", DeviceTypeEnum.DC_SYSTEM, "直流系统"));
        devicePatternMap.put("稳控", new RegexPattern(null,"^稳控装置$", DeviceTypeEnum.STABILITY_CONTROL, "稳控装置"));
        devicePatternMap.put("备自投装置", new RegexPattern(null,"^备自投装置$", DeviceTypeEnum.BACKUP_AUTOMATIC_SWITCH, "备自投装置"));
        devicePatternMap.put("失步解列", new RegexPattern(null,"^.*失步解列$", DeviceTypeEnum.OUT_OF_STEP_SEPARATION, "××失步解列"));
        devicePatternMap.put("频率电压紧急控制", new RegexPattern(null,"^.*频率电压紧急控制装置$", DeviceTypeEnum.FREQUENCY_VOLTAGE_EMERGENCY_CONTROL, "××频率电压紧急控制装置"));
        devicePatternMap.put("VQC", new RegexPattern(null,"^VQC$", DeviceTypeEnum.VQC, "VQC"));
        devicePatternMap.put("通信电源系统", new RegexPattern(null,"^通信电源$", DeviceTypeEnum.COMMUNICATION_POWER_CONTROL, "通信电源"));
        devicePatternMap.put("小电流接地选线装置", new RegexPattern(null,"^\\d+小电流接地选线装置$", DeviceTypeEnum.SMALL_CURRENT, "××小电流接地选线装置"));
        // 全部匹配
        devicePatternMap.put("公用", null);
//        devicePatternMap.put("备自投装置", null);

        // 清除 公用数据
        List<AlarmTypeRuleModel> deviceSCADAInfoTypeRuleModels = deviceSCADAInfoTypeMapper.selectListAndInfoRuleWhenDeviceEmpty();
        String patterIds = deviceSCADAInfoTypeRuleModels.stream().map(e -> e.getRulePatternId() + (StringUtils.isNotBlank(e.getPatternId()) && StringUtils.isNotBlank(e.getRulePatternId()) ? e.getPatternId() : "") + e.getPatternId()).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(patterIds)) {
            deviceSCADARegexMapper.deleteIds(Arrays.asList(patterIds.split(",")));
        }
        List<String> typeIds = deviceSCADAInfoTypeRuleModels.stream().map(AlarmType::getId).collect(Collectors.toList());
        if (!typeIds.isEmpty()) {
            deviceSCADAInfoTypeMapper.deleteIds(typeIds);
        }
        List<String> ruleIds = deviceSCADAInfoTypeRuleModels.stream().map(AlarmTypeRuleModel::getRuleId).filter(Objects::nonNull).collect(Collectors.toList());
        if (!ruleIds.isEmpty()) {
            deviceSCADAInfoRuleMapper.deleteIds(ruleIds);
        }
        String[] sheetNames = {"公用", "直流系统", "稳控", "备自投装置", "失步解列", "频率电压紧急控制", "VQC", "通信电源控制", "小电流"};
        multiDeviceRuleInput(ExcelUtils.readExcelToObj(this.inputFilePath, 35, 43,sheetNames, 2), devicePatternMap);
    }

    // 多个设备 一起导入
    private void multiDeviceRuleInput(List<Map<String,String>> result,  Map<String, RegexPattern> devicePatternMap) {
        Map<String, List<Map<String, String>>> ruleInSameDevice = result.stream().collect(Collectors.groupingBy(e -> e.get("device")));

//        TransactionStatus transaction = dataSourceTransactionManager.getTransaction(transactionDefinition);
//        transaction.setRollbackOnly();
        for (Map.Entry<String, List<Map<String, String>>> entry : ruleInSameDevice.entrySet()) {
            String deviceName = entry.getKey();
            List<Map<String, String>> list = entry.getValue();

            if (!devicePatternMap.containsKey(deviceName)) {
                throw new RuntimeException(deviceName + "sheet没有对应的设备正则");
            }

            RegexPattern deviceRegexPattern = devicePatternMap.get(deviceName);

            Device device = new Device();
            String[] replace = new String[0];
            if (deviceRegexPattern != null) {
                // 清除旧数据
                clearInput(new String[]{deviceRegexPattern.getVoltage()}, deviceRegexPattern.getDeviceTypeEnum());
                // FIXME
                Device existedDevice = deviceMapper.selectOne(Wrappers.<Device>lambdaQuery().eq(Device::getVoltage, deviceRegexPattern.getVoltage()).eq(Device::getType, deviceRegexPattern.getDeviceTypeEnum().getDesc()));
                if (existedDevice != null) {
                    throw new RuntimeException(deviceRegexPattern.getVoltage() + deviceName + "设备已存在");
                }

                device.setVoltage(deviceRegexPattern.getVoltage());
                device.setName(deviceName);
                device.setType(deviceRegexPattern.getDeviceTypeEnum().getDesc());
                if (StringUtils.isNotBlank(deviceRegexPattern.getPattern())) {
                    device.setPatternId(simpleRule(deviceRegexPattern.getPattern(), null));
                }
                deviceMapper.insert(device);
                replace = Optional.ofNullable(deviceRegexPattern.getReplaceFirst()).orElse(Strings.EMPTY).split(",");
            }

            saveDataToDB(device, replace, list);
        }
//        dataSourceTransactionManager.commit(transaction);
    }

    @Test
    public void ruleInput() {
        inputFilePath = "C:\\Users\\15259\\Desktop\\项目\\2025-08-01 远动信息表校核\\规则检测\\v2.0-2026-03-25\\规范信息导入表格.xlsx";
        publicRuleInput();
        switchRuleInput();
        lineRuleInput();
        lineHighResistanceRuleInput();
        masterTransformerRuleInput();
        capacitanceAndReactanceRuleInput();
        zigzagTransformAndGroundTransformerRuleInput();
        stationTransformerRuleInput();
        petersonCoilRuleInput();
        resistorInput();
        stationPowerRuleInput();
        generatrixRuleInput();
    }

    // 开关 测试数据
    @Test
    public void switchTestDataBuild () {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0, new String[]{"500kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("500kV××", "500kV" + (random.nextInt(9000) + 1000)));
        }
//        writeToExcel(testData);
        result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"220kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("220kV××", "220kV" + (random.nextInt(9000) + 1000)));
        }
//        writeToExcel(testData);
        result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"110kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("110kV××", "110kV" + (random.nextInt(9000) + 1000)));
        }
//        writeToExcel(testData);
        result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"35kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("35kV××", "35kV" + (random.nextInt(9000) + 1000)));
        }
//        writeToExcel(testData);
        result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"20kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("20kV××", "20kV" + (random.nextInt(9000) + 1000)));
        }
        result = ExcelUtils.readExcelToObj(this.inputFilePath, 0, 0,new String[]{"10kV开关"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("10kV××", "10kV" + (random.nextInt(9000) + 1000)));
        }
        writeToExcel(testData);
    }

    // 线 测试数据
    @Test
    public void lineTestDataBuild () {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"500kV线路"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("500kV××", "500kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"220kV线路"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("220kV××", "220kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"110kV线路"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("110kV××", "110kV" + (random.nextInt(9000) + 1000)));
        }

        writeToExcel(testData);
    }

    // 主变测试数据
    @Test
    public void masterTransformerRuleTestDataBuild () {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 5, 0,new String[]{"500kV主变"},2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            rule = "500kV" + rule;
            testData.add(rule.replace("#×", "#" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 5, 0,new String[]{"220kV主变"},2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            rule = "220kV" + rule;
            testData.add(rule.replace("#×", "#" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 5, 0,new String[]{"110kV主变"},2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            rule = "110kV" + rule;
            testData.add(rule.replace("#×", "#" + (random.nextInt(9000) + 1000)));
        }

        writeToExcel(testData);
    }

    // 母线测试数据
    @Test
    public void generatrixTestDataBuild () {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"500kV母线"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("500kV×", "500kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"66kV母线"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("66kV×", "66kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"35kV母线"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("35kV×", "35kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"20kV母线"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("20kV×", "20kV" + (random.nextInt(9000) + 1000)));
        }

        result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"10kV母线"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("10kV×", "10kV" + (random.nextInt(9000) + 1000)));
        }

        writeToExcel(testData);
    }

    // 500kV线高抗
    @Test
    public void lineHighResistanceTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();
        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"500kV高抗"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("500kV××线高抗", "500kV" + (random.nextInt(9000) + 1000) + "线高抗"));
        }
        writeToExcel(testData);

    }

    // 电容器组|电抗器组 测试数据
    @Test
    public void capacitanceAndReactanceTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        String[] voltages = {"66kV", "35kV", "20kV", "10kV"};

        for (String voltage : voltages) {
            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{voltage+"电容|电抗"}, 2);
            for (Map<String, String> map : result) {
                String rule = map.get("rule");
                testData.add(rule.replace(voltage + "#×", voltage + "#" + (random.nextInt(9000) + 1000)));
            }
        }

        writeToExcel(testData);
    }

    // 曲折变|接地变 测试数据
    @Test
    public void zigzagAndGroundTransformerTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();

        String[] voltages = {"20kV", "10kV"};

        for (String voltage : voltages) {
            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{voltage+"曲折变|接地变"}, 2);
            for (Map<String, String> map : result) {
                String rule = map.get("rule");
                testData.add(rule.replace(voltage + "#×", voltage + "#" + (random.nextInt(9000) + 1000)));
            }
        }

        writeToExcel(testData);
    }

    // 消弧线圈 测试数据
    @Test
    public void petersonCoilTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();
        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"消弧线圈"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("××消弧线圈", (random.nextInt(9000) + 1000) + "消弧线圈"));
        }
        writeToExcel(testData);
    }

    // 站用电 测试数据
    @Test
    public void stationPowerTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();
        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"380kV站用电"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("380V#×", "380V#" + (random.nextInt(9000) + 1000)));
        }
        writeToExcel(testData);
    }

    // 站用变 测试数据
    @Test
    public void stationTransformerTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();
        String[] voltages = {"110kV", "35kV", "10kV"};

        for (String voltage : voltages) {
            List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{voltage + "站用变"}, 2);
            for (Map<String, String> map : result) {
                String rule = map.get("rule");
                testData.add(rule.replace("#×站用变", voltage + "#" + (random.nextInt(9000) + 1000) + "站用变"));
            }
        }
        writeToExcel(testData);
    }

    // 小电阻 测试数据
    @Test
    public void resisterTestDataBuild() {
        Random random = new Random();
        List<String> testData = new ArrayList<>();
        List<Map<String,String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 11, 0,new String[]{"小电阻"}, 2);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            testData.add(rule.replace("××小电阻", (random.nextInt(9000) + 1000) + "小电阻"));
        }
        writeToExcel(testData);
    }

    @Test
    public void publicTestDataBuild() {
        List<String> testData = new ArrayList<>();
        Random random = new Random();
        List<Map<String, String>> result = ExcelUtils.readExcelToObj(this.inputFilePath, 34, 42, null, 3);
        for (Map<String, String> map : result) {
            String rule = map.get("rule");
            rule = rule.replaceAll("××", random.nextInt(9000) + 1000 + "");
            rule = rule.replaceAll("#×", "#" + (random.nextInt(9000) + 1000));
            rule = rule.replaceAll("×", "" + (random.nextInt(9000) + 1000));
            testData.add(rule);
        }
        writeToExcel(testData);
    }

    private void writeToExcel(List<String> datas) {
        for (String data : datas) {
            System.out.println(data);
        }
    }

    private void saveDataToDB(Device device, String[] prefixs, List<Map<String,String>> result) {
        Map<String, AlarmType> infoTypeMap = new HashMap<>();
        List<AlarmRule> infoRules = new ArrayList<>();
        for(Map<String,String> map:result){
            AlarmType infoType = infoTypeMap.get(map.get("name"));
            if (infoType == null) {
                infoType = new AlarmType();
                if ("软压板".equals(map.get("name"))) {
                    System.out.println();
                }
                infoType.setName(map.get("name"));
                infoType.setComment(map.get("comment"));
                String priority = map.get("priority");
                if (StringUtils.isNotBlank(priority)) {
                    infoType.setPriority(Integer.valueOf(priority.substring(0, !priority.contains(".") ? priority.length() : priority.indexOf("."))));
                }
                infoType.setUpload("√".equals(map.get("upload")));
                infoType.setMasterMerge("是".equals(map.get("masterMerge")));
                infoType.setDeviceId(device.getId());
                String id = deviceMapper.nextSeqId() + "";
                infoType.setId(id);
                infoTypeMap.put(map.get("name"), infoType);
            }

            if ("1".equals(map.get("includeDeviceName"))) {
                infoType.setIncludeDeviceName(true);
            }

            if ("1".equals(map.get("typeFullTextMatch"))) {
                infoType.setFullTextMatch(true);
            }

            // 配置信息类型规则
            if (StringUtils.isNotBlank(map.get("typePattern"))) {// 存在 类型匹配时，跳过 规范信息描述
                infoType.setPatternId(createRegex(map.get("typePattern"), map.get("typePatternMessage")));
            } else {
                infoRules.add(createRule(infoType.getId(), map.get("rule"), prefixs, map.get("rulePattern"), map.get("rulePatternMessage"), map.get("includeDeviceName"), map.get("ruleFullTextMatch")));
            }
        }

        // 保存进数据库
        deviceSCADAInfoTypeService.saveBatch(infoTypeMap.values(), infoTypeMap.values().size());
        deviceSCADAInfoRuleService.saveBatch(infoRules, infoRules.size());
    }

    private String simpleRule(String pattern, String message) {
        //FIXME
        List<AlarmRegex> regexs = deviceSCADARegexMapper.selectList(Wrappers.<AlarmRegex>lambdaQuery().eq(AlarmRegex::getPattern, pattern));
        AlarmRegex deviceSCADARegex;
        if (regexs.isEmpty()) {
            deviceSCADARegex = new AlarmRegex();
            deviceSCADARegex.setPattern(pattern);
            deviceSCADARegex.setNotMatchMessage(message);
            deviceSCADARegexMapper.insert(deviceSCADARegex);
        } else {
            deviceSCADARegex = regexs.get(0);
        }

        return deviceSCADARegex.getId();
    }

    private AlarmRule createRule(String deviceSCADAInfoTypeId, String name, String[] prefixs, String rulePattern, String rulePatternMessage, String includeDeviceName, String ruleFullTextMatch) {
        AlarmRule rule = new AlarmRule();
        rule.setDeviceScadaInfoTypeId(deviceSCADAInfoTypeId);
        rule.setId(deviceMapper.nextSeqId() + "");
        rule.setName(name);
        String ruleStr = name;
        for (String prefix : prefixs) {
            ruleStr = ruleStr.replaceAll(prefix, "");
        }
        rule.setRule(ruleStr);
        if (StringUtils.isNotBlank(rulePattern)) {
            String patternId = createRegex(rulePattern, rulePatternMessage);
            rule.setPatternId(patternId);
        }
        if ("1".equals(includeDeviceName)) {
            rule.setIncludeDeviceName(true);
        }
        if ("1".equals(ruleFullTextMatch)) {
            rule.setFullTextMatch(true);
        }
        return rule;
    }

    // 创建 正则
    private String createRegex(String pattern, String message) {

        RegexTransfer regexTransfer = new RegexTransfer(pattern, message);

        String res = regexTransferMap.get(regexTransfer);
        if (StringUtils.isNotBlank(res)) {
            return res;
        }

        String[] patterns = pattern.split("\n");
        message = message == null ? "" : message;
        String[] messages = message.split(",");

        List<AlarmRegex> regexs = new ArrayList<>();
        for (int i = 0; i < patterns.length; i++) {
            AlarmRegex deviceSCADARegex = new AlarmRegex();
            deviceSCADARegex.setPattern(patterns[i]);
            deviceSCADARegex.setNotMatchMessage(messages[i]);
            regexs.add(deviceSCADARegex);
        }

        try {
            deviceSCADARegexMapper.insertBatchSomeColumn(regexs);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(Arrays.toString(regexs.toArray(new AlarmRegex[0])));
        }

        res = regexs.stream().map(AlarmRegex::getId).collect(Collectors.joining(","));
        this.regexTransferMap.put(regexTransfer, res);

        return res;
    }

    /**
     * 清除导入数据
     * @param voltages 电压
     * @param deviceType 设备类型
     */
    private void clearInput(String[] voltages, DeviceTypeEnum  deviceType) {
        LambdaQueryWrapper<Device> deviceQueryWrapper = new LambdaQueryWrapper<>();
        if (ArrayUtils.isNotEmpty(voltages)) {
            deviceQueryWrapper.in(Device::getVoltage, Arrays.asList(voltages));
        } else {
            deviceQueryWrapper.isNull(Device::getVoltage);
        }
        deviceQueryWrapper.eq(Device::getType, deviceType.getDesc());
        // FIXME
        List<Device> devices = deviceMapper.selectList(deviceQueryWrapper);

        if (devices.isEmpty()) {
            return;
        }

        for (Device device : devices) {
            // 删除 正则
            List<AlarmRegex> deviceSCADARegexes = deviceSCADARegexMapper.selectByDeviceId(device.getId());
            if (CollectionUtils.isNotEmpty(deviceSCADARegexes)) {
                deviceSCADARegexMapper.deleteIds(deviceSCADARegexes.stream().map(AlarmRegex::getId).collect(Collectors.toList()));
            }

            // 删除 规范信息
            List<AlarmRule> deviceSCADAInfoRules = deviceSCADAInfoRuleMapper.selectByDevice(device.getId());
            if (CollectionUtils.isNotEmpty(deviceSCADAInfoRules)) {
                deviceSCADAInfoRuleMapper.deleteIds(deviceSCADAInfoRules.stream().map(AlarmRule::getId).collect(Collectors.toList()));
            }

            // 删除 信息类型
            List<AlarmType> infoTypes = deviceSCADAInfoTypeMapper.selectByDevice(device.getId());
            if (CollectionUtils.isNotEmpty(infoTypes)) {
                deviceSCADAInfoTypeMapper.deleteIds(infoTypes.stream().map(AlarmType::getId).collect(Collectors.toList()));
            }
            // 删除设备
            deviceMapper.deleteById(device.getId());
        }
    }

    @Getter
    @Setter
    public static class RegexTransfer {

        private String pattern;

        private String message;

        public RegexTransfer(String pattern, String message) {
            this.pattern = pattern;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegexTransfer that = (RegexTransfer) o;
            return Objects.equals(pattern, that.pattern) && Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pattern, message);
        }
    }

    @Getter
    public static class RegexPattern{

        private final String voltage;

        private final String pattern;

        private final DeviceTypeEnum deviceTypeEnum;

        private final String replaceFirst;

        public RegexPattern(String voltage, String pattern, DeviceTypeEnum deviceTypeEnum, String replaceFirst) {
            this.voltage = voltage;
            this.pattern = pattern;
            this.deviceTypeEnum = deviceTypeEnum;
            this.replaceFirst = replaceFirst;
        }

    }

    @Getter
    @Setter
    public static class TestInfo {

        @ExcelProperty(value = "规范信息描述举例", index = 7)
        private String info;

        public TestInfo(String info) {
            this.info = info;
        }

    }

    /**
     * 截取规则文件示例，生成测试文件
     */
    @Test
    public void truncateTemplateAsTestInfo() {
        Path inputPath = Paths.get("C:\\Users\\15259\\Desktop\\南方电网典型遥信规范表12.15.xls");
        Path outPath = Paths.get( "C:\\Users\\15259\\Desktop\\test.xls");
        try (
                HSSFWorkbook workbook = new HSSFWorkbook(Files.newInputStream(inputPath));
                HSSFWorkbook targetWorkbook = new HSSFWorkbook()
        ){
            int targetRowNum = 1;
            HSSFSheet targetSheet = targetWorkbook.createSheet();
            HSSFRow firstRow = targetSheet.createRow(0);
            firstRow.createCell(0).setCellValue("attr_oid");
            firstRow.createCell(1).setCellValue("位置");
            firstRow.createCell(2).setCellValue("告警信号名称");


            for (int i = 2; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.isSheetHidden(i)) {
                    continue; // 忽略隐藏的sheet
                }
                HSSFSheet sheet = workbook.getSheetAt(i);
                HSSFRow secondRow = sheet.getRow(2);
                int infoIndex = 0;
                for (short k = 0; k < secondRow.getLastCellNum(); k++) {
                    HSSFCell cell = secondRow.getCell(k);
                    String cellValue = "";
                    try {
                        cellValue = cell.getStringCellValue();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    if ("规范信息描述举例".equals(cellValue)) {
                        infoIndex = k;
                        break;
                    }
                }

                if (infoIndex == 0) {
                    throw new RuntimeException(String.format("获取%s所在列异常", "规范信息描述举例"));
                }

                for (int j = 3; j < sheet.getLastRowNum(); j++) {
                    HSSFRow row = sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    HSSFCell cell = row.getCell(infoIndex);
                    if (cell == null) {
                        continue;
                    }
                    String s = cell.getStringCellValue();
                    if (StringUtils.isBlank(s)) {
                        continue;
                    }
                    String[] mes = s.split("[、\\n]");
                    for (String m : mes) {
                        if (StringUtils.isBlank(m)) {
                            continue;
                        }

                        HSSFRow targetRow = Optional.ofNullable(targetSheet.getRow(targetRowNum)).orElse(targetSheet.createRow(targetRowNum));
                        targetRowNum++;
                        Optional.ofNullable(targetRow.getCell(2)).orElse(targetRow.createCell(2)).setCellValue(m.trim());
                    }
                }
            }

            targetWorkbook.write(Files.newOutputStream(outPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
