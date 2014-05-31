package edu.uci.ics.xarchutils;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.xml.sax.SAXException;

import edu.uci.isr.xarch.*;

/**
 * An interface that provides a single point of service for the entire set
 * of DOM-based xArch libraries.  All parameters to this interface are
 * serializable and do not contain direct pointers into the structure
 * of an xArch document.
 * 
 * @author Eric M. Dashofy (<a href="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</a>)
 */
public interface XArchFlatInterface extends XArchFlatQueryInterface{
	
	/**
	 * Creates a new IXArch object and returns a reference to it.
	 * @param uri The URI of this xArch.  If no URL is appropriate,
	 * then a <CODE>"urn:"</CODE> URI should be used.
	 * @return Reference to an IXArch object.
	 */
	public ObjRef createXArch(String uri);

	/**
	 * Clones an existing IXArch object and returns a reference
	 * to the clone.
	 * @param xArchRef Reference to the IXArch object to clone.
	 * @param newURI URI to assign to the cloned document.
	 * @return Reference to cloned IXArch object.
	 */
	public ObjRef cloneXArch(ObjRef xArchRef, String newURI);

	/**
	 * Parses an xArch document from a file, given its filename, and returns
	 * a reference to an IXArch object representing the contents of that file.
	 * <B><FONT COLOR="#FF0000">NOTE! </FONT></B> If the URI is already open,
	 * this function returns a reference to that already open URI.
	 * 
	 * @param fileName File name to parse.
	 * @return Reference to an IXArch object.
	 * @exception FileNotFoundException if the file was not found.
	 * @exception IOException if there was an I/O error reading the file.
	 * @exception SAXException if there was an error parsing the XML in the file.
	 */
	public ObjRef parseFromFile(String fileName) throws FileNotFoundException, IOException, SAXException;

	/**
	 * Parses an xArch document from a URI and returns
	 * a reference to an IXArch object representing the contents of that file.
	 * <B><FONT COLOR="#FF0000">NOTE! </FONT></B> If the URI is already open,
	 * this function returns a reference to that already open URI.
	 *
	 * @param urlString URL to load and parse.
	 * @return Reference to an IXArch object.
	 * @exception MalformedURLException if the URL was not well-formed.
	 * @exception IOException if there was an I/O error reading the file.
	 * @exception SAXException if there was an error parsing the XML in the file.
	 */
	public ObjRef parseFromURL(String urlString) throws MalformedURLException, IOException, SAXException;

	/**
	 * Writes an open xArch document to a file.
	 * Emits an XArchFileEvent as a side effect.
	 * 
	 * @param xArchRef Reference to the top-level element of the document
	 * to be written.
	 * @param fileName The filename to write the file to.
	 * @exception java.io.IOException if there was an I/O error writing the file.
	 */
	public void writeToFile(ObjRef xArchRef, String fileName) throws java.io.IOException;

	/**
	 * Closes an xArch document.  Invalidates all ObjRefs that refer to anything
	 * in the given referenced tree.
	 * Note: The preferred call to close an xArch document is <code>close(ObjRef)</code>
	 * since document URIs may change.
	 * @param uriString URI of the open xArch to close.
	 */
	public void close(String uriString);
	
	/**
	 * Closes an xArch document.  Invalidates all ObjRefs that refer to anything
	 * in the given referenced tree.
	 * @param xArchRef Top-level element of document to close.
	 */
	public void close(ObjRef xArchRef);

	/**
	 * Serializes an xArch document back to XML.
	 * @param xArchRef Reference to the IXArch node that is the root of the xArch document.
	 * @return Serialized XML document.
	 */
	public String serialize(ObjRef xArchRef);
	
