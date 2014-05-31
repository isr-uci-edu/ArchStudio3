package edu.uci.ics.widgets;

import java.awt.Color;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ColorTransferable implements Transferable{
	public static DataFlavor COLOR_DATA_FLAVOR = new DataFlavor(java.awt.Color.class, "java.awt.Color");
	
	protected Color c;
	
	public ColorTransferable(Color c){
		this.c = c;
	}
	
	public Color getColor(){
		return c;
	}

	public DataFlavor[] getTransferDataFlavors(){
		return new DataFlavor[]{COLOR_DATA_FLAVOR, DataFlavor.stringFlavor};
	}

	public boolean isDataFlavorSupported(DataFlavor flavor){
		DataFlavor[] supportedFlavors = getTransferDataFlavors();
		for(int i = 0; i < supportedFlavors.length; i++){
			if(supportedFlavors[i].equals(flavor)){
				return true;
			}
		}
		return false;
	}

	public Object getTransferData(DataFlavor flavor)
	throws UnsupportedFlavorException, IOException{
		if(flavor.equals(COLOR_DATA_FLAVOR)){
			return c;
		}
		else if(flavor.equals(DataFlavor.stringFlavor)){
			return "#" + WidgetUtils.getHexColorString(c);
		}
		else{
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
