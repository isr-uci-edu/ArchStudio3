package edu.uci.ics.bna.floatingtable;

import java.awt.Color;

public class TableCell implements java.io.Serializable{

	public static final int VALIGN_TOP = 100;
	public static final int VALIGN_MIDDLE = 200;
	public static final int VALIGN_BOTTOM = 300;

	public static final int ALIGN_LEFT = 400;
	public static final int ALIGN_CENTER = 500;
	public static final int ALIGN_RIGHT = 600;

	protected Object o;
	protected Color foregroundColor = null;
	protected Color backgroundColor = null;

	protected int colspan = 1;
	protected int rowspan = 1;
	
	protected int valign = VALIGN_MIDDLE;
	protected int align = ALIGN_LEFT;

	protected boolean bold = false;
	protected boolean italics = false;
	protected int fontSize = 0;

	public TableCell(Object o){
		this.o = o;
	}
	
	public Color getBackgroundColor(){
		return backgroundColor;
	}

	public int getColspan(){
		return colspan;
	}

	public Color getForegroundColor(){
		return foregroundColor;
	}

	public Object getObject(){
		return o;
	}

	public int getRowspan(){
		return rowspan;
	}

	public void setBackgroundColor(Color color){
		backgroundColor = color;
	}

	public void setColspan(int i){
		colspan = i;
	}

	public void setForegroundColor(Color color){
		foregroundColor = color;
	}

	public void setObject(Object object){
		o = object;
	}

	public void setRowspan(int i){
		rowspan = i;
	}
	
	public void setVAlign(int valign){
		this.valign = valign;
	}
	
	public int getVAlign(){
		return valign;
	}
	
	public void setAlign(int align){
		this.align = align;
	}
	
	public int getAlign(){
		return align;
	}
	
	public boolean isBold() {
		return bold;
	}

	public boolean isItalics() {
		return italics;
	}

	public void setBold(boolean b) {
		bold = b;
	}

	public void setItalics(boolean b) {
		italics = b;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int i) {
		fontSize = i;
	}

	public String getHtml(){
		StringBuffer sb = new StringBuffer();
		sb.append("<td");

		if(colspan > 1){
			sb.append(" colspan=\"");
			sb.append(colspan);
			sb.append("\"");
		}
		if(rowspan > 1){
			sb.append(" rowspan=\"");
			sb.append(rowspan);
			sb.append("\"");
		}
		
		switch(valign){
			case VALIGN_TOP:
				sb.append(" valign=\"top\"");
				break;
			case VALIGN_MIDDLE:
				sb.append(" valign=\"middle\"");
				break;
			case VALIGN_BOTTOM:
				sb.append(" valign=\"bottom\"");
				break;
		}

		switch(align){
			case ALIGN_LEFT:
				sb.append(" align=\"left\"");
				break;
			case ALIGN_CENTER:
				sb.append(" align=\"center\"");
				break;
			case ALIGN_RIGHT:
				sb.append(" align=\"right\"");
				break;
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
		sb.append(">");

		if(fontSize != 0){
			sb.append("<font size=\"");
			if(fontSize > 0) sb.append("+");
			sb.append(fontSize);
			sb.append("\">");
		}
		if(isBold()){
			sb.append("<b>");
		}
		if(isItalics()){
			sb.append("<i>");
		}
		
		sb.append(o.toString());
		
		if(isItalics()){
			sb.append("</i>");
		}
		if(isBold()){
			sb.append("</b>");
		}
		if(fontSize != 0){
			sb.append("</font>");
		}
		
		sb.append("</td>");
		return sb.toString();
	}

}
