package edu.uci.ics.bna.floatingtable;

public class EmptyTableCell extends TableCell{

	public EmptyTableCell() {
		super("");
	}
	
	public String getHtml(){
		return "<td></td>";
	}

}
