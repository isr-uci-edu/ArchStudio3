package archstudio.comp.xarchtrans;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.SAXException;

import c2.util.ReadWriteLock;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.xarchutils.*;
import edu.uci.isr.xarch.XArchInstanceMetadata;
import edu.uci.isr.xarch.XArchTypeMetadata;

public class XArchTransactionsC2Component extends AbstractC2DelegateBrick{
	
	protected XArchFlatInterface xarch;
	protected XArchFlatTransactionsInterface xarchtrans;
	
	protected ReadWriteLock transactionReadWriteLock = new ReadWriteLock();
	
	public XArchTransactionsC2Component(Identifier id){
		super(id);
		this.addMessageProcessor(new XArchTransactionsMessageProcessor());
		//xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		xarch = new XArchFlatImpl();
		xarchtrans = new XArchFlatTransactionsImpl(xarch);
		//deployCustomService(this, bottomIface, bottomIface, xarchtrans, new Class[]{XArchFlatTransactionsInterface.class, XArchFlatInterface.class}, new Class[]{});
		EBIWrapperUtils.deployService(
			this, bottomIface, bottomIface, xarchtrans, 
			new Class[]{XArchFlatTransactionsInterface.class, XArchFlatInterface.class,
				XArchFlatQueryInterface.class}, 
			new Class[]{ /*XArchFlatListener.class,*/ XArchFileListener.class});
		
		//Deploy our filtered state change interface
		
		EBIStateChangeAdapter sca = new EBIStateChangeAdapter(this, bottomIface, xarchtrans, XArchFlatListener.class,
			new TransMessageFilter());
		Vector v = (Vector)this.getProperty("stateChangeAdapters");
		if(v == null){
			v = new Vector();
		}
		v.addElement(sca);
		this.setProperty("stateChangeAdapters", v);
	}
	
	class TransMessageFilter implements MessageFilter{
		public boolean accept(Message m){
			if(m instanceof NamedPropertyMessage){
				try{
					XArchFlatEvent evt = (XArchFlatEvent)((NamedPropertyMessage)m).getParameter("paramValue0");
					if(evt.getSource().getUID().startsWith("$$dup_trans:")){
						//System.out.println("Blocked a message.");
						return false;
					}
					if(((ObjRef)evt.getTarget()).getUID().startsWith("$$dup_trans:")){
						//System.out.println("Blocked a message.");
						return false;
					}
				}
				catch(Throwable t){
				}
			}
			return true;
		}
	}
						
			

