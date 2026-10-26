package com.zhuofan.validator.javafx.controller;

import atlantafx.base.theme.Styles;
import com.zhuofan.validator.javafx.JavaFxServiceUtils;
import com.zhuofan.validator.javafx.JavaFxUiUtils;
import com.zhuofan.validator.javafx.component.SearchableComboBoxTreeTableCell;
import com.zhuofan.validator.javafx.component.SelectorTreeTableCell;
import com.zhuofan.validator.javafx.properties.DeviceInfoNode;
import com.zhuofan.validator.javafx.service.device.*;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.mapper.DeviceInfoMapper;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.utils.SpringUtils;
import jakarta.annotation.Resource;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.converter.DefaultStringConverter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.SearchableComboBox;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * FXCollections.observableArrayList()
 */
@Slf4j
@Controller
public class DeviceListController extends Control{

    // 查询区域
    public TextField substationModel;
    public ComboBox<String> voltageLevelSelector;
    public ComboBox<String> deviceTypeSelector;
    public TextField deviceNameField;
    public Button searchButton;

    // 数据区域
    public StackPane stackPane;
    // 数据区域1-表格数据组件
    public TreeTableView<DeviceInfoNode> deviceDataTable;
    public TreeTableColumn<DeviceInfoNode, String> deviceNameCol;
    public TreeTableColumn<DeviceInfoNode, String> deviceTypeCol;
    public TreeTableColumn<DeviceInfoNode, String> voltageLevelCol;
    public TreeTableColumn<DeviceInfoNode, String> bayCol;
    public TreeTableColumn<DeviceInfoNode, SignalTemplate> templateCol;
    public TreeTableColumn<DeviceInfoNode, String> operationCol;
    // 表格数据 根节点
    private final TreeItem<DeviceInfoNode> dataRoot = new TreeItem<>();

    // 数据区域2-编辑区域
    public Pane modal;
    public TextField modalDeviceName;
    public ComboBox<String> modalDeviceType;
    public ComboBox<String> modalVoltageLevel;
    public ComboBox<SignalTemplate> modalSignalTemplate;
    public ComboBox<String> modalBay;

    @Setter
    private ProgressIndicator progressIndicator;

    @Resource
    private SearchService searchService;

    @Resource
    private SaveService saveService;

    @Resource
    private JavaFxDeviceService javaFxDeviceService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    private final JavaFxServiceUtils asyncServiceUtils = new JavaFxServiceUtils();

    @Resource
    private DeviceInfoService deviceInfoService;

    private DeviceInfoNode parent;

    /**
     * 初始化
     */
    @FXML
    public void initialize() {
        asyncServiceUtils.setExecutor(executor);
        // 表格初始化
        initTable();
        // 电压、设备类型初始化
        resetDeviceTypeAndVoltage();
        // 信号模板初始化
        resetSignalTemplate();

        modalSignalTemplate.setItems(FXCollections.observableArrayList());
        modalSignalTemplate.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SignalTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        // ComboBox 选中显示值
        modalSignalTemplate.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SignalTemplate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // 初始化查询数据
        this.searchButton.fire();
    }

    public void initTable() {
        this.deviceDataTable.setFixedCellSize(-1);
        this.deviceDataTable.setEditable(true);
        this.deviceDataTable.setShowRoot(false);
        this.deviceDataTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // 绑定数据
        this.deviceDataTable.rootProperty().bind(new SimpleObjectProperty<>(dataRoot));

        saveService.valueProperty().addListener((observable, oldVal, newVal) -> {
            if (BooleanUtils.isTrue(newVal)) {
                JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "修改成功", Duration.millis(3000));
            } else if (BooleanUtils.isFalse(newVal)) {
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "修改失败", Duration.millis(3000));
            }
        });

