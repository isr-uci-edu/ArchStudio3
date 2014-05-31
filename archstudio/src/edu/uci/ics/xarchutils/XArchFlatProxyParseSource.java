package edu.uci.ics.xarchutils;

/**
 * Class representing a parse source (file or URL) for an
 * XArchFlatProxy document.  Instances of this class should be
 * created through the static <code>create</code> methods contained
 * herein.
 * 
 * @author Eric M. Dashofy <a href="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</a>
 */
public class XArchFlatProxyParseSource{

	public static final int SOURCE_FILE = 100;
	public static final int SOURCE_URL = 200;
	
	private int sourceType;
	private Object sourceData;
	
	protected XArchFlatProxyParseSource(int sourceType, Object sourceData){
		this.sourceType = sourceType;
		this.sourceData = sourceData;
	}
	
	/**
	 * Create a file-based parse source.
	 * @see XArchFlatProxyImplementation.parse()
	 *
	 * @param fileName Filename of the XML file to parse.
	 * @return an XArchFlatProxyParseSource that can be passed to
	 * <code>XArchFlatProxyImplementation.parse</code>.
	 */
	public static XArchFlatProxyParseSource createFileSource(String fileName){
		return new XArchFlatProxyParseSource(SOURCE_FILE, fileName);
	}
	
	/**
	 * Create a URL-based parse source.
	 * @see XArchFlatProxyImplementation.parse()
	 * 
 	 * @param url URL of the XML file to parse.
	 * @return an XArchFlatProxyParseSource that can be passed to
	 * <code>XArchFlatProxyImplementation.parse</code>.
	 */
	public static XArchFlatProxyParseSource createURLSource(String url){
		return new XArchFlatProxyParseSource(SOURCE_URL, url); 
	}

	/**
	 * Get the source type of this parse source.
	 * Should return either <code>SOURCE_FILE</code> or
	 * <code>SOURCE_URL</code>.
	 * @return
	 */	
	public int getSourceType(){
		return sourceType;
	}
	
	/**
	 * Gets the source data of this parse source.
	 * The source data is dependent on the source type.
	 * If the source type is <code>SOURCE_FILE</code> then
	 * the source data is a String containing the filename.
	 * If the source type is <code>SOURCE_URL</code> then
	 * the source data is a URL containing the filename.
	 * @return Source data for the parse source.
	 */
	public Object getSourceData(){
		return sourceData;
	}
}
