package com.zhuofan.validator.javafx.controller;

import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件处理不能阻塞，否则导致桌面端无响应（阻塞了），在事件处理逻辑中调用的组件也无法正常显示（必须事件处理逻辑走完才会刷新，所以才用线程池异步）
 */
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    public Pane modal;

    // 0.0 ---> 1.0
    @FXML
    public ProgressBar progressBar;
    // 0.0 ---> 1.0
    @FXML
    public ProgressIndicator progressIndicator;

    @FXML
    private Pane analysis;
    /**
     * 注意 在 main.fxml中引入时，fx:id必须是analysis，这时才能自动注入analysisController
     */
    @FXML
    private AnalysisController analysisController;

    @FXML
    public AnchorPane build;
    @FXML
    private BuildController buildController;

    @FXML
    public void initialize() {
        analysisController.setProgressBar(progressBar);
        analysisController.setProgressIndicator(progressIndicator);

        buildController.setProgressBar(progressBar);
        buildController.setProgressIndicator(progressIndicator);
    }

}