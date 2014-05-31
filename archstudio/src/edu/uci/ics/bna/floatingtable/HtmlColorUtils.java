package edu.uci.ics.bna.floatingtable;

public class HtmlColorUtils{

	private HtmlColorUtils(){}
	
	private static void c2h(StringBuffer sb, int colorElement){
		String es = Integer.toHexString(colorElement);
		if(es.length() == 1){
			sb.append("0");
		}
		sb.append(es);
	}
	
	public static String colorToHtml(java.awt.Color c){
		StringBuffer sb = new StringBuffer("#");
		c2h(sb, c.getRed());
		c2h(sb, c.getGreen());
		c2h(sb, c.getBlue());
		return sb.toString();
	}

}
