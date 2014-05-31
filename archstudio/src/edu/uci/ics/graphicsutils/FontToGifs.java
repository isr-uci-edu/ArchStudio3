package edu.uci.ics.graphicsutils;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;

public class FontToGifs{

	static String fontName = null;
	static int fontSize = 100;
	static int codePageBase = 0;
	static File directory = new File("res");
	static Color fontColor = Color.black;
	
	public static void main(String[] args){
		try{
			for(int i = 0; i < args.length; i++){
				if(args[i].equals("-font")){
					i++;
					fontName = args[i];
				}
				else if(args[i].equals("-size")){
					i++;
					fontSize = Integer.parseInt(args[i]);
				}
				else if(args[i].equals("-codepagebase")){
					i++;
					codePageBase = Integer.valueOf(args[i], 16).intValue();
				}
				else if(args[i].equals("-outputdir")){
					i++;
					directory = new File(args[i]);
				}
				else if(args[i].equals("-fontcolor")){
					i++;
					String colorString = args[i];
					if(colorString.length() != 6){
						printArgs();
						return;
					}
					String redString = colorString.substring(0,2);
					String greenString = colorString.substring(2,4);
					String blueString = colorString.substring(4,6);
					
					int red = Integer.valueOf(redString, 16).intValue();
					int green = Integer.valueOf(greenString, 16).intValue();
					int blue = Integer.valueOf(blueString, 16).intValue();
						
					fontColor = new Color(red, green, blue);
				}
				else{
					printArgs();
					return;
				}
			}
			if(fontName == null){
				printArgs();
				return;
			}
		}
		catch(Exception e){
			printArgs();
			return;
		}
		writeFiles();
	}
	
	public static void printArgs(){
		System.out.println("arg error.");
		System.out.println("java FontToGifs -font <name> [-size <size>] [-codepagebase <codepage>] [-outputdir <dir>] [-fontcolor <fontcolor_in_html_style>]");
		return;
	}
	
	public static void writeFiles(){
		if(!directory.exists()){			
			if(!directory.mkdirs()){
				System.out.println("Could not create directory.");
				return;
			}
		}

		int defaultFontSize = fontSize;
		//Font theFont =
		//	new Font("MapSym-EN-Air-APP6a", Font.PLAIN, defaultFontSize);
		System.out.println("CodePageBase = " + codePageBase);
		Font theFont =
			new Font(fontName, Font.PLAIN, defaultFontSize);

		byte[] r = new byte[256];
		byte[] g = new byte[256];
		byte[] b = new byte[256];
		
		for(int i = 0; i < 256; i++){
			r[i] = 0;
			g[i] = 0;
			b[i] = 0;
		}
		
		r[0] = (byte)0xff;
		g[0] = (byte)0xff;
		b[0] = (byte)0xff;
		
		r[1] = (byte)fontColor.getRed();
		g[1] = (byte)fontColor.getGreen();
		b[1] = (byte)fontColor.getBlue();
		
		for(int cindex = 0; cindex < 256; cindex++){
			System.out.println("Creating character: " + cindex);
			BufferedImage tempImage = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_INDEXED);
			Graphics2D tempG2D = tempImage.createGraphics();
			int charInt = codePageBase + cindex;
			char character = (char)charInt;
			System.out.println("charInt = " + charInt);
			
			if(!theFont.canDisplay(character)){
				System.out.println("Skipping.");
				continue;
			}
			
			//char character = (char)((char)codePageBase) + ((char)cindex);
			LineMetrics theMetrics = theFont.getLineMetrics("" + character, tempG2D.getFontRenderContext());
			
			TextLayout layout = new TextLayout(character + "", theFont, tempG2D.getFontRenderContext());
			Rectangle2D bounds = layout.getBounds();
	
			int width = /*(int)bounds.getWidth()*/+ (int)layout.getAdvance() + 1;
			if(width < 2) width = 100;
			int height = (int)bounds.getHeight()+1;
			if(height < 2){
				height = (int)theMetrics.getHeight() + 1;
			}
			if(height < 1){
				height = 100;
			}
			
			int descent = (int)theMetrics.getDescent() + 1;
			//int height = (int)theMetrics.getHeight() + 1;
			
			IndexColorModel icm = new IndexColorModel(8, 256, r, g, b, 0);
			
			System.out.println("Width = " + width);
			System.out.println("Height = " + height);
			BufferedImage fontImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
			Graphics2D g2d = fontImage.createGraphics();
	
			g2d.setBackground(Color.white);
			g2d.setPaint(Color.white);
			g2d.fillRect(0, 0, width, height);
			g2d.setPaint(fontColor);
			g2d.setFont(theFont);
			g2d.drawString(character + "", 0, height);
			
			GifEncoder enc = new GifEncoder(fontImage);
			//enc.setTransparentPixel(0xFFFFFF);
			try{
				File charFile = new File(directory, "char" + Integer.toHexString(cindex) + ".gif");
				FileOutputStream fos = new FileOutputStream(charFile);
				enc.write(fos);
				fos.close();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}

}
