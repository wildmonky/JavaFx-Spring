package com.zhuofan.validator.javafx.controller.workOrder;

import com.zhuofan.validator.JavaFxApplication;
import com.zhuofan.validator.spring.service.WorkOrderService;
import com.zhuofan.validator.javafx.utils.JavaFxLoadUtils;
import com.zhuofan.validator.javafx.utils.JavaFxServiceUtils;
import com.zhuofan.validator.javafx.utils.JavaFxUiUtils;
import com.zhuofan.validator.spring.model.AutomaticMaintenanceWorkOrderModel;
import jakarta.annotation.Resource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class IndexController extends Control {

    @Resource
    private JavaFxServiceUtils serviceUtils;

    @Resource
    private WorkOrderService workOrderService;

    public Map<String, List<String>> crewMemberMap = new HashMap<>();
    public Map<String, List<String>> crewSubstationMap = new HashMap<>();
    public Map<String, WorkContent> workContentTypeMap = new HashMap<>();

    public StackPane container;
    /**
     * 班组
     */
    public ComboBox<String> crewParam;
    private ObservableList<String> crewData;
    /**
     * 工作内容类型
     */
    public ComboBox<String> contentParam;
    /**
     * 厂家名称
     */
    public ComboBox<String> factoryParam;
    public TextField factoryMember;

    private MaintenanceWorkOrderController controller;

    @FXML
    public void initialize()  {
        FXMLLoader loader = JavaFxLoadUtils.loadFXML(JavaFxApplication.class.getResource("/view/workOrder/automaticEquipmentMaintenance.fxml"));
        Node root = loader.getRoot();
        controller = loader.getController();
        container.getChildren().add(root);

        crewMemberMap.put("继保自动化一班", Arrays.asList("徐锡斌", "卢沛坚", "周妙秀", "梁广贤", "冯超", "成煌", "陈华森", "伍子东", "杨清梅", "连文超", "王一凡"));
        crewMemberMap.put("继保自动化二班", Arrays.asList("刘凯", "陈瀚昌", "黄沛亮", "李欧", "古世平", "聂吉强", "孙高星", "吴凯", "刘嘉镇", "林晓婉", "刘智"));
        crewMemberMap.put("继保自动化三班", Arrays.asList("李锦图", "梁欢利", "陈转银", "刘沣", "梁广荣", "沈亮", "黄小宝", "田新华", "刘易锟"));
        crewMemberMap.put("继保自动化四班", Arrays.asList("诸源胜", "袁俊权", "王康仲", "谢思洋", "周伟彬", "邱育义", "梁尔东", "王雨佳", "胡美玉"));
        crewData = FXCollections.observableArrayList(crewMemberMap.keySet());
        crewParam.setItems(crewData);

        crewSubstationMap.put("继保自动化一班", Arrays.asList("500kV东莞站", "220kV裕元站", "220kV冠和站", "220kV赵林站", "220kV古坑站", "110kV角社站", "110kV竹岭站", "110kV黄江站", "110kV田心站", "110kV社贝站", "110kV鹿窝站", "110kV谢岗站", "110kV大龙站", "110kV金洋站", "110kV黎村站", "110kV樟木头站", "110kV鸡啼岗站", "110kV稔子园站"));
        crewSubstationMap.put("继保自动化二班", Arrays.asList("500kV横沥站", "220kV七星站", "220kV下沙站", "220kV步田站", "110kV企石站", "110kV中坑站", "110kV上江站", "110kV冬湖站", "110kV城西站", "110kV河畔站", "110kV隔坑站", "110kV独洲站", "110kV石排站"));
        crewSubstationMap.put("继保自动化三班", Arrays.asList("220kV白玉站", "220kV葵湖站", "220kV玉泉站", "110kV黄洞站", "110kV大窝站", "110kV狮岭站", "110kV雁田站", "110kV土桥站", "110kV铁松站", "110kV清溪站", "110kV德岭站", "110kV三中站", "110kV长龙站", "110kV凤岗站", "110kV银山站", "110kV金龙站", "110kV油甘埔站", "110kV天堂围站"));
        crewSubstationMap.put("继保自动化四班", Arrays.asList("220kV莆心站", "220kV元江站", "220kV道岭站", "220kV角布站", "110kV常平站", "110kV合浦站", "110kV长江站", "110kV桥梓站", "110kV司马站", "110kV塘厦站", "110kV宝石站", "110kV大坪站", "110kV林村站", "110kV新厅站", "110kV湖柏站", "110kV泰安站", "110kV莲湖站", "110kV溪头站", "110kV凤凰站", "110kV智造站", "110kV果合岭站", "110kV白石岗站"));

        crewParam.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (Objects.equals(oldVal, newVal)) {
                return;
            }

            controller.orderRequester.setItems(FXCollections.observableArrayList(crewMemberMap.getOrDefault(newVal, Collections.emptyList())));
            controller.workLocation.setItems(FXCollections.observableArrayList(crewSubstationMap.getOrDefault(newVal, Collections.emptyList())));
        });
        crewParam.getSelectionModel().select(0);

        factoryParam.setItems(FXCollections.observableArrayList("无", "南瑞继保", "南瑞科技", "长园深瑞", "北京四方"));

        initWorkContentMap();
    }

    public void initWorkContentMap() {
        List<WorkContent> contents = new ArrayList<>();
        contents.add(WorkContent.of("空", "", content -> ""));
        contents.add(WorkContent.of("保护测控定检（500kV主变）", "#X主变保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "#X主变测控定检时站端加量测试设备，或改造后加量测试设备或联调，会导致主变PQ数值与实际不符，导致地区负荷总加跳变，状态估计不准确，报出母线有功不平衡告警。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施：#X主变PQ封锁为0，XXXX开关（中开关）、XXXX开关（边开关）、220X开关、30X开关封锁分。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("#(\\d+)主变");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String device = matcher.group(1);
                return r.replaceAll("#X主变", "#" + device + "主变");
            }

            return r;
        }));
        contents.add(WorkContent.of("保护测控定检（220kV主变）", "#X主变保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "#X主变测控定检时站端加量测试设备，或改造后加量测试设备或联调，会导致主变PQ数值与实际不符，导致地区负荷总加跳变，状态估计不准确，报出母线有功不平衡告警。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施：#X主变PQ封锁为0，变高开关220封锁分。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("#(\\d+)主变");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String device = matcher.group(1);
                return r.replaceAll("#X主变", "#" + device + "主变");
            }

            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（500kV线路（西关口））", "500kVXXXX线、XXXX开关（中开关）、XXXX开关（边开关）保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "1.线路测控定检时站端加量测试设备，或改造后加量测试设备或联调，会导致线路遥测数值与实际不符；\n" +
                    "2.500kVXXXX线线路间隔为500kV西电关口线路，将导致西电关口有功跳变，进而影响中调AGC计算和现货出清，状态估计异常，报出线路及母线有功不平衡告警、有功跳变告警。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施：500kVXXXX线PQI封锁0，500kVXXXX线XXXX开关、500kV第X串联络开关XXXX开关封锁分；\n" +
                    "3.总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("500kV(.+)线、(.+)开关（中开关）、(.+)开关（边开关）");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String lineName = "500kV" + matcher.group(1) + "线";
                String s = r.replaceAll("500kVXXXX线", lineName);
                s = s.replaceAll(lineName + "XXXX开关", lineName + matcher.group(3) + "开关");

                String device = matcher.group(1);
                String num = "X";
                if (device != null && device.length() >= 3) {
                   device = device.substring(2, 3);
                }

                s = s.replaceAll("500kV第X串联络开关XXXX开关", "500kV第" + num + "串联络开关" + device + "开关");
                return s;
            }
            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（500kV线路（非西关口））", "500kVXXXX线、XXXX开关（中开关）、XXXX开关（边开关）保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "1.线路测控定检时站端加量测试设备，或改造后加量测试设备或联调，会导致线路遥测数值与实际不符；\n" +
                    "2.500kVXXXX线路间隔非西电关口交流线路，将影响状态估计，报出线路和母线有功不平衡告警。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施：500kVXXXX线PQI封锁0，500kVXXXX线XXXX开关、500kV第X串联络开关XXXX开关封锁分；\n" +
                    "3.总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("500kV(.+)线、(.+)开关（中开关）、(.+)开关（边开关）");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String lineName = "500kV" + matcher.group(1) + "线";
                String s = r.replaceAll("500kVXXXX线", lineName);
                s = s.replaceAll(lineName + "XXXX开关", lineName + matcher.group(3) + "开关");

                String device = matcher.group(1);
                String num = "X";
                if (device != null && device.length() >= 3) {
                    device = device.substring(2, 3);
                }

                s = s.replaceAll("500kV第X串联络开关XXXX开关", "500kV第" + num + "串联络开关" + device + "开关");
                return s;
            }
            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（500kV联络开关）", "500kV XXXX联络开关保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "站端作传动试验，会导致开关监测变位，开关变位信号上调度告警窗，影响调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施:500kV第X串联络XXXX开关封锁分。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("500kV ?(\\d+)联络开关");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String device = matcher.group(1);
                String num = "X";
                if (device != null && device.length() > 3) {
                    num = device.substring(2, 3);
                    return r.replaceAll("500kV第X串联络XXXX开关", "500kV第" + num + "串联络" + device + "开关");
                }
            }

            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（220kV线路）", "220kVXX线路保护测控定检", content -> {
            String r = "风险评估：\n" +
                    "线路测控定检时站端加量测试设备，或改造后加量测试设备或联调，会导致线路遥测数值与实际不符；\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施:XX线线路PQI封锁0，XX开关封锁分。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("220kV(.+)线路");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String lineName = matcher.group(1);
                if (StringUtils.isNotBlank(lineName)) {
                    return r.replace("XX线线路", lineName + "线路");
                }
            }

            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（220kV母联分段）", "220kV X-XM母联XXXX开关保护测控装置定检", content -> {
            String r = "风险评估：\n" +
                    "1.站端作传动试验，会导致开关监测变位，开关变位信号上调度告警窗，影响调度监盘；\n" +
                    "2.站端加量测试设备，会导致线路遥测数值与实际不符。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施：220kV X-XM母联XXXX开关封锁分，电流封锁为0。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("(.+)[-—]+(.+)母联(.+)开关");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String s1 = matcher.group(1);
                s1 = StringUtils.isNotBlank(s1) ? s1 : "X";
                String s2 = matcher.group(2);
                s2 = StringUtils.isNotBlank(s2) ? s2 : "X";
                String s3 = matcher.group(3);
                s3 = StringUtils.isNotBlank(s3) ? s3 : "XXXX";

                return r.replace("X-XM母联XXXX开关", s1 + "-" + s2 + "M母联" + s3 + "开关");
            }

            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（220kV母线PT）", "220kV XM母线测控装置定检", content -> {
            String r = "风险评估：\n" +
                    "在220kV #XM母线PT定检期间，站端加量测试设备，会导致母线电压跳变，影响中调、地调监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施：母线XXX地刀遥信状态封锁为当前状态，220kV XM母线电压封锁为当前值。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("([^ ]+?)母线");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String deviceName = matcher.group(1);
                if (StringUtils.isNotBlank(deviceName)) {
                    return r.replaceAll("XM", deviceName);
                }
            }

            return r;
        }));

        contents.add(WorkContent.of("保护测控定检（35kV电容器/电抗器组）", "35kV #X电容器组XXX开关保护测控装置定检", content -> {
            String r = "风险评估：\n" +
                    "在35kV #X电容器组XXX开关测控装置定检期间，站端加量测试设备，会影响状态估计；\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施：35kV #X电容器组XXX开关无功功率封锁为0，XXX开关封锁为分。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("(#.+?电容器组)(.+?开关)");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String d1 = matcher.group(1);
                String d2 = matcher.group(2);

                if (StringUtils.isNotBlank(d1)) {
                    r = r.replace("#X电容器组", d1);
                }

                if (StringUtils.isNotBlank(d2)) {
                    r = r.replace("XXX开关", d2);
                }

                return r;
            }

            return r;
        }));

        contents.add(WorkContent.of("远动重启（500kV站）", "按实际填写：如地调远动点表信号整改、XX保护装置程序版本升级、四遥验收等", content -> "风险评估：\n" +
                "1.工作过程中需要重启远动装置，影响调度监盘；\n" +
                "2.当工作不规范或远动双机存在异常时，远动主备机切换或双主机通道切换时可能发生数据跳变。\n" +
                "预控措施：\n" +
                "1.工作前做好安全技术交底，做好工作监护；\n" +
                "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好远动装置重启安全措施：500kV线路有功P取人工对端代，地调挂全站停电检修牌，站端做好作业过程中的远动配置修改项、数据及通道状态的检查确认，避免因不规范作业造成远动上送主站数据异常，得到许可后才能开始工作；\n" +
                "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                "4.与总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。"));

        contents.add(WorkContent.of("远动重启（220kV站）", "按实际填写：如地调远动点表信号整改、XX保护装置程序版本升级、四遥验收等", content -> "风险评估：\n" +
                "1.工作过程中需要重启远动装置，影响调度监盘；\n" +
                "2.当工作不规范或远动双机存在异常时，远动主备机切换或双主机通道切换时可能发生数据跳变。\n" +
                "预控措施：\n" +
                "1.工作前做好安全技术交底，做好工作监护；\n" +
                "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好远动装置重启安全措施：中调不做相关封锁，地调挂全站停电检修牌，站端做好作业过程中的远动配置修改项、数据及通道状态的检查确认，避免因不规范作业造成远动上送主站数据异常，得到许可后才能开始工作；\n" +
                "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                "4.中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。"));

        contents.add(WorkContent.of("综改（500kV站）", "500kVXX间隔测控装置、500kVXX开关测控装置改造调试及验收", content -> {
            String r = "风险评估：\n" +
                    "在开展500kVXX间隔改造调试及验收工作过程中：\n" +
                    "1.需重启远动机，期间总调、中调、地调调度数据网及专线通道将短时中断；\n" +
                    "2.站端作传动试验或改造后联调，会导致开关监测变位，开关变位信号上调度告警窗，影响总调、中调、地调调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号及数据屏蔽措施：500kVXX间隔PQI封锁为0，50XX边开关及50XX中开关封锁为分位。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作;\n" +
                    "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                    "4.与总调、中调、地调调度自动化人员核对业务通道正确及数据无误后方可结束工作。";

            Pattern pattern = Pattern.compile("500kV(.+?)间隔");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String s = matcher.group(1);
                return r.replaceAll("500kVXX间隔", s);
            }

            return r;
        }));

        contents.add(WorkContent.of("主变综改（220kV站）", "#X主变保护测控装置改造调试及验收", content -> {
            String r = "风险评估：\n" +
                    "在开展#X主变保护测控装置改造调试及验收工作过程中：\n" +
                    "1.需重启远动机，期间中调、地调调度数据网及专线通道将短时中断；\n" +
                    "2.站端作传动试验或改造后联调，会导致开关监测变位，开关变位信号上调度告警窗，影响中调、地调调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号及数据屏蔽措施：#X主变PQ封锁0，变高220X开关封锁分；\n" +
                    "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                    "4.与中调、地调调度自动化人员核对业务通道正确及数据无误后方可结束工作。";

            Pattern pattern = Pattern.compile("#(\\d+)主变");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String num = matcher.group(1);
                r = r.replace("#X主变", "#" + num + "主变");
                r = r.replace("220X开关", "220" + num + "开关");
                return r;
            }

            return r;
        }));

        contents.add(WorkContent.of("线路综改（220kV站）", "220kVXX线保护测控装置改造调试及验收", content -> {
            String r = "风险评估：\n" +
                    "在开展220kVXX线保护测控装置改造调试及验收工作过程中：\n" +
                    "1.需重启远动机，期间中调、地调调度数据网及专线通道将短时中断；\n" +
                    "2.站端作传动试验或改造后联调，会导致开关监测变位，开关变位信号上调度告警窗，影响中调、地调调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，确保工作得到中调、地调调度自动化人员允许并做好相关信号及数据屏蔽措施：220kVXX线XXXX开关封锁分，PQI封锁0；\n" +
                    "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                    "4.与中调、地调调度自动化人员核对业务通道正确及数据无误后方可结束工作。";

            Pattern pattern = Pattern.compile("220kV(.+?)线");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String lineName = matcher.group(1);
                if (StringUtils.isNotBlank(lineName)) {
                    return r.replace("220kVXX线", lineName);
                }
            }

            return r;
        }));

        contents.add(WorkContent.of("配合其他专业", "配合检修专业开展XX工作", content ->
            "风险评估：\n" +
                    "检修过程中可能断开遥信电源，并分合XX开关及XX刀闸试验，会导致开关或刀闸监测变位，开关或刀闸变位信号上调度告警窗，影响调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，申请总调、中调、地调将XX开关及XX刀闸封锁分。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.与总调、中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。"
        ));

        contents.add(WorkContent.of("特维传动", "500kVXX线XXXX、XXXX开关 I级特维传动试验", content -> {
            String r = "风险评估：\n" +
                    "在开展XXXX、XXXX开关传动试验工作过程中，会导致开关监测变位，开关变位信号上调度告警窗，影响调度监盘。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，申请总调、中调、地调将XXXX、XXXX开关封锁分。确保工作得到总调、中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.与总调、中调、地调调度自动化人员核对业务通道正确及数据无误后方可结束工作。";

            Pattern pattern = Pattern.compile("线(.+?、?)开关");
            Matcher matcher = pattern.matcher(r);
            if (matcher.find()) {
                String d = matcher.group(1);
                if (StringUtils.isNotBlank(d)) {
                    return r.replace("XXXX、XXXX开关", d + "开关");
                }
            }

            return r;
        }));

        contents.add(WorkContent.of("WPMU工作", "WPMU装置功能升级/WPMU装置回路检查/WPMU通道采样值测试", content -> "风险评估：\n" +
                "工作过程中总调、中调WPMU主站会收到异常数据。\n" +
                "预控措施：\n" +
                "1.工作前做好安全技术交底，做好工作监护；\n" +
                "2.工作前后做好工作联系和工作申请，确保工作得到总调、中调调度自动化人员允许并做好安全措施：封锁全站WPMU数据，得到许可后才能开始工作；\n" +
                "3.在修改配置工作前，做好相关数据及配置的备份；\n" +
                "4.与总调、中调自动化值班员进行数据核对，确认无误后方可解封，结束工作。"));

        contents.add(WorkContent.of("刀闸双确认回路接入", "XXXXX刀闸双确认回路接入", content -> {
            String r = "风险评估：\n" +
                    "工作期间可能会导致主站接收到误遥信信号，主站监视刀闸状态与现场刀闸实际位置不一致，导致状态估计计算时拓扑发生偏差，计算结果与实际值偏差加大。\n" +
                    "预控措施：\n" +
                    "1.工作前做好安全技术交底，做好工作监护；\n" +
                    "2.工作前后做好工作联系和工作申请，申请中调、地调将XXXXX刀闸封锁为当前状态。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                    "3.与中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。";

            Pattern pattern = Pattern.compile("(.+)刀闸");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String s = matcher.group(1);
                return r.replace("XXXXX刀闸", s + "刀闸");
            }

            return r;
        }));

        contents.add(WorkContent.of("自动化消缺工作", "按实际填写，如：直流I段接地故障检查处理，需断开110kV母线间隔测控装置电源及遥信电源空开", content -> "风险评估：\n" +
                "消缺过程可能会导致上送自动化数据与实际不符，导致地区负荷总加跳变，影响现货出清，状态估计不准确，报出母线有功不平衡告警，开关或刀闸监测变位，开关或刀闸变位信号上调度告警窗，影响调度监盘等。\n" +
                "预控措施：\n" +
                "1.工作前做好安全技术交底，做好工作监护；\n" +
                "2.工作前后做好工作联系和工作申请，申请中调、地调将XXXX开关，XXXXX、XXXXX刀闸封锁为当前状态（注：按受影响间隔封锁）。确保工作得到中调、地调调度自动化人员允许并做好相关信号屏蔽措施后再开始工作；\n" +
                "3.与中调、地调自动化值班员进行数据核对，确认无误后方可解封，结束工作。"));

        workContentTypeMap = contents.stream().collect(Collectors.toMap(WorkContent::getType, e -> e));
        contentParam.setItems(FXCollections.observableArrayList(contents.stream().map(WorkContent::getType).toList()));
        contentParam.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (Objects.equals(oldVal, newVal)) {
                return;
            }

            WorkContent workContent = workContentTypeMap.get(newVal);
            if (workContent == null) {
                controller.workContent.setText(null);
                return;
            }
            controller.workContent.setText(workContent.getContent());
        });
    }

    public void build(ActionEvent event) {
        String factory = factoryParam.getSelectionModel().getSelectedItem();
        if (StringUtils.isBlank(factory) || Objects.equals(factory, "无")) {
            controller.comment.setText("1.已核实无供应商技术人员；\n" +
                    "2.使用“四专设备”进行维护。");
        } else {
            String factoryMember = this.factoryMember.getText();
            controller.comment.setText("1.已核实" + factory + "公司技术人员" + factoryMember + "在白名单内；\n" +
                    "2.使用“四专设备”进行维护；\n" +
                    "3.进站供应商技术人员手机、手表、移动介质等统一管控，调试过程全程现场监护。");
        }

        String workContentType = contentParam.getSelectionModel().getSelectedItem();
        WorkContent workContent = workContentTypeMap.get(workContentType);
        if (workContent == null) {
            controller.automaticRequirement.setText(null);
            return;
        }

        Function<String, String> func = workContent.getAutomaticRequirementFunc();
        if (func != null) {
            try {
                String workContentStr = controller.workContent.getText();
                String result = func.apply(workContentStr);
                controller.automaticRequirement.setText(result);
            } catch (RuntimeException e) {
                log.error("", e);
            }
        }
    }

    /**
     * 导出
     * @param event
     */
    public void export(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择导出位置");
        FileChooser.ExtensionFilter excelExtensionFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        fileChooser.getExtensionFilters().add(excelExtensionFilter);
        File selectedFile = fileChooser.showSaveDialog(container.getScene().getWindow());

        if (selectedFile == null) {
            JavaFxUiUtils.alertMessage(Alert.AlertType.WARNING, "请选择导出位置", Duration.seconds(5));
            return;
        }

        AutomaticMaintenanceWorkOrderModel data = controller.getData();
        serviceUtils.asyncRun(() -> {
           // 导出
            try {
                workOrderService.export(data, Files.newOutputStream(selectedFile.toPath()));
            } catch (IOException e) {
                log.error("", e);
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, e.getMessage(), Duration.seconds(5));
            }
            return true;
        }, (result, err) -> {
            if (err != null) {
                log.error("", err);
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                return;
            }

            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "导出成功", Duration.seconds(5));
        });
    }

    @Getter
    @Setter
    public static class WorkContent {

        private String type;

        private String content;

        private Function<String, String> automaticRequirementFunc;

        public static WorkContent of(String type, String content, Function<String, String> automaticRequirement) {
            WorkContent workContent = new WorkContent();
            workContent.setType(type);
            workContent.setContent(content);
            workContent.setAutomaticRequirementFunc(automaticRequirement);
            return workContent;
        }

    }

}
