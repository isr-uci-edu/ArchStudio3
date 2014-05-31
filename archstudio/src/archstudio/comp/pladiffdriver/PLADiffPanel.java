package archstudio.comp.pladiffdriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.*;

import archstudio.invoke.*;
import archstudio.comp.pladiff.*;


//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * This is the main GUI Driver for PLADiff component in archstudio.
 * The driver provides different options of performing the diff.  A diff
 * method may use either a version of a type, or a structure as a starting point.
 *
 * Its broken up into 3 main panels.  The top panel is the set up panel
 * where all the selection takes place.  The center panel is where the starting point is 
 * selected.  The very bottom panel is used to start the diff process
 */
public class PLADiffPanel extends JPanel implements ActionListener, MessageProvider
{
	protected final static String NO_ARCH_OPEN = "[No Architectures Open]";
	protected final static boolean ORIG_ARCH = true;
	protected final static boolean NEW_ARCH = false;		
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
					Selecting the architectures, and type/structure
	************************************************************************/
	protected JPanel north; // this is the panel that will contain 
	
	// This is the radio button and its button group that will be used
	// to select whether the starting point will be a type-version or a structure
	protected ButtonGroup structTypeButtonGroup = new ButtonGroup( );
	protected JRadioButton typeRadioButton;
	protected JRadioButton structureRadioButton;
	
	/************************************************************************
	Stuff for the center panel
					Selecting the starting points
	************************************************************************/
	protected JPanel center;
	
	// all the URL selection and type selection stuff
	// We need 2 combo box models to keep track of the open URL list
	// For the original arch
	protected DefaultComboBoxModel origUrlComboBoxModel = new DefaultComboBoxModel();
	protected JComboBox origUrlList;
	protected JPanel origUrlListPanel;
	protected String origUrl = null;
	
	// for the new arch
	protected DefaultComboBoxModel newUrlComboBoxModel = new DefaultComboBoxModel();
	protected JComboBox newUrlList;
	protected JPanel newUrlListPanel;
	protected String newUrl = null;
	
	// These are the necessary elements for the original architecture panel
	protected JPanel origArchPanel;
	protected JPanel origSelectorPanel = null;
	protected JPanel origPlaceholder = new JPanel( );
	protected TypeSelector origTypeSelector = null;
	protected StructureSelector origStructSelector = null;
	

	// This is for the new architecture panel
	protected JPanel newArchPanel;
	protected JPanel newSelectorPanel = null;
	protected JPanel newPlaceholder = new JPanel( );
	protected TypeSelector newTypeSelector = null;
	protected StructureSelector newStructSelector = null;
	
	//protected JProgressBar mainProgressBar;
	//protected JPanel progressPanel;
	
	/************************************************************************
	Stuff for the bottom panel
					Starting the diff
	************************************************************************/
	protected JPanel bottom;
	// This is for the user to enter in the diff location
	protected JTextField diffUrlField;
	// the button to perform the diff
	protected JButton diffButton;
	
	public PLADiffPanel( XArchFlatInterface xArchIntf, String compId )
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
	
