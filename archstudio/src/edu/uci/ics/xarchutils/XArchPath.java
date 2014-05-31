package edu.uci.ics.xarchutils;

import java.util.*;

/**
 * An XArchPath is similar to an XML XPath but it works for xArch-based
 * XML documents.  An XArchPath can be converted to and from a string
 * easily with the functions in this class.  It is useful for quickly
 * assessing an element's position within an XML document without having
 * to call costly operations to walk around the XML tree.
 *
 * <p>An XArchPath is composed of segments, starting at the XML tree
 * root and proceeding down the tree to the specified element.
 * Each segment has one of the following formats:
 * 
 * <p><i>tagName</i> (for elements where that is the only tag)
 * <p><i>tagName</i>:<i>tagIndex</i> Where tagIndex is the the index
 * of that tag name within the list of all tags with the same name.
 * So, if there are five tags called ComponentInstance in the same
 * place, the third one is <code>componentInstance:2</code> (remember,
 * indices are zero-based).
 * <p><i>tagName</i>:id=<i>tagID</i> Where tagID is the ID of the
 * element, if the element has an ID attribute.
 *
 * <p>So, a sample XArchPath might be:
 *
 * <p><code>xArch/ArchStructure:id=hello there/Component:5/Description</code>
 * 
 * <p>Any slashes in the ID or tag name are escaped with a backslash;
 * backslashes are also escaped as a double-backslash.
 */
public class XArchPath implements java.io.Serializable{

	protected ArrayList tagNames;
	protected ArrayList tagIndexes;
	protected ArrayList tagIDs;
	
	/*
	public static void main(String[] args){
		XArchPath xpath = new XArchPath("xArch/ArchStructure:id=hello there/Component:5/Description");
		System.out.println(xpath.toString());
		System.out.println(xpath.toTagsOnlyString());
		System.out.println(xpath.toDumpString());
	}
  */
	
	/**
	 * Constructor used by parser; not for external consumption.
	 * @param tagNameArray Array of tag names, in order, in the XArchPath
	 * @param tagIndexArray Array of tag indices, in order, in the XArchPath.  Tags
	 * without a specific index use -1.
	 * @param tagIDArray Array of tag IDs, in order, in the XArchPath.  Tags
	 * without IDs use null.
	 */
	public XArchPath(String[] tagNameArray, int[] tagIndexArray, String[] tagIDArray){
		tagNames = new ArrayList(Arrays.asList(tagNameArray));
		tagIndexes = new ArrayList();
		for(int i = 0; i < tagIndexArray.length; i++){
			tagIndexes.add(new Integer(tagIndexArray[i]));
		}
		tagIDs = new ArrayList(Arrays.asList(tagIDArray));
	}
	
	/**
	 * Get the length, in number of elements, of this XArchPath.
	 * @return XArchPath's length.
	 */
	public int getLength(){
		return tagNames.size();
	}
	
	/**
	 * Get the name of the tag at the given segment.
	 * @param index Segment number
	 * @return name of tag at that segment
	 */
	public String getTagName(int index){
		return (String)tagNames.get(index);
	}
	
	/**
	 * Get the index of the tag at the given segment.
	 * @param index Segment number
	 * @return index of tag at that segment, or -1 if the
	 * index is not applicable
	 */
	public int getTagIndex(int index){
		return ((Integer)tagIndexes.get(index)).intValue();
	}
	
	/**
	 * Get the ID of the tag at the given segment.
	 * @param index Segment number
	 * @return ID of tag at that segment, or null if the
	 * index has no ID
	 */
	public String getTagID(int index){
		return (String)tagIDs.get(index);
	}
	

	protected void addSegment(int i, String s, String tagName, String tagAttribute){
		if(tagName.length() == 0){
			throw new IllegalArgumentException("Illegal XArchPath Specification; Zero Length Name at [" + i + "] in " + s);
		}
		
		tagNames.add(tagName);
		if(tagAttribute == null){
			tagIndexes.add(new Integer(-1));
			tagIDs.add(null);
		}
		else{
			if(tagAttribute.length() == 0){
				throw new IllegalArgumentException("Illegal XArchPath Specification; Zero Length Annotation at [" + i + "] in " + s);
			}
			
			if(tagAttribute.startsWith("id=")){
				tagAttribute = tagAttribute.substring(3);
				tagIndexes.add(new Integer(-1));
				tagIDs.add(tagAttribute);
			}
			else{
				try{
					int index = Integer.parseInt(tagAttribute);
					tagIndexes.add(new Integer(index));
					tagIDs.add(null);
				}
				catch(NumberFormatException nfe){
					throw new IllegalArgumentException("Illegal XArchPath Specification; Illegal Tagged Value at [" + i + "] in " + s);
				}
			}
		}
	}
	
