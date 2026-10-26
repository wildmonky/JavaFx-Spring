package com.zhuofan.validator.javafx.service.signal;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.javafx.properties.SignalNode;
import com.zhuofan.validator.spring.entity.DeviceInfoSignal;
import com.zhuofan.validator.spring.mapper.DeviceInfoSignalMapper;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.SignalModel;
import com.zhuofan.validator.spring.model.TreeNode;
import com.zhuofan.validator.spring.service.DeviceInfoSignalService;
import jakarta.annotation.Resource;
import javafx.scene.control.TreeItem;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
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

    public TreeItem<SignalNode> searchDeviceSignalTree(List<String> deviceInfoIds, SignalModel param) {
        List<TreeNode<SignalModel>> treeNodes = deviceInfoSignalService.searchSignalTreeByDeviceInfoIdsAndSignal(deviceInfoIds, param);
        TreeItem<SignalNode> root = new TreeItem<>();
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