	//This is initializes all the panels and elements
	// Note: this does not add each individual panels to this panel
	protected void init()
	{
		JPanel tempPane;
		/************************ Setting up the north panel ***********************/
		north = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
				
		// First add the panel for the selection of the type of starting point
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		// The type version is selected by default
		isStructural = false;
		typeRadioButton = new JRadioButton( "Type-Version", true );
		typeRadioButton.addActionListener( this );
		
		structureRadioButton = new JRadioButton( "ArchStructure", false );
		structureRadioButton.addActionListener( this );
		
		structTypeButtonGroup.add( typeRadioButton );
		structTypeButtonGroup.add( structureRadioButton );
		
		tempPane.add( new JLabel( "Please select the type of starting points: " ) );
		tempPane.add( typeRadioButton );
		tempPane.add( structureRadioButton );
		
		north.add( tempPane );
		
		/************************ Setting up the center panel ***********************/
		center = new JPanel( new GridLayout( 1, 2 ) );
		
		
		origArchPanel = new JPanel( );
		origArchPanel.setLayout( new BoxLayout( origArchPanel, BoxLayout.Y_AXIS ) );
		newArchPanel = new JPanel( );
		newArchPanel.setLayout( new BoxLayout( newArchPanel, BoxLayout.Y_AXIS ) );

				
		// Now create the panel to select the original architecture
		origUrlListPanel = new JPanel( );
		origUrlListPanel.setLayout( new GridLayout( 2, 1 ) );
		origUrlList = new JComboBox( origUrlComboBoxModel );
		origUrlList.addActionListener( this );
		
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		tempPane.add( new JLabel( "Original Architecture: " ) );
		origUrlListPanel.add( tempPane );
		
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		tempPane.add( origUrlList );
		origUrlListPanel.add( tempPane );
		
		// Now create the panel to select the new architecture
		newUrlListPanel = new JPanel( );
		newUrlListPanel.setLayout( new GridLayout( 2, 1 ) );
		newUrlList = new JComboBox( newUrlComboBoxModel );
		newUrlList.addActionListener( this );
		
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		tempPane.add( new JLabel( "New Architecture: " ) );
		newUrlListPanel.add( tempPane );
		
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		tempPane.add( newUrlList );
		newUrlListPanel.add( tempPane );

		//Go get the initial list of URLs.
		updateOpenURLs();

		// refresh both the 
		refreshArchPanels( ORIG_ARCH, true );
		refreshArchPanels( NEW_ARCH, true );

		center.add( origArchPanel );
		center.add( newArchPanel );
		
		/************************ Setting up the bottom panel ***********************/
		bottom = new JPanel( new GridLayout( 2, 1 ) );
		
		// Setting up the panel for user to enter in the diff doc info
		tempPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		diffUrlField = new JTextField( 30 );
		
		tempPane.add( new JLabel( "Diff Document URL: " ) );
		tempPane.add( diffUrlField );
		
		bottom.add( tempPane );

		// add the diff button
		tempPane = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
		
		diffButton = new JButton( "Diff" );
		diffButton.addActionListener( (ActionListener)
			c2.pcwrap.ThreadInterfaceProxyFactory.createThreadInterfaceProxy( this,
			ActionListener.class ) );
		
		tempPane.add( diffButton );
		
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
		PLADiffPanel.add(progressPanel);
		*/
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
			origUrlComboBoxModel.removeAllElements( );
			newUrlComboBoxModel.removeAllElements( );
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xArch.getOpenXArchURIs();
			if(urls.length == 0)
			{
				origUrlComboBoxModel.addElement( NO_ARCH_OPEN );
				newUrlComboBoxModel.addElement( NO_ARCH_OPEN );
				
				// Null the strings
				origUrl = newUrl = null;
				// null the selectors
				origStructSelector = newStructSelector = null;
				origTypeSelector = newTypeSelector = null;
				
				// refresh the panels since they don't contain anything now
				// it shouldn't matter what we pass in here
				refreshArchPanels( ORIG_ARCH, true );
				refreshArchPanels( NEW_ARCH, true );
			}
			else
			{
				boolean origExists = false;
				boolean newExists = false;
				
				// This flags just prevents us from listening to the 
				// action performed of the combo boxes while we are still adding elements
				
				// we dont' want it to update while we are still adding
				// elements to the list model
				ignoreActionPerformed = true;
				for( int i = 0; i < urls.length; i++ )
				{
					origUrlComboBoxModel.addElement( urls[i] );
					if( urls[i].equals( origUrl ) )
					{
						origExists = true;
					}
					
					newUrlComboBoxModel.addElement( urls[i] );
					if( urls[i].equals( newUrl ) )
					{
						newExists = true;
					}
				}
				ignoreActionPerformed = false;
				
				// if the same element is no longer there
				if( !origExists )
				{
					// the element from before was removed
					// selects the first element by default
					origUrl = ( String )origUrlComboBoxModel.getElementAt( 0 );
				}
				origUrlList.setSelectedItem( origUrl );
				
				// if there is at least 2 architectures open
				// then we select a different arch to diff against
				if( urls.length > 1 )
				{
					// if the same element is no longer there
					if( !newExists )
					{
						// can't find the elemnt from before
						// selects the second element by default
						newUrl = ( String )newUrlComboBoxModel.getElementAt( 1 );
					}
				}
				// don't have enough open architectures so just pick the first
				else
				{
					newUrl = origUrl;
				}
				newUrlList.setSelectedItem( newUrl );
			}
			repaint();
		}
	}
	
	/**
	 * This method is necessary to invoke when a StatusMessage has been sent from the PLADiff.
	 */
	public void updateProgress( PLADiffStatusMessage smsg )
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
				
				ignoreOpenArchUpdate = false;
				
				diffButton.setEnabled(true);
				typeRadioButton.setEnabled( true );
				structureRadioButton.setEnabled( true );
				diffUrlField.setEnabled( true );
				origUrlList.setEnabled( true );
				newUrlList.setEnabled( true );
				
				updateOpenURLs();
				JOptionPane.showMessageDialog(this, "Error while performing Diff!" , "Error", 
					JOptionPane.ERROR_MESSAGE);
				
				smsg.getError().printStackTrace( );
			}
			//we are finished with selecting, nothing left to do
			else
			{
				// mainProgressBar.setString("Finished");
				ignoreOpenArchUpdate = false;
							
				diffButton.setEnabled(true);
				typeRadioButton.setEnabled( true );
				structureRadioButton.setEnabled( true );
				diffUrlField.setEnabled( true );
				origUrlList.setEnabled( true );
				newUrlList.setEnabled( true );
				
				updateOpenURLs();
				
				//System.out.println("Finished Selecting!");
				//String op = "Finished " + opPerformed;
				JOptionPane.showMessageDialog(this, "Finished Diffing!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
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
			
			if( source == origUrlList )
			{
				origUrl = label;
				// refresh the panel
				refreshArchPanels( ORIG_ARCH, true );
			}
			else if( source == newUrlList )
			{

				newUrl = label;
				// refresh the panel
				refreshArchPanels( NEW_ARCH, true );
			}
			validate();
			repaint();
		}
		if( source instanceof JButton )
		{
			label = ( ( JButton )source ).getText( );

			// the diff button
			if(label.equals( "Diff" ) )
			{
				origUrl = ( String )origUrlComboBoxModel.getSelectedItem( );
				newUrl = ( String )newUrlComboBoxModel.getSelectedItem( );
				String diffUrl = diffUrlField.getText( );
				
				// checks error conditions
				if( origUrl == null || origUrl.equals( NO_ARCH_OPEN ) )
				{
					JOptionPane.showMessageDialog(this, "No original architecture selected.", 
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if( newUrl == null || newUrl.equals( NO_ARCH_OPEN ) )
				{
					JOptionPane.showMessageDialog(this, "No new architecture selected.", 
						"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if( diffUrl.equals( "" ) )
				{
					JOptionPane.showMessageDialog(this, "No diff document URL/URN specified."
						, "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				else if( !isStructural && ( !origTypeSelector.isVersionSelected( ) ||
										    !newTypeSelector.isVersionSelected( ) ) )
				{
					JOptionPane.showMessageDialog(this, "Not all Type/Versions specified."
						, "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
				else if( isStructural && ( origStructSelector.getStructureID( ) == null ||
										  newStructSelector.getStructureID( ) == null ) )
				{
					JOptionPane.showMessageDialog(this, "Not all Structures specified."
						, "Error", JOptionPane.ERROR_MESSAGE );
					return;
				}
		
				//now decide which selections were made
				String origId, newId;
				if( isStructural )
				{
					origId = origStructSelector.getStructureID( );
					newId = newStructSelector.getStructureID( );
				}
				else
				{
					origId = origTypeSelector.getTypeId( );
					newId = newTypeSelector.getTypeId( );
				}
				
				// finally check for the warning condition
				if( origId.equals( newId ) &&
					origUrl.equals( newUrl ) )
				{
					int choice = JOptionPane.showConfirmDialog( this, 
						"Original and new starting elements are the same, continue?", 
						"Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
					
					// the user doesn't want to continue
					if( choice == JOptionPane.CANCEL_OPTION )
					{
						return;
					}
				}
				
				ignoreOpenArchUpdate = true;
				// disable all the buttons and fields
				typeRadioButton.setEnabled( false );
				structureRadioButton.setEnabled( false );
				diffUrlField.setEnabled( false );
				diffButton.setEnabled( false );
				origUrlList.setEnabled( false );
				newUrlList.setEnabled( false );
				
				System.out.println( "Performing Diff" );
				
				/*
				System.out.println( "Orig ID: " + origId );
				System.out.println( "New ID: " + newId );
				*/
				
				PerformPLADiffMessage diffMessage = new PerformPLADiffMessage( origUrl,
					newUrl, diffUrl, origId, newId, isStructural, id );

				fireMessageSent( diffMessage );
			}		
		}
		else if( source instanceof JRadioButton )
		{
			label = ( ( JRadioButton )source ).getText( );
			
			if( label.equals( "Type-Version" ) )
			{
				// we went from structural to type
				if( isStructural )
				{					
					isStructural = false;
					// refresh the panels
					// we are just switching between the 2, but same file
					refreshArchPanels( ORIG_ARCH, false );
					refreshArchPanels( NEW_ARCH, false );
				}
			}
			else if( label.equals( "ArchStructure" ) )
			{
				// we went from type to structure
				if( !isStructural )
				{				
					isStructural = true;
					// refresh the panels
					// we are just switching between the 2, but same file
					refreshArchPanels( ORIG_ARCH, false );
					refreshArchPanels( NEW_ARCH, false );
				}
			}
					
			validate();
			repaint();
		}
	}
	
	// This function just removes all that was in the architecture panel specified
	// and all the necessary elements back
	protected void refreshArchPanels( boolean isOrigPanel, boolean createNewSelector )
	{
		if( isOrigPanel )
		{
			origArchPanel.removeAll( );
			origArchPanel.add( origUrlListPanel );

			// if the original exists, the new one must exist too (it might be the same)
			if( origUrl != null && !origUrl.equals( NO_ARCH_OPEN ) )
			{
				//System.out.println( "refresh orig" );
				if( origSelectorPanel != null )
					origSelectorPanel.removeAll( );
				if( createNewSelector )
				{
					origSelectorPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
					origTypeSelector = new TypeSelector( origUrl, "ComponentType", xArch );
					origStructSelector = new StructureSelector( origUrl, xArch );
				}
				
				if( isStructural )
					origSelectorPanel.add( origStructSelector );
				else
					origSelectorPanel.add( origTypeSelector );
				
				origArchPanel.add( origSelectorPanel );
			}
			else
			{
				// just add a place holder
				origArchPanel.add( origPlaceholder );
			}
		}
		else // update the new one
		{
			newArchPanel.removeAll( );
			newArchPanel.add( newUrlListPanel );
			
			// if the original exists, the new one must exist too (it might be the same)
			if( newUrl != null && !newUrl.equals( NO_ARCH_OPEN ) )
			{
				//System.out.println( "refresh new" );
				if( newSelectorPanel != null )
					newSelectorPanel.removeAll( );
				if( createNewSelector )
				{
					newSelectorPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
					newTypeSelector = new TypeSelector( newUrl, "ComponentType", xArch );
					newStructSelector = new StructureSelector( newUrl, xArch );
				}
				
				if( isStructural )
					newSelectorPanel.add( newStructSelector );
				else
					newSelectorPanel.add( newTypeSelector );
				
				newArchPanel.add( newSelectorPanel );
			}
			else
			{
				// just add a place holder
				newArchPanel.add( newPlaceholder );
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
