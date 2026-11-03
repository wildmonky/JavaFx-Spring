package com.zhuofan.validator.spring.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.javafx.properties.SignalNodeProp;
import com.zhuofan.validator.spring.entity.DeviceInfoSignal;
import com.zhuofan.validator.spring.mapper.DeviceInfoSignalMapper;
import com.zhuofan.validator.spring.model.SignalModel;
import com.zhuofan.validator.spring.model.TreeNode;
import jakarta.annotation.Resource;
import javafx.scene.control.TreeItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SignalService {

    @Resource
    private DeviceInfoSignalMapper deviceSignalMapper;

    @Resource
    private DeviceInfoSignalService deviceInfoSignalService;

    public void removeById(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }

        deviceSignalMapper.deleteById(id);
    }

    public void clearSignalByDeviceInfoId(String deviceInfoId) {
        if (StringUtils.isBlank(deviceInfoId)) {
            return;
        }

        deviceSignalMapper.delete(Wrappers.<DeviceInfoSignal>lambdaQuery().eq(DeviceInfoSignal::getDeviceInfoId, deviceInfoId));
    }

    public void recreate(List<String> deviceInfoIds) {
        deviceInfoSignalService.checkSignalAndBuildSignalWhenNonExisted(deviceInfoIds);
    }

    public TreeItem<SignalNodeProp> searchDeviceSignalTree(List<String> deviceInfoIds, SignalModel param) {
        List<TreeNode<SignalModel>> treeNodes = deviceInfoSignalService.searchSignalTreeByDeviceInfoIdsAndSignal(deviceInfoIds, param);
        TreeItem<SignalNodeProp> root = new TreeItem<>();
        return iterateToTreeItem(root, treeNodes);
    }

    public static TreeItem<SignalNodeProp> iterateToTreeItem(TreeItem<SignalNodeProp> rootItem, List<TreeNode<SignalModel>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return rootItem;
        }

        for (TreeNode<SignalModel> child : children) {
            TreeItem<SignalNodeProp> treeChildItem = new TreeItem<>();
            treeChildItem.setValue(new SignalNodeProp(child.getSelf()));
            List<TreeNode<SignalModel>> childChildren = child.getChildren();
            TreeItem<SignalNodeProp> treeItem = iterateToTreeItem(treeChildItem, childChildren);
            rootItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

}
