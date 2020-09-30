package com.zys.springbootuploaddownload.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zys.springbootuploaddownload.entity.ExcelData;
import com.zys.springbootuploaddownload.entity.Student;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author: yushizhong
 * @Date: 2020/2/22 12:07
 * @Title: 解析excel内容+导出excel
 * 解析excel：调用readExcel方法
 * 导出excel：浏览器下载调用exportExcel方法
 * 本地下载调用generateExcel方法
 */
public class ExcelUtil {

    public final static String XLSX = ".xlsx";
    public final static String XLS = ".xls";


    /**
     * 获取Excel文件（.xls和.xlsx都支持）
     *
     * @param path 文件全路径
     * @return 解析excle后的Json数据
     */
    public static JSONArray readExcel(String path, Map<String, String> myMap) throws Exception {
        File file = new File(path);
        int res = checkFile(file);
        if (res == 0) {
            System.out.println("File not found");
        } else if (res == 1) {
            return readXLSX(file, myMap);
        } else if (res == 2) {
            return readXLS(file, myMap);
        }
        JSONArray array = new JSONArray();
        return array;
    }

    /**
     * 判断File文件的类型
     *
     * @param file 传入的文件
     * @return 0-文件为空，1-XLSX文件，2-XLS文件，3-其他文件
     */
    public static int checkFile(File file) {
        if (file == null) {
            return 0;
        }
        String fileName = file.getName();
        if (fileName.endsWith(XLSX)) {
            return 1;
        }
        if (fileName.endsWith(XLS)) {
            return 2;
        }
        return 3;
    }

    /**
     * 读取XLSX文件
     *
     * @param file
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static JSONArray readXLSX(File file, Map<String, String> myMap) throws IOException, InvalidFormatException {
        Workbook book = new XSSFWorkbook(file);
        //只读第一个sheet表内容
        Sheet sheet = book.getSheetAt(0);
        return read(sheet, book, myMap);
    }

    /**
     * 读取XLS文件
     *
     * @param file 文件对象
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static JSONArray readXLS(File file, Map<String, String> myMap) throws FileNotFoundException, IOException {
        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(new FileInputStream(file));
        Workbook book = new HSSFWorkbook(poifsFileSystem);
        //只读第一个sheet表内容
        Sheet sheet = book.getSheetAt(0);
        return read(sheet, book, myMap);
    }

    /**
     * 解析数据
     *
     * @param sheet 表格sheet对象
     * @param book  用于流关闭
     * @param myMap excel列名替换的值
     * @return
     * @throws IOException
     */
    public static JSONArray read(Sheet sheet, Workbook book, Map<String, String> myMap) throws IOException {
        int rowStart = sheet.getFirstRowNum();    // 首行下标
        int rowEnd = sheet.getLastRowNum();    // 尾行下标
        // 如果首行与尾行相同，表明只有一行，直接返回空数组
        if (rowStart == rowEnd) {
            book.close();
            return new JSONArray();
        }
        // 获取第一行JSON对象键
        Row firstRow = sheet.getRow(rowStart);
        int cellStart = firstRow.getFirstCellNum();
        int cellEnd = firstRow.getLastCellNum();
        Map<Integer, String> keyMap = new HashMap<Integer, String>();
        for (int j = cellStart; j < cellEnd; j++) {
            keyMap.put(j, getValue(firstRow.getCell(j), rowStart, j, book, true));
        }
        keyMap = replaceKey(keyMap, myMap);
        // 获取每行JSON对象的值
        JSONArray array = new JSONArray();
        for (int i = rowStart + 1; i <= rowEnd; i++) {
            Row eachRow = sheet.getRow(i);
            JSONObject obj = new JSONObject();
            StringBuffer sb = new StringBuffer();
            for (int k = cellStart; k < cellEnd; k++) {
                if (eachRow != null) {
                    String val = getValue(eachRow.getCell(k), i, k, book, false);
                    sb.append(val);        // 所有数据添加到里面，用于判断该行是否为空
                    obj.put(keyMap.get(k), val);
                }
            }
            if (sb.toString().length() > 0) {
                array.add(obj);
            }
        }
        book.close();
        return array;
    }

