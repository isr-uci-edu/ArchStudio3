package archstudio.comp.critics.archstructure;

import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class ArchStructureCriticC2Component extends AbstractCritic{
	public static final Identifier ISSUE_ARCHSTRUCTURE_MISSING_ID =
		new SimpleIdentifier("ISSUE_ARCHSTRUCTURE_MISSING_ID");
	public static final Identifier ISSUE_ARCHSTRUCTURE_MISSING_DESCRIPTION =
		new SimpleIdentifier("ISSUE_ARCHSTRUCTURE_MISSING_DESCRIPTION");
	
	public ArchStructureCriticC2Component(Identifier id){
		super(id);
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{};
	}
	
	protected CriticIssue getIDIssue(ObjRef xArchRef, ObjRef archStructureRef){
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_ARCHSTRUCTURE_MISSING_ID,
			"Design Time Architecture Missing Identifier",
			"All design-time architectures (ArchStructure elements) must " +
			"have an identifier for them to be valid.",
			"ArchStructure tag in architecture must have an ID.",
			xArchRef,
			new ObjRef[]{archStructureRef}
		);
	}
	
	protected CriticIssue getDescriptionIssue(ObjRef xArchRef, ObjRef archStructureRef){
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_ARCHSTRUCTURE_MISSING_DESCRIPTION,
			"Design Time Architecture Missing Description",
			"All design-time architectures (ArchStructure elements) must " +
			"have a description for them to be valid.",
			"ArchStructure tag in architecture must have a Description.",
			xArchRef,
			new ObjRef[]{archStructureRef}
		);
	}
	
	public String getDescription(){
		return "The ArchStructure critic ensures that the ArchStructure " +
			"elements in a xADL 2.0 document have descriptions and identifiers.";
	}
	
	protected void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		for(int j = 0; j < archStructureRefs.length; j++){
			String id = (String)xarch.get(archStructureRefs[j], "id");
			if(id == null){
				CriticIssue issue = getIDIssue(xArchRef, archStructureRefs[j]);
				addIssue(issue);
			}
			else{
				CriticIssue issue = getIDIssue(xArchRef, archStructureRefs[j]);
				removeIssue(issue);
			}

			ObjRef descriptionRef = (ObjRef)xarch.get(archStructureRefs[j], "Description");
			if(descriptionRef == null){
				CriticIssue issue = getDescriptionIssue(xArchRef, archStructureRefs[j]);
				addIssue(issue);
			}
			else{
				String descString = (String)xarch.get(descriptionRef, "value");
				if(descString == null){
					CriticIssue issue = getDescriptionIssue(xArchRef, archStructureRefs[j]);
					addIssue(issue);
				}
				else{
					CriticIssue issue = getDescriptionIssue(xArchRef, archStructureRefs[j]);
					removeIssue(issue);
				}
			}
		}
		setBusy(false);
	}

	protected boolean isPossiblyRelevant(ObjRef xArchRef, XArchFlatEvent evt){
		String name = evt.getTargetName();
		//System.out.println("event name is " + name);
		if(name == null){
			return true;
		}
		return
			name.equals("id") ||
			name.equals("xArch") ||
			name.equals("archStructure") ||
			name.equals("$SIMPLETYPEVALUE$") ||
			name.equals("description");
	}
	
	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		if(isPossiblyRelevant(xArchRef, evt)){
			checkDocument(xArchRef);
		}
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
