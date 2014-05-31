package edu.uci.ics.xarchutils;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import edu.uci.isr.xarch.*;

public class XArchFlatImpl implements XArchFlatInterface, XArchFileListener,
XArchListener, XArchEventProvider{

	//Map URLs to xArches
	private Hashtable xArches = new Hashtable();
	
	private Object tableLock = new Object();
	
	//Map ObjRef -> Objects
	private HashMap objects = new HashMap(5000, 0.60f);
	//Map Objects -> ObjRefs (for reverse-lookup purposes)...
	private HashMap reverseObjects = new HashMap(5000, 0.60f);
	
	private Vector xArchFileListeners = new Vector();
	private Vector xArchFlatListeners = new Vector();
	
	private IdTable idTable;
	private XMLLinkResolver resolver;
	
	//Cache contexts so we don't create a zillion of them.
	private ContextCache contextCache;
	
	//This is a dirty hack that I put here because DOM 2 sucks
	//and it should go away when we standardize on DOM 3/Java 1.5
	//and upgrade the data binding library.
	//
	//When we do an add() or a set(), the data binding library
	//will change the underlying DOM node of the WrapperImpl.
	//This means that the hashCode() changes, so the reverseObjects
	//table stops working right, because we hashed the WrapperImpl
	//with the original hashCode, which has now changed.  So, when
	//we go to look up the WrapperImpl's ObjRef, we'll fail unless 
	//we rehash the element after the add() or set()
	//with the new hashCode.  This seems all well and good--we should
	//just be able to remove it from the reverseObjects table, do the
	//add() or set(), and then re-add it to the table.  HOWEVER, the
	//add() or set() will have the side effect of throwing an XArchEvent,
	//and the handler for that uses the reverseObjects table.  So, what
	//we have to do is store the wrapper-to-objref mapping temporarily
	//in a special cache (the gutschange cache) so while the wrapperimpl
	//is temporarily missing from the reverseObjects table we can still
	//look up its ObjRef.
	private Vector gutsChangeCache = new Vector();
	private static class GutsChangeMapping{
		public Object o;
		public ObjRef ref;
	}
	
	public XArchFlatImpl(){
		contextCache = new ContextCache();
		idTable = new IdTable();
		idTable.addXArchFileListener(this);
		resolver = new XMLLinkResolver(idTable);
	}
	
	private void putGutsChangeMap(Object o, ObjRef ref){
		synchronized(gutsChangeCache){
			GutsChangeMapping gcm = new GutsChangeMapping();
			gcm.o = o;
			gcm.ref = ref;
			gutsChangeCache.add(0, gcm);
		}
	}
	
	private ObjRef getGutsChangeMap(Object o){
		synchronized(gutsChangeCache){
			for(int i = 0; i < gutsChangeCache.size(); i++){
				GutsChangeMapping gcm = (GutsChangeMapping)gutsChangeCache.elementAt(i);
				if(gcm.o.equals(o)){
					return gcm.ref;
				}
			}
			return null;
		}
	}
	
	protected static String uncapFirstLetter(String s){
		if(s == null){
			return null;
		}
		else if(s.length() == 0){
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toLowerCase(s.charAt(0)));
		sb.append(s.substring(1));
		return sb.toString();
	}
	
	protected static String capFirstLetter(String s){
		if(s == null){
			return null;
		}
		else if(s.length() == 0){
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toUpperCase(s.charAt(0)));
		sb.append(s.substring(1));
		return sb.toString();
	}
	
	private static Collection arrayToCollection(Object[] arr){
		Vector v = new Vector(arr.length);
		for(int i = 0; i < arr.length; i++){
			v.addElement(arr[i]);
		}
		return v;
	}

	private ObjRef put(Object o){
		return put(o, null);
	}
	
	private ObjRef put(Object o, String objRefUid){
		synchronized(tableLock){
			ObjRef alreadyInThere = (ObjRef)reverseObjects.get(o);
			if(alreadyInThere != null){
				//System.out.println("That was already in there! : " + o);
				return alreadyInThere;
			}
			alreadyInThere = getGutsChangeMap(o);
			if(alreadyInThere != null){
				//System.out.println("That was already in the gutschange cache! : " + o);
				return alreadyInThere;
			}
			
			ObjRef objRef;
			if(objRefUid == null){
				do{
					objRef = new ObjRef();
				}while(objects.get(objRef) != null);
			}
			else{
				objRef = new ObjRef(objRefUid);
			}
			
			//System.out.println("Dude, that wasn't in there: " + o);
			//System.out.println("I'm going to call that: " + objRef);
			
			objects.put(objRef, o);
			reverseObjects.put(o, objRef.duplicate());
			return objRef;
		}
	}
		
	private Object get(ObjRef ref){
		synchronized(tableLock){
			Object o = objects.get(ref);
			if(o == null){
				//System.out.println("Table is: " + objects);
				throw new NoSuchObjectException(ref);
			}
			return o;
		}
	}
	
	public boolean isValidObjRef(ObjRef ref){
		synchronized(tableLock){
			Object o = objects.get(ref);
			if(o != null){
				return true;
			}
			else{
				return false;
			}
		}
	}
	
	static MethodCache methodCache = new MethodCache();
	
	private static Method getMethod(Class c, String name){
		Method m = methodCache.getMethod(c, name);
		if(m == null){
			throw new InvalidOperationException(c, name);
		}
		return m;
		/*
		Method[] methods = c.getMethods();
		for(int i = 0; i < methods.length; i++){
			if(methods[i].getName().equals(name)){
				return methods[i];
			}
		}
		throw new InvalidOperationException(c, name);
		*/
	}
	
	public ObjRef createXArch(String url){
		ObjRef cachedRef = (ObjRef)xArches.get(url);
		if(cachedRef != null){
			throw new IllegalArgumentException("An open xArch with that URL has already been created.");
		}
		//System.out.println("Creating new xArch.");
		IXArch xArch = idTable.createXArch(url);
		//This will, via a callback, make the xArch import in here, so now it'll magically
		//be in the ID table and we just have to get it.
		ObjRef ref = (ObjRef)xArches.get(url);
		if(ref == null){
			throw new RuntimeException("Bad mojo!");
		}		
		return ref;
	}

	public ObjRef cloneXArch(ObjRef xArchRef, String newURI){
		ObjRef cachedRef = (ObjRef)xArches.get(newURI);
		if(cachedRef != null){
			throw new IllegalArgumentException("An open xArch with that URL has already been created.");
		}
		Object to = get(xArchRef);
		if(!(to instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Target of clone must be of type IXArch");
		}
		IXArch xArchToClone = (IXArch)to;
		
		//System.out.println("Creating new xArch.");
		IXArch clonedXArch = idTable.cloneXArch(xArchToClone, newURI);
		//This will, via a callback, make the xArch import in here, so now it'll magically
		//be in the ID table and we just have to get it.
		ObjRef ref = (ObjRef)xArches.get(newURI);
		if(ref == null){
			throw new RuntimeException("Bad mojo!");
		}
		return ref;
	}

	public ObjRef cloneXArchElementDepthOne(ObjRef targetRef, String prefix){
		Object to = get(targetRef);
		if(!(to instanceof IXArchElement)){
			throw new IllegalArgumentException("Target of cloneXArchElementDepthOne must be of type IXArchElement");
		}
		if(!(to instanceof DOMBased)){
			throw new IllegalArgumentException("Target of cloneXArchElementDepthOne must be of DOM-based");
		}
		
		IXArchElement targetElt = (IXArchElement)to;
		Element domElt = (Element)((DOMBased)targetElt).getDOMNode();
		NodeList nl = domElt.getChildNodes();

		IXArchElement targetEltClone = targetElt.cloneElement(1);
		ObjRef cloneRef = put(targetEltClone, prefix + targetRef.getUID());
		
		//System.out.println("Dooped objRef = " + cloneRef);
		//Okay, we have cloned the target element and its children, but we need to clone
		//the associated ObjRefs of its children
		Element domEltOfClone = (Element)((DOMBased)targetElt).getDOMNode();
		NodeList clonenl = domEltOfClone.getChildNodes();

		int size = nl.getLength();
		IXArch xArch = targetElt.getXArch();
		for(int i = 0; i < size; i++){
			Node n = nl.item(i);
			Node clonen = clonenl.item(i);
			if(n instanceof Element){
				//Make a wrapper for the child so we can look it up
				Object wrapper = IdTable.makeWrapper(xArch, (Element)n);
				//Find the child of the original node
				ObjRef originalRef = (ObjRef)reverseObjects.get(wrapper);
				//If the child of the original node wasn't already ObjReffed, ref it now.
				if(originalRef == null){
					originalRef = put(wrapper);
				}
				//Now that we have the original object reffed for sure,
				//ref its clone.
				Object cloneWrapper = IdTable.makeWrapper(xArch, (Element)clonen);
				put(cloneWrapper, prefix + originalRef.getUID());
			}
		}
		
		return cloneRef;
	}
	
	private void importXArch(String url){
		if(xArches.get(url) != null){
			return;
		}
			
		IXArch xArch = idTable.getXArch(url);
		if(xArch == null){
			throw new IllegalArgumentException("Attempt to import invalid URL.");
		}
		
		ObjRef objRef = put(xArch);
		xArch.addXArchListener(this);
		xArches.put(url, objRef);
	}
	
	public void renameXArch(String oldURI, String newURI){
		synchronized(xArches){
			if(oldURI == null){
				throw new IllegalArgumentException("URI to rename was null.");
			}
			ObjRef xArchRef = (ObjRef)xArches.get(oldURI);
			if(xArchRef == null){
				throw new IllegalArgumentException("Invalid URI to close.");
			}
			xArches.remove(oldURI);
			xArches.put(newURI, xArchRef);
			XArchFileEvent evt = new XArchFileEvent(XArchFileEvent.XARCH_RENAMED_EVENT, oldURI, newURI, xArchRef);
			fireXArchFileEvent(evt);
		}
	}
	
	public ObjRef parseFromFile(String fileName) throws FileNotFoundException, IOException, SAXException{
		File f = new File(fileName);
		return parseFromURL(f.toURL().toString());
	}
	
	public ObjRef parseFromURL(String urlString) throws MalformedURLException, IOException, SAXException{
		ObjRef cachedRef = (ObjRef)xArches.get(urlString);
		if(cachedRef != null){
			return cachedRef;
		}
		else{
			IXArch xArch = idTable.parseFromURL(urlString);

			//The following now happens by import magic
			//ObjRef ref = put(xArch);
			//xArches.put(urlString, ref);
			//xArch.addXArchListener(this);
			
			ObjRef ref = (ObjRef)xArches.get(urlString);
			if(ref == null){
				throw new RuntimeException("Bad mojo!");
			}		
			return ref;
		}
		
		/*
		URL url = new URL(urlString);
		InputStreamReader isr = new InputStreamReader(url.openStream());
		IXArch xArch = XArchUtils.parse(isr);
		ObjRef ref = new ObjRef();
		objects.put(ref, xArch);
		xArches.put(urlString, ref);
		xArch.addXArchListener(this);
		return ref;
		*/
	}

	public String[] getOpenXArchURLs(){
		return getOpenXArchURIs();
	}

	public String[] getOpenXArchURIs(){
		synchronized(xArches){
			String[] arr = new String[xArches.size()];
			int i = 0;
			for(Enumeration en = xArches.keys(); en.hasMoreElements(); ){
				arr[i++] = (String)en.nextElement();
			}
			return arr;
		}
	}

	public ObjRef[] getOpenXArches(){
		synchronized(xArches){
			ObjRef[] arr = new ObjRef[xArches.size()];
			int i = 0;
			for(Iterator it = xArches.values().iterator(); it.hasNext(); ){
				arr[i++] = (ObjRef)it.next();
			}
			return arr;
		}
	}

	public boolean equals(ObjRef ref1, ObjRef ref2){
		Object to1 = get(ref1);
		Object to2 = get(ref2);
		return to1.equals(to2);
	}

	public String getXArchURL(ObjRef xArchRef){
		return getXArchURI(xArchRef);
	}

	public String getXArchURI(ObjRef xArchRef){
		if(xArchRef == null){
			throw new IllegalArgumentException("Null ObjRef passed to getXArchURL");
		}
		Object to = get(xArchRef);
		if(!(to instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Target of serialization must be of type IXArch");
		}
		for(Enumeration en = xArches.keys(); en.hasMoreElements(); ){
			String url = (String)en.nextElement();
			ObjRef ref = (ObjRef)xArches.get(url);
			if(equals(xArchRef, ref)){
				return url;
			}
		}
		return null;
	}
	
	public ObjRef getOpenXArch(String url){
		if(url == null){
			throw new IllegalArgumentException("Null URL passed to getOpenXArch");
		}
		return (ObjRef)xArches.get(url);
	}

	public void close(String url){
		if(url == null){
			throw new IllegalArgumentException("URL to close was null.");
		}
		ObjRef xArchRef = (ObjRef)xArches.get(url);
		if(xArchRef == null){
			throw new IllegalArgumentException("Invalid URL to close.");
		}
		close(xArchRef);
	}
	
	public void close(ObjRef xArchRef){
		Object to = get(xArchRef);
		if(!(to instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Target of close must be of type IXArch");
		}		
		
		String uri = getXArchURI(xArchRef);
		contextCache.removeAll(xArchRef);
		idTable.forgetXArch((IXArch)to);
		forgetXArch(xArchRef);
		XArchFileEvent closeEvent = new XArchFileEvent(XArchFileEvent.XARCH_CLOSED_EVENT,
			uri, xArchRef);
		fireXArchFileEvent(closeEvent);
	}
	
	private void forgetXArch(ObjRef xArchRef){
		//ObjRef xArchRef = (ObjRef)xArches.get(url);
		if(xArchRef == null){
			throw new IllegalArgumentException("Invalid document to close.");
		}
		
		Object to = get(xArchRef);
		if(!(to instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Target of close must be of type IXArch");
		}
		
		Vector toRemove = new Vector();
		
		synchronized(tableLock){
			for(Iterator en = reverseObjects.keySet().iterator(); en.hasNext(); ){
				Object o = en.next();
				if(o instanceof IXArchElement){
					IXArchElement elt = (IXArchElement)o;
					if(elt.getXArch() != null){
						if(elt.getXArch().equals(to)){
							toRemove.addElement(elt);
						}
					}
					else{
						System.out.println("Warning: element xArch was null: " + elt);
					}
				}
			}
			int size = toRemove.size();
			for(int i = 0; i < size; i++){
				IXArchElement elt = (IXArchElement)toRemove.elementAt(i);
				ObjRef delRef = (ObjRef)reverseObjects.get(elt);
				reverseObjects.remove(elt);
				objects.remove(delRef);
			}
			
			synchronized(xArches){
				String[] keys = (String[])xArches.keySet().toArray(new String[0]);
				for(int i = 0; i < keys.length; i++){
					ObjRef checkXArchRef = (ObjRef)xArches.get(keys[i]);
					if(checkXArchRef.equals(xArchRef)){
						xArches.remove(keys[i]);
					}
				}
			}

			//xArches.remove(url);
		}
	}

	public String serialize(ObjRef xArchRef){
		Object to = get(xArchRef);
		if(!(to instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Target of serialization must be of type IXArch");
		}
		try{
			return idTable.getXArchImplementation().serialize((edu.uci.isr.xarch.IXArch)to, null);
		}
		catch(XArchSerializeException e){
			//This shouldn't happen.
			throw new RuntimeException(e);
		}
	}
	
	
	public void writeToFile(ObjRef xArchRef, String fileName) throws java.io.IOException{
		String serializedForm = serialize(xArchRef);
		FileWriter fw = new FileWriter(fileName);
		fw.write(serializedForm);
		fw.close();
		fireXArchFileEvent(new XArchFileEvent(XArchFileEvent.XARCH_SAVED_EVENT,
			getXArchURL(xArchRef), new File(fileName).toURL().toString(), xArchRef));
	}

	public boolean isAttached(ObjRef childRef){
		ObjRef xArchRef = getXArch(childRef);
		return hasAncestor(childRef, xArchRef);
	}
	
	public String getElementName(ObjRef ref){
		Object elt = get(ref);
		if(!(elt instanceof IXArchElement)){
			throw new IllegalArgumentException("Targets of getElementName(...) must be IXArchElements");
		}
		if(!(elt instanceof DOMBased)){
			throw new IllegalArgumentException("Targets of getElementName(...) must be DOM-based");
		}
		Node node = ((DOMBased)elt).getDOMNode();
		return node.getLocalName();
	}
	
	public boolean hasAncestor(ObjRef childRef, ObjRef ancestorRef){
		Object child = get(childRef);
		Object ancestor = get(ancestorRef);
		if(!(child instanceof IXArchElement)){
			throw new IllegalArgumentException("Targets of hasAncestor(...) must be IXArchElements");
		}
		if(!(ancestor instanceof IXArchElement)){
			throw new IllegalArgumentException("Targets of hasAncestor(...) must be IXArchElements");
		}

		if(!(child instanceof DOMBased)){
			throw new IllegalArgumentException("Targets of hasAncestor(...) must be DOM-based");
		}
		if(!(ancestor instanceof DOMBased)){
			throw new IllegalArgumentException("Targets of hasAncestor(...) must be DOM-based");
		}
		
		Node ancestorNode = ((DOMBased)ancestor).getDOMNode();
		Node childNode = ((DOMBased)child).getDOMNode();
		
		Node n = childNode;
		while(true){
			if(n == null){
				return false;
			}
			if(n.equals(ancestorNode)){
				return true;
			}
			if(childNode.getNodeType() == Node.ATTRIBUTE_NODE){
				n = ((Attr)childNode).getOwnerElement();
			}
			else{
				n = n.getParentNode();
			}
		}
	}

	public ObjRef getParent(ObjRef to){
		//System.out.println("Getting parent of: " + to);
		Object child = get(to);
		//System.out.println("to is a " + child.getClass());
		if(!(child instanceof IXArchElement)){
			throw new IllegalArgumentException("Target of getParent(...) must be an IXArchElement");
		}
		IXArchElement parent = idTable.getParent((IXArchElement)child);
		if(parent == null){
			return null;
		}
		return put(parent);
	}

	public ObjRef[] getAllAncestors(ObjRef to){
		Vector v = new Vector();
		v.addElement(to);
		while(true){
			ObjRef parent = getParent(to);
			if(parent != null){
				v.addElement(parent);
				to = parent;
			}
			else{
				ObjRef[] arr = new ObjRef[v.size()];
				v.copyInto(arr);
				return arr;
			}
		}
	}

	public ObjRef getByID(String id){
		IXArchElement elt = idTable.getEntity(id);
		//System.out.println(idTable.idTableView());
		if(elt == null){
			return null;
		}
		else{
			
			return put(elt);
		}
	}

	public ObjRef getByID(ObjRef xArchRef, String id){
		Object co = get(xArchRef);
		if(!(co instanceof IXArch)){
			throw new IllegalArgumentException("Context of getByID be of type IXArch");
		}
		IXArch xArch = (IXArch)co;
		
		IXArchElement elt = idTable.getEntity(xArch, id);
		if(elt == null){
			return null;
		}
		else{
			return put(elt);
		}
	}

	//Simulates contextObject.add[typeOfThing](thingToAdd)
	public void add(ObjRef contextObject, String typeOfThing, ObjRef thingToAdd){
		synchronized(gutsChangeCache){
			Object co = get(contextObject);
			typeOfThing = capFirstLetter(typeOfThing);
			String methodName = "add" + typeOfThing;
			Object to = get(thingToAdd);
		
			Method m = getMethod(co.getClass(), methodName);
		
			try{
				putGutsChangeMap(to, thingToAdd);
				reverseObjects.remove(to);
				m.invoke(co, new Object[]{to});
			}
			catch(IllegalArgumentException iae){
				throw new InvalidOperationException(co.getClass(), methodName, new Class[]{to.getClass()});
			}
			catch(IllegalAccessException iae){
				throw new RuntimeException(iae.toString());
			}
			catch(InvocationTargetException ite){
				throw new RuntimeException(ite.getTargetException());
			}
			finally{
				reverseObjects.put(to, thingToAdd);
				gutsChangeCache.clear();
			}
			return;
		}
	}
	
	public void add(ObjRef contextObject, String typeOfThing, ObjRef[] thingsToAdd){
		synchronized(gutsChangeCache){
			Object co = get(contextObject);
			typeOfThing = capFirstLetter(typeOfThing);
			String methodName = "add" + typeOfThing + "s";
		
			Object[] objArr = new Object[thingsToAdd.length];
			for(int i = 0; i < thingsToAdd.length; i++){
				objArr[i] = get(thingsToAdd[i]);
				putGutsChangeMap(objArr[i], thingsToAdd[i]);
				reverseObjects.remove(objArr[i]);
			}
		
			Method m = getMethod(co.getClass(), methodName);
		
			try{
				m.invoke(co, new Object[]{arrayToCollection(objArr)});
			}
			catch(IllegalArgumentException iae){
				throw new InvalidOperationException(co.getClass(), methodName, new Class[]{Collection.class});
			}
			catch(IllegalAccessException iae){
				throw new RuntimeException(iae.toString());
			}
			catch(InvocationTargetException ite){
				throw new RuntimeException(ite.getTargetException());
			}
			finally{
				for(int i = 0; i < thingsToAdd.length; i++){
					reverseObjects.put(objArr[i], thingsToAdd[i]);
				}
				gutsChangeCache.clear();
			}
			return;
		}
	}
	
	public void clear(ObjRef contextObject, String typeOfThing){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName1 = "clear" + typeOfThing;
		String methodName2 = "clear" + typeOfThing + "s";
		
		Method m = null;
		try{
			m = getMethod(co.getClass(), methodName1);
		}
		catch(InvalidOperationException ioe){
			//Hmm.  clearFoobar() isn't a method.  What about clearFoobars?
			m = getMethod(co.getClass(), methodName2);
		}
		
		try{
			m.invoke(co, new Object[]{});
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName1);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
		return;
	}
	
	//Returns a string for simple values and an ObjRef for complex ones, or null
	//if there wasn't anything there at all.
	public synchronized Object get(ObjRef contextObject, String typeOfThing){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "get" + typeOfThing;

		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			if(o == null){
				return null;
			}
			else if(o instanceof String){
				return o;
			}
			else{
				return put(o);
			}
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}

	public ObjRef get(ObjRef contextObject, String typeOfThing, String id){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "get" + typeOfThing;
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{id});
			if(o == null){
				return null;
			}
			return put(o);
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
		
	public ObjRef[] get(ObjRef contextObject, String typeOfThing, String[] ids){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "get" + typeOfThing + "s";
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{ids});
			Object[] arr = (Object[])o;
			
			ObjRef[] refArr = new ObjRef[arr.length];
			for(int i = 0; i < arr.length; i++){
				refArr[i] = put(arr[i]);
			}
			return refArr;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef[] getAll(ObjRef contextObject, String typeOfThing){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "getAll" + typeOfThing + "s";
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			Collection c = (Collection)o;
			
			ObjRef[] refArr = new ObjRef[c.size()];
			int i = 0;
			for(Iterator it = c.iterator(); it.hasNext(); ){
				refArr[i++] = put(it.next());
			}
			return refArr;
			/*
			Object[] arr = c.toArray();
			
			ObjRef[] refArr = new ObjRef[arr.length];
			for(int i = 0; i < arr.length; i++){
				refArr[i] = put(arr[i]);
			}
			return refArr;
			*/
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public boolean has(ObjRef contextObject, String typeOfThing, String valueToCheck){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "has" + typeOfThing;
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Boolean b = (Boolean)m.invoke(co, new Object[]{valueToCheck});
			return b.booleanValue();
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{String.class});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public boolean has(ObjRef contextObject, String typeOfThing, ObjRef thingToCheck){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "has" + typeOfThing;
		
		Object to = get(thingToCheck);
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Boolean b = (Boolean)m.invoke(co, new Object[]{to});
			return b.booleanValue();
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{to.getClass()});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}

	public boolean hasAll(ObjRef contextObject, String typeOfThing, ObjRef[] thingsToCheck){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "hasAll" + typeOfThing + "s";
		
		Vector v = new Vector(thingsToCheck.length);
		for(int i = 0; i < thingsToCheck.length; i++){
			v.addElement(get(thingsToCheck[i]));
		}
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Boolean b = (Boolean)m.invoke(co, new Object[]{v});
			return b.booleanValue();
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public boolean[] has(ObjRef contextObject, String typeOfThing, ObjRef[] thingsToCheck){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "has" + typeOfThing + "s";
		
		Vector v = new Vector(thingsToCheck.length);
		for(int i = 0; i < thingsToCheck.length; i++){
			v.addElement(get(thingsToCheck[i]));
		}
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Collection c = (Collection)m.invoke(co, new Object[]{v});
			Vector responseVector = new Vector();
			for(Iterator it = c.iterator(); it.hasNext(); ){
				Boolean b = (Boolean)it.next();
				responseVector.addElement(b);
			}
			boolean[] responseArray = new boolean[responseVector.size()];
			for(int i = 0; i < responseArray.length; i++){
				responseArray[i] = ((Boolean)responseVector.elementAt(i)).booleanValue();
			}
			return responseArray;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{Collection.class});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public void remove(ObjRef contextObject, String typeOfThing, ObjRef thingToRemove){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "remove" + typeOfThing;
		Object to = get(thingToRemove);
		
		Method m = getMethod(co.getClass(), methodName);
		//System.out.println("m = " + m);
		//System.out.println("to.class = " + to.getClass());
		try{
			m.invoke(co, new Object[]{to});
		}
		catch(IllegalArgumentException iae){
			//iae.printStackTrace();
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{to.getClass()});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
		return;
	}
	
	public void remove(ObjRef contextObject, String typeOfThing, ObjRef[] thingsToRemove){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "remove" + typeOfThing + "s";
		
		Object[] objArr = new Object[thingsToRemove.length];
		for(int i = 0; i < thingsToRemove.length; i++){
			objArr[i] = get(thingsToRemove[i]);
		}
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			m.invoke(co, new Object[]{arrayToCollection(objArr)});
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{Collection.class});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
		return;
	}
	
	public void set(ObjRef contextObject, String typeOfThing, String value){
		Object co = get(contextObject);
		typeOfThing = capFirstLetter(typeOfThing);
		String methodName = "set" + typeOfThing;
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			m.invoke(co, new Object[]{value});
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{String.class});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public void set(ObjRef contextObject, String typeOfThing, ObjRef value){
		synchronized(gutsChangeCache){
			Object co = get(contextObject);
			typeOfThing = capFirstLetter(typeOfThing);
			String methodName = "set" + typeOfThing;
			
			Object to = get(value);
			
			Method m = getMethod(co.getClass(), methodName);
			
			try{
				putGutsChangeMap(to, value);
				reverseObjects.remove(to);
				m.invoke(co, new Object[]{to});
			}
			catch(IllegalArgumentException iae){
				throw new InvalidOperationException(co.getClass(), methodName, new Class[]{to.getClass()});
			}
			catch(IllegalAccessException iae){
				throw new RuntimeException(iae.toString());
			}
			catch(InvocationTargetException ite){
				throw new RuntimeException(ite.getTargetException());
			}
			finally{
				reverseObjects.put(to, value);
				gutsChangeCache.clear();
			}
		}
	}

	private static Method findIsEqualMethod(Class coClass, Class c){
		if(c == null) return null;
		try{
			Method m = coClass.getMethod("isEqual", new Class[]{c});
			if(m != null) return m;
		}
		catch(NoSuchMethodException nsme1){}
		Class[] interfaceClasses = c.getInterfaces();
		if(interfaceClasses != null){
			for(int i = 0; i < interfaceClasses.length; i++){
				try{
					Method m = coClass.getMethod("isEqual", new Class[]{interfaceClasses[i]});
					if(m != null) return m;
				}
				catch(NoSuchMethodException nsme2){}
			}
		}
		return findIsEqualMethod(coClass, c.getSuperclass());
	}

	public boolean isEqual(ObjRef contextObject, ObjRef thingToCheck){
		Object co = get(contextObject);
		String methodName = "isEqual";
		
		Object to = get(thingToCheck);
		
		//Method m = getMethod(co.getClass(), methodName);
		/*
		Method m = null;
		try{
			m = co.getClass().getMethod(methodName, new Class[]{to.getClass()});
		}
		catch(NoSuchMethodException nsme){
			throw new InvalidOperationException(methodName + "(" + to.getClass() + ")");		
		}
		*/
		Method m = findIsEqualMethod(co.getClass(), to.getClass());
		if(m == null){
			throw new InvalidOperationException(methodName + "(" + to.getClass() + ")");
		}

		try{
			Boolean b = (Boolean)m.invoke(co, new Object[]{to});
			return b.booleanValue();
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName, new Class[]{to.getClass()});
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
		
	public boolean isEquivalent(ObjRef contextObject, ObjRef thingToCheck){
		Object co = get(contextObject);
		Class coClass = co.getClass();
		String methodName = "isEquivalent";
		
		Object to = get(thingToCheck);
		Class toClass = to.getClass();
		
		//This is specially included here to fix a bug that could report
		//no-such-method when the classes are not the same due to the
		//cache-by-name behavior of the method cache, even though
		//an underlying method does actually exist...which would return
		//false in this case.
		
		if(!coClass.equals(toClass)){
			return false;
		}
		
		Method m = getMethod(coClass, methodName);
		
		try{
			Boolean b = (Boolean)m.invoke(co, new Object[]{to});
			return b.booleanValue();
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef createContext(ObjRef xArchObject, String contextType){
		Object co = get(xArchObject);
		if(!(co instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("xArchObject must be of type IXArch");
		}
		
		ObjRef existingContext = contextCache.get(xArchObject, contextType);
		if(existingContext != null){
			return existingContext;
		}
		
		Object context = idTable.getXArchImplementation().createContext(
			(edu.uci.isr.xarch.IXArch)co, contextType);
		if(context == null){
			throw new RuntimeException("Error creating context.");
		}
		ObjRef contextRef = put(context);
		contextCache.put(xArchObject, contextType, contextRef);
		return contextRef;
	}
	
	public ObjRef create(ObjRef contextObject, String typeOfThing){
		Object co = get(contextObject);
		String methodName = "create" + capFirstLetter(typeOfThing);
		
		//System.out.println("in create; co=" + co);
		//System.out.println("in create; co.class=" + co.getClass());
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			return put(o);
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef createElement(ObjRef contextObject, String typeOfThing){
		Object co = get(contextObject);
		String methodName = "create" + capFirstLetter(typeOfThing) + "Element";
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			return put(o);
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef promoteTo(ObjRef contextObject, String promoteTo, ObjRef targetObject){
		Object co = get(contextObject);
		String methodName = "promoteTo" + capFirstLetter(promoteTo);
	
		Object to = get(targetObject);
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			//System.out.println("Promotion beginning.  Class = " + to.getClass());
			//System.out.println("Ref = " + targetObject);
			
			Object o = m.invoke(co, new Object[]{to});
			//This will always be there, because we just promoted
			//an existing element.  Two elements are equal() when
			//their underlying DOM elements are equal, and the
			//underlying DOM element hasn't changed here--just
			//the wrapper.
			
			// this is now done in the handleXArchEvent method
			//reverseObjects.remove(to);
			//objects.remove(targetObject);
			
			// but let's still check it
			ObjRef newRef = put(o, targetObject.getUID());
			if(!newRef.equals(targetObject)){
				throw new RuntimeException("Bad Mojo!");
			}
			
			//System.out.println("New class = " + o.getClass());
			//System.out.println("New ref = " + newRef);
			//System.out.println("New class, according to flat = " + getType(newRef));
			return newRef;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef recontextualize(ObjRef contextObject, String typeOfThing, ObjRef targetObject){
		Object co = get(contextObject);
		String methodName = "recontextualize" + capFirstLetter(typeOfThing);
	
		Object to = get(targetObject);
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{to});
			objects.remove(targetObject);
			reverseObjects.remove(to);
			
			return put(o, targetObject.getUID());
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef getXArch(ObjRef contextObject){
		Object co = get(contextObject);
		String methodName = "getXArch";
	
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			return put(o);
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef[] getAllElements(ObjRef contextObject, String typeOfThing, ObjRef xArchObject){
		Object co = get(contextObject);
		String methodName = "getAll" + capFirstLetter(typeOfThing) + "s";
		Object to = get(xArchObject);
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{to});
			Collection c = (Collection)o;
			
			Object[] arr = c.toArray();
			
			ObjRef[] refArr = new ObjRef[arr.length];
			for(int i = 0; i < arr.length; i++){
				refArr[i] = put(arr[i]);
			}
			return refArr;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public ObjRef getElement(ObjRef contextObject, String typeOfThing, ObjRef xArchObject){
		Object co = get(contextObject);
		String methodName = "get" + capFirstLetter(typeOfThing);
		Object to = get(xArchObject);
		
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{to});
			if(o == null){
				return null;
			}
			return put(o);
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public XArchTypeMetadata getTypeMetadata(ObjRef contextObject){
		Object co = get(contextObject);
		String methodName = "getTypeMetadata";
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			if(o instanceof XArchTypeMetadata)
				return (XArchTypeMetadata)o;
			return null;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public XArchInstanceMetadata getInstanceMetadata(ObjRef contextObject){
		Object co = get(contextObject);
		String methodName = "getInstanceMetadata";
		Method m = getMethod(co.getClass(), methodName);
		
		try{
			Object o = m.invoke(co, new Object[]{});
			if(o instanceof XArchInstanceMetadata)
				return (XArchInstanceMetadata)o;
			return null;
		}
		catch(IllegalArgumentException iae){
			throw new InvalidOperationException(co.getClass(), methodName);
		}
		catch(IllegalAccessException iae){
			throw new RuntimeException(iae.toString());
		}
		catch(InvocationTargetException ite){
			throw new RuntimeException(ite.getTargetException());
		}
	}
	
	public String getType(ObjRef contextObject){
		Object co = get(contextObject);
		return co.getClass().getName();
	}
	
	public boolean isInstanceOf(ObjRef contextObject, String className){
		Object co = get(contextObject);
		try{
			Class c = Class.forName(className);
			return (c.isAssignableFrom(co.getClass()));
		}
		catch(ClassNotFoundException e){
			throw new IllegalArgumentException("Invalid class name: " + className);
		}
	}

	public ObjRef resolveHref(ObjRef xArchRef, String href){
		Object co = get(xArchRef);
		if(!(co instanceof edu.uci.isr.xarch.IXArch)){
			throw new IllegalArgumentException("Context of resolveHref must be of type IXArch");
		}
		try{
			IXArchElement elt = resolver.resolveHref((IXArch)co, href);
			if(elt == null){
				return null;
			}
			else{
				return put(elt);
			}
		}
		catch(IllegalArgumentException iae){
			throw iae;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException(e.toString());
		}
	}

	public void addXArchFlatListener(XArchFlatListener l){
		xArchFlatListeners.addElement(l);
	}
	
	public void removeXArchFlatListener(XArchFlatListener l){
		xArchFlatListeners.removeElement(l);
	}
	
	public void addXArchFileListener(XArchFileListener l){
		xArchFileListeners.addElement(l);
	}
	
	public void removeXArchFileListener(XArchFileListener l){
		xArchFileListeners.removeElement(l);
	}

	public void handleXArchEvent(XArchEvent evt){
		
		//Transform the XArchEvent into an XArchFlatEvent
		IXArchElement src = evt.getSource();
		//ObjRef srcRef = (ObjRef)reverseObjects.get(src);
		//if(srcRef == null){
		ObjRef srcRef = put(src);
		//}
		XArchPath srcPath = getXArchPath(srcRef);
		//System.out.println(srcPath);

		XArchPath targetPath = null;
		Object target = evt.getTarget();
		
		// update the tables for promote
		int eventType = evt.getEventType();
		if(eventType == XArchEvent.PROMOTE_EVENT){
			target = put(target, ((ObjRef)reverseObjects.remove(src)).getUID());
		}
		else{
			if(target != null){
				if(!(target instanceof String)){
					//ObjRef targetRef = (ObjRef)reverseObjects.get(target);
					//if(targetRef == null){
					ObjRef targetRef = put(target);
					//}
					target = targetRef;
					targetPath = getXArchPath(targetRef);
				}
			}
		}
		
		XArchFlatEvent flatEvt = new XArchFlatEvent(srcRef, srcPath, eventType, evt.getSourceType(),
			evt.getTargetName(), target, targetPath, evt.getIsAttached());
		
		for(Enumeration en = xArchFlatListeners.elements(); en.hasMoreElements(); ){
			XArchFlatListener l = (XArchFlatListener)en.nextElement();
			l.handleXArchFlatEvent(flatEvt);
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
		//System.out.println("Got file event.");
		int evtType = evt.getEventType();
		if((evtType == XArchFileEvent.XARCH_CREATED_EVENT) ||
			(evtType == XArchFileEvent.XARCH_OPENED_EVENT)){
			//System.out.println("Importing!");
			importXArch(evt.getURL());
			evt = new XArchFileEvent(evt, getOpenXArch(evt.getURL()));
			fireXArchFileEvent(evt);
		}
		else if(evtType == XArchFileEvent.XARCH_CLOSED_EVENT){
			//System.out.println("Forgetting!");
			//forgetXArch(evt.getXArchRef());
			//The close event will be fired by close() from here...
		}	
		else{
			evt = new XArchFileEvent(evt, getOpenXArch(evt.getURL()));
			fireXArchFileEvent(evt);
		}
	}

	protected void fireXArchFileEvent(XArchFileEvent evt){
		for(Enumeration en = xArchFileListeners.elements(); en.hasMoreElements(); ){
			XArchFileListener l = (XArchFileListener)en.nextElement();
			l.handleXArchFileEvent(evt);
		}
	}

	public void forgetAllWithPrefix(ObjRef xArchRef, String prefix){
		synchronized(tableLock){
			IXArch xArch = (IXArch)get(xArchRef);
			ArrayList markedForDeath = new ArrayList();
			for(Iterator it = objects.keySet().iterator(); it.hasNext(); ){
				ObjRef ref = (ObjRef)it.next();
				if(ref.getUID().startsWith(prefix)){
					Object o = objects.get(ref);
					if(o instanceof IXArchElement){
						IXArchElement elt = (IXArchElement)o;
						if(elt.getXArch().equals(xArch)){
							markedForDeath.add(ref);
						}
					}
				}
			}
			for(Iterator it = markedForDeath.iterator(); it.hasNext(); ){
				ObjRef ref = (ObjRef)it.next();
				Object o = objects.get(ref);
				objects.remove(ref);
				reverseObjects.remove(o);
			}
		}
	}
	
	public ObjRef cloneElement(ObjRef targetObjectRef, int depth){
		Object to = get(targetObjectRef);
		if(!(to instanceof IXArchElement)){
			throw new IllegalArgumentException("Target of cloneElement() must be an IXArchElement");
		}
		IXArchElement elt = (IXArchElement)to;
		IXArchElement clonedElt = idTable.cloneElement(elt, depth);
		ObjRef clonedRef = put(clonedElt);
		//System.out.println("Ref of clone is " + clonedRef);
		return clonedRef;
	}

	public ObjRef[] getReferences(ObjRef xArchRef, String id){
		Object co = get(xArchRef);
		if(!(co instanceof IXArch)){
			throw new IllegalArgumentException("Context of getByID be of type IXArch");
		}
		IXArch xArch = (IXArch)co;
		
		Collection elts = idTable.getReferences(xArch, id);
		ObjRef[] arr = new ObjRef[elts.size()];
		int i = 0;
		for(Iterator it = elts.iterator(); it.hasNext(); ){
			IXArchElement elt = (IXArchElement)it.next();
			arr[i] = put(elt);
			i++;
		}
		return arr;
	}

	public void dump(ObjRef ref){
		Object co = get(ref);
		System.out.println(co);
	}
	
	private static String stripPrefix(String s){
		return s.substring(s.indexOf(":") + 1);
	}
	
	public ObjRef resolveXArchPath(ObjRef xArchRef, XArchPath p){
		String pathTagsString = p.toTagsOnlyString();
		if(!pathTagsString.startsWith("xArch/")){
			return null;
		}
		
		ObjRef curNode = xArchRef;
		outerLoop: for(int i = 1; i < p.getLength(); i++){
			String tagName = p.getTagName(i);
			int tagIndex = p.getTagIndex(i);
			String tagID = p.getTagID(i);
			
			if(i == 1){
				//It's the top-level element
				int localIndex = 0;
				ObjRef[] objects = this.getAll(curNode, "Object");
				for(int j = 0; j < objects.length; j++){
					String objectTagName = capFirstLetter(getElementName(objects[j]));
					if(capFirstLetter(tagName).equals(objectTagName)){
						if(tagID != null){
							try{
								String objectID = (String)this.get(objects[j], "Id");
								if(objectID != null){
									if(objectID.equals(tagID)){
										curNode = objects[j];
										continue outerLoop;
									}
								}
							}
							catch(Exception e){}
						}
						else{
							//Go by index
							if((tagIndex == -1) || (tagIndex == 0)){
								//Looking for the first one
								curNode = objects[j];
								continue outerLoop;
							}
							else{
								if(tagIndex == localIndex){
									curNode = objects[j];
									continue outerLoop;
								}
								localIndex++;
							}
						}
					}
				}
				return null;
			}
			else{
				//It's not the very first one
				if(tagID != null){
					try{
						ObjRef ref = (ObjRef)this.get(curNode, tagName);
						if(ref != null){
							try{
								String objectID = (String)this.get(ref, "Id");
								if(objectID != null){
									if(objectID.equals(tagID)){
										curNode = ref;
										continue;
									}
								}
							}
							catch(Exception e){}
						}
					}
					catch(Exception e){}
					try{
						ObjRef[] refs = this.getAll(curNode, tagName);
						for(int j = 0; j < refs.length; j++){
							try{
								String objectID = (String)this.get(refs[j], "Id");
								if(objectID != null){
									if(objectID.equals(tagID)){
										curNode = refs[j];
										continue;
									}
								}
							}
							catch(Exception e){}
						}
					}
					catch(Exception e){}
					//We fell off the tree;
					return null;
				}
				else{
					//Find by index
					if(tagIndex < 1){
						try{
							ObjRef ref = (ObjRef)this.get(curNode, tagName);
							if(ref != null){
								curNode = ref;
								continue;
							}
						}
						catch(Exception e){}
						try{
							ObjRef[] refs = this.getAll(curNode, tagName);
							if((refs != null) && (refs.length > 0)){
								curNode = refs[0];
								continue;
							}
						}
						catch(Exception e){}
						//Fell off the tree
						return null;
					}
					else{
						try{
							ObjRef[] refs = this.getAll(curNode, tagName);
							if((refs != null) && (refs.length > 0)){
								curNode = refs[tagIndex];
								continue;
							}
						}
						catch(Exception e){}
						//Fell off the tree;
						return null;
					}
				}
			}
		}
		return curNode;
	}
	
	public XArchPath getXArchPath(ObjRef ref){
		Object co = get(ref);
		//if(!(co instanceof IXArch)){
		//	throw new IllegalArgumentException("Target of getXArchPath must be of type IXArch");
		//}
		
		if(!(co instanceof DOMBased)){
			throw new IllegalArgumentException("Targets of getXArchPath must be DOM-based");
		}
		
		Node coNode = ((DOMBased)co).getDOMNode();
		
		ArrayList tagNameList = new ArrayList();
		ArrayList tagIndexList = new ArrayList();
		ArrayList tagIDList = new ArrayList();
		
		Node n = coNode;
		while(true){
			if(n == null){
				//done
				break;
			}
			if(n.getNodeType() == Node.ATTRIBUTE_NODE){
				n = ((Attr)n).getOwnerElement();
			}
			else{
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element elt = (Element)n;
					String tagName = elt.getTagName();
					String tagID = IdTable.getId(elt);
					int tagIndex = -1;
					if(tagID == null){
						tagIndex = 0;
						Node sibNode = n;
						while(true){
							sibNode = sibNode.getPreviousSibling();
							if(sibNode == null){
								break;
							}
							if(sibNode.getNodeType() != Node.ELEMENT_NODE){
								break;
							}
							Element sibElt = (Element)sibNode;
							String sibTagName = sibElt.getTagName();
							if(!sibTagName.equals(tagName)){
								break;
							}
							tagIndex++;
						}
					}
					tagNameList.add(0, stripPrefix(tagName));
					tagIndexList.add(0, new Integer(tagIndex));
					tagIDList.add(0, tagID);
				}
				else{
					//done
					break;
				}
				n = n.getParentNode();
			}
		}
		String[] tagNameArr = (String[])tagNameList.toArray(new String[0]);
		Integer[] tagIndexArr = (Integer[])tagIndexList.toArray(new Integer[0]);
		String[] tagIDArr = (String[])tagIDList.toArray(new String[0]);
		int[] tagIndexIntArr = new int[tagIndexArr.length];
		for(int i = 0; i < tagIndexArr.length; i++){
			tagIndexIntArr[i] = tagIndexArr[i].intValue();
		}
		XArchPath xp = new XArchPath(tagNameArr, tagIndexIntArr, tagIDArr);
		return xp;
	}

	private String bulkQueryGetID(ObjRef ref){
		try{
			String id = (String)get(ref, "id");
			return id;
		}
		catch(Exception e){
			return null;
		}
	}

	public XArchBulkQueryResults bulkQuery(XArchBulkQuery q){
		ObjRef rootRef = q.getQueryRootRef();
		Object root = get(rootRef);
		String rootRefElementName = getElementName(rootRef);
		Class rootClass = root.getClass();
		XArchTypeMetadata resultTypeMetadata = getTypeMetadata(rootRef);
		XArchInstanceMetadata resultInstanceMetadata = getInstanceMetadata(rootRef);
		String rootID = bulkQueryGetID(rootRef);
		ObjRef xArchRef = getXArch(rootRef);
		XArchPath xArchPath = getXArchPath(rootRef);
		
		XArchBulkQueryResults qr = new XArchBulkQueryResults(rootRefElementName, rootRef, resultTypeMetadata, resultInstanceMetadata, rootClass, rootID, xArchPath, xArchRef); 
		
		XArchBulkQueryNode[] children = q.getChildren();
		for(int i = 0; i < children.length; i++){
			processBulkQueryNode(qr, children[i]);
		}
		return qr;
	}
	
	private XArchBulkQueryResultNode buildBulkQueryResultNode(ObjRef resultRef){
		String resultTagName = getElementName(resultRef);
		Object resultObject = get(resultRef);
		//System.out.println("resultObject = " + resultObject);
		XArchTypeMetadata resultTypeMetadata = getTypeMetadata(resultRef);
		XArchInstanceMetadata resultInstanceMetadata = getInstanceMetadata(resultRef);
		Class resultClass = resultObject.getClass();
		String resultID = bulkQueryGetID(resultRef);
		XArchPath xArchPath = getXArchPath(resultRef);
		return new XArchBulkQueryResultNode(resultTagName, resultRef, resultTypeMetadata, resultInstanceMetadata, resultClass, resultID, xArchPath);
	}
	
	private boolean bqIsXArchRef(ObjRef baseRef){
		Object co = get(baseRef);
		if(co == null){
			return false;
		}
		return (co instanceof edu.uci.isr.xarch.IXArch); 
	}
	
	private void processBulkQueryNode(XArchBulkQueryResultNode parentResultNode, XArchBulkQueryNode qn){
		ObjRef baseRef = parentResultNode.getObjRef();
		String tagName = qn.getTagName();
		boolean isPlural = qn.isPlural();

		if(isPlural){
			ObjRef[] resultRefs;
			
			if(bqIsXArchRef(baseRef)){
				ArrayList resultRefList = new ArrayList();
				
				try{
					ObjRef[] childRefs = getAll(baseRef, "Object");
					for(int i = 0; i < childRefs.length; i++){
						String childTagName = getElementName(childRefs[i]);
						if(childTagName != null) childTagName = capFirstLetter(childTagName);
						if((childTagName != null) && (childTagName.equals(tagName))){
							resultRefList.add(childRefs[i]);
						}
					}
				}
				catch(InvalidOperationException ioe){
					//Do nothing; there's no children of this kind.
				}
				resultRefs = (ObjRef[])resultRefList.toArray(new ObjRef[0]);
			}
			else{
				try{
					resultRefs = getAll(baseRef, tagName);
				}
				catch(InvalidOperationException ioe){
					resultRefs = new ObjRef[0];
				}
			}
			for(int i = 0; i < resultRefs.length; i++){
				XArchBulkQueryResultNode resultNode = buildBulkQueryResultNode(resultRefs[i]);
				parentResultNode.addChild(resultNode);
				XArchBulkQueryNode[] childQueryNodes = qn.getChildren();
				for(int j = 0; j < childQueryNodes.length; j++){
					processBulkQueryNode(resultNode, childQueryNodes[j]);
				}
			}
		}
		else{
			Object resultObj = null;
			if(bqIsXArchRef(baseRef)){
				try{
					ObjRef[] childRefs = getAll(baseRef, "Object");
					for(int i = 0; i < childRefs.length; i++){
						String childTagName = getElementName(childRefs[i]);
						if(childTagName != null) childTagName = capFirstLetter(childTagName);
						if((childTagName != null) && (childTagName.equals(tagName))){
							resultObj = childRefs[i];
							break;
						}
					}
				}
				catch(InvalidOperationException ioe){
					//do nothing
				}
			}
			else{
				try{
					resultObj = get(baseRef, tagName);
				}
				catch(InvalidOperationException ioe){
					//do nothing
				}
			}
			
			if(resultObj == null){
				//Nothing down this subtree.
				return;
			}
			else if(resultObj instanceof ObjRef){
				ObjRef resultRef = (ObjRef)resultObj;
				XArchBulkQueryResultNode resultNode = buildBulkQueryResultNode(resultRef);
				parentResultNode.addChild(resultNode);
				XArchBulkQueryNode[] childQueryNodes = qn.getChildren();
				for(int j = 0; j < childQueryNodes.length; j++){
					processBulkQueryNode(resultNode, childQueryNodes[j]);
				}
			}
			else if(resultObj instanceof String){
				String resultString = (String)resultObj;
				XArchBulkQueryResultNode resultNode = new XArchBulkQueryResultNode(tagName, resultString);
				parentResultNode.addChild(resultNode);
			}
			else{
				//this shouldn't happen
				throw new RuntimeException("get() call returned object that was not an ObjRef or a String: " + resultObj);
			}
		}
	}
	
	

}