	class XArchTransactionsMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			//System.out.println(m);
			if(m.getDestination().getInterfaceIdentifier().equals(topIface.getIdentifier())){
				if(!(m instanceof c2.legacy.conn.FilterInterestMessage)){
					sendNotification(m);
				}
			}
		}
	}

	/*
	public void deployCustomService(DelegateBrick b, Interface callIface, Interface stateChangeIface, 
	Object api, Class[] interfaces, Class[] stateChangeInterfaces){
		for(int i = 0; i < interfaces.length; i++){
			CallProcessor cp = new TransactionsCallProcessor(b, callIface, api, interfaces[i]);
			EBIWrapperUtils.addThreadMessageProcessor(b, cp);

			//Add an exclusive interest filter
			EBIInterestLifecycleAdapter eila = new EBIInterestLifecycleAdapter(b, callIface, 
				new EBIInterestFilter(interfaces[i].getName().toString()));
			b.addLifecycleProcessor(eila);
		}
		for(int i = 0; i < stateChangeInterfaces.length; i++){
			EBIStateChangeAdapter sca = new EBIStateChangeAdapter(b, stateChangeIface, api, stateChangeInterfaces[i]);
			Vector v = (Vector)b.getProperty("stateChangeAdapters");
			if(v == null){
				v = new Vector();
			}
			v.addElement(sca);
			b.setProperty("stateChangeAdapters", v);
		}
	}
	
	class TransactionsCallProcessor extends CallProcessor{
		public TransactionsCallProcessor(DelegateBrick b, Interface iface, Object api, Class interfaceClass){
			super(b, iface, api, interfaceClass);
		}
		
		public void handle(Message m){
			if(!(m instanceof NamedPropertyMessage)){
				return;
			}
			//System.out.println("It was an NPM");
			//System.out.println("Transactor got message: " + m);
			NamedPropertyMessage npm = (NamedPropertyMessage)m;
	
			String targetClass = (String)npm.getParameter("targetInterface");
			if(targetClass == null){
				return;
			}
			else if(!targetClass.equals(interfaceClass.getName())){
				//Not targeted to our interface
				return;
			}
			
			String methodName = npm.getName();

			//System.out.println("*****Transactor got message for us!  Name was: " + methodName);
			
			if(methodName.startsWith("add") ||
				methodName.startsWith("set") ||
				methodName.startsWith("clear") ||
				methodName.startsWith("promote") ||
				methodName.startsWith("recontextualize") ||
				methodName.startsWith("createTransaction") ||
				methodName.startsWith("remove") ||
				methodName.startsWith("close") ||
				methodName.startsWith("commit") ||
				methodName.startsWith("rollback")){
				
				super.handle(m);
				return;
			}
			else{
				transactionReadWriteLock.getReadLock();
				npm.addParameter("targetInterface", "edu.uci.ics.xarchutils.XArchFlatInterface");
				sendToAll(m, topIface);
				transactionReadWriteLock.releaseLock();
			}
		}
	}
	*/
	
	public class XArchFlatTransactionsImpl implements XArchFlatTransactionsInterface{
	
		private static final String DUP_PREFIX = "$$dup_trans:";
	
		protected XArchFlatInterface xarch;
		
		//Maps ObjRefs of IXArch elements to Transaction objects
		protected Hashtable openTransactions;
		
		//Maps Transactions to OperationLists
		protected Hashtable operationLists;
		protected Object transactionLock = new Object();
		
		public XArchFlatTransactionsImpl(XArchFlatInterface xarch){
			this.xarch = xarch;
			openTransactions = new Hashtable();
			operationLists = new Hashtable();
		}
		
		public Transaction createTransaction(ObjRef xArchRef){
			if(!isInstanceOf(xArchRef, "edu.uci.isr.xarch.IXArch")){
				throw new IllegalArgumentException("xArchRef passed to createTransaction(...) must refer to an IXArch.");
			}
			synchronized(transactionLock){
				while(true){
					Transaction currentlyOpenTransaction = (Transaction)openTransactions.get(xArchRef);
					if(currentlyOpenTransaction == null){
						Transaction t = new Transaction(xArchRef);
						openTransactions.put(xArchRef, t);
						operationLists.put(t, new OperationList());
						return t;
					}
					else{
						try{
							transactionLock.wait();
						}
						catch(InterruptedException e){
						}
					}
				}
			}
		}
		
		/*
		class TransactionAggregatorMessageProcessor implements MessageProcessor, XArchFlatListener{
			OperationList oplist = null;
	
			String cmdName = null;
			ObjRef cmdBaseObjRef = null;
			int index = 0;
			int subIndex = 0;
			int subListLength = -1;
			
			List evtList;
			
			public TransactionAggregatorMessageProcessor(OperationList oplist){
				this.oplist = oplist;
				evtList = new ArrayList(oplist.list.size());
				index = 0;
				subIndex = 0;
				next();
				addMessageProcessor(this);
			}
			
			protected synchronized void setupCommand(int cmdIndex){
				index = cmdIndex;
				subIndex = 0;
				List cmdList = (List)oplist.list.get(index);
				cmdName = (String)cmdList.get(0);  //get the command name
				cmdBaseObjRef = (ObjRef)cmdList.get(1); //get the base object ref
				if(cmdName.equals("add2") || (cmdName.equals("remove2"))){
					ObjRef[] refs = (ObjRef[])cmdList.get(3);
					subListLength = refs.length;
				}
				else{
					subListLength = -1;
				}
			}
			
			protected synchronized void next(){
				if(cmdName == null){
					setupCommand(0);
					return;
				}
				else{
					if(subListLength != -1){
						subIndex++;
						if(subIndex >= subListLength){
							index++;
							if(index == oplist.list.size()){
								//Last command done!
								cleanupAndSend();
								return;
							}
							else{
								setupCommand(index);
							}
						}
					}
					else{
						index++;
						if(index == oplist.list.size()){
							//Last command done!
							cleanupAndSend();
							return;
						}
						else{
							setupCommand(index);
						}
					}
				}
			}
			
			protected boolean matches(XArchFlatEvent evt){
				//System.out.println("Checking match.");
				if(evt.getSource().equals(cmdBaseObjRef)){
					int eventType = evt.getEventType();
					if(cmdName.startsWith("add")){
						if(eventType == XArchFlatEvent.ADD_EVENT){
							System.out.println("Match");
							return true;
						}
					}
					else if(cmdName.startsWith("remove")){
						if(eventType == XArchFlatEvent.REMOVE_EVENT){
							System.out.println("Match");
							return true;
						}
					}
					else if(cmdName.startsWith("clear")){
						if(eventType == XArchFlatEvent.CLEAR_EVENT){
							System.out.println("Match");
							return true;
						}
					}
					else if(cmdName.startsWith("set")){
						if(eventType == XArchFlatEvent.SET_EVENT){
							System.out.println("Match");
							return true;
						}
					}
					else if(cmdName.startsWith("promote")){
						if(eventType == XArchFlatEvent.PROMOTE_EVENT){
							System.out.println("Match");
							return true;
						}
					}
				}
				System.out.println("Not this time");
				return false;
			}
		
			public void cleanupAndSend(){
				//removeMessageProcessor(this);
				XArchFlatEvent[] eventArray = (XArchFlatEvent[])evtList.toArray(new XArchFlatEvent[0]);
				System.out.println("We did it: " + c2.util.ArrayUtils.arrayToString(eventArray));
				sendToAll(new XArchTransactionEvent(eventArray), bottomIface);
			}
			
			public synchronized void handleStateChangeEvent(XArchFlatEvent evt){
				if(matches(evt)){
					evtList.add(evt);
					next();
				}
			}
			
			public synchronized void handleXArchFlatEvent(XArchFlatEvent evt){
				handleStateChangeEvent(evt);
			}
			
			public void handle(Message m){
				if(m instanceof NamedPropertyMessage){
					NamedPropertyMessage npm = (NamedPropertyMessage)m;
					try{
						if(npm.getBooleanParameter("stateChangeMessage")){
							Object evtObject = npm.getParameter("paramValue0");
							if(evtObject instanceof XArchFlatEvent){
								XArchFlatEvent evt = (XArchFlatEvent)evtObject;
								handleStateChangeEvent(evt);
							}
							return;
						}
					}
					catch(Exception e){
					}
				}
				return;
			}
		}					
		*/
		
		class TransactionMessageAggregator implements XArchFlatListener{
			ArrayList eventList = new ArrayList();
			
			public TransactionMessageAggregator(){
			}
			
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				eventList.add(evt);
			}

			public void done(){
				XArchFlatEvent[] eventArray = (XArchFlatEvent[])eventList.toArray(new XArchFlatEvent[0]);
				XArchTransactionEvent evt = new XArchTransactionEvent(eventArray);
				//System.out.println(c2.util.ArrayUtils.arrayToString(eventArray));
				sendToAll(evt, bottomIface);
			}
		}
		
		private void releaseTransaction(Transaction t){
			synchronized(transactionLock){
				openTransactions.remove(t.getXArchRef());
				operationLists.remove(t);
				transactionLock.notifyAll();
			}
		}
		
		public void commit(Transaction t){
			ObjRef xArchRef = t.getXArchRef();
			validateTransaction(t, xArchRef);
			//Don't permit reads when the document is being transactionally updated.
			transactionReadWriteLock.getWriteLock();
			try{
				OperationList ol = (OperationList)operationLists.get(t);
				//addMessageProcessor(new TransactionAggregatorMessageProcessor(ol));
				
				//TransactionAggregatorMessageProcessor tamp = new TransactionAggregatorMessageProcessor(ol);
				//((XArchFlatImpl)xarch).addXArchFlatListener(tamp);
				TransactionMessageAggregator tma = new TransactionMessageAggregator();
				((XArchFlatImpl)xarch).addXArchFlatListener(tma);
				ol.playbackList(xarch);
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				xarch.forgetAllWithPrefix(t.getXArchRef(), DUP_PREFIX);
				//((XArchFlatImpl)xarch).removeXArchFlatListener(tamp);
				tma.done();
				releaseTransaction(t);
			}
			finally{
				transactionReadWriteLock.releaseLock();
			}
		}
		
		public void rollback(Transaction t){
			validateTransaction(t, t.getXArchRef());
			xarch.forgetAllWithPrefix(t.getXArchRef(), DUP_PREFIX);
			releaseTransaction(t);
		}
	
		public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.add(baseObjectRef, typeOfThing, thingToAddRef);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.add(baseObjectRef, typeOfThing, thingsToAddRefs);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public void clear(ObjRef baseObjectRef, String typeOfThing){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.clear(baseObjectRef, typeOfThing);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public void remove(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToRemove){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.remove(baseObjectRef, typeOfThing, thingToRemove);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
		
		public void remove(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToRemove){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.remove(baseObjectRef, typeOfThing, thingsToRemove);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public void set(ObjRef baseObjectRef, String typeOfThing, String value){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.set(baseObjectRef, typeOfThing, value);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public void set(ObjRef baseObjectRef, String typeOfThing, ObjRef value){
			ObjRef xArchRef = getXArch(baseObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				xarch.set(baseObjectRef, typeOfThing, value);
				tma.done();
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public ObjRef promoteTo(ObjRef contextObjectRef, String promotionTarget, ObjRef targetObjectRef){
			ObjRef xArchRef = getXArch(targetObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			TransactionMessageAggregator tma = new TransactionMessageAggregator();
			((XArchFlatImpl)xarch).addXArchFlatListener(tma);
			try{
				ObjRef ref = xarch.promoteTo(contextObjectRef, promotionTarget, targetObjectRef);
				tma.done();
				return ref;
			}
			finally{
				((XArchFlatImpl)xarch).removeXArchFlatListener(tma);
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
	
		public ObjRef recontextualize(ObjRef contextObjectRef, String typeOfThing, ObjRef targetObjectRef){
			ObjRef xArchRef = getXArch(targetObjectRef);
			Transaction t = createTransaction(xArchRef);
			transactionReadWriteLock.getWriteLock();
			try{
				return xarch.recontextualize(contextObjectRef, typeOfThing, targetObjectRef);
			}
			finally{
				transactionReadWriteLock.releaseLock();
				releaseTransaction(t);
			}
		}
		
		protected void validateTransaction(Transaction t, ObjRef baseObjectRef){
			ObjRef xArchRef = getXArch(baseObjectRef);
			if(t == null){
				throw new InvalidTransactionException(t, "Transaction cannot be null.");
			}
			synchronized(transactionLock){
				Transaction openTransaction = (Transaction)openTransactions.get(xArchRef);
				if(openTransaction == null){
					throw new InvalidTransactionException(t, "No transaction currently open on that document.");
				}
				if(!openTransaction.equals(t)){
					throw new InvalidTransactionException(t, "Not the currently open transaction.");
				}
			}
		}
			
		public void add(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			ObjRef thingToAddRefDup = new ObjRef(DUP_PREFIX + thingToAddRef.getUID());
			if(!xarch.isValidObjRef(thingToAddRefDup)){
				xarch.cloneXArchElementDepthOne(thingToAddRef, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.add(baseObjectRefDup, typeOfThing, thingToAddRefDup);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordAdd(baseObjectRef, typeOfThing, thingToAddRef);
		}
		
		public void add(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			ObjRef[] thingsToAddRefsDups = new ObjRef[thingsToAddRefs.length];
			
			for(int i = 0; i < thingsToAddRefs.length; i++){
				ObjRef thingToAddRefDup = new ObjRef(DUP_PREFIX + thingsToAddRefs[i].getUID());
				if(!xarch.isValidObjRef(thingToAddRefDup)){
					xarch.cloneXArchElementDepthOne(thingsToAddRefs[i], DUP_PREFIX);
				}
				thingsToAddRefsDups[i] = thingToAddRefDup;
			}
			
			//If this works, we can record it as part of the transaction
			xarch.add(baseObjectRefDup, typeOfThing, thingsToAddRefsDups);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordAdd(baseObjectRef, typeOfThing, thingsToAddRefs);
		}
		
		public void clear(Transaction t, ObjRef baseObjectRef, String typeOfThing){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.clear(baseObjectRefDup, typeOfThing);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordClear(baseObjectRef, typeOfThing);
		}
		
		public void remove(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef thingToRemove){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			ObjRef thingToRemoveDup = new ObjRef(DUP_PREFIX + thingToRemove.getUID());
			if(!xarch.isValidObjRef(thingToRemoveDup)){
				xarch.cloneXArchElementDepthOne(thingToRemove, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.remove(baseObjectRefDup, typeOfThing, thingToRemoveDup);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordRemove(baseObjectRef, typeOfThing, thingToRemove);
		}
		
		public void remove(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToRemove){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			ObjRef[] thingsToRemoveDups = new ObjRef[thingsToRemove.length];
			
			for(int i = 0; i < thingsToRemove.length; i++){
				ObjRef thingToRemoveDup = new ObjRef(DUP_PREFIX + thingsToRemove[i].getUID());
				if(!xarch.isValidObjRef(thingToRemoveDup)){
					xarch.cloneXArchElementDepthOne(thingsToRemove[i], DUP_PREFIX);
				}
				thingsToRemoveDups[i] = thingToRemoveDup;
			}
			
			//If this works, we can record it as part of the transaction
			xarch.remove(baseObjectRefDup, typeOfThing, thingsToRemoveDups);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordRemove(baseObjectRef, typeOfThing, thingsToRemove);
		}
		
		public void set(Transaction t, ObjRef baseObjectRef, String typeOfThing, String value){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			//System.out.println("Made the base object ref.");
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				//System.out.println("Oops!  Dooping!");
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.set(baseObjectRefDup, typeOfThing, value);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordSet(baseObjectRef, typeOfThing, value);
		}
		
		public void set(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef value){
			validateTransaction(t, baseObjectRef);
			ObjRef baseObjectRefDup = new ObjRef(DUP_PREFIX + baseObjectRef.getUID());
			if(!xarch.isValidObjRef(baseObjectRefDup)){
				xarch.cloneXArchElementDepthOne(baseObjectRef, DUP_PREFIX);
			}
			
			ObjRef valueDup = new ObjRef(DUP_PREFIX + value.getUID());
			if(!xarch.isValidObjRef(valueDup)){
				xarch.cloneXArchElementDepthOne(value, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.set(baseObjectRefDup, typeOfThing, valueDup);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordSet(baseObjectRef, typeOfThing, value);
		}
		
		public void promoteTo(Transaction t, ObjRef contextObjectRef, String promotionTarget, ObjRef targetObjectRef){
			validateTransaction(t, targetObjectRef);
			
			ObjRef targetObjectRefDup = new ObjRef(DUP_PREFIX + targetObjectRef.getUID());
			if(!xarch.isValidObjRef(targetObjectRefDup)){
				xarch.cloneXArchElementDepthOne(targetObjectRef, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.promoteTo(contextObjectRef, promotionTarget, targetObjectRefDup);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordPromoteTo(contextObjectRef, promotionTarget, targetObjectRef);
		}
		
		public void recontextualize(Transaction t, ObjRef contextObjectRef, String typeOfThing, ObjRef targetObjectRef){
			validateTransaction(t, targetObjectRef);
			
			ObjRef targetObjectRefDup = new ObjRef(DUP_PREFIX + targetObjectRef.getUID());
			if(!xarch.isValidObjRef(targetObjectRefDup)){
				xarch.cloneXArchElementDepthOne(targetObjectRef, DUP_PREFIX);
			}
			
			//If this works, we can record it as part of the transaction
			xarch.recontextualize(contextObjectRef, typeOfThing, targetObjectRefDup);
			OperationList l = (OperationList)operationLists.get(t);
			l.recordRecontextualize(contextObjectRef, typeOfThing, targetObjectRef);
		}
		
		public void close(String urlString){
			ObjRef xArchRef = getOpenXArch(urlString);
			close(xArchRef);
		}
		
		public void close(ObjRef xArchRef){
			Transaction t = createTransaction(getXArch(xArchRef));
			try{
				xarch.close(xArchRef);
			}
			finally{
				releaseTransaction(t);
			}
		}
	
		/**********************************************************/
		
		public ObjRef createXArch(String url){
			return xarch.createXArch(url);
		}
	
		public void renameXArch(String oldURI, String newURI){
			xarch.renameXArch(oldURI, newURI);
		}
		
		public ObjRef cloneXArch(ObjRef xArchRef, String newURL){
			return xarch.cloneXArch(xArchRef, newURL);
		}
	
		public ObjRef parseFromFile(String fileName) throws FileNotFoundException, IOException, SAXException{
			return xarch.parseFromFile(fileName);
		}
	
		public ObjRef parseFromURL(String urlString) throws MalformedURLException, IOException, SAXException{
			return xarch.parseFromURL(urlString);
		}
	
		public String serialize(ObjRef xArchRef){
			return xarch.serialize(xArchRef);
		}
		
		public ObjRef getByID(ObjRef xArchRef, String id){
			return xarch.getByID(xArchRef, id);
		}
		
		public ObjRef getByID(String id){
			return xarch.getByID(id);
		}
		
		public Object get(ObjRef baseObjectRef, String typeOfThing){
			return xarch.get(baseObjectRef, typeOfThing);
		}
	
		public ObjRef get(ObjRef baseObjectRef, String typeOfThing, String id){
			return xarch.get(baseObjectRef, typeOfThing, id);
		}
		
		public ObjRef[] get(ObjRef baseObjectRef, String typeOfThing, String[] ids){
			return xarch.get(baseObjectRef, typeOfThing, ids);
		}
	
		public ObjRef[] getAll(ObjRef baseObjectRef, String typeOfThing){
			return xarch.getAll(baseObjectRef, typeOfThing);
		}
	
		public boolean has(ObjRef baseObjectRef, String typeOfThing, String valueToCheck){
			return xarch.has(baseObjectRef, typeOfThing, valueToCheck);
		}
	
		public boolean has(ObjRef baseObjectRef, String typeOfThing, ObjRef valueToCheck){
			return xarch.has(baseObjectRef, typeOfThing, valueToCheck);
		}
	
		public boolean hasAll(ObjRef baseObjectRef, String typeOfThing, ObjRef[] valuesToCheck){
			return xarch.hasAll(baseObjectRef, typeOfThing, valuesToCheck);
		}
	
		public boolean[] has(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToCheck){
			return xarch.has(baseObjectRef, typeOfThing, thingsToCheck);
		}
		
		public boolean isEqual(ObjRef baseObjectRef, ObjRef thingToCheck){
			return xarch.isEqual(baseObjectRef, thingToCheck);
		}
	
		public boolean isEquivalent(ObjRef baseObjectRef, ObjRef thingToCheck){
			return xarch.isEquivalent(baseObjectRef, thingToCheck);
		}
	
		public ObjRef createContext(ObjRef xArchObject, String contextType){
			return xarch.createContext(xArchObject, contextType);
		}
	
		public ObjRef create(ObjRef contextObjectRef, String typeOfThing){
			return xarch.create(contextObjectRef, typeOfThing);
		}
		
		public ObjRef createElement(ObjRef contextObjectRef, String typeOfThing){
			return xarch.createElement(contextObjectRef, typeOfThing);
		}
	
		public ObjRef getXArch(ObjRef baseObjectRef){
			return xarch.getXArch(baseObjectRef);
		}
	
		public ObjRef getElement(ObjRef contextObjectRef, String typeOfThing, ObjRef xArchObjectRef){
			return xarch.getElement(contextObjectRef, typeOfThing, xArchObjectRef);
		}
		
		public ObjRef[] getAllElements(ObjRef contextObjectRef, String typeOfThing, ObjRef xArchObjectRef){
			return xarch.getAllElements(contextObjectRef, typeOfThing, xArchObjectRef);
		}
		
		public XArchTypeMetadata getTypeMetadata(ObjRef baseObjectRef){
			return xarch.getTypeMetadata(baseObjectRef);
		}
		
		public XArchInstanceMetadata getInstanceMetadata(ObjRef baseObjectRef){
			return xarch.getInstanceMetadata(baseObjectRef);
		}
		
		public String getType(ObjRef baseObjectRef){
			return xarch.getType(baseObjectRef);
		}
		
		public boolean isInstanceOf(ObjRef baseObjectRef, String className){
			return xarch.isInstanceOf(baseObjectRef, className);
		}
	
		public ObjRef[] getAllAncestors(ObjRef targetObjectRef){
			return xarch.getAllAncestors(targetObjectRef);
		}
		
		public ObjRef getParent(ObjRef targetObjectRef){
			return xarch.getParent(targetObjectRef);
		}
		
		public ObjRef resolveHref(ObjRef xArchRef, String href){
			return xarch.resolveHref(xArchRef, href);
		}
	
		public String[] getOpenXArchURLs(){
			return getOpenXArchURIs();
		}
		
		public String[] getOpenXArchURIs(){
			return xarch.getOpenXArchURIs();
		}
		
		public ObjRef[] getOpenXArches(){
			return xarch.getOpenXArches();
		}
		
		public ObjRef getOpenXArch(String url){
			return xarch.getOpenXArch(url);
		}
		
		public String getXArchURL(ObjRef xArchRef){
			return getXArchURI(xArchRef);
		}
		
		public String getXArchURI(ObjRef xArchRef){
			return xarch.getXArchURI(xArchRef);
		}
		
		public boolean isValidObjRef(ObjRef ref){
			return xarch.isValidObjRef(ref);
		}
		
		public ObjRef cloneXArchElementDepthOne(ObjRef ref, String prefix){
			return null;
		}
		
		public void forgetAllWithPrefix(ObjRef xArchRef, String prefix){
		}
		
		public ObjRef cloneElement(ObjRef targetObjectRef, int depth){
			return xarch.cloneElement(targetObjectRef, depth);
		}
		
		public ObjRef[] getReferences(ObjRef xArchRef, String id){
			return xarch.getReferences(xArchRef, id);
		}
	
		public boolean isAttached(ObjRef childRef){
			return xarch.isAttached(childRef);
		}
		
		public boolean hasAncestor(ObjRef childRef, ObjRef ancestorRef){
			return xarch.hasAncestor(childRef, ancestorRef);
		}
		
		public void dump(ObjRef ref){
			xarch.dump(ref);
		}
		
		public void addXArchFlatListener(XArchFlatListener l){
			((XArchFlatImpl)xarch).addXArchFlatListener(l);
		}
		
		public void removeXArchFlatListener(XArchFlatListener l){
			((XArchFlatImpl)xarch).removeXArchFlatListener(l);
		}
		
		public void addXArchFileListener(XArchFileListener l){
			((XArchFlatImpl)xarch).addXArchFileListener(l);
		}
		
		public void removeXArchFileListener(XArchFileListener l){
			((XArchFlatImpl)xarch).removeXArchFileListener(l);
		}
		
		public void writeToFile(ObjRef xArchRef, String fileName) throws java.io.IOException{
			xarch.writeToFile(xArchRef, fileName);
		}
		
		public String getElementName(ObjRef xArchRef){
			return xarch.getElementName(xArchRef);
		}
		
		public XArchPath getXArchPath(ObjRef ref){
			return xarch.getXArchPath(ref);
		}
		
		public ObjRef resolveXArchPath(ObjRef xArchRef, XArchPath xArchPath){
			return xarch.resolveXArchPath(xArchRef, xArchPath);
		}

		public XArchBulkQueryResults bulkQuery(XArchBulkQuery q) {
			return xarch.bulkQuery(q);
		}
	}
	
}
