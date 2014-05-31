/**
 * This is the GUI Driver for the ArchDiff component in Arch Studio.
 * It allows the user to select 2 openned architectural descriptions and perform the diff.
 * 
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

package archstudio.comp.archdiffdriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import archstudio.invoke.*;
//This is imported to the selector
import archstudio.comp.archdiff.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class ArchDiffDriverC2Component extends AbstractC2DelegateBrick 
	implements InvokableBrick, c2.fw.Component
{
	public static final String PRODUCT_NAME = "ArchDiff Driver Component";
	public static final String PRODUCT_VERSION = "1.0";
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	//protected IArchDiff archDiff;
	
	protected ArchDiffDriverFrame archDiffFrame = null;
	
	public ArchDiffDriverC2Component(Identifier id){
		super( id );
		
		this.addLifecycleProcessor(new ArchDiffDriverLifecycleProcessor());
		this.addMessageProcessor( new ArchDiffDriverMessageProcessor());
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService(
			this, topIface, XArchFlatInterface.class );

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(archDiffFrame != null){
					archDiffFrame.updateOpenURLs();
					return;
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);

		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Differencing and Merging/Architecture Differencing Engine GUI", 
			"Calculates the difference between two architectures");
	}
		
	public void invoke(InvokeMessage im){
		newWindow();
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		//System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(archDiffFrame == null){
			//System.out.println("Creating new frame.");
			archDiffFrame = new ArchDiffDriverFrame();
		}
		else{
			archDiffFrame.requestFocus();
		}
	}

	public void closeWindow(){
		if(archDiffFrame != null){
			archDiffFrame.setVisible(false);
			archDiffFrame.dispose();
			archDiffFrame = null;
		}
	}
	
	class ArchDiffDriverLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			closeWindow();
		}
	}
	
	class ArchDiffDriverMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof ArchDiffStatusMessage){
				ArchDiffStatusMessage adsm = (ArchDiffStatusMessage)m;
				if(adsm.getErrorOccurred()){
					Exception e = adsm.getError();
					JOptionPane.showMessageDialog(archDiffFrame, e.toString(), "Diff Error",
						JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(archDiffFrame, "Diff Successful", "Diff Success",
						JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}
	
	private class ArchDiffDriverFrame extends JFrame implements ActionListener
	{
		
		protected DefaultComboBoxModel urlComboBoxModel = new DefaultComboBoxModel( );
		protected DefaultComboBoxModel urlComboBoxModel2 = new DefaultComboBoxModel( );
		
		protected JComboBox openUrlList;
		protected JComboBox openUrlList2;
		protected JTextField diffDest;
		
		protected JButton diffButton;
		
		// constant for showing that no architecture is open
		private final static String NO_ARCH_OPEN = "[No Architectures Open]";
		
		public ArchDiffDriverFrame()
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
			
			JPanel diffPanel = new JPanel();    // it is this panel that will be
			                                        // added to the tabbed pane
			                            
			// sets up the combo box (the drop down menu) for the list of 
			// open architectures
			openUrlList = new JComboBox(urlComboBoxModel);
			openUrlList2 = new JComboBox( urlComboBoxModel2 );
			
			// sets up the select button
			diffButton = new JButton( "Diff" );
			diffButton.addActionListener( this );
			diffButton.setActionCommand( "Diff" );
			
			// sets up the text field
			diffDest = new JTextField( 30 );
			
			
			diffPanel.setLayout( new BorderLayout( ) );
			// this is the center panel that will go into the diffPanel
			JPanel centerPanel = new JPanel( new GridLayout( 3, 1 ) );
			// this is the top center panel, for the first URL list
			tempPanel = new JPanel( );
			tempPanel.setLayout(new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Architecture 1: " ) );
			tempPanel.add( openUrlList);
			
			centerPanel.add( tempPanel );
			
			// now we add the second panel which is for the selection of the second arch.
			tempPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Architecture 2: " ) );
			tempPanel.add( openUrlList2 );
			
			centerPanel.add( tempPanel );
			
			// third panel for the text field
			tempPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Diff Destination: " ) );
			tempPanel.add( diffDest );
			
			centerPanel.add( tempPanel );
			
			// adds the center panel
			diffPanel.add( centerPanel, BorderLayout.CENTER );
			
			// now set up the south panel for the diff button
			tempPanel = new JPanel( );
			tempPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
			tempPanel.add( diffButton);
			diffPanel.add( tempPanel, BorderLayout.SOUTH );
			
			// adds the diff panel to the tabbed panel
			tabbedPane.addTab( "Diff", diffPanel );
			
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
			
			this.addWindowListener( new ArchDiffDriverWindowAdapter( ) );
			
			//Go get the initial list of URLs.
			updateOpenURLs();
		}
		
		class ArchDiffDriverWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				archDiffFrame = null;
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
				
				// the event came from the diff button.  so we perform diff
				if(label.equals( "Diff" ) )
				{
					// grab the different URLs
					String url = ( String )openUrlList.getSelectedItem();
					String url2 = ( String )openUrlList2.getSelectedItem( );
					String dest = diffDest.getText( );
					
					if(url.equals( NO_ARCH_OPEN ) || 
						url2.equals( NO_ARCH_OPEN ) )
					{
						JOptionPane.showMessageDialog(this, "No architecture selected.", "Error",
							JOptionPane.ERROR_MESSAGE);
						return;
					}
					else if( dest == null || dest.equals( "" ) )
					{
						JOptionPane.showMessageDialog(this, "No destination specified.", "Error",
							JOptionPane.ERROR_MESSAGE);
						return;
					}						
					else if( url.equals( url2 ) )
					{
						// just a warning to the user
						int response = JOptionPane.showConfirmDialog( this, 
							"Performing Diff on same architecture!  Continue?", 
							"Warning", JOptionPane.YES_NO_OPTION );
						// user said no, so we stop
						if( response == JOptionPane.NO_OPTION )
						{
							return;
						}
					}						
					try
					{
						System.out.println( "Doing Diff!" );
						PerformArchDiffMessage padm = new PerformArchDiffMessage(url, url2, dest);
						sendToAll(padm, topIface);
						//archDiff.diff( url, url2, dest );	
					}
					catch( Exception e)
					{
						JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace( );
						return;
					}					
				}
			}
		}
	}
}


