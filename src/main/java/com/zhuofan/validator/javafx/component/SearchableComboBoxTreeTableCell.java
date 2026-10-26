package com.zhuofan.validator.javafx.component;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeTableCell;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SearchableComboBoxTreeTableCell<R, N, S> extends TreeTableCell<R, N> {
    private SearchableComboBox<N> searchableComboBox;

    private Function<N, S> getSelectorDisplayText;

    // 根据行数据获取对应的下拉数据项
    private BiFunction<R, List<N>, N> selectByRowData;

    private ObservableList<N> allItems;

    public SearchableComboBoxTreeTableCell() {}

    public SearchableComboBoxTreeTableCell(BiFunction<R, List<N>, N> selectByRowData, Function<N,S> getSelectorDisplayText, ObservableList<N> items) {
        this.selectByRowData = selectByRowData;
        this.getSelectorDisplayText = getSelectorDisplayText;
        this.searchableComboBox = new SearchableComboBox<>(items);
        this.allItems = items;

        searchableComboBox.setConverter(buildStringConverter());
        // 根据需求设置 ComboBox 的尺寸
        searchableComboBox.setPrefWidth(150);
        searchableComboBox.editableProperty().bindBidirectional(editableProperty());

        // 监听选中值变化，写回数据模型
        searchableComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (getTableRow() != null && getTableRow().getItem() != null) {
                // 假设你的数据模型有一个 setter 方法
                commitEdit(newVal); // 如果使用编辑模式，可以调用 commitEdit
                // 或者在始终显示模式下，直接更新数据模型：
                // ((MyDataModel) getTableRow().getItem()).setSomeProperty(newVal);
            }
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
//        N item = getItem();
//        if (item != null) {
//            S displayText = getSelectorDisplayText.apply(getItem());
//            searchableComboBox.getEditor().setText(displayText == null ? null : displayText.toString());
//        }
        setText(null);
        setGraphic(searchableComboBox);
    }

    @Override
    protected void updateItem(N item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            // 重要：避免在单元格复用过程中触发不必要的事件
            searchableComboBox.setValue(item);

            if (isEditing()) {
                setGraphic(searchableComboBox);
                setText(null);
                return;
            }

            N selectedData = getSelectedByRowData();
            if (selectedData == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            S displayText = getSelectorDisplayText.apply(selectedData);
            setText(displayText == null ? null : displayText.toString());
            setGraphic(null);
        }
    }

    public N getSelectedByRowData() {
        return this.selectByRowData.apply(getTableRow().getItem(), allItems);
    }

    public StringConverter<N> buildStringConverter() {
        return new StringConverter<>() {

            @Override public String toString(N t) {
                if (t == null) {return null;}
                S displayText = getSelectorDisplayText.apply(t);
                return displayText == null ? null : displayText.toString();
            }

            // 不手动编辑，无需实现
            @Override public N fromString(String string) {
                return null;
            }
        };
    }

}
