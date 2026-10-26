package com.zhuofan.validator.javafx.service.device;

import com.zhuofan.validator.javafx.JavaFxUiUtils;
import com.zhuofan.validator.javafx.properties.DeviceInfoNode;
import com.zhuofan.validator.javafx.service.CommonJavaFxService;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.TreeNode;
import com.zhuofan.validator.spring.service.DeviceService;
import jakarta.annotation.Resource;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class SearchService extends CommonJavaFxService<DeviceInfoModel, TreeItem<DeviceInfoNode>> {

    @Resource
    private DeviceService deviceService;

//    @Setter
//    private DeviceInfoModel param = null;

    @Setter
    private TreeItem<DeviceInfoNode> root = new TreeItem<>();

    public SearchService() {
        super();
    }

    @Override
    public TreeItem<DeviceInfoNode> run(DeviceInfoModel param) {
        if (param == null) {
            param = new DeviceInfoModel();
        }
        List<TreeNode<DeviceInfoModel>> devices = deviceService.findDevices(param);
        root.getChildren().clear();
        return iterateToTreeItem(root, devices);
    }

    public static TreeItem<DeviceInfoNode> iterateToTreeItem(TreeItem<DeviceInfoNode> rootItem, List<TreeNode<DeviceInfoModel>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return rootItem;
        }

        for (TreeNode<DeviceInfoModel> child : children) {
            TreeItem<DeviceInfoNode> treeChildItem = new TreeItem<>();
            treeChildItem.setValue(new DeviceInfoNode(child.getSelf()));
            List<TreeNode<DeviceInfoModel>> childChildren = child.getChildren();
            TreeItem<DeviceInfoNode> treeItem = iterateToTreeItem(treeChildItem, childChildren);
            rootItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

    @Override
    public void whenFailed(WorkerStateEvent event) {
        super.whenFailed(event);
        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "服务异常", Duration.seconds(10));
    }

}
