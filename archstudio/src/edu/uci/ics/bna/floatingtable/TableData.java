package edu.uci.ics.bna.floatingtable;

import java.awt.Color;
import java.util.*;

public class TableData {

	protected List rowList = new ArrayList();

	protected int cellPadding = 0;
	protected int cellSpacing = 0;
	protected int border = 0;
	protected Color backgroundColor = null;
	protected Color foregroundColor = null;

	public TableData(){
	}
	
	public TableData(TableRow firstRow){
		rowList.add(firstRow);
	}
	
	public TableData(TableRow[] rows){
		for(int i = 0; i < rows.length; i++){
			rowList.add(rows[i]);
		}
	}
	
	public void setBorder(int border){
		this.border = border;
	}
	
	public int getBorder(){
		return border;
	}
	
	public int getNumRows(){
		return rowList.size();
	}
	
	public int getNumCols(){
		if(rowList.size() == 0){
			return 0;
		}
		
		TableRow row = (TableRow)rowList.get(0);
		return row.getNumCols();
	}
	
	public void addRow(TableRow row){
		rowList.add(row);
	}
	
	public void addRows(TableRow[] rows){
		for(int i = 0; i < rows.length; i++){
			rowList.add(rows[i]);
		}
	}
	
	public void removeRow(int index){
		rowList.remove(index);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public int getCellPadding() {
		return cellPadding;
	}

	public int getCellSpacing() {
		return cellSpacing;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setBackgroundColor(Color color) {
		backgroundColor = color;
	}

	public void setCellPadding(int i) {
		cellPadding = i;
	}

	public void setCellSpacing(int i) {
		cellSpacing = i;
	}

	public void setForegroundColor(Color color) {
		foregroundColor = color;
	}

	public String getHtml(int width, int height){
		StringBuffer sb = new StringBuffer();
		sb.append("<table");
		
		if(border > 0){
			sb.append(" border=").append(border);
		}
		
		if(width > 0){
			sb.append(" width=").append(width);
		}
		if(height > 0){
			sb.append(" height=").append(height);
		}

		if(backgroundColor != null){
			sb.append(" bgcolor=\"");
			sb.append(HtmlColorUtils.colorToHtml(backgroundColor));
			sb.append("\"");
		}
		if(foregroundColor != null){
			sb.append(" color=\"");
			sb.append(HtmlColorUtils.colorToHtml(foregroundColor));
			sb.append("\"");
		}
		
		if(cellSpacing > 0){
			sb.append(" cellspacing=\"");
			sb.append(cellPadding);
			sb.append("\"");
		}
		
		if(cellPadding > 0){
			sb.append(" cellpadding=\"");
			sb.append(cellPadding);
			sb.append("\"");
		}
		
		sb.append(">");
		
		for(Iterator it = rowList.iterator(); it.hasNext(); ){
			TableRow row = (TableRow)it.next();
			sb.append(row.getHtml());
		}
		
		sb.append("</table>");
		return sb.toString();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof TableData)){
			return false;
		}
		String oHtml = ((TableData)o).getHtml(0, 0);
		String tHtml = getHtml(0, 0);
		return oHtml.equals(tHtml);
	}
}
