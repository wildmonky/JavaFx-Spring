package com.zhuofan.validator.javafx.utils;

import jakarta.annotation.Resource;
import javafx.application.Platform;
import lombok.Setter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class JavaFxServiceUtils {

    @Setter
    @Resource
    private ThreadPoolTaskExecutor executor;

    public <P,T> void asyncRun(Function<P, T> func, P p, BiConsumer<T, ? super Throwable> dataWithinUiThread) {
        CompletableFuture.supplyAsync(() -> func.apply(p), executor).whenCompleteAsync(dataWithinUiThread, Platform::runLater);
    }

    public <T> void asyncRun(Supplier<T> run, BiConsumer<T, ? super Throwable> dataWithinUiThread) {
        CompletableFuture.supplyAsync(run, executor).whenCompleteAsync(dataWithinUiThread, Platform::runLater);
    }

}