	/**
	 * Parse a stringified XArchPath into an XArchPath object.
	 * @param s Stringified XArchPath
	 * @exception IllegalArgumentException if the string is invalid.
	 */
	public XArchPath(String s){
		tagNames = new ArrayList();
		tagIndexes = new ArrayList();
		tagIDs = new ArrayList();
		
		//If mode = 0; we're parsing a tag name
		//If mode = 1; we're parsing a tag attribute
		int mode = 0;
		
		StringBuffer tokenBuf = new StringBuffer();
		
		String tagName = null;
		String tagAttribute = null;
		
		try{
			for(int i = 0; i < s.length(); i++){
				char ch = s.charAt(i);
				if(ch == '\\'){
					i++;
					ch = s.charAt(i);
					tokenBuf.append(ch);
				}
				else{
					//It wasn't an escaped character, so it might
					//have been the delimiter
					if(ch == '/'){
						if(mode == 0){ //No tagged attribute
							tagName = tokenBuf.toString();
							tokenBuf.setLength(0);
							tagAttribute = null;
						}
						else if(mode == 1){ //we were parsing a tagged attribute
							tagAttribute = tokenBuf.toString();
							tokenBuf.setLength(0);
						}
						//This the end of the segment
						mode = 0; //mode = 0 now
						addSegment(i, s, tagName, tagAttribute);
						tagName = null;
						tagAttribute = null;
					}
					else if(ch == ':'){
						if(mode == 0){
							tagName = tokenBuf.toString();
							tokenBuf.setLength(0);
							//Now parsing a tagged attribute
							mode = 1;
						}
						else if(mode == 1){
							throw new IllegalArgumentException("Illegal XArchPath Specification; Illegal Colon at [" + i + "] in " + s);
						}
					}
					else{
						tokenBuf.append(ch);
					}
				}
			}
			if(tokenBuf.length() > 0){
				if(mode == 0){
					tagName = tokenBuf.toString();
					tagAttribute = null;
					addSegment(s.length(), s, tagName, tagAttribute);
				}
				if(mode == 1){
					tagAttribute = tokenBuf.toString();
					addSegment(s.length(), s, tagName, tagAttribute);
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			throw new IllegalArgumentException("Illegal XArchPath Specification; Unexpected end of path in " + s);
		}		
	}
	
	public static String escape(String s){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < s.length(); i++){
			char ch = s.charAt(i);
			if(ch == ':'){
				sb.append("\\:");
			}
			else if(ch == '/'){
				sb.append("\\/");
			}
			else if(ch == '\\'){
				sb.append("\\\\");
			}
			else{
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Converts this XArchPath into a string.
	 * @return String representation of this XArchPath
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < tagNames.size(); i++){
			String tagName = (String)tagNames.get(i);
			int tagIndex = ((Integer)tagIndexes.get(i)).intValue();
			String tagID = (String)tagIDs.get(i);
		
			sb.append(escape(tagName));
			if(tagID != null){
				sb.append(":id=" + escape(tagID));
			}
			else if(tagIndex != -1){
				sb.append(":" + tagIndex);
			}
			if(i < tagNames.size() - 1){
				sb.append("/");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Converts this XArchPath into a string, stripping all
	 * information except tag names.
	 * @return String representation of this XArchPath with
	 * tag names only.
	 */
	public String toTagsOnlyString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < tagNames.size(); i++){
			String tagName = (String)tagNames.get(i);
			int tagIndex = ((Integer)tagIndexes.get(i)).intValue();
			String tagID = (String)tagIDs.get(i);
			
			sb.append(escape(tagName));
			if(i < tagNames.size() - 1){
				sb.append("/");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Converts this XArchPath into a debugging string.
	 * @return String representation of this XArchPath, useful
	 * for debugging.
	 */
	public String toDumpString(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < tagNames.size(); i++){
			String tagName = (String)tagNames.get(i);
			int tagIndex = ((Integer)tagIndexes.get(i)).intValue();
			String tagID = (String)tagIDs.get(i);
			
			sb.append(tagName);
			sb.append(",");
			if(tagID != null){
				sb.append("id=" + tagID);
				sb.append(",");
			}
			else if(tagIndex != -1){
				sb.append("[");
				sb.append(tagIndex);
				sb.append("]");
			}
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}
		
}
