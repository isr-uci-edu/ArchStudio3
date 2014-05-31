/**
 * This is a simple data manager component for the coalition forces
 * demo.  It creates a panel with two buttons from which the user
 * can direct the US and French applications to share COPS data with
 * one another. It simply performs archmerge operations.
 * 
 * Note: the component listens for when the xadl for the US and French
 * applications are parsed.  It assumes the names "USFinal.xml" and
 * "FrenchFinal.xml" will be used.  It also assumes that "DiffUSFinal.xml"
 * and DiffFrenchFinal.xml" exists in the same directory.  .. yes it
 * is not terribly smart.
 *
 * @author Kari A. Nies
 */

package archstudio.comp.coalitiondatamgr;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import archstudio.invoke.*;
//This is imported to the merge
import archstudio.comp.archmerge.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.URL;

public class CoalitionDataMgrC2Component extends AbstractC2DelegateBrick 
    implements InvokableBrick, c2.fw.Component
{
    public static final String PRODUCT_NAME = 
        "Coalition Demo Data Manager Component";
	public static final String SERVICE_NAME = "Demos/Coalition Demo Data Manager";
    public static final String PRODUCT_VERSION = "1.0";



    // These are EPC interfaces implemented on other components,
    // in this case xArchADT and ArchMerchC2Component. Because 
    // these are EBIWrapperComponent(s), we can call functions in
    // them directly and all the communication gets translated
    // from procedure calls (PC) to EPC.
    protected XArchFlatInterface xarch;  
    protected IArchMerge archMerge;
    
    protected CoalitionDataMgrFrame coalitionDataMgrFrame = null;
    
    public CoalitionDataMgrC2Component( Identifier id )
    {
        super( id );
        
        this.addMessageProcessor( new StateChangeMessageProcessor( ) );
        xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService
            (this, topIface, XArchFlatInterface.class );
        
        //archMerge = ( IArchMerge )EBIWrapperUtils.addExternalService
        //    (this, topIface, IArchMerge.class );
        
        InvokeUtils.deployInvokableService(this, bottomIface, 
                							SERVICE_NAME, 
                                           "Coalition Demo Data Manager GUI");
    }
    
    public void invoke(InvokeMessage im)
    {
        newWindow();
    }
    
    //This is called when we get an invoke message from the invoker.
    public void newWindow()
    {
        //This makes sure we only have one active window open.
        if (coalitionDataMgrFrame == null){
            //System.out.println("Creating new frame.");
            coalitionDataMgrFrame = new CoalitionDataMgrFrame();
        } else {
            coalitionDataMgrFrame.requestFocus();
        }
    }


    class StateChangeMessageProcessor implements MessageProcessor {

        public void handle(Message m)
        {
            if (coalitionDataMgrFrame != null) {
                if (m instanceof NamedPropertyMessage) {
                    NamedPropertyMessage npm = (NamedPropertyMessage)m;
                    try {
                        // If we get a state change message from above, it may 
                        // have come from xArchADT.  We need to pay attention 
                        // to this message if it indicates a file was opened 
                        // or closed.
                        if (npm.getBooleanParameter("stateChangeMessage")) {
                            // The first parameter of the state change message, 
                            // if it does indicate a change in the 
                            // open-file-list, will be an XArchFileEvent
                            if (npm.getParameter("paramValue0") 
                                instanceof XArchFileEvent) {
                                XArchFileEvent evt = 
                                    (XArchFileEvent)npm.getParameter("paramValue0");
                                if (evt.getEventType() == 
                                    XArchFileEvent.XARCH_OPENED_EVENT) {
                                    String openedURL = evt.getURL();
                                    // check for for US or Fr coaltion arch  
                                    coalitionDataMgrFrame.checkURL(openedURL);
                                }
                            }
                        }
                    }
                    catch(Exception e) {
                        return;
                    }
                }
            }
        }
    }

    private class CoalitionDataMgrFrame extends JFrame 
                                        implements ActionListener {

        protected String usArchURL = null;
        protected String usDiffURL = null;
        protected String frenchArchURL = null;
        protected String frenchDiffURL = null;
        
        protected JButton usButton;
        protected JButton frenchButton;
        
        public CoalitionDataMgrFrame()
        {
            super(PRODUCT_NAME + " " + PRODUCT_VERSION);
            init();
        }

         
        private void init()
        {
            Toolkit tk = getToolkit();
            Dimension screenSize = tk.getScreenSize();
            /*
            double xSize = (500);
            double ySize = (300);
            */
            double xPos = (screenSize.getWidth() * 0.25);
            double yPos = (screenSize.getHeight() * 0.30);
            
            JPanel tempPanel;    
            JPanel panel = new JPanel();   
            
            // sets up the select buttons
            usButton = new JButton( "Share US Data Now" );
            usButton.addActionListener(this);
            usButton.setActionCommand( "US" );
            usButton.setEnabled(false);

            frenchButton = new JButton( "Share French Data Now" );
            frenchButton.addActionListener(this);
            frenchButton.setActionCommand( "French" );
            frenchButton.setEnabled(false);
           
            checkOpenURLs(); // determines if buttons should be enabled
            
            panel.setLayout(new BorderLayout());
            Box box = Box.createVerticalBox();
            box.add(usButton);
            box.add(frenchButton);
            panel.add(box, BorderLayout.CENTER);
            // stretch us button to same length as french button
            usButton.setPreferredSize(frenchButton.getPreferredSize());
            usButton.setMaximumSize(frenchButton.getMaximumSize());
            usButton.setMinimumSize(frenchButton.getMinimumSize());

            getContentPane().add(panel);
            pack();

            setVisible(true);
            //setSize((int)xSize, (int)ySize);
            setLocation((int)xPos, (int)yPos);
            setVisible(true);
            paint(getGraphics());
            
            this.addWindowListener(new CoalitionDataMgrWindowAdapter());
        }
        
        class CoalitionDataMgrWindowAdapter extends WindowAdapter
        {
            public void windowClosing(WindowEvent e)
            {
                destroy();
                dispose();
                setVisible(false);
                coalitionDataMgrFrame = null;
            }
        }				

        public void checkURL (String strURL) 
        {
            URL archURL;
            URL diffURL;

            try {
                archURL = new URL(strURL);
                String fileName = archURL.getFile();
                // check for for US or Fr coaltion arch
                if (fileName.endsWith("/USFinal.xml")) {
                    usArchURL = strURL;
                    // assume filename of diff url
                    usDiffURL = strURL.replaceAll("USFinal.xml",
                                                  "DiffUSFinal.xml");
                    usButton.setEnabled(true);
                } else if (fileName.endsWith("/FranceFinal.xml")) {
                    // assume filename of diff url
                    frenchArchURL = strURL;
                    frenchDiffURL = strURL.replaceAll("FranceFinal.xml",
                                                      "DiffFranceFinal.xml");
                    frenchButton.setEnabled(true);
                }
            } catch( Exception e) {
                JOptionPane.showMessageDialog(this, e.toString(), 
                                              "Error", 
                                              JOptionPane.ERROR_MESSAGE);
                e.printStackTrace( );
                return;
            }
        }
        
        private void checkOpenURLs() 
        {
            String[] urls = xarch.getOpenXArchURIs();
            if (urls.length > 0) {
                for(int i = 0; i < urls.length; i++) {
                    checkURL(urls[i]);
                }
            }
        }

     
        // this is the action listener for this class.
        public void actionPerformed(ActionEvent evt)
        {
            if (evt.getSource() instanceof JButton) {
                String label = ((JButton)evt.getSource()).getActionCommand();
                String diffURL = null;
                String archURL = null;
                
                if (label.equals("US")) {
                    archURL = usArchURL;
                    diffURL = usDiffURL;
                    usButton.setEnabled(false);
                } else if (label.equals("French")) {
                    archURL = frenchArchURL;
                    diffURL = frenchDiffURL;
                    frenchButton.setEnabled(false);
                }						
                try {
                    System.out.println( "Doing merge!" );
                    xarch.parseFromURL(diffURL);
										PerformArchMergeMessage pamm = new PerformArchMergeMessage(archURL, diffURL);
										sendToAll(pamm, topIface);
                    //archMerge(diffURL, archURL);	
                }
                catch( Exception e) {
                    JOptionPane.showMessageDialog(this, e.toString(), 
                                                  "Error", 
                                                  JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace( );
                    return;
                }					
            }
        }
    }
}

