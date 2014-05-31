/*
 * Copyright (c) 2000-2001 Regents of the University of California.
 * All rights reserved.
 *
 * This software was developed at the University of California, Irvine.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the University of California, Irvine.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package edu.uci.ics.xarchutils;

import edu.uci.isr.xarch.*;

import edu.uci.isr.xarch.instance.*;
/*
import edu.uci.isr.xarch.types.*;
import edu.uci.isr.xarch.versions.*;
import edu.uci.isr.xarch.variants.*;
import edu.uci.isr.xarch.javaimplementation.*;
*/
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;


/* 
below imports required by parseToDocument, should go away 
if/when that method is moved to XArchUtils.
*/
import org.w3c.dom.*;
import org.xml.sax.*;
//import org.apache.xerces.parsers.*;
import org.xml.sax.SAXException;
import java.io.IOException;


/**
 * Id Table class.  Across multiple XArch's this class keeps track of:
 * <OL>
 * <LI>all Ids and their associates XArch elements
 * <LI>for each Id, all references to (XML Links 
 * that reference) the corresponding XArch element.
 * <LI> all open XArch's and their corresponding URL string (if any).
 * </OL>
 *
 * @author Kari Nies
 * @author Eric M. Dashofy
 */
// Implementation Note: this implementation assumes that ids are unique
// across XML documents.  In anticiaption that this assumption could
// change, both the id and the reference tables are partitioned by
// XArch instances.
// Amended:  I added functionality to let you have it both ways --EMD

public class IdTable 
{
	private Vector xArchFileListeners = new Vector();
	
	/**
	 * Instantiated XArch Listener for handling XArch Events.
	 * This listener is attatched to all XArch instances created
	 * by this class.
	 */
	private XArchListener listener = new XArchListener() {
		public void handleXArchEvent (XArchEvent evt) {
			handler(evt);
		}
	};
	
	/** Map to keep track URLs for all parsed XML documents */
	private Map urlToXArchMap = new HashMap();
	/** Map to keep a separate idTable for each xArch */
	private Map xArchToIdsMap = new HashMap();
	/** Maps xArches to therir reverse-ID-lookup tables */
	private Map xArchToReverseIdsMap = new HashMap();
	/** Map to keep a separate references table for each xArch.
	    Maps IXArches to Maps.  The internal Map maps 
	    ids (Strings) to Collections.  The Collections contain
	    IXArchElements. */
	private Map xArchToRefsMap = new HashMap();
	
	private IXArchImplementation xArchImplementation;
	private XMLLinkResolver resolver;
	
	/** 
	 * Constructor. Uses the default (DOM-based) implementation
	 * for its IXArchImplementation.
	 */
	public IdTable(){
		this(XArchUtils.getDefaultXArchImplementation());
	}
	
	/** 
	 * Constructor 
	 * @param xArchImplementation Specifies the IXArchImplementation
	 * object to use for document creation and manipulation.
	 */
	public IdTable(IXArchImplementation xArchImplementation){
		this.xArchImplementation = xArchImplementation;
		resolver = new XMLLinkResolver(this);
	}
	
	/**
	 * Gets the <code>IXArchImplementation</code> used by this
	 * <code>IdTable</code>.
	 * @return the <code>IXArchImplementation</code> used by this
	 * <code>IdTable</code>.
	 */
	public IXArchImplementation getXArchImplementation(){
		return xArchImplementation;
	}
	
	/**
	 * Gets an already open xArch.  A URL string must be provided.
	 * @param url The URL of the xArch to get.
	 * @return the given <CODE>IXArch</CODE> element, or <CODE>null</CODE>
	 * if none exists.
	 */
	public IXArch getXArch(String url){
		return (IXArch)urlToXArchMap.get(url);
	}
	
	/**
	 * Creates an instance of an empty XArch. A URL string must be
	 * provided.  For documents with no fixed URL, a <CODE>urn:</CODE>
	 * URL may be used.
	 * @param url The URL of the xArch to create.
	 * @return the newly created empty XArch.
	 */
	public IXArch createXArch(String url) {
		IXArch xArch = xArchImplementation.createXArch();
		xArch.addXArchListener(listener);
		urlToXArchMap.put(url, xArch);
		xArchToIdsMap.put(xArch, new HashMap());
		xArchToReverseIdsMap.put(xArch, new HashMap());
		xArchToRefsMap.put(xArch, new HashMap());
		fireXArchFileEvent(new XArchFileEvent(XArchFileEvent.XARCH_CREATED_EVENT, url));
		return xArch;
	}
	
