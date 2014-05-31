package archstudio.comp.plamergedriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.*;

import archstudio.invoke.*;
import archstudio.comp.pladiff.*;
import archstudio.comp.plamerge.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * This is the main GUI Driver for PLAMerge component in archstudio.
 * The driver provides the UI for the merge service.  Merge
 * method may use either a version of a type, or a structure as a starting point.
 * This is determined by the starting point of the diff file..
 *
 * Its broken up into 3 main panels.  The top panel is the set up panel
 * where all the selection takes place.  The center panel is where the starting point is 
 * selected.  The very bottom panel is used to start the merge process
 */
public class PLAMergePanel extends JPanel implements ActionListener, MessageProvider
{
	protected final static String NO_ARCH_OPEN = "[No Architectures Open]";

	// global State variables
	protected boolean isStructural; // this keeps track of whether or not we are
								    // starting on structures or types
	protected String id;		   // This is the ID of this component (the driver)
	protected XArchFlatInterface xArch;
	
	// This contains a list of all the listeners we are sending messages to.
	protected Vector messageListeners = new Vector( 3 );

	// These are the flags used to fix a racing conditions and make sure data is 
	// consistent
	protected boolean ignoreOpenArchUpdate;

	// This flags just prevents us from listening to the 
	// action performed of the combo boxes while we are still adding elements
	protected boolean ignoreActionPerformed;
	
	
	/************************************************************************
	Stuff for the top panel
					Selecting the diff file 
	************************************************************************/
	protected JPanel north; // this is the panel that will contain the combo boxe
	
	// this is the model for selecting the diff file
	protected DefaultComboBoxModel diffUrlComboBoxModel = new DefaultComboBoxModel();
	protected JComboBox diffUrlList;
	protected String diffUrl = null;
	
	/************************************************************************
	Stuff for the center panel
					Selecting the starting points
	************************************************************************/
	protected JPanel center;
	
	// selecting the architecture to merge into
	protected DefaultComboBoxModel archUrlComboBoxModel = new DefaultComboBoxModel();
	protected JComboBox archUrlList;
	protected JPanel archUrlListPanel;
	protected String archUrl = null;
	protected String targetArchUrl = null;
	
	// These are the necessary elements for the architecture starting
	// point selector panel
	protected JPanel archPanel;
	protected JPanel archSelectorPanel = null;
	protected JPanel placeholder = new JPanel( );
	protected TypeSelector typeSelector = null;
	protected StructureSelector structSelector = null;

	//protected JProgressBar mainProgressBar;
	//protected JPanel progressPanel;
	
	/************************************************************************
	Stuff for the bottom panel
					Starting the diff
	************************************************************************/
	protected JPanel bottom;
	// this is where the user enters name of the new merged architecture
	protected JTextField targetUrlField;
	// the button to perform the merge
	protected JButton mergeButton;
	
	//******************** HELPER FUNCTIONS ***********************
	// This function takes the URL passed in and tries to get a reference 
	// to the file.  It will return true if the file is a Diff document
	// false otherwise
	protected boolean isDiffDoc( String url )
	{
		ObjRef arch = xArch.getOpenXArch( url );
		ObjRef diffContext = xArch.createContext( arch, "Pladiff" );
		
		ObjRef diff = xArch.getElement( diffContext, "PLADiff", arch );
		
		// if we got the diff successfully, it is considered a diff document
		return diff != null;
	}
	
	// This function checks the open diff document at the location specified
	// to see if it started on a structural or on a type-version.
	// Returns true if the diff doc's starting point is structural
	protected boolean isDiffStructural( String url )
	{
		ObjRef arch = xArch.getOpenXArch( url );
		ObjRef diffContext = xArch.createContext( arch, "Pladiff" );
		
		ObjRef diff = xArch.getElement( diffContext, "PLADiff", arch );
		ObjRef diffPart = ( ObjRef )xArch.get( diff, "DiffPart" );
		if( diffPart != null )
		{
			ObjRef diffLocation = ( ObjRef )xArch.get( diffPart, "DiffLocation" );
			ObjRef locationDesc = ( ObjRef )xArch.get( diffLocation, "Location" );
		
			String location = ( String )xArch.get( locationDesc, "Value" );
		
			return location.equals( IPLADiff.STRUCTURAL_STARTING_POINT );
		}
		// doesn't really matter what we return here.  The diff is empty
		// so its really neither structural nor type-version
		return false;
	}
	
