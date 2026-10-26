package com.zhuofan.validator.javafx.service.signal;

import com.zhuofan.validator.javafx.properties.DeviceInfoNode;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.TreeNode;
import com.zhuofan.validator.spring.service.DeviceService;
import jakarta.annotation.Resource;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceSearchService extends Service<TreeItem<DeviceInfoNode>> {

    @Resource
    private DeviceService deviceService;

    @Setter
    private DeviceInfoModel param = null;

    @Setter
    private CheckBoxTreeItem<DeviceInfoNode> root = new CheckBoxTreeItem<>();

    public DeviceSearchService() {
        super();
    }

    @Override
    public Task<TreeItem<DeviceInfoNode>> createTask() {
        return new Task<>() {
            @Override
            protected TreeItem<DeviceInfoNode> call() throws Exception {
                if (param == null) {
                    param = new DeviceInfoModel();
                }
                List<TreeNode<DeviceInfoModel>> devices = deviceService.findDevices(param);
                root.getChildren().clear();
                return iterateToTreeItem(root, devices);
            }
        };
    }

    public static CheckBoxTreeItem<DeviceInfoNode> iterateToTreeItem(CheckBoxTreeItem<DeviceInfoNode> rootItem, List<TreeNode<DeviceInfoModel>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return rootItem;
        }

        for (TreeNode<DeviceInfoModel> child : children) {
            CheckBoxTreeItem<DeviceInfoNode> treeChildItem = new CheckBoxTreeItem<>();
            treeChildItem.setIndependent(true);
            treeChildItem.setValue(new DeviceInfoNode(child.getSelf()));
            List<TreeNode<DeviceInfoModel>> childChildren = child.getChildren();
            CheckBoxTreeItem<DeviceInfoNode> treeItem = iterateToTreeItem(treeChildItem, childChildren);
            rootItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

}