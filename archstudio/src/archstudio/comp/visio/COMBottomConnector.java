package archstudio.comp.visio;

import c2.fw.*;

/**
 * <p>Title:        COMBottomConnector</p>
 * <p>Description:  Connector between COM and ArchStudio, with COM at bottom</p>
 * <p>Copyright:    Copyright (c) 2002</p>
 * <p>Organization: ISR, UCI</p>
 * @author          Jie Ren <A HREF="mailto:jie@ics.uci.edu">jie@ics.uci.edu</A>
 * @version         1.0
 */

public class COMBottomConnector extends COMConnector {
    /**
     * Initialize a <code>COMConnector</code>
     * @param  id   identifier of the connector
     */
    public COMBottomConnector(Identifier id) {
        super(id);
    }

    /**
     * Initialize a <code>COMConnector</code>
     * @param id        identifier of the connector
     * @param params    initialization parameters
     * acceptable parameters are
     *     LocalPort    = the port used for listening incoming COM messages
     *     RemoteHost   = the host that outgoing ArchStudio messages are sent to
     *     RemotePort   = the port that outgoing ArchStudio messages are sent to
     */
    public COMBottomConnector(Identifier id, InitializationParameter[] params){
        super(id, params);
    }

    /**
     * Set the local interface as a C2 TOP interface
     */
    protected void setLocalInterface() {
        localIface = new SimpleInterface(c2.legacy.AbstractC2Brick.TOP_INTERFACE_ID, this);
    }
}