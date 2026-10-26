package com.zhuofan.validator.javafx.controller;

import atlantafx.base.theme.Styles;
import com.alibaba.excel.util.BooleanUtils;
import com.zhuofan.validator.javafx.JavaFxServiceUtils;
import com.zhuofan.validator.javafx.JavaFxUiUtils;
import com.zhuofan.validator.javafx.properties.SignalTemplateDetailProperty;
import com.zhuofan.validator.javafx.service.signal.template.SignalTemplateService;
import com.zhuofan.validator.spring.entity.SignalTemplate;
import com.zhuofan.validator.spring.entity.SignalTemplateDetail;
import com.zhuofan.validator.spring.model.SignalTemplateDetailModel;
import com.zhuofan.validator.spring.model.SignalTemplateModel;
import jakarta.annotation.Resource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.Collections;

@Slf4j
@Controller
public class SignalTemplateController extends Control {

    @Resource
    private DeviceListController deviceListController;

    // 查询参数
    public TextField nameParam;
    // 查询按钮
    public Button searchNameButton;
    public Button showAddTemplateButton;
    // 设备信号树
    public ListView<SignalTemplateModel> templateNameListView;
    private final ObservableList<SignalTemplateModel> templateNameData = FXCollections.observableArrayList();
    // 设备新增弹出框
    public StackPane templateStackPane;
    public VBox templateModal;
    public TextField templateNameParam;

    // 信号查询参数
    public TextField typeNameParam;
    public TextField descParam;
    // 信号查询按钮
    public Button searchDetailButton;
    public Button showAddDetailModalButton;
    public Button importButton;

    // 信号表格
    public TableView<SignalTemplateDetailProperty> table;
    private final ObservableList<SignalTemplateDetailProperty> tableData = FXCollections.observableArrayList();
    // 信号表格列
    public TableColumn<SignalTemplateDetailProperty, String> typeCol;
    public TableColumn<SignalTemplateDetailProperty, String> descCol;
    public TableColumn<SignalTemplateDetailProperty, String> sampleCol;
    public TableColumn<SignalTemplateDetailProperty, String> contentCol;
    public TableColumn<SignalTemplateDetailProperty, String> normalStatusCol;
    public TableColumn<SignalTemplateDetailProperty, String> remoteSignalingTypeCol;
    public TableColumn<SignalTemplateDetailProperty, String> operationCol;

    // 信号新增弹出框
    public StackPane templateDetailStackPane;
    public VBox templateDetailModal;
    public TextField typeName;
    public TextField name;
    public TextField sample;
    public TextField content;
    public ScrollPane tableModal;

    /**
     * 0-未显示;1-新增;2-修改
     */
    private int templateModalType = 0;
    private SignalTemplateModel editSignalTemplate = null;

    @Setter
    private ProgressIndicator progressIndicator;

    private SignalTemplateModel selectedSignalTemplate = null;

    @Resource
    private SignalTemplateService signalTemplateService;

    @Resource
    private ThreadPoolTaskExecutor executor;

    private final JavaFxServiceUtils serviceUtils = new JavaFxServiceUtils();

    /**
     * 初始化
     */
    @FXML
    public void initialize() {

        serviceUtils.setExecutor(executor);
        templateNameListView.setItems(templateNameData);
        templateNameListView.setCellFactory(lv -> {
            ListCell<SignalTemplateModel> cell = new ListCell<>() {
                private final Button delButton = new Button();

                {
                    delButton.setText("删除");
                    delButton.getStyleClass().add(Styles.DANGER);
                    delButton.setOnAction(actionEvent -> {
                        SignalTemplateModel item = getItem();
                        if (item == null) {
                            return;
                        }
                        JavaFxUiUtils.dialog(templateStackPane,
                                "是否删除？",
                                () -> {
                                    signalTemplateService.removeTemplateByName(item.getName());
                                    return true;
                                },
                                executor,
                                (confirmResult, err) -> {
                                    if (err != null) {
                                        log.error("信号模板删除异常", err);
                                        return;
                                    }
                                    if (BooleanUtils.isTrue(confirmResult)) {
                                        ActionEvent event = new ActionEvent(this, delButton);
                                        searchTemplate(event);
                                        deviceListController.resetSignalTemplate();
                                    }
                                },
                                null,
                                executor
                        );
                    });
                }

                @Override
                protected void updateItem(SignalTemplateModel item, boolean empty) {
                    super.updateItem(item, empty);  // 必须调用
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getName());
                        setItem(item);
                        setGraphic(delButton);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                SignalTemplateModel item = cell.getItem();
                if (item == null) {
                    return; // 空cell点击，不触发
                }
                selectedSignalTemplate = item;
                searchDetail(item.getId());
            });

            return cell;
        });
        searchTemplate(new ActionEvent(this, this));

