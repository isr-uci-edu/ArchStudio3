package archstudio.comp.critics.typelink;

import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class TypeLinkCriticC2Component extends AbstractCritic{
	public static final Identifier ISSUE_ELEMENT_MISSING_TYPE =
		new SimpleIdentifier("ISSUE_ELEMENT_MISSING_TYPE");
	public static final Identifier ISSUE_INVALID_TYPE_LINK =
		new SimpleIdentifier("ISSUE_INVALID_TYPE_LINK");
	public static final Identifier ISSUE_TYPE_LINK_BROKEN =
		new SimpleIdentifier("ISSUE_TYPE_LINK_BROKEN");
	
	public TypeLinkCriticC2Component(Identifier id){
		super(id);
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{};
	}
	
	protected CriticIssue getElementMissingTypeIssue(ObjRef xArchRef, ObjRef elementRef, String elementID){
		if(elementID == null){
			elementID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_ELEMENT_MISSING_TYPE,
			"Architecture Element Missing Type",
			"For type reasoning, each architecture element (component, connector, " +
			"or interface) should have a link to its type.",
			"Element with ID " + elementID + " in architecture " +
			"should have a valid type link.",
			xArchRef,
			new ObjRef[]{elementRef}
		);
	}
	
	protected CriticIssue getInvalidTypeLinkIssue(ObjRef xArchRef, ObjRef elementRef, String elementID){
		if(elementID == null){
			elementID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("broken_link"),
			"Broken Link",
			ISSUE_TYPE_LINK_BROKEN,
			"Invalid Type Link",
			"The XML Link from an architecture element to its type is invalid.",
			"Element with ID " + elementID + " in architecture " +
			"has a broken or un-resolvable type link.",
			xArchRef,
			new ObjRef[]{elementRef}
		);
	}
	
	protected CriticIssue getElementTypeLinkBroken(ObjRef xArchRef, ObjRef elementRef, String elementID){
		if(elementID == null){
			elementID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("broken_link"),
			"Broken Link",
			ISSUE_TYPE_LINK_BROKEN,
			"Broken Type Link",
			"The XML Link from an architecture element to its type could " +
			"not be resolved or is broken.",
			"Element with ID " + elementID + " in architecture " +
			"has a broken or un-resolvable type link.",
			xArchRef,
			new ObjRef[]{elementRef}
		);
	}
	
	public String getDescription(){
		return "The type link critic checks to make sure each element " +
			"(component, connector, or interface) in a xADL 2.0 design-time description " + 
			"has a (valid) link to a type.";
	}
	
	protected void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		
		ArrayList issueList = new ArrayList();
		
		for(int j = 0; j < archStructureRefs.length; j++){
			
			ObjRef[] componentRefs = xarch.getAll(archStructureRefs[j], "Component");
			ObjRef[] connectorRefs = xarch.getAll(archStructureRefs[j], "Connector");

			for(int i = 0; i < (componentRefs.length + connectorRefs.length); i++){
				ObjRef brickRef;
				if(i < componentRefs.length){
					brickRef = componentRefs[i];
				}
				else{
					brickRef = connectorRefs[i - componentRefs.length];
				}
				ObjRef typeRef = (ObjRef)xarch.get(brickRef, "Type");
				if(typeRef == null){
					String id = (String)xarch.get(brickRef, "Id");
					issueList.add(getElementMissingTypeIssue(xArchRef, brickRef, id));
				}
				else{
					//Cool, it has a typeRef
					String xlinkType = (String)xarch.get(typeRef, "type");
					if(xlinkType == null){
						String id = (String)xarch.get(brickRef, "Id");
						issueList.add(getInvalidTypeLinkIssue(xArchRef, brickRef, id));						
					}
					else if((xlinkType != null) && (xlinkType.equals("simple"))){
						//We can only process simple XLinks
						String href = (String)xarch.get(typeRef, "href");
						if(href == null){
							String id = (String)xarch.get(brickRef, "Id");
							issueList.add(getElementMissingTypeIssue(xArchRef, brickRef, id));
						}
						else{
							try{
								ObjRef targetRef = xarch.resolveHref(xarch.getXArch(typeRef), href);
								if(targetRef == null){
									String id = (String)xarch.get(brickRef, "Id");
									issueList.add(getElementTypeLinkBroken(xArchRef, brickRef, id));
								}
							}
							catch(IllegalArgumentException iae){
								String id = (String)xarch.get(brickRef, "Id");
								issueList.add(getElementTypeLinkBroken(xArchRef, brickRef, id));
							}
						}
					}
				}
			}
		}
		CriticIssue[] issues = (CriticIssue[])issueList.toArray(new CriticIssue[0]);
		replaceIssues(xArchRef, issues);
		setBusy(false);
	}

	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		//String architectureURI = xarch.getXArchURI(xArchRef);
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
