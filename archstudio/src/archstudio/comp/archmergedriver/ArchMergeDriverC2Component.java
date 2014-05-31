/**
 * This is the GUI Driver for the ArchMerge component.  It provides the basic
 * functionality of letting the user select 2 opened documents, a merge and a architectural
 * description, and merge the 2 files.
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

package archstudio.comp.archmergedriver;

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

public class ArchMergeDriverC2Component extends AbstractC2DelegateBrick 
	implements InvokableBrick, c2.fw.Component
{
	public static final String PRODUCT_NAME = "ArchMerge Driver Component";
	public static final String PRODUCT_VERSION = "1.0";
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	//protected IArchMerge archMerge;
	
	protected ArchMergeDriverFrame archMergeFrame = null;
	
	public ArchMergeDriverC2Component( Identifier id )
	{
		super( id );
		
		this.addLifecycleProcessor(new ArchMergeDriverLifecycleProcessor());
		
		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(archMergeFrame != null){
					archMergeFrame.updateOpenURLs();
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);

		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService(
			this, topIface, XArchFlatInterface.class );
		
		//archMerge = ( IArchMerge )EBIWrapperUtils.addExternalService(
		//	this, topIface, IArchMerge.class );
		
		addMessageProcessor(new ArchMergeDriverMessageProcessor());
		
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Differencing and Merging/Architecture Merging Engine GUI", 
			"Merges architectural differences into other architectures as patches");
	}
	
	public void invoke(InvokeMessage im){
		newWindow();
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		//System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(archMergeFrame == null){
			//System.out.println("Creating new frame.");
			archMergeFrame = new ArchMergeDriverFrame();
		}
		else{
			archMergeFrame.requestFocus();
		}
	}
	
	public void closeWindow(){
		if(archMergeFrame != null){
			archMergeFrame.setVisible(false);
			archMergeFrame.dispose();
			archMergeFrame = null;
		}
	}
	
	class ArchMergeDriverLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			closeWindow();
		}
	}
	
	class ArchMergeDriverMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof ArchMergeStatusMessage){
				ArchMergeStatusMessage amsm = (ArchMergeStatusMessage)m;
				if(amsm.getErrorOccurred()){
					Exception e = amsm.getError();
					JOptionPane.showMessageDialog(archMergeFrame, e.toString(), "Merge Error",
						JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(archMergeFrame, "Merge Successful", "Merge Success",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}
	
	private class ArchMergeDriverFrame extends JFrame implements ActionListener
	{
		
		protected DefaultComboBoxModel urlComboBoxModel = new DefaultComboBoxModel( );
		protected DefaultComboBoxModel urlComboBoxModel2 = new DefaultComboBoxModel( );
		
		protected JComboBox diffLocation;
		protected JComboBox archLocation;
		
		protected JButton mergeButton;
		
		// constant for showing that no architecture is open
		private final static String NO_ARCH_OPEN = "[No Architectures Open]";
		
		public ArchMergeDriverFrame()
		{
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			archstudio.Branding.brandFrame(this);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init()
		{
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (500);
			double ySize = (300);
			double xPos = (screenSize.getWidth() * 0.25);
			double yPos = (screenSize.getHeight() * 0.30);
			
			JPanel tempPanel;    
			// this just makes it a tabbed pane.  Why?  Not sure....
			JTabbedPane tabbedPane = new JTabbedPane();
			
			JPanel mergePanel = new JPanel();    // it is this panel that will be
			// added to the tabbed pane
			
			// sets up the combo box (the drop down menu) for the list of 
			// open architectures
			diffLocation = new JComboBox(urlComboBoxModel);
			archLocation = new JComboBox( urlComboBoxModel2 );
			
			// sets up the select button
			mergeButton = new JButton( "Merge" );
			mergeButton.addActionListener( this );
			mergeButton.setActionCommand( "merge" );
			
			// now we set up the panels that will beadded to mergePanel
			mergePanel.setLayout( new BorderLayout( ) );
			// this is the center panel that will go into the mergePanel
			JPanel centerPanel = new JPanel( new GridLayout( 2, 1 ) );
			// this is the top center panel, for the first URL list
			tempPanel = new JPanel( );
			tempPanel.setLayout(new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Diff URL: " ) );
			tempPanel.add( diffLocation );
			
			centerPanel.add( tempPanel );
			
			// now we add the second panel which is for the selection of the second arch.
			tempPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Architecture URL: " ) );
			tempPanel.add( archLocation );
			
			centerPanel.add( tempPanel );
			
			// adds the center panel
			mergePanel.add( centerPanel, BorderLayout.CENTER );
			
			// now set up the south panel for the merge button
			tempPanel = new JPanel( );
			tempPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
			tempPanel.add( mergeButton);
			mergePanel.add( tempPanel, BorderLayout.SOUTH );
			
			// adds the merge panel to the tabbed panel
			tabbedPane.addTab( "merge", mergePanel );
			
			// finally add everything to this fram
			JPanel mainPanel = new JPanel();
			
			mainPanel.setLayout( new BorderLayout( ) );
			mainPanel.add( tabbedPane, BorderLayout.CENTER );
			
			this.getContentPane( ).add( new JScrollPane( mainPanel ) );
			
			setVisible( true );
			setSize( ( int )xSize, ( int )ySize );
			setLocation( ( int )xPos, ( int )yPos );
			setVisible( true );
			paint( getGraphics( ) );
			
			this.addWindowListener( new ArchMergeDriverWindowAdapter( ) );
			
			//Go get the initial list of URLs.
			updateOpenURLs();
		}
		
		class ArchMergeDriverWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				archMergeFrame = null;
			}
		}				
		
		// this is to update the open architectures in the ComboBox by updating the
		// combo model
		public void updateOpenURLs()
		{
			urlComboBoxModel.removeAllElements( );
			urlComboBoxModel2.removeAllElements( );
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xarch.getOpenXArchURIs();
			if(urls.length == 0)
			{
				urlComboBoxModel.addElement( NO_ARCH_OPEN );
				urlComboBoxModel2.addElement( NO_ARCH_OPEN );
			}
			else
			{
				for(int i = 0; i < urls.length; i++)
				{
					urlComboBoxModel.addElement(urls[i]);
					urlComboBoxModel2.addElement(urls[i]);
				}
				// if there is at least 2 architectures open
				if( urls.length > 1 )
				{
					// selects the first element by default
					urlComboBoxModel.setSelectedItem( urlComboBoxModel.getElementAt( 0 ) );
					// selects the second element by default
					urlComboBoxModel2.setSelectedItem( urlComboBoxModel2.getElementAt( 1 ) );	
				}
			}
			repaint();
		}
		
		// this is the action listener for this class.
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() instanceof JButton)
			{
				String label = ( ( JButton )evt.getSource( ) ).getText();
				
				// the event came from the merge button.  so we perform merge
				if(label.equals( "Merge" ) )
				{
					// grab the mergeerent URLs
					String diffURI = ( String )diffLocation.getSelectedItem();
					String archURI = ( String )archLocation.getSelectedItem( );
					
					if(diffURI.equals( NO_ARCH_OPEN ) || 
						archURI.equals( NO_ARCH_OPEN ) )
					{
						JOptionPane.showMessageDialog(this, "No architecture selected.", "Error",
							JOptionPane.ERROR_MESSAGE);
						return;
					}		
					// if 2 urls are the same
					else if( diffURI.equals( archURI ) )
					{
						JOptionPane.showMessageDialog(this, "Cannot merge the same architectures!.", "Error",
							JOptionPane.ERROR_MESSAGE);
						return;
					}						
					//try
					//{
						//System.out.println( "Doing merge!" );
						PerformArchMergeMessage pamm = new PerformArchMergeMessage(archURI, diffURI);
						sendToAll(pamm, topIface);
						//archMerge.merge( diffURI, archURI );	
					//}
					//catch( Exception e)
					//{
					//	JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					//	e.printStackTrace( );
					//	return;
					//}					
				}
			}
		}
	}
}

