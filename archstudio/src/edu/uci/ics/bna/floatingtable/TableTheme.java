package edu.uci.ics.bna.floatingtable;

import java.awt.Color;

public class TableTheme implements java.io.Serializable{

	protected Color tableBackgroundColor;
	protected Color tableForegroundColor;

	protected int cellPadding;
	protected int cellSpacing;

	protected Color headerForegroundColor;
	protected Color headerBackgroundColor;
	protected boolean headerBold;
	protected boolean headerItalics;
	protected int headerFontSize;
	
	protected Color subheadForegroundColor;
	protected Color subheadBackgroundColor;
	protected boolean subheadBold;
	protected boolean subheadItalics;
	protected int subheadFontSize;
	
	protected Color bodyForegroundColor;
	protected Color bodyBackgroundColor;
	protected boolean bodyBold;
	protected boolean bodyItalics;
	protected int bodyFontSize;
	
	public TableTheme(
		Color tableBackgroundColor,
		Color tableForegroundColor,
		
		int cellPadding,
		int cellSpacing,
		
		Color headerForegroundColor,
		Color headerBackgroundColor,
		boolean headerBold,
		boolean headerItalics,
		int headerFontSize,
			
		Color subheadForegroundColor,
		Color subheadBackgroundColor,
		boolean subheadBold,
		boolean subheadItalics,
		int subheadFontSize,
			
		Color bodyForegroundColor,
		Color bodyBackgroundColor,
		boolean bodyBold,
		boolean bodyItalics,
		int bodyFontSize
	){
		this.tableBackgroundColor = tableBackgroundColor; 
		this.tableForegroundColor = tableForegroundColor;
		
		this.cellPadding = cellPadding;
		this.cellSpacing = cellSpacing;
		
		this.headerForegroundColor = headerForegroundColor;
		this.headerBackgroundColor = headerBackgroundColor;
		this.headerBold = headerBold;
		this.headerItalics = headerItalics;
		this.headerFontSize = headerFontSize;
			
		this.subheadForegroundColor = subheadForegroundColor;
		this.subheadBackgroundColor = subheadBackgroundColor;
		this.subheadBold = subheadBold;
		this.subheadItalics = subheadItalics;
		this.subheadFontSize = subheadFontSize;
			
		this.bodyForegroundColor = bodyForegroundColor;
		this.bodyBackgroundColor = bodyBackgroundColor;
		this.bodyBold = bodyBold;
		this.bodyItalics = bodyItalics;
		this.bodyFontSize = bodyFontSize;
	}

	public Color getBodyBackgroundColor() {
		return bodyBackgroundColor;
	}

	public boolean isBodyBold() {
		return bodyBold;
	}

	public int getBodyFontSize() {
		return bodyFontSize;
	}

	public Color getBodyForegroundColor() {
		return bodyForegroundColor;
	}

	public boolean isBodyItalics() {
		return bodyItalics;
	}

	public int getCellPadding() {
		return cellPadding;
	}

	public int getCellSpacing() {
		return cellSpacing;
	}

	public Color getHeaderBackgroundColor() {
		return headerBackgroundColor;
	}

	public boolean isHeaderBold() {
		return headerBold;
	}

	public int getHeaderFontSize() {
		return headerFontSize;
	}

	public Color getHeaderForegroundColor() {
		return headerForegroundColor;
	}

	public boolean isHeaderItalics() {
		return headerItalics;
	}

	public Color getSubheadBackgroundColor() {
		return subheadBackgroundColor;
	}

	public boolean isSubheadBold() {
		return subheadBold;
	}

	public int getSubheadFontSize() {
		return subheadFontSize;
	}

	public Color getSubheadForegroundColor() {
		return subheadForegroundColor;
	}

	public boolean isSubheadItalics() {
		return subheadItalics;
	}

	public Color getTableBackgroundColor() {
		return tableBackgroundColor;
	}

	public Color getTableForegroundColor() {
		return tableForegroundColor;
	}

}
