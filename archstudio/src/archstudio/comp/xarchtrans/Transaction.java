package archstudio.comp.xarchtrans;

import edu.uci.ics.xarchutils.*;

public class Transaction{
	
	private ObjRef xArchRef;
	private long transactionId;
	private static long nextTransactionId = 1000;
	
	public Transaction(ObjRef xArchRef){
		this.xArchRef = xArchRef;
		this.transactionId = nextTransactionId++;
	}
	
	public ObjRef getXArchRef(){
		return xArchRef;
	}

	public boolean equals(Object o){
		if(!(o instanceof Transaction)) return false;
		Transaction otherTransaction = (Transaction)o;
		return transactionId == otherTransaction.transactionId;
	}
	
	public int hashCode(){
		return (int)((long)transactionId & (long)0xffff);
	}
}
