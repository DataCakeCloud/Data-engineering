package com.ushareit.dstask.web.utils.excel;

import com.google.common.collect.Maps;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 导出excel的默认样式
 * 如需扩展样式 请实现AbstractExcelSheetVO
 *
 * @author CAOXUEDONG
 */
@Slf4j
@ToString
public class ExcelSheetVO extends AbstractExcelSheetVO {

    private final Map<String, CellStyle> cellStyleMap = Maps.newHashMap();

    private static final String INTEGER = "integer";

    private static final String DECIMAL = "decimal";

    private static final String DATE = "date";

    private static final String PERCENT = "percent";

    private static final String DEFAULT = "default";

    public ExcelSheetVO() {
        super();
    }

    @Override
    protected void mergedRegion(Sheet sheet) {

    }

    @Override
    protected void setHeaderStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum) {
        if (rowNum == 0) {
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();

            //设置单元格为文本格式
            cellStyle.setDataFormat(wb.createDataFormat().getFormat("@"));

            //font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
            //font.setFontHeightInPoints((short) 12);
            sheet.setColumnWidth(cellNum, 256 * 18);//列宽
            cellStyle.setFont(font);
            //cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); //居中
            cell.setCellStyle(cellStyle);
        }
    }

    @Override
    protected void setCellStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum) {
        if (isNum(cell) && !isPercent(cell)) {
            CellStyle cellStyle = null;
            if (isInteger(cell)) {
                cellStyle = getCellStyle(wb, INTEGER);
            } else {
                cellStyle = getCellStyle(wb, DECIMAL);
            }
            cell.setCellStyle(cellStyle);
            cell.setCellValue(parseDouble(cell.getStringCellValue()));
        } else if (isPercent(cell)) {
            try {
                NumberFormat format = NumberFormat.getPercentInstance();

                Number percent = format.parse(cell.getStringCellValue());
                cell.setCellStyle(getCellStyle(wb, PERCENT));
                cell.setCellValue(percent.doubleValue());
            } catch (ParseException e) {
                log.error("ParseException", e);
                cell.setCellStyle(getCellStyle(wb, PERCENT));
            }
        } else if (isDate(cell)) {
            //Date date = DateTimeUtils.parse(cell.getStringCellValue(), DateTimeUtils.DateTimeFormatterEnum.YYYY_MM_DD);
            //cell.setCellStyle(getCellStyle(wb, DATE));
            //cell.setCellValue(date);
        } else {
            cell.setCellStyle(getCellStyle(wb, DEFAULT));
            cell.setCellValue(cell.getStringCellValue());
        }
    }

    private CellStyle getCellStyle(Workbook workbook, String type) {
        CellStyle cellStyle = cellStyleMap.get(type);

        if (Objects.nonNull(cellStyle)) {
            return cellStyle;
        }
        DataFormat dataFormat = workbook.createDataFormat();
        switch (type) {
            case INTEGER:
                cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(dataFormat.getFormat("#,##0"));
                cellStyleMap.put(INTEGER, cellStyle);
                break;
            case DECIMAL:
                cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
                cellStyleMap.put(DECIMAL, cellStyle);
                break;
            case DATE:
                cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(dataFormat.getFormat("yyyy-mm-dd"));
                cellStyleMap.put(DATE, cellStyle);
                break;
            case PERCENT:
                cellStyle = workbook.createCellStyle();
                cellStyle.setDataFormat(dataFormat.getFormat("0.00%"));
                cellStyleMap.put(PERCENT, cellStyle);
            case DEFAULT:
                cellStyle = workbook.createCellStyle();
                cellStyleMap.put(DEFAULT, cellStyle);
                break;
            default:
                cellStyle = workbook.createCellStyle();
                cellStyleMap.put(DEFAULT, cellStyle);
                break;
        }
        return cellStyleMap.get(type);
    }

    private boolean isNum(Cell cell) {
        String cellValue = cell.getStringCellValue();
        //可匹配0  15664  1.0   102,365   102,365.10
        return cellValue.matches("^(-?\\d+)(,\\d+)*(\\.\\d+)?$");
    }

    private boolean isInteger(Cell cell) {
        String cellValue = cell.getStringCellValue();
        //return cellValue.matches("^[-\\+]?[\\d]*$");
        return cellValue.matches("^(-?\\d+)(,\\d+)*$");
    }

    public static Pattern percentMatch=Pattern.compile("^\\d+\\.{0,1}\\d*%$");
    private boolean isPercent(Cell cell) {
        String cellValue = cell.getStringCellValue();
        return percentMatch.matcher(cellValue).find();
    }

    private boolean isDate(Cell cell) {
        String cellValue = cell.getStringCellValue();
        return cellValue.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private double parseDouble(String cellVal) {
        String val = StringUtils.replaceAll(cellVal, ",", "");
        return Double.parseDouble(val);
    }


}
