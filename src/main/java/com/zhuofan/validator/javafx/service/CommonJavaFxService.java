package com.zhuofan.validator.javafx.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 通用JavaFx Service
 */
public abstract class CommonJavaFxService<P, T> extends Service<T> {

    @Setter
    private P param;

    @Getter
    private Task<T> task;

    public CommonJavaFxService() {}

    public CommonJavaFxService(P param) {
        this.param = param;
    }

    @Override
    protected Task<T> createTask() {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return CommonJavaFxService.this.run(param);
            }
        };

        task.setOnFailed(this::whenFailed);
        this.task = task;
        return task;
    }

    /**
     * 实际执行逻辑
     * @param param 参数
     * @return 返回结果
     */
    public abstract T run(P param);

    public void whenFailed(WorkerStateEvent event) {
        Throwable exception = task.getException();
        if (exception != null) {
            exception.printStackTrace();
            return;
        }
        exception = event.getSource().getException();
        exception.printStackTrace();
    }

}
