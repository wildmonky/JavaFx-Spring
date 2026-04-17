import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class ExcelUtils {

    private static final String[] props = new String[14];

    static {
        props[0] = "device";
        props[1] = "name";
        props[2] = "rule";
        props[3] = "priority";
        props[4] = "masterMerge";
        props[5] = "upload";
        props[6] = "comment";
        props[7] = "typePattern";
        props[8] = "typePatternMessage";
        props[9] = "rulePattern";
        props[10] = "rulePatternMessage";
        props[11] = "includeDeviceName";
        props[12] = "typeFullTextMatch";
        props[13] = "ruleFullTextMatch";
    }

    public static void main(String[] args){
        //读取excel数据
        List<Map<String,String>> result = ExcelUtils.readExcelToObj("C:\\Users\\15259\\Desktop\\告警信号“一站一册”规范化问题清单梳理工作\\南方电网典型遥信规范表（2023版）.xls", 0,0,null,3);
        for(Map<String,String> map:result){
            System.out.println(map);
        }

    }
    /**
     * 读取excel数据
     * @param path
     */
    public static List<Map<String,String>> readExcelToObj(String path, int startSheetIndex, int endSheetIndex, String[] sheetNames, int startReadLine) {

        Workbook wb;
        List<Map<String,String>> result = null;
        try {
            wb = WorkbookFactory.create(new File(path));
            result = readExcel(wb, startSheetIndex, endSheetIndex, sheetNames, startReadLine, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取excel文件
     * @param wb
     * @param startSheetIndex sheet页下标：从0开始
     * @param endSheetIndex sheet页下标：从0开始
     * @param sheetNames sheet页名称 名称为空时，根据索引读取
     * @param startReadLine 开始读取的行:从0开始
     * @param tailLine 去除最后读取的行
     */
    private static List<Map<String,String>> readExcel(Workbook wb,int startSheetIndex, int endSheetIndex, String[] sheetNames, int startReadLine, int tailLine) {
        List<Sheet> sheets = new ArrayList<>();
        if (sheetNames != null && sheetNames.length > 0) {
            for (String sheetName : sheetNames) {
                sheets.add(wb.getSheet(sheetName));
            }
        } else {
            for (int i = startSheetIndex; i <= endSheetIndex; i++) {
                sheets.add(wb.getSheetAt(i));
            }
        }

        List<Map<String,String>> result = new ArrayList<>();
        int maxColumn = 0;
        Row row;
        for (Sheet sheet : sheets) {
            for(int i=startReadLine; i<sheet.getLastRowNum()-tailLine+1; i++) {
                row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String,String> map = new HashMap<>();
                maxColumn = Math.max(maxColumn, row.getLastCellNum());
                for(int j=0; j< maxColumn; j++) {
                    String returnStr = "";
                    boolean isMerge = isMergedRegion(sheet, i, j);
                    //判断是否具有合并单元格
                    if(isMerge) {
                        //                    System.out.print(rs + "------ ");
                        returnStr = getMergedRegionValue(sheet, row.getRowNum(), j);
                    }else {
                        Cell c = row.getCell(j);
                        if (c != null) {
                            //设置单元格类型
                            c.setCellType(CellType.STRING);
//                    System.out.print(c.getRichStringCellValue()+"++++ ");
                            returnStr = c.getRichStringCellValue().getString();
                        }
                    }

                    if (j >= 14) {
                        System.out.println();
                    }

                    map.put(props[j], returnStr);

                }
                result.add(map);
            }
        }

        return result;

    }

    /**
     * 获取合并单元格的值
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    private static String getMergedRegionValue(Sheet sheet ,int row , int column){
        int sheetMergeCount = sheet.getNumMergedRegions();

        for(int i = 0 ; i < sheetMergeCount ; i++){
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();

            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    Row fRow = sheet.getRow(firstRow);
                    Cell fCell = fRow.getCell(firstColumn);
                    return getCellValue(fCell) ;
                }
            }
        }

        return null ;
    }

    /**
     * 判断合并了行
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    private boolean isMergedRow(Sheet sheet,int row ,int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row == firstRow && row == lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断指定的单元格是否是合并单元格
     * @param sheet
     * @param row 行下标
     * @param column 列下标
     * @return
     */
    private static boolean isMergedRegion(Sheet sheet,int row ,int column) {
        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            if(row >= firstRow && row <= lastRow){
                if(column >= firstColumn && column <= lastColumn){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断sheet页中是否含有合并单元格
     * @param sheet
     * @return
     */
    private boolean hasMerged(Sheet sheet) {
        return sheet.getNumMergedRegions() > 0 ? true : false;
    }

    /**
     * 合并单元格
     * @param sheet
     * @param firstRow 开始行
     * @param lastRow 结束行
     * @param firstCol 开始列
     * @param lastCol 结束列
     */
    private void mergeRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * 获取单元格的值
     * @param cell
     * @return
     */
    private static String getCellValue(Cell cell){

        if(cell == null) return "";

        if(cell.getCellType() == CellType.STRING){

            return cell.getStringCellValue();

        }else if(cell.getCellType() == CellType.BOOLEAN){

            return String.valueOf(cell.getBooleanCellValue());

        }else if(cell.getCellType() == CellType.FORMULA){

            return cell.getCellFormula() ;

        }else if(cell.getCellType() == CellType.NUMERIC){

            return String.valueOf(cell.getNumericCellValue());

        }
        return "";
    }
    /**
     * 从excel读取内容
     */
    public static void readContent(String fileName)  {
        boolean isE2007 = false;    //判断是否是excel2007格式
        if(fileName.endsWith("xlsx"))
            isE2007 = true;
        try {
            InputStream input = new FileInputStream(fileName);  //建立输入流
            Workbook wb  = null;
            //根据文件格式(2003或者2007)来初始化
            if(isE2007)
                wb = new XSSFWorkbook(input);
            else
                wb = new HSSFWorkbook(input);
            Sheet sheet = wb.getSheetAt(0);     //获得第一个表单
            Iterator<Row> rows = sheet.rowIterator(); //获得第一个表单的迭代器
            while (rows.hasNext()) {
                Row row = rows.next();  //获得行数据
                System.out.println("Row #" + row.getRowNum());  //获得行号从0开始
                Iterator<Cell> cells = row.cellIterator();    //获得第一行的迭代器
                while (cells.hasNext()) {
                    Cell cell = cells.next();
                    System.out.println("Cell #" + cell.getColumnIndex());
                    switch (cell.getCellType()) {   //根据cell中的类型来输出数据
                        case NUMERIC:
                            System.out.println(cell.getNumericCellValue());
                            break;
                        case STRING:
                            System.out.println(cell.getStringCellValue());
                            break;
                        case BOOLEAN:
                            System.out.println(cell.getBooleanCellValue());
                            break;
                        case FORMULA:
                            System.out.println(cell.getCellFormula());
                            break;
                        default:
                            System.out.println("unsuported sell type======="+cell.getCellType());
                            break;
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}

