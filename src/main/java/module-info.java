module com.zhuofan.validator {
    // requires 当前模块需要引入包
    // requires transitive A 依赖本模块的程序会自动依赖A
    // require static lombok 程序编译时，lombok依赖包必须存在，运行时不一定存在。跟maven scope重复？
    // exports 导出当前模块的包，允许所有外部模块引入。注：只能访问公有类、接口、枚举、注解
    // exports A(本模块中的包) to B（外部模块） 只对B模块可引入，其他外部模块无法引入。注：只能访问公有类、接口、枚举、注解
    // 如果是编译时生成代理类，那么open、opens需与exports结合使用。
    // open module org.lizhao.validator 开放整个模块，允许运行时反射访问，与opens不能同时使用
    // opens A 允许包A在运行时，被反射访问
    // opens A to B 允许包A在运行时，被B模块通过反射访问
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;

    requires spring.boot.autoconfigure;
    requires spring.boot;
    requires spring.context;
    requires spring.core;
    requires spring.beans;
    requires spring.tx;

    requires jakarta.annotation;

    requires org.apache.poi.ooxml;

    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;

    requires org.slf4j;
    requires org.apache.logging.log4j;

    requires org.mybatis;
    requires com.baomidou.mybatis.plus.core;
    requires com.baomidou.mybatis.plus.annotation;
    requires com.baomidou.mybatis.plus.extension;
    requires org.mybatis.spring;
    requires java.sql;
    requires spring.aop;

    requires com.google.gson;
    requires java.desktop;
    requires java.rmi;
    requires java.management.rmi;
//    requires com.gluonhq.charm.glisten;
//    requires com.gluonhq.attach.util;
    requires atlantafx.base;
    requires org.apache.commons.codec;
    requires easyexcel.core;
    requires devtoolsfx.gui;
    requires devtoolsfx.connector;
    requires org.controlsfx.controls;

    exports com.zhuofan.validator;
    exports com.zhuofan.validator.javafx.properties;
    exports com.zhuofan.validator.javafx.event;
    exports com.zhuofan.validator.javafx.controller to javafx.fxml, spring.beans;
    opens com.zhuofan.validator.javafx.controller to javafx.fxml, spring.core;
    exports com.zhuofan.validator.javafx.service to javafx.fxml, spring.beans;
    opens com.zhuofan.validator.javafx.service to javafx.fxml, spring.core;
    exports com.zhuofan.validator.javafx.service.signal to spring.beans, spring.core;
    opens com.zhuofan.validator.javafx.service.signal to spring.beans, spring.core;

    exports com.zhuofan.validator.javafx.service.signal.template to spring.beans, spring.core;
    opens com.zhuofan.validator.javafx.service.signal.template to spring.beans, spring.core;

    exports com.zhuofan.validator.spring.config;
    opens com.zhuofan.validator.spring.config to spring.core;

    exports com.zhuofan.validator.spring.config.mybatis;
    opens com.zhuofan.validator.spring.config.mybatis to spring.core;

    exports com.zhuofan.validator.spring.service;
    opens com.zhuofan.validator.spring.service to spring.core;

    exports com.zhuofan.validator.spring.mapper;
    opens com.zhuofan.validator.spring.mapper;

    exports com.zhuofan.validator.spring.entity;
    opens com.zhuofan.validator.spring.entity;

    exports com.zhuofan.validator.spring.model;
    opens com.zhuofan.validator.spring.model to com.google.gson;

    // 否则flyway无法获取文件输入流
    opens db.migration.h2;
    // json文件读取
    opens data;
    exports com.zhuofan.validator.spring;
    exports com.zhuofan.validator.spring.enums;
    exports com.zhuofan.validator.spring.event;
    opens com.zhuofan.validator.javafx;
    exports com.zhuofan.validator.javafx;
    exports com.zhuofan.validator.javafx.preloader;
    exports com.zhuofan.validator.javafx.service.device to javafx.fxml, spring.beans;
    opens com.zhuofan.validator.javafx.service.device to javafx.fxml, spring.core;

    exports com.zhuofan.validator.javafx.component;
    opens com.zhuofan.validator.javafx.component;
}