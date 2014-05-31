package archstudio.tron;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class TronTestResultMessage extends NamedPropertyMessage{
	public TronTestResultMessage(TronTest test, ObjRef documentRef, TronIssue[] issues){
		super("TronTestResultMessage");
		super.addParameter("test", test);
		super.addParameter("documentRef", documentRef);
		super.addParameter("issues", issues);
	}

	protected TronTestResultMessage(TronTestResultMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronTestResultMessage(this);
	}

	public void setTest(TronTest test){
		addParameter("test", test);
	}

	public TronTest getTest(){
		return (TronTest)getParameter("test");
	}

	public void setDocumentRef(ObjRef documentRef){
		addParameter("documentRef", documentRef);
	}

	public ObjRef getDocumentRef(){
		return (ObjRef)getParameter("documentRef");
	}
	
	public void setIssues(TronIssue[] issues){
		addParameter("issues", issues);
	}

	public TronIssue[] getIssues(){
		return (TronIssue[])getParameter("issues");
	}

}

