package archstudio.comp.visio;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.xarchutils.*;
import archstudio.invoke.*;

/**
 * Title:           VisioAgentComponent
 * Description:     Component for Visio communication
 * Copyright:       Copyright (c) 2000-2002
 * Organization:    ISR, UCI
 *
 * Handles the communication between Visio front end and the other part of ArchStudio
 * It forwards the messages between the two part.
 *
 * @author          Jie Ren <A HREF="mailto:jie@ics.uci.edu">jie@ics.uci.edu</A>
 * @version         2.0
 */

public class VisioAgentComponent extends AbstractC2DelegateBrick implements InvokableBrick, XArchFlatListener {

	public static final String PRODUCT_NAME = "Visio Agent Component";
	public static final String PRODUCT_VERSION = "2.0";

	protected XArchFlatInterface xArchADT;

    protected VisioAgentStub visioAgent = new VisioAgentStub();
    /**
     * Create a new <code>VisioAgentComponent</code> given the identifier
     * @param id identifier of the component
     */
    public VisioAgentComponent(Identifier id) {
        super(id);

        this.addMessageProcessor(new StateChangeMessageProcessor());
        xArchADT = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
        InvokeUtils.deployInvokableService(this, bottomIface, "Visio", "Visio Graphical Editor");
    }

    public void invoke(InvokeMessage im){
        if(im.getServiceName().equals("Visio")){
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")) {
                String url = im.getArchitectureURL();
                String[] command = {"visio32", "xadl.vst"};
                try {
                    Runtime.getRuntime().exec(command);
                }
                catch (IOException e) {
                    command[0] = "visio";
                    try {
                        Runtime.getRuntime().exec(command);
                    }
                    catch (IOException e2) {
                        System.out.println("Cannot start Visio. " +
                            "Please put the executable in PATH.");
                    }
                }
            }
        }
    }

    class StateChangeMessageProcessor implements MessageProcessor{
        public void handle(Message m){
            if (m.getSource().getBrickIdentifier().equals(new SimpleIdentifier("VisioConnector"))) {
                // message is from COM
                if(m instanceof NamedPropertyMessage){
                    NamedPropertyMessage npm = (NamedPropertyMessage)m;
                    String command = (String)npm.getParameter("Event");
                    String url = (String)npm.getParameter("URL");
                    NamedPropertyMessage response = new NamedPropertyMessage("Response");
                    if (url != null)
                        response.addParameter("URL", url);
                    if(npm.hasParameter("SynchronousID"))  // response id
                        response.addParameter("SynchronousID", (String)npm.getParameter("SynchronousID"));
                    /**todo turn to reflection */
                    if(command.equals("parseFromURL")) {
                        parseFromURL(url);
                    }
                    else if(command.equals("remvoeURL")) {
                        removeURL(url);
                    }
                    else if(command.equals("readxADLFile")) {
                        readxADLFile((String)npm.getParameter("Filename"));
                    }
                    else if(command.equals("getOpenxArchURLs")) {
                        String[] urls = getOpenXArchURLs();
                        for(int i = 0; i<urls.length; i++) {
                            response.addParameter("URL"+i, urls[i]);
                        }
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("writexADLFile")) {
                        writexADLFile((String)npm.getParameter("Filename"));
                    }
                    else if(command.equals("getComponentTypes")) {
                        response.addParameter("ComponentTypes", getComponentTypes(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getConnectorTypes")) {
                        response.addParameter("ConnectorTypes", getConnectorTypes(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getComponents")) {
                        response.addParameter("Components", getComponents(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getConnectors")) {
                        response.addParameter("Connectors", getConnectors(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getWelds")) {
                        response.addParameter("Welds", getWelds(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getGroups")) {
                        response.addParameter("Groups", getGroups(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getInterfaces")) {
                        response.addParameter("Interfaces",
                                getInterfaces(url, (String)npm.getParameter("Name")));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("getDescription")) {
                        response.addParameter("Description",
                                getDescription(url, (String)npm.getParameter("Name")));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("setDescription")) {
                        setDescription(url, (String)npm.getParameter("Name"),
                                       (String)npm.getParameter("Description"));
                    }
                    else if(command.equals("getArchitectureName")) {
                        response.addParameter("ArchitectureName",
                                getArchitectureName(url));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("setArchitectureName")) {
                        setArchitectureName(url, (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("compareArchitecture")) {
                        if(compareArchitecture(url,
                                (String)npm.getParameter("Name"),
                                (String)npm.getParameter("Components"),
                                (String)npm.getParameter("Connectors"),
                                (String)npm.getParameter("Welds"),
                                (String)npm.getParameter("Groups")))
                            response.addParameter("Identical", "True");
                        else
                            response.addParameter("Identical", "False");
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("addComponent")) {
                        addComponent(url, (String)npm.getParameter("Type"), (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("addConnector")) {
                        addConnector(url, (String)npm.getParameter("Type"), (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("removeComponent")) {
                        removeComponent(url, (String)npm.getParameter("Type"), (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("removeConnector")) {
                        removeConnector(url, (String)npm.getParameter("Type"), (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("renameComponent")) {
                        renameComponent(url, (String)npm.getParameter("Type"),
                                        (String)npm.getParameter("OldName"), (String)npm.getParameter("NewName"));
                    }
                    else if(command.equals("renameConnector")) {
                        renameConnector(url, (String)npm.getParameter("Type"),
                                        (String)npm.getParameter("OldName"), (String)npm.getParameter("NewName"));
                    }
                    else if(command.equals("weld")) {
                        weld(url, (String)npm.getParameter("Top"), (String)npm.getParameter("Bottom"));
                    }
                    else if(command.equals("weldInterfaces")) {
                        weldInterfaces(url, (String)npm.getParameter("TopInterface"),
                                       (String)npm.getParameter("BottomInterface"));
                    }
                    else if(command.equals("unweld")) {
                        unweld(url, (String)npm.getParameter("Top"), (String)npm.getParameter("Bottom"));
                    }
                    else if(command.equals("unweldInterfaces")) {
                        unweldInterfaces(url, (String)npm.getParameter("TopInterface"),
                                       (String)npm.getParameter("BottomInterface"));
                    }
                    else if(command.equals("addGroup")) {
                        addGroup(url, (String)npm.getParameter("Group"), (String)npm.getParameter("Members"));
                    }
                    else if(command.equals("removeGroup")) {
                        removeGroup(url, (String)npm.getParameter("Group"));
                    }
                    else if(command.equals("addComponents")) {
                        addComponents(url, (String)npm.getParameter("Components"));
                    }
                    else if(command.equals("addConnectors")) {
                        addConnectors(url, (String)npm.getParameter("Connectors"));
                    }
                    else if(command.equals("addWelds")) {
                        addWelds(url, (String)npm.getParameter("Welds"));
                    }
                    else if(command.equals("addGroups")) {
                        addWelds(url, (String)npm.getParameter("Welds"));
                    }
                    else if(command.equals("getAdders")) {
                        response.addParameter("Adders", getAdders(url, (String)npm.getParameter("Name")));
                        sendMessageToCOM(response);
                    }
                    else if(command.equals("addChild")) {
                        addChild(url, (String)npm.getParameter("Name"), (String)npm.getParameter("Type"));
                    }
                    else if(command.equals("createStructure")) {
                        createStructure(url, (String)npm.getParameter("Name"), (String)npm.getParameter("Description"));
                    }
                    else if(command.equals("setStructure")) {
                        setStructure(url, (String)npm.getParameter("Name"));
                    }
                    else if(command.equals("createInterfaceType")) {
                        createInterfaceType(url, (String)npm.getParameter("Name"), (String)npm.getParameter("Description"));
                    }
                    else if(command.equals("createComponentTypes")) {
                        createComponentType(url,
                                (String)npm.getParameter("Name"),
                                (String)npm.getParameter("Description"),
                                (String)npm.getParameter("Signatures"),
                                (String)npm.getParameter("Subarchitecture"));
                    }
                    else if(command.equals("createConnectorTypes")) {
                        createConnectorType(url,
                                (String)npm.getParameter("Name"),
                                (String)npm.getParameter("Description"),
                                (String)npm.getParameter("Signatures"),
                                (String)npm.getParameter("Subarchitecture"));
                    }
                }
            }
            else {
                // message is from ArchStudio
                if(m instanceof NamedPropertyMessage){
                    NamedPropertyMessage npm = (NamedPropertyMessage)m;
                    try{
                        if(npm.hasParameter("stateChangeMessage"))
                            if(npm.getBooleanParameter("stateChangeMessage")){
                                Object evtObject = npm.getParameter("paramValue0");

                                if(evtObject instanceof XArchFlatEvent){
                                    XArchFlatEvent evt = (XArchFlatEvent)evtObject;
                                    handleXArchFlatEvent(evt);
                                }
                                else if(evtObject instanceof XArchFileEvent){
                                    XArchFileEvent evt = (XArchFileEvent)evtObject;
                                    handleXArchFileEvent(evt);
                                }
                                return;
                            }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return;
            }
        }
    }

    public void handleXArchFlatEvent(XArchFlatEvent evt) {
        int srcType = evt.getSourceType();
        if((srcType == XArchFlatEvent.ATTRIBUTE_CHANGED) || (srcType == XArchFlatEvent.SIMPLE_TYPE_VALUE_CHANGED)){
        }
        else if(srcType == XArchFlatEvent.ELEMENT_CHANGED){
            int eventType = evt.getEventType();
            ObjRef refSource = evt.getSource();
            ObjRef refTarget = (ObjRef)evt.getTarget();
            String targetName = evt.getTargetName();
            String url;
            try {
              url = getXArchURL(xArchADT.getXArch(refSource));
            }
            catch (NoSuchObjectException e) {
              // hack so we are not bothered by (badly) timed events after elements are removed
              return;
            }

            VisioAgentWorker worker = getWorker(url);
            if ( worker == null )   // no one is interested
                return;

            try {
                if (targetName.equals("component")) {
                    String type = worker.getHrefId(refTarget, "Type");
                    String id = (String)xArchADT.get(refTarget, "Id");
                    if (eventType == XArchFlatEvent.ADD_EVENT)
                        visioAgent.ComponentAdded(url, type, id);
                    else if (eventType == XArchFlatEvent.REMOVE_EVENT)
                        visioAgent.ComponentDeleted(url, type, id);
                }
                else if (targetName.equals("connector")) {
                    String type = worker.getHrefId(refTarget, "Type");
                    String id = (String)xArchADT.get(refTarget, "Id");
                    if (eventType == XArchFlatEvent.ADD_EVENT)
                        visioAgent.ConnectorAdded(url, type, id);
                    else if (eventType == XArchFlatEvent.REMOVE_EVENT)
                        visioAgent.ConnectorDeleted(url, type, id);
                }
                else if (targetName.equals("link")) {
                    // link might point to non-existent elements
                    ObjRef[] points = xArchADT.getAll(refTarget, "Point");
                    String top = "", topInterface = "", bottom = "", bottomInterface = "";
                    for (int j = 0; j<points.length; j++) { // link's points
                        String iid = worker.getHrefId(points[j], "AnchorOnInterface");
                        String brick = (String)xArchADT.get(xArchADT.getParent(xArchADT.getByID(worker.refIXArch, iid)), "Id");
                        if (top.equals("")) {
                            top = brick;
                            topInterface = iid;
                        }
                        else {
                            bottom = brick;
                            bottomInterface = iid;
                        }
                    }
                    if (eventType == XArchFlatEvent.ADD_EVENT)
                        visioAgent.InterfaceWelded(url, top, topInterface, bottom, bottomInterface);
                    else if (eventType == XArchFlatEvent.REMOVE_EVENT)
                        visioAgent.InterfaceUnwelded(url, top, topInterface, bottom, bottomInterface);
                }
                else if (targetName.equals("group")) {
                    String group = (String)xArchADT.get(refTarget, "Id");
                    if (eventType == XArchFlatEvent.ADD_EVENT) {
                        ObjRef[] members = xArchADT.getAll(refTarget, "Member");
                        String g = "";
                        for (int j = 0; j<members.length; j++) {
                            g += (worker.getHrefId(members[j]) + ",");
                        }
                        visioAgent.GroupAdded(url, group, g);
                    }
                    else if (eventType == XArchFlatEvent.REMOVE_EVENT)
                        visioAgent.GroupDeleted(url, group);
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }

    }

    class VisioAgentStub {
        /**
         * A component is added
         * @param url the url to which this function applies
         * @param type type of component
         * @param name id of component
         */
        public void ComponentAdded(String url, String type, String name) {
            NamedPropertyMessage m = new NamedPropertyMessage("ComponentAdded");
            m.addParameter("Event", "ComponentAdded");
            m.addParameter("URL", url);
            m.addParameter("Type", type);
            m.addParameter("Name", name);
            sendMessageToCOM(m);
        }

        /**
         * A connector is added
         * @param url the url to which this function applies
         * @param type type of connector
         * @param name id of connector
         */
        public void ConnectorAdded(String url, String type, String name) {
            NamedPropertyMessage m = new NamedPropertyMessage("ConnectorAdded");
            m.addParameter("Event", "ConnectorAdded");
            m.addParameter("URL", url);
            m.addParameter("Type", type);
            m.addParameter("Name", name);
            sendMessageToCOM(m);
        }

        /**
         * A component is deleted
         * @param url the url to which this function applies
         * @param type type of component
         * @param name id of component
         */
        public void ComponentDeleted(String url, String type, String name) {
            NamedPropertyMessage m = new NamedPropertyMessage("ComponentDeleted");
            m.addParameter("Event", "ComponentDeleted");
            m.addParameter("URL", url);
            m.addParameter("Type", type);
            m.addParameter("Name", name);
            sendMessageToCOM(m);
        }

        /**
         * A connector is deleted
         * @param url the url to which this function applies
         * @param type type of connector
         * @param name id of connector
         */
        public void ConnectorDeleted(String url, String type, String name) {
            NamedPropertyMessage m = new NamedPropertyMessage("ConnectorDeleted");
            m.addParameter("Event", "ConnectorDeleted");
            m.addParameter("URL", url);
            m.addParameter("Type", type);
            m.addParameter("Name", name);
            sendMessageToCOM(m);
        }

        /**
         * A pair of instnaces are welded
         * @param url the url to which this function applies
         * @param top id of "top" instance
         * @param bottom id of "bottom" instance
         */
        public void Welded(String url, String top, String bottom) {
            NamedPropertyMessage m = new NamedPropertyMessage("Welded");
            m.addParameter("Event", "Welded");
            m.addParameter("URL", url);
            m.addParameter("Top", top);
            m.addParameter("Bottom", bottom);
            sendMessageToCOM(m);
        }

        /**
         * A pair of instnaces are unwelded
         * @param url the url to which this function applies
         * @param top id of "top" instance
         * @param bottom id of "bottom" instance
         */
        public void Unwelded(String url, String top, String bottom) {
            NamedPropertyMessage m = new NamedPropertyMessage("Unwelded");
            m.addParameter("Event", "Unwelded");
            m.addParameter("URL", url);
            m.addParameter("Top", top);
            m.addParameter("Bottom", bottom);
            sendMessageToCOM(m);
        }

        /**
         * A pair of instnaces are welded
         * @param url the url to which this function applies
         * @param top id of "top" instance
         * @param topInterface id of top interface
         * @param bottom id of "bottom" instance
         * @param bottomInterface id of bottom interface
         */
        public void InterfaceWelded(String url, String top, String topInterface, String bottom, String bottomInterface) {
            NamedPropertyMessage m = new NamedPropertyMessage("InterfaceWelded");
            m.addParameter("Event", "InterfaceWelded");
            m.addParameter("URL", url);
            m.addParameter("Top", top);
            m.addParameter("TopInterface", topInterface);
            m.addParameter("Bottom", bottom);
            m.addParameter("BottomInterface", bottomInterface);
            sendMessageToCOM(m);
        }

        /**
         * A pair of instnaces are unwelded
         * @param url the url to which this function applies
         * @param top id of "top" instance
         * @param topInterface id of top interface
         * @param bottom id of "bottom" instance
         * @param bottomInterface id of bottom interface
         */
        public void InterfaceUnwelded(String url, String top, String topInterface, String bottom, String bottomInterface) {
            NamedPropertyMessage m = new NamedPropertyMessage("InterfaceUnwelded");
            m.addParameter("Event", "InterfaceUnwelded");
            m.addParameter("URL", url);
            m.addParameter("Top", top);
            m.addParameter("TopInterface", topInterface);
            m.addParameter("Bottom", bottom);
            m.addParameter("BottomInterface", bottomInterface);
            sendMessageToCOM(m);
        }

        /**
         * A group is added
         * @param url the url to which this function applies
         * @param group id of group
         * @param members ids of members, each id is followed by a ","
         */
        public void GroupAdded(String url, String group, String members) {
            NamedPropertyMessage m = new NamedPropertyMessage("GroupAdded");
            m.addParameter("Event", "GroupAdded");
            m.addParameter("URL", url);
            m.addParameter("Group", group);
            m.addParameter("Members", members);
            sendMessageToCOM(m);
        }

        /**
         * A group is deleted
         * @param url the url to which this function applies
         * @param group id of group
         */
        public void GroupDeleted(String url, String group) {
            NamedPropertyMessage m = new NamedPropertyMessage("GroupDeleted");
            m.addParameter("Event", "GroupDeleted");
            m.addParameter("URL", url);
            m.addParameter("Group", group);
            sendMessageToCOM(m);
        }

        /**
         * A message is sent from one instace to another welded instance
         * @param url the url to which this function applies
         * @param sender id of sender
         * @param receiver id of receiver
         * @param message message sent
         */
        public void MessageSent(String url, String sender, String receiver, String message) {
        }

    }

    private void sendMessageToCOM(NamedPropertyMessage m) {
        sendToAll(m, getInterface(c2.legacy.AbstractC2Brick.BOTTOM_INTERFACE_ID));
    }

    private String getXArchURL(ObjRef xarch) {
        String[] urls = xArchADT.getOpenXArchURIs();
        for ( int i = 0; i<urls.length; i++ ) {
            if (xarch.equals(xArchADT.getOpenXArch(urls[i])))
                return urls[i];
        }
        return null;
    }

    private String getType(ObjRef obj) {
        String dn = xArchADT.getType(obj);
        if(dn.lastIndexOf(".") != -1){
            dn = dn.substring(dn.lastIndexOf(".") + 1);
        }
        if(dn.endsWith("Impl")){
            dn = dn.substring(0, dn.length() - 4);
        }
        return dn;
    }

    public void handleXArchFileEvent(XArchFileEvent evt) {
    }

    // map from a url to the worker
    private Map workers = new HashMap();

    private VisioAgentWorker getWorker(String url) {
        return (VisioAgentWorker)workers.get(url);
    }

    public void parseFromURL(String url) {
        if ( workers.get(url)==null ) {
            workers.put(url, new VisioAgentWorker(url));
        }
    }

    public void removeURL(String url) {
        if ( workers.get(url)!=null ) {
            workers.remove(url);
        }
    }

    public void readxADLFile(String filename)  {
        if ( workers.get(filename)==null ) {
            workers.put(filename, new VisioAgentWorker(filename, ""));
        }
    }

    public String[] getOpenXArchURLs()  {
        String[] urls = null;
        try {
            urls = xArchADT.getOpenXArchURIs();
        }
        catch (Exception e) {
            System.err.println("VisioAgent exception: " +
                               e.getMessage());
            e.printStackTrace();
        }
        return urls;
    }

    public void writexADLFile(String filename)  {
    }

    public String getComponentTypes(String url)  {
        return getWorker(url).getComponentTypes();
    }

    public String getConnectorTypes(String url)  {
        return getWorker(url).getConnectorTypes();
    }

    public String getComponents(String url)  {
        return getWorker(url).getComponents();
    }

    public String getConnectors(String url)  {
        return getWorker(url).getConnectors();
    }

    public String getWelds(String url)  {
        return getWorker(url).getWelds();
    }

    public String getGroups(String url)  {
        return getWorker(url).getGroups();
    }

    public String getInterfaces(String url, String idBrick)  {
        return getWorker(url).getInterfaces(idBrick);
    }

    public String getDescription(String url, String id)  {
        return getWorker(url).getDescription(id);
    }

    public void setDescription(String url, String id, String description)  {
        getWorker(url).setDescription(id, description);
    }

    public String getArchitectureName(String url)  {
        return getWorker(url).getArchitectureName();
    }

    public void setArchitectureName(String url, String name)  {
        getWorker(url).setArchitectureName(name);
    }

    public boolean compareArchitecture(String url, String architectureName,
                        String components, String connectors, String welds, String groups)  {
        return getWorker(url).compareArchitecture(architectureName, components, connectors, welds, groups);
    }

    public void addComponent(String url, String type, String name)  {
        getWorker(url).addComponent(type, name);
    }

    public void addConnector(String url, String type, String name)  {
        getWorker(url).addConnector(type, name);
    }

    public void removeComponent(String url, String type, String name)  {
        getWorker(url).removeComponent(type, name);
    }

    public void removeConnector(String url, String type, String name)  {
        getWorker(url).removeConnector(type, name);
    }

    public void renameComponent(String url, String type, String oldName, String newName)  {
        getWorker(url).renameComponent(type, oldName, newName);
    }

    public void renameConnector(String url, String type, String oldName, String newName)  {
        getWorker(url).renameConnector(type, oldName, newName);
    }

    public void weld(String url, String top, String bottom)  {
        getWorker(url).weld(top, bottom);
    }

    public void weldInterfaces(String url, String idTopInterface, String idBottomInterface)  {
        getWorker(url).weldInterfaces(idTopInterface, idBottomInterface);
    }

    public void unweld(String url, String top, String bottom)  {
        getWorker(url).unweld(top, bottom);
    }

    public void unweldInterfaces(String url, String idTopInterface, String idBottomInterface)  {
        getWorker(url).unweldInterfaces(idTopInterface, idBottomInterface);
    }

    public void addGroup(String url, String group, String members)  {
        getWorker(url).addGroup(group, members);
    }

    public void removeGroup(String url, String group)  {
        getWorker(url).removeGroup(group);
    }

    public void addComponents(String url, String components)  {
        getWorker(url).addComponents(components);
    }

    public void addConnectors(String url, String connectors)  {
        getWorker(url).addConnectors(connectors);
    }

    public void addWelds(String url, String welds)  {
        getWorker(url).addWelds(welds);
    }

    public void addGroups(String url, String groups)  {
        getWorker(url).addGroups(groups);
    }

    public String getAdders(String url, String brick)  {
        return getWorker(url).getAdders(brick);
    }

    public void addChild(String url, String brick, String typeOfChild)  {
        getWorker(url).addChild(brick, typeOfChild);
    }

    public void createStructure(String url, String id, String description)  {
        getWorker(url).createStructure(id, description);
    }

    public void setStructure(String url, String id)  {
        getWorker(url).setStructure(id);
    }

    public void createInterfaceType(String url, String id, String description)  {
        getWorker(url).createInterfaceType(id, description);
    }

    public void createComponentType(String url, String id, String description, String signatures, String subarch)  {
        getWorker(url).createBrickType("Component", id, description, signatures, subarch);
    }

    public void createConnectorType(String url, String id, String description, String signatures, String subarch)  {
        getWorker(url).createBrickType("Connector", id, description, signatures, subarch);
    }

    /**
     * Worker for one url
     */
    class VisioAgentWorker {
        ObjRef refIXArch, refTypesContext, refIXArchTypes, refIXArchStructure, refIXArchitecture;
        VisioAgentWorker(String url) {
            try {
                refIXArch = xArchADT.parseFromURL(url);
                initialize();
            }
            catch (Exception e) {
                System.err.println("VisioAgentComponent exception: " +
                                   e.getMessage());
                e.printStackTrace();
            }
        }

        VisioAgentWorker(String filename, String overload) {
            try {
                refIXArch = xArchADT.parseFromFile(filename);
                initialize();
            }
            catch (Exception e) {
                System.err.println("VisioAgentComponent exception: " +
                                   e.getMessage());
                e.printStackTrace();
            }
        }

        private void initialize() {
            refTypesContext = xArchADT.createContext(refIXArch, "types");
            refIXArchTypes = xArchADT.getElement(refTypesContext, "ArchTypes", refIXArch);
            if (refIXArchTypes==null) {
                refIXArchTypes = xArchADT.createElement(refTypesContext, "ArchTypes");
                xArchADT.add(refIXArch, "Object", refIXArchTypes);
            }
            //refIXArchStructure = xArchADT.getElement(refTypesContext, "ArchStructure", refIXArch);
            refIXArchStructure = getPrimaryStructure();
            if (refIXArchStructure==null) {
                refIXArchStructure = xArchADT.createElement(refTypesContext, "ArchStructure");
                xArchADT.add(refIXArch, "Object", refIXArchStructure);
            }
            refIXArchitecture = refIXArchStructure;
        }

        void writexADLFile(String filename) {
        }

        private ObjRef getPrimaryStructure() {
            // support multiple structures, one is primary, others are subarchitectures for types
            ObjRef[] structures = xArchADT.getAllElements(refTypesContext, "ArchStructure", refIXArch);
            for(int i = 0; i<structures.length; i++) {
                ObjRef refStructure = structures[i];
                if (!isSubarchitecture(refStructure)) {
                    // the first that is not sub architecture of some type
                    return refStructure;
                }
            }
            return null; // should not happen
        }

        boolean isSubarchitecture(ObjRef refStructure) {
            ObjRef[] componentTypes = xArchADT.getAll(refIXArchTypes, "ComponentType");
            ObjRef[] connectorTypes = xArchADT.getAll(refIXArchTypes, "ConnectorType");
            ObjRef[] types = new ObjRef[componentTypes.length+connectorTypes.length];
            for(int i = 0; i<componentTypes.length; i++)
                types[i] = componentTypes[i];
            for(int i = 0; i<connectorTypes.length; i++)
                types[i+componentTypes.length] = connectorTypes[i];
            for(int i = 0; i<types.length; i++) {
                ObjRef refSubArch = (ObjRef)xArchADT.get(types[i], "SubArchitecture");
                if (refSubArch!=null) {
                    ObjRef refToCompare = (ObjRef)xArchADT.get(refSubArch, "ArchStructure");
                    String toCompare = getHrefId(refToCompare);
                    String structure = (String)xArchADT.get(refStructure, "Id");
                    if(structure.equals(toCompare))
                        return true;
                }
            }
            return false;
        }

        String getComponentTypes() {
            return getBrickTypes("Component");
        }

        String getConnectorTypes()  {
            return getBrickTypes("Connector");
        }

        String getBrickTypes(String kind) {
            ObjRef[] brickTypes = xArchADT.getAll(refIXArchTypes, kind+"Type");
            String[] bricks = new String[brickTypes.length];
            Map sd = new HashMap();
            for(int i = 0; i<brickTypes.length; i++) {
                bricks[i] = (String)xArchADT.get(brickTypes[i], "Id");

                String description = "";
                ObjRef refDescription = (ObjRef)xArchADT.get(brickTypes[i], "Description");
                if (refDescription!=null) {
                    description = (String)xArchADT.get(refDescription, "Value");
                }

                String subArch = "";
                ObjRef refSubArch = (ObjRef)xArchADT.get(brickTypes[i], "SubArchitecture");
                if (refSubArch != null) {
                    ObjRef refStructure = (ObjRef)xArchADT.get(refSubArch, "ArchStructure");
                    subArch = getHrefId(refStructure);
                }

                sd.put(bricks[i], description + "," + subArch + ",");
            }
            Arrays.sort(bricks);
            String s = "";
            for(int i = 0; i<brickTypes.length; i++) {
                s += bricks[i] + "," + sd.get(bricks[i]) + ";";
            }
            return s;
        }

        String getComponents() {
            return getBricks("Component");
        }

        String getConnectors() {
            return getBricks("Connector");
        }

        private String getBricks(String kind) {
            ObjRef[] bricks = xArchADT.getAll(refIXArchStructure, kind);
            String c = "";
            for(int i = 0; i<bricks.length; i++) {
                String id = (String)xArchADT.get(bricks[i], "Id");
                String type = getHrefId(bricks[i], "Type");
                c += (type + "," + id + ";");
            }
            return c;
        }

        String getHrefId(ObjRef refObj, String href) {
            ObjRef refHref = xArchADT.resolveHref(refIXArch, (String)xArchADT.get((ObjRef)xArchADT.get(refObj, href), "Href"));
            return (String)xArchADT.get(refHref, "Id");
        }

        String getHrefId(ObjRef refObj) {
            String t = (String)xArchADT.get(refObj, "Href");
            ObjRef refHref = xArchADT.resolveHref(refIXArch, (String)xArchADT.get(refObj, "Href"));
            return (String)xArchADT.get(refHref, "Id");
        }

        String getWelds() {
            // get bricks
            ObjRef[] components = xArchADT.getAll(refIXArchStructure, "Component");
            ObjRef[] connectors = xArchADT.getAll(refIXArchStructure, "Connector");
            ObjRef[] bricks = new ObjRef[components.length+connectors.length];
            for (int i = 0; i < components.length; i++) {
                bricks[i] = components[i];
            }
            for (int i = 0; i < connectors.length; i++) {
                bricks[i + components.length] = connectors[i];
            }

            ObjRef[] links = xArchADT.getAll(refIXArchStructure, "link");
            String w = "";
            for (int i = 0; i<links.length; i++) {  // all links
                ObjRef[] points = xArchADT.getAll(links[i], "Point");
                String top = "", topInterface = "", bottom = "", bottomInterface = "";
                for (int j = 0; j<points.length; j++) { // link's points
                    String iid = getHrefId(points[j], "AnchorOnInterface");
                    String brick = (String)xArchADT.get(xArchADT.getParent(xArchADT.getByID(refIXArch, iid)), "Id");
                    if (top.equals("")) {
                        top = brick;
                        topInterface = iid;
                    }
                    else {
                        bottom = brick;
                        bottomInterface = iid;
                    }
                }
                w += ( top + "," + topInterface + "," + bottomInterface + "," + bottom + ";" );
            }
            return w;
        }

        String getGroups() {
            ObjRef[] groups = xArchADT.getAll(refIXArchStructure, "Group");
            String g = "";
            for (int i = 0; i<groups.length; i++) {
                g += ((String)xArchADT.get(groups[i], "Id") + ",");
                ObjRef[] members = xArchADT.getAll(groups[i], "Member");
                for (int j = 0; j<members.length; j++) {
                    g += (getHrefId(members[j]) + ",");
                }
                g += ";";
            }
            return g;
        }

        String getInterfaces(String idBrick) {
            ObjRef[] refInterfaces = xArchADT.getAll(xArchADT.getByID(idBrick), "Interface");
            String interfaces = "";
            for ( int i = 0; i<refInterfaces.length; i++ ) {
                String id = (String)xArchADT.get(refInterfaces[i], "Id");
                String type = getHrefId(refInterfaces[i], "Type");
                String direction = "";
                ObjRef refDirection = (ObjRef)xArchADT.get(refInterfaces[i], "Direction");
                if (refDirection==null)
                    direction = "";
                else
                    direction = (String)xArchADT.get(refDirection, "Value");
                String description = "";
                ObjRef refDescription = (ObjRef)xArchADT.get(refInterfaces[i], "Description");
                if (refDescription==null)
                    description = "";
                else
                    description = (String)xArchADT.get(refDescription, "Value");
                interfaces += id + "," + type + "," + direction + "," + description + ";";
            }
            return interfaces;
        }

        String getDescription(String id) {
            ObjRef refDescription = (ObjRef)xArchADT.get(xArchADT.getByID(id), "Description");
            if (refDescription==null)
                return "";
            else
                return (String)xArchADT.get(refDescription, "Value");
        }

        void setDescription(String id, String description) {
            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", description);
            xArchADT.set(xArchADT.getByID(id), "Description", refDescription);
        }

        String getArchitectureName()  {
            String r = (String)xArchADT.get(refIXArchitecture, "Id");
            if (r==null)
                return "";
            else
                return r;
        }

        void setArchitectureName(String name) {
            xArchADT.set(refIXArchitecture, "Id", name);
        }

        void addComponent(String type, String name) {
            addBrick("Component", type, name);
        }

        void addConnector(String type, String name) {
            addBrick("Connector", type, name);
        }

        // rule of id in structure
        //      component, connector: whatever id
        //      interface on component/connector: I_BrickId_InterfaceTypeId
        //      link between interfaces: InterfaceId1-InterfaceId2
        private void addBrick(String kind, String type, String name) {
            ObjRef refBrick = xArchADT.create(refTypesContext, kind);
            xArchADT.set(refBrick, "Id", name);

            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", name);
            xArchADT.set(refBrick, "Description", refDescription);

            ObjRef refTypeLink = xArchADT.create(refTypesContext, "XMLLink");
            // to do: href handling
            xArchADT.set(refTypeLink, "Href", "#"+type);
            xArchADT.set(refBrick, "Type", refTypeLink);

            ObjRef refType = xArchADT.get(refIXArchTypes, kind + "Type", type);
            ObjRef[] refSignatures = xArchADT.getAll(refType, "Signature");
            ObjRef[] refInterfaces = new ObjRef[refSignatures.length];
            for(int i = 0; i<refSignatures.length; i++) {
                String direction = (String)xArchADT.get((ObjRef)xArchADT.get(refSignatures[i], "Direction"), "Value");
                String interfaceHref = (String)xArchADT.get((ObjRef)xArchADT.get(refSignatures[i], "Type"), "Href");
                String interfaceId = (String)xArchADT.get(xArchADT.resolveHref(refIXArch, interfaceHref), "Id");
                refInterfaces[i] = createInterface("I_" + name + "_" + interfaceId, direction,
                                    "the " + interfaceId + " interface of " + name, interfaceHref);
            }
            xArchADT.add(refBrick, "Interface", refInterfaces);

            xArchADT.add(refIXArchStructure, kind, refBrick);
        }

        ObjRef createInterface(String id, String direction, String description, String type) {
            ObjRef refInterface = xArchADT.create(refTypesContext, "Interface");
            xArchADT.set(refInterface, "Id", id);

            ObjRef refDirection = xArchADT.create(refTypesContext, "Direction");
            xArchADT.set(refDirection, "Value", direction);
            xArchADT.set(refInterface, "Direction", refDirection);

            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", description);
            xArchADT.set(refInterface, "Description", refDescription);

            ObjRef refType = xArchADT.create(refTypesContext, "XMLLink");
            xArchADT.set(refType, "Href", type);
            xArchADT.set(refInterface, "Type", refType);

            return refInterface;
        }

        void removeComponent(String type, String name) {
            removeBrick("Component", type, name);
        }

        void removeConnector(String type, String name) {
            removeBrick("Connector", type, name);
        }

        private void removeBrick(String kind, String type, String name) {
            ObjRef[] links = xArchADT.getAll(refIXArchStructure, "Link");
            for (int i = 0; i<links.length; i++) {  // all links
                boolean match = false;
                ObjRef[] points = xArchADT.getAll(links[i], "Point");
                for (int j = 0; j<points.length; j++) { // link's points
                    String iid = getHrefId(points[j], "AnchorOnInterface");
                    ObjRef iref = xArchADT.getByID(refIXArch, iid);
                    ObjRef oref = xArchADT.getParent(iref);
                    String brick = (String)xArchADT.get(oref, "Id");
                    //String brick = (String)xArchADT.get(xArchADT.getParent(xArchADT.getByID(refIXArch, iid)), "Id");
                    if (brick.equals(name)) {
                        match = true;
                        break;
                    }
                }
                if (match)
                    xArchADT.remove(xArchADT.getParent(links[i]), "Link", links[i]);
            }

            ObjRef refBrick = xArchADT.getByID(refIXArch, name);
            xArchADT.remove(xArchADT.getParent(refBrick), kind, refBrick);
        }

        void renameComponent(String type, String oldName, String newName) {
            renameBrick("Component", type, oldName, newName);
        }

        void renameConnector(String type, String oldName, String newName) {
            renameBrick("Connector", type, oldName, newName);
        }

        private void renameBrick(String kind, String type, String oldName, String newName) {
            String  iid = "", newIID = "";
            int     l = -1;

            ObjRef[] links = xArchADT.getAll(refIXArchStructure, "Link");
            for (int i = 0; i<links.length; i++) {  // all links
                boolean match = false;
                ObjRef[] points = xArchADT.getAll(links[i], "Point");
                for (int j = 0; j<points.length; j++) { // link's points
                    iid = getHrefId(points[j], "AnchorOnInterface");
                    ObjRef iref = xArchADT.getByID(refIXArch, iid);
                    ObjRef oref = xArchADT.getParent(iref);
                    String brick = (String)xArchADT.get(oref, "Id");
                    //String brick = (String)xArchADT.get(xArchADT.getParent(xArchADT.getByID(refIXArch, iid)), "Id");
                    if (brick.equals(oldName)) {
                        // change anchor pointer
                        ObjRef aref = (ObjRef)xArchADT.get(points[j], "AnchorOnInterface");
                        l = iid.indexOf("_", 2);
                        if ( l != -1 ) {    // iid looks like I_BrickID_InterfaceID
                            newIID = iid.substring(l+1);
                            newIID = "I_" + newName + "_" + newIID;
                            xArchADT.set(aref, "Href", "#" + newIID);
                        }

                        // change link id
                        String lid = (String)xArchADT.get(links[i], "Id");
                        l = lid.indexOf(iid);
                        if ( l != -1 ) {    // link id looks like IID-IID
                            String newLid = newIID;
                            if (l==0)
                                newLid += lid.substring(l + iid.length());
                            else
                                newLid = lid.substring(0, l) + newIID;
                            xArchADT.set(links[i], "Id", newLid);
                        }

                        match = true;
                        break;
                    }
                }
            }

            // change brick id
            ObjRef refBrick = xArchADT.getByID(refIXArch, oldName);
            xArchADT.set(refBrick, "Id", newName);

            // change interface id
            ObjRef[] interfaces = xArchADT.getAll(refBrick, "Interface");
            for ( int i = 0; i<interfaces.length; i++ ) {
                iid = (String)xArchADT.get(interfaces[i], "Id");
                l = iid.indexOf("_", 2);
                if (l != -1) {
                    newIID = iid.substring(l+1);
                    newIID = "I_" + newName + "_" + newIID;
                    xArchADT.set(interfaces[i], "Id", newIID);
                }
            }
        }

        static final String C2TopInterface = "C2TopInterface",
                            C2BottomInterface = "C2BottomInterface";
        void weld(String top, String bottom) {
            String topInterface = getInterface(top, C2BottomInterface);
            String bottomInterface = getInterface(bottom, C2TopInterface);
            weldInterfaces(topInterface, bottomInterface);
        }

        String getInterface(String idBrick, String interfaceType) {
            // get an interface from brick id whose type is interfaceType
            ObjRef[] refInterfaces = xArchADT.getAll(xArchADT.getByID(idBrick), "Interface");
            for ( int i = 0; i<refInterfaces.length; i++ ) {
                String id = (String)xArchADT.get(refInterfaces[i], "Id");
                String type = getHrefId(refInterfaces[i], "Type");
                if (interfaceType.equals(type))
                    return id;
            }
            return "";
        }

        void weldInterfaces(String idTopInterface, String idBottomInterface) {
            ObjRef refLink = xArchADT.create(refTypesContext, "Link");
            xArchADT.set(refLink, "Id", idTopInterface + "-" + idBottomInterface);

            ObjRef refTopPoint = xArchADT.create(refTypesContext, "Point");
            ObjRef refTopAnchor = xArchADT.create(refTypesContext, "XMLLink");
            // to do: href handling
            xArchADT.set(refTopAnchor, "Href", "#"+idTopInterface);
            xArchADT.set(refTopPoint, "AnchorOnInterface", refTopAnchor);

            ObjRef refBottomPoint = xArchADT.create(refTypesContext, "Point");
            ObjRef refBottomAnchor = xArchADT.create(refTypesContext, "XMLLink");
            xArchADT.set(refBottomAnchor, "Href", "#"+idBottomInterface);
            xArchADT.set(refBottomPoint, "AnchorOnInterface", refBottomAnchor);

            xArchADT.add(refLink, "Point", refTopPoint);
            xArchADT.add(refLink, "Point", refBottomPoint);

            xArchADT.add(refIXArchStructure, "Link", refLink);
        }

        void unweld(String top, String bottom) {
            String topInterface = "I_" + top + "_" + C2BottomInterface;
            String bottomInterface = "I_" + bottom + "_" + C2TopInterface;
            if (xArchADT.getByID(top) != null && xArchADT.getByID(bottom) != null) {
                // the above naming convention should be correct,
                // but if the bricks still exist ( not in Remove Brick operation)
                // double check the interface id
                topInterface = getInterface(top, C2BottomInterface);
                bottomInterface = getInterface(bottom, C2TopInterface);
            }
            unweldInterfaces(topInterface, bottomInterface);
        }

        void unweldInterfaces(String idTopInterface, String idBottomInterface) {
            ObjRef refLink = xArchADT.getByID(refIXArch, idTopInterface + "-" + idBottomInterface);
            if (refLink == null) {
                // try reverse the ids
                refLink = xArchADT.getByID(refIXArch, idBottomInterface + "-" + idTopInterface);
                if (refLink == null) {
                    // well, cannot find by id specification, try link href
                    ObjRef[] links = xArchADT.getAll(refIXArchStructure, "Link");
                    for (int i = 0; i<links.length; i++) {  // all links
                        ObjRef[] points = xArchADT.getAll(links[i], "Point");
                        boolean topMatch = false, bottomMatch = false;
                        for (int j = 0; j<points.length; j++) { // link's points
                            String iid = getHrefId(points[j], "AnchorOnInterface");
                            if (iid.equals(idTopInterface))
                                topMatch = true;
                            if (iid.equals(idBottomInterface))
                                bottomMatch = true;
                        }
                        if (topMatch && bottomMatch) {
                            refLink = links[i];
                            break;
                        }
                    }
                }
            }
            if ( refLink != null)
                xArchADT.remove(xArchADT.getParent(refLink), "Link", refLink);
        }

        void addGroup(String group, String members) {
            ObjRef refGroup = xArchADT.create(refTypesContext, "Group");
            xArchADT.set(refGroup, "Id", group);
            xArchADT.add(refIXArchStructure, "Group", refGroup);

            StringTokenizer mems = new StringTokenizer(members, ",");
            while (mems.hasMoreElements())
            {
                String member = mems.nextToken();
                ObjRef refLink = xArchADT.create(refTypesContext, "XMLLink");
                // to do: href handling
                xArchADT.set(refLink, "Href", "#"+member);
                xArchADT.add(refGroup, "Member", refLink);
            }
        }

        void removeGroup(String group) {
            ObjRef refGroup = xArchADT.getByID(refIXArch, group);
            if ( refGroup != null )
                xArchADT.remove(xArchADT.getParent(refGroup), "Group", refGroup);
        }

        void addComponents(String components) {
            StringTokenizer c = new StringTokenizer(components, ";");
            while (c.hasMoreElements())
            {
                StringTokenizer component = new StringTokenizer(c.nextToken(), ",");
                String componentType = component.nextToken();
                String componentID = component.nextToken();
                addComponent(componentType, componentID);
            }
        }

        void addConnectors(String connectors) {
            StringTokenizer c = new StringTokenizer(connectors, ";");
            while (c.hasMoreElements())
            {
                StringTokenizer connector = new StringTokenizer(c.nextToken(), ",");
                String connectorType = connector.nextToken();
                String connectorID = connector.nextToken();
                addConnector(connectorType, connectorID);
            }
        }

        void addWelds(String welds) {
            StringTokenizer w = new StringTokenizer(welds, ";");
            while (w.hasMoreElements())
            {
                StringTokenizer weld = new StringTokenizer(w.nextToken(), ",");
                String idTopInterface = weld.nextToken();
                String idBottomInterface = weld.nextToken();
                weldInterfaces(idTopInterface, idBottomInterface);
            }
        }

        void addGroups(String groups) {
            StringTokenizer g = new StringTokenizer(groups, ";");
            while (g.hasMoreElements())
            {
                String gg = g.nextToken();
                int posComma = gg.indexOf(",");
                String group = gg.substring(0, posComma);
                String members = gg.substring(posComma+1);
                addGroup(group, members);
            }
        }

        // return true if compatible with current loaded architecture
        boolean compareArchitecture(String structureName,
                                    String components, String connectors,
                                    String welds, String groups) {
            setStructure(structureName);

            String com = getComponents();
            String con = getConnectors();
            String w = getWelds();
            String g = getGroups();

            if (compareStrings(com, components, ";") || compareStrings(con, connectors, ";") ||
                compareStrings(w, welds, ";"))
                return true;

            Map gm = new HashMap();
            StringTokenizer gt = new StringTokenizer(g, ";");
            String gg = "", group = "", members = "";
            int posComma = -1;
            while (gt.hasMoreElements())    // put groups into a map
            {
                gg = gt.nextToken();
                posComma = gg.indexOf(",");
                group = gg.substring(0, posComma);
                members = gg.substring(posComma+1);
                gm.put(group, members);
            }
            gt = new StringTokenizer(groups, ";");
            while (gt.hasMoreElements())    // check groups with map
            {
                gg = gt.nextToken();
                posComma = gg.indexOf(",");
                group = gg.substring(0, posComma);
                members = gg.substring(posComma+1);
                String m = (String)gm.remove(group);
                if (m==null)
                    return true;
                if (compareStrings(m, members, ","))
                    return true;
            }
            if(gm.size()>0)
                return true;
            return false;
        }

        // return true if two strings do not contain same elements
        boolean compareStrings(String inString, String outString, String delimiter) {
            Set in = new HashSet();
            StringTokenizer i = new StringTokenizer(inString, delimiter);
            while(i.hasMoreElements()) {
                in.add(i.nextToken());
            }
            StringTokenizer o = new StringTokenizer(outString, delimiter);
            while(o.hasMoreElements()) {
                if (!in.remove(o.nextToken()))
                    return true;
            }
            if (in.size()>0)
                return true;
            else
                return false;
        }

        // meta programming example
        String getAdders(String brick) {
            ObjRef refBrick = xArchADT.getByID(refIXArch, brick);
            Class brickClass = null;
            try {
                brickClass = Class.forName(xArchADT.getType(refBrick));
            }
            catch ( ClassNotFoundException e ) {
                System.err.println("VisioAgentComponent exception: " + e.getMessage());
                e.printStackTrace();
            }

            String adders = "";
            Method[] origMethods = brickClass.getMethods();
            for(int i = 0; i < origMethods.length; i++){
                Method m = origMethods[i];
                String methodName = m.getName();
                if(methodName.startsWith("set")){
                    Class c = m.getDeclaringClass();
                    if(c.getName().startsWith("edu.uci.isr.xarch.")){
                        int mods = m.getModifiers();
                        if(Modifier.isPublic(mods)){
                            if(!Modifier.isStatic(mods)){
                                Class[] paramClasses = m.getParameterTypes();
                                if(paramClasses.length == 1){
                                    if(!paramClasses[0].equals(java.lang.String.class)){
                                        if(paramClasses[0].getName().startsWith("edu.uci.isr.xarch.")){
                                            if(paramClasses[0].getName().indexOf(".", 18) != -1){
                                                String thing = methodName.substring(3);
                                                Object refThing = xArchADT.get(refBrick, thing);
                                                if (refThing == null)
                                                    adders += thing + ";";
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if(methodName.startsWith("add")){
                    Class c = m.getDeclaringClass();
                    if(c.getName().startsWith("edu.uci.isr.xarch.")){
                        int mods = m.getModifiers();
                        if(Modifier.isPublic(mods)){
                            if(!Modifier.isStatic(mods)){
                                Class[] paramClasses = m.getParameterTypes();
                                if(paramClasses.length == 1){
                                    if(!paramClasses[0].equals(java.util.Collection.class)){
                                        adders += methodName.substring(3) + ";";
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return adders;
        }

        void addChild(String brick, String typeOfChild) {
            ObjRef refBrick = xArchADT.getByID(refIXArch, brick);
            ObjRef refChild = xArchADT.create(refTypesContext, typeOfChild);
            try {
                xArchADT.add(refBrick, typeOfChild, refChild);
            }
            catch ( InvalidOperationException e ) {
                // no add method, try set method
                xArchADT.set(refBrick, typeOfChild, refChild);
            }
        }

        void createStructure(String id, String description) {
            ObjRef refStructure = xArchADT.createElement(refTypesContext, "ArchStructure") ;
            xArchADT.set(refStructure, "Id", id);

            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", description);
            xArchADT.set(refStructure, "Description", refDescription);

            xArchADT.add(refIXArch, "Object", refStructure);

            refIXArchStructure = refStructure;
        }

        void setStructure(String id) {
            refIXArchStructure = xArchADT.getByID(refIXArch, id);
        }

        void createInterfaceType(String id, String description) {
            ObjRef refInterfaceType = xArchADT.create(refTypesContext, "InterfaceType");
            xArchADT.set(refInterfaceType, "Id", id);

            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", description);
            xArchADT.set(refInterfaceType, "Description", refDescription);

            xArchADT.add(refIXArchTypes, "InterfaceType", refInterfaceType);
        }

        void createBrickType(String kind, String id, String description,
                             String signatures, String subarch) {
            ObjRef refType = xArchADT.create(refTypesContext, kind+"Type");
            xArchADT.set(refType, "Id", id);

            ObjRef refDescription = xArchADT.create(refTypesContext, "Description");
            xArchADT.set(refDescription, "Value", description);
            xArchADT.set(refType, "Description", refDescription);

            StringTokenizer st = new StringTokenizer(signatures, ";");
            while (st.hasMoreElements())    // check groups with map
            {
                ObjRef refSignature = xArchADT.create(refTypesContext, "Signature");

                String signature = st.nextToken();
                int posComma = signature.indexOf(",");
                String direction = signature.substring(0, posComma);
                String interfaceType = signature.substring(posComma+1);

                ObjRef refDirection = xArchADT.create(refTypesContext, "Direction");
                xArchADT.set(refDirection, "Value", direction);
                xArchADT.set(refSignature, "Direction", refDirection);

                ObjRef refTypeLink = xArchADT.create(refTypesContext, "XMLLink");
                // to do: href handling
                xArchADT.set(refTypeLink, "Href", "#"+interfaceType);
                xArchADT.set(refSignature, "Type", refTypeLink);

                xArchADT.add(refType, "Signature", refSignature);
            }

            if (!subarch.equals("")) {
                ObjRef refSubArch = xArchADT.create(refTypesContext, "SubArchitecture");
                ObjRef refStructureLink = xArchADT.create(refTypesContext, "XMLLink");
                // to do: href handling
                xArchADT.set(refStructureLink, "Href", "#"+subarch);
                xArchADT.set(refSubArch, "ArchStructure", refStructureLink);

                xArchADT.set(refType, "subArchitecture", refSubArch);
            }

            xArchADT.add(refIXArchTypes, kind+"Type", refType);
        }
    }
}
