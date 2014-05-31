package archstudio.comp.visio;

import java.io.*;
import java.util.*;
import java.net.*;
import c2.fw.*;

/**
 * <p>Title:        COMConnector</p>
 * <p>Description:  Connector between COM and ArchStudio</p>
 * <p>Copyright:    Copyright (c) 2002, 2003</p>
 * <p>Organization: ISR, UCI</p>
 * @author          Jie Ren <A HREF="mailto:jie@ics.uci.edu">jie@ics.uci.edu</A>
 * @version         1.1
 * todo: tweak socket establishment sequencing
 */

public class COMConnector extends AbstractBrick implements Connector, ICOMConnector {

    public static final Identifier LOCAL_INTERFACE_ID = new SimpleIdentifier("IFACE_LOCAL");

    protected Interface     localIface;
    protected int           localPort = 3333;
    protected int           remotePort = 4444;
    protected String        remoteHost = "localhost";
    protected String	    owner = "VisioAgent";
    protected boolean       initDone = false;
    protected List          messageQueue = new ArrayList();

    /**
     * Initialize a <code>COMConnector</code>
     * @param  id   identifier of the connector
     */
    public COMConnector(Identifier id) {
        this(id, new InitializationParameter[]{});
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
    public COMConnector(Identifier id, InitializationParameter[] params){
        super(id);
        setLocalInterface();
		for (int i = 0; i<params.length; i++) {
			if (params[i].getName().equalsIgnoreCase("localPort")) {
				localPort = Integer.parseInt(params[i].getValue());
			}
			if (params[i].getName().equalsIgnoreCase("remotePort")) {
				remotePort = Integer.parseInt(params[i].getValue());
			}
			if (params[i].getName().equalsIgnoreCase("remoteHost")) {
				remoteHost = params[i].getValue();
			}
			if (params[i].getName().equalsIgnoreCase("owner")) {
				owner = params[i].getValue();
			}
		}
    }

    /**
     * set the local interface. The interface is the only interface that is
     * visible to ArchStudio. It could be just a simple, non directional interface.
     * Or, in accordance with C2 architecture style, it can be either the top
     * interface or the bottom interface.
     */
    protected void setLocalInterface() {
        localIface = new SimpleInterface(LOCAL_INTERFACE_ID, this);
    }

    /**
     * Given an interface identifier, get the corresponding interface, if it exists
     * @param       id      the interface identifier to get
     * @return      the local interface if the identifer matches. otherwise null.
     */
    public Interface getInterface(Identifier id){
        if(id.equals(localIface.getIdentifier())){
            return localIface;
        }
        else{
            return null;
        }
    }

    /**
     * Get all the interfaces, in this case the only local interface
     */
    public Interface[] getAllInterfaces(){
        return new Interface[]{
            localIface
        };
    }

    protected ListenerThread listener;
    protected SenderThread sender;
    public void init(){
        listener = new ListenerThread(localPort);
        sender = new SenderThread(remoteHost, remotePort);
    }

    protected boolean done = false;
    public void begin(){
        done = false;
        listener.start();
        sender.start();
    }

    public void end(){
        done = true;
    }

    public void destroy(){
    }

    /**
     * Handle the incoming ArchStudio message. Send them to Visio
     * @param   m   Message to handle
     */
    public void handle(Message m) {
        if(!m.getSource().getBrickIdentifier().equals(new SimpleIdentifier(owner)))
            return;

        // VisioAgent sends out an invokable message at initialization, we don't want it
        if(m instanceof archstudio.invoke.InvokableStateMessage)
            return;

        if (m instanceof NamedPropertyMessage) {
            NamedPropertyMessage nm = (NamedPropertyMessage)m;
            synchronized(messageQueue) {
                messageQueue.add(nm);
                synchronized(sender) {
                    sender.notify();
                }
            }
        }
    }

    class ListenerThread extends Thread {
        ServerSocket    server;
        ListenerThread(int port) {
            try {
                server = new ServerSocket(port);
            }
            catch (IOException e) {
                System.err.println("Cannot create server socket at " + port);
                e.printStackTrace();
            }
        }

        public void run() {
            try
            {
                while (!done) {
                    Socket client = server.accept();

                    // All COM strings are Unicode strings
                    InputStreamReader in = new InputStreamReader(client.getInputStream(),
                            "UnicodeLittleUnmarked");

                    // the format of the message is name, value, name, value, ..., empty string
                    // each string is a set of Unicode char followed by a Unicode 0 ('\0')
                    NamedPropertyMessage message = new NamedPropertyMessage("COMMessage");
                    boolean readingName = true;
                    String  name = null;

                    try {
                    int		input = in.read();
                    while ( input != -1 )   // data is available
                    {
                        StringBuffer buffer = new StringBuffer();
                        char 	c = (char)input;
                        while (c != '\0' && input != -1) {  // read a string
                            buffer.append(c);
                            input = in.read();
                            c = (char)input;
                        }
                        String result = buffer.toString();
                        if(result.equals("") && readingName) {
                            sendToAll(message, localIface);
                            message = new NamedPropertyMessage("COMMessage");
                            readingName = true;
                        }
                        else if(readingName) {
                            name = result;
                            readingName = false;
                        }
                        else {
                            message.addParameter(name, result);
                            readingName = true;
                        }
                        if (input != -1 ){
                            input = in.read();
                        }
                    }
                    }
                    catch (SocketException se) {
                    }
                    // end of this connection, loop back for the next one
                    in.close();
                    client.close();
                }
                server.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class SenderThread extends Thread {
        Socket              socket = null;
        OutputStreamWriter  out = null;
        String              host = "localhost";
        int                 port = -1;
        SenderThread(String host, int port) {
            this.host = host;
            this.port = port;
            try {
                socket = new Socket(host, port);
                out = new OutputStreamWriter(socket.getOutputStream(), "UnicodeLittleUnmarked");
            }
            catch (IOException e) {
                socket = null;
                out = null;
            }
        }

        /**todo reconnect, start order */
        public void run() {
            try {
                while (!done) {
                    // wait for message
                    synchronized(this) {
                        try {
                            wait();
                        }
                        catch (InterruptedException e) {
                        }
                    }

                    // if cannot connect, try to connect after next message
                    if(!isConnected())
                        continue;

                    synchronized(messageQueue) {
                        while(!messageQueue.isEmpty()) {
                            NamedPropertyMessage m = (NamedPropertyMessage)messageQueue.remove(0);
                            int		endofstring = 0;
                            String[]  names = m.getAllPropertyNames();
                            boolean successful = false;
                            while (!successful) {
                                try {
                                    for(int i = 0; i<names.length; i++) {
                                        Object value = m.getParameter(names[i]);
                                        if (value instanceof String) {
                                            String s = (String)value;
                                            out.write(names[i], 0, names[i].length());
                                            out.write(endofstring);
                                            out.write(s, 0, s.length());
                                            out.write(endofstring);
                                        }
                                    }
                                    out.write(endofstring);
                                    out.flush();
                                    successful = true;
                                }
                                catch (SocketException se) {
                                    successful = false;
                                    socket = null;
                                    out = null;
                                    if (!isConnected())
                                        throw se;
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isConnected() {
            if (socket != null)
                return true;

            int i = 0, maxTry = 60;
            while ( i<maxTry ) {
                try {
                    socket = new Socket(host, port);
                    out = new OutputStreamWriter(socket.getOutputStream(), "UnicodeLittleUnmarked");
                    return true;
                }
                catch (IOException e) {
                    i++;
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ie) {
                    }
                }
            }
            System.err.println("Cannot connect to " + host + " at "+ port);
            return false;
        }
    }
}