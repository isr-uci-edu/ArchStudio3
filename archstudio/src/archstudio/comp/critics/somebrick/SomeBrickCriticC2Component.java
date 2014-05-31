package archstudio.comp.critics.somebrick;

import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class SomeBrickCriticC2Component extends AbstractCritic{
	public static final Identifier ISSUE_ARCHSTRUCTURE_HAS_NO_BRICKS =
		new SimpleIdentifier("ISSUE_ARCHSTRUCTURE_HAS_NO_BRICKS");
	
	public SomeBrickCriticC2Component(Identifier id){
		super(id);
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{};
	}
	
	protected CriticIssue getNoBricksIssue(ObjRef xArchRef, ObjRef archStructureRef){
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_ARCHSTRUCTURE_HAS_NO_BRICKS,
			"Design-time Architecture Has No Bricks",
			"All design-time architectures (ArchStructure elements) should " +
			"have at least one component or connector for them to be valid.",
			"ArchStructure tag in architecture " +
			"should have at least one brick.",
			xArchRef,
			new ObjRef[]{archStructureRef}
		);
	}
	
	public String getDescription(){
		return "The SomeBricks critic checks that the ArchStructure " +
			"elements in a xADL 2.0 document contain at least one component or connector.";
	}
	
	protected void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		for(int j = 0; j < archStructureRefs.length; j++){
			ObjRef[] componentRefs = xarch.getAll(archStructureRefs[j], "Component");
			ObjRef[] connectorRefs = xarch.getAll(archStructureRefs[j], "Connector");
			if((componentRefs.length + connectorRefs.length) == 0){
				CriticIssue issue = getNoBricksIssue(xArchRef, archStructureRefs[j]);
				addIssue(issue);
			}
			else{
				CriticIssue issue = getNoBricksIssue(xArchRef, archStructureRefs[j]);
				removeIssue(issue);
			}
		}
		setBusy(false);
	}

	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		String architectureURI = xarch.getXArchURI(xArchRef);
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
