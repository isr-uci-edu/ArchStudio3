package edu.uci.ics.widgets.finddialog;

public interface FindDialogListener {

	public void doFind(FindDialog fd, String text);
	public void doGoto(FindDialog fd, Object target);
	public void isClosing(FindDialog fd);
}
