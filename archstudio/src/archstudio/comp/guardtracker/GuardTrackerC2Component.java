package archstudio.comp.guardtracker;

import archstudio.comp.booleannotation.IBooleanNotation;
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

import java.util.*;

import edu.uci.ics.xarchutils.*;

public class GuardTrackerC2Component extends AbstractC2DelegateBrick{
	protected XArchFlatInterface xarch;
	protected IBooleanNotation bni;
	
	//Maps xArchRefs to GuardedDocuments
	protected Map guardedDocumentMap;

	public GuardTrackerC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );
		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				handleFlatEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);
		bni = (IBooleanNotation)EBIWrapperUtils.addExternalService(this, topIface, IBooleanNotation.class);
		
		guardedDocumentMap = new HashMap();
		addMessageProcessor(new GuardTrackerC2ComponentMessageProcessor());
		addLifecycleProcessor(new GuardTrackerLifecycleProcessor());
	}
	
	protected DelayedExecutor delayedExecutor = null;
	
	class GuardTrackerLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			delayedExecutor = new DelayedExecutor();
			delayedExecutor.setDaemon(true);
			delayedExecutor.setPriority(Thread.NORM_PRIORITY / 2);
			delayedExecutor.start();
		}
		
		public void end(){
			if(delayedExecutor != null){
				delayedExecutor.terminate();
				delayedExecutor = null;
			}
		}
	}
	
	class DelayedExecutor extends Thread{
		boolean terminate = false;
		Set docsToUpdate = new HashSet();
		
		public synchronized void doUpdateDocument(ObjRef xArchRef){
			//System.out.println("queueing for update: " + xArchRef);
			docsToUpdate.add(xArchRef);
			this.interrupt();
		}
		
		public synchronized void terminate(){
			this.terminate = true;
			this.notifyAll();
		}
		
		public synchronized void run(){
			while(!terminate){
				try{
					this.wait();
				}
				catch(InterruptedException ie){}
				if(terminate) return;
				//Okay, something got put in the queue.  Let's wait a couple seconds to process.
				while(true){
					try{
						this.wait(2000);
						break;
					}
					catch(InterruptedException ie2){}
				}
				if(terminate) return;
				ObjRef[] refsToUpdate = (ObjRef[])docsToUpdate.toArray(new ObjRef[0]);
				docsToUpdate.clear();
				for(int i = 0; i < refsToUpdate.length; i++){
					updateDocument(refsToUpdate[i]);
				}
			}
		}
	}
	
	class GuardTrackerC2ComponentMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof GetAllGuardsMessage){
				handleGetAllGuards();
			}
		}
	}
	
	public void handleGetAllGuards(){
		ObjRef[] xArchRefs = getAllXArchRefs();
		for(int i = 0; i < xArchRefs.length; i++){
			sendGuardsMessage(xArchRefs[i]);
		}
		sendAllGuardsMessage();
	}
	
	public void putGuardedDocument(ObjRef xArchRef, GuardedDocument guardedDocument){
		guardedDocumentMap.put(xArchRef, guardedDocument);
	}
	
	public ObjRef[] getAllXArchRefs(){
		return (ObjRef[])guardedDocumentMap.keySet().toArray(new ObjRef[0]);
	}
	
	public GuardedDocument[] getAllGuardedDocuments(){
		return (GuardedDocument[])guardedDocumentMap.values().toArray(new GuardedDocument[0]);
	}
	
	public GuardedDocument getGuardedDocument(ObjRef xArchRef){
		return (GuardedDocument)guardedDocumentMap.get(xArchRef);
	}
	
	public void removeGuardedDocument(ObjRef xArchRef){
		guardedDocumentMap.remove(xArchRef);
	}
	
	protected void sendGuardsMessage(ObjRef xArchRef){
		String[] guardStrings = getAllGuards(xArchRef);
		GuardsMessage gm = new GuardsMessage(xArchRef, guardStrings);
		sendToAll(gm, bottomIface);
	}
	
	protected void sendAllGuardsMessage(){
		String[] guardStrings = getAllGuards();
		AllGuardsMessage agm = new AllGuardsMessage(guardStrings);
		sendToAll(agm, bottomIface);
	}
	
	public void handleFileEvent(XArchFileEvent evt){
		switch(evt.getEventType()){
		case XArchFileEvent.XARCH_CREATED_EVENT:
		case XArchFileEvent.XARCH_OPENED_EVENT:
			handleNewDocument(evt.getXArchRef());
			break;
		case XArchFileEvent.XARCH_CLOSED_EVENT:
			handleClosedDocument(evt.getXArchRef());
			break;
		}
	}
	
	public void handleFlatEvent(XArchFlatEvent evt){
		if(documentNeedsUpdate(evt)){
			ObjRef xArchRef = null;
			if(evt.getSource() != null){
				xArchRef = xarch.getXArch(evt.getSource());
			}
			if(xArchRef == null){
				if((evt.getTarget() != null) && (evt.getTarget() instanceof ObjRef)){
					xArchRef = xarch.getXArch((ObjRef)evt.getTarget());
				}
			}
			if(xArchRef != null){
				if(delayedExecutor == null){
					updateDocument(xArchRef);
				}
				else{
					delayedExecutor.doUpdateDocument(xArchRef);
				}
			}
		}
	}
	
	protected void updateDocument(ObjRef xArchRef){
		//System.out.println("updating document in guardtracker");
		try{
			removeGuardedDocument(xArchRef);
			GuardedDocument guardedDocument = parseDocument(xArchRef);
			putGuardedDocument(xArchRef, guardedDocument);

			sendGuardsMessage(xArchRef);
			sendAllGuardsMessage();
		}
		catch(Exception e){
			//If the document was closed before parsing, for example,
			//we don't want to kill the component.
			System.err.println("Warning: Exception in GuardTracker: " + e);
		}
	}
	
	public boolean documentNeedsUpdate(XArchFlatEvent evt){
		int eventType = evt.getEventType();
		
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null) sourcePathString = sourcePath.toTagsOnlyString();
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null) targetPathString = targetPath.toTagsOnlyString();
		
		if(sourcePath != null){
			for(int i = 0; i < sourcePath.getLength(); i++){
				String segment = sourcePath.getTagName(i);
				if(segment.equals("optional")){
					return true;
				}
				if(segment.equals("variant")){
					return true;
				}
			}
		}
		if(targetPath != null){
			for(int i = 0; i < targetPath.getLength(); i++){
				String segment = targetPath.getTagName(i);
				if(segment.equals("optional")){
					return true;
				}
				if(segment.equals("variant")){
					return true;
				}
			}
		}
		
		if((eventType == XArchFlatEvent.SET_EVENT) || (eventType == XArchFlatEvent.ADD_EVENT)){
			//If any of these were added then we should refresh
			if(targetPathString != null){
				if(targetPathString.equals("xArch/archStructure")) return true;
				if(targetPathString.equals("xArch/archStructure/component")) return true;
				if(targetPathString.equals("xArch/archStructure/component/interface")) return true;
				if(targetPathString.equals("xArch/archStructure/connector")) return true;
				if(targetPathString.equals("xArch/archStructure/connector/interface")) return true;
				if(targetPathString.equals("xArch/archStructure/link")) return true;
				if(targetPathString.equals("xArch/archTypes")) return true;
				if(targetPathString.equals("xArch/archTypes/componentType")) return true;
				if(targetPathString.equals("xArch/archTypes/componentType/signature")) return true;
				if(targetPathString.equals("xArch/archTypes/connectorType")) return true;
				if(targetPathString.equals("xArch/archTypes/connectorType/signature")) return true;
			}
		}
		else if((eventType == XArchFlatEvent.CLEAR_EVENT) || (eventType == XArchFlatEvent.REMOVE_EVENT)){
			if(targetPathString != null){
				if(targetPathString.equals("archStructure")) return true;
				if(targetPathString.equals("component")) return true;
				if(targetPathString.equals("connector")) return true;
				if(targetPathString.equals("interface")) return true;
				if(targetPathString.equals("link")) return true;
				if(targetPathString.equals("archTypes")) return true;
				if(targetPathString.equals("componentType")) return true;
				if(targetPathString.equals("connectorType")) return true;
				if(targetPathString.equals("signature")) return true;
			}
		}
		return false;
	}
	
	public static XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/component*/optional/guard");
		q.addQueryPath("archStructure*/connector*/optional/guard");
		q.addQueryPath("archStructure*/component*/interface*/optional/guard");
		q.addQueryPath("archStructure*/connector*/interface*/optional/guard");
		q.addQueryPath("archStructure*/link*/optional/guard");

		q.addQueryPath("archTypes*/componentType*/variant*/guard");
		q.addQueryPath("archTypes*/componentType*/signature*/optional/guard");
		q.addQueryPath("archTypes*/connectorType*/variant*/guard");
		q.addQueryPath("archTypes*/connectorType*/signature*/optional/guard");
		
		return q;
	}
	
	private XArchFlatQueryInterface runBulkQuery(ObjRef xArchRef){
		//ObjRef xArchRef = xarch.getXArch(structureRef);
		XArchBulkQuery q = getBulkQuery(xArchRef);
		XArchBulkQueryResults qr = xarch.bulkQuery(q);
		return new XArchBulkQueryResultProxy(xarch, qr);
	}
	
	public void handleNewDocument(ObjRef xArchRef){
		GuardedDocument guardedDocument = parseDocument(xArchRef);
		putGuardedDocument(xArchRef, guardedDocument);
		sendGuardsMessage(xArchRef);
		sendAllGuardsMessage();
	}
	
	public void handleClosedDocument(ObjRef xArchRef){
		removeGuardedDocument(xArchRef);
		sendAllGuardsMessage();
	}
	
	public String[] getAllGuards(ObjRef xArchRef){
		Set allGuardStrings = new HashSet();
		GuardedDocument doc = getGuardedDocument(xArchRef);
		if(doc != null){
			GuardedNode[] guardedNodes = doc.getAllGuardedNodes();
			for(int i = 0; i < guardedNodes.length; i++){
				String[] guardStrings = guardedNodes[i].getAllGuardStrings();
				for(int j = 0; j < guardStrings.length; j++){
					if(guardStrings[j] != null) allGuardStrings.add(guardStrings[j]);
				}
			}
		}
		String[] guardStrings = (String[])allGuardStrings.toArray(new String[0]);
		Arrays.sort(guardStrings);
		return guardStrings;
	}
	
	public String[] getAllGuards(){
		Set allGuardStrings = new HashSet();
		GuardedDocument[] docs = getAllGuardedDocuments();
		for(int j = 0; j < docs.length; j++){
			GuardedNode[] guardedNodes = docs[j].getAllGuardedNodes();
			for(int i = 0; i < guardedNodes.length; i++){
				String[] guardStrings = guardedNodes[i].getAllGuardStrings();
				for(int k = 0; j < guardStrings.length; j++){
					if(guardStrings[k] != null) allGuardStrings.add(guardStrings[k]);
				}
			}
		}
		String[] guardStrings = (String[])allGuardStrings.toArray(new String[0]);
		Arrays.sort(guardStrings);
		return guardStrings;
	}
	
	public GuardedDocument parseDocument(ObjRef xArchRef){
		GuardedDocument guardedDocument = new GuardedDocument(xArchRef);
		XArchFlatQueryInterface xarchbulk = runBulkQuery(xArchRef);
		
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarchbulk.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		ObjRef[] archTypesRefs = xarchbulk.getAllElements(typesContextRef, "ArchTypes", xArchRef);
		
		for(int i = 0; i < archStructureRefs.length; i++){
			ObjRef archStructureRef = archStructureRefs[i];
			//Optional Components
			ObjRef[] componentRefs = xarchbulk.getAll(archStructureRef, "component");
			for(int j = 0; j < componentRefs.length; j++){
				ObjRef componentRef = componentRefs[j];
				ObjRef optionalRef = (ObjRef)xarchbulk.get(componentRef, "optional");
				if(optionalRef != null){
					ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef, "guard");
					if(guardRef != null){
						GuardedNode guardedNode = parseOptional(componentRef);
						if(guardedNode != null){
							guardedDocument.putGuardedNode(componentRef, guardedNode);
						}
					}
				}
				//Optional interfaces on components
				ObjRef[] interfaceRefs = (ObjRef[])xarchbulk.getAll(componentRef, "interface");
				for(int k = 0; k < interfaceRefs.length; k++){
					ObjRef interfaceRef = interfaceRefs[k];
					ObjRef optionalRef2 = (ObjRef)xarchbulk.get(interfaceRef, "optional");
					if(optionalRef2 != null){
						ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef2, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(interfaceRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(interfaceRef, guardedNode);
							}
						}
					}
				}
			}
			//Optional Connectors
			ObjRef[] connectorRefs = xarchbulk.getAll(archStructureRef, "connector");
			for(int j = 0; j < connectorRefs.length; j++){
				ObjRef connectorRef = connectorRefs[j];
				ObjRef optionalRef = (ObjRef)xarchbulk.get(connectorRef, "optional");
				if(optionalRef != null){
					ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef, "guard");
					if(guardRef != null){
						GuardedNode guardedNode = parseOptional(connectorRef);
						if(guardedNode != null){
							guardedDocument.putGuardedNode(connectorRef, guardedNode);
						}
					}
				}
				//Optional interfaces on connectors
				ObjRef[] interfaceRefs = (ObjRef[])xarchbulk.getAll(connectorRef, "interface");
				for(int k = 0; k < interfaceRefs.length; k++){
					ObjRef interfaceRef = interfaceRefs[k];
					ObjRef optionalRef2 = (ObjRef)xarchbulk.get(interfaceRef, "optional");
					if(optionalRef2 != null){
						ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef2, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(interfaceRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(interfaceRef, guardedNode);
							}
						}
					}
				}
			}
			
			//Optional Links
			ObjRef[] linkRefs = xarchbulk.getAll(archStructureRef, "link");
			for(int j = 0; j < linkRefs.length; j++){
				ObjRef linkRef = linkRefs[j];
				ObjRef optionalRef = (ObjRef)xarchbulk.get(linkRef, "optional");
				if(optionalRef != null){
					ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef, "guard");
					if(guardRef != null){
						GuardedNode guardedNode = parseOptional(linkRef);
						if(guardedNode != null){
							guardedDocument.putGuardedNode(linkRef, guardedNode);
						}
					}
				}
			}
		}
		
		for(int i = 0; i < archTypesRefs.length; i++){
			ObjRef archTypesRef = archTypesRefs[i];
			//Variant component types
			ObjRef[] componentTypeRefs = xarchbulk.getAll(archTypesRef, "componentType");
			for(int j = 0; j < componentTypeRefs.length; j++){
				ObjRef componentTypeRef = componentTypeRefs[j];
				ObjRef[] variantRefs = xarchbulk.getAll(componentTypeRef, "variant");
				if(variantRefs.length > 0){
					GuardedNode guardedNode = parseVariant(componentTypeRef);
					if(guardedNode != null){
						guardedDocument.putGuardedNode(componentTypeRef, guardedNode);
					}
				}
				//Optional signatures on component types
				ObjRef[] signatureRefs = (ObjRef[])xarchbulk.getAll(componentTypeRef, "signature");
				for(int k = 0; k < signatureRefs.length; k++){
					ObjRef interfaceRef = signatureRefs[k];
					ObjRef optionalRef2 = (ObjRef)xarchbulk.get(interfaceRef, "optional");
					if(optionalRef2 != null){
						ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef2, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(interfaceRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(interfaceRef, guardedNode);
							}
						}
					}
				}
			}
			
			//Variant connector types
			ObjRef[] connectorTypeRefs = xarchbulk.getAll(archTypesRef, "connectorType");
			for(int j = 0; j < connectorTypeRefs.length; j++){
				ObjRef connectorTypeRef = connectorTypeRefs[j];
				ObjRef[] variantRefs = xarchbulk.getAll(connectorTypeRef, "variant");
				if(variantRefs.length > 0){
					GuardedNode guardedNode = parseVariant(connectorTypeRef);
					if(guardedNode != null){
						guardedDocument.putGuardedNode(connectorTypeRef, guardedNode);
					}
				}
				//Optional signatures on connector types
				ObjRef[] signatureRefs = (ObjRef[])xarchbulk.getAll(connectorTypeRef, "signature");
				for(int k = 0; k < signatureRefs.length; k++){
					ObjRef interfaceRef = signatureRefs[k];
					ObjRef optionalRef2 = (ObjRef)xarchbulk.get(interfaceRef, "optional");
					if(optionalRef2 != null){
						ObjRef guardRef = (ObjRef)xarchbulk.get(optionalRef2, "guard");
						if(guardRef != null){
							GuardedNode guardedNode = parseOptional(interfaceRef);
							if(guardedNode != null){
								guardedDocument.putGuardedNode(interfaceRef, guardedNode);
							}
						}
					}
				}
			}
		}
		
		return guardedDocument;
	}
	
	public GuardedNode parseOptional(ObjRef nodeRef){
		GuardedNode guardedNode = new GuardedNode(nodeRef);
		try{
			ObjRef optionalRef = (ObjRef)xarch.get(nodeRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				String guardString = bni.booleanGuardToString(optionalRef);
				guardedNode.putGuard(guardRef, guardString);
			}
			return guardedNode;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public GuardedNode parseVariant(ObjRef nodeRef){
		try{
			ObjRef[] variantRefs = xarch.getAll(nodeRef, "variant");
			if(variantRefs.length == 0){
				return null;
			}
			GuardedNode guardedNode = new GuardedNode(nodeRef);
			for(int i = 0; i < variantRefs.length; i++){
				ObjRef guardRef = (ObjRef)xarch.get(variantRefs[i], "guard");
				String guardString = bni.booleanGuardToString(variantRefs[i]);
				guardedNode.putGuard(guardRef, guardString);
			}
			return guardedNode;
		}
		catch(Exception e){
			return null;
		}
	}
	
	static class GuardedDocument{
		protected ObjRef xArchRef;
		
		//Maps node ObjRefs to GuardedNodes
		protected Map guardNodeMap;
		
		public GuardedDocument(ObjRef xArchRef){
			this.xArchRef = xArchRef;
			guardNodeMap = new HashMap();
		}
		
		public ObjRef getXArchRef(){
			return xArchRef;
		}
		
		public ObjRef[] getAllNodeRefs(){
			return (ObjRef[])guardNodeMap.keySet().toArray(new ObjRef[0]);
		}
		
		public GuardedNode[] getAllGuardedNodes(){
			return (GuardedNode[])guardNodeMap.values().toArray(new GuardedNode[0]);
		}
		
		public void putGuardedNode(ObjRef nodeRef, GuardedNode guardedNode){
			guardNodeMap.put(nodeRef, guardedNode);
		}
		
		public GuardedNode getGuardedNode(ObjRef nodeRef){
			return (GuardedNode)guardNodeMap.get(nodeRef);
		}
		
		public void removeGuardedNode(ObjRef nodeRef){
			guardNodeMap.remove(nodeRef);
		}
	}
	
	static class GuardedNode{
		protected ObjRef nodeRef;
		
		//Maps guard ObjRefs to guard Strings;
		protected Map guardMap;

		public GuardedNode(ObjRef nodeRef){
			this.nodeRef = nodeRef;
			guardMap = new HashMap();
		}
		
		public ObjRef getNodeRef(){
			return nodeRef;
		}
		
		public void putGuard(ObjRef guardRef, String guardString){
			guardMap.put(guardRef, guardString);
		}
		
		public String getGuardString(ObjRef guardRef){
			return (String)guardMap.get(guardRef);
		}
		
		public void removeGuard(ObjRef guardRef){
			guardMap.remove(guardRef);
		}
		
		public ObjRef[] getAllGuardRefs(){
			return (ObjRef[])guardMap.keySet().toArray(new ObjRef[0]);
		}
		
		public String[] getAllGuardStrings(){
			return (String[])guardMap.values().toArray(new String[0]);
		}
	}
	
}
