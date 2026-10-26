package com.zhuofan.validator.javafx;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.theme.Styles;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.util.StringUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class JavaFxUiUtils {

    public static void alertMessage(Alert.AlertType alertType, String message, Duration duration) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.setOnShowing(dialogEvent -> {
            // 定时关闭 3s
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(duration, event -> alert.close()));
            timeline.play();
        });
        alert.show();
    }

    public static void showView(Pane parent, Node node) {
        // 1. 隐藏所有子节点
        for (Node child : parent.getChildren()) {
            child.setVisible(false);
        }

        // 2. 显示目标
        node.setVisible(true);

        // 3. 显示遮罩
        parent.setVisible(true);
        parent.setManaged(true);
        parent.setMouseTransparent(false);
        parent.toFront();
    }

    /**
     * 弹出确认框
     *
     * @param root 弹出框所在根节点
     * @param message 展示的消息
     * @param confirm 确认逻辑
     * @param confirmExecutor 确认逻辑执行器
     * @param confirmPostOnUiThread 确认逻辑执行执行后,UI组件调用
     * @param cancel 取消逻辑
     * @param cancelExecutor 取消逻辑执行器
     */
    public static void dialog(StackPane root, String message,
                                  Supplier<Boolean> confirm,
                                  Executor confirmExecutor,
                                  BiConsumer<Boolean, Throwable> confirmPostOnUiThread,
                                  Supplier<Boolean> cancel,
                                  Executor cancelExecutor
    ) {
        Node node = root.getChildren().stream().filter(e -> StringUtils.equals("modalPane", e.getId())).findFirst().orElse(null);
        ModalPane modalPane = (ModalPane) node;
        if (modalPane != null) {
            if (!modalPane.isVisible()) {
                modalPane.setVisible(true);
            }

            if (modalPane.getContent() != null) {
                // TODO 强制创建skin,要不然显示空 !!!
                modalPane.applyCss();
                modalPane.layout();
                // 展示
                modalPane.setDisplay(true);
                return;
            }
        }

        if (modalPane == null) {
            modalPane = new ModalPane();
            modalPane.setId("modalPane");
            modalPane.setAlignment(Pos.CENTER);
            root.getChildren().add(modalPane);
        }

        VBox vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPrefWidth(360);
        vBox.setMaxWidth(360);
        vBox.setPadding(new Insets(24));
        vBox.setBorder(Border.stroke(LinearGradient.valueOf("linear-gradient(from 0% 0% to 100% 100%, red  0% , blue 30%,  black 100%)")));

        Text content = new Text();
        content.setText(message);
        content.setTextAlignment(TextAlignment.CENTER);
        content.setWrappingWidth(312);
        vBox.getChildren().add(content);

        Button cancelButton = new Button();
        cancelButton.setText("取消");
        cancelButton.getStyleClass().add("regular");
        final ModalPane mp = modalPane;
        cancelButton.setOnAction(actionEvent -> {
            if (cancel == null) {
                mp.hide(true);
                return;
            }

            if (cancelExecutor != null) {
                CompletableFuture.supplyAsync(cancel, cancelExecutor)
                        .whenCompleteAsync((result, err) -> {
                            if (err != null) {
                                log.error("", err);
                                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                                return;
                            }

                            mp.hide(true);
                        }, Platform::runLater);
                return;
            }

            CompletableFuture.supplyAsync(cancel)
                    .whenCompleteAsync((result, err) -> {
                        if (err != null) {
                            log.error("", err);
                            JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                            return;
                        }

                        mp.hide(true);
                    }, Platform::runLater);
        });

        Button confirmButton = new Button();
        confirmButton.setText("确定");
        confirmButton.getStyleClass().add(Styles.SUCCESS);
        confirmButton.setOnAction(actionEvent -> {
            if (confirm == null) {
                mp.hide(true);
                return;
            }

            if (confirmExecutor != null) {
                CompletableFuture.supplyAsync(confirm, confirmExecutor)
                        .whenCompleteAsync((result, err) -> {
                            if (err != null) {
                                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                            }

                            confirmPostOnUiThread.accept(result, err);

                            if (BooleanUtils.isTrue(result)) {
                                mp.hide(true);
                            }
                        }, Platform::runLater);
                return;
            }

            CompletableFuture.supplyAsync(confirm)
                    .whenCompleteAsync((result, err) -> {
                        if (err != null) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                        }

                        confirmPostOnUiThread.accept(result, err);

                        if (BooleanUtils.isTrue(result)) {
                            mp.hide(true);
                        }
                    }, Platform::runLater);
        });
        // 按钮组
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(cancelButton, confirmButton);
        vBox.getChildren().add(buttons);
        // TODO 强制创建skin,要不然显示空 !!!
        modalPane.applyCss();
        modalPane.layout();
            // 展示
        modalPane.show(vBox);
    }

    public static HBox labeledRow(String name, Node field, double labelWidth) {
        Label label = new Label(name + ":");
        label.setMinWidth(labelWidth);
        label.setAlignment(Pos.CENTER_RIGHT);
        HBox row = new HBox(8, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

}
