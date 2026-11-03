package com.zhuofan.validator.javafx.utils;

import com.zhuofan.validator.spring.utils.SpringUtils;
import javafx.fxml.FXMLLoader;

import java.net.URL;

public class JavaFxLoadUtils {

    public static FXMLLoader loadFXML(URL url) {
        try {
            // fxml加载fxml初始化
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            // 设置ControllerFactory，交给spring管理，方便注入
            fxmlLoader.setControllerFactory(SpringUtils::getBean);
            fxmlLoader.load();
            return fxmlLoader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