	//This is initializes all the panels and elements
	// Note: this does not add each individual panels to this panel
	protected void init()
	{
		JPanel tempPane;
		/************************ Setting up the north panel ***********************/
		north = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		
		
		// First add the panel for the selection of the type of starting point
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		
		// sets up the URL list
		diffUrlList = new JComboBox( diffUrlComboBoxModel );
		diffUrlList.addActionListener( this );
		
		tempPane.add( new JLabel( "Diff document: " ) );
		tempPane.add( diffUrlList );
		
		north.add( tempPane );
		
		/************************ Setting up the center panel ***********************/
		isStructural = false;
		center = new JPanel( new GridLayout( 1, 2 ) );
		
		archPanel = new JPanel( );
		archPanel.setLayout( new BoxLayout( archPanel, BoxLayout.Y_AXIS ) );
		
		// Now create the panel to select the architecture to merge into
		archUrlListPanel = new JPanel( );
		archUrlListPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		// create the combo box
		archUrlList = new JComboBox( archUrlComboBoxModel );
		archUrlList.addActionListener( this );
		
		archUrlListPanel.add( new JLabel( "Architecture to Merge: " ) );
		archUrlListPanel.add( archUrlList );
		
		//Go get the initial list of URLs.
		updateOpenURLs();
		
		// refresh the selector panels
		// this will add all the url list panels and so forth 
		refreshArchPanel( true );
		
		center.add( archPanel );
		
		/************************ Setting up the bottom panel ***********************/
		bottom = new JPanel( new GridLayout( 2, 1 ) );
		
		// pane for user to enter target url
		tempPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		targetUrlField = new JTextField(30);
		
		tempPane.add(new JLabel("Target Merge URL: "));
		tempPane.add(targetUrlField);
		
		bottom.add(tempPane);
		
		// add the merge button
		tempPane = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
		
		mergeButton = new JButton( "Merge" );
		mergeButton.addActionListener( (ActionListener)
			c2.pcwrap.ThreadInterfaceProxyFactory.createThreadInterfaceProxy( this,
			ActionListener.class ) );
		
		tempPane.add( mergeButton );
		bottom.add( tempPane );
		
		/*
		mainProgressBar = new JProgressBar();
		mainProgressBar.setPreferredSize(new Dimension(300,21));
		mainProgressBar.setMinimumSize(new Dimension(300,21));
		mainProgressBar.setStringPainted(true);
		progressPanel = new JPanel();
		progressPanel.setLayout( new FlowLayout(FlowLayout.CENTER));
		progressPanel.add(selectButton);
		progressPanel.add(mainProgressBar);
		PLAMergePanel.add(progressPanel);
		*/
	}
	
	// This function just removes all that was in the architecture panel
	// and all the necessary elements back
	protected void refreshArchPanel( boolean createNewSelector )
	{
		archPanel.removeAll( );
		archPanel.add( archUrlListPanel );
		
		if( diffUrl != null && !diffUrl.equals( NO_ARCH_OPEN ) )
		{
			isStructural = isDiffStructural( diffUrl );
		}
		
		if( archUrl != null && !archUrl.equals( NO_ARCH_OPEN ) )
		{
			//System.out.println( "refresh orig" );
			if( archSelectorPanel != null )
				archSelectorPanel.removeAll( );
			if( createNewSelector )
			{
				archSelectorPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
				typeSelector = new TypeSelector( archUrl, "ComponentType", xArch );
				structSelector = new StructureSelector( archUrl, xArch );
			}
			
			if( isStructural )
				archSelectorPanel.add( structSelector );
			else
				archSelectorPanel.add( typeSelector );
			
			archPanel.add( archSelectorPanel );
		}
		else
		{
			// just add a place holder
			archPanel.add( placeholder );
		}
	}
	
