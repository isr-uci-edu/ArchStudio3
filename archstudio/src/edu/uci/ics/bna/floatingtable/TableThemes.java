package edu.uci.ics.bna.floatingtable;

import java.awt.Color;
import edu.uci.ics.widgets.Colors;

public class TableThemes {

	private TableThemes(){}
	
	public static TableTheme DEFAULT_THEME = 
		new TableTheme(
			Color.LIGHT_GRAY, //table background
			Color.BLACK, //table foreground
			2, //cellpadding
			0, //cellspacing
			Color.WHITE, //header foreground
			Color.BLACK, //header background
			true, //header bold
			false, //header italics
			+1, //header font size
			Color.LIGHT_GRAY, //subhead foreground
			Color.DARK_GRAY, //subhead background
			false, //subhead bold
			true, //subhead italics
			0, //subhead font size
			Color.BLACK, //body foreground
			Color.LIGHT_GRAY, //body background
			false, //body bold
			false, //body italics
			0 //body font size
		);

	public static TableTheme createTheme(Color lightColor){
		return createTheme(lightColor.brighter(), lightColor, lightColor.darker(), lightColor.darker().darker());
	}
		
	public static TableTheme createTheme(Color brightColor, Color lightColor, Color medColor, Color darkColor){
		return new TableTheme(
			lightColor, //table background
			darkColor, //table foreground
			2, //cellpadding
			0, //cellspacing
			brightColor, //header foreground
			darkColor, //header background
			true, //header bold
			false, //header italics
			+1, //header font size
			lightColor, //subhead foreground
			medColor, //subhead background
			false, //subhead bold
			true, //subhead italics
			0, //subhead font size
			darkColor, //body foreground
			lightColor, //body background
			false, //body bold
			false, //body italics
			0 //body font size
		);
	}
}
