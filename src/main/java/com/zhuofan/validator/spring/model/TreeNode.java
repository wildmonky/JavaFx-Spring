package com.zhuofan.validator.spring.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TreeNode<T> {

    private TreeNode<T> parent;

    private T self;

    private List<TreeNode<T>> children;

}
