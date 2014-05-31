package archstudio.tron;

public class TronTestError extends TronToolNotice{
	
	protected String testUID;
	
	public TronTestError(String testUID, String message, String additionalDetail, Throwable error){
		super(message, additionalDetail, error);
		this.testUID = testUID;
	}
	
	public String getTestUID(){
		return testUID;
	}

	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
}