        // 表格初始化
        this.table.setFixedCellSize(-1);
        this.table.setEditable(true);
        // 绑定数据
        this.table.setItems(tableData);
        searchSignalDetail(new ActionEvent(this, this));

        initTableCol();
    }

    public void initTableCol() {
        this.typeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        this.typeCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getTypeProp();
        });

        this.descCol.setCellFactory(TextFieldTableCell.forTableColumn());
        this.descCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getDescProp();
        });

        this.sampleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        this.sampleCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getSampleProp();
        });

        this.contentCol.setCellFactory(TextFieldTableCell.forTableColumn());
        this.contentCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getContentProp();
        });

        this.normalStatusCol.setCellFactory(ComboBoxTableCell.forTableColumn("常开", "常闭"));
        this.normalStatusCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getNormalStatusProp();
        });

        this.remoteSignalingTypeCol.setCellFactory(ComboBoxTableCell.forTableColumn("双位置遥信", "单点遥信"));
        this.remoteSignalingTypeCol.setCellValueFactory(cellData -> {
            SignalTemplateDetailProperty value = cellData.getValue();
            return value == null ? null : value.getRemoteSignalingTypeProp();
        });

        this.operationCol.setCellFactory(col ->
                new TableCell<>() {
                    private final Button delButton = new Button("删除");

                    {

                        delButton.getStyleClass().add(Styles.SMALL);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        setAlignment(Pos.CENTER);

                        delButton.getStyleClass().addAll(Styles.DANGER, Styles.BUTTON_OUTLINED);
                        delButton.setOnAction(event -> {
                            // 获取被点击按钮所在的当前TreeItem对象
                            SignalTemplateDetailProperty prop = getTableRow().getItem();
                            if (prop == null || StringUtils.isBlank(prop.getId())) {
                                JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "空数据", Duration.seconds(3));
                                return;
                            }

                            serviceUtils.asyncRun(() -> {
                                signalTemplateService.removeTemplateDetailById(prop.getId());
                                return true;
                            }, (result, err) -> {
                                if (err != null) {
                                    JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(3));
                                    return;
                                }

                                if (BooleanUtils.isTrue(result)) {
                                    JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "删除成功", Duration.seconds(3));
                                    ActionEvent actionEvent = new ActionEvent(this, delButton);
                                    searchSignalDetail(actionEvent);
                                }
                            });

                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        // 空行或无效数据时不显示按钮
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(delButton);
                            setAlignment(Pos.CENTER);
                        }
                    }
                }
        );
    }

    //------------------------------------------------------------Signal Template--------------------------------------------------------------------------
    public void searchTemplate(ActionEvent actionEvent) {
        String template = nameParam.getText();
        serviceUtils.asyncRun(
                () -> signalTemplateService.searchTemplateByName(template),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }

                    templateNameData.clear();
                    if (result == null) {
                        return;
                    }
                    templateNameData.addAll(result);

                    // 选中模板置为空
                    this.selectedSignalTemplate = null;
                    searchSignalDetail(actionEvent);
                }
        );
    }

    /**
     * 展示SignalTemplate保存界面
     * @param actionEvent
     */
    public void showAddTemplateModal(ActionEvent actionEvent) {
        templateModalType = 1;
        JavaFxUiUtils.showView(templateStackPane, templateModal);
    }

    /**
     *  保存模板名称
     * @param actionEvent
     */
    public void saveTemplate(ActionEvent actionEvent) {
        String templateNameParam = this.templateNameParam.getText();
        SignalTemplate param = new SignalTemplate();
        param.setName(templateNameParam);

        if (this.templateModalType == 2) {
            param.setId(this.editSignalTemplate.getId());
        }

        serviceUtils.asyncRun(
                () -> signalTemplateService.saveTemplate(param),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }

                    searchTemplate(null);
                    JavaFxUiUtils.showView(templateStackPane, templateNameListView);
                    deviceListController.resetSignalTemplate();
                }
        );
    }

    public void hideTemplateModal(ActionEvent actionEvent) {
        JavaFxUiUtils.showView(templateStackPane, templateNameListView);
    }

    //-----------------------------------------------------Signal Template Detail--------------------------------------------------------------------
    /**
     * 查询信号
     * @param actionEvent
     */
    public void searchSignalDetail(ActionEvent actionEvent) {
        searchDetail(selectedSignalTemplate == null ? null : selectedSignalTemplate.getId());
    }

    public void searchDetail(String templateId) {
        if (templateId == null) {
            tableData.clear();
            return;
        }
        String type = typeNameParam.getText();
        String desc = descParam.getText();

        SignalTemplateDetailModel param = new SignalTemplateDetailModel();
        param.setType(type);
        param.setSample(desc);
        if (StringUtils.isNotBlank(templateId)) {
            param.setTemplateId(templateId);
        }

        serviceUtils.asyncRun(
                () -> signalTemplateService.searchDetails(param),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }

                    tableData.clear();
                    if (result == null) {
                        return;
                    }
                    tableData.addAll(result);
                }
        );
    }

    public void showSignalDetailModal(ActionEvent actionEvent) {
        JavaFxUiUtils.showView(templateDetailStackPane, templateDetailModal);
    }

    public void hideSignalDetailModal(ActionEvent actionEvent) {
        JavaFxUiUtils.showView(templateDetailStackPane, tableModal);
    }

    public void saveTemplateDetail(ActionEvent actionEvent) {
        if (this.selectedSignalTemplate == null || this.selectedSignalTemplate.getId() == null) {
            JavaFxUiUtils.alertMessage(Alert.AlertType.WARNING, "先选中模板,再添加明细", Duration.seconds(5));
            JavaFxUiUtils.showView(templateDetailStackPane, templateDetailModal);
            return;
        }

        String type = this.typeName.getText();
        String desc = this.name.getText();
        String sample = this.sample.getText();
        String content = this.content.getText();

        SignalTemplateDetail detail = new SignalTemplateDetail();
        detail.setTemplateId(this.selectedSignalTemplate.getId());
        detail.setType(type);
        detail.setDesc(desc);
        detail.setSample(sample);
        detail.setContent(content);

        serviceUtils.asyncRun(
                () -> signalTemplateService.saveDetail(Collections.singletonList(detail)),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }
                    JavaFxUiUtils.showView(templateDetailStackPane, tableModal);
                    searchSignalDetail(actionEvent);
                    hideSignalDetailModal(actionEvent);
                }
        );
    }

    /**
     * 批量导入模板信号
     * @param actionEvent
     */
    public void importSignalDetail(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("文件列表");
        FileChooser.ExtensionFilter excelExtensionFilter = new FileChooser.ExtensionFilter("Excel Files", "*.xlsx");
        fileChooser.getExtensionFilters().add(excelExtensionFilter);
        File selectedFile = fileChooser.showOpenDialog(this.importButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        serviceUtils.asyncRun(
                () -> signalTemplateService.importDataToDb(selectedFile),
                (result, err) -> {
                    if (err != null) {
                        log.error("信号模板导入异常", err);
                        log.error("信号模板导入异常", err);
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(10));
                        return;
                    }

                    JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "导入成功", Duration.seconds(10));
                    searchTemplate(actionEvent);
                    searchSignalDetail(actionEvent);
                    deviceListController.resetSignalTemplate();
                }
        );
    }

}
