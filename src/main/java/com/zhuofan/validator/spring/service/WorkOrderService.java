package com.zhuofan.validator.spring.service;

import com.zhuofan.validator.spring.excel.WorkOrderCellWriteHandler;
import com.zhuofan.validator.spring.model.AutomaticMaintenanceWorkOrderModel;
import com.zhuofan.validator.spring.utils.ExcelUtils;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class WorkOrderService {

    List<AutoHeight<AutomaticMaintenanceWorkOrderModel>> rowIndexAndColumnIndexList = new ArrayList<>();

    @PostConstruct
    public void postConstruct() {
        // 工作内容
        rowIndexAndColumnIndexList.add(AutoHeight.of(10, 2, AutomaticMaintenanceWorkOrderModel::getWorkContent));
        // 安自要求
        rowIndexAndColumnIndexList.add(AutoHeight.of(14, 3, AutomaticMaintenanceWorkOrderModel::getAzRequirement));
        // 保护要求
        rowIndexAndColumnIndexList.add(AutoHeight.of(15, 3, AutomaticMaintenanceWorkOrderModel::getProtectRequirement));
        // 通信要求
        rowIndexAndColumnIndexList.add(AutoHeight.of(16, 3, AutomaticMaintenanceWorkOrderModel::getCommunicateRequirement));
        // 自动化要求
        rowIndexAndColumnIndexList.add(AutoHeight.of(17, 3, AutomaticMaintenanceWorkOrderModel::getAutomaticRequirement));
        // 其它要求
        rowIndexAndColumnIndexList.add(AutoHeight.of(18, 3, AutomaticMaintenanceWorkOrderModel::getOtherRequirement));
        // 是否影响自动化
        rowIndexAndColumnIndexList.add(AutoHeight.of(20, 3, AutomaticMaintenanceWorkOrderModel::getIsImpactAuto));
        // 自动化并网新设备
        rowIndexAndColumnIndexList.add(AutoHeight.of(22, 3, AutomaticMaintenanceWorkOrderModel::getAutoNewEquip));
        // 自动化定检设备
        rowIndexAndColumnIndexList.add(AutoHeight.of(23, 3, AutomaticMaintenanceWorkOrderModel::getAutoMaintenanceEquip));
        // 自动化反措设备
        rowIndexAndColumnIndexList.add(AutoHeight.of(24, 3, AutomaticMaintenanceWorkOrderModel::getAutoAntiAccidentEquip));
        // 自动化消缺设备
        rowIndexAndColumnIndexList.add(AutoHeight.of(25, 3, AutomaticMaintenanceWorkOrderModel::getAutoEliminationEquip));
        // 涉及供电可靠性的影响
        rowIndexAndColumnIndexList.add(AutoHeight.of(26, 3, AutomaticMaintenanceWorkOrderModel::getImpact));
        // 其他的影响
        rowIndexAndColumnIndexList.add(AutoHeight.of(27, 3, AutomaticMaintenanceWorkOrderModel::getOtherImpact));
        // 申请备注
        rowIndexAndColumnIndexList.add(AutoHeight.of(29, 3, AutomaticMaintenanceWorkOrderModel::getComment));
    }

    public void export(AutomaticMaintenanceWorkOrderModel model, OutputStream outputStream) {
        ClassPathResource template = new ClassPathResource("/exportTemplate/work_order.xlsx");

        try {
            // 1.创建临时文件
            Path tempFilePath = Files.createTempFile("test", ".xlsx");
            File tempFile = tempFilePath.toFile();

            FesodSheet.write(tempFile)
                    .withTemplate(template.getInputStream())
                    .registerWriteHandler(new WorkOrderCellWriteHandler())
                    .sheet()
                    .doFill(model);

            // 2.加载已存在的 Excel 文件
            int rowIndex = 28, colIndex = 3;
            try (FileInputStream fis = new FileInputStream(tempFile);
                 XSSFWorkbook workbook = new XSSFWorkbook(fis)) {


                // 获取目标工作表和单元格
                XSSFSheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    throw new IllegalArgumentException("Sheet not found at index: 0");
                }

                // 设置附件
                if (model.getFile() != null) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        row = sheet.createRow(rowIndex);
                    }
                    Cell cell = row.getCell(colIndex);
                    if (cell == null) {
                        cell = row.createCell(colIndex);
                    }

                    // 3. 读取要嵌入的文件的二进制数据
                    File embedFile = model.getFile();
                    byte[] fileBytes = IOUtils.toByteArray(new FileInputStream(embedFile));

                    // 4. 使用 POI 的 addOlePackage 将文件嵌入工作簿
                    // 参数: 文件字节数组, 显示标签, 文件名, 命令/ProgID
                    int oleIndex = workbook.addOlePackage(
                            fileBytes,
                            embedFile.getName(),       // 单元格中显示的图标下方文字
                            embedFile.getName(),       // 嵌入对象的文件名
                            embedFile.getName()        // 用于打开文件的命令（通常与文件名相同）
                    );

                    // 5. 创建绘图对象，将 OLE 对象锚定到目标单元格
                    XSSFDrawing drawing = sheet.createDrawingPatriarch();
                    // 创建一个锚点，指定 OLE 对象的位置和大小
                    XSSFClientAnchor anchor = new XSSFClientAnchor(
                            0, 0, 0, 0,
                            colIndex, rowIndex,
                            colIndex + 1, rowIndex + 1
                    );
                    int pictureIndex = registerAttachmentIcon(workbook, embedFile);
                    // 创建 OLE 形状并连接到已嵌入的 OLE 包
                    drawing.createObjectData(anchor, oleIndex, pictureIndex);
                }

                // 设置行高
                for (AutoHeight<AutomaticMaintenanceWorkOrderModel> data : rowIndexAndColumnIndexList) {
                    int autoHeightRowIndex = data.getRowIndex();
                    int autoHeightColumnIndex = data.getColumnIndex();
                    Function<AutomaticMaintenanceWorkOrderModel, String> contentFunc = data.getContentFunc();
                    String content = null;
                    if (contentFunc != null) {
                        content = contentFunc.apply(model);
                    }
                    XSSFRow autoHeightRow = sheet.getRow(autoHeightRowIndex);
                    XSSFCell autoHeightRowCell = autoHeightRow.getCell(autoHeightColumnIndex);
                    XSSFFont font = workbook.getFontAt(autoHeightRowCell.getCellStyle().getFontIndex());
                    ExcelUtils.autoFitRowHeight(sheet, autoHeightRow, content, autoHeightColumnIndex, font);
                }

                // 将修改后的工作簿写回文件
                workbook.write(outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * 把工作簿附件的预览图标注册进 POI 图片集合
     *
     * @return pictureIndex, 供 XSSFDrawing.createObjectData 使用
     */
    private int registerAttachmentIcon(XSSFWorkbook workbook, File embedFile) throws IOException {
        byte[] iconBytes = this.loadFileAssociationIcon(embedFile);
        if (iconBytes == null) {
            // 回落: 使用内置图标资源
            URL iconUrl = WorkOrderService.class.getResource("/lizi.png");
            if (iconUrl == null) {
                throw new IOException("未找到附件图标资源 /lizi.png, 无法为OLE对象生成预览图标");
            }
            try (InputStream in = iconUrl.openStream()) {
                iconBytes = IOUtils.toByteArray(in);
            }
        }
        // XSSF 用 PNG 兼容性最好
        return workbook.addPicture(iconBytes, Workbook.PICTURE_TYPE_PNG);
    }

    /**
     * 取操作系统中该文件类型关联的图标(如 pdf/zip/docx 各自不同), 转成 PNG 字节
     */
    private byte[] loadFileAssociationIcon(File file) {
        try {
            javax.swing.Icon icon = javax.swing.filechooser.FileSystemView
                    .getFileSystemView().getSystemIcon(file);
            if (icon == null) {
                return null;
            }

            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            if (width <= 0 || height <= 0) {
                return null;
            }

            // 系统默认图标一般只有 16x16, 放大到 48x48, 避免 Excel 里显示模糊
            int target = 48;
            BufferedImage image = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            try {
                AffineTransform scale = AffineTransform.getScaleInstance(
                        target / (double) width,
                        target / (double) height
                );
                g2d.drawImage(toBufferedImage(icon, width, height), scale, null);
            } finally {
                g2d.dispose();
            }

            ByteArrayOutputStream iconOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", iconOut);
            return iconOut.toByteArray();
        } catch (Exception e) {
            log.warn("获取系统文件图标失败, 将使用内置图标: {}", file.getName(), e);
            return null;
        }
    }

    private BufferedImage toBufferedImage(javax.swing.Icon icon, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        try {
            icon.paintIcon(null, g2d, 0, 0);
        } finally {
            g2d.dispose();
        }
        return image;
    }

    @Getter
    @Setter
    public static class AutoHeight<T>{

        private int rowIndex;

        private int columnIndex;

        private Function<T, String> contentFunc;

        public static <T> AutoHeight<T> of(int rowIndex, int columnIndex, Function<T, String> getContent) {
            AutoHeight<T> autoHeight = new AutoHeight<>();
            autoHeight.setRowIndex(rowIndex);
            autoHeight.setColumnIndex(columnIndex);
            autoHeight.setContentFunc(getContent);
            return autoHeight;
        }

    }

}
