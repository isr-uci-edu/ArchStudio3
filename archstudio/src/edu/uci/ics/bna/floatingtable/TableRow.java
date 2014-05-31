package edu.uci.ics.bna.floatingtable;

public class TableRow implements java.io.Serializable{

	protected TableCell[] cells;

	public TableRow(TableCell[] cells){
		this.cells = cells;
	}
	
	public int getNumCells(){
		return cells.length;
	}
	
	public int getNumCols(){
		int cnt = 0;
		for(int i = 0; i < cells.length; i++){
			cnt += cells[i].getColspan();
		}
		return cnt;
	}
	
	public TableCell getCellAt(int i){
		return cells[i];
	}
	
	public String getHtml(){
		StringBuffer sb = new StringBuffer();
		sb.append("<tr>");
		for(int i = 0; i < cells.length; i++){
			sb.append(cells[i].getHtml());
		}
		sb.append("</tr>");
		return sb.toString();
	}

}