	/**
	 * Adds a child to an xArch element.
	 * Roughly equivalent to: baseObject.add[TypeOfThing](thingToAdd);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void addComponent(IComponent c);</CODE>
	 * then that would be called here as:
	 * <CODE>add(baseObjectRef, "Component", cRef);</CODE>
	 * where baseObjectRef is a reference to the base object, the typeOfThing
	 * is shown as "Component", and the thingToAdd is a reference to an <CODE>IComponent</CODE>.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>add[typeOfThing]</CODE> method.
	 * @param typeOfThing A string containing the type of thing to add.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>addComponent</CODE>,
	 * then typeOfThing would be "Component".
	 * @param thingToAddRef Reference to the object to add as a child of baseObject.
	 */
	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef);

	/**
	 * Adds a set of children to an xArch element.
	 * Roughly equivalent to: baseObject.add[TypeOfThing](thingsToAdd);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void addComponents(Collection c);</CODE>
	 * then that would be called here as:
	 * <CODE>add(baseObjectRef, "Component", cRefs);</CODE>
	 * where baseObjectRef is a reference to the base object, the typeOfThing
	 * is shown as "Component", and the thingToAdd is a array of references to <CODE>IComponent</CODE>s.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>add[typeOfThing]s</CODE> method.
	 * @param typeOfThing A string containing the type of thing to add.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>addComponents</CODE>,
	 * then typeOfThing would be "Component".
	 * @param thingToAddRefs References to the objects to add as children of baseObject.
	 */
	public void add(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs);

	/**
	 * Clears a child or a set of children from an xArch element.
	 * Roughly equivalent to: baseObject.clear[TypeOfThing](); or
	 * baseObject.clear[TypeOfThing]s();
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void clearDescription();</CODE>
	 * then that would be called here as:
	 * <CODE>clear(baseObjectRef, "Description");</CODE>
	 * where baseObjectRef is a reference to the base object, and the typeOfThing
	 * is shown as "Description".
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>clear[typeOfThing](s)</CODE> method.
	 * @param typeOfThing A string containing the type of thing to clear.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>clearDescription</CODE> or <CODE>clearDescriptions</CODE>,
	 * then typeOfThing would be "Description".
	 */
	public void clear(ObjRef baseObjectRef, String typeOfThing);

	/**
	 * Removes a child element from a parent element.
	 * Roughly equivalent to: baseObject.remove[typeOfThing](thingToRemove);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void removeComponent(IComponent thingToRemove);</CODE>
	 * then that would be called here as:
	 * <CODE>remove(baseObjectRef, "Component", thingToRemove);</CODE>
	 * where baseObjectRef is a reference to the base object, and the typeOfThing
	 * is shown as "Component".  The value to remove is passed by object reference.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>remove[typeOfThing](...)</CODE> method.
	 * @param typeOfThing A string containing the type of thing where the
	 * value will be removed.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>removeComponent</CODE>,
	 * then typeOfThing would be "Component".
	 * @param thingToRemove A reference to the object to remove.
	 */
	public void remove(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToRemove);
	
	/**
	 * Removes a set of children from a parent element.
	 * Roughly equivalent to: baseObject.remove[typeOfThing]s(thingsToRemove);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void removeComponents(Collection thingsToRemove);</CODE>
	 * then that would be called here as:
	 * <CODE>remove(baseObjectRef, "Component", thingsToRemove);</CODE>
	 * where baseObjectRef is a reference to the base object, and the typeOfThing
	 * is shown as "Component".  The values to remove are passed as an array of
	 * object references.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>remove[typeOfThing]s(Collection)</CODE> method.
	 * @param typeOfThing A string containing the type of thing where the
	 * value will be removed.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>removeComponents</CODE>,
	 * then typeOfThing would be "Component".
	 * @param thingToRemove An array of references to the objects to remove.
	 */
	public void remove(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToRemove);

	/**
	 * Sets the value of an attribute or simple-value element.
	 * Roughly equivalent to: baseObject.set[typeOfThing](value);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void setValue(String value);</CODE>
	 * then that would be called here as:
	 * <CODE>set(baseObjectRef, "Value", value);</CODE>
	 * where baseObjectRef is a reference to the base object, and the typeOfThing
	 * is shown as "Value".  The value to set is passed as a string.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>set[typeOfThing](String)</CODE> method.
	 * @param typeOfThing A string containing the type of thing where the
	 * value will be set.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>setValue</CODE>,
	 * then typeOfThing would be "Value".
	 * @param value The value to set.
	 */
	public void set(ObjRef baseObjectRef, String typeOfThing, String value);

	/**
	 * Sets the value of an complex-type child element.
	 * Roughly equivalent to: baseObject.set[typeOfThing](value);
	 * So, if the object referred to by baseObjectRef implements a method called
	 * <CODE>void setDescription(IDescription value);</CODE>
	 * then that would be called here as:
	 * <CODE>set(baseObjectRef, "Description", value);</CODE>
	 * where baseObjectRef is a reference to the base object, and the typeOfThing
	 * is shown as "Description".  The values to set is passed as an object reference.
	 * @param baseObjectRef Reference to a base element containing an 
	 * <CODE>set[typeOfThing](...)</CODE> method.
	 * @param typeOfThing A string containing the type of thing where the
	 * value will be set.  For instance,
	 * if the object referred to by baseObjectRef contains a method called 
	 * <CODE>setValue</CODE>,
	 * then typeOfThing would be "Value".
	 * @param value The value to set.
	 */
	public void set(ObjRef baseObjectRef, String typeOfThing, ObjRef value);
	
	/**
	 * Creates a context object that can be used for creating, recontextualizing,
	 * and promoting elements in that context (XML namespace).
	 * Roughly equivalent to: new [contextType]Context(iXArch);
	 * 
	 * @param xArchObject Reference to an XArch object to contextualize.
	 * @param contextType The name of the context to create.  If the context object
	 * desired is, say, <CODE>TypesContext</CODE> then the <CODE>contextType</CODE>
	 * parameter will be <CODE>"types"</CODE>.
	 * @return Reference to a new context object of the given type.
	 */
	public ObjRef createContext(ObjRef xArchObject, String contextType);

	/**
	 * Creates an element in a context. <B>Note:</B> If you want to create
	 * a top-level element (that will be attached to an IXArch) you should use
	 * <CODE>createElement(...)</CODE> instead!
	 * Roughly equivalent to: contextObjectRef.create[typeOfThing]();
	 * 
	 * @param contextObjectRef Reference to a context object (created by
	 * <CODE>createContext(...)</CODE> that contains an appropriate 
	 * create method.
	 * @param typeOfThing Type of object to create.  If the context object
	 * contains a method called <CODE>createComponent(...)</CODE> then the <CODE>typeOfThing</CODE>
	 * parameter will be <CODE>"Component"</CODE>.
	 * @return Reference to a new object of the given type.
	 */
	public ObjRef create(ObjRef contextObjectRef, String typeOfThing);
	
	/**
	 * Creates a top-level element in a context.  Use this function in place
	 * Roughly equivalent to: contextObject.create[typeOfThing]Element();
	 * 
	 * @param contextObjectRef Reference to a context object (created by
	 * <CODE>createContext(...)</CODE> that contains an appropriate 
	 * createElement method.
	 * @param typeOfThing Type of object to create.  If the context object
	 * contains a method called <CODE>createArchStructureElement(...)</CODE> then the <CODE>typeOfThing</CODE>
	 * parameter will be <CODE>"ArchStructure"</CODE>.
	 * @return Reference to a new object of the given type.
	 */
	public ObjRef createElement(ObjRef contextObjectRef, String typeOfThing);

	/**
	 * Promotes a given object to one of its subtypes.
	 * Roughly equivalent to: contextObject.promoteTo[promotionTarget](targetObject);
	 * 
	 * @param contextObjectRef Reference to a context object (created by
	 * <CODE>createContext(...)</CODE> that contains an appropriate 
	 * promoteTo method.
	 * @param promotionTarget Type of object to promote targetObject to.  If the context object
	 * contains a method called <CODE>promoteToVariantComponent(IComponent)</CODE> then the <CODE>promotionTarget</CODE>
	 * parameter will be <CODE>"VariantComponent"</CODE>.
	 * @param targetObjectRef Reference to the object to be promoted.
	 * @return Reference to a promoted object of the promotionTarget type.
	 */
	public ObjRef promoteTo(ObjRef contextObjectRef, String promotionTarget, ObjRef targetObjectRef);

	/**
	 * Recontextualize an object created in a different context into the given
	 * context.
	 * Roughly equivalent to: contextObject.recontextualize[typeOfThing](targetObject);
	 * 
	 * @param contextObjectRef Reference to a context object (created by
	 * <CODE>createContext(...)</CODE> that contains an appropriate 
	 * recontextualize method.
	 * @param typeOfThing Type of object to be recontextualized.  If the context object
	 * contains a method called <CODE>recontextualizeComponent(IComponent)</CODE> then the <CODE>typeOfThing</CODE>
	 * parameter will be <CODE>"Component"</CODE>.
	 * @param targetObjectRef Reference to the object to be recontextualized.
	 * @return Reference to a recontextualized object.
	 */
	public ObjRef recontextualize(ObjRef contextObjectRef, String typeOfThing, ObjRef targetObjectRef);
	
	/**
	 * Gets all already-opened IXArch URLs.
	 * @deprecated Use getOpenXArchURIs() instead.
	 * @return Array of URL strings that represent open xArches.
	 */
	public String[] getOpenXArchURLs();

	/**
	 * Gets all already-opened IXArch URIs.
	 * @return Array of URI strings that represent open xArches.
	 */
	public String[] getOpenXArchURIs();
	
	/**
	 * Gets the top-level elements for all already-opened documents.
	 * @return array of ObjRefs referring to top-level elements of open xArches.
	 */
	public ObjRef[] getOpenXArches();
	
	/**
	 * Gets a reference to an already-open IXArch given its URI.
	 * @param uri URI of the already-open IXArch to get.
	 * @return <CODE>ObjRef</CODE> referring to the open IXArch, or
	 * <CODE>null</CODE> if the URI does not refer to an open xArch.
	 */
	public ObjRef getOpenXArch(String uri);

	/**
	 * Gets the URL of an open IXArch object.
	 * @param xArchRef An ObjRef to an IXArch object.
	 * @return URL of the xArch document, or <CODE>null</CODE> if one
	 * was not found.
	 * @deprecated Use getXArchURI() instead.
	 */
	public String getXArchURL(ObjRef xArchRef);
	
	/**
	 * Gets the URI of an open IXArch object.
	 * @param xArchRef An ObjRef to an IXArch object.
	 * @return URI of the xArch document, or <CODE>null</CODE> if one
	 * was not found.
	 */
	public String getXArchURI(ObjRef xArchRef);
	
	/**
	 * Determines if a given ObjRef is valid, that is, if there
	 * is an object associated with it.
	 * @param ref ObjRef to check.
	 * @return <CODE>true</CODE> if there is an associated object,
	 * <CODE>false</CODE> otherwise.
	 */
	public boolean isValidObjRef(ObjRef ref);

	/**
	 * For transaction oracle support.  Not for public consumption.
	 */
	public ObjRef cloneXArchElementDepthOne(ObjRef targetRef, String prefix);

	/**
	 * For transaction oracle support.  Not for public consumption.
	 */
	public void forgetAllWithPrefix(ObjRef xArchRef, String prefix);

	/**
	 * Clones an element with the given depth, changing IDs in the copy 
	 * if necessary to maintain consistency.
	 * @param targetObjectRef Element to clone.
	 * @param depth Depth to clone (see IXArchElement documentation).
	 * @return <CODE>ObjRef</CODE> to the cloned element.
	 */
	public ObjRef cloneElement(ObjRef targetObjectRef, int depth);

	/**
	 * Dumps the raw XML of an object to the console.  For debugging
	 * purposes ONLY.
	 * @param ref Reference to object to dump.
	 */
	public void dump(ObjRef ref);
	
	/**
	 * Changes the URI of an open document. All ObjRefs remain valid.
	 * An <code>XArchFileEvent</code> is emitted to notify listeners
	 * of the URI change.
	 * 
	 * @param oldURI URI of some currently open document.
	 * @param newURI The new URI for the document.
	 */
	public void renameXArch(String oldURI, String newURI);

	public XArchBulkQueryResults bulkQuery(XArchBulkQuery q);
}

