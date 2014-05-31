package archstudio.comp.xarchtrans;

public class InvalidTransactionException extends RuntimeException{

	private Transaction transaction;
	
	public InvalidTransactionException(Transaction transaction){
		this.transaction = transaction;
	}
	
	public InvalidTransactionException(Transaction transaction, String message){
		super(message);
		this.transaction = transaction;
	}

}
