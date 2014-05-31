package archstudio.comp.tron.gui;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.uci.ics.xadlutils.Resources;

public class TronGUITable extends JTable{

	public TronGUITable(TronGUITableModel tm){
		super(tm);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTableHeader().getColumnModel().getColumn(TronGUITableModel.SEVERITY_COLUMN_INDEX).setCellRenderer(new TronGUITableCellRenderer());
		getTableHeader().getColumnModel().getColumn(TronGUITableModel.SEVERITY_COLUMN_INDEX).setPreferredWidth(16);
		getTableHeader().getColumnModel().getColumn(TronGUITableModel.ISSUE_COLUMN_INDEX).setPreferredWidth(300);
		invalidate();
	}

	static class TronGUITableCellRenderer extends DefaultTableCellRenderer{
		public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column){
			super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
			if(value != null){
				if(value.equals(TronGUITableModel.ERROR_INDICATOR)){
					setText("");
					setIcon(Resources.ERROR_ICON_16);
				}
				else if(value.equals(TronGUITableModel.WARNING_INDICATOR)){
					setText("");
					setIcon(Resources.WARNING_ICON_16);
				}
				else{
					setText("");
					setIcon(Resources.INFO_ICON_16);
				}
			}
			return this;
		}
	}

}
