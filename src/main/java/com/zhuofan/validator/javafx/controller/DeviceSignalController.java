package com.zhuofan.validator.javafx.controller;

import atlantafx.base.controls.ModalPane;
import atlantafx.base.layout.ModalBox;
import atlantafx.base.theme.Styles;
import com.zhuofan.validator.javafx.JavaFxServiceUtils;
import com.zhuofan.validator.javafx.JavaFxUiUtils;
import com.zhuofan.validator.javafx.properties.DeviceInfoNode;
import com.zhuofan.validator.javafx.properties.SignalNode;
import com.zhuofan.validator.javafx.properties.SignalTemplateDetailProperty;
import com.zhuofan.validator.javafx.service.device.DeviceInfoService;
import com.zhuofan.validator.javafx.service.signal.DeviceSearchService;
import com.zhuofan.validator.javafx.service.signal.SignalFxService;
import com.zhuofan.validator.javafx.service.signal.SignalService;
import com.zhuofan.validator.spring.entity.DeviceInfo;
import com.zhuofan.validator.spring.entity.DeviceInfoSignal;
import com.zhuofan.validator.spring.model.DeviceInfoModel;
import com.zhuofan.validator.spring.model.SignalModel;
import com.zhuofan.validator.spring.service.DeviceInfoSignalService;
import jakarta.annotation.Resource;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class DeviceSignalController extends Control{

    // 设备查询参数
    public ComboBox<String> voltageLevelSelector;
    public ComboBox<String> deviceTypeSelector;
    public TextField deviceNameField;
    // 设备查询按钮
    public Button searchDeviceButton;
    // 设备信号树
    public TreeView<DeviceInfoNode> deviceTreeView;

    private final CheckBoxTreeItem<DeviceInfoNode> deviceTreeData = new CheckBoxTreeItem<>();

    // 信号查询参数
    public TextField searchParamTypeName;
    public TextField searchParamTemplate;
    // 信号查询按钮
    public Button searchSignalButton;
    // 信号导出按钮
    public Button exportSignalButton;

    // 信号表格
    public TreeTableView<SignalNode> dataTable;
    // 信号列
    public TreeTableColumn<SignalNode, String> nameCol;
    public TreeTableColumn<SignalNode, String> typeCol;
    public TreeTableColumn<SignalNode, String> templateCol;
    public TreeTableColumn<SignalNode, String> signalCol;
    public TreeTableColumn<SignalNode, String> bayNameCol;
    public TreeTableColumn<SignalNode, String> normalStatusCol;
    public TreeTableColumn<SignalNode, String> remoteSignalingTypeCol;
    public TreeTableColumn<SignalNode, SignalNode> operationCol;

    // 弹出框
    public ModalPane modal;
    public ModalBox signalModal;
    public TextField typeFieldInModal;
    public TextField templateFieldInModal;
    public TextField signalFieldInModal;
    public TextField bayInModal;
    public ComboBox<String> normalStatusInModal;
    public ComboBox<String> remoteSignalingTypeInModal;

    private DeviceInfoSignal modalData;

    @Setter
    private ProgressIndicator progressIndicator;

    private final TreeItem<SignalNode> dataRoot = new TreeItem<>();

    @Resource
    private DeviceSearchService deviceSearchService;

    @Resource
    private SignalFxService signalFxService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    private final JavaFxServiceUtils serviceUtils = new JavaFxServiceUtils();

    @Resource
    private SignalService signalService;

    private final Set<String> selectedDeviceIds = new HashSet<>();

    @Resource
    private DeviceInfoService deviceInfoService;

    @Resource
    private DeviceInfoSignalService deviceInfoSignalService;

    /**
     * 初始化
     */
    @FXML
    public void initialize() {
        serviceUtils.setExecutor(executor);
        // 查询条件初始化
        initDeviceSearchParam();

        // 绑定设备
        deviceTreeView.setRoot(this.deviceTreeData);
        deviceTreeView.setShowRoot(false);
        deviceSearchService.valueProperty().addListener((obs, oldVal, newVal) -> {
            // service restart时，value会被置为null
            if (newVal == null) {
                return;
            }
            this.deviceTreeData.getChildren().clear();
            this.deviceTreeData.getChildren().addAll(newVal.getChildren());
        });
        // 允许多选
//        deviceTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        deviceTreeView.setCellFactory(tv -> {
            CheckBoxTreeCell<DeviceInfoNode> cell = new CheckBoxTreeCell<>() {
                @Override
                public void updateItem(DeviceInfoNode item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }
                    String showText = getDeviceNodeName(item);
                    setText(showText);
                    CheckBoxTreeItem<DeviceInfoNode> treeItem = (CheckBoxTreeItem<DeviceInfoNode>) getTreeItem();
                    ChangeListener<Boolean> listener = (observe, oldVal, newVal) -> {
                        if (oldVal == newVal) {
                            return;
                        }

                        if (newVal) {
                            selectedDeviceIds.add(item.getId());
                        } else {
                            selectedDeviceIds.remove(item.getId());
                        }
                    };
                    treeItem.selectedProperty().removeListener(listener);
                    treeItem.selectedProperty().addListener(listener);
                }
            };

//            cell.setOnMouseClicked(event -> {
//                if (cell.isEmpty()) return;   // 空白单元格不处理
//                TreeItem<DeviceInfoNode> item = cell.getTreeItem();
//                if (item == null) return;
//
//                // 不能有耗时操作否则只会触发单击逻辑
////                if (event.getClickCount() == 2) {
////                    checkBox.setSelected(!checkBox.selectedProperty().get());
////                    JavaFxUtils.alertMessage(Alert.AlertType.INFORMATION, "双击", Duration.seconds(10));
////                }
//            });

            return cell;
        });

        // 表格初始化
        initTable();
        deviceSearchService.restart();

        this.signalModal = initSignalModal();
    }

    public void initTable() {
        this.dataTable.setFixedCellSize(-1);
        this.dataTable.setEditable(true);
        this.dataTable.setShowRoot(false);
        this.dataTable.getStyleClass().addAll(Styles.BORDERED, Styles.STRIPED, Styles.DENSE);
        // 绑定数据
        this.dataTable.rootProperty().bind(new SimpleObjectProperty<>(dataRoot));

        this.nameCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        this.nameCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getDeviceNameProperty();
        });

        this.typeCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        this.typeCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getTypeProperty();
        });

        this.templateCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        this.templateCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getTemplateProperty();
        });

        this.signalCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        this.signalCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getSignalProperty();
        });

        this.bayNameCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        this.bayNameCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getBayNameProp();
        });

        this.normalStatusCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn("常开", "常闭"));
        this.normalStatusCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getNormalStatusProp();
        });

        this.remoteSignalingTypeCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn("双位置遥信", "单点遥信"));
        this.remoteSignalingTypeCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return value == null ? null : value.getRemoteSignalingTypeProp();
        });

        // 操作列
        operationCol.setCellValueFactory(cellData -> {
            SignalNode value = cellData.getValue().getValue();
            return new SimpleObjectProperty<>(value);
        });
        operationCol.setCellFactory(col -> new TreeTableCell<>() {
            final Button editBtn = new Button("编辑");
            final Button delBtn = new Button("删除");
            final HBox btnBox = new HBox(2);

            final Button addBtn = new Button("新增信号");
            final Button clearBtn = new Button("清空信号");
            final Button recreateBtn = new Button("生成信号");
            final HBox box = new HBox(2);

            {
                delBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.DANGER);
                delBtn.setOnAction(ActionEvent -> {
                    SignalNode item = getItem();
                    if (item == null || item.getId() == null) {
                        return;
                    }

                    serviceUtils.asyncRun(() -> {
                        signalService.removeById(item.getId());
                        return true;
                    }, (result, err) -> {
                        if (err != null) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                            return;
                        }

                        if (BooleanUtils.isTrue(result)) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "删除成功", Duration.seconds(5));
                        }

                        ActionEvent event = new ActionEvent(this, clearBtn);
                        searchSignal(event);
                    });
                });

                editBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.ACCENT);
                editBtn.setOnAction(actionEvent -> {
//                    TreeItem<SignalNode> treeItem = this.getTableRow().getTreeItem();
                    SignalNode item = getItem();
                    deviceSignalModalAndShow(item);
                });
                btnBox.getChildren().addAll(editBtn, delBtn);
                btnBox.setAlignment(Pos.CENTER);
                btnBox.setPrefHeight(0);
                btnBox.setMaxHeight(35);

                addBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.SUCCESS);
                addBtn.setOnAction(actionEvent -> {
                    SignalNode item = getItem();
                    deviceSignalModalAndShow(null);
                });

                clearBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.DANGER);
                clearBtn.setOnAction(actionEvent -> {
                    SignalNode item = getItem();
                    if (item == null || item.getDeviceInfoId() == null) {
                        return;
                    }
                    serviceUtils.asyncRun(() -> {
                        signalService.clearSignalByDeviceInfoId(item.getDeviceInfoId());
                        return true;
                    }, (result, err) -> {
                        if (err != null) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                            return;
                        }

                        if (BooleanUtils.isNotTrue(result)) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.WARNING, "清空失败", Duration.seconds(5));
                        } else {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "清空成功", Duration.seconds(5));
                        }

                        ActionEvent event = new ActionEvent(this, clearBtn);
                        searchSignal(event);
                    });
                });

                recreateBtn.getStyleClass().addAll(Styles.SMALL, Styles.BUTTON_OUTLINED, Styles.ACCENT);
                recreateBtn.setOnAction(actionEvent -> {
                    SignalNode item = getItem();
                    serviceUtils.asyncRun(() -> {
                        signalService.recreate(Collections.singletonList(item.getDeviceInfoId()));
                        return true;
                    }, (result, err) -> {
                        if (err != null) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                            return;
                        }

                        if (BooleanUtils.isNotTrue(result)) {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.WARNING, "生成信号失败", Duration.seconds(5));
                        } else {
                            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "生成信号成功", Duration.seconds(5));
                        }

                        ActionEvent event = new ActionEvent(this, clearBtn);
                        searchSignal(event);
                    });
                });

                box.getChildren().addAll(addBtn, clearBtn, recreateBtn);
                box.setAlignment(Pos.CENTER);
                // 设置高度
                box.setPrefHeight(0);
                box.setMaxHeight(35);
            }

            @Override
            protected void updateItem(SignalNode item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                if (StringUtils.isNotBlank(item.getId())) {
                    setGraphic(btnBox);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    public void initDeviceSearchParam() {
        serviceUtils.asyncRun(
                () -> deviceInfoService.searchDeviceInfo(null),
                (result, err) -> {
                    List<String> voltages = result.stream().map(DeviceInfo::getVoltage).distinct().sorted().toList();

//                    List<String> voltages = Arrays.asList("500kV","220kV","110kV", "66kV", "35kV", "10kV");
                    SimpleListProperty<String> voltageLevelProperties = new SimpleListProperty<>(FXCollections.observableArrayList());
                    voltageLevelProperties.add("所有");
                    voltageLevelProperties.addAll(voltages);
                    voltageLevelSelector.setItems(voltageLevelProperties);
                    // 设备类型设置
                    List<String> deviceTypes = result.stream().map(DeviceInfo::getType).distinct().toList();
//                    List<String> deviceTypes = Arrays.asList("主变", "接地变", "母线", "线路", "开关", "分段开关", "母联开关", "刀闸", "接地刀闸");
                    SimpleListProperty<String> deviceTypeProperties = new SimpleListProperty<>(FXCollections.observableArrayList());
                    deviceTypeProperties.add("所有");
                    deviceTypeProperties.addAll(deviceTypes);
                    deviceTypeSelector.setItems(deviceTypeProperties);
                }
        );
    }

    public static String getDeviceNodeName(DeviceInfoNode item) {
        return item.getVoltage() + item.getName();
    }

    public void deviceSelectedTrigger(TreeItem<SignalNode> root, TreeItem<DeviceInfoNode> node, boolean selected, List<TreeItem<SignalNode>> value) {
        if (root == null || node == null) {
            return;
        }

        signalFxService.restart();

        String deviceName = DeviceSignalController.getDeviceNodeName(node.getValue());
        List<TreeItem<SignalNode>> nodes = new ArrayList<>();
        // 删除已存在的设备信号节点
        if (!selected) {
            // 查询设备对应节点
            findNodes(root, deviceName, nodes);
            if (nodes.isEmpty()) {
                return;
            }

            // 删除对应的节点
            for (TreeItem<SignalNode> n : nodes) {
                TreeItem<SignalNode> parent = n.getParent();
                parent.getChildren().remove(n);
            }

            return;
        }

        // 添加新选中的设备信号节点
        TreeItem<DeviceInfoNode> parentNode = node.getParent();
        DeviceInfoNode parentDevice = parentNode.getValue();
        if (parentDevice != null) {// 存在父节点
            String parentDeviceName = DeviceSignalController.getDeviceNodeName(parentDevice);
            findNodes(root, parentDeviceName, nodes);
        } else { // 无父节点
            nodes.add(root);
        }

        if (nodes.isEmpty()) {
            return;
        }

        TreeItem<SignalNode> treeItem = nodes.get(0);
        treeItem.getChildren().addAll(value);
    }

    /**
     * 查询{@code device}所在的节点
     * @param root 根节点
     * @param deviceName 设备名
     * @return 设备名对应的节点列表
     */
    public void findNodes(TreeItem<SignalNode> root, String deviceName, List<TreeItem<SignalNode>> nodeList) {
        ObservableList<TreeItem<SignalNode>> children = root.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        List<TreeItem<SignalNode>> nodes = children.stream().filter(e -> {
            SignalNode node = e.getValue();
            if (node == null) {
                return false;
            }

            return StringUtils.equals(node.getDeviceName(), deviceName);
        }).toList();

        nodeList.addAll(nodes);

        for (TreeItem<SignalNode> child : children) {
            findNodes(child, deviceName, nodeList);
        }
    }

    public void searchDevice(ActionEvent actionEvent) {
        // 清空选中的组件
        selectedDeviceIds.clear();

        String voltageLevel = voltageLevelSelector.getValue();
        String deviceType = deviceTypeSelector.getValue();
        String deviceName = deviceNameField.getText();

        DeviceInfoModel param = new DeviceInfoModel();
        param.setVoltage("所有".equals(voltageLevel) ? null : voltageLevel);
        param.setType("所有".equals(deviceType) ? null : deviceType);
        param.setName(deviceName);

        deviceSearchService.setParam(param);
        deviceSearchService.restart();
    }

    /**
     * 查询信号
     * @param actionEvent
     */
    public void searchSignal(ActionEvent actionEvent) {
        String type = searchParamTypeName.getText();
        String template = searchParamTemplate.getText();

        SignalModel param = new SignalModel();
        param.setSignalType(type);
        param.setSignalTemplate(template);

        serviceUtils.asyncRun(
                () -> signalService.searchDeviceSignalTree(new ArrayList<>(selectedDeviceIds), param),
                (result, err) -> {
                    if (err != null) {
                        log.error("设备信号查询失败", err);
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }
                    ObservableList<TreeItem<SignalNode>> children = this.dataRoot.getChildren();
                    children.clear();
                    children.addAll(result.getChildren());
                }
        );

        signalFxService.setParam(new ArrayList<>(selectedDeviceIds));
//        signalFxService.restart();
    }

    public void exportSignal(ActionEvent actionEvent) {

        Node node = (Node) actionEvent.getTarget();
        Window window = node.getScene().getWindow();

        //得到用户导出的文件路径
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel xlsx files (*.xlsx)", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(window);

        if (file == null) {
            log.info("未选择导出文件位置");
            return;
        }

        serviceUtils.asyncRun(() -> {
            try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
                deviceInfoSignalService.exportDeviceSignals(selectedDeviceIds, outputStream);
            } catch (Exception e) {
                return false;
            }
            return true;
        }, (result, err) -> {
            if (err != null) {
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                return;
            }

            JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "下载成功", Duration.seconds(5));
        });
    }

    public void deviceSignalModalAndShow(DeviceInfoSignal deviceSignal) {
        modal.setVisible(true);
        // 设置值
        setModalData(deviceSignal);
        modal.show(signalModal);
    }

    public void setModalData(DeviceInfoSignal signal) {
        this.modalData = signal;
        if (this.modalData == null) {
            typeFieldInModal.clear();
            templateFieldInModal.clear();
            signalFieldInModal.clear();
            bayInModal.clear();
            normalStatusInModal.getSelectionModel().clearSelection();
            remoteSignalingTypeInModal.getSelectionModel().clearSelection();
            return;
        }

        typeFieldInModal.setText(this.modalData.getSignalType());
        templateFieldInModal.setText(this.modalData.getSignalTemplate());
        signalFieldInModal.setText(this.modalData.getSignal());
        bayInModal.setText(this.modalData.getBayName());
        normalStatusInModal.getSelectionModel().select(this.modalData.getNormalStatus());
        remoteSignalingTypeInModal.getSelectionModel().select(this.modalData.getRemoteSignalingType());
    }

    public DeviceInfoSignal getModalData() {
        String type = typeFieldInModal.getText();
        String template = templateFieldInModal.getText();
        String signal = signalFieldInModal.getText();

        this.modalData.setSignalType(type);
        this.modalData.setSignalTemplate(template);
        this.modalData.setSignal(signal);
        this.modalData.setBayName(bayInModal.getText());
        this.modalData.setNormalStatus(normalStatusInModal.getSelectionModel().getSelectedItem());
        this.modalData.setRemoteSignalingType(remoteSignalingTypeInModal.getSelectionModel().getSelectedItem());

        return this.modalData;
    }


    /**
     * 初始化 deviceSignal 模态框
     */
    public ModalBox initSignalModal() {
        ModalBox modalBox = new ModalBox();
        modalBox.setPrefWidth(420);
        modalBox.setPrefHeight(300);
        modalBox.setMaxWidth(420);
        modalBox.setMaxHeight(300);

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(24));
        vBox.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("设备信号");
        vBox.getChildren().add(title);

        double labelWidth = 90;
        typeFieldInModal = new TextField();
        templateFieldInModal = new TextField();
        signalFieldInModal = new TextField();
        bayInModal = new TextField();
        normalStatusInModal = new ComboBox<>(FXCollections.observableArrayList("常开", "常闭"));
        normalStatusInModal.setEditable(true);
        normalStatusInModal.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        remoteSignalingTypeInModal = new ComboBox<>(FXCollections.observableArrayList("单点遥信", "双位置遥信"));
        remoteSignalingTypeInModal.setEditable(true);
        remoteSignalingTypeInModal.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        typeFieldInModal.setMaxWidth(Double.MAX_VALUE);
        templateFieldInModal.setMaxWidth(Double.MAX_VALUE);
        signalFieldInModal.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().addAll(
                JavaFxUiUtils.labeledRow("信息类型名称", typeFieldInModal, labelWidth),
                JavaFxUiUtils.labeledRow("规范信息描述", templateFieldInModal, labelWidth),
                JavaFxUiUtils.labeledRow("规范信息", signalFieldInModal, labelWidth),
                JavaFxUiUtils.labeledRow("间隔", bayInModal, labelWidth),
                JavaFxUiUtils.labeledRow("信号常态", normalStatusInModal, labelWidth),
                JavaFxUiUtils.labeledRow("遥信类型", remoteSignalingTypeInModal, labelWidth)
        );

        HBox hBox = new HBox(20);
        Button cancelBtn = new Button("取消");
        cancelBtn.getStyleClass().add(Styles.BUTTON_OUTLINED);
        cancelBtn.setOnAction(actionEvent -> {
            // 隐藏modal
            this.modal.hide();
        });

        Button saveBtn = new Button("保存");
        saveBtn.getStyleClass().addAll(Styles.ACCENT, Styles.BUTTON_OUTLINED);
        saveBtn.setOnAction(actionEvent -> {
            // 保存数据
            DeviceInfoSignal deviceInfoSignal = getModalData();
            serviceUtils.asyncRun(
                    () -> deviceInfoSignalService.saveDeviceInfoSignal(deviceInfoSignal),
                    (result, err) -> {
                        modal.hide();
                        searchSignal(new ActionEvent(this, saveBtn));
                    }
            );
        });

        hBox.getChildren().addAll(cancelBtn, saveBtn);
        hBox.setMaxWidth(Double.MAX_VALUE);
        hBox.setAlignment(Pos.CENTER);

        vBox.getChildren().add(hBox);

        modalBox.setOnClose(event -> {
            this.modal.hide();
        });
        modalBox.addContent(vBox);
        AnchorPane.setTopAnchor(vBox, 0.0);
        AnchorPane.setLeftAnchor(vBox, 0.0);
        AnchorPane.setBottomAnchor(vBox, 0.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        return modalBox;
    }

}