	/**
	 * Clones an XArch (document). A URL string for the clone must be
	 * provided.  For documents with no fixed URL, a <CODE>urn:</CODE>
	 * URL may be used.
	 * @param xArchToClone The IXArch object to clone.
	 * @param url The URL for the cloned xArch.
	 * @return the newly created cloned XArch.
	 */
	public IXArch cloneXArch(IXArch xArchToClone, String url) {
		IXArch xArch = xArchImplementation.cloneXArch(xArchToClone);
		xArch.addXArchListener(listener);
		urlToXArchMap.put(url, xArch);
		xArchToIdsMap.put(xArch, new HashMap());
		xArchToReverseIdsMap.put(xArch, new HashMap());
		xArchToRefsMap.put(xArch, new HashMap());
		try{
			cacheIds(xArch);
			cacheRefs(xArch);
		}
		catch(SAXException se){
			//This shouldn't happen
			throw new RuntimeException(se.toString());
		}
		catch(IOException ioe){
			//This shouldn't happen
			throw new RuntimeException(ioe.toString());
		}
		fireXArchFileEvent(new XArchFileEvent(XArchFileEvent.XARCH_CREATED_EVENT, url));
		return xArch;
	}
	
	/**
	 * Creates and instance of an XArch and associates the XArch with
	 * the given URL string. If an XArch associated with the
	 * given URL string already exists, then that pre-existing
	 * XArch is returned. Otherwise the XML document whose location
	 * is specified by the given URL string is parsed and the corresponding
	 * XArch returned. An XArch created by this method may be
	 * referenced by an external XML document.
	 * <it> This implementation assumes that there is only one valid
	 * and unique URL string for a given XML document. Therefore the
	 * URL string must be chosen and used consistently.</it>
	 * @param url a URL string specifying the location of the XML document on  which the XArch will be based.
	 * @return an XArch instance based on the specified XML document
	 */
	public IXArch parseFromURL(String url) throws SAXException,IOException {
		IXArch xArch = (IXArch)(urlToXArchMap.get(url));
		if (xArch != null) {
			return xArch;
		}
		try{
			xArch = xArchImplementation.parse(new InputStreamReader(getContents(url)));
		
			xArch.addXArchListener(listener);
			urlToXArchMap.put(url, xArch);
			xArchToIdsMap.put(xArch, new HashMap()); 
			xArchToReverseIdsMap.put(xArch, new HashMap());
			xArchToRefsMap.put(xArch, new HashMap());
			cacheIds(xArch);
			//System.out.println("xArch is: " + xArch);
			cacheRefs(xArch);
			fireXArchFileEvent(new XArchFileEvent(XArchFileEvent.XARCH_OPENED_EVENT, url));
			return xArch;
		}
		catch(XArchParseException xpe){
			Throwable t = xpe.getCause();
			if(t != null){
				if(t instanceof SAXException){
					throw ((SAXException)t);
				}
				else if(t instanceof IOException){
					throw ((IOException)t);
				}
			}
			throw new RuntimeException(xpe);
		}
	}
	
	/**
	 * Given an <code>id</code> attribute value, returns the XArch element whose
	 * <code>id</code> attribute matches that value.
	 * @param id the <code>id</code> attribute value to search for.
	 * @return the XArch Element with the specified <code>id</code> attribute value.  Will return null if the given id is not known.
	 */
	public IXArchElement getEntity(String id) {
		Iterator itr = xArchToIdsMap.values().iterator();
		while(itr.hasNext()) {
			Map idtable = (Map)(itr.next());
			Object o = idtable.get(id);
			if (o != null) {
				return (IXArchElement) o;
			}
		}
		return null;
	}
	
	/**
	 * Given an <code>id</code> attribute value, and the xArch object
	 * representing the root of the tree to search, returns the XArch element whose
	 * <code>id</code> attribute matches that value.
	 * @param xArch The root element of the tree to search
	 * @param id the <code>id</code> attribute value to search for.
	 * @return the XArch Element with the specified <code>id</code> attribute value.  Will return null if the given id is not known.
	 */
	public IXArchElement getEntity(IXArch xArch, String id) {
		Map idtable = (Map)xArchToIdsMap.get(xArch);
		if(idtable == null){
			throw new IllegalArgumentException("No such xArch");
		}
		
		Object o = idtable.get(id);
		if(o != null){
			return (IXArchElement) o;
		}
		return null;
	}
	
	/**
	 * Given an <code>id</code> attribute value, returns a collection of XArch
	 * elements where each element references the XArch element with the
	 * given <code>id</code> attribute value. By "references" we mean that the
	 * referencing element is an xml link to the referenced element.
	 * If there are multiple elements with the same ID (in different documents),
	 * this will return an arbitrary one.
	 * @param id the <code>id</code> attribute value used to search for references.
	 * @return a collection of XArch elements that reference the
	 * XArch element with the given id value. If there
	 * are no such elements, an empty collection is returned.
	 */
	public Collection getReferences(String id) {
		Iterator itr = xArchToRefsMap.values().iterator();
		while(itr.hasNext()) {
			Map refstable = (Map)(itr.next());
			Collection c = (Collection) refstable.get(id);
			if (c != null) {
				return c;
			}
		}
		return new Vector();
	}
	