//        this.deviceDataTable.setRowFactory(tableView -> {
//            TreeTableRow<DeviceInfoNode> row = new TreeTableRow<>();
//            row.setMinHeight(40);
//            return row;
//        });
//        // 设置空根节点占位，避免后面根节点null --> non null时展示空行
//        deviceData.setRoot(new TreeItem<>(null));
//        deviceData.setShowRoot(false);
        deviceNameCol.setCellFactory(col -> {
            TextFieldTreeTableCell<DeviceInfoNode, String> cell = new TextFieldTreeTableCell<>() {
                private boolean focusCommitAdded = false;
                @Override
                public void startEdit() {
                    super.startEdit();
                    final TextField editor = (TextField) getGraphic();
                    if (editor != null && !focusCommitAdded) {
                        focusCommitAdded = true;
                        editor.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                            // 编辑器失去焦点且仍处于编辑态时,手动提交,触发 setOnEditCommit
                            if (!isFocused && isEditing()) {
                                commitEdit(editor.getText());
                            }
                        });
                    }

                }
            };
            cell.setConverter(new DefaultStringConverter());
            return cell;
        });
        deviceNameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        deviceNameCol.setOnEditCommit(event -> {
            if (Strings.CS.equals(event.getOldValue(), event.getNewValue())) {
                return;
            }
            String newValue = event.getNewValue();
            TreeItem<DeviceInfoNode> row = event.getRowValue();
            DeviceInfoNode value = row.getValue();
            value.setName(newValue);

            saveService.setDeviceInfo(value);
            saveService.restart();
        });

        deviceTypeCol.setCellValueFactory(cellData -> {
            DeviceInfoNode property = cellData.getValue().getValue();
            return property == null ? null : property.getTypeProp();
        });
        deviceTypeCol.setOnEditCommit(handler -> {
            TreeItem<DeviceInfoNode> rowValue = handler.getRowValue();
            String newDeviceType = handler.getNewValue();
            if (Strings.CS.equals(handler.getOldValue(), newDeviceType)) {
                return;
            }
            DeviceInfoNode row = rowValue.getValue();
            row.setType(newDeviceType);
            saveService.setDeviceInfo(row);
            saveService.restart();
        });

        voltageLevelCol.setCellValueFactory(cellData -> {
            DeviceInfoNode property = cellData.getValue().getValue();
            return property == null ? null : property.getVoltageLevelProp();
        });
        voltageLevelCol.setOnEditCommit(handler -> {
            TreeItem<DeviceInfoNode> rowValue = handler.getRowValue();
            String newVoltageLevel = handler.getNewValue();
            if (Strings.CS.equals(handler.getOldValue(), newVoltageLevel)) {
                return;
            }
            rowValue.getValue().setVoltage(newVoltageLevel);
            saveService.setDeviceInfo(rowValue.getValue());
            saveService.restart();
        });

        bayCol.setCellValueFactory(cellData -> {
            DeviceInfoNode property = cellData.getValue().getValue();
            return property == null ? null : property.getBayNameProp();
        });
        bayCol.setOnEditCommit(handler -> {
            TreeItem<DeviceInfoNode> rowValue = handler.getRowValue();
            String newBay = handler.getNewValue();
            if (Strings.CS.equals(handler.getOldValue(), newBay)) {
                return;
            }
            rowValue.getValue().setBayName(newBay);
            saveService.setDeviceInfo(rowValue.getValue());
            saveService.restart();
        });

        // 信号模板列
        templateCol.setOnEditCommit(handler -> {
            TreeItem<DeviceInfoNode> rowValue = handler.getRowValue();
            SignalTemplate item = handler.getNewValue();
            if (Objects.equals(handler.getOldValue(), item)) {
                return;
            }
            rowValue.getValue().setSignalTemplateId(item == null ? null : item.getId());
            saveService.setDeviceInfo(rowValue.getValue());
            saveService.restart();
        });

        // 操作列 按键
        // 单元格值，不要设置为null，以便在页面刷新时按钮渲染判断
        operationCol.setCellValueFactory(cellData -> {
            DeviceInfoNode property = cellData.getValue().getValue();
            return property == null ? null : property.getNameProp();
        });
        operationCol.setCellFactory(this::operationColumnCellFactory);

        this.searchService.valueProperty().addListener((obs, oldV, newV) -> {
            this.dataRoot.getChildren().clear();
            if (newV == null) {
                return;
            }
            this.dataRoot.getChildren().addAll(newV.getChildren());
        });
    }

    public void resetDeviceTypeAndVoltage() {
        asyncServiceUtils.asyncRun(
                () -> deviceInfoService.searchDeviceInfo(null),
                (result, err) -> {
                    // 电压等级设置
                    List<String> voltages = result.stream().map(DeviceInfo::getVoltage).filter(StringUtils::isNotBlank).distinct().toList();
                    SimpleListProperty<String> properties = new SimpleListProperty<>(FXCollections.observableArrayList());
                    properties.add("所有");
                    properties.addAll(voltages);
                    voltageLevelSelector.setItems(properties);

                    // 设备类型设置
                    List<String> deviceTypes = result.stream().map(DeviceInfo::getType).filter(StringUtils::isNotBlank).distinct().toList();
                    SimpleListProperty<String> deviceProperties = new SimpleListProperty<>(FXCollections.observableArrayList());
                    deviceProperties.add("所有");
                    deviceProperties.addAll(deviceTypes);
                    deviceTypeSelector.setItems(deviceProperties);

                    // 间隔设置
                    List<String> bays = result.stream().map(DeviceInfo::getBayName).filter(StringUtils::isNotBlank).distinct().toList();
//                    SimpleListProperty<String> bayProps = new SimpleListProperty<>(FXCollections.observableArrayList());
//                    bayProps.add("所有");
//                    bayProps.addAll(deviceTypes);

                    deviceTypeCol.setCellFactory(col -> {
                        ComboBoxTreeTableCell<DeviceInfoNode, String> cell = new ComboBoxTreeTableCell<>(deviceTypes.toArray(new String[0]));
                        cell.setEditable(true);
                        return cell;
                    });
                    voltageLevelCol.setCellFactory(col -> {
                        ComboBoxTreeTableCell<DeviceInfoNode, String> cell = new ComboBoxTreeTableCell <>(voltages.toArray(new String[0]));
                        cell.setComboBoxEditable(true);
                        return cell;
                    });
                    bayCol.setCellFactory(col -> {
                        ComboBoxTreeTableCell<DeviceInfoNode, String> cell = new ComboBoxTreeTableCell <>(bays.toArray(new String[0]));
                        cell.setComboBoxEditable(true);
                        return cell;
                    });

                    // 编辑modal 设置
                    this.modalDeviceType.setItems(FXCollections.observableArrayList(deviceTypes));
                    this.modalVoltageLevel.setItems(FXCollections.observableArrayList(voltages));
                }
        );
    }

    public void resetSignalTemplate() {
        asyncServiceUtils.asyncRun(
                () -> javaFxDeviceService.findSignalTemplates(null),
                (data, e) -> {
                    if (e != null) {
                        log.error("模板查询异常", e);
                        return;
                    }
                    modalSignalTemplate.setItems(FXCollections.observableArrayList(data));
                    Map<String, SignalTemplate> templateIdMap = data.stream().collect(Collectors.toMap(SignalTemplate::getId, it -> it));
                    templateCol.setCellValueFactory(cellData -> {
                        DeviceInfoNode row = cellData.getValue().getValue();
                        return new SimpleObjectProperty<>(templateIdMap.get(row.getSignalTemplateId()));
                    });
                    templateCol.setCellFactory(col -> {
//                        SelectorTreeTableCell<DeviceInfoNode, SignalTemplate, String> cell =
//                                new SelectorTreeTableCell<>(
//                                        DeviceInfo::getSignalTemplateId,
//                                        SignalTemplate::getId,
//                                        SignalTemplate::getName,
//                                        data
//                                );
//                        cell.setComboBoxEditable(true);
//                        return cell;
//                        ComboBoxTreeTableCell<DeviceInfoNode, String> cell = new ComboBoxTreeTableCell<>();
                        SearchableComboBoxTreeTableCell<DeviceInfoNode, SignalTemplate, String> cell = new SearchableComboBoxTreeTableCell<>(
                                (rowData, candidates) -> {
                                    if (CollectionUtils.isEmpty(candidates)) {
                                        return null;
                                    }

                                    return candidates.stream()
                                            .filter(t -> Objects.equals(rowData.getSignalTemplateId(), t.getId()))
                                            .findFirst().orElse(null);
                                },
                                SignalTemplate::getName,
                                FXCollections.observableArrayList(data)
                        );
                        cell.setEditable(true);
                        return cell;
                    });
                }
        );
    }

    public void search(ActionEvent actionEvent) {
        String voltageLevel = voltageLevelSelector.getValue();
        String deviceType = deviceTypeSelector.getValue();
        String deviceName = deviceNameField.getText();

        DeviceInfoModel param = new DeviceInfoModel();
        param.setVoltage("所有".equals(voltageLevel) ? null : voltageLevel);
        param.setType("所有".equals(deviceType) ? null : deviceType);
        param.setName(deviceName);

        searchService.setParam(param);
        searchService.restart();
    }

    /**
     * 按钮单元格工厂
     *
     * @param column
     * @return
     */
    private TreeTableCell<DeviceInfoNode, String> operationColumnCellFactory(TreeTableColumn<DeviceInfoNode, String> column) {
        return new TreeTableCell<>() {

            private final HBox buttonBox = new HBox(3);
            private final Button addButton = new Button("新增子设备");
//            private final Button editButton = new Button("编辑");
            private final Button delButton = new Button("删除");

            {
                addButton.getStyleClass().add(Styles.SMALL);
                delButton.getStyleClass().add(Styles.SMALL);
                buttonBox.getChildren().addAll(addButton, delButton);
                buttonBox.setAlignment(Pos.CENTER);
                buttonBox.setMaxHeight(Region.USE_PREF_SIZE);
                buttonBox.setPrefHeight(30);
//                buttonBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, null)));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);

                addButton.getStyleClass().addAll("button-outlined", "success");
                addButton.setOnAction(event -> {
                    // 获取被点击按钮所在的当前TreeItem对象
                    TreeItem<DeviceInfoNode> treeItem = getTableRow().getTreeItem();
                    if (treeItem != null) {
                        DeviceInfoNode itemData = treeItem.getValue();
                        System.out.println("add Button clicked for: " + itemData.getName());
                        // 在这里执行业务逻辑，例如：
                        // 删除该项: treeItem.getParent().getChildren().remove(treeItem);
                        // 展示编辑框
                        setModalDataThenShow(itemData, null);
                    }
                });

//                editButton.getStyleClass().addAll("button-outlined", "success");
//                editButton.setOnAction(event -> {
//                    // 获取被点击按钮所在的当前TreeItem对象
//                    TreeItem<DeviceInfoNode> treeItem = getTableRow().getTreeItem();
//                    if (treeItem != null) {
//                        DeviceInfoNode itemData = treeItem.getValue();
//                        System.out.println("edit Button clicked for: " + itemData.getName());
//                        // 在这里执行业务逻辑，例如：
//                        // 删除该项: treeItem.getParent().getChildren().remove(treeItem);
//                        // 展示编辑框
////                        setEditModalDataThenShow(null, treeItem);
//                    }
//                });

                delButton.getStyleClass().addAll("button-outlined", "danger");
                delButton.setOnAction(event -> {
                    // 获取被点击按钮所在的当前TreeItem对象
                    TreeItem<DeviceInfoNode> treeItem = getTableRow().getTreeItem();
                    if (treeItem != null) {
                        DeviceInfoNode itemData = treeItem.getValue();
//                        System.out.println("del Button clicked for: " + itemData.getName());
                        // 在这里执行业务逻辑，例如：
                        treeItem.getParent().getChildren().remove(treeItem);
                        boolean result = remove(itemData);

                        if (result) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "删除成功", Duration.millis(3000));
                            return;
                        }

                        String errMes = String.format("删除失败, device info id：%s", itemData.getId());
                        log.error(errMes);
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, errMes, Duration.millis(3000));
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // 空行或无效数据时不显示按钮
                if (empty || getTableRow() == null || getTableRow().getTreeItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
//                    requestLayout();
                }
            }
        };
    }

    public void setModalDataThenShow(DeviceInfoNode parent, DeviceInfoNode currentNode) {
        DeviceInfoNode node = new DeviceInfoNode();
        if (ObjectUtils.allNull(parent, currentNode)) {
            // 创建无父节点的设备
        } else if (parent != null && currentNode == null) {
            // 创建有父节点的设备
            node.setParentId(parent.getId());
        } else {
            // 编辑节点
            node = currentNode;
        }

        this.parent = parent;
        this.modalDeviceName.setText(node.getName());
        this.modalDeviceType.setValue(node.getType());
        this.modalVoltageLevel.setValue(node.getVoltage());
        SignalTemplate template = null;
        if (node.getSignalTemplateId() != null) {

        }
        this.modalSignalTemplate.setValue(template);

        // 展示编辑框
        JavaFxUiUtils.showView(stackPane, modal);
    }

    public void hideModal(ActionEvent event) {
        // 重置修改modal终端的数据
        // 删除保存按钮触发方法
//        this.editSaveButton.setOnAction(null);
        // 清空 修改modal终端的数据
        this.modalDeviceName.setText(null);
        this.modalDeviceType.setValue(null);
        this.modalVoltageLevel.setValue(null);
        showTable(event);
    }

    public void showAddModal(ActionEvent actionEvent) {
        cleanModalData();
        JavaFxUiUtils.showView(stackPane, modal);
    }

    public void showTable(ActionEvent actionEvent) {
        JavaFxUiUtils.showView(stackPane, deviceDataTable);
    }

    public void save(ActionEvent actionEvent) {
        DeviceInfo modalData = getModalData();
        asyncServiceUtils.asyncRun(() -> save(modalData), (result, err) -> {
            if (err != null) {
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "保存失败", Duration.seconds(5));
                return;
            }
            cleanModalData();
            JavaFxUiUtils.showView(stackPane, deviceDataTable);
            search(new ActionEvent(this, actionEvent.getTarget()));
        });
    }

    public DeviceInfo getModalData() {
        String deviceName = this.modalDeviceName.getText();
        String deviceType = this.modalDeviceType.getSelectionModel().getSelectedItem();
        String voltage = this.modalVoltageLevel.getSelectionModel().getSelectedItem();
        SignalTemplate selectedSignalTemplate = this.modalSignalTemplate.getSelectionModel().getSelectedItem();

        DeviceInfo deviceInfo = new DeviceInfo();
        if (this.parent != null) {
            deviceInfo.setParentId(parent.getId());
        }
        deviceInfo.setName(deviceName);
        deviceInfo.setType(deviceType);
        deviceInfo.setVoltage(voltage);
        if (selectedSignalTemplate != null) {
            deviceInfo.setSignalTemplateId(selectedSignalTemplate.getId());
        }
        return deviceInfo;
    }

    public void cleanModalData() {
        this.parent = null;
        this.modalDeviceName.setText("");
        this.modalDeviceType.getSelectionModel().clearSelection();
        this.modalVoltageLevel.getSelectionModel().clearSelection();
        this.modalSignalTemplate.getSelectionModel().clearSelection();
    }

    //--------------------------------------------------- Spring Bean ----------------------------------------------------------------
    public boolean save(DeviceInfo deviceInfo) {
        DeviceInfoMapper deviceInfoMapper = SpringUtils.getBean("", DeviceInfoMapper.class);
        try {
            if (StringUtils.isNotBlank(deviceInfo.getId())) {
                deviceInfoMapper.updateById(deviceInfo);
            } else {
                deviceInfoMapper.insert(deviceInfo);
            }
            return true;
        } catch (Exception e) {
            log.error("数据保存失败: {}", deviceInfo);
            return false;
        }
    }

    public boolean remove(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            log.error("数据删除失败: 空数据");
            return false;
        }

        DeviceInfoMapper deviceInfoMapper = SpringUtils.getBean("", DeviceInfoMapper.class);
        try {
            deviceInfoMapper.deleteById(deviceInfo.getId());
        } catch (RuntimeException e) {
            log.error("数据删除失败: {}", deviceInfo);
            return false;
        }

        return true;
    }

}