    /**
     * 获取每个单元格的数据
     *
     * @param cell   单元格对象
     * @param rowNum 第几行
     * @param index  该行第几个
     * @param book   主要用于关闭流
     * @param isKey  是否为键：true-是，false-不是。 如果解析Json键，值为空时报错；如果不是Json键，值为空不报错
     * @return
     * @throws IOException
     */
    public static String getValue(Cell cell, int rowNum, int index, Workbook book, boolean isKey) throws IOException {
        // 空白或空
        if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            if (isKey) {
                book.close();
                throw new NullPointerException(String.format("the key on row %s index %s is null ", ++rowNum, ++index));
            } else {
                return "";
            }
        }
        // 0. 数字 类型
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return df.format(date);
            }
            //防止当作数字而导致最后的0丢失
            DecimalFormat df = new DecimalFormat("0");
            String val = df.format(cell.getNumericCellValue());
            val = val.toUpperCase();
            if (val.contains("E")) {
                val = val.split("E")[0].replace(".", "");
            }
            return val;
        }
        // 1. String类型
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            String val = cell.getStringCellValue();
            if (val == null || val.trim().length() == 0) {
                if (book != null) {
                    book.close();
                }
                return "";
            }
            return val.trim();
        }
        // 2. 公式 CELL_TYPE_FORMULA
        if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            return cell.getStringCellValue();
        }
        // 4. 布尔值 CELL_TYPE_BOOLEAN
        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return cell.getBooleanCellValue() + "";
        }
        // 5.	错误 CELL_TYPE_ERROR
        return "";
    }

    /**
     * 替换读取的数据key
     *
     * @param oldMap 原始map
     * @param myMap  要替换的value map
     * @return
     */
    public static Map<Integer, String> replaceKey(Map<Integer, String> oldMap, Map<String, String> myMap) {
        Map<Integer, String> newMap = new HashMap<>();
        int size = myMap.size();
        if (oldMap.size() != size) {
            throw new NullPointerException("表格内容无法识别，请检查内容的规范性！");
        } else {
            for (Integer key : oldMap.keySet()) {
                for (String key2 : myMap.keySet()) {
                    if (oldMap.get(key).equals(key2)) {
                        newMap.put(key, myMap.get(key2));
                    }
                }
            }
        }
        return newMap;
    }

    public static void exportExcel(String sheetName, Map<String, String> keyMap, String fileName, List<Map> list, HttpServletResponse response) throws Exception {

    }


    /**
     * 使用浏览器选下载
     *
     * @param response
     * @param fileName 下载时的文件名
     * @param data
     * @throws Exception
     */
    public static void exportExcel(HttpServletResponse response, String fileName, ExcelData data) throws Exception {
        // 告诉浏览器用什么软件可以打开此文件
        response.setHeader("content-Type", "application/vnd.ms-excel");
        // 下载文件的默认名称
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xls", "utf-8"));
        exportExcel(data, response.getOutputStream());
    }

    /**
     * 本地下载
     *
     * @param excelData
     * @param path      文件要存储的路径
     * @return
     * @throws Exception
     */
    public static int generateExcel(ExcelData excelData, String path) throws Exception {
        File f = new File(path);
        FileOutputStream out = new FileOutputStream(f);
        return exportExcel(excelData, out);
    }

    /**
     * 创建excel表
     *
     * @param data
     * @param out
     * @return
     * @throws Exception
     */
    private static int exportExcel(ExcelData data, OutputStream out) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        int rowIndex = 0;
        try {
            //设置工作表的名字
            String sheetName = data.getName();
            if (null == sheetName) {
                sheetName = "Sheet1";
            }
            //创建工作表
            XSSFSheet sheet = wb.createSheet(sheetName);
            rowIndex = writeExcel(wb, sheet, data);
            wb.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //此处需要关闭 wb 变量
            out.close();
        }
        return rowIndex;
    }


    /**
     * 设置表显示字段
     *
     * @param wb
     * @param sheet
     * @param data
     * @return
     */
    private static int writeExcel(XSSFWorkbook wb, Sheet sheet, ExcelData data) {
        int rowIndex = 0;
        rowIndex = writeTitlesToExcel(wb, sheet, data.getTitles());
        rowIndex = writeRowsToExcel(wb, sheet, data.getRows(), rowIndex);
        autoSizeColumns(sheet, data.getTitles().size() + 1);
        return rowIndex;
    }

    /**
     * 设置表头
     *
     * @param wb
     * @param sheet
     * @param titles
     * @return
     */
    private static int writeTitlesToExcel(XSSFWorkbook wb, Sheet sheet, List<String> titles) {
        int rowIndex = 0;
        int colIndex = 0;
        Font titleFont = wb.createFont();
        //设置字体
        titleFont.setFontName("宋体");
        //设置字号
        titleFont.setFontHeightInPoints((short) 12);
        //设置颜色
        titleFont.setColor(IndexedColors.BLACK.index);
        XSSFCellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        setBorder(titleStyle, BorderStyle.THIN);
        Row titleRow = sheet.createRow(rowIndex);
        titleRow.setHeightInPoints(25);
        colIndex = 0;
        for (String field : titles) {
            Cell cell = titleRow.createCell(colIndex);
            cell.setCellValue(field);
            cell.setCellStyle(titleStyle);
            colIndex++;
        }
        rowIndex++;
        return rowIndex;
    }

    /**
     * 设置内容
     *
     * @param wb
     * @param sheet
     * @param rows
     * @param rowIndex
     * @return
     */
    private static int writeRowsToExcel(XSSFWorkbook wb, Sheet sheet, List<List<Object>> rows, int rowIndex) {
        int colIndex;
        Font dataFont = wb.createFont();
        dataFont.setFontName("宋体");
        dataFont.setFontHeightInPoints((short) 12);
        dataFont.setColor(IndexedColors.BLACK.index);
        XSSFCellStyle dataStyle = wb.createCellStyle();
        dataStyle.setFont(dataFont);
        setBorder(dataStyle, BorderStyle.THIN);
        for (List<Object> rowData : rows) {
            Row dataRow = sheet.createRow(rowIndex);
            dataRow.setHeightInPoints(25);
            colIndex = 0;
            for (Object cellData : rowData) {
                Cell cell = dataRow.createCell(colIndex);
                if (cellData != null) {
                    cell.setCellValue(cellData.toString());
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(dataStyle);
                colIndex++;
            }
            rowIndex++;
        }
        return rowIndex;
    }

    /**
     * 自动调整列宽
     *
     * @param sheet
     * @param columnNumber
     */
    private static void autoSizeColumns(Sheet sheet, int columnNumber) {
        for (int i = 0; i < columnNumber; i++) {
            int orgWidth = sheet.getColumnWidth(i);
            sheet.autoSizeColumn(i, true);
            int newWidth = (int) (sheet.getColumnWidth(i) + 100);
            if (newWidth > orgWidth) {
                sheet.setColumnWidth(i, newWidth);
            } else {
                sheet.setColumnWidth(i, orgWidth);
            }
        }
    }

    /**
     * 设置边框
     *
     * @param style
     * @param border
     */
    private static void setBorder(XSSFCellStyle style, BorderStyle border) {
        style.setBorderTop(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
        style.setBorderBottom(border);
    }


}
