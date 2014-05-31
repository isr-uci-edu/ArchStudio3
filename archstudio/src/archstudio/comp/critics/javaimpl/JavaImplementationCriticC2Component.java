package archstudio.comp.critics.javaimpl;

import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class JavaImplementationCriticC2Component extends AbstractCritic{
	public static final Identifier ISSUE_TYPE_MISSING_IMPLEMENTATION =
		new SimpleIdentifier("ISSUE_TYPE_MISSING_IMPLEMENTATION");
	public static final Identifier ISSUE_JAVA_IMPLEMENTATION_MISSING_MAIN_CLASS =
		new SimpleIdentifier("ISSUE_JAVA_IMPLEMENTATION_MISSING_MAIN_CLASS");
	
	public JavaImplementationCriticC2Component(Identifier id){
		super(id);
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{};
	}
	
	protected CriticIssue getTypeMissingImplementationIssue(ObjRef xArchRef, ObjRef typeRef, String typeID){
		if(typeID == null){
			typeID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_TYPE_MISSING_IMPLEMENTATION,
			"Architecture Type Missing Implementation",
			"An architecture cannot be instantiated without implementations " +
			"for each of its component, connector, and interface types.",
			"Type with ID " + typeID + " in architecture should have an implementation specified.",
			xArchRef,
			new ObjRef[]{typeRef}
		);
	}
	
	protected CriticIssue getJavaImplementationMissingMainClassIssue(ObjRef xArchRef, ObjRef implRef, String typeID){
		if(typeID == null){
			typeID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_JAVA_IMPLEMENTATION_MISSING_MAIN_CLASS,
			"Java Implementation Missing Main Class",
			"For the Java Implementation specification of a type to be valid, " +
			"the implementation must specify a main class.",
			"Type with ID " + typeID + " in architecture " +
			"has a Java Implementation with no specified main class or an " + 
			"incompletely specified main class.",
			xArchRef,
			new ObjRef[]{implRef}
		);
	}
	
	public String getDescription(){
		return "The Java implementation critic checks to make sure each element type" +
			"(component, connector, or interface type) in a xADL 2.0 design-time description " + 
			"has an implementation.  If that implementation is a Java implementation, it also " + 
			"checks to see if that implementation has a main class.";
	}
	
	private static ObjRef getJavaImplementation(XArchFlatInterface xarch, ObjRef typeRef){
		ObjRef[] implementationRefs = xarch.getAll(typeRef, "implementation");
		for(int i = 0; i < implementationRefs.length; i++){
			boolean isJavaImplementation = xarch.isInstanceOf(implementationRefs[i], "edu.uci.isr.xarch.javaimplementation.IJavaImplementation");
			if(isJavaImplementation){
				return implementationRefs[i];
			}
		}
		return null;
	}

	protected void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef implementationContextRef = xarch.createContext(xArchRef, "implementation");
		ObjRef javaimplementationContextRef = xarch.createContext(xArchRef, "javaimplementation");
		ObjRef[] archTypesRefs = xarch.getAllElements(typesContextRef, "ArchTypes", xArchRef);
		
		ArrayList issueList = new ArrayList();
		
		for(int j = 0; j < archTypesRefs.length; j++){
			ObjRef[] componentTypeRefs = xarch.getAll(archTypesRefs[j], "ComponentType");
			ObjRef[] connectorTypeRefs = xarch.getAll(archTypesRefs[j], "ConnectorType");
			ObjRef[] interfaceTypeRefs = xarch.getAll(archTypesRefs[j], "InterfaceType");

			for(int i = 0; i < (componentTypeRefs.length + connectorTypeRefs.length + interfaceTypeRefs.length); i++){
				ObjRef typeRef;
				if(i < componentTypeRefs.length){
					typeRef = componentTypeRefs[i];
				}
				else if(i < (componentTypeRefs.length + connectorTypeRefs.length)){
					typeRef = connectorTypeRefs[i - componentTypeRefs.length];
				}
				else{
					typeRef = interfaceTypeRefs[i - componentTypeRefs.length - connectorTypeRefs.length];
				}
				
				String typeID = (String)xarch.get(typeRef, "Id");
				
				ObjRef implRef = null;
				try{
					implRef = getJavaImplementation(xarch, typeRef);
					if(implRef == null){
						issueList.add(getTypeMissingImplementationIssue(xArchRef, typeRef, typeID));
					}
				}
				catch(Exception e){
					//this type has no implementation.
					issueList.add(getTypeMissingImplementationIssue(xArchRef, typeRef, typeID));
				}
				
				if(implRef != null){
					//Type has an implementation.
					//We can only proceed with the criticism if it's a Java implementation, though.
					if(xarch.isInstanceOf(implRef, "edu.uci.isr.xarch.javaimplementation.IJavaImplementation")){
						ObjRef mainClassRef = (ObjRef)xarch.get(implRef, "MainClass");
						//System.out.println("Got main class ref: " + mainClassRef);
						//System.out.println("The type was: " + xarch.get(typeRef, "Id"));
						if(mainClassRef == null){
							issueList.add(getJavaImplementationMissingMainClassIssue(xArchRef, implRef, typeID));
						}
						else{
							ObjRef mainClassNameRef = (ObjRef)xarch.get(mainClassRef, "JavaClassName");
							if(mainClassNameRef == null){
								issueList.add(getJavaImplementationMissingMainClassIssue(xArchRef, implRef, typeID));
							}
							else{
								String mainClassName = (String)xarch.get(mainClassNameRef, "Value");
								if(mainClassName == null){
									issueList.add(getJavaImplementationMissingMainClassIssue(xArchRef, implRef, typeID));
								}
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

	protected boolean isPossiblyRelevant(ObjRef xArchRef, XArchFlatEvent evt){
		//System.out.println(evt.getSourcePath());
		String name = evt.getTargetName();
		if(name == null){
			return true;
		}
		return
			(evt.getEventType() == XArchFlatEvent.PROMOTE_EVENT) ||
			name.equals("interfaceType") ||
			name.equals("connectorType") ||
			name.equals("componentType") ||
			name.equals("implementation") ||
			name.equals("mainClass") ||
			name.equals("auxClass") ||
			name.equals("$SIMPLETYPEVALUE$") ||
			name.equals("javaClassName") ||
			name.equals("archTypes") ||
			name.equals("xArch");
	}

	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		//String architectureURI = xarch.getXArchURI(xArchRef);
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
	