	/**
	 * Given an <code>id</code> attribute value, returns a collection of XArch
	 * elements where each element references the XArch element with the
	 * given <code>id</code> attribute value. By "references" we mean that the
	 * referencing element is an xml link to the referenced element.
	 * @param id the <code>id</code> attribute value used to search for references.
	 * @return a collection of XArch elements that reference the
	 * XArch element with the given id value. If there
	 * are no such elements, an empty collection is returned.
	 */
	public Collection getReferences(IXArch xArch, String id) {
		Map refsTable = (Map)(xArchToRefsMap.get(xArch));
		if(refsTable == null){
			return new Vector();
		}
		
		Collection c = (Collection) refsTable.get(id);
		if (c != null){
			return c;
		}
		return new Vector();
	}
	
	/**
	 * Test whether the XML document corresponding to given URL string
	 * has been parsed into memory and has a corresponding XArch.
	 * <it> This implementation assumes that there is only one valid
	 * and unique URL string for a given XML document.  Therefore one
	 * URL string must be chosen and used consistently.</it>
	 * @param url The URL string to test.
	 * @return whether or not the given URL has been parsed into memory.
	 */
	public boolean urlLoaded(String url) {
		IXArch xArch = (IXArch)(urlToXArchMap.get(url));
		return (xArch != null);
	}
	
	/**
	 * Returns a printable image of the entire contents of the Id Table.
	 * @return a String containing a printable image of of the Id Table.
	 */
	public String idTableView() {
		StringBuffer buf = new StringBuffer();
		buf.append('\n');
		Set urls = urlToXArchMap.keySet();
		Iterator urlItr = urls.iterator();
		// for each xArch
		while (urlItr.hasNext()) {
			String url = (String)(urlItr.next());
			buf.append("XArch URL: <" + url + ">\n");
			IXArch xArch = (IXArch)urlToXArchMap.get(url);
			Iterator idsItr;
			String id;
			// get Id Table
			Map idTable = (Map)(xArchToIdsMap.get(xArch));
			buf.append("Id Table:\n");
			idsItr = (idTable.keySet()).iterator();
			// for each id in Ids Table
			while (idsItr.hasNext()) {
				id = (String)(idsItr.next());
				buf.append("\tId: <" + id + "> ");
				IXArchElement xArchElt = (IXArchElement)(idTable.get(id));
				buf.append(xArchElt.getClass().getName() + '\n');
			}
			// get Refs Table
			Map refsTable = (Map)(xArchToRefsMap.get(xArch));
			buf.append("Refs Table:\n");
			idsItr = (refsTable.keySet()).iterator();
			// for each id in the Refs Table
			while (idsItr.hasNext()) {
				id = (String)(idsItr.next());
				buf.append("\tId: <" + id + ">\n");
				Collection cRefs = (Collection)(refsTable.get(id));
				for (Iterator refsItr = cRefs.iterator(); refsItr.hasNext();) {
					buf.append("\t\tXMLLink Parent: ");
					IXArchElement link = (IXArchElement)refsItr.next();
					Node Parent = ((DOMBased)link).getDOMNode().
						getParentNode();
					IXArchElement parentElement = (IXArchElement)
						makeWrapper(link.getXArch(), (Element)Parent);
					buf.append(parentElement.getClass().getName() + '\n');
				}
			}
		}
		return buf.toString();
	}
	
	private static boolean isEqual(IXArchElement e1,
		IXArchElement e2) {
		if(!(e1 instanceof DOMBased) || !(e2 instanceof DOMBased)) {
			throw new IllegalArgumentException
				("Cannot handle non-DOM-based xArch entities.");
		}
		return (((DOMBased)e1).getDOMNode() == ((DOMBased)e2).getDOMNode());
	}
	
