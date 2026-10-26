package com.zhuofan.validator.javafx.service.signal;

import com.zhuofan.validator.javafx.JavaFxUiUtils;
import com.zhuofan.validator.javafx.properties.SignalNode;
import com.zhuofan.validator.javafx.service.CommonJavaFxService;
import com.zhuofan.validator.spring.model.SignalModel;
import com.zhuofan.validator.spring.model.TreeNode;
import com.zhuofan.validator.spring.service.DeviceInfoSignalService;
import jakarta.annotation.Resource;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SignalFxService extends CommonJavaFxService<List<String>, TreeItem<SignalNode>> {

    @Resource
    private DeviceInfoSignalService service;

    private final TreeItem<SignalNode> root = new TreeItem<>();

    @Override
    public TreeItem<SignalNode> run(List<String> param) {
        return buildSignalNodeByDeviceName(param);
    }

    @Override
    public void whenFailed(WorkerStateEvent event) {
        super.whenFailed(event);
        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "服务异常：" + event, Duration.seconds(10));
    }

    public TreeItem<SignalNode> buildSignalNodeByDeviceName(List<String> deviceIds) {
        List<TreeNode<SignalModel>> treeNodes = service.searchSignalTreeByDeviceInfoIds(deviceIds);
        root.getChildren().clear();
        return iterateToTreeItem(root, treeNodes);
    }

    public static TreeItem<SignalNode> iterateToTreeItem(TreeItem<SignalNode> rootItem, List<TreeNode<SignalModel>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return rootItem;
        }

        for (TreeNode<SignalModel> child : children) {
            TreeItem<SignalNode> treeChildItem = new TreeItem<>();
            treeChildItem.setValue(new SignalNode(child.getSelf()));
            List<TreeNode<SignalModel>> childChildren = child.getChildren();
            TreeItem<SignalNode> treeItem = iterateToTreeItem(treeChildItem, childChildren);
            rootItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

}
