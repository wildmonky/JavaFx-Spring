package com.zhuofan.validator.javafx.controller;

import com.zhuofan.validator.javafx.controller.deviceSignal.DeviceListController;
import com.zhuofan.validator.javafx.controller.deviceSignal.DeviceSignalController;
import jakarta.annotation.Resource;
import javafx.fxml.FXML;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.Objects;

/**
 * 事件处理不能阻塞，否则导致桌面端无响应（阻塞了），在事件处理逻辑中调用的组件也无法正常显示（必须事件处理逻辑走完才会刷新，所以才用线程池异步）
 */
@Slf4j
@Controller
public class MainController {

    public static volatile boolean useVirtualKeyboardVal;

    // 0.0 ---> 1.0
    @FXML
    public ProgressBar progressBar;
    // 0.0 ---> 1.0
    @FXML
    public ProgressIndicator progressIndicator;

    public CheckBox useVirtualKeyboard;

    @FXML
    private Pane analysis;
    @FXML
    public AnchorPane build;
    /**
     * 注意 在 main.fxml中引入时，fx:id必须是analysis，这时才能自动注入analysisController
     */
    @Resource
    private AnalysisController analysisController;

    @Resource
    private BuildController buildController;

    @Resource
    public DeviceListController deviceListController;

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

        useVirtualKeyboard.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (Objects.equals(oldVal, newVal)) {
                return;
            }

            if (newVal == null) {
                return;
            }

            useVirtualKeyboardVal = newVal;
        });
    }

}