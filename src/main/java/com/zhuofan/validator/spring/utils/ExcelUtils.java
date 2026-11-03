package com.zhuofan.validator.spring.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

public class ExcelUtils {

    /**
     * 估算并设置自适应行高
     *
     * @param sheet      当前工作表
     * @param row        当前行
     * @param content    单元格文本内容
     * @param columnIndex 列索引（用于获取列宽）
     * @param font       字体信息（获取字体大小）
     */
    public static void autoFitRowHeight(Sheet sheet, Row row, String content, int columnIndex, Font font) {
        if (content == null || content.isEmpty()) {
            row.setHeightInPoints(15); // 默认高度
            return;
        }

        // 1. 获取字体大小 (Points)
        // 如果 font 为空，默认为 11 号字
        short fontHeightInPoints = font != null ? font.getFontHeightInPoints() : 11;

        // 2. 计算单行基础高度 (含少量行间距)
        // 经验值：字体大小 * 1.3 左右
        float baseLineHeight = fontHeightInPoints * 1.3f;

        // 3. 获取列宽并转换为“标准字符数”
        // POI默认：1个英文字符 = 256单位，1个中文字符 = 512单位
        int colWidthUnits = getColumnWidth(sheet, row, columnIndex);
        // 减去一点 padding (比如留出 1.5 个字符的宽度防止贴边)
        float maxCharsPerLine = (colWidthUnits / 256.0f) - 1.5f;
        if (maxCharsPerLine <= 1) maxCharsPerLine = 1; // 防止极端列宽

        // 4. 计算视觉总行数
        int totalVisualLines = 0;

        // 4.1 按换行符拆分
        String[] paragraphs = content.split("\n", -1); // -1 保证末尾的空行也被计算

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                totalVisualLines += 1; // 遇到空行，直接加1
                continue;
            }

            // 4.2 计算该段落的“等效字符宽度”（中文=2，英文=1）
            int effectiveWidth = 0;
            for (char c : paragraph.toCharArray()) {
                // 判断是否为中文字符或全角字符
                if (isChinese(c)) {
                    effectiveWidth += 2;
                } else {
                    effectiveWidth += 1;
                }
            }

            // 4.3 计算该段落折行后的视觉行数
            int linesForParagraph = (int) Math.ceil(effectiveWidth / maxCharsPerLine);
            totalVisualLines += Math.max(1, linesForParagraph);
        }

        // 5. 计算最终高度并设置 (添加 3 磅的上下留白)
        float finalHeight = (totalVisualLines * baseLineHeight) + 3.0f;

        // POI 行高上限是 409 磅，防止异常数据撑爆
        row.setHeightInPoints(Math.min(finalHeight, 409f));
    }

    /**
     * 粗略判断字符是否为中文（或全角符号）
     */
    private static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5 ||
                c >= 0x3000 && c <= 0x303F || // 中文标点
                c >= 0xFF00 && c <= 0xFFEF;   // 全角字符
    }

    private static int getColumnWidth(Sheet sheet, Row row, int columnIndex) {
        List<CellRangeAddress> mergeRanges = sheet.getMergedRegions().stream()
                .filter(e -> e.getFirstRow() <= row.getRowNum() && e.getLastRow() >= row.getRowNum()
                        && e.getFirstColumn() <= columnIndex && e.getLastColumn() >= columnIndex
                ).toList();
        if (CollectionUtils.isEmpty(mergeRanges)) {
            return sheet.getColumnWidth(columnIndex);
        }

        CellRangeAddress range = mergeRanges.get(0);
        int columnWidthCount = 0;
        for (int i = range.getFirstColumn(); i <= range.getLastColumn() ; i++) {
            columnWidthCount += sheet.getColumnWidth(i);
        }
        return columnWidthCount;
    }

}
