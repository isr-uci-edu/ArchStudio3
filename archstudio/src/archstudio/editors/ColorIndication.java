package archstudio.editors;

import java.awt.Color;

public class ColorIndication implements Indication{

	protected Color color;

	public ColorIndication(Color color){
		this.color = color;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public Color getColor(){
		return color;
	}

}
