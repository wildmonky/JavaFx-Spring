package com.zhuofan.validator.spring.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuofan.validator.javafx.properties.DeviceInfoNodeProp;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.mapper.DeviceInfoMapper;
import com.zhuofan.validator.spring.mapper.SignalTemplateMapper;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.TreeNode;
import jakarta.annotation.Resource;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeviceInfoService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    @Resource
    private SignalTemplateMapper templateMapper;

    public List<TreeNode<DeviceInfoModel>> findDevices(DeviceInfoModel deviceInfoModel) {

        List<DeviceInfoModel> devices = deviceInfoMapper.selectDeviceInfos(deviceInfoModel);
        if (CollectionUtils.isEmpty(devices)) {
            return Collections.emptyList();
        }

        List<String> rootIds = devices.stream().filter(e -> StringUtils.isBlank(e.getParentId())).map(DeviceInfo::getId).toList();
        Map<String, List<DeviceInfoModel>> parentIdMap = devices.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).collect(Collectors.groupingBy(DeviceInfo::getParentId));
        Map<String, DeviceInfoModel> idMap = devices.stream().collect(Collectors.toMap(DeviceInfo::getId, e -> e, (e1, e2) -> e2));

        List<TreeNode<DeviceInfoModel>> list = new ArrayList<>();

        for (String rootId : rootIds) {
            DeviceInfoModel deviceInfo = idMap.get(rootId);
            TreeNode<DeviceInfoModel> root = new TreeNode<>();
            root.setSelf(deviceInfo);
            TreeNode<DeviceInfoModel> node = buildTree(root, d -> {
                String id = d.getId();
                return Optional.ofNullable(parentIdMap.get(id)).orElse(Collections.emptyList());
            });
            if (node == null) {
                continue;
            }
            list.add(node);
        }

        return list;
    }

    public static <T> TreeNode<T> buildTree(TreeNode<T> root, Function<T, List<T>> childrenFunc) {
        if (childrenFunc == null) {
            return root;
        }

        List<T> children = childrenFunc.apply(root.getSelf());

        if (children == null) {
            return root;
        }

        for (T child : children) {
            TreeNode<T> childNode = new TreeNode<>();
            childNode.setParent(root);
            childNode.setSelf(child);
            childNode = buildTree(childNode, childrenFunc);
            List<TreeNode<T>> rootChildren = root.getChildren();
            if (rootChildren == null) {
                root.setChildren(new ArrayList<>());
                rootChildren = root.getChildren();
            }
            rootChildren.add(childNode);
        }

        return root;
    }

    /**
     * 保存
     * @param deviceInfo
     */
    public boolean save(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }

        return deviceInfoMapper.insertOrUpdate(deviceInfo);
    }

    public List<DeviceInfo> searchDeviceInfo(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return deviceInfoMapper.selectList(Wrappers.emptyWrapper());
        }

        return deviceInfoMapper.selectList(Wrappers.<DeviceInfo>lambdaQuery()
                .eq(deviceInfo.getId() != null, DeviceInfo::getId, deviceInfo.getId())
                .like(StringUtils.isNotBlank(deviceInfo.getName()), DeviceInfo::getName, deviceInfo.getName())
        );
    }

    public TreeItem<DeviceInfoNodeProp> searchDeviceInfoTree(DeviceInfoModel param) {
        CheckBoxTreeItem<DeviceInfoNodeProp> root = new CheckBoxTreeItem<>();

        if (param == null) {
            param = new DeviceInfoModel();
        }

        List<TreeNode<DeviceInfoModel>> devices = findDevices(param);
        root.getChildren().clear();
        return iterateToTreeItem(root, devices);
    }

    public static CheckBoxTreeItem<DeviceInfoNodeProp> iterateToTreeItem(CheckBoxTreeItem<DeviceInfoNodeProp> rootItem, List<TreeNode<DeviceInfoModel>> children) {
        if (CollectionUtils.isEmpty(children)) {
            return rootItem;
        }

        for (TreeNode<DeviceInfoModel> child : children) {
            CheckBoxTreeItem<DeviceInfoNodeProp> treeChildItem = new CheckBoxTreeItem<>();
            treeChildItem.setIndependent(true);
            treeChildItem.setValue(new DeviceInfoNodeProp(child.getSelf()));
            List<TreeNode<DeviceInfoModel>> childChildren = child.getChildren();
            CheckBoxTreeItem<DeviceInfoNodeProp> treeItem = iterateToTreeItem(treeChildItem, childChildren);
            rootItem.getChildren().add(treeItem);
        }

        return rootItem;
    }

    public List<SignalTemplate> findSignalTemplates(SignalTemplate param) {
        if (param == null) {
            return templateMapper.selectList(Wrappers.emptyWrapper());
        }
        return templateMapper.selectList(Wrappers.<SignalTemplate>lambdaQuery()
                .eq(param.getId() != null, SignalTemplate::getId, param.getId())
                .like(StringUtils.isNotBlank(param.getName()), SignalTemplate::getName, param.getName())
        );
    }

}
