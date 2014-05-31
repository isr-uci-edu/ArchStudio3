package edu.uci.ics.bna.floatingtable;

import java.util.*;

public class TwoColumnTableUtils{

	protected TableTheme theme;

	public TwoColumnTableUtils(TableTheme theme){
		this.theme = theme;
	}

	public void applyTheme(TableData table){
		table.setCellPadding(theme.getCellPadding());
		table.setCellSpacing(theme.getCellSpacing());
		table.setForegroundColor(theme.getTableForegroundColor());
		table.setBackgroundColor(theme.getTableBackgroundColor());
	}
	
	public TableRow createHeaderRow(Object text){
		TableCell tc = new TableCell(text);
		tc.setColspan(2);
		tc.setForegroundColor(theme.getHeaderForegroundColor());
		tc.setBackgroundColor(theme.getHeaderBackgroundColor());
		tc.setBold(theme.isHeaderBold());
		tc.setItalics(theme.isHeaderItalics());
		tc.setFontSize(theme.getHeaderFontSize());
		return new TableRow(new TableCell[]{tc});
	}

	public TableRow createSubheadRow(Object text){
		TableCell tc = new TableCell(text);
		tc.setColspan(2);
		tc.setForegroundColor(theme.getSubheadForegroundColor());
		tc.setBackgroundColor(theme.getSubheadBackgroundColor());
		tc.setBold(theme.isSubheadBold());
		tc.setItalics(theme.isSubheadItalics());
		tc.setFontSize(theme.getSubheadFontSize());
		return new TableRow(new TableCell[]{tc});
	}

	public TableRow createBodyRow(Object name, Object value){
		TableCell nameCell = createNameCell(name);
		TableCell valueCell = createValueCell(value);
		return new TableRow(new TableCell[]{nameCell, valueCell});
	}
	
	protected TableCell createNameCell(Object name){
		TableCell nameCell = new TableCell(name);
		nameCell.setForegroundColor(theme.getBodyForegroundColor());
		nameCell.setBackgroundColor(theme.getBodyBackgroundColor());
		nameCell.setBold(theme.isBodyBold());
		nameCell.setItalics(theme.isBodyItalics());
		nameCell.setFontSize(theme.getBodyFontSize());
		return nameCell;
	}
	
	protected TableCell createValueCell(Object value){
		TableCell valueCell = new TableCell(value);
		valueCell.setForegroundColor(theme.getBodyForegroundColor());
		valueCell.setBackgroundColor(theme.getBodyBackgroundColor());
		valueCell.setBold(theme.isBodyBold());
		valueCell.setItalics(theme.isBodyItalics());
		valueCell.setFontSize(theme.getBodyFontSize());
		return valueCell;
	}
	
	public TableRow[] createMultiValueBodyRows(Object name, Object[] values){
		if(values.length == 0){
			return new TableRow[]{createBodyRow(name, "")};
		}
		if(values.length == 1){
			return new TableRow[]{createBodyRow(name, values[0])};
		}
		
		TableRow[] rows = new TableRow[values.length];
		
		TableCell nameCell = createNameCell(name);
		nameCell.setRowspan(values.length);
		
		TableCell valueCell = createValueCell(values[0]);
		
		rows[0] = new TableRow(new TableCell[]{nameCell, valueCell});
		for(int i = 1; i < values.length; i++){
			valueCell = createValueCell(values[i]);
			rows[i] = new TableRow(new TableCell[]{valueCell});
		}
		return rows;
	}
}
