package com.zhuofan.validator.javafx.controller;

//import com.gluonhq.charm.glisten.control.ProgressIndicator;
import jakarta.annotation.Resource;
import javafx.fxml.FXML;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

/**
 * 事件处理不能阻塞，否则导致桌面端无响应（阻塞了），在事件处理逻辑中调用的组件也无法正常显示（必须事件处理逻辑走完才会刷新，所以才用线程池异步）
 */
@Slf4j
@Controller
public class MainController {

    // 0.0 ---> 1.0
    @FXML
    public ProgressBar progressBar;
    // 0.0 ---> 1.0
    @FXML
    public ProgressIndicator progressIndicator;
    public AnchorPane signalList;
    @FXML
    private Pane analysis;
    @FXML
    public AnchorPane build;
    public AnchorPane deviceList;
    /**
     * 注意 在 main.fxml中引入时，fx:id必须是analysis，这时才能自动注入analysisController
     */
    @Resource
    private AnalysisController analysisController;

    @Resource
    private BuildController buildController;

    @Resource
    public DeviceListController deviceListController;

    public AnchorPane deviceSignalList;
    @Resource
    public DeviceSignalController deviceSignalController;

    @FXML
    public void initialize() {
        analysisController.setProgressBar(progressBar);
        analysisController.setProgressIndicator(progressIndicator);

        buildController.setProgressBar(progressBar);
        buildController.setProgressIndicator(progressIndicator);

        deviceListController.setProgressIndicator(progressIndicator);
        deviceSignalController.setProgressIndicator(progressIndicator);
    }

}