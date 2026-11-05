# 项目说明

## 运行环境

- **Java** 21
- **JavaFX** 21.0.1

## 技术框架

| 框架 | 版本 |
|------|------|
| JavaFX | 21.0.1 |
| Spring Boot | 3.5.16 |
| MyBatis Plus | 3.5.18 |
| H2 Database | — |

## 启动方式

运行主类：

```
com.zhuofan.validator.JavaFxApplication
```

## 打包

> 备注：生成可执行 jar 和 exe 安装包
> - 可执行 jar → `target/app.jar`
> - exe 安装包 → `buildExe/`

```bash
mvn clean package
```