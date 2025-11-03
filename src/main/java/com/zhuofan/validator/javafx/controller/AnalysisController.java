package com.zhuofan.validator.javafx.controller;

import com.zhuofan.validator.javafx.service.AnalysisService;
import com.zhuofan.validator.javafx.service.BaseService;
import com.zhuofan.validator.javafx.service.DownloadService;
import com.zhuofan.validator.javafx.service.ServiceBuilder;
import com.zhuofan.validator.spring.model.AlarmInfo;
import com.zhuofan.validator.spring.model.DeviceSCADAInputInfo;
import com.zhuofan.validator.spring.model.ProgressModel;
import com.zhuofan.validator.spring.service.AlarmService;
import com.zhuofan.validator.spring.utils.SpringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Getter
@Setter
public class AnalysisController {

    private final Logger logger = LoggerFactory.getLogger(AnalysisController.class);

    @FXML
    private Text fileName;
    @FXML
    public Button analysisButton;
    @FXML
    public Button chooseFileButton;
    @FXML
    public Button downloadButton;

    @FXML
    private TableView<AlarmInfo> dataTable;
    @FXML
    public TableColumn<AlarmInfo, String> pointCode;
    @FXML
    private TableColumn<AlarmInfo, String> alarmContent;
    @FXML
    private TableColumn<AlarmInfo, String> needReport;
    @FXML
    private TableColumn<AlarmInfo, String> needMainMerge;
    @FXML
    private TableColumn<AlarmInfo, String> message;

    public ProgressBar progressBar;
    public ProgressIndicator progressIndicator;

    private List<AlarmInfo> alarmInfoList;

    private Path selectedFilePath;

    @FXML
    private void initialize() {
        pointCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPointCode()));
        alarmContent.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAlarmContent()));
        needReport.setCellValueFactory(cellData -> new SimpleStringProperty(BooleanUtils.isTrue(cellData.getValue().getNeedReport()) ? "是" : "否"));
        needMainMerge.setCellValueFactory(cellData -> new SimpleStringProperty(BooleanUtils.isTrue(cellData.getValue().getNeedMainMerge()) ? "是" : "否"));
        message.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
    }

    @FXML
    protected void onOpenFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("文件列表");
        FileChooser.ExtensionFilter excelExtensionFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        fileChooser.getExtensionFilters().add(excelExtensionFilter);
        File selectedFile = fileChooser.showOpenDialog(fileName.getScene().getWindow());
        if (selectedFile != null) {
            int maxLength = 28;
            String name = selectedFile.getName();
            if (name.length() >= maxLength) {
                name = name.substring(0, maxLength - 1) + "...";
            }
            fileName.setText(name);
            this.selectedFilePath = selectedFile.toPath();
        }
    }

    /**
     * 解析按钮事件
     */
    @FXML
    public void analysis() {
        AlarmService alarmService = SpringUtils.getBean("", AlarmService.class);
        AtomicReference<ProgressModel> progressModelAtomicReference = new AtomicReference<>();
        ServiceBuilder.builder(AnalysisService.class).task(() -> {
            Map<AlarmService.SheetInfo, List<DeviceSCADAInputInfo>> resultMap;
            try {
                resultMap = alarmService.check(Files.newInputStream(selectedFilePath), progressModelAtomicReference);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            List<AlarmInfo> list = new ArrayList<>();
            List<DeviceSCADAInputInfo> result = resultMap.values().stream().flatMap(List::stream).toList();
            int i = 1;
            for (DeviceSCADAInputInfo r : result) {
                String mes = "异常";
                List<String> tips = r.getTips();
                if (r.getInfoTypeId() == null && CollectionUtils.isEmpty(tips)) {
                    mes = "";
                }

                if (r.getInfoTypeId() != null && CollectionUtils.isEmpty(tips)) {
                    mes = "符合规范";
                }

                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(tips)) {
                    mes = org.apache.commons.collections4.CollectionUtils.isEmpty(tips) ? "符合规范" : String.join("；", r.getTips().toArray(new String[0]));
                }
                AlarmInfo info = AlarmInfo.builder()
                        .rowNum(i++ + "")
                        .pointCode(r.getPointCode())
                        .alarmContent(r.getWarnInfo())
                        .device(r.getDevice())
                        .needReport(false)
                        .needMainMerge(r.getMasterMerge())
                        .message(mes)
                        .build();
                list.add(info);
            }
            // 进度条走完
            progressModelAtomicReference.updateAndGet(t -> {
                t.setProgress(1d);
                return t;
            });
            return list;
        }).beforeTaskStart(service -> {
            alarmInfoList = Collections.emptyList();
            // 选择文件按钮
            chooseFileButton.setDisable(true);
            // 下载按钮
            downloadButton.setDisable(true);
            // 禁止再次点击解析按钮，防止重复触发
            analysisButton.setDisable(true);
            // 清空表格数据
            dataTable.getItems().clear();
            // 刷新表格样式
            dataTable.refresh();
            // 开启进度条
            AnalysisService analysisService = (AnalysisService) service;
            analysisService.setProgressBar(progressBar);
            analysisService.startProcessBar(() -> {
                ProgressModel progressModel = progressModelAtomicReference.get();
                return progressModel == null ? 0 : progressModel.getProgress();
            },300);
        }).afterTaskSucceed(service -> {
            alarmInfoList = service.getValue();
            // 向表格插入数据
            dataTable.getItems().addAll(alarmInfoList);
            // 刷新表格样式
            dataTable.refresh();
        }).taskFinally(service -> {
            // 选择文件按钮
            chooseFileButton.setDisable(false);
            // 下载按钮
            downloadButton.setDisable(false);
            // 允许解析按钮点击
            analysisButton.setDisable(false);
        }).build().start();
    }

    @FXML
    public void download(ActionEvent event) {

        if (CollectionUtils.isEmpty(alarmInfoList)) {
            logger.info("无待下载数据");
            return;
        }

        Button downloadButton = (Button)event.getSource();
        Stage stage = (Stage)downloadButton.getScene().getWindow();

        //得到用户导出的文件路径
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel xlsx files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            logger.info("未选择导出文件位置");
            return;
        }

        String path = file.getPath();

        AlarmService alarmService = BaseService.springBean("alarmService", AlarmService.class);
        List<AlarmInfo> data = alarmInfoList;
        DownloadService downloadService = new DownloadService();
        downloadService.setProgressIndicator(progressIndicator);
        ServiceBuilder.buildWith(downloadService)
                .task(() -> {
                    try (
                            OutputStream outputStream = Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    ) {
                        AlarmService.SheetInfo sheetInfo = new AlarmService.SheetInfo();
                        sheetInfo.setName("遥信");
                        sheetInfo.setIndex(0);
                        HashMap<AlarmService.SheetInfo, List<AlarmInfo>> map = new HashMap<>();
                        map.put(sheetInfo, data);
                        alarmService.createExcel("test", map, outputStream);
                    } catch (IOException e) {
//                        throw new RuntimeException(e);
                        logger.info(e.getMessage());
                    }
                    return null;
                })
                .beforeTaskStart(service -> downloadButton.setDisable(true))
                .taskFinally(service -> downloadButton.setDisable(false))
                .build()
                .start();
    }

}