	// ********************** PUBLIC FUNCTIONS *****************************
	public PLAMergePanel( XArchFlatInterface xArchIntf, String compId )
	{
		xArch = xArchIntf;
		id = compId;
		init();
		ignoreOpenArchUpdate = false;
		ignoreActionPerformed = false;
		
		this.setLayout( new BorderLayout( ) );
		// add each of the major panels
		this.add( north, BorderLayout.NORTH );
		this.add( center, BorderLayout.CENTER  );
		this.add( bottom, BorderLayout.SOUTH  );
		
		this.setVisible( true );
	}

	/**
	 * This method is necessary to invoke when a StateChangeMessage has been 
	 * sent from the xArchADT. The list of open urls is removed, and a new list is created by generating 
	 * a new call to getOpenXArchURLs, thus repopulating the list with the current available architectures. 
	 */
	public void updateOpenURLs( )
	{
		if( !ignoreOpenArchUpdate )
		{	
			diffUrlComboBoxModel.removeAllElements( );
			archUrlComboBoxModel.removeAllElements( );
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xArch.getOpenXArchURIs();

			boolean diffExists = false;
			boolean archExists = false;
			
			// This flags just prevents us from listening to the 
			// action performed of the combo boxes while we are still adding elements
			
			// we dont' want it to update while we are still adding
			// elements to the list model
			ignoreActionPerformed = true;
			for( int i = 0; i < urls.length; i++ )
			{
				if( isDiffDoc( urls[i] ) )
				{
					// its a diff doc
					diffUrlComboBoxModel.addElement( urls[i] );
					if( urls[i].equals( diffUrl ) )
					{
						diffExists = true;
					}
				}
				else
				{
					// its a regular architectuire
					archUrlComboBoxModel.addElement( urls[i] );
					if( urls[i].equals( archUrl ) )
					{
						archExists = true;
					}
				}
			}
			ignoreActionPerformed = false;
			
			// if there were no diff docs to process
			if( diffUrlComboBoxModel.getSize( ) == 0 )
			{
				// no open diff files
				diffUrlComboBoxModel.addElement( NO_ARCH_OPEN );
				diffUrl = null;
			}
			else if( !diffExists )
			{
				// the element from before was removed
				// selects the first element by default
				diffUrl = ( String )diffUrlComboBoxModel.getElementAt( 0 );
			}
			if( diffUrl != null )
			{
				// sets the selected item as the default diff URL
				diffUrlList.setSelectedItem( diffUrl );
			}
			
			// now set the default architecture
			if( archUrlComboBoxModel.getSize( ) == 0 )
			{
				// there are no open architecture files
				archUrlComboBoxModel.addElement( NO_ARCH_OPEN );
				
				// Null the string
				archUrl = null;
				// null the selectors
				structSelector = null;
				typeSelector = null;
				
				// refresh the panels since they don't contain anything now
				// it shouldn't matter what we pass in here
				refreshArchPanel( true );
			}	
			else if( !archExists )
			{
				// can't find the elemnt from before
				// selects the first element by default
				archUrl = ( String )archUrlComboBoxModel.getElementAt( 0 );
			}
			
			if( archUrl != null )
			{
				// sets the selected architecture
				// note this will trigger the action listern to update everythign
				archUrlList.setSelectedItem( archUrl );
			}
		}
		repaint();
	}
	
