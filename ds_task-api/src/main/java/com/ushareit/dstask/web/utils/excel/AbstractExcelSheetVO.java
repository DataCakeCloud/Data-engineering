package com.ushareit.dstask.web.utils.excel;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractExcelSheetVO {

	protected String name;

	protected List<List<String>> datas = new ArrayList<List<String>>();

	public AbstractExcelSheetVO() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<List<String>> getDatas() {
		return datas;
	}

	public void setDatas(List<List<String>> datas) {
		this.datas = datas;
	}


	public void addData(List<String> data) {
		datas.add(data);
	}

	/**
	 * 增加空的数据，为了把excel格式设置为文本类型。
	 *
	 * @param rows
	 * @param columns
	 */
	public void addEmptyData(int rows, int columns) {
		for (int i = 0; i < rows; i++) {
			List<String> data = new ArrayList<String>();
			for (int j = 0; j < columns; j++) {
				data.add("");
			}
			addData(data);
		}
	}

	//提供默认表头方法
	public void defaultHeaderStyle(Sheet sheet, Row row, Cell cell, CellStyle cellStyle, Font font, int cellNum) {
		font.setFontHeightInPoints((short) 16);//设置字体大小
		//font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
		font.setFontHeightInPoints((short) 12);
		row.setHeightInPoints(20);//行高
		sheet.setColumnWidth(cellNum, 256 * 18);//列宽
		cellStyle.setFont(font);
		cell.setCellStyle(cellStyle);
	}


	/**
	 * 暂时用户扩展sheet上的行列合并
	 * @param sheet
     */
	protected abstract void mergedRegion(Sheet sheet);

	/**
	 * 设置表头格式
	 * @param wb
	 * @param sheet
	 * @param row
	 * @param cell
	 * @param rowNum
     * @param cellNum
     */
	protected abstract void setHeaderStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum);

	/**
	 * 设置单元格样式
	 * @param wb
	 * @param sheet
	 * @param row
	 * @param cell
	 * @param rowNum
     * @param cellNum
     */
	protected abstract void setCellStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum);


}
