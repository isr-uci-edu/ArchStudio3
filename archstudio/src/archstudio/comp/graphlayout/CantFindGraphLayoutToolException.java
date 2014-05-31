package archstudio.comp.graphlayout;

import java.io.Serializable;

public class CantFindGraphLayoutToolException extends Exception implements Serializable{

	public CantFindGraphLayoutToolException(String reason){
		super(reason);
	}

}
