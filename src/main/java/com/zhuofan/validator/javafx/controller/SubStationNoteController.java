package com.zhuofan.validator.javafx.controller;

import atlantafx.base.theme.Styles;
import com.zhuofan.validator.javafx.utils.JavaFxServiceUtils;
import com.zhuofan.validator.javafx.utils.JavaFxUiUtils;
import com.zhuofan.validator.spring.service.SubstationNoteService;
import com.zhuofan.validator.spring.entity.SubstationNote;
import com.zhuofan.validator.spring.model.SubstationModel;
import com.zhuofan.validator.spring.model.SubstationNoteModel;
import jakarta.annotation.Resource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Slf4j
@Controller
public class SubStationNoteController extends Control {

    // 站点列表
    private ObservableList<SubstationModel> substationData;

    // 查询参数
    public ComboBox<SubstationModel> substationParam;
    public DatePicker dateParam;
    public ComboBox<String> statusParam;
    public TextField contentParam;

    // 查询按钮
    public Button searchButton;
    public Button showAddModalButton;

    // 表格数据
    private final ObservableList<SubstationNoteModel> tableData = FXCollections.observableArrayList();
    // 表格
    public TableView<SubstationNoteModel> table;
    public TableColumn<SubstationNoteModel, String> operationCol;
    public TableColumn<SubstationNoteModel, String> substationCol;
    public TableColumn<SubstationNoteModel, String> planDateCol;
    public TableColumn<SubstationNoteModel, String> statusCol;
    public TableColumn<SubstationNoteModel, String> contentCol;
    public TableColumn<SubstationNoteModel, String> commentCol;

    // 根容器
    public StackPane rootPane;
    // 表格容器
    public ScrollPane tablePane;
    // 弹出框容器
    public VBox modalPane;
    // 弹出框填写内容
    public DatePicker dateModal;
    public TextArea contentModal;
    public ComboBox<SubstationModel> substationModal;
    public ComboBox<String> statusModal;
    public TextArea commentModal;

    /**
     * 0-未显示;1-新增;2-修改
     */
    private int modalType = 0;
    private SubstationNote editData = null;

    @Setter
    private ProgressIndicator progressIndicator;

    @Resource
    private JavaFxServiceUtils serviceUtils;

    @Resource
    private SubstationNoteService substationNoteService;

    private final ObservableList<String> STATUS_LIST =  FXCollections.observableArrayList("待办", "已完成", "已取消", "已超时");

    /**
     * 初始化
     */
    @FXML
    public void initialize() {
        // 时间控件格式
        this.dateParam.setConverter(dateConverter());
        this.dateModal.setConverter(dateConverter());

        this.contentModal.setWrapText(true);
        this.commentModal.setWrapText(true);

        resetSearchParam();
        initTableCol();

        search(new ActionEvent(this, this));
    }

