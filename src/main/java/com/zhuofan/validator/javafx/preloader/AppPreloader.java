package com.zhuofan.validator.javafx.preloader;

import atlantafx.base.theme.PrimerLight;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.InputStream;

/**
 * <pre>{@code
 * // 设置类预加载
 *  System.setProperty("javafx.preloader", "com.zhuofan.validator.javafx.preloader.AppPreloader");
 *  // 启动JavaFx
 *  JavaFxApplication.launch(args);}
 *</pre>
 * 自定义预加载展示页面逻辑<br/>
 * 主程序使用{@link Application#notifyPreloader}触发{@link AppPreloader#handleApplicationNotification}
 * <pre>{@code notifyPreloader(new AppProgressNotification(0.8, "应用图标加载完成")) }</pre>
 *
 */
public class AppPreloader extends Preloader {

    private Stage preloaderStage;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Image image;

    @Override
    public void init() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/lizi.png");
        if (inputStream != null) {
            this.image = new Image(inputStream);
        }
        // 启用AtlantaFX样式
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    /**
     * 预加载页面初始化
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        this.preloaderStage = primaryStage;
        // 1. 构建加载界面
        progressBar = new ProgressBar(0);
        statusLabel = new Label("正在加载应用...");
        VBox root = new VBox(20, statusLabel, progressBar);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 300, 150);
        // 应用bootstrap样式
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        if (image != null) {
            primaryStage.getIcons().addAll(image);
        }
        primaryStage.setScene(scene);
        primaryStage.show(); // 2. 立即显示加载界面
    }

    /**
     * 自定义预加载提醒处理逻辑
     * @param info the application-generated notification
     */
    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof AppReadyPreloaderNotification) {
            fadeOut();
            return;
        }

        if (!(info instanceof AppProgressNotification notification)) {
            return;
        }

        double progress = notification.getProgress();
        progressBar.setProgress(progress);
        statusLabel.setText(notification.getStage() + "...");
    }

    /**
     * 淡出
     */
    private void fadeOut() {
        FadeTransition fade = new FadeTransition(Duration.millis(400), preloaderStage.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            preloaderStage.hide();
            preloaderStage.close();
        });
        fade.play();
    }

    /**
     * 已被JavaFx内部使用，源码直接置为1，或是1~100 每10ms步进1，更像演示 <br/>
     * 源码{@link com.sun.javafx.application.LauncherImpl#launchApplication1}
     * @param pn the progress notification
     */
    @Override
    public void handleProgressNotification(ProgressNotification pn) {}

    /**
     * 仅支持有限的生命周期阶段 {@link javafx.application.Preloader.StateChangeNotification.Type}
     * @param info the state change notification
     */
    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {}

}
