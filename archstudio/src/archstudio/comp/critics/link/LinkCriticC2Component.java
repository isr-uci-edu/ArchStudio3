package archstudio.comp.critics.link;


import archstudio.critics.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class LinkCriticC2Component extends AbstractCritic{

	//The link has !=2 endpoints
	public static final Identifier ISSUE_INVALID_LINK_ENDPOINT_SET =
		new SimpleIdentifier("ISSUE_INVALID_LINK_ENDPOINT_SET");
	
	//The link is invalid (i.e. has no xlink type
	public static final Identifier ISSUE_INVALID_LINK_ENDPOINT =
		new SimpleIdentifier("ISSUE_INVALID_LINK_ENDPOINT");
	
	//The link has an XLink for an endpoint that can't be resolved
	public static final Identifier ISSUE_BROKEN_LINK_ENDPOINT =
		new SimpleIdentifier("ISSUE_BROKEN_LINK_ENDPOINT");

	//Point has no anchorOnInterface
	public static final Identifier ISSUE_POINT_HAS_NO_ANCHOR =
		new SimpleIdentifier("ISSUE_POINT_HAS_NO_ANCHOR");
	
	//The link has a valid XLink as an endpoint, but the XLink
	//points at something that's not a component or connector 
	//interface.
	public static final Identifier ISSUE_ENDPOINT_TARGET_NOT_INTERFACE =
		new SimpleIdentifier("ISSUE_ENDPOINT_TARGET_NOT_INTERFACE");
	
	public LinkCriticC2Component(Identifier id){
		super(id);
	}
	
	public Identifier[] getDependencies(){
		return new Identifier[]{};
	}
	
	protected CriticIssue getInvalidLinkEndpointSetIssue(ObjRef xArchRef, ObjRef linkRef, String linkID){
		if(linkID == null){
			linkID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_INVALID_LINK_ENDPOINT_SET,
			"Link Has Invalid Set of Endpoints",
			"Links between bricks in an architecture should have " +
			"exactly two endpoints for the link to be valid.",
			"Link with ID " + linkID + " in architecture " +
			"should have exactly two endpoints.",
			xArchRef,
			new ObjRef[]{linkRef}
		);
	}
	
	protected CriticIssue getInvalidLinkEndpointIssue(ObjRef xArchRef, ObjRef pointRef, String linkID){
		if(linkID == null){
			linkID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_INVALID_LINK_ENDPOINT,
			"Link Has Invalid Endpoint XLink",
			"The XLink on a brick-to-brick link's endpoint is invalid.",
			"Link with ID " + linkID + " in architecture " +
			"has an invalid endpoint XLink.",
			xArchRef,
			new ObjRef[]{pointRef}
		);
	}
	
	protected CriticIssue getPointHasNoAnchorIssue(ObjRef xArchRef, ObjRef pointRef, String linkID){
		if(linkID == null){
			linkID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("incomplete_architecture"),
			"Incomplete Architecture",
			ISSUE_POINT_HAS_NO_ANCHOR,
			"Link Point has no AnchorOnInterface",
			"One of the brick-to-brick links in the architecture has an " +
			"endpoint specified, but that endpoint has no anchor link to " +
			"a component or connector interface.",
			"Link with ID " + linkID + " in architecture " +
			"has an incomplete endpoint link.",
			xArchRef,
			new ObjRef[]{pointRef}
		);
	}
	
	protected CriticIssue getBrokenLinkEndpointIssue(ObjRef xArchRef, ObjRef pointRef, String linkID){
		if(linkID == null){
			linkID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("broken_link"),
			"Broken Link",
			ISSUE_BROKEN_LINK_ENDPOINT,
			"Broken Link EndpointThing",
			"The XML link of one endpoint of a brick-to-brick link " +
			"in the design time description of an architecture is " +
			"broken or cannot be resolved.",
			"Link with ID " + linkID + " in architecture " +
			"has a broken endpoint link.",
			xArchRef,
			new ObjRef[]{pointRef}
		);
	}
	
	protected CriticIssue getEndpointTargetNotInterfaceIssue(ObjRef xArchRef, ObjRef pointRef, String linkID){
		if(linkID == null){
			linkID = "[unknown]";
		}
		return new CriticIssue(
			this.getIdentifier(),
			new SimpleIdentifier("topology"),
			"Topology",
			ISSUE_ENDPOINT_TARGET_NOT_INTERFACE,
			"Link EndpointThing Target not an Interface",
			"The target of an architecture link endpoint " + 
			"is not a component or connector interface.",
			"Link with ID " + linkID + " in architecture " +
			"has an endpoint link that points to something other than a component " + 
			"or connector interface.",
			xArchRef,
			new ObjRef[]{pointRef}
		);
	}

	public String getDescription(){
		return "The link critic checks to see if each brick-to-brick Link in a xADL 2.0 " +
			"document has exactly two endpoints, whether those endpoints are valid (i.e. not broken) " + 
			"XLinks, and whether those XLinks point to interfaces on either connectors or components.";
	}
	
	private XArchFlatQueryInterface runBulkQuery(ObjRef xArchRef){
		//ObjRef xArchRef = xarch.getXArch(structureRef);
		XArchBulkQuery q = getBulkQuery(xArchRef);
		XArchBulkQueryResults qr = xarch.bulkQuery(q);
		return new XArchBulkQueryResultProxy(xarch, qr);
	}
	
	public static XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/component*/id");
		q.addQueryPath("archStructure*/component*/description/value");
		q.addQueryPath("archStructure*/component*/interface*");
		q.addQueryPath("archStructure*/component*/interface*/id");
		q.addQueryPath("archStructure*/component*/interface*/description/value");
		q.addQueryPath("archStructure*/component*/interface*/direction/value");

		q.addQueryPath("archStructure*/connector*/id");
		q.addQueryPath("archStructure*/connector*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*");
		q.addQueryPath("archStructure*/connector*/interface*/id");
		q.addQueryPath("archStructure*/connector*/interface*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*/direction/value");

		q.addQueryPath("archStructure*/link*/id");
		q.addQueryPath("archStructure*/link*/description/value");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/type");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/href");

		return q;
	}
	
	protected void checkDocument(ObjRef xArchRef){
		if(!isActive()) return;
		setBusy(true);
		//ObjRef xArchRef = xarch.getOpenXArch(architectureURI);
		
		XArchFlatQueryInterface xarchbulk = runBulkQuery(xArchRef);
		
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef[] archStructureRefs = xarchbulk.getAllElements(typesContextRef, "ArchStructure", xArchRef);
		
		ArrayList issueList = new ArrayList();
		
		for(int j = 0; j < archStructureRefs.length; j++){
			ObjRef[] linkRefs = xarchbulk.getAll(archStructureRefs[j], "Link");
			
			for(int i = 0; i < linkRefs.length; i++){
				ObjRef linkRef = linkRefs[i];
				String linkID = (String)xarchbulk.get(linkRef, "Id");
				
				ObjRef[] pointRefs = xarchbulk.getAll(linkRef, "Point");
				if(pointRefs.length != 2){
					issueList.add(getInvalidLinkEndpointSetIssue(xArchRef, linkRef, linkID));
				}
				for(int p = 0; p < pointRefs.length; p++){
					ObjRef pointRef = pointRefs[p];
					ObjRef anchorRef = (ObjRef)xarchbulk.get(pointRef, "anchorOnInterface");
					if(anchorRef == null){
						issueList.add(getPointHasNoAnchorIssue(xArchRef, pointRef, linkID));
					}
					else{
						String xlinkType = (String)xarchbulk.get(anchorRef, "type");
						if((xlinkType == null) || (xlinkType.equals(""))){
							issueList.add(getInvalidLinkEndpointIssue(xArchRef, pointRef, linkID));
						}
						else if((xlinkType != null) && (xlinkType.equals("simple"))){
							//We can only process simple XLinks
							String href = (String)xarchbulk.get(anchorRef, "href");
							if(href == null){
								issueList.add(getInvalidLinkEndpointIssue(xArchRef, pointRef, linkID));
							}
							else{
								try{
									ObjRef targetRef = xarchbulk.resolveHref(xarchbulk.getXArch(pointRef), href);
									boolean wasInBulk = true;
									if(targetRef == null){
										//We couldn't resolve in the context of the bulk query;
										//let's try a full-doc resolve.
										wasInBulk = false;
										targetRef = xarch.resolveHref(xarchbulk.getXArch(pointRef), href);
									}
									if(targetRef == null){
										issueList.add(getBrokenLinkEndpointIssue(xArchRef, pointRef, linkID));
									}
									else{
										boolean isInterface = wasInBulk ? 
											xarchbulk.isInstanceOf(targetRef, "edu.uci.isr.xarch.types.IInterface") :
											xarch.isInstanceOf(targetRef, "edu.uci.isr.xarch.types.IInterface");
										if(!isInterface){
											issueList.add(getEndpointTargetNotInterfaceIssue(xArchRef, pointRef, linkID));
										}
										else{
											//It may be pointing to an unattached element
											if(!xarchbulk.isAttached(targetRef)){
												issueList.add(getBrokenLinkEndpointIssue(xArchRef, pointRef, linkID));
											}
										}
									}
								}
								catch(IllegalArgumentException iae){
									issueList.add(getBrokenLinkEndpointIssue(xArchRef, pointRef, linkID));
								}
							}
						}
					}
				}
			}
		}
		
		CriticIssue[] issues = (CriticIssue[])issueList.toArray(new CriticIssue[0]);
		replaceIssues(xArchRef, issues);
		setBusy(false);
	}

	protected boolean isPossiblyRelevant(ObjRef xArchRef, XArchFlatEvent evt){
		String name = evt.getTargetName();
		//System.out.println("name: " + name);
		if(name == null){
			return true;
		}
		
		XArchPath sourcePath = evt.getSourcePath();
		if(sourcePath != null){
			String sourcePathString = sourcePath.toTagsOnlyString();
			if(sourcePathString.startsWith("xArch/archStructure/link")){
				return true;
			}
			else if(sourcePathString.startsWith("xArch/archStructure/component")){
				return true;
			}
			else if(sourcePathString.startsWith("xArch/archStructure/connector")){
				return true;
			}
			else if(sourcePathString.startsWith("xArch/archTypes/componentType")){
				return true;
			}
			else if(sourcePathString.startsWith("xArch/archTypes/connectorType")){
				return true;
			}
			else if(sourcePathString.startsWith("xArch/archTypes/interfaceType")){
				return true;
			}
		}
		
		XArchPath targetPath = evt.getTargetPath();
		if(targetPath != null){
			String targetPathString = targetPath.toTagsOnlyString();
			if(targetPathString.startsWith("xArch/archStructure/link")){
				return true;
			}
			else if(targetPathString.startsWith("xArch/archStructure/component")){
				return true;
			}
			else if(targetPathString.startsWith("xArch/archStructure/connector")){
				return true;
			}
			else if(targetPathString.startsWith("xArch/archTypes/componentType")){
				return true;
			}
			else if(targetPathString.startsWith("xArch/archTypes/connectorType")){
				return true;
			}
			else if(targetPathString.startsWith("xArch/archTypes/interfaceType")){
				return true;
			}
		}
		
		return
			name.equals("archStructure") ||
			name.equals("xArch");
	}
	
	
	protected void handleXArchEvent(XArchFlatEvent evt){
		if(!isActive()) return;
		ObjRef source = (ObjRef)evt.getSource();
		ObjRef xArchRef = xarch.getXArch(source);
		String architectureURI = xarch.getXArchURI(xArchRef);
		//System.out.println("Name of this event is: " + evt.getTargetName());
		if(isPossiblyRelevant(xArchRef, evt)){
			checkDocument(xArchRef);
		}
	}
	
	protected void handleFileEvent(XArchFileEvent evt){
		if(!isActive()) return;
		int type = evt.getEventType();
		if((type == XArchFileEvent.XARCH_CREATED_EVENT) ||
			(type == XArchFileEvent.XARCH_OPENED_EVENT)){
			checkDocument(evt.getXArchRef());
		}
	}

}
	

