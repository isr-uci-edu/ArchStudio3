package edu.uci.isr.registry;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
//import org.apache.xml.serialize.*;
import org.w3c.dom.*;

public class TextRegistrySerializer{
	public static final String NS_URI = "http://www.ics.uci.edu/~edashofy/textregistry.xsd";
	
	public String serialize(RegistryNode rn){
		if(!rn.isRoot()){
			throw new IllegalArgumentException("Node is not a registry root node.");
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			DOMImplementation dom = db.getDOMImplementation();
			DocumentType docType = dom.createDocumentType("RegistryDocType", "", "");
			Document doc = dom.createDocument(NS_URI, "registry", docType);
			Element rootElt = doc.getDocumentElement();
			
			RegistryNode[] children = rn.getAllChildren();
			for(int i = 0; i < children.length; i++){
				rootElt.appendChild(serialize(doc, children[i]));
			}
			return getPrettyXmlRepresentation(doc);
		}
		catch(ParserConfigurationException pce){
			pce.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a nice-looking representation of the given XML document.
	 * Sub-elements are properly indented, and long lines are indent-wrapped
	 * nicely, too.
	 * @param doc Document to turn into a string.
	 * @return String representation of the document, prettyprinted.
	 */
	private static String getPrettyXmlRepresentation(Document doc) throws DOMException{
		try{
			StringWriter sw = new StringWriter();

			Source domSource = new javax.xml.transform.dom.DOMSource(doc);
			Result streamResult = new javax.xml.transform.stream.StreamResult(sw);

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			t.transform(domSource, streamResult);

			return sw.toString();
		}
		catch(Exception e){
			System.err.println("This shouldn't happen.");
			e.printStackTrace();
			return null;
		}
	}

	protected Element serialize(Document doc, RegistryEntry re){
		Element thisElt = doc.createElement("entry");
		thisElt.setAttribute("key", re.getKey());
		
		String[] values = re.getAllValues();
		for(int i = 0; i < values.length; i++){
			Element valElt = doc.createElement("value");
			String encodedContent = values[i];
			try{
				encodedContent = java.net.URLEncoder.encode(values[i], "UTF-8");
			}
			catch(UnsupportedEncodingException uee){
				System.err.println("This shouldn't happen.");
				uee.printStackTrace();
			}
			valElt.setAttribute("content", encodedContent);
			thisElt.appendChild(valElt);
		}
		return thisElt;
	}
		
	protected Element serialize(Document doc, RegistryNode rn){
		Element thisElt = doc.createElement("node");
		thisElt.setAttribute("name", rn.getName());
		RegistryEntry[] entries = rn.getAllEntries();
		for(int i = 0; i < entries.length; i++){
			thisElt.appendChild(serialize(doc, entries[i]));
		}
		RegistryNode[] children = rn.getAllChildren();
		for(int i = 0; i < children.length; i++){
			thisElt.appendChild(serialize(doc, children[i]));
		}
		return thisElt;
	}
	
	public RegistryNode deserialize(InputStream is) throws IOException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(is);
			Element elt = doc.getDocumentElement();
			
			RegistryNode rn = new RegistryNode();
			NodeList childNodes = elt.getChildNodes();
			int len = childNodes.getLength();
			for(int i = 0; i < len; i++){
				Node n = childNodes.item(i);
				if(n instanceof Element){
					Element childElt = (Element)n;
					String tagName = childElt.getTagName().trim();
					if(tagName.equals("node")){
						RegistryNode chrn = deserializeRegistryNode(childElt);
						rn.putChild(chrn);
					}
				}
			}
			return rn;
		}
		catch(org.xml.sax.SAXException saxe){
			throw new RegistryParseException("Parse error parsing registry.");
		}
		catch(ParserConfigurationException pce){
			pce.printStackTrace();
			return null;
		}
	}

	public RegistryNode deserializeRegistryNode(Element elt){
		String name = elt.getAttribute("name");
		if((name == null) || (name.equals(""))){
			throw new RegistryParseException("Node missing name.");
		}
		RegistryNode rn = new RegistryNode(name);
		NodeList childNodes = elt.getChildNodes();
		int len = childNodes.getLength();
		for(int i = 0; i < len; i++){
			Node n = childNodes.item(i);
			if(n instanceof Element){
				Element childElt = (Element)n;
				String tagName = childElt.getTagName().trim();
				if(tagName.equals("node")){
					RegistryNode chrn = deserializeRegistryNode(childElt);
					rn.putChild(chrn);
				}
				else if(tagName.equals("entry")){
					RegistryEntry chre = deserializeRegistryEntry(childElt);
					rn.putEntry(chre);
				}
			}
		}
		return rn;
	}
	
	public RegistryEntry deserializeRegistryEntry(Element elt){
		String key = elt.getAttribute("key");
		if((key == null) || (key.equals(""))){
			throw new RegistryParseException("Entry missing key.");
		}
		RegistryEntry re = new RegistryEntry(key);
		NodeList childNodes = elt.getChildNodes();
		int len = childNodes.getLength();
		for(int i = 0; i < len; i++){
			Node n = childNodes.item(i);
			if(n instanceof Element){
				Element childElt = (Element)n;
				if(childElt.getTagName().trim().equals("value")){
					String content = childElt.getAttribute("content");
					if(content == null){
						throw new RegistryParseException("Entry value missing content.");
					}
					try{
						content = java.net.URLDecoder.decode(content, "UTF-8");
					}
					catch(UnsupportedEncodingException uee){
						System.err.println("This shoudn't happen.");
						uee.printStackTrace();
					}
					re.putValue(content);
				}
			}
		}
		return re;
	}
					
	
	
}

