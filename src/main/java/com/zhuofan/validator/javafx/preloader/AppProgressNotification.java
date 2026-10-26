package com.zhuofan.validator.javafx.preloader;

import javafx.application.Preloader;
import lombok.Getter;

@Getter
public class AppProgressNotification implements Preloader.PreloaderNotification {

    private final double progress;

    private final String stage;

    public AppProgressNotification(double progress, String stage) {
        this.progress = progress;
        this.stage = stage;
    }

}
