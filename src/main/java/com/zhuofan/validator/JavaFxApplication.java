package com.zhuofan.validator;

import atlantafx.base.theme.PrimerLight;
import com.zhuofan.validator.javafx.preloader.AppProgressNotification;
import com.zhuofan.validator.javafx.preloader.AppReadyPreloaderNotification;
import com.zhuofan.validator.spring.MainApplication;
import devtoolsfx.gui.GUI;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.zhuofan.validator.javafx.event.JavaFxExitEvent;
import com.zhuofan.validator.spring.utils.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;

public class JavaFxApplication extends Application {

    private static final Logger logger = LoggerFactory.getLogger(JavaFxApplication.class.getName());

    private Scene scene;

    private Image image;

    public static void main(String[] args) {
        // 设置类预加载
        System.setProperty("javafx.preloader", "com.zhuofan.validator.javafx.preloader.AppPreloader");
        // 启动JavaFx
        JavaFxApplication.launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        // 全局异常捕获
        Thread.setDefaultUncaughtExceptionHandler(this::handlerUncaughtException);

        notifyPreloader(new AppProgressNotification(0.1, "容器初始化"));
        String[] args = getParameters().getRaw().toArray(new String[0]);
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MainApplication.class, args);
        SpringUtils.setApplicationContext(applicationContext);
        notifyPreloader(new AppProgressNotification(0.5, "容器初始化完成"));

        try {
            // fxml加载fxml初始化
            FXMLLoader fxmlLoader = new FXMLLoader(JavaFxApplication.class.getResource("/view/main.fxml"));
            // 设置ControllerFactory，交给spring管理，方便注入
            fxmlLoader.setControllerFactory(SpringUtils::getBean);
            notifyPreloader(new AppProgressNotification(0.6, "开始加载页面组件"));
            Parent root = fxmlLoader.load();
            // 完全透明，淡入
            root.setOpacity(0);
            notifyPreloader(new AppProgressNotification(0.7, "窗口初始化"));
            scene = new Scene(root, 1300, 700);
            // 应用bootstrap样式 FIXME
//            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            // 启用AtlantaFX样式
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            notifyPreloader(new AppProgressNotification(0.7, "组件样式加载完成"));
            InputStream inputStream = getClass().getResourceAsStream("/lizi.png");
            if (inputStream != null) {
                image = new Image(inputStream);
            }
            notifyPreloader(new AppProgressNotification(0.8, "应用图标加载完成"));
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
        }

        notifyPreloader(new AppProgressNotification(1, "初始化完成，正在载入应用"));
    }

    private void handlerUncaughtException(Thread t, Throwable e) {
        logger.error("JavaFx异常关闭，Uncaught exception", e);
        SpringUtils.getApplicationContext().publishEvent(new JavaFxApplication());
        Platform.exit();
    }

    @Override
    public void start(Stage stage) throws Exception {
//        stage.setOnShown(e -> GUI.openToolStage(stage, getHostServices()));
        stage.setTitle("遥信规范检测工具");
        if (image != null) {
            stage.getIcons().add(image);
        }
        stage.setScene(scene);
        stage.show();
        notifyPreloader(new AppReadyPreloaderNotification());
        fadeIn(scene.getRoot());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        SpringUtils.getApplicationContext().publishEvent(new JavaFxExitEvent(this));
    }

    public static void shutdown() {
        SpringUtils.shutdown();
        Platform.exit();
    }

    private void fadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

}