	/**
	 * Testing routine that checks the Id tables against the References
	 * tables and makes sure that all references are valid.
	 */
	public void testIdTable () 
		throws SAXException, IOException {
		Set urls = urlToXArchMap.keySet();
		Iterator urlItr = urls.iterator();
		while (urlItr.hasNext()) {
			String url = (String)(urlItr.next());
			IXArch xArch = (IXArch)urlToXArchMap.get(url);
			Iterator idItr;
			String id;
			// get Id Table
			Map idTable = (Map)(xArchToIdsMap.get(xArch));
			// get Refs Table
			Map refsTable = (Map)(xArchToRefsMap.get(xArch));
			// for each id in the Refs table
			idItr = (refsTable.keySet()).iterator();
			while (idItr.hasNext()) {
				id = (String)(idItr.next());
				IXArchElement idElt = (IXArchElement)(idTable.get(id));
				IXArchElement refElt;
				IXMLLink ref;
				Collection c = (Collection)(refsTable.get(id));
				if (c != null ) {
					for (Iterator refsItr = c.iterator(); refsItr.hasNext();) {
						ref = (IXMLLink)(refsItr.next());
						refElt = resolver.resolveXMLLink(ref);
						// check that reference is valid
						if (refElt == null) {
							throw new RuntimeException("testIdTable failed.  Invalid reference:" + ref.getHref());
						}
						// check that refElt and idElt are equivalent.
						if (!isEqual(refElt, idElt)) {
							throw new RuntimeException("testIdTable failed equality test.");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Adds the given id and its associated XArch element to the
	 * Id table associated with the given XArch.
	 * @param xArch the XArch in which the element resides.
	 * @param id the id string for the given XArch element.
	 * @param elem the XArch element to be assoicated with the given id.
	 */
	private void addId(IXArch xArch, String id, IXArchElement elem) {
		Map idTable = (Map)(xArchToIdsMap.get(xArch));
		if (idTable == null) {
			throw new RuntimeException("no id table for the specified xArch");
		}
		Map reverseIdTable = (Map)xArchToReverseIdsMap.get(xArch);
		if (reverseIdTable == null) {
			throw new RuntimeException("no reverse id table for the specified xArch");
		}
		
		String oldId = (String)reverseIdTable.get(elem);
		idTable.remove(oldId);
		
		idTable.put(id, elem);
		reverseIdTable.put(elem, id);
	}
	
	/**
	 * Removes an id from the id table assoicated with the given XArch.
	 * @param xArch the XArch indicating which id table to search.
	 * @param id the id to be removed.
	 */
	private void removeId(IXArch xArch, String id) {
		Map idTable = (Map) (xArchToIdsMap.get(xArch));
		if (idTable == null) {
			throw new RuntimeException("no id table for the specified xArch");
		}
		Map reverseIdTable = (Map)xArchToReverseIdsMap.get(xArch);
		if (reverseIdTable == null) {
			throw new RuntimeException("no reverse id table for the specified xArch");
		}
		Object o = idTable.get(id);
		if(o != null){
			reverseIdTable.remove(o);
		}
		idTable.remove(id);
	}
	
	/**
	 * Given an XML Link element, returns the id string of the referenced
	 * element.  Determined by the character substring following a "#" in
	 * an href.
	 * @param xmlLink the XML Link element from which to extract an id string.
	 * @return the id string extracted from the href.
	 */
	private static String determineId(IXMLLink xmlLink) {
		String href = xmlLink.getHref();
		if (href == null) {
			throw new RuntimeException("null href.");
		}
		int i = href.indexOf('#');
		if (i == -1 || i == (href.length()-1)) {
			throw new RuntimeException("Invalid href: " + href);
		} else {
			return href.substring(i+1);
		}
	}
	
	/**
	 * Given an href, returns the id string of the referenced
	 * element.  Determined by the character substring following a "#" in
	 * an href.
	 * @param href The href part of an XLink
	 * @return the id string extracted from the href.
	 */
	private static String determineId(String href){
		if (href == null) {
			throw new RuntimeException("null href.");
		}
		int i = href.indexOf('#');
		if (i == -1 || i == (href.length()-1)) {
			throw new RuntimeException("Invalid href: " + href);
		} else {
			return href.substring(i+1);
		}
	}
	
	/**
	 * Given an XML Link element, returns the url string of the referenced
	 * element, if any.  Determined by the character substring preceeding
	 * "#" in an href.
	 * @param xmlLink the XML Link element from which to extract an id string.
	 * @return the url string extracted from the href, will be the empty
	 * string for a local reference.
	 */
	private static String determineURL(IXMLLink xmlLink) {
		String href = xmlLink.getHref();
		if (href == null) {
			throw new RuntimeException("null href.");
		}
		int i = href.indexOf('#');
		if (i == -1 || i == (href.length()-1)) {
			throw new RuntimeException("Invalid href: " + href);
		} else {
			return href.substring(0,i);
		}
	}
	
	/**
	 * Adds a new reference to the reference table.
	 * An XML link is used to determine both the <it referenced>
	 * and the <it referencing> element.  The id string is extracted
	 * from the href of the xml Link which indicates the
	 * referenced element.  The xml link itself is the
	 * referencing element - it is is the element that the reference 
	 * table will return.
	 * @param xmlLink The XML link used to create a new reference entry.
	 */
	private void addRef(IXMLLink xmlLink) 
		throws SAXException, IOException {
		// determine the xArch context
		IXArch xArch;
		String href = xmlLink.getHref();
		if(href == null){
			return;
		}
		if (href.indexOf('#') == 0) {
			// local ref
			xArch = xmlLink.getXArch();
		} else {
			// external ref
			String url = null;
			try{
				url = determineURL(xmlLink);
			}
			catch(RuntimeException e){
				//It's a bad or malformed reference, so we're not going to cache it.
				return;
			}
			
			try{
				xArch = parseFromURL(url);
			}
			catch(Exception e){
				//A (temporarily) bad href is not a reason to kill the program.
				System.err.println("Warning: " + e.toString());
				return;
			}
		}
		Map refsTable = (Map)(xArchToRefsMap.get(xArch));
		if (refsTable == null) {
			throw new RuntimeException
				("no reference table for the xArch: " + xArch);
		}
		String id = null;
		try{
			id = determineId(xmlLink);
		}
		catch(RuntimeException e){
			//It's a bad href or a work in progress.  Return.
			return;
		}
		Collection c = (Collection) refsTable.get(id);
		if (c == null) {
			c = new Vector();
			refsTable.put(id, c);
		}
		if (! c.contains(xmlLink)) {
			c.add(xmlLink);
		}
	}
	
	/** 
	 * Removes a reference from the reference table.
	 * @param xmlLink XML link indicating  the reference to be removed.
	 * @param href Old href that is no longer on this XML link to remove.
	 */
	private void removeRef(IXMLLink xmlLink, String href) 
		throws SAXException, IOException {
		// resolve xml link to get the context of the referenced node
		IXArchElement elem = null;
		
		try{
			elem = resolver.resolveHref(xmlLink.getXArch(), href);
		}
		catch(IllegalArgumentException iae){
			//The href was bad--we probably didn't cache it in the first place.
			return;
		}
		
		if(elem == null){
			//The href was bad--we probably didn't cache it in the first place
			return;
		}
		
		IXArch xArch = elem.getXArch();
		
		Map refsTable = (Map)(xArchToRefsMap.get(xArch));
		if (refsTable == null) {
			throw new RuntimeException
				("no reference table for the xArch: " + xArch);
		}
		String id = determineId(href);
		Collection c = (Collection) refsTable.get(id);
		if ((c != null) && (c.contains(xmlLink))) {
			c.remove(xmlLink);
		}
		//This next part removed because it unnecessarily throws an
		//exception if, for instance, the ID had nothing pointing to it
		//and somebody did a clearHref() or a setHref(), which are
		//valid operations.
		/* else {
			throw new RuntimeException("no existing reference to id: " + id);
		}*/
	}
	
	/** 
	 * Removes a reference from the reference table.
	 * @param xmlLink XML link indicating  the reference to be removed.
	 */
	private void removeRef(IXMLLink xmlLink) 
		throws SAXException, IOException {
		// resolve xml link to get the context of the referenced node
		IXArchElement elem = resolver.resolveXMLLink(xmlLink);
		if(elem == null){
			//It didn't point to anything, let's return.
			return;
		}
		
		IXArch xArch = elem.getXArch();
		
		Map refsTable = (Map)(xArchToRefsMap.get(xArch));
		if (refsTable == null) {
			throw new RuntimeException
				("no reference table for the xArch: " + xArch);
		}
		String id = determineId(xmlLink);
		Collection c = (Collection) refsTable.get(id);
		if ((c != null) && (c.contains(xmlLink))) {
			c.remove(xmlLink);
		}
		//This next part removed because it unnecessarily throws an
		//exception if, for instance, the ID had nothing pointing to it
		//and somebody did a clearHref() or a setHref(), which are
		//valid operations.
		/* else {
			throw new RuntimeException("no existing reference to id: " + id);
		}*/
	}
	
	public IXArchElement getParent(IXArchElement elt){
		if(!(elt instanceof DOMBased)){
			throw new IllegalArgumentException("This function does not work on non-DOM-based entities.");
		}
		DOMBased db = (DOMBased)elt;
		Node parentNode = db.getDOMNode().getParentNode();
		if(parentNode == null){
			//System.out.println("Parent was null of : " + elt);
			return null;
		}
		if(!(parentNode instanceof Element)){
			if(parentNode instanceof Document){
				//System.out.println("Parent was document of : " + elt);
				return null;
			}
			
			throw new RuntimeException("Parent node is a " + parentNode.getClass() + ", not an element?!?");
		}
		IXArchElement p = (IXArchElement)makeWrapper(elt.getXArch(), (Element)parentNode);
		//if(p ==  null) System.out.println("MakeWrapper failed on : " + elt);
		return p;
	}
	
	/**
	 * Wraps a DOM node, turning it into an XArch element.
	 * @param xArch XArch to which this node belongs.
	 * @param elt DOM node.
	 */
	public static Object makeWrapper (IXArch xArch, Element elt) {
		try{
			if(elt.equals(((DOMBased)xArch).getDOMNode())){
				return xArch;
			}
		}
		catch(Exception e){}
		
		//System.out.println("Processing tag: " + elt.getTagName());
		QName typeName = XArchUtils.getXSIType(elt);
		//System.out.println("Got xsi:type=" + typeName);
		if (typeName == null) {
			//System.out.println("No type name for elt: " + elt);
			return null;
		} else {
			try {
				// lifted from generated API code as makeDerivedWrapper
				String packageTitle = XArchUtils.getPackageTitle
					(typeName.getNamespaceURI());
				String packageName = XArchUtils.getPackageName(packageTitle);
				String implName = XArchUtils.getImplName
					(packageName, typeName.getName());
				//System.out.println("wrapping class <" + implName + ">\n");
				Class c = Class.forName(implName);
				
				java.lang.reflect.Constructor con = c.getConstructor
					(new Class[]{Element.class});
				Object o = con.newInstance(new Object[]{elt});
				if(o instanceof IXArchElement){
					((IXArchElement)o).setXArch(xArch);
				}
				return o;
			}
			catch(Exception e){
				//Lots of bad things could happen, but this
				//is OK, because this is best-effort anyway.
				//System.out.println("exception raised in makeWrapper");
				//e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getId(Node n){
		NamedNodeMap nnm = n.getAttributes();
		int len = nnm.getLength();
		for(int i = 0; i < len; i++){
			Attr child = (Attr)nnm.item(i);
			//System.out.println(child.getName());
			if(child.getName().equals("id")){
				return child.getValue();
			}
			if(child.getName().endsWith(":id")){
				return child.getValue();
			}
		}
		return null;
	}
	
	private void setId(Node n, String id){
		NamedNodeMap nnm = n.getAttributes();
		int len = nnm.getLength();
		for(int i = 0; i < len; i++){
			Attr child = (Attr)nnm.item(i);
			//System.out.println(child.getName());
			if(child.getName().equals("id")){
				child.setValue(id);
				return;
			}
			if(child.getName().endsWith(":id")){
				child.setValue(id);
				return;
			}
		}
	}
		
	
	/**
	 * Clone function that doesn't break IDs.  Prefixes
	 * all IDs with "Copy of" like the Windows file system;
	 * i.e. foo -> Copy of foo (or Copy (2) of foo, etc.).
	 * @param element <CODE>IXArchElement</CODE> to clone.
	 * @param depth Depth at which to clone.  See IXArchElement documentation
	 * for more info on depth.
	 */
	public IXArchElement cloneElement(IXArchElement elt, int depth){
		if(!(elt instanceof DOMBased)){
			throw new IllegalArgumentException("Parameter elt in function cloneElement() must be DOM-based.");
		}
		//First, clone the element.
		IXArchElement clonedElt = (IXArchElement)elt.cloneElement(depth);
		//Now, fix up all the IDs to clean up duplicates:
		changeIdsToCopies(elt.getXArch(), ((DOMBased)clonedElt).getDOMNode());
		/*
		try{
			cacheRefs(elt.getXArch(), ((DOMBased)elt).getDOMNode());		
		}
		catch(IOException ioe){
			throw new RuntimeException(ioe);
		}	
		catch(SAXException se){
			throw new RuntimeException(se);
		}
		*/
		return clonedElt;
	}
	
	private String getCopyIdValue(String idValue){
		if(!idValue.startsWith("Copy")){
			return "Copy of " + idValue;
		}
		if(idValue.startsWith("Copy of ")){
			return "Copy (2) of " + idValue.substring(8);
		}
		if(idValue.startsWith("Copy (")){
			StringBuffer sb = new StringBuffer();
			for(int i = 6; i < idValue.length(); i++){
				if(idValue.charAt(i) == ')'){
					break;
				}
				else{
					sb.append(idValue.charAt(i));
				}
			}
			
			String numString = sb.toString();
			int num;
			try{
				num = Integer.parseInt(numString);
			}
			catch(NumberFormatException nfe){
				return "Copy of " + idValue;
			}
			
			String currentPrefix = "Copy (" + numString + ") of ";
			if(!idValue.startsWith(currentPrefix)){
				return "Copy of " + idValue;
			}
			int newNum = num + 1;
			String newPrefix = "Copy (" + newNum + ") of ";
			String s = idValue.substring(currentPrefix.length());
			return newPrefix + s;
		}
		return "Copy of " + idValue;
	}
	
	private void changeIdsToCopies(IXArch xArch, Node n){
		if (n instanceof Element) {
			// check if n has an "id" attibute
			//System.out.println("Caching ID for: " + n);
			//String namespaceURI = 
			//    DOMUtils.getXSIType((Element)n).getNamespaceURI();
			String namespaceURI = ((Element)n).getNamespaceURI();
			String idValue = DOMUtils.getAttributeValue
				(n, namespaceURI, "id");
			if(idValue == null){
				idValue = getId((Element)n);
			}
			
			if (idValue != null) {
				boolean isGood = false;
				do{
					idValue = getCopyIdValue(idValue);
					if(getEntity(xArch, idValue) == null){
						isGood = true;
					}
				}while(!isGood);
				setId(n, idValue);
			}
		}
		NodeList nl = n.getChildNodes();
		int size = nl.getLength();
		for (int i = 0; i < size; i++) {
			changeIdsToCopies(xArch, nl.item(i));
		}
	}
		
	
	/**
	 * Recursive cacheIds.  Determines if a given node contains
	 * an Id attribute.  If so it is added to the Id table
	 * of the specified XArch.
	 * @param xArch XArch that is being traversed.
	 * @param n current node to check for an Id attribute.
	 */
	private void cacheIds (IXArch xArch, Node n) {
		if (n instanceof Element) {
			// check if n has an "id" attibute
			//System.out.println("Caching ID for: " + n);
			//String namespaceURI = 
			//    DOMUtils.getXSIType((Element)n).getNamespaceURI();
			String namespaceURI = ((Element)n).getNamespaceURI();
			String idValue = DOMUtils.getAttributeValue
				(n, namespaceURI, "id");
			if(idValue == null){
				idValue = getId((Element)n);
			}
			if (idValue != null) {
				Object o = makeWrapper(xArch, (Element)n);
				if (o == null) {
					//We probably don't have the appropriate xarchlibs installed.
					//This is not a fatal error.
					
					//throw new RuntimeException
						//("Failed to make wrapper while caching Ids.");
				}
				else{
					addId(xArch, idValue, (IXArchElement)o);
				}
			}
		}
		NodeList nl = n.getChildNodes();
		int size = nl.getLength();
		for (int i = 0; i < size; i++) {
			cacheIds(xArch, nl.item(i));
		}
	}
	
	/**
	 * Traverses the specified XArch searching for all nodes with Id
	 * attributes and adding them to the Id table.
	 * @param xArch XArch to search for Ids.
	 */
	private void cacheIds(IXArch xArch) {
		if(!(xArch instanceof DOMBased)) {
			throw new IllegalArgumentException
				("Cannot handle non-DOM-based xArch entities.");
		}
		Node root = ((DOMBased)xArch).getDOMNode();
		NodeList nl = root.getChildNodes();
		int size = nl.getLength();
		for (int i = 0; i < size; i++) {
			Node n = nl.item(i);
			cacheIds(xArch, n);
		}
	}
	
	/**
	 * Recursive cacheRefs.  Determines if a given node is an
	 * XML link.  If so it is added to the reference table.
	 * @param xArch XArch that is being traversed.
	 * @param n current node to check for an XML link.
	 */
	private void cacheRefs(IXArch xArch, Node n)         
		throws SAXException, IOException {
		// check for XML Links and continue traversing child nodes
		if (n instanceof Element) {
			if (DOMUtils.hasXSIType((Element)n, 
				"http://www.ics.uci.edu/pub/arch/xArch/instance.xsd", 
					"XMLLink")) {
				IXMLLink xmlLink = new XMLLinkImpl((Element)n);
				xmlLink.setXArch(xArch);
				addRef(xmlLink);
			}
		}
		NodeList nl = n.getChildNodes();
		int size = nl.getLength();
		Node child;
		for (int i = 0; i < size; i++) {
			child = nl.item(i);
			cacheRefs(xArch, child);
		}
	}    
	
	/**
	 * Traverses the specified XArch searching for all XML link
	 * nodes and adding THE PARENT node to the Reference Table.
	 * @param xArch XArch to search for XML Links.
	 */
	private void cacheRefs(IXArch xArch) 
		throws SAXException, IOException {
		if(!(xArch instanceof DOMBased)) {
			throw new IllegalArgumentException
				("Cannot handle non-DOM-based xArch entities.");
		}
		Node root = ((DOMBased)xArch).getDOMNode();
		NodeList nl = root.getChildNodes();
		int size = nl.getLength();
		for (int i = 0; i < size; i++) {
			Node n = nl.item(i);
			cacheRefs(xArch, n);
		}
	}
	
	/**
	 * Handler for the XArchListener that is attatched to each XArch
	 * element created by this class.
	 */
	private void handler(XArchEvent evt) {
		IXArchElement src = evt.getSource();
		int srcType = evt.getSourceType();
		int eventType = evt.getEventType();
		String targetName = evt.getTargetName();
		boolean isAttached = evt.getIsAttached();
		if (src instanceof IXArchElement) {
			try {
				IXArch xArch = ((IXArchElement)src).getXArch();
				String idValue;
				IXArchElement xArchElt;
				if (srcType == XArchEvent.ATTRIBUTE_CHANGED) {
					// handle change to id attribute, this includes
					// the creation of an new element with an id attribute.
					if (targetName.equals("id")) {
						idValue = (String)(evt.getTarget());
						if (eventType == XArchEvent.SET_EVENT) {
							addId(xArch, idValue, src);
						} 
						else if (evt.getEventType()== XArchEvent.CLEAR_EVENT){
							removeId(xArch, idValue);
						}
						// handle change to href attribute, this includes
						// the creation of a new elment with an href attribute.
					}
					else if (targetName.equals("href")) {
						if (src instanceof IXMLLink) {
							if (eventType == XArchEvent.SET_EVENT) {
								// XXX
								// Same thing as above...
								addRef((IXMLLink)src);
							}
							else if (evt.getEventType() == XArchEvent.CLEAR_EVENT) {
								removeRef((IXMLLink)src, (String)evt.getTarget());
							}
						}
					}
				} 
				else if ((srcType == XArchEvent.ELEMENT_CHANGED) && 
					(eventType == XArchEvent.ADD_EVENT)){
					xArchElt = (IXArchElement)(evt.getTarget());
					if(!(xArchElt instanceof DOMBased)){
						throw new IllegalArgumentException("Cannot handle non-DOM-based xArch entities.");
					}
					if(isAttached){
						cacheIds(xArchElt.getXArch(), ((DOMBased)xArchElt).getDOMNode());
						cacheRefs(xArchElt.getXArch(), ((DOMBased)xArchElt).getDOMNode());
					}
				}					
				else if ((srcType == XArchEvent.ELEMENT_CHANGED) && 
					(eventType == XArchEvent.REMOVE_EVENT)){
					xArchElt = (IXArchElement)(evt.getTarget());
					if (xArchElt instanceof IXMLLink) {
						removeRef((IXMLLink)xArchElt);
					}
					else {
						// handle removal of any element with and id attribute
						if(!(xArchElt instanceof DOMBased)) {
							throw new IllegalArgumentException
								("Cannot handle non-DOM-based xArch entities.");
						}
						// check if elt has an "id" attibute
						idValue = getId(((DOMBased)xArchElt).getDOMNode());
						//Node elt = ((DOMBased)xArchElt).getDOMNode();
						//String namespaceURI = 
						//	DOMUtils.getXSIType((Element)elt).getNamespaceURI();
						//idValue = DOMUtils.getAttributeValue
						//	(elt, namespaceURI, "id");
						if (idValue != null) {
							removeId(xArch, idValue);
						}
					}
				}                      
			} 
			catch (SAXException e) {
				throw new RuntimeException("SAXException thrown in handler.");
			} 
			catch (IOException e) {
				throw new RuntimeException("IOException thrown in handler.");
			}
		}
	}
	
	/**
	 * Removes an xArch from the IdTable class instance.
	 * @param xArch XArch to remove fromm IdTable.
	 */  
	public  void forgetXArch(IXArch xArch) {
		// Remove it from the list of active XArch's and remove
		// its corresponding Id Table, Reference Table.  Also
		// removs its listener.
		xArch.removeXArchListener(listener);
		xArchToIdsMap.remove(xArch);
		xArchToReverseIdsMap.remove(xArch);
		xArchToRefsMap.remove(xArch);
		Iterator itr = urlToXArchMap.keySet().iterator();
		while(itr.hasNext()) {
			String url = (String) itr.next();
			if (xArch == urlToXArchMap.get(url)) {
				urlToXArchMap.remove(url);
				fireXArchFileEvent(new XArchFileEvent(XArchFileEvent.XARCH_CLOSED_EVENT, url));
				break;
			}
		}
	}
	
	/** 
	 * Given a URL, returns an open InputStream.
	 * @param URL URL string for an XML document.
	 */
	private InputStream getContents(String url) 
		throws MalformedURLException, IOException {
		URL source = new URL(url);
		return source.openStream();
	}
	
	public void addXArchFileListener(XArchFileListener l){
		xArchFileListeners.addElement(l);
	}
	
	public void removeXArchFileListener(XArchFileListener l){
		xArchFileListeners.removeElement(l);
	}
	
	protected void fireXArchFileEvent(XArchFileEvent evt){
		for(Enumeration en = xArchFileListeners.elements(); en.hasMoreElements(); ){
			XArchFileListener l = (XArchFileListener)en.nextElement();
			l.handleXArchFileEvent(evt);
		}
	}
}