    /**
     * 查询参数设置
     */
    public void resetSearchParam() {
        // 变电站数据
        // modal 设置
        substationData = FXCollections.observableArrayList();
//        substationData = JavaFxUiUtils.comoBoxBindFilter(
//                substationParam,
//                (m, text) -> {
//                    if (StringUtils.isBlank(text)) {
//                        return true;
//                    }
//
//                    if (m == null) {
//                        return false;
//                    }
//
//                    if (m.getDisplayName() == null) {
//                        return false;
//                    }
//
//                    return m.getDisplayName().contains(text);
//                },
//                (m, text) -> {
//                    if (m == null) {
//                        return false;
//                    }
//
//                    return Objects.equals(m.getDisplayName(), text);
//                }
//        );
        this.substationParam.setItems(substationData);
//        this.substationParam.selectionModelProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal == null) {
//                substationParam.getSelectionModel().clearSelection();
//            }
//        });
        this.substationParam.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SubstationModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        this.substationParam.setConverter(substationConverter());

        this.substationModal.setItems(substationData);
        this.substationModal.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SubstationModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName());
            }
        });
        this.substationModal.setConverter(substationConverter());
        searchSubstationData(null);

        this.statusParam.setItems(STATUS_LIST);
        this.statusModal.setItems(STATUS_LIST);
    }

    public void initTableCol() {
        // 表格初始化
        this.table.setFixedCellSize(-1);
        this.table.setEditable(false);
        // 表格绑定数据
        this.table.setItems(tableData);

        this.substationCol.setCellValueFactory(cellData -> {
            SubstationNoteModel value = cellData.getValue();
            return value == null ? null : new SimpleStringProperty(value.getCategory());
        });

        this.statusCol.setCellValueFactory(cellData -> {
            SubstationNote value = cellData.getValue();
            return value == null ? null : new SimpleStringProperty(value.getStatus());
        });

        this.planDateCol.setCellValueFactory(cellData -> {
            SubstationNoteModel value = cellData.getValue();
            if (value == null) {
                return null;
            }

            LocalDate planDate = value.getPlanDate();
            String dateStr;
            try {
                dateStr = planDate == null ? null : planDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeException e) {
                dateStr = null;
            }

            return new SimpleStringProperty(dateStr);
        });

        this.contentCol.setCellValueFactory(cellData -> {
            SubstationNote value = cellData.getValue();
            return value == null ? null : new SimpleStringProperty(value.getContent());
        });

        this.commentCol.setCellValueFactory(cellData -> {
            SubstationNote value = cellData.getValue();
            return value == null ? null : new SimpleStringProperty(value.getComment());
        });

        this.operationCol.setCellFactory(col ->
                new TableCell<>() {
                    private final HBox btnBar = new HBox(5);
                    private final Button delButton = new Button("删除");
                    private final Button editButton = new Button("编辑");

                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        setAlignment(Pos.CENTER);
                        btnBar.setAlignment(Pos.CENTER);
                        btnBar.getChildren().addAll(delButton, editButton);

                        delButton.getStyleClass().addAll(Styles.SMALL, Styles.DANGER, Styles.BUTTON_OUTLINED);
                        delButton.setOnAction(event -> {
                            // 获取被点击按钮所在的当前TreeItem对象
                            SubstationNote prop = getTableRow().getItem();
                            if (prop == null || StringUtils.isBlank(prop.getId())) {
                                JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "空数据", Duration.seconds(3));
                                return;
                            }

                            serviceUtils.asyncRun(() -> {
                                SubstationNote note = new SubstationNote();
                                note.setId(prop.getId());
                                return substationNoteService.removeSubstationNote(note);
                            }, (result, err) -> {
                                if (err != null) {
                                    JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(3));
                                    return;
                                }

                                if (BooleanUtils.isTrue(result)) {
                                    JavaFxUiUtils.alertMessage(Alert.AlertType.INFORMATION, "删除成功", Duration.seconds(3));
                                    ActionEvent actionEvent = new ActionEvent(this, delButton);
                                    search(actionEvent);
                                }
                            });

                        });

                        editButton.getStyleClass().addAll(Styles.SMALL, Styles.ACCENT);
                        editButton.setOnAction(event -> {
                            SubstationNoteModel item = getTableRow().getItem();
                            showEditModal(item);
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
                            setGraphic(btnBar);
                            setAlignment(Pos.CENTER);
                        }
                    }
                }
        );
    }

    public void showAddModal(ActionEvent event) {
        setModalData(null);
        this.modalType = 1;
        JavaFxUiUtils.showView(rootPane, modalPane);
    }

    public void hideModal(ActionEvent event) {
        this.modalType = 0;
        JavaFxUiUtils.showView(rootPane, tablePane);
    }

    public void search(ActionEvent event) {
        searchNotes(getSearchParam());
    }

    public void save(ActionEvent event) {
        SubstationModel substation = this.substationModal.getSelectionModel().getSelectedItem();
        String status = this.statusModal.getSelectionModel().getSelectedItem();
        LocalDate planDate = this.dateModal.getValue();
        String content = this.contentModal.getText();
        String comment = this.commentModal.getText();

        SubstationNote note = new SubstationNote();
        note.setCategory(substation == null ? null : substation.getDisplayName());
        note.setPlanDate(planDate);
        note.setContent(content);
        note.setStatus(status);
        note.setComment(comment);

        if (modalType == 2) {
            if (editData == null) {
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "编辑对象为空", Duration.seconds(5));
                return;
            }
            note.setId(editData.getId());
        }

        saveNote(note);
    }


    //------------------------------------------------------------Parent Node: Stations--------------------------------------------------------------------------

    /**
     * @Return 可编辑 ComboBox 选中项与编辑器文本的转换规则 {@link substationParam} {@link substationModal}
     */
    private StringConverter<SubstationModel> substationConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SubstationModel model) {
                return model == null ? "" : model.getDisplayName();
            }

            @Override
            public SubstationModel fromString(String text) {
                if (StringUtils.isBlank(text)) {
                    return null;
                }
                return substationData.stream()
                        .filter(item -> text.equals(item.getName()) || text.equals(item.getDisplayName()))
                        .findFirst()
                        .orElse(null);
            }
        };
    }

    /**
     * @return 日期转换 {@link #dateParam} {@link #dateModal}
     */
    private StringConverter<LocalDate> dateConverter() {
        return new StringConverter<>() {

            @Override
            public String toString(LocalDate object) {
                return object == null ? "" : object.format(DateTimeFormatter.ISO_LOCAL_DATE);
            }

            @Override
            public LocalDate fromString(String string) {
                try {
                    return LocalDate.parse(string, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
    }

    public void showEditModal(SubstationNote note) {
        this.editData = note;
        this.modalType = 2;
        setModalData(note);
        JavaFxUiUtils.showView(rootPane, modalPane);
    }

    /**
     * 获取查询参数
     * @return 查询参数
     */
    public SubstationNote getSearchParam() {
        SubstationModel substation = this.substationParam.getSelectionModel().getSelectedItem();
        LocalDate date = this.dateParam.getValue();
        String status = this.statusParam.getSelectionModel().getSelectedItem();
        String content = this.contentParam.getText();

        SubstationNote note = new SubstationNote();
        note.setCategory(substation == null ? null : substation.getDisplayName());
        note.setPlanDate(date);
        note.setStatus(status);
        note.setContent(content);
        return note;
    }

    public void setModalData(SubstationNote note) {
        if (note == null) {
            this.substationModal.getSelectionModel().clearSelection();
            this.statusModal.getSelectionModel().select("待办");
            this.dateModal.setValue(null);
            this.contentModal.setText(null);
            this.commentModal.setText(null);
            return;
        }

        if (StringUtils.isBlank(note.getCategory())) {
            this.substationModal.getSelectionModel().clearSelection();
        } else {
            SubstationModel substation = substationData.stream()
                    .filter(e -> Objects.equals(e.getDisplayName(), note.getCategory()))
                    .findFirst().orElse(null);

            this.substationModal.getSelectionModel().select(substation);
        }

        this.statusModal.getSelectionModel().select(note.getStatus());
        this.dateModal.setValue(note.getPlanDate());
        this.contentModal.setText(note.getContent());
        this.commentModal.setText(note.getComment());
    }

    public void searchSubstationData(SubstationModel param) {
        serviceUtils.asyncRun(
                () -> substationNoteService.searchSubstations(param),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }
                    substationData.clear();
                    if (result == null) {
                        return;
                    }
                    substationData.addAll(result);
                    // 选中模板置为空
                    this.editData = null;
                }
        );
    }

    public void searchNotes(SubstationNote note) {
        serviceUtils.asyncRun(
                () -> substationNoteService.searchSubstationNotes(new SubstationNoteModel(note)),
                (result, err) -> {
                    if (err != null) {
                        log.error("", err);
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }

                    tableData.clear();
                    if (result != null) {
                        tableData.setAll(result);
                    }
                    JavaFxUiUtils.showView(rootPane, tablePane);
                }
        );
    }

    /**
     *  保存站点备忘名称
     */
    public void saveNote(SubstationNote data) {
        serviceUtils.asyncRun(
                () -> substationNoteService.saveSubstationNote(data),
                (result, err) -> {
                    if (err != null) {
                        JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, err.getMessage(), Duration.seconds(5));
                        return;
                    }

                    JavaFxUiUtils.showView(rootPane, tablePane);
                    this.modalType = 0;
                    search(new ActionEvent(this, this));
                }
        );
    }

}
