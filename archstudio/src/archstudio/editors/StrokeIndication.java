package archstudio.editors;

import java.awt.Stroke;

public class StrokeIndication implements Indication {

	protected Stroke stroke;

	public StrokeIndication(Stroke stroke){
		this.stroke = stroke;
	}
	
	public void setStroke(Stroke stroke){
		this.stroke = stroke;
	}
	
	public Stroke getStroke(){
		return stroke;
	}

}
