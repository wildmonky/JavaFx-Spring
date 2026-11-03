package com.zhuofan.validator.spring.excel;

import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.write.handler.CellWriteHandler;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * WorkOrder 单元格写处理器
 */
public class WorkOrderCellWriteHandler implements CellWriteHandler {

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        if (isHead) return; // 只处理内容行

        if (cell == null) {
            return;
        }

        String cellValue = cell.getStringCellValue();
    }

}
