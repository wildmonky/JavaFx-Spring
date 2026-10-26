package com.zhuofan.validator.javafx.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.util.StringConverter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 表格 选择单元格
 *
 * @param <S> 行数据
 * @param <T> ComboBox中数据类型
 * @param <P> id 比较的数据类型，判断是否选中，数据库保存的value的数据类型
 */
public class SelectorTreeTableCell<S, T, P> extends ComboBoxTreeTableCell<S,T>{

    /**
     * 选择框展示数据获取方法
     * 默认展示文本是String类型
     */
    @Setter
    private Function<T, String> getSelectorDisplayText;

    /**
     * 行数据中对应选择框数据中值的获取方法
     */
    @Setter
    private Function<S, P> getRowKey;

    /**
     * 选择框中值获取方法
     */
    @Setter
    private Function<T, P> getSelectorCandidateKey;

    public SelectorTreeTableCell() {
        super();
    }

    public SelectorTreeTableCell(StringConverter<T> converter) {
        super();
        setConverter(converter);
    }

    public SelectorTreeTableCell(Function<S, P> getRowKey,
                                 Function<T, P> getSelectorCandidateKey,
                                 Function<T, String> getSelectorDisplayText,
                                 List<T> items
    ) {
        super(FXCollections.observableList(items));
        this.getRowKey = getRowKey;
        this.getSelectorCandidateKey = getSelectorCandidateKey;
        this.getSelectorDisplayText = getSelectorDisplayText;
        setConverter(buildStringConverter());
    }

    @Override
    public void startEdit() {
        super.startEdit();
        // 重新设置选中项
        ComboBox<T> comboBox = (ComboBox<T>) getGraphic();
        ObservableList<T> items = getItems();
        TreeTableRow<S> row = getTableRow();
        S rowData = row.getItem();
        T selected = dataInSelector(rowData, items);
        comboBox.getSelectionModel().select(selected);
    }

    public T dataInSelector(S row, List<T> candidates) {
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        for (T candidate : candidates) {
            if (Objects.equals(getRowKey.apply(row), getSelectorCandidateKey.apply(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        Cell<T> cell = this;
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                ComboBox<T> comboBox = (ComboBox<T>) getGraphic();
                if (comboBox != null) {
                    comboBox.getSelectionModel().select(cell.getItem());
                }
                cell.setText(null);
                cell.setGraphic(comboBox);
            } else {
                cell.setText(item == null ? "" : getSelectorDisplayText.apply(item));
                cell.setGraphic(null);
            }
        }
    }

    public StringConverter<T> buildStringConverter() {
        return new StringConverter<>() {

            @Override public String toString(T t) {
                return t == null ? null : getSelectorDisplayText.apply(t);
            }

            // 不手动编辑，无需实现
            @Override public T fromString(String string) {
                return null;
            }
        };
    }

}