	/**
	 * This method is necessary to invoke when a StatusMessage has been sent from the PLAMerge.
	 */
	public void updateProgress( PLAMergeStatusMessage smsg )
	{
		if( smsg == null )
			return;
		if( !smsg.getIsDone( ) )
		{
			/*  Currently, no status bar... so do nothign while we are still going
			mainProgressBar.setMaximum(smsg.getUpperBound());
			mainProgressBar.setValue(smsg.getCurrentValue());
			double tempValue = mainProgressBar.getPercentComplete();
			tempValue = tempValue*100;
			DecimalFormat format = new DecimalFormat("##");
			String percent = format.format(tempValue);
			percent = "Selecting: " + percent + "%";
			mainProgressBar.setString(percent);
			repaint();
			*/
			return;
		}
		else
		{	
			// mainProgressBar.setValue(0);
			// String url = messageParameters.url;
			if( smsg.getErrorOccurred( ) )
				//we got an error message, set everything back and throw an exception
			{
				//mainProgressBar.setString("Error");

				JOptionPane.showMessageDialog(this, "Error while performing Merge!" , "Error", 
					JOptionPane.ERROR_MESSAGE);
				smsg.getError().printStackTrace( );
			}
			//we are finished with selecting, nothing left to do
			else
			{
				// mainProgressBar.setString("Finished");				
				//System.out.println("Finished Selecting!");
				//String op = "Finished " + opPerformed;
				JOptionPane.showMessageDialog(this, "Finished Mergeing!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
			}
			
			// enable everything back
			ignoreOpenArchUpdate = false;
			
			mergeButton.setEnabled(true);
			diffUrlList.setEnabled( true );
			archUrlList.setEnabled( true );
			targetUrlField.setEnabled( true );
			
			updateOpenURLs();
		}
	}
	
	public void actionPerformed( ActionEvent evt )
	{
		String label;
		Object source = evt.getSource( );
		
		// Something got selected and we want to listen to the event
		if( source instanceof JComboBox && !ignoreActionPerformed )
		{
			label = (String)( ( JComboBox)source ).getSelectedItem( );

			if( label == null || label.equals( NO_ARCH_OPEN ) )			
				return;
			
			if( source == diffUrlList )
			{
				diffUrl = label;
				// refresh the panel
				// no need to recreate the panels, we are just switching
				// on the structure or type
				refreshArchPanel( false );
			}
			else if( source == archUrlList )
			{
				archUrl = label;
				// refresh the panel
				// and we need to recreate the selector this time because
				// the selected architecture MIGHT have changed
				refreshArchPanel( true );
			}
			validate();
			repaint();
		}
		if( source instanceof JButton )
		{
			label = ( ( JButton )source ).getText( );

			// the diff button
			if(label.equals( "Merge" ) )
			{
				diffUrl = ( String )diffUrlComboBoxModel.getSelectedItem( );
				archUrl = ( String )archUrlComboBoxModel.getSelectedItem( );
				targetArchUrl = (String)targetUrlField.getText();
				
				// checks error conditions
				if( diffUrl == null || diffUrl.equals( NO_ARCH_OPEN ) )
				{
					JOptionPane.showMessageDialog(this, "No Diff document selected.", 
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if( archUrl == null || archUrl.equals( NO_ARCH_OPEN ) )
				{
					JOptionPane.showMessageDialog(this, "No architecture selected.", 
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if( !isStructural && !typeSelector.isVersionSelected( ) )
				{
					JOptionPane.showMessageDialog(this, "No Type/Version specified."
						, "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				else if( isStructural && structSelector.getStructureID( ) == null )
				{
					JOptionPane.showMessageDialog(this, "No Structure specified."
						, "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				else if(targetArchUrl == null || targetArchUrl.equals(""))
				{
					JOptionPane.showMessageDialog(this, "No Target Merge Architecture specified.",
						"ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
						
				//now decide which selections were made
				String startingID;
				if( isStructural )
				{
					startingID = structSelector.getStructureID( );
				}
				else
				{
					startingID = typeSelector.getTypeId( );
				}
				
				ignoreOpenArchUpdate = true;
				// disable all the buttons and fields
				mergeButton.setEnabled( false );
				diffUrlList.setEnabled( false );
				archUrlList.setEnabled( false );
				targetUrlField.setEnabled(false);
				
				System.out.println( "Performing Merge" );
				
				/*
				System.out.println( "Orig ID: " + origId );
				System.out.println( "New ID: " + newId );
				*/
				
				PerformPLAMergeMessage mergeMessage = new PerformPLAMergeMessage( diffUrl,
					archUrl, targetArchUrl, startingID, isStructural, id );

				fireMessageSent( mergeMessage );
			}		
		}
	}

	/*****************************************************************************
	Sending Messages
	******************************************************************************/
	protected void fireMessageSent(Message m)
	{
		int size = messageListeners.size();
		for(int i = 0; i < size; i++)
		{
			((MessageListener)messageListeners.elementAt(i)).messageSent(m);
		}
	}
	
	public void addMessageListener(MessageListener l)
	{
		messageListeners.addElement(l);
	}
	
	public void removeMessageListener(MessageListener l)
	{
		messageListeners.removeElement(l);
	}
}
