package edu.uci.ics.xarchutils;

/**
 * Describes an object that receives and handles flat xArch
 * events.
 *
 * @author Eric M. Dashofy [edashofy@ics.uci.edu]
 */
public interface XArchFlatListener{

	/**
	 * Called automatically when a flat xArch event
	 * occurs, usually indicating a change in an
	 * xArch document.
	 *
	 * @param evt xArch event that occurred.
	 */
	public void handleXArchFlatEvent(XArchFlatEvent evt);

}


