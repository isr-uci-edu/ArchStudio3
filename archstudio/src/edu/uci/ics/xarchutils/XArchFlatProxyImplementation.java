package edu.uci.ics.xarchutils;

import edu.uci.isr.xarch.IXArch;
import edu.uci.isr.xarch.IXArchContext;
import edu.uci.isr.xarch.IXArchElement;
import edu.uci.isr.xarch.IXArchImplementation;
import edu.uci.isr.xarch.XArchParseException;
import edu.uci.isr.xarch.XArchSerializeException;

/**
 * <code>IXArchImplementation</code> implementation for the
 * XArchFlatProxy mechanism.
 * 
 * @author Eric M. Dashofy <a href="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</a>
 */
public class XArchFlatProxyImplementation implements IXArchImplementation{

	protected static int nextInt = 1;
	protected XArchFlatInterface xarch;

	protected XArchFlatProxyImplementation(XArchFlatInterface xarch){
		this.xarch = xarch;
	}
	
	/**
	 * Creates a new xArch document with a generated unique URI.
	 * Most callers will prefer <code>createXArch(params)</code>.
	 * @return Proxy to top-level element of a new document.
	 */
	public IXArch createXArch(){
		return createXArch(null);
	}

	/**
	 * Creates a new xArch document with a given URI.
	 * @param params An object whose <code>toString()</code>
	 * value represents the URI for the new document.
	 * @return Proxy to top-level element of new document.
	 */
	public IXArch createXArch(Object params){
		if(params == null){
			params = "urn:newArchitecture" + (nextInt++);
		}
		String newUri = params.toString();
		
		ObjRef ref = xarch.createXArch(newUri);
		IXArch xArch = (IXArch) XArchFlatProxyUtils.proxy(xarch, ref);
		return xArch;
	}

	public IXArchContext createContext(IXArch xArch, String contextType){
		if(!(xArch instanceof XArchFlatProxyInterface)){
			throw new IllegalArgumentException("Passed-in xArch must be a proxy object.");
		}
		ObjRef xArchRef = XArchFlatProxyUtils.getObjRef(xArch);
		ObjRef contextRef = xarch.createContext(xArchRef, contextType);
		return (IXArchContext)XArchFlatProxyUtils.proxy(xarch, contextRef);
	}

	/**
	 * Clone an xArch document.  The cloned document will have an
	 * auto-generated unique URI.  Most callers will prefer
	 * <code>cloneXArch(xArch, params)</code>.
	 * 
	 * @param xArch Proxy to top-level element of document to clone.
	 * @return Proxy to top-level element of cloned document.
	 */
	public IXArch cloneXArch(IXArch xArch){
		return cloneXArch(xArch, null);
	}
	
	/**
	 * Clone an xArch document.  The URI of the cloned document
	 * will be the <code>toString()</code> of the params Object.
	 * 
	 * @param xArch Proxy to top-level element of document to clone.
	 * @param params URI of the cloned document.
	 * @return Proxy to top-level element of cloned document.
	 */
	public IXArch cloneXArch(IXArch xArch, Object params){
		if(params == null){
			params = "urn:newArchitecture" + (nextInt++);
		}
		String newUri = params.toString();
		
		if(!(xArch instanceof XArchFlatProxyInterface)){
			throw new IllegalArgumentException("Passed-in xArch must be a proxy object.");
		}
		ObjRef xArchRef = XArchFlatProxyUtils.getObjRef(xArch);
		ObjRef cloneRef = xarch.cloneXArch(xArchRef, newUri);
		IXArch xArchClone = (IXArch) XArchFlatProxyUtils.proxy(xarch, cloneRef);
		return xArchClone;
	}

	public boolean isContainedIn(IXArch xArch, IXArchElement elt){
		if(!(xArch instanceof XArchFlatProxyInterface)){
			throw new IllegalArgumentException("Passed-in xArch must be a proxy object.");
		}
		if(!(elt instanceof XArchFlatProxyInterface)){
			throw new IllegalArgumentException("Passed-in elt must be a proxy object.");
		}
		ObjRef xArchRef = XArchFlatProxyUtils.getObjRef(xArch);
		ObjRef eltRef = XArchFlatProxyUtils.getObjRef(elt);
		return xarch.getXArch(eltRef).equals(xArchRef) && xarch.isAttached(eltRef);
	}

	/**
	 * Parses a document from a given source.  Valid document sources
	 * are instances of <code>XArchFlatProxyParseSource</code>.  See
	 * that class for more information.
	 * 
	 * @see XArchFlatProxyParseSource
	 * 
	 * @param documentSource The source from which to parse a document
	 * into a set of data bindings.
	 * @return <code>IXArch</code> corresponding to the top-level element of
	 * the parsed document.
	 * @throws IllegalArgumentException if the type of source is invalid.
	 * @throws XArchParseException if the source is valid but the parsing
	 * fails.
	 */
	public IXArch parse(Object documentSource) throws XArchParseException{
		if(documentSource instanceof XArchFlatProxyParseSource){
			XArchFlatProxyParseSource src = (XArchFlatProxyParseSource)documentSource;
			if(src.getSourceType() == XArchFlatProxyParseSource.SOURCE_FILE){
				String fileName = (String)src.getSourceData();
				try{
					ObjRef xArchRef = xarch.parseFromFile(fileName);
					IXArch xArch = (IXArch)XArchFlatProxyUtils.proxy(xarch, xArchRef);
					return xArch;
				}
				catch(Exception e){
					throw new XArchParseException(e);
				}
			}
			else if(src.getSourceType() == XArchFlatProxyParseSource.SOURCE_URL){
				String url = (String)src.getSourceData();
				try{
					ObjRef xArchRef = xarch.parseFromURL(url);
					IXArch xArch = (IXArch)XArchFlatProxyUtils.proxy(xarch, xArchRef);
					return xArch;
				}
				catch(Exception e){
					throw new XArchParseException(e);
				}
			}
		}
		throw new IllegalArgumentException("Can't parse given document source.");
	}

	/**
	 * Serializes an XArchFlatProxy-based xArch document into XML.
	 * Params are ignored in this implementation but may
	 * be supported in future versions.
	 * @param xArch Top-level element of document to serialize.
	 * @param params (ignored)
	 * @return String representation of the given document in XML format.
	 * @throws XArchSerializeException if the serialization failed.  In
	 * this implementation, the chained exception is generally a
	 * <code>DOMException</code>.
	 */
	public String serialize(IXArch xArch, Object params) throws XArchSerializeException{
		if(!(xArch instanceof XArchFlatProxyInterface)){
			throw new IllegalArgumentException("Passed-in xArch must be a proxy object.");
		}
		ObjRef xArchRef = XArchFlatProxyUtils.getObjRef(xArch);
		String serData = xarch.serialize(xArchRef);
		return serData;
	}

}
