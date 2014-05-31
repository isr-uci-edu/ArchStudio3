package archstudio.comp.graphlayout;

import java.io.*;
import java.util.*;

import archstudio.invoke.*;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

import archstudio.preferences.*;
import archstudio.comp.preferences.*;

//Imported to support xArch
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.nativeutils.*;
import archstudio.comp.xarchtrans.*;

public class GraphLayoutC2Component extends AbstractC2DelegateBrick
	implements IGraphLayout
{
	public static boolean DEBUG_WRITE_INOUT_FILES = true;

	protected IPreferences preferences;
	protected XArchFlatTransactionsInterface realxarch;
	
	public GraphLayoutC2Component(Identifier id){
		super(id);
		
		realxarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this,
			topIface, XArchFlatTransactionsInterface.class);
		
		preferences = (IPreferences)EBIWrapperUtils.addExternalService(this,
			topIface, IPreferences.class);
			
		// tells the world that this component is now providing its service (merge)
		//impl = new ArchMergeImpl(xarchTrans);
		//addMessageProcessor(new ArchMergeMessageProcessor());
		
		EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
			this, new Class[]{ IGraphLayout.class }, 
			new Class[0] );
		
		GraphLayoutPreferencePanel glpp = new GraphLayoutPreferencePanel();
		PreferencesUtils.deployPreferencesService(this, bottomIface, "ArchStudio 3/Graph Layout", glpp);
	}
	
	public GraphLayout doDotLayout(ObjRef archStructureRef, GraphParameters gp) throws CantFindGraphLayoutToolException{
		return doLayout(TOOL_DOT, archStructureRef, gp);
	}
	
	public GraphLayout doNeatoLayout(ObjRef archStructureRef, GraphParameters gp) throws CantFindGraphLayoutToolException{
		return doLayout(TOOL_NEATO, archStructureRef, gp);
	}
	
	public GraphLayout doLayout(int tool, ObjRef archStructureRef, GraphParameters gp) throws CantFindGraphLayoutToolException{
		GraphLayoutImpl gli = new GraphLayoutImpl(gp);
		return gli.getGraphLayout(tool, archStructureRef);
	}
	
	class GraphLayoutImpl{
		
		protected XArchFlatQueryInterface xarch;
		protected Map interfacePositions;
		protected GraphParameters gp;
		
		public GraphLayoutImpl(GraphParameters gp){
			this.gp = gp;
			interfacePositions = new HashMap();
		}
		
		public GraphLayout getGraphLayout(int tool, ObjRef structureRef) throws CantFindGraphLayoutToolException{
			XArchBulkQuery q = new XArchBulkQuery(structureRef);
			q.addQueryPath("component*/id");
			q.addQueryPath("component*/description/value");
			q.addQueryPath("component*/interface*/id");
			q.addQueryPath("component*/interface*/description/value");
			q.addQueryPath("component*/interface*/direction/value");

			q.addQueryPath("connector*/id");
			q.addQueryPath("connector*/description/value");
			q.addQueryPath("connector*/interface*/id");
			q.addQueryPath("connector*/interface*/description/value");
			q.addQueryPath("connector*/interface*/direction/value");
			
			q.addQueryPath("link*/id");
			q.addQueryPath("link*/description/value");
			q.addQueryPath("link*/point*/anchorOnInterface/type");
			q.addQueryPath("link*/point*/anchorOnInterface/href");
			
			XArchBulkQueryResults qr = realxarch.bulkQuery(q);
			this.xarch = new XArchBulkQueryResultProxy(realxarch, qr);

			//System.err.println("getting dot input");
			String toolInput = getToolInput(tool, structureRef);
			//System.out.println(dotInput);
			try{
				if(DEBUG_WRITE_INOUT_FILES){
					FileOutputStream fos = new FileOutputStream("dotinput.txt");
					fos.write(toolInput.getBytes());
					fos.close();
				}
				
				//System.err.println("running dot");
				String dotOutput = runTool(tool, toolInput);
				
				if(DEBUG_WRITE_INOUT_FILES){
					FileOutputStream fos = new FileOutputStream("dotoutput.txt");
					fos.write(dotOutput.getBytes());
					fos.close();
				}
				
				//System.err.println("processing output");
				GraphLayout gl = processDotOutput(dotOutput);
				return gl;
			}
			catch(IOException e){
				System.err.println("This shouldn't happen.");
				e.printStackTrace();
				return null;
			}
			//System.out.println("Done.");
		}
		
		protected String getNeatoStringForBrick(ObjRef brickRef){
			ObjRef[] interfaceRefs = xarch.getAll(brickRef, "Interface");
			
			String id = XadlUtils.getID(xarch, brickRef);
			String description = XadlUtils.getDescription(xarch, brickRef);
			
			XArchPath brickXArchPath = xarch.getXArchPath(brickRef);
			boolean isComponent = false;
			//System.out.println(brickXArchPath.toTagsOnlyString());
			if(brickXArchPath.toTagsOnlyString().endsWith("component")){
				isComponent = true;
			}
			StringBuffer brickLine = new StringBuffer();
			
			brickLine.append("  ");
			brickLine.append(getAlias(id));
			brickLine.append(" [shape=box");

			if(isComponent){
				brickLine.append(",width=");
				brickLine.append(gp.getRelativeComponentWidth());
				brickLine.append(",height=");
				brickLine.append(gp.getRelativeComponentHeight());
			}
			else{
				brickLine.append(",width=");
				brickLine.append(gp.getRelativeConnectorWidth());
				brickLine.append(",height=");
				brickLine.append(gp.getRelativeConnectorHeight());
			}

			brickLine.append(",label=\"");
			//brickLine.append(" [shape=box,label=\"");
			
			brickLine.append("<" + getAlias(id) + "> ");
			brickLine.append("\"];");

			List unknownList = new ArrayList();
			for(int i = 0; i < interfaceRefs.length; i++){
				String interfaceId = XadlUtils.getID(xarch, interfaceRefs[i]);
				unknownList.add(interfaceId);
			}
			
			List emptyList = new ArrayList();
			
			List[] interfaceListArray = new List[]{emptyList, emptyList, emptyList, emptyList, unknownList};
			interfacePositions.put(id, interfaceListArray);
			
			return brickLine.toString();
		}
		
		protected String getToolStringForBrick(int tool, ObjRef brickRef){
			switch(tool){
				case TOOL_DOT:
					return getDotStringForBrick(brickRef);
				case TOOL_NEATO:
					return getNeatoStringForBrick(brickRef);
				default:
					throw new IllegalArgumentException("Invalid tool");
			}
		}

		protected String getDotStringForBrick(ObjRef brickRef){
			ObjRef[] interfaceRefs = xarch.getAll(brickRef, "Interface");
		
			String id = XadlUtils.getID(xarch, brickRef);
			String description = XadlUtils.getDescription(xarch, brickRef);
			
			XArchPath brickXArchPath = xarch.getXArchPath(brickRef);
			boolean isComponent = false;
			//System.out.println(brickXArchPath.toTagsOnlyString());
			if(brickXArchPath.toTagsOnlyString().endsWith("component")){
				isComponent = true;
			}
		
			ArrayList unknownList = new ArrayList();
			ArrayList northList = new ArrayList();
			ArrayList eastList = new ArrayList();
			ArrayList southList = new ArrayList();
			ArrayList westList = new ArrayList();

			for(int i = 0; i < interfaceRefs.length; i++){
				String interfaceId = XadlUtils.getID(xarch, interfaceRefs[i]);
				String interfaceDescription = XadlUtils.getDescription(xarch, interfaceRefs[i]);
				String interfaceDirection = XadlUtils.getDirection(xarch, interfaceRefs[i]);
				
				String lcInterfaceDescription = interfaceDescription.toLowerCase();
				String lcInterfaceDirection = interfaceDirection.toLowerCase();
				
				if((lcInterfaceDescription.indexOf("top") != -1) ||
				(lcInterfaceDescription.indexOf("north") != -1)){
					northList.add(interfaceId);
				}
				else if((lcInterfaceDescription.indexOf("right") != -1) ||
				(lcInterfaceDescription.indexOf("peer") != -1) ||
				(lcInterfaceDescription.indexOf("east") != -1)){
					eastList.add(interfaceId);
				}
				else if((lcInterfaceDescription.indexOf("left") != -1) ||
				(lcInterfaceDescription.indexOf("west") != -1)){
					westList.add(interfaceId);
				}
				else if((lcInterfaceDescription.indexOf("bottom") != -1) ||
				(lcInterfaceDescription.indexOf("south") != -1)){
					southList.add(interfaceId);
				}
				else{
					if(lcInterfaceDirection.equals("in")){
						//System.out.println("adding 'in' to northlist");
						northList.add(interfaceId);
					}
					else if(lcInterfaceDirection.equals("out")){
						//System.out.println("adding 'out' to southlist");
						southList.add(interfaceId);
					}
					else{
						unknownList.add(interfaceId);
					}
				}
			}
 		
			StringBuffer brickLine = new StringBuffer();
		
			boolean useQuarteredRecord = (northList.size() + eastList.size() +
			southList.size() + westList.size()) > 0;
 		 		
			if(useQuarteredRecord){
				//Distribute the unknown-side interfaces around the other sides
				ArrayList shortestList;
				for(int i = 0; i < unknownList.size(); i++){
					String unknownId = (String)unknownList.get(i);
					shortestList = southList;
					if(northList.size() < shortestList.size()) shortestList = northList;
					if(eastList.size() < shortestList.size()) shortestList = eastList;
					if(westList.size() < shortestList.size()) shortestList = westList;
					shortestList.add(unknownId);
				}
				unknownList.clear();
 			
				brickLine.append("  ");
				brickLine.append(getAlias(id));
				brickLine.append(" [shape=record");
				if(isComponent){
					brickLine.append(",width=");
					brickLine.append(gp.getRelativeComponentWidth());
					brickLine.append(",height=");
					brickLine.append(gp.getRelativeComponentHeight());
				}
				else{
					brickLine.append(",width=");
					brickLine.append(gp.getRelativeConnectorWidth());
					brickLine.append(",height=");
					brickLine.append(gp.getRelativeConnectorHeight());
				}
				brickLine.append(",label=\"");
 			
				brickLine.append("{");
				//Append the northlist
				if(northList.size() == 0){
					brickLine.append("<__n> |");
				}
				else if(northList.size() == 1){
					brickLine.append("<" + getAlias((String)northList.get(0)) + "> |");
				}
				else{
					brickLine.append("{");
					for(int i = 0; i < northList.size(); i++){
						brickLine.append("<" + getAlias((String)northList.get(i)) + "> ");
						if(i < (northList.size() - 1)){
							brickLine.append("|");
						}
					}
					brickLine.append("} |");
				}

				brickLine.append("{");
				//Append the westlist
				if(westList.size() == 0){
					brickLine.append("<__w> |");
				}
				else if(westList.size() == 1){
					brickLine.append("<" + getAlias((String)westList.get(0)) + "> |");
				}
				else{
					brickLine.append("{");
					for(int i = 0; i < westList.size(); i++){
						brickLine.append("<" + getAlias((String)westList.get(i)) + "> ");
						if(i < (westList.size() - 1)){
							brickLine.append("|");
						}
					}
					brickLine.append("} |");
				}
			
				brickLine.append("<" + getAlias(id) + "> |");
			
				//Append the eastlist
				if(eastList.size() == 0){
					brickLine.append("<__e> }|");
				}
				else if(eastList.size() == 1){
					brickLine.append("<" + getAlias((String)eastList.get(0)) + "> }|");
				}
				else{
					brickLine.append("{");
					for(int i = 0; i < eastList.size(); i++){
						brickLine.append("<" + getAlias((String)eastList.get(i)) + "> ");
						if(i < (eastList.size() - 1)){
							brickLine.append("|");
						}
					}
					brickLine.append("} }|");
				}

				//Append the southlist
				if(southList.size() == 0){
					brickLine.append("<__s> }");
				}
				else if(southList.size() == 1){
					brickLine.append("<" + getAlias((String)southList.get(0)) + "> }");
				}
				else{
					brickLine.append("{");
					for(int i = 0; i < southList.size(); i++){
						brickLine.append("<" + getAlias((String)southList.get(i)) + "> ");
						if(i < (southList.size() - 1)){
							brickLine.append("|");
						}
					}
					brickLine.append("} ");
				}
				brickLine.append("}");
				brickLine.append("\"];");
			}
			else{ //useQuarteredRecord = false
				brickLine.append("  ");
				brickLine.append(getAlias(id));
				brickLine.append(" [shape=record");

				if(isComponent){
					brickLine.append(",width=");
					brickLine.append(gp.getRelativeComponentWidth());
					brickLine.append(",height=");
					brickLine.append(gp.getRelativeComponentHeight());
				}
				else{
					brickLine.append(",width=");
					brickLine.append(gp.getRelativeConnectorWidth());
					brickLine.append(",height=");
					brickLine.append(gp.getRelativeConnectorHeight());
				}

				brickLine.append(",label=\"");
				//brickLine.append(" [shape=box,label=\"");
 			
				if(unknownList.size() == 0){
					brickLine.append("<" + getAlias(id) + "> ");
				}
				else{
					
					//For blocks with 2 endpoints, render them top and bottom;
					//for more than 2, render them left to right.
					if(unknownList.size() <= 2){
						brickLine.append("{");
					}
					for(int i = 0; i < unknownList.size(); i++){
						brickLine.append("<" + getAlias((String)unknownList.get(i)) + "> ");
						if(i < (unknownList.size() - 1)){
							brickLine.append("|");
						}
					}
					if(unknownList.size() <= 2){
						brickLine.append("}");
					}
				}
				brickLine.append("\"];");
			}

			//Update the interfacePositions map
			//System.out.println("updating the interfacepositions for " + id);
			List[] interfaceListArray = new List[]{northList, southList, eastList, westList, unknownList};
			interfacePositions.put(id, interfaceListArray);

			return brickLine.toString();
		}
	
		protected String getToolStringForLink(int tool, ObjRef linkRef){
			String id = XadlUtils.getID(xarch, linkRef);
			String desc = XadlUtils.getDescription(xarch, linkRef);
		
			StringBuffer linkLine = new StringBuffer();
		
			String brick1Id = null;
			String interface1Id = null;
		
			String brick2Id = null;
			String interface2Id = null;
		
			ObjRef[] pointRefs = xarch.getAll(linkRef, "Point");
			if(pointRefs.length != 2){
				return null;
			}
		
			//Get brick/interface combo on Point 0
			ObjRef anchor1Ref = (ObjRef)xarch.get(pointRefs[0], "anchorOnInterface");
			//System.out.println("got here 1");
			if(anchor1Ref == null) return null;
			String href1 = (String)xarch.get(anchor1Ref, "href");
			if(href1 == null) return null;
			if(!href1.startsWith("#")) return null;
		
			//System.out.println("got here 2");
			interface1Id = href1.substring(1);
			if(interface1Id == null) return null;
			ObjRef iface1Ref = xarch.getByID(interface1Id);
			if(iface1Ref == null) return null;
			String interface1Description = XadlUtils.getDescription(xarch, iface1Ref);
		
			ObjRef brick1Ref = xarch.getParent(iface1Ref);
			brick1Id = XadlUtils.getID(xarch, brick1Ref);
		
			//System.out.println("got here 3");
			//Get brick/interface combo on Point 1
			ObjRef anchor2Ref = (ObjRef)xarch.get(pointRefs[1], "anchorOnInterface");
			if(anchor2Ref == null) return null;
			String href2 = (String)xarch.get(anchor2Ref, "href");
			if(href2 == null) return null;
			if(!href2.startsWith("#")) return null;
		
			//System.out.println("got here 4");
			interface2Id = href2.substring(1);		
			if(interface2Id == null) return null;
			ObjRef iface2Ref = xarch.getByID(interface2Id);
			if(iface2Ref == null) return null;
			String interface2Description = XadlUtils.getDescription(xarch, iface2Ref);
		
			ObjRef brick2Ref = xarch.getParent(iface2Ref);
			brick2Id = XadlUtils.getID(xarch, brick2Ref);
		
			//System.out.println("got here 5");
			if((brick1Id == null) || (brick2Id == null) ||
			(interface1Id == null) || (interface2Id == null)){
				//System.out.println("brick1ID = " + brick1Id);
				//System.out.println("int1ID = " + interface1Id);
				//System.out.println("brick2ID = " + brick2Id);
				//System.out.println("int2ID = " + interface2Id);
				return null;
			}
			//System.out.println("got here 6");
			
			if(tool == TOOL_DOT){
				if(interface1Description.toLowerCase().indexOf("top") != -1){
					if(interface2Description.toLowerCase().indexOf("bottom") != -1){
						String t = brick1Id;
						brick1Id = brick2Id;
						brick2Id = t;
						
						t = interface1Id;
						interface1Id = interface2Id;
						interface2Id = t;
					}
				}
			
				if(interface1Description.toLowerCase().indexOf("north") != -1){
					if(interface2Description.toLowerCase().indexOf("south") != -1){
						String t = brick1Id;
						brick1Id = brick2Id;
						brick2Id = t;
						
						t = interface1Id;
						interface1Id = interface2Id;
						interface2Id = t;
					}
				}
				
				if(interface1Description.toLowerCase().indexOf("left") != -1){
					if(interface2Description.toLowerCase().indexOf("right") != -1){
						String t = brick1Id;
						brick1Id = brick2Id;
						brick2Id = t;
						
						t = interface1Id;
						interface1Id = interface2Id;
						interface2Id = t;
					}
				}

				if(interface1Description.toLowerCase().indexOf("west") != -1){
					if(interface2Description.toLowerCase().indexOf("east") != -1){
						String t = brick1Id;
						brick1Id = brick2Id;
						brick2Id = t;
						
						t = interface1Id;
						interface1Id = interface2Id;
						interface2Id = t;
					}
				}
			}
			
			linkLine.append("  ");
			linkLine.append(getAlias(brick1Id));
			//if(tool == TOOL_DOT){
				linkLine.append(":");
				linkLine.append(getAlias(interface1Id));
			//}
		
			linkLine.append(" -> ");
		
			linkLine.append(getAlias(brick2Id));
			//if(tool == TOOL_DOT){
				linkLine.append(":");
				linkLine.append(getAlias(interface2Id));
			//}
		
			linkLine.append(" [label=\"");
			linkLine.append(getAlias(id));
			linkLine.append("\"]");
			
			linkLine.append(";");
			return linkLine.toString();
		}

		public String getToolInput(int tool, ObjRef archStructureRef){
			String lineSeparator = System.getProperty("line.separator");
			ObjRef[] componentRefs = xarch.getAll(archStructureRef, "Component");
			ObjRef[] connectorRefs = xarch.getAll(archStructureRef, "Connector");
			ObjRef[] linkRefs = xarch.getAll(archStructureRef, "Link");
		
			//System.out.println("component count = " + componentRefs.length);
			//System.out.println("connector count = " + connectorRefs.length);
			//System.out.println("link count = " + linkRefs.length);
		
			StringBuffer toolInput = new StringBuffer();
		
			toolInput.append("digraph arch{").append(lineSeparator);
			//toolInput.append("  node [shape=record,height=100,width=100];").append(lineSeparator);
			toolInput.append("  node [shape=record,height=1.0,width=1.0];").append(lineSeparator);
			//if(tool == TOOL_NEATO){
			//	toolInput.append("  edge [len=7];").append(lineSeparator);				
			//}
			
			if(tool == TOOL_DOT){
				String nodesep = (String)gp.getProperty("nodesep");
				if(nodesep != null){
					try{
						double d = Double.parseDouble(nodesep);
						//It's a double
						toolInput.append("  nodesep = ").append(nodesep).append(";").append(lineSeparator);				
					}
					catch(NumberFormatException nfe){}
				}
				String ranksep = (String)gp.getProperty("ranksep");
				if(ranksep != null){
					try{
						double d = Double.parseDouble(ranksep);
						//It's a double
						toolInput.append("  ranksep = ").append(ranksep).append(";").append(lineSeparator);				
					}
					catch(NumberFormatException nfe){}
				}
			}
			
			toolInput.append(lineSeparator);
			
			for(int i = 0; i < componentRefs.length; i++){
				toolInput.append(getToolStringForBrick(tool, componentRefs[i]));
				toolInput.append(lineSeparator);
			}

			toolInput.append(lineSeparator);

			for(int i = 0; i < connectorRefs.length; i++){
				toolInput.append(getToolStringForBrick(tool, connectorRefs[i]));
				toolInput.append(lineSeparator);
			}
		
			toolInput.append(lineSeparator);

			for(int i = 0; i < linkRefs.length; i++){
				toolInput.append(getToolStringForLink(tool, linkRefs[i]));
				toolInput.append(lineSeparator);
			}

			toolInput.append("}");
			toolInput.append(lineSeparator);
			return toolInput.toString();
		}

		protected HashMap aliasMap = new HashMap();
		int idCount = 0;
	
		public String getAlias(String id){
			String alias = (String)aliasMap.get(id);
			if(alias != null){
				return alias;
			}
			alias = "elt" + idCount;
			idCount++;
			aliasMap.put(id, alias);
			return alias;
		}
		
		public String getId(String alias){
			Set entries = aliasMap.entrySet();
			for(Iterator it = entries.iterator(); it.hasNext(); ){
				Map.Entry entry = (Map.Entry)it.next();
				if(entry.getValue().equals(alias)){
					return (String)entry.getKey();
				}
			}
			return null;
		}
		
		public String runTool(int tool, String toolInput) throws IOException, CantFindGraphLayoutToolException{
			String toolName = null;
			switch(tool){
			case TOOL_DOT:
				toolName = "dot";
				break;
			case TOOL_NEATO:
				toolName = "neato";
				break;
			default:
				throw new IllegalArgumentException("Invalid tool");
			}
			File toolExe = null;
			
			int os = SystemUtils.guessOperatingSystem();
			String toolFilename = toolName;
			if(os == SystemUtils.OS_WINDOWS){
				toolFilename = toolFilename + ".exe";
			}
			
			if(preferences.keyExists(IPreferences.SYSTEM_SPACE, 
			"/archstudio/comp/graphlayout", "graphvizpath")){
				String graphvizPath = preferences.getStringValue(IPreferences.SYSTEM_SPACE, 
				"/archstudio/comp/graphlayout", "graphvizpath", null);
				
				if(graphvizPath != null){
					toolExe = new File(graphvizPath, toolFilename);
					if(!toolExe.exists()){
						File bindir = new File(graphvizPath, "bin");
						toolExe = new File(bindir, toolFilename);
					}
				}
			}
			
			if((toolExe == null) || (!toolExe.exists())){
				toolExe = SystemUtils.findFileOnSystemPath(toolFilename);
				if((toolExe == null) || (!toolExe.exists())){
					throw new CantFindGraphLayoutToolException("Can't find " + toolName + "; checked preferences and system path.");
				}
			}
			
			String pathToToolExe = toolExe.getAbsolutePath();
			
			//We have to flip Y so we don't get a mathematical (i.e. 0,0 at the lower-left corner)
			//coordinate system.
			List commandLineEltList = new ArrayList();
			commandLineEltList.add(pathToToolExe);
			commandLineEltList.add("-Tplain-ext");
			commandLineEltList.add("-y");
			
			if(tool == TOOL_NEATO){
				String overlapOption = (String)gp.getProperty("overlapOption");
				if(overlapOption != null){
					commandLineEltList.add("-Goverlap=" + overlapOption);
				}
				Double edgeWeight = (Double)gp.getProperty("edgeWeight");
				if(edgeWeight != null){
					commandLineEltList.add("-Eweight=" + edgeWeight.toString());
				}
				Double edgeLength = (Double)gp.getProperty("edgeLength");
				if(edgeLength != null){
					commandLineEltList.add("-Elen=" + edgeLength.toString());
				}
			}
			String[] commandline = (String[])commandLineEltList.toArray(new String[0]);
			Process p = Runtime.getRuntime().exec(commandline);
			
			NativeProcess np = new NativeProcess(p, toolInput);
			np.start();
			np.waitForCompletion();
		
			String outputData = np.getStdout().trim();
			return outputData;
		}
		
		public GraphLayout processDotOutput(String dotOutput){
			try{
				GraphLayout gl = new GraphLayout();
				
				BufferedReader br = new BufferedReader(new StringReader(dotOutput));
				
				double scale = gp.getScale();
				
				while(true){
					String line = br.readLine().trim();
					if(line.startsWith("stop")){
						return gl;
					}
					else if(line.startsWith("node")){
						StringTokenizer tok = new StringTokenizer(line);
						String nodeToken = tok.nextToken();
						String eltToken = tok.nextToken();
						String xToken = tok.nextToken();
						String yToken = tok.nextToken();
						String widthToken = tok.nextToken();
						String heightToken = tok.nextToken();
						
						GraphLayout.Node node = new GraphLayout.Node();
						node.setNodeId(getId(eltToken));
						
						double xd = Double.parseDouble(xToken);
						double yd = Double.parseDouble(yToken);
						double widthd = Double.parseDouble(widthToken);
						double heightd = Double.parseDouble(heightToken);
	
						//We want a bounds rectangle, (xd,yd) is the CENTER
						//of the box, so we have to offset it by half the width
						//and half the height to get the UL coordinate.
						
						xd -= (widthd / 2.0d);
						yd -= (heightd / 2.0d);

						xd *= scale;
						int x = (int)Math.round(xd);
						
						yd *= scale;
						int y = (int)Math.round(yd);
						
						widthd *= scale;
						int width = (int)Math.round(widthd);
						
						heightd *= scale;
						int height = (int)Math.round(heightd);
						
						java.awt.Rectangle bounds = new java.awt.Rectangle(x, y, width, height);
						node.setBounds(bounds);
						
						//Add the interface position lists
						//System.out.println("glc2 trying to get nodeId");
						List[] interfacePositionArray = (List[])interfacePositions.get(node.getNodeId());
						//System.out.println("node.nodeID = " + node.getNodeId());
						//System.out.println("interfacePositionArray =" + interfacePositionArray);
						if(interfacePositionArray != null){
							List northList = interfacePositionArray[0];
							List southList = interfacePositionArray[1];
							List eastList = interfacePositionArray[2];
							List westList = interfacePositionArray[3];
							List unknownList = interfacePositionArray[4];
							
							for(Iterator it = northList.iterator(); it.hasNext(); ){
								node.addPort(new GraphLayout.CompassPort((String)it.next(), GraphLayout.CompassPort.NORTH)); 
							}
							for(Iterator it = southList.iterator(); it.hasNext(); ){
								node.addPort(new GraphLayout.CompassPort((String)it.next(), GraphLayout.CompassPort.SOUTH)); 
							}
							for(Iterator it = eastList.iterator(); it.hasNext(); ){
								node.addPort(new GraphLayout.CompassPort((String)it.next(), GraphLayout.CompassPort.EAST)); 
							}
							for(Iterator it = westList.iterator(); it.hasNext(); ){
								node.addPort(new GraphLayout.CompassPort((String)it.next(), GraphLayout.CompassPort.WEST)); 
							}
							for(Iterator it = unknownList.iterator(); it.hasNext(); ){
								node.addPort(new GraphLayout.PlainPort((String)it.next())); 
							}
						}
						
						gl.addNode(node);
					}
					else if(line.startsWith("edge")){
						StringTokenizer tok = new StringTokenizer(line);
						String edgeToken = tok.nextToken();
						String endpt1Token = tok.nextToken();
						String endpt2Token = tok.nextToken();
						String numPointsToken = tok.nextToken();
						int numPoints = Integer.parseInt(numPointsToken);
						
						String[] xTokens = new String[numPoints];
						String[] yTokens = new String[numPoints];
						
						for(int i = 0; i < numPoints; i++){
							xTokens[i] = tok.nextToken();
							yTokens[i] = tok.nextToken();
						}
						
						String edgeIdToken = tok.nextToken();
						
						GraphLayout.Edge edge = new GraphLayout.Edge();
						
						edge.setEdgeId(getId(edgeIdToken));
						
						int colon1Index = endpt1Token.indexOf(":");
						String node1Token = null;
						String port1Token = null;
						//Workaround for a stupid dot bug in 1.15 or 1.16+
						//that causes it to leave out the colon between the
						//edges and endpoints...
						if(colon1Index != -1){
							node1Token = endpt1Token.substring(0, colon1Index);
							port1Token = endpt1Token.substring(colon1Index + 1);
						}
						else{
							int secondEIndex = endpt1Token.indexOf("e", 1);
							node1Token = endpt1Token.substring(0, secondEIndex);
							port1Token = endpt1Token.substring(secondEIndex);
						}
						
						int colon2Index = endpt2Token.indexOf(":");
						String node2Token = null;
						String port2Token = null;
						//Workaround for a stupid dot bug in 1.15 or 1.16+
						//that causes it to leave out the colon between the
						//edges and endpoints...
						if(colon2Index != -1){
							node2Token = endpt2Token.substring(0, colon2Index);
							port2Token = endpt2Token.substring(colon2Index + 1);
						}
						else{
							int secondEIndex = endpt2Token.indexOf("e", 1);
							node2Token = endpt2Token.substring(0, secondEIndex);
							port2Token = endpt2Token.substring(secondEIndex);
						}
						edge.setEndpoint1(getId(node1Token), getId(port1Token));
						edge.setEndpoint2(getId(node2Token), getId(port2Token));
												
						for(int i = 0; i < numPoints; i++){
							double xd = Double.parseDouble(xTokens[i]);
							xd *= scale;
							int x = (int)Math.round(xd);
							
							double yd = Double.parseDouble(yTokens[i]);
							yd *= scale;
							int y = (int)Math.round(yd);
							
							java.awt.Point p = new java.awt.Point(x, y); 
							edge.addPoint(p);
						}
						
						gl.addEdge(edge);
					}					
				}
			}
			catch(IOException e){
				throw new RuntimeException("This shouldn't happen.");
			}
		}
	}
}
