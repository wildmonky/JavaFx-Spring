package com.zhuofan.validator.spring.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProgressModel {

    private long total;

    private long processed = 0;

    private long remain;

    private double progress;

    public ProgressModel(long total) {
        this.total = total;
        this.remain = total;
    }

    public void setProcessed(long processed) {
        if (processed < this.processed || processed > this.total) {
            return;
        }

        this.processed = processed;
        this.remain = this.total - processed;
        this.progress = processed * 1d / this.total;
    }

}
