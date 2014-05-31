package archstudio.tron;

public class TronToolNotice implements java.io.Serializable{

	protected String message;
	protected String additionalDetail;
	protected Throwable error;
	
	public TronToolNotice(String message, String additionalDetail, Throwable error){
		super();
		this.message = message;
		this.additionalDetail = additionalDetail;
		this.error = error;
	}
	
	public String getAdditionalDetail(){
		return additionalDetail;
	}

	public void setAdditionalDetail(String additionalDetail){
		this.additionalDetail = additionalDetail;
	}
	
	public Throwable getError(){
		return error;
	}
	
	public void setError(Throwable error){
		this.error = error;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
}
