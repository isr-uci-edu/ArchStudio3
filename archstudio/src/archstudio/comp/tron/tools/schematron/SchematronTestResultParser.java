package archstudio.comp.tron.tools.schematron;

import archstudio.tron.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.*;

import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchPath;

public class SchematronTestResultParser{
	
	public static final String CONTENT_ELEMENT_DELIMITER_REGEX = "\\|\\*\\|";

	//Returns an array of objects.  Each Object is either a TronTestResult
	//or a SchematronTestException in case the test result parsing failed.
	public static Object[] parseTestResults(XArchFlatInterface xarch, ObjRef documentRef, String toolID, Document schematronProcessingResults){
		List testResultList = new ArrayList();
		
		TestResultSnippet[] resultSnippets = null;
		try{
			resultSnippets = parseTestResultSnippets(schematronProcessingResults);
			for(int i = 0; i < resultSnippets.length; i++){
				try{
					TronTestResult testResult = parseResultSnippet(xarch, documentRef, toolID, resultSnippets[i]);
					testResultList.add(testResult);
				}
				catch(SchematronTestException ste){
					testResultList.add(ste);
				}
			}
		}
		catch(SchematronTestException ste){
			testResultList.add(ste);
		}

		return testResultList.toArray();
	}
	
	private static TronTestResult parseResultSnippet(XArchFlatInterface xarch, ObjRef documentRef, String toolID, TestResultSnippet snippet)
	throws SchematronTestException{
		String testUID = snippet.activePatternElement.getAttribute("id");
		if(testUID == null){
			String desc = "Test result had no UID";
			String name = snippet.activePatternElement.getAttribute("name");
			if(name != null){
				desc += ": " + name;
			}
			throw new SchematronTestException(desc);
		}
		TronIssue[] tronIssues = new TronIssue[snippet.failedAssertElements.length];
		for(int j = 0; j < snippet.failedAssertElements.length; j++){
			Element failedAssertElt = snippet.failedAssertElements[j];
			String locationXPath = failedAssertElt.getAttribute("location");
			Element contentElt = (Element)failedAssertElt.getFirstChild();
			String content = getElementContents(contentElt);
			if((content == null) || (content.equals(""))){
				SchematronTestException ste = new SchematronTestException("Failed assert had no content.");
				ste.setTestUID(testUID);
				throw ste;
			}
			
			ContentProperties contentProperties = ContentProperties.create(content);
			//We have the testUID
			//We have the documentRef
			//We have the toolID,
			int severity = TronIssue.SEVERITY_ERROR;
			String severityString = contentProperties.getSeverity();
			if(severityString != null){
				if(severityString.toLowerCase().equals("warning")){
					severity = TronIssue.SEVERITY_WARNING;
				}
				else if(severityString.toLowerCase().equals("info")){
					severity = TronIssue.SEVERITY_INFO;
				}
			}
			//We have the severity.
			String headline = contentProperties.getText();
			if(headline == null){
				SchematronTestException ste = new SchematronTestException("Failed assert had no text.");
				ste.setTestUID(testUID);
				throw ste;
			}
			//We have the headline.
			String detailedDescription = contentProperties.getDetail();
			//if(detailedDescription == null){
			//	detailedDescription = "[No additional detail.]";
			//}
			//We have the detail
			String iconHref = contentProperties.getIconHref();
			//We have the icon href
			TronElementIdentifier[] elementIdentifiers = contentProperties.getElementIdentifiers();
			//We have the element identifiers.
			if(elementIdentifiers.length == 0){
				//This is a last-ditch effort to generate an element identifier
				//from the XPath location if one doesn't exist.
				XArchPath xArchPath = xpath2xarchpath(locationXPath);
				ObjRef resolvedRef = xarch.resolveXArchPath(documentRef, xArchPath);
				if(resolvedRef != null){
					TronElementIdentifier ei = new TronElementIdentifier(resolvedRef, null);
					elementIdentifiers = new TronElementIdentifier[]{ei};
				}
			}
			/*
			else{
				String id = elementIdentifiers[0].getElementID();
				ObjRef ref = xarch.getByID(documentRef, id);
				System.out.println(ref);
				System.out.println(xarch.getXArchPath(ref));
			}
			*/
			tronIssues[j] = new TronIssue(testUID, documentRef, toolID, severity,
				headline, detailedDescription, iconHref, elementIdentifiers);
		}
		return new TronTestResult(documentRef, testUID, tronIssues);
	}
	
