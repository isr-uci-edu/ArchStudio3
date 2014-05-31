package archstudio.comp.critics.aeminst;

import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class AEMInstantiateCriticC2Component extends AbstractCritic{
	
	public static final Identifier ISSUE_AEM_CANNOT_INSTANTIATE =
		new SimpleIdentifier("ISSUE_AEM_CANNOT_INSTANTIATE");
	
	public static final Identifier[] SHOWSTOPPER_ISSUE_TYPE_IDS = new Identifier[]{
		archstudio.comp.critics.typelink.TypeLinkCriticC2Component.ISSUE_ELEMENT_MISSING_TYPE,
		archstudio.comp.critics.typelink.TypeLinkCriticC2Component.ISSUE_INVALID_TYPE_LINK,
		archstudio.comp.critics.typelink.TypeLinkCriticC2Component.ISSUE_TYPE_LINK_BROKEN,
		archstudio.comp.critics.somebrick.SomeBrickCriticC2Component.ISSUE_ARCHSTRUCTURE_HAS_NO_BRICKS,
		archstudio.comp.critics.link.LinkCriticC2Component.ISSUE_BROKEN_LINK_ENDPOINT,
		archstudio.comp.critics.link.LinkCriticC2Component.ISSUE_ENDPOINT_TARGET_NOT_INTERFACE,
		archstudio.comp.critics.link.LinkCriticC2Component.ISSUE_INVALID_LINK_ENDPOINT,
		archstudio.comp.critics.link.LinkCriticC2Component.ISSUE_INVALID_LINK_ENDPOINT_SET,
		archstudio.comp.critics.link.LinkCriticC2Component.ISSUE_POINT_HAS_NO_ANCHOR,
		archstudio.comp.critics.javaimpl.JavaImplementationCriticC2Component.ISSUE_JAVA_IMPLEMENTATION_MISSING_MAIN_CLASS,
		archstudio.comp.critics.javaimpl.JavaImplementationCriticC2Component.ISSUE_TYPE_MISSING_IMPLEMENTATION,
		archstudio.comp.critics.archstructure.ArchStructureCriticC2Component.ISSUE_ARCHSTRUCTURE_MISSING_ID
	};	
	
	protected Set openShowstoppers = new HashSet();
	
	public AEMInstantiateCriticC2Component(Identifier id){
		super(id);
		this.addMessageProcessor(new CriticIssueMessageProcessor());
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{
			new SimpleIdentifier("TypeLinkCritic"),
			new SimpleIdentifier("JavaImplCritic"),
			new SimpleIdentifier("ArchStructureCritic"),
			new SimpleIdentifier("LinkCritic"),
			new SimpleIdentifier("SomeBrickCritic")
		};
	}
	
	protected boolean isShowstopper(CriticIssue issue){
		Identifier issueTypeID = issue.getIssueTypeID();
		
		for(int i = 0; i < SHOWSTOPPER_ISSUE_TYPE_IDS.length; i++){
			if(issueTypeID.equals(SHOWSTOPPER_ISSUE_TYPE_IDS[i])){
				return true;
			}
		}
		return false;
	}
		
	protected CriticIssue getAEMCannotInstantiateIssue(ObjRef xArchRef, ObjRef structureRef, 
	String structureID,	CriticIssue[] underlyingIssues){
		if(structureID == null){
			structureID = "[unknown]";
		}
		String addlInfo = "";
		if(underlyingIssues.length > 0){
			addlInfo = " due to the following other issues: ";
			for(int i = 0; i < underlyingIssues.length; i++){
				addlInfo += underlyingIssues[i].getHeadline();
				addlInfo += "; ";
			}
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("aem"),
			"Architecture Evolution Manager",
			ISSUE_AEM_CANNOT_INSTANTIATE,
			"AEM Cannot Instantiate Architecture",
			"To instantiate a design-time description of an architecture, many criteria " +
			"must be met.  Some of these criteria include: all components and connectors " +
			"must have a valid type, those types must have (Java) implementations completely " + 
			"specified, and all brick-to-brick links must be valid.",
			"AEM cannot instantiate design-time architecture " + structureID + 
			" in architecture. " + addlInfo,
			xArchRef,
			new ObjRef[]{structureRef}
		);
	}
	
	public String getDescription(){
		return "The AEM Instantiate Critic checks to see whether the Architecture " +
			"Evolution Manager can instantiate a design-time architecture into a " + 
			"running system.";
	}
	
	protected synchronized void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		
		//Make sure we're checking an open document.
		if(!xarch.isValidObjRef(xArchRef)) return;
		
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		ObjRef[] archTypesRefs = xarch.getAllElements(typesContextRef, "ArchTypes", xArchRef);
		
		ArrayList issueList = new ArrayList();
		
		ArrayList showstopperList = new ArrayList();

		for(int j = 0; j < archStructureRefs.length; j++){
			String archStructureID = (String)xarch.get(archStructureRefs[j], "Id");
			
			//System.out.println("Checking open showstoppers.");
			for(Iterator it = openShowstoppers.iterator(); it.hasNext(); ){
				CriticIssue showstopper = (CriticIssue)it.next();
				//System.out.println("Showstopper under consideration.");
				if(showstopper.getXArchRef().equals(xArchRef)){
					//System.out.println("Architecture URI matches!");
					ObjRef[] affectedElements = showstopper.getAffectedElements();
					for(int i = 0; i < affectedElements.length; i++){
						//System.out.println("Checking if I'm affected...");
						if(xarch.hasAncestor(affectedElements[i], archStructureRefs[j])){
							//System.out.println("I am!");
							showstopperList.add(showstopper);
							break;
						}
						else{
							for(int k = 0; k < archTypesRefs.length; k++){
								if(xarch.hasAncestor(affectedElements[i], archTypesRefs[k])){
									showstopperList.add(showstopper);
									break;
								}
							}
						}
					}
				}
			}
			
			if(showstopperList.size() != 0){
				CriticIssue[] showstoppers = (CriticIssue[])showstopperList.toArray(new CriticIssue[0]);
				issueList.add(getAEMCannotInstantiateIssue(xArchRef, archStructureRefs[j],
					archStructureID, showstoppers));
			}
		}
		CriticIssue[] issues = (CriticIssue[])issueList.toArray(new CriticIssue[0]);
		replaceIssues(xArchRef, issues);
		setBusy(false);
	}

	class CriticIssueMessageProcessor implements MessageProcessor{
		public synchronized void handle(Message m){
			if(!isActive()) return;
			if(m instanceof CriticIssueMessage){
				CriticIssueMessage cim = (CriticIssueMessage)m;
				int status = cim.getStatus();
				CriticIssue issue = cim.getIssue();
				if(isShowstopper(issue)){
					if(status == CriticIssueMessage.ISSUE_CLOSED){
						openShowstoppers.remove(issue);
						checkDocument(issue.getXArchRef());
					}
					else if(status == CriticIssueMessage.ISSUE_OPEN){
						//System.out.println("adding showstopper: " + issue);
						openShowstoppers.add(issue);
						checkDocument(issue.getXArchRef());
					}
				}
			}
		}
	}
	
	protected void doActivate(){
		super.doActivate();
		sendToAll(new CriticGetIssuesMessage(getDependencies()), topIface);
	}
		
	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		checkDocument(xArchRef);
	}
	
	protected void handleFileEvent(XArchFileEvent evt){
		if(!isActive()) return;
		int type = evt.getEventType();
		if((type == XArchFileEvent.XARCH_CREATED_EVENT) ||
			(type == XArchFileEvent.XARCH_OPENED_EVENT)){
			checkDocument(evt.getXArchRef());
		}
	}

}