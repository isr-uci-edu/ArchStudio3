package edu.uci.ics.xadlutils;

import java.util.ArrayList;

import archstudio.comp.aem.InvalidArchitectureDescriptionException;

import edu.uci.ics.xarchutils.*;

public class XadlUtils{
	
	/*
	public static void main(String[] args){
		XArchFlatInterface xarch = new XArchFlatImpl();
		ObjRef xArchRef = xarch.createXArch("urn:foo");
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef archStructureRef1 = xarch.createElement(typesContextRef, "archStructure");
		ObjRef archStructureRef2 = xarch.createElement(typesContextRef, "archStructure");
		ObjRef componentRef = xarch.create(typesContextRef, "component");

		System.out.println("I'm now adding the id to the thing.");
		//xarch.dump(componentRef);
		xarch.set(componentRef, "id", "myid");
		//xarch.dump(componentRef);
		xarch.add(xArchRef, "Object", archStructureRef1);
		xarch.add(xArchRef, "Object", archStructureRef2);
		
		System.out.println("-----------------------STARTING");
		xarch.add(archStructureRef1, "component", componentRef);
		
		System.out.println(xarch.serialize(xArchRef));
		
		System.out.println("componentRef is:" + componentRef);
		ObjRef myidRef = xarch.getByID(xArchRef, "myid");
		System.out.println("myidRef is:" + myidRef);
		
		xarch.remove(archStructureRef1, "component", componentRef);
		xarch.add(archStructureRef2, "component", componentRef);
		
		System.out.println("componentRef is:" + componentRef);
		ObjRef myidRef2 = xarch.getByID(xArchRef, "myid");
		System.out.println("myidRef2 is:" + myidRef2);
	}
	*/
	
	public static ObjRef getArchTypes(XArchFlatInterface xarch, ObjRef xArchRef){
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", xArchRef);
		return archTypesRef;
	}
	
	public static String getHref(XArchFlatQueryInterface xarch, ObjRef ref){
		try{
			String href = (String)xarch.get(ref, "href");
			return href;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static String getID(XArchFlatQueryInterface xarch, ObjRef ref){
		try{
			String id = (String)xarch.get(ref, "id");
			return id;
		}
		catch(Exception e){
			//e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get the brick for an interface
	 * 
	 * @param xarch				the query interface
	 * @param interfaceName		the name of the interface
	 * @return the reference for the brick that owns the interface
	 */
	public static ObjRef getBrickForInterface(XArchFlatInterface xarch, String interfaceName) {
		return getBrickForInterface(xarch, xarch.getByID(interfaceName));
	}
	
	/**
	 * Get the brick for an interface
	 * 
	 * @param xarch				the query interface
	 * @param interfaceRef		the reference of the interface
	 * @return the reference for the brick that owns the interface
	 */
	public static ObjRef getBrickForInterface(XArchFlatInterface xarch, ObjRef interfaceRef) {
	    ObjRef	result = null;
	    if (interfaceRef != null)
	    	result = xarch.getParent(interfaceRef);
	    return result;
	}
	
	public static ObjRef resolveXLink(XArchFlatQueryInterface xarch, ObjRef ref){
		String linkTypeString = (String)xarch.get(ref, "type");
		if((linkTypeString == null) || (linkTypeString.equals("simple"))){
			String hrefString = (String)xarch.get(ref, "href");
			if(hrefString == null){
				return null;
			}
			else{
				ObjRef xArchRef = xarch.getXArch(ref);
				ObjRef targetRef = xarch.resolveHref(xArchRef, hrefString);
				return targetRef;
			}
		}
		else{
			throw new IllegalArgumentException("Can only resolve simple type XLinks.");
		}
	}

	public static ObjRef resolveXLink(XArchFlatQueryInterface xarch, ObjRef ref, String childName){
		ObjRef childRef = (ObjRef)xarch.get(ref, childName);
		if(childRef == null){
			return null;
		}
		else{
			return resolveXLink(xarch, childRef);
		}
	}
	
	public static ObjRef resolveTypeLink(XArchFlatQueryInterface xarch, ObjRef xArchRef, ObjRef brickRef) throws InvalidArchitectureDescriptionException{
		ObjRef typeLinkRef = (ObjRef)xarch.get(brickRef, "type");
		return resolveXLink(xarch, xArchRef, typeLinkRef);
	}
	
	
	public static ObjRef resolveXLink(XArchFlatQueryInterface xarch, ObjRef xArchRef, ObjRef xLinkRef) throws InvalidArchitectureDescriptionException{
		if(xLinkRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_ELEMENT_MISSING_TYPE);
		}
		String xlinkType = (String)xarch.get(xLinkRef, "type");
		if(xlinkType == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK);
		}				
		if(!xlinkType.equals("simple")){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_PROCESS_XLINK);
		}
		String xlinkHref = (String)xarch.get(xLinkRef, "href");
		
		//System.out.println("Got type href:" + xlinkHref);
		
		if(xlinkHref == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK);
		}
		ObjRef targetRef = xarch.resolveHref(xArchRef, xlinkHref);
		if(targetRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK_TARGET);
		}
		