	private static XArchPath xpath2xarchpath(String xpath){
		if(xpath.startsWith("/")){
			xpath = xpath.substring(1);
		}
		try{
			String[] pathSegments = xpath.split("\\/");
			for(int i = 0; i < pathSegments.length; i++){
				//strip the namespace
				int colonIndex = pathSegments[i].indexOf(":");
				pathSegments[i] = pathSegments[i].substring(colonIndex + 1);
				int bracketIndex = pathSegments[i].indexOf("[");
				if(bracketIndex != -1){
					String eltName = pathSegments[i].substring(0, bracketIndex);
					String numericSegment = pathSegments[i].substring(bracketIndex);
					numericSegment = numericSegment.substring(1, numericSegment.length() - 1);
					int number = Integer.parseInt(numericSegment);
					number--;
					pathSegments[i] = eltName + ":" + number;
				}
			}
			StringBuffer xArchPathBuf = new StringBuffer();
			for(int i = 0; i < pathSegments.length; i++){
				if(i != 0) xArchPathBuf.append("/");
				xArchPathBuf.append(pathSegments[i]);
			}
			XArchPath path = new XArchPath(xArchPathBuf.toString());
			return path;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	
	private static String getElementContents(Element elt){
		elt.normalize();
		NodeList childNodes = elt.getChildNodes();
		for(int i = 0; i < childNodes.getLength(); i++){
			if(childNodes.item(i) instanceof Text){
				return ((Text)childNodes.item(i)).getNodeValue();
			}
		}
		return "";
	}
	
	//The test results file is organized weirdly.  It looks like:
	//<ns...>
	//<ns...> ...
	//<active-pattern ... />
	//<failed-assert ... />
	//<failed-assert ... />
	//<active-pattern ... />
	//<failed-assert ... />
	//...
	//So all the failed assertions are at the top level but split
	//up by active-patterns.  There will always be an active-pattern
	//heading even if there are no failed-asserts
	private static TestResultSnippet[] parseTestResultSnippets(Document schematronProcessingResults)
	throws SchematronTestException{
		List testResultSnippetList = new ArrayList();
		
		Element rootElement = schematronProcessingResults.getDocumentElement();
		NodeList topLevelElements = rootElement.getChildNodes();
		
		Element activePatternElement = null;
		List failedAssertElementList = new ArrayList();
		for(int i = 0; i < topLevelElements.getLength(); i++){
			Node childNode = topLevelElements.item(i);
			if(childNode instanceof Element){
				Element childElt = (Element)childNode;
				String tagName = childElt.getLocalName();
				if(tagName.equals("active-pattern")){
					if(activePatternElement != null){
						//Save the old snippet
						TestResultSnippet snippet = new TestResultSnippet();
						snippet.activePatternElement = activePatternElement;
						snippet.failedAssertElements = (Element[])failedAssertElementList.toArray(new Element[0]);
						testResultSnippetList.add(snippet);
					}
					activePatternElement = childElt;
					failedAssertElementList.clear();
				}
				if(tagName.equals("failed-assert")){
					failedAssertElementList.add(childElt);
				}
			}
		}
		if(activePatternElement != null){
			//Save the old snippet
			TestResultSnippet snippet = new TestResultSnippet();
			snippet.activePatternElement = activePatternElement;
			snippet.failedAssertElements = (Element[])failedAssertElementList.toArray(new Element[0]);
			testResultSnippetList.add(snippet);
		}
		
		return (TestResultSnippet[])testResultSnippetList.toArray(new TestResultSnippet[0]);
	}
	
	private static class TestResultSnippet{
		public Element activePatternElement;
		public Element[] failedAssertElements;
	}
	
	private static class ContentProperties{
		private Properties properties;
		
		public ContentProperties(Properties properties){
			this.properties = properties;
		}
		
		public String getSeverity(){
			return properties.getProperty("severity");
		}
		
		public String getIconHref(){
			return properties.getProperty("iconHref");
		}
		
		public String getDetail(){
			return properties.getProperty("detail");
		}
		
		public String getText(){
			return properties.getProperty("text");
		}
		
		public TronElementIdentifier[] getElementIdentifiers(){
			List elementIdentifierList = new ArrayList();
			
			//Get the default ID
			String id = properties.getProperty("id");
			if(id != null){
				String iddesc = properties.getProperty("iddesc");
				TronElementIdentifier eltIdentifier = new TronElementIdentifier(id, iddesc);
				elementIdentifierList.add(eltIdentifier);
			}
			
			int index = 0;
			while(true){
				String indexid = properties.getProperty("id" + index);
				if(indexid != null){
					String indexiddesc = properties.getProperty("iddesc" + index);
					TronElementIdentifier eltIdentifier = new TronElementIdentifier(indexid, indexiddesc);
					elementIdentifierList.add(eltIdentifier);
					index++;
				}
				else{
					break;
				}
			}
			return (TronElementIdentifier[])elementIdentifierList.toArray(new TronElementIdentifier[0]);
		}
		
		public static ContentProperties create(String content) throws SchematronTestException{
			Properties newProperties = new Properties();
			String[] propertyElements = content.split(CONTENT_ELEMENT_DELIMITER_REGEX);
			for(int i = 0; i < propertyElements.length; i++){
				int equalsIndex = propertyElements[i].indexOf("=");
				if(equalsIndex == -1){
					if(newProperties.containsKey("text")){
						throw new SchematronTestException("Test result has multiple segments with name 'text'");
					}
					newProperties.put("text", propertyElements[i].trim());
				}
				else{
					String propName = propertyElements[i].substring(0, equalsIndex).trim();
					String propValue = propertyElements[i].substring(equalsIndex + 1).trim();
					if(newProperties.containsKey(propName)){
						throw new SchematronTestException("Test result has multiple segments with name '" + propName + "'");
					}
					newProperties.put(propName, propValue);
				}
			}
			return new ContentProperties(newProperties);
		}
	}
}
