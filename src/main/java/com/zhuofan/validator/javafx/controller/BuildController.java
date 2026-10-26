package com.zhuofan.validator.javafx.controller;

import com.zhuofan.validator.javafx.service.*;
import com.zhuofan.validator.spring.model.ProgressModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Controller
public class BuildController {

    @FXML
    public Button chooseFileButton;
    @FXML
    public Text templateFileName;
    @FXML
    public ListView<String> templateDataView;
    @FXML
    public ListView<String> buildDataView;
    @FXML
    public Button downloadButton;
    @FXML
    public Button buildButton;
    @FXML
    public TextField startNum;
    @FXML
    public TextField endNum;

    private Path selectedFilePath;

    @Setter
    public ProgressBar progressBar;
    @Setter
    public ProgressIndicator progressIndicator;

    @Setter
    @Getter
    private List<String> templateData;

    @Setter
    @Getter
    private List<String> buildData;

    private void initialize() {
        // 创建一个TextFormatter，使用IntegerStringConverter，并设置过滤器只允许数字
        TextFormatter<Integer> formatter = new TextFormatter<>(new IntegerStringConverter(), 0,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*")) { // 允许负号和数字
                        return change;
                    }
                    return null; // 拒绝非数字输入
                });
        this.startNum.setTextFormatter(formatter);
        this.endNum.setTextFormatter(formatter);
    }

    @FXML
    protected void onOpenFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("文件列表");
        FileChooser.ExtensionFilter excelExtensionFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        fileChooser.getExtensionFilters().add(excelExtensionFilter);
        File selectedFile = fileChooser.showOpenDialog(templateFileName.getScene().getWindow());
        if (selectedFile != null) {
            int maxLength = 28;
            String name = selectedFile.getName();
            if (name.length() >= maxLength) {
                name = name.substring(0, maxLength + 1) + "...";
            }
            templateFileName.setText(name);
            this.selectedFilePath = selectedFile.toPath();
        }

        AtomicReference<ProgressModel> progressModelAtomicReference = new AtomicReference<>();
        ServiceBuilder.builder(BuildService.class)
                .beforeTaskStart(service -> {
                    BuildService buildService = (BuildService)service;
                    buildService.setProgressBar(this.progressBar);
                    buildService.setProgressBar(progressBar);
                    buildService.startProcessBar(() -> {
                        ProgressModel progressModel = progressModelAtomicReference.get();
                        return progressModel == null ? 0 : progressModel.getProgress();
                    },300);
                })
                .task(() -> {
                    List<String> reList = new ArrayList<>();
                    try (
                            InputStream inputStream = Files.newInputStream(this.selectedFilePath, StandardOpenOption.READ);
                            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                    ) {
                        XSSFSheet sheet = workbook.getSheetAt(0);
                        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                            XSSFRow row = sheet.getRow(i);
                            if (row == null) {
                                continue;
                            }
                            XSSFCell cell = row.getCell(0);
                            if (cell == null) {
                                continue;
                            }

                            try {
                                String stringCellValue = cell.getStringCellValue();
                                reList.add(stringCellValue);
                            } catch (RuntimeException ignored) {}
                        }

                        return reList;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).afterTaskSucceed(service -> {
                    this.templateDataView.getItems().clear();
                    this.templateData = service.getValue();
                    this.templateDataView.getItems().addAll(this.templateData);
                }).taskFinally(service -> {
                   BaseProgressBarService<List<String>> progressBarService = (BaseProgressBarService<List<String>>) service;
                   progressBarService.destroyProgressBar();
                }).build().start();
    }

    /**
     * 解析按钮事件
     */
    @FXML
    public void build() {
        AtomicReference<ProgressModel> progressModelAtomicReference = new AtomicReference<>();
        ProgressModel progressModel = new ProgressModel(1);
        progressModelAtomicReference.set(progressModel);
        ServiceBuilder.builder(BuildService.class).task(() -> {
            List<String> reList = BuildService.replaceNumberAfterFAnd7(this.templateData,
                    Integer.parseInt(this.startNum.getText()),
                    Integer.parseInt(this.endNum.getText())
            );

            // 进度条走完
            progressModelAtomicReference.updateAndGet(t -> {
                t.setProgress(1d);
                return t;
            });
            return reList;
        }).beforeTaskStart(service -> {
            this.buildData = Collections.emptyList();
            // 选择文件按钮
            chooseFileButton.setDisable(true);
            // 下载按钮
            downloadButton.setDisable(true);
            // 禁止再次点击解析按钮，防止重复触发
            buildButton.setDisable(true);
            // 开启进度条
            BuildService analysisService = (BuildService) service;
            analysisService.setProgressBar(progressBar);
            analysisService.startProcessBar(() -> {
                ProgressModel progress = progressModelAtomicReference.get();
                return progress == null ? 0 : progress.getProgress();
            },300);
        }).afterTaskSucceed(service -> {
            this.buildDataView.getItems().clear();
            this.buildData = service.getValue();
            this.buildDataView.getItems().addAll(this.buildData);
        }).taskFinally(service -> {
            // 选择文件按钮
            chooseFileButton.setDisable(false);
            // 下载按钮
            downloadButton.setDisable(false);
            // 允许解析按钮点击
            buildButton.setDisable(false);
        }).build().start();
    }

    @FXML
    public void download(ActionEvent event) {
        List<String> data = this.buildData;

        if (CollectionUtils.isEmpty(data)) {
            log.info("无待下载数据");
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
            log.info("未选择导出文件位置");
            return;
        }

        String path = file.getPath();

        DownloadService downloadService = new DownloadService();
        downloadService.setProgressIndicator(progressIndicator);
        ServiceBuilder.buildWith(downloadService)
                .task(() -> {
                    try (
                            OutputStream outputStream = Files.newOutputStream(Paths.get(path), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                            XSSFWorkbook workbook = new XSSFWorkbook()
                    ) {
                        XSSFSheet sheet = workbook.createSheet();

                        int rowIndex = 0;
                        int maxLength = 0;
                        for (String s : data) {
                            XSSFRow row = sheet.createRow(rowIndex++);
                            XSSFCell cell = row.createCell(0);
                            cell.setCellValue(s);
                            maxLength = Math.max(maxLength, s == null ? maxLength : s.length());
                        }
                        sheet.setColumnWidth(0, (int)Math.min(255, (maxLength * 0.31 + 5) / 0.31) * 256);
                        workbook.write(outputStream);
                    } catch (IOException e) {
                        log.info(e.getMessage());
                    }
                    return null;
                })
                .beforeTaskStart(service -> downloadButton.setDisable(true))
                .taskFinally(service -> downloadButton.setDisable(false))
                .build()
                .start();
    }

}