		return targetRef;		
	}
	
	public static void setXLinkByHref(XArchFlatInterface xarch, ObjRef ref, String linkElementName, String href){
		boolean foundLinkRef = true;
		ObjRef linkRef = (ObjRef)xarch.get(ref, linkElementName);
		if(linkRef == null){
			foundLinkRef = false;
			ObjRef xArchRef = xarch.getXArch(ref);
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			linkRef = xarch.create(typesContextRef, "XMLLink"); 
		}
		xarch.set(linkRef, "type", "simple");
		xarch.set(linkRef, "href", href);
		if(!foundLinkRef){
			xarch.set(ref, linkElementName, linkRef);
		}
	}
	
	public static void setXLink(XArchFlatInterface xarch, ObjRef ref, String linkElementName, String targetID){
		setXLinkByHref(xarch, ref, linkElementName, "#" + targetID);
	}
	
	public static String getDescription(XArchFlatQueryInterface xarch, ObjRef ref){
		try{
			ObjRef descRef = (ObjRef)xarch.get(ref, "Description");
			if(descRef != null){
				String desc = (String)xarch.get(descRef, "Value");
				return desc;
			}
			return null;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static void setDescription(XArchFlatInterface xarch, ObjRef ref, String newDescription){
		try{
			ObjRef descRef = (ObjRef)xarch.get(ref, "Description");
			if(descRef != null){
				xarch.set(descRef, "Value", newDescription);
				return;
			}
			else{
				ObjRef xarchRef = xarch.getXArch(ref);
				ObjRef typesContextRef = xarch.createContext(xarchRef, "types");
				descRef = xarch.create(typesContextRef, "Description");
				xarch.set(descRef, "Value", newDescription);
				xarch.set(ref, "Description", descRef);
			}
		}
		catch(Exception e){
		}
	}
	
	public static String getDirection(XArchFlatQueryInterface xarch, ObjRef ref){
		try{
			ObjRef dirRef = (ObjRef)xarch.get(ref, "Direction");
			if(dirRef != null){
				String dir = (String)xarch.get(dirRef, "Value");
				return dir;
			}
			return null;
		}
		catch(Exception e){
			return null;
		}
	}	

	public static void setDirection(XArchFlatInterface xarch, ObjRef ref, String newDirection){
		try{
			ObjRef dirRef = (ObjRef)xarch.get(ref, "Direction");
			if(dirRef != null){
				xarch.set(dirRef, "Value", newDirection);
				return;
			}
			else{
				ObjRef xarchRef = xarch.getXArch(ref);
				ObjRef typesContextRef = xarch.createContext(xarchRef, "types");
				dirRef = xarch.create(typesContextRef, "Direction");
				xarch.set(dirRef, "Value", newDirection);
				xarch.set(ref, "Direction", dirRef);
			}
		}
		catch(Exception e){
		}
	}
	
	public static LinkInfo getLinkInfo(XArchFlatQueryInterface xarch, ObjRef linkRef, boolean resolve){
		LinkInfo li = new LinkInfo();
		
		li.setLinkRef(linkRef);
		
		ObjRef[] pointRefs = xarch.getAll(linkRef, "Point");
		
		ObjRef xArchRef = null;
		if(resolve){
			xArchRef = xarch.getXArch(linkRef);
		}
		
		if(pointRefs.length > 0){
			li.setPoint1Ref(pointRefs[0]);
			ObjRef anchor1Ref = (ObjRef)xarch.get(pointRefs[0], "AnchorOnInterface");
			if(anchor1Ref != null){
				li.setAnchor1Ref(anchor1Ref);
				String anchor1Type = (String)xarch.get(anchor1Ref, "type");
				li.setAnchor1Type(anchor1Type);
				String anchor1Href = (String)xarch.get(anchor1Ref, "href");
				li.setAnchor1Href(anchor1Href);
				
				if(resolve){
					ObjRef point1Target = xarch.resolveHref(xArchRef, anchor1Href);
					li.setPoint1Target(point1Target);
				}
			}
		}
		if(pointRefs.length > 1){
			li.setPoint2Ref(pointRefs[1]);
			ObjRef anchor2Ref = (ObjRef)xarch.get(pointRefs[1], "AnchorOnInterface");
			if(anchor2Ref != null){
				li.setAnchor2Ref(anchor2Ref);
				String anchor2Type = (String)xarch.get(anchor2Ref, "type");
				li.setAnchor2Type(anchor2Type);
				String anchor2Href = (String)xarch.get(anchor2Ref, "href");
				li.setAnchor2Href(anchor2Href);
				
				if(resolve){
					ObjRef point2Target = xarch.resolveHref(xArchRef, anchor2Href);
					li.setPoint2Target(point2Target);
				}
			}
		}
		return li;
	}

	//Gets all the signature-interface mappings in a type involving this signature
	public static ObjRef[] getSignatureInterfaceMappings(XArchFlatQueryInterface xarch, ObjRef typeRef, ObjRef signatureRef){
		ArrayList simRefList = new ArrayList();
		ObjRef subArchitectureRef = (ObjRef)xarch.get(typeRef, "subArchitecture");
		if(subArchitectureRef != null){
			ObjRef xArchRef = xarch.getXArch(subArchitectureRef);
			//System.out.println("got subArchitectureRef");
			ObjRef[] simRefs = xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");
			for(int i = 0; i < simRefs.length; i++){
				//System.out.println("checking SIM" + i);
				ObjRef sigLinkRef = (ObjRef)xarch.get(simRefs[i], "outerSignature");
				if(sigLinkRef != null){
					//System.out.println("got sig link ref");
					String href = getHref(xarch, sigLinkRef);
					if(href != null){
						//System.out.println("got sig link href");
						ObjRef referencedSigRef = xarch.resolveHref(xArchRef, href);
						//System.out.println("referencedSigRef = " + referencedSigRef);
						//System.out.println("referencedSigID = " + getID(xarch, referencedSigRef));
						//System.out.println("sigRef = " + signatureRef);
						//System.out.println("sigID= " + getID(xarch, signatureRef));
						if((referencedSigRef != null) && (referencedSigRef.equals(signatureRef))){
							//System.out.println("got referenced sig ref");
							simRefList.add(simRefs[i]);
						}
					}
				}
			}
		}
		return (ObjRef[])simRefList.toArray(new ObjRef[0]);
	}

	public static class LinkInfo{
		protected ObjRef linkRef;
		protected ObjRef point1Ref;
		protected ObjRef anchor1Ref;
		protected ObjRef point2Ref;
		protected ObjRef anchor2Ref;
		protected String anchor1Type;
		protected String anchor1Href;
		protected String anchor2Type;
		protected String anchor2Href;
		
		protected ObjRef point1Target;
		protected ObjRef point2Target;

		public String getAnchor1Href() {
			return anchor1Href;
		}

		public ObjRef getAnchor1Ref() {
			return anchor1Ref;
		}

		public String getAnchor1Type() {
			return anchor1Type;
		}

		public String getAnchor2Href() {
			return anchor2Href;
		}

		public ObjRef getAnchor2Ref() {
			return anchor2Ref;
		}

		public String getAnchor2Type() {
			return anchor2Type;
		}

		public ObjRef getLinkRef() {
			return linkRef;
		}

		public ObjRef getPoint1Ref() {
			return point1Ref;
		}

		public ObjRef getPoint1Target() {
			return point1Target;
		}

		public ObjRef getPoint2Ref() {
			return point2Ref;
		}

		public ObjRef getPoint2Target() {
			return point2Target;
		}

		public void setAnchor1Href(String string) {
			anchor1Href = string;
		}

		public void setAnchor1Ref(ObjRef ref) {
			anchor1Ref = ref;
		}

		public void setAnchor1Type(String string) {
			anchor1Type = string;
		}

		public void setAnchor2Href(String string) {
			anchor2Href = string;
		}

		public void setAnchor2Ref(ObjRef ref) {
			anchor2Ref = ref;
		}

		public void setAnchor2Type(String string) {
			anchor2Type = string;
		}

		public void setLinkRef(ObjRef ref) {
			linkRef = ref;
		}

		public void setPoint1Ref(ObjRef ref) {
			point1Ref = ref;
		}

		public void setPoint1Target(ObjRef ref) {
			point1Target = ref;
		}

		public void setPoint2Ref(ObjRef ref) {
			point2Ref = ref;
		}

		public void setPoint2Target(ObjRef ref) {
			point2Target = ref;
		}
	}
	
}
