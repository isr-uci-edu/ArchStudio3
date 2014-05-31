package edu.uci.ics.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.*;

import edu.uci.ics.nativeutils.SystemUtils;

// Generate new tetradic color schemes at:
// http://wellstyled.com/tools/colorscheme2/index-en.html

public class ColorSchemes{
	
	public static ColorScheme[] loadDefaultColorSchemes(){
		InputStream is = ClassLoader.getSystemResourceAsStream("edu/uci/ics/widgets/res/colorschemes.txt");
		return loadColorSchemes(is);
	}
	
	public static ColorScheme[] loadColorSchemes(InputStream is){
		List schemeList = new ArrayList();
		
		String name = null;
		List colorArrayList = new ArrayList();
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while(true){
				String line = br.readLine();
				if(line == null){
					if(name != null){
						Color[][] colorArrays = (Color[][])colorArrayList.toArray(new Color[0][]);
						ColorScheme scheme = new ColorScheme(name, colorArrays);
						schemeList.add(scheme);
					}
					break;
				}
				line = line.trim();
				if(line.length() == 0){
					continue;
				}
				else if(line.startsWith("#")){
					continue;
				}
				else if(line.startsWith("&")){
					if(name != null){
						Color[][] colorArrays = (Color[][])colorArrayList.toArray(new Color[0][]);
						ColorScheme scheme = new ColorScheme(name, colorArrays);
						schemeList.add(scheme);
					}
					name = line.substring(1).trim();
					colorArrayList.clear();
				}
				else{
					String[] colorStrings = line.split("\\b");
					List colorList = new ArrayList();
					for(int i = 0; i < colorStrings.length; i++){
						String colorString = colorStrings[i].trim();
						if(colorString.length() > 0){
							try{
								int colorRgb = Integer.parseInt(colorString, 16);
								Color c = new Color(colorRgb);
								colorList.add(c);
							}
							catch(NumberFormatException nfe){
							}
						}
					}
					Color[] colorArray = (Color[])colorList.toArray(new Color[0]);
					colorArrayList.add(colorArray);
				}
			}
		}
		catch(IOException e){
		}
		try{
			is.close();
		}
		catch(IOException ioe2){
		}
		ColorScheme[] colorSchemeArray = (ColorScheme[])schemeList.toArray(new ColorScheme[0]);
		return colorSchemeArray;
	}
	
	public static final ColorScheme[] ALL_COLOR_SCHEMES = loadDefaultColorSchemes();
}
