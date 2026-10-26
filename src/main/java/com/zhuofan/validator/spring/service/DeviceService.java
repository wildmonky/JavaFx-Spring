package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.mapper.DeviceInfoMapper;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.TreeNode;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

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

}
