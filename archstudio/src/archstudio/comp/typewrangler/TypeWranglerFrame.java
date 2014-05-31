package archstudio.comp.typewrangler;

import archstudio.comp.archipelago.types.BrickTypeThing;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import edu.uci.ics.widgets.*;
import edu.uci.ics.xadlutils.*;
import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TypeWranglerFrame extends JFrame implements ActionListener, ListSelectionListener, XArchFlatListener{
	private static int initialWindowPositionOffset = 1;
	
	protected TypeWranglerC2Component c2Component;
	
	private ObjRef documentSource;
	private XArchFlatTransactionsInterface xarch;
	
	protected java.util.List archipelagoTreePlugins = new ArrayList();
	
	protected JMenuItem miNewWindow;
	protected JMenuItem miHookOpenArchitecture;
	protected JMenuItem miClose;
	
	protected ObjRef currentBrickRef = null;
	protected ObjRef currentBrickTypeRef = null;
	protected MappedData[] currentMaps = null;
	protected SignatureData[] currentSignatures = null;
	protected InterfaceData[] currentInterfaces = null;
	
	public TypeWranglerFrame(TypeWranglerC2Component c2Component,
	XArchFlatTransactionsInterface xarch){
		super();
		archstudio.Branding.brandFrame(this);
		this.c2Component = c2Component;
		this.xarch = xarch;
		
		setWindowTitle(null);
		init();
		repaint();
	}
	
	private void closeWindow(){
		closeDocument();
		this.setVisible(false);
		this.dispose();
	}
	
	protected synchronized void setWindowCursor(Cursor cursor){
		this.setCursor(cursor);
	}
	
	protected synchronized Cursor getWindowCursor(){
		return this.getCursor();
	}
	
	private void init(){
		Toolkit tk = getToolkit();
		Dimension screenSize = tk.getScreenSize();
		double xSize = 790;
		double ySize = 550;
		/*
		//Allow subsequent windows to cascade out a bit (up to 5 steps)
		xPos += (initialWindowPositionOffset * 16);
		yPos += (initialWindowPositionOffset * 16);
		initialWindowPositionOffset++;
		if(initialWindowPositionOffset == 5){
			initialWindowPositionOffset = 0;
		}
		*/
		
		//Set up Menu Bar
		JMenuBar mb = new JMenuBar();
		JMenu mArchitecture = new JMenu("Architecture");
		JMenu mEdit = new JMenu("Edit");
		
		miNewWindow = new JMenuItem("New Window");
		WidgetUtils.setMnemonic(miNewWindow, 'N');
		miNewWindow.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
		miNewWindow.addActionListener(this);
		
		miHookOpenArchitecture = new JMenuItem("Open Architecture...");
		WidgetUtils.setMnemonic(miHookOpenArchitecture, 'O');
		miHookOpenArchitecture.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
		miHookOpenArchitecture.addActionListener(this);
		
		miClose = new JMenuItem("Close Window");
		WidgetUtils.setMnemonic(miClose, 'C');
		miClose.addActionListener(this);
		
		mArchitecture.add(miNewWindow);
		mArchitecture.add(miHookOpenArchitecture);
		mArchitecture.add(new JSeparator());
		mArchitecture.add(miClose);
		
		mb.add(mArchitecture);
		
		this.setJMenuBar(mb);
		
		this.getContentPane().setLayout(new BorderLayout());
		//this.getContentPane().add("Center", splitPane);
		
		documentSource = null;
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent evt){
					closeWindow();
				}
			}
		);
		
		initGUI();
		
		setVisible(true);
		setSize((int)xSize, (int)ySize);
		//setLocation((int)xPos, (int)yPos);
		WidgetUtils.centerInScreen(this);
		setVisible(true);
		invalidate();
		validate();
		paint(getGraphics());
	}
	
	protected JButton bInstance;
	protected JLabel lCurrentType;
	protected JButton bChangeType;
	
	protected DefaultTableModel tmMapped;
	protected JTable tMapped;
	protected JButton bRenameMappedInterface;
	protected JButton bRenameMappedSignature;
	protected JButton bChangeMappedDirection;
	protected JButton bChangeMappedType;
	protected JButton bUnmap;
	protected JLabel lCurrentMapStatus;

	protected DefaultTableModel tmUnmappedInterfaces;
	protected JTable tUnmappedInterfaces;
	protected JButton bNewInterface;
	protected JButton bEditInterface;
	protected JPopupMenu mEditInterface;
	protected JMenuItem miRenameInterface;
	protected JMenuItem miChangeInterfaceDirection;
	protected JMenuItem miChangeInterfaceType;
	
	protected DefaultTableModel tmUnmappedSignatures;
	protected JTable tUnmappedSignatures;
	protected JButton bNewSignature;
	protected JPopupMenu mEditSignature;
	protected JButton bEditSignature;
	protected JMenuItem miRenameSignature;
	protected JMenuItem miChangeSignatureDirection;
	protected JMenuItem miChangeSignatureType;
	
	protected JButton bMap;
	protected JButton bCreateSignature;
	protected JButton bCreateInterface;
	
	protected JButton bClose;
	
	protected static final int MAPTABLE_INTERFACE_COLNUM = 0;
	protected static final int MAPTABLE_INTERFACE_DIRECTION_COLNUM = 1;
	protected static final int MAPTABLE_INTERFACE_TYPE_COLNUM = 2;
	protected static final int MAPTABLE_SIGNATURE_COLNUM = 3;
	protected static final int MAPTABLE_SIGNATURE_DIRECTION_COLNUM = 4;
	protected static final int MAPTABLE_SIGNATURE_TYPE_COLNUM = 5;
	protected static final int MAPTABLE_STATUS_TYPE_COLNUM = 6;
	
	protected static final String[] MAPTABLE_COLUMNS = new String[]{
		"Interface", "Direction", "Type", "Signature", "Direction", "Type", "Status"
	};
	
	protected static final int INTTABLE_INTERFACE_COLNUM = 0;
	protected static final int INTTABLE_INTERFACE_DIRECTION_COLNUM = 1;
	protected static final int INTTABLE_INTERFACE_TYPE_COLNUM = 2;
	
	protected static final String[] INTTABLE_COLUMNS = new String[]{
		"Interface", "Direction", "Type"
	};
	
	protected static final int SIGTABLE_SIGNATURE_COLNUM = 0;
	protected static final int SIGTABLE_SIGNATURE_DIRECTION_COLNUM = 1;
	protected static final int SIGTABLE_SIGNATURE_TYPE_COLNUM = 2;

	protected static final String[] SIGTABLE_COLUMNS = new String[]{
		"Signature", "Direction", "Type"
	};

	public void initGUI(){
		getContentPane().removeAll();
		if(getDocumentSource() == null){
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add("Center", getNothingComponent());
			return;
		}
		
		JPanel instancePanel = new JPanel();
		instancePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		instancePanel.setBorder(new TitledBorder("Choose Instance"));

		bInstance = new JButton("[No Instance Selected]");
		bInstance.addActionListener(this);
		
		instancePanel.add(bInstance);
		instancePanel.add(Box.createHorizontalStrut(10));
		instancePanel.add(new JLabel("Type:"));
		lCurrentType = new JLabel("[No Type]");
		instancePanel.add(lCurrentType);
		instancePanel.add(Box.createHorizontalStrut(10));
		
		bChangeType = new JButton("Change Type...");
		bChangeType.addActionListener(this);
		
		instancePanel.add(bChangeType);
		
		JPanel mappedPanel = new JPanel();
		mappedPanel.setLayout(new BorderLayout());
		mappedPanel.setBorder(new TitledBorder("Mapped Interfaces and Signatures"));
		//mappedPanel.add("North", new JPanelUL(new JLabel("Mapped Signatures and Interfaces")));
		
		tmMapped = new DefaultTableModel();
		for(int i = 0; i < MAPTABLE_COLUMNS.length; i++){
			tmMapped.addColumn(MAPTABLE_COLUMNS[i]);
		}
		
		tMapped = new JTable(tmMapped){
			public boolean isCellEditable(int arg0, int arg1){
				return false;
			}
		};
		tMapped.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tMapped.getSelectionModel().addListSelectionListener(this);
		mappedPanel.add("Center", new JScrollPane(tMapped));
		
		JPanel mappedBottomPanel = new JPanel();
		mappedBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JPanel currentMapStatusPanel = new JPanel();
		currentMapStatusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JLabel lCurrentMapStatusLead = new JLabel("Status:");
		lCurrentMapStatusLead.setFont(WidgetUtils.SANSSERIF_PLAIN_SMALL_FONT);
		currentMapStatusPanel.add(lCurrentMapStatusLead);
		lCurrentMapStatus = new JLabel("");
		lCurrentMapStatus.setFont(WidgetUtils.SANSSERIF_PLAIN_SMALL_FONT);
		currentMapStatusPanel.add(lCurrentMapStatus);
		
		bRenameMappedInterface = new JButton("Rename Interface...");
		bRenameMappedInterface.addActionListener(this);
		mappedBottomPanel.add(bRenameMappedInterface);

		bRenameMappedSignature = new JButton("Rename Signature...");
		bRenameMappedSignature.addActionListener(this);
		mappedBottomPanel.add(bRenameMappedSignature);
		
		bChangeMappedDirection = new JButton("Assign Direction...");
		bChangeMappedDirection.addActionListener(this);
		mappedBottomPanel.add(bChangeMappedDirection);
		
		bChangeMappedType = new JButton("Assign Type...");
		bChangeMappedType.addActionListener(this);
		mappedBottomPanel.add(bChangeMappedType);
		
		bUnmap = new JButton("Unmap");
		bUnmap.addActionListener(this);
		mappedBottomPanel.add(bUnmap);
		
		JPanel mappedBottomPanelWrapper = new JPanel();
		mappedBottomPanelWrapper.setLayout(new BoxLayout(mappedBottomPanelWrapper, BoxLayout.Y_AXIS));
		mappedBottomPanelWrapper.add(currentMapStatusPanel);
		mappedBottomPanelWrapper.add(mappedBottomPanel);
		
		mappedPanel.add("South", mappedBottomPanelWrapper);
		
		JPanel unmappedInterfacesPanel = new JPanel();
		unmappedInterfacesPanel.setLayout(new BorderLayout());
		unmappedInterfacesPanel.setBorder(new TitledBorder("Unmapped Interfaces"));
		
		tmUnmappedInterfaces = new DefaultTableModel();
		for(int i = 0; i < INTTABLE_COLUMNS.length; i++){
			tmUnmappedInterfaces.addColumn(INTTABLE_COLUMNS[i]);
		}
		
		tUnmappedInterfaces = new JTable(tmUnmappedInterfaces){
			public boolean isCellEditable(int arg0, int arg1){
				return false;
			}
		};
		tUnmappedInterfaces.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tUnmappedInterfaces.getSelectionModel().addListSelectionListener(this);
		unmappedInterfacesPanel.add("Center", new JScrollPane(tUnmappedInterfaces));
		
		JPanel unmappedInterfacesBottomPanel = new JPanel();
		unmappedInterfacesBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		bNewInterface = new JButton("New");
		bNewInterface.addActionListener(this);
		
		bEditInterface = new JButton("Edit...");
		bEditInterface.addActionListener(this);

		mEditInterface = new JPopupMenu();
		
		miRenameInterface = new JMenuItem("Change Name...");
		WidgetUtils.setMnemonic(miRenameInterface, 'N');
		miRenameInterface.addActionListener(this);
		
		miChangeInterfaceDirection = new JMenuItem("Change Direction...");
		WidgetUtils.setMnemonic(miChangeInterfaceDirection, 'D');
		miChangeInterfaceDirection.addActionListener(this);
		
		miChangeInterfaceType = new JMenuItem("Change/Assign Type...");
		WidgetUtils.setMnemonic(miChangeInterfaceType, 'T');
		miChangeInterfaceType.addActionListener(this);
		
		mEditInterface.add(miRenameInterface);
		mEditInterface.add(miChangeInterfaceDirection);
		mEditInterface.add(miChangeInterfaceType);

		unmappedInterfacesBottomPanel.add(bNewInterface);
		unmappedInterfacesBottomPanel.add(bEditInterface);
		
		unmappedInterfacesPanel.add("South", unmappedInterfacesBottomPanel);
		
		JPanel mapButtonsPanel = new JPanel();
		mapButtonsPanel.setLayout(new BoxLayout(mapButtonsPanel, BoxLayout.Y_AXIS));

		JPanel gp = new JPanel();
		gp.setLayout(new GridLayout(3, 1));
		
		bMap = new JButton("< Map >");
		bMap.addActionListener(this);
		
		bCreateSignature = new JButton("Create >");
		bCreateSignature.addActionListener(this);
		
		bCreateInterface = new JButton("< Create");
		bCreateInterface.addActionListener(this);
		
		mapButtonsPanel.add(Box.createVerticalStrut(40));
		gp.add(bMap);
		gp.add(bCreateSignature);
		gp.add(bCreateInterface);
		mapButtonsPanel.add(new JPanelUL(gp));
		//mapButtonsPanel.add(Box.createGlue());
		
		JPanel unmappedSignaturesPanel = new JPanel();
		unmappedSignaturesPanel.setLayout(new BorderLayout());
		unmappedSignaturesPanel.setBorder(new TitledBorder("Unmapped Signatures"));
		
		tmUnmappedSignatures = new DefaultTableModel();
		for(int i = 0; i < SIGTABLE_COLUMNS.length; i++){
			tmUnmappedSignatures.addColumn(SIGTABLE_COLUMNS[i]);
		}
		
		tUnmappedSignatures = new JTable(tmUnmappedSignatures){
			public boolean isCellEditable(int arg0, int arg1){
				return false;
			}
		};
		tUnmappedSignatures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tUnmappedSignatures.getSelectionModel().addListSelectionListener(this);
		unmappedSignaturesPanel.add("Center", new JScrollPane(tUnmappedSignatures));
		
		JPanel unmappedSignaturesBottomPanel = new JPanel();
		unmappedSignaturesBottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		bNewSignature = new JButton("New");
		bNewSignature.addActionListener(this);

		bEditSignature = new JButton("Edit...");
		bEditSignature.addActionListener(this);
		
		mEditSignature = new JPopupMenu();

		miRenameSignature = new JMenuItem("Change Name...");
		WidgetUtils.setMnemonic(miRenameSignature, 'N');
		miRenameSignature.addActionListener(this);

		miChangeSignatureDirection = new JMenuItem("Change Direction...");
		WidgetUtils.setMnemonic(miChangeSignatureDirection, 'D');
		miChangeSignatureDirection.addActionListener(this);
		
		miChangeSignatureType = new JMenuItem("Change/Assign Type...");
		WidgetUtils.setMnemonic(miChangeSignatureType, 'T');
		miChangeSignatureType.addActionListener(this);
		
		mEditSignature.add(miRenameSignature);
		mEditSignature.add(miChangeSignatureDirection);
		mEditSignature.add(miChangeSignatureType);
		
		unmappedSignaturesBottomPanel.add(bNewSignature);
		unmappedSignaturesBottomPanel.add(bEditSignature);
		
		unmappedSignaturesPanel.add("South", unmappedSignaturesBottomPanel);
		
		JPanel interfacesAndSignaturesPanel = new JPanel();
		//interfacesAndSignaturesPanel.setLayout(new GridLayout2(1, 3));
		interfacesAndSignaturesPanel.setLayout(new BoxLayout(interfacesAndSignaturesPanel, BoxLayout.X_AXIS));
		interfacesAndSignaturesPanel.add(unmappedInterfacesPanel);
		interfacesAndSignaturesPanel.add(mapButtonsPanel);
		interfacesAndSignaturesPanel.add(unmappedSignaturesPanel);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		bClose = new JButton("Close");
		bClose.addActionListener(this);
		bottomPanel.add(bClose);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel mainWidgetsPanel = new JPanel();
		mainWidgetsPanel.setLayout(new BoxLayout(mainWidgetsPanel, BoxLayout.Y_AXIS));
		
		mainWidgetsPanel.add(instancePanel);
		mainWidgetsPanel.add(mappedPanel);
		mainWidgetsPanel.add(interfacesAndSignaturesPanel);
		
		mainPanel.add("Center", mainWidgetsPanel);
		mainPanel.add("South", bottomPanel);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add("Center", mainPanel);
		
		refreshGUIContents();
		validate();
		repaint();
	}
	
	public void refreshGUIContents(){
		if(getDocumentSource() == null){
			return;
		}
		refreshInstancesPanel();
		refreshTableContents();
		refreshButtonStates();
	}
	
	public void refreshInstancesPanel(){
		bInstance.setText("[No Current Brick]");
		bInstance.setIcon(null);
		bChangeType.setEnabled(false);
		lCurrentType.setIcon(null);
		lCurrentType.setText("(N/A)");
		
		if(currentBrickRef != null){
			String description = XadlUtils.getDescription(xarch, currentBrickRef);
			if(xarch.isInstanceOf(currentBrickRef, "edu.uci.isr.xarch.types.IComponent")){
				if(description == null){
					description = "(Unnamed Component)";
				}
				bInstance.setIcon(Resources.COMPONENT_ICON);
			}
			else if(xarch.isInstanceOf(currentBrickRef, "edu.uci.isr.xarch.types.IConnector")){
				if(description == null){
					description = "(Unnamed Connector)";
				}
				bInstance.setIcon(Resources.CONNECTOR_ICON);
			}
			bInstance.setText(description);

			currentBrickTypeRef = null;
			String typeDescription = null;
			
			currentBrickTypeRef = XadlUtils.resolveXLink(xarch, currentBrickRef, "type");
			if(currentBrickTypeRef != null){
				typeDescription = XadlUtils.getDescription(xarch, currentBrickTypeRef);
				if(typeDescription == null){
					typeDescription = "(Unnamed Type)";
				}
			}
			else{
				typeDescription = "(No type)";
			}

			if(currentBrickTypeRef != null){
				if(xarch.isInstanceOf(currentBrickTypeRef, "edu.uci.isr.xarch.types.IComponentType")){
					lCurrentType.setIcon(Resources.COMPONENT_TYPE_ICON);
				}
				else if(xarch.isInstanceOf(currentBrickTypeRef, "edu.uci.isr.xarch.types.IComponentType")){
					lCurrentType.setIcon(Resources.CONNECTOR_TYPE_ICON);
				}
			}
			lCurrentType.setText(typeDescription);

			bChangeType.setEnabled(true);
		}

		validate();
		repaint();
	}

	static class InterfaceData implements Comparable{
		public ObjRef interfaceRef;
		public String interfaceID;
		public String interfaceDirection;
		public String interfaceDescription;
		public InterfaceTypeData typeData;

		public int compareTo(Object o2){
			return interfaceDescription.compareTo(((InterfaceData)o2).interfaceDescription);
		}
	}
	
	static class SignatureData implements Comparable{
		public ObjRef signatureRef;
		public String signatureID;
		public String signatureDirection;
		public String signatureDescription;
		public InterfaceTypeData typeData;
		public int mappedInterfaceCount = 0;

		public int compareTo(Object o2){
			return signatureDescription.compareTo(((SignatureData)o2).signatureDescription);
		}
	}
	
	static class InterfaceTypeData implements Comparable{
		public ObjRef interfaceTypeRef;
		public String interfaceTypeID;
		public String interfaceTypeDescription;

		public int compareTo(Object o2){
			return interfaceTypeDescription.compareTo(((InterfaceTypeData)o2).interfaceTypeDescription);
		}
	}
	
	static class MappedData implements Comparable{
		public InterfaceData interfaceData;
		public SignatureData signatureData;

		public int compareTo(Object o2){
			return interfaceData.compareTo(((MappedData)o2).interfaceData);
		}
	}
	
	private InterfaceTypeData getInterfaceTypeData(ObjRef thingWithInterfaceTypeRef){
		ObjRef interfaceTypeRef = XadlUtils.resolveXLink(xarch, thingWithInterfaceTypeRef, "type");
		if(interfaceTypeRef != null){
			String interfaceTypeID = XadlUtils.getID(xarch, interfaceTypeRef);
			if(interfaceTypeID != null){
				String interfaceTypeDescription = XadlUtils.getDescription(xarch, interfaceTypeRef);
				if(interfaceTypeDescription == null){
					interfaceTypeDescription = "(Unnamed Interface Type)";
				}
				InterfaceTypeData itd = new InterfaceTypeData();
				itd.interfaceTypeRef = interfaceTypeRef;
				itd.interfaceTypeID = interfaceTypeID;
				itd.interfaceTypeDescription = interfaceTypeDescription;
				return itd;
			}
		}
		return null;
	}
	
	public void refreshTableContents(){
		tUnmappedInterfaces.setEnabled(false);
		tUnmappedSignatures.setEnabled(false);
		tMapped.setEnabled(false);
		currentMaps = new MappedData[0];
		currentSignatures = new SignatureData[0];
		currentInterfaces = new InterfaceData[0];
		
		ArrayList interfaces = new ArrayList();
		ArrayList signatures = new ArrayList();
		ArrayList maps = new ArrayList();
		
		if(currentBrickRef != null){
			tUnmappedInterfaces.setEnabled(true);
			ObjRef[] interfaceRefs = xarch.getAll(currentBrickRef, "interface");
			for(int i = 0; i < interfaceRefs.length; i++){
				String id = XadlUtils.getID(xarch, interfaceRefs[i]);
				if(id != null){
					String description = XadlUtils.getDescription(xarch, interfaceRefs[i]);
					if(description == null){
						description = "(Unnamed Interface)";
					}
					String direction = XadlUtils.getDirection(xarch, interfaceRefs[i]);
					if(direction == null){
						direction = "none";
					}
					InterfaceData ifaceData = new InterfaceData();
					ifaceData.interfaceRef = interfaceRefs[i];
					ifaceData.interfaceID = id;
					ifaceData.interfaceDescription = description;
					ifaceData.interfaceDirection = direction;
					ifaceData.typeData = getInterfaceTypeData(interfaceRefs[i]);
					interfaces.add(ifaceData);
				}
			}
			
			if(currentBrickTypeRef != null){
				tUnmappedSignatures.setEnabled(true);
				tMapped.setEnabled(true);
				ObjRef[] signatureRefs = xarch.getAll(currentBrickTypeRef, "signature");
				for(int i = 0; i < signatureRefs.length; i++){
					String id = XadlUtils.getID(xarch, signatureRefs[i]);
					if(id != null){
						String description = XadlUtils.getDescription(xarch, signatureRefs[i]);
						if(description == null){
							description = "(Unnamed Signature)";
						}
						String direction = XadlUtils.getDirection(xarch, signatureRefs[i]);
						if(direction == null){
							direction = "none";
						}
						SignatureData sigData = new SignatureData();
						sigData.signatureRef = signatureRefs[i];
						sigData.signatureID = id;
						sigData.signatureDescription = description;
						sigData.signatureDirection = direction;
						sigData.typeData = getInterfaceTypeData(signatureRefs[i]);
						signatures.add(sigData);
					}
				}
			}
			
			//OK, we have the data about all the signatures and interfaces.
			//Let's get data about the mappings.
			if(currentBrickTypeRef != null){
				for(Iterator it = interfaces.iterator(); it.hasNext(); ){
					InterfaceData ifaceData = (InterfaceData)it.next();
					ObjRef interfaceRef = ifaceData.interfaceRef;
					ObjRef signatureLinkRef = (ObjRef)xarch.get(interfaceRef, "signature");
					if(signatureLinkRef != null){
						String signatureLinkHref = XadlUtils.getHref(xarch, signatureLinkRef);
						if(signatureLinkHref != null){
							ObjRef mappedSignatureRef = xarch.resolveHref(getDocumentSource(), signatureLinkHref);
							if(mappedSignatureRef != null){
								SignatureData mappedSigData = null;
								for(Iterator it2 = signatures.iterator(); it2.hasNext(); ){
									SignatureData sd = (SignatureData)it2.next();
									if(sd.signatureRef.equals(mappedSignatureRef)){
										mappedSigData = sd;
										break;
									} 
								}
								if(mappedSigData != null){
									MappedData mappedData = new MappedData();
									mappedData.interfaceData = ifaceData;
									mappedData.signatureData = mappedSigData;
									mappedSigData.mappedInterfaceCount++;
									maps.add(mappedData);
								}
							}
						}
					}
				}
			}
			//OK, we have all the mappings generated (if any).
			//If there is no type the signature and
			//mappings lists are empty.
			
			//Let's sort the lists for preparation of insertion in the tables
			Collections.sort(interfaces);
			Collections.sort(signatures);
			Collections.sort(maps);
			
			tmMapped.setRowCount(0);
			tmUnmappedInterfaces.setRowCount(0);
			tmUnmappedSignatures.setRowCount(0);
			
			for(Iterator it = maps.iterator(); it.hasNext(); ){
				MappedData mappedData = (MappedData)it.next();
				String[] mapRow = new String[MAPTABLE_COLUMNS.length];
				
				mapRow[MAPTABLE_INTERFACE_COLNUM] = mappedData.interfaceData.interfaceDescription;
				mapRow[MAPTABLE_INTERFACE_DIRECTION_COLNUM] = mappedData.interfaceData.interfaceDirection;
				if(mappedData.interfaceData.typeData != null){
					mapRow[MAPTABLE_INTERFACE_TYPE_COLNUM] = mappedData.interfaceData.typeData.interfaceTypeDescription;
				}
				else{
					mapRow[MAPTABLE_INTERFACE_TYPE_COLNUM] = "(No type)";
				}
				
				mapRow[MAPTABLE_SIGNATURE_COLNUM] = mappedData.signatureData.signatureDescription;
				mapRow[MAPTABLE_SIGNATURE_DIRECTION_COLNUM] = mappedData.signatureData.signatureDirection;
				if(mappedData.signatureData.typeData != null){
					mapRow[MAPTABLE_SIGNATURE_TYPE_COLNUM] = mappedData.signatureData.typeData.interfaceTypeDescription;
				}
				else{
					mapRow[MAPTABLE_SIGNATURE_TYPE_COLNUM] = "(No type)";
				}
				
				mapRow[MAPTABLE_STATUS_TYPE_COLNUM] = "";
				if(mappedData.interfaceData.typeData == null){
					mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "Interface missing type. ";
				}
				if(mappedData.signatureData.typeData == null){
					mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "Signature missing type. ";
				}
				if(mappedData.signatureData.mappedInterfaceCount > 1){
					mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "Multiple interfaces for signature. ";
				}
				if(!mappedData.interfaceData.interfaceDirection.equals(mappedData.signatureData.signatureDirection)){
					mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "Directions don't match. ";
				}
				if(mappedData.interfaceData.typeData != null){
					if(mappedData.signatureData.typeData != null){
						if(!mappedData.interfaceData.typeData.interfaceTypeRef.equals(mappedData.signatureData.typeData.interfaceTypeRef)){
							mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "Types don't match. ";
						}
					}
				}
				if(mapRow[MAPTABLE_STATUS_TYPE_COLNUM].equals("")){
					mapRow[MAPTABLE_STATUS_TYPE_COLNUM] += "OK";
				}
				//Remove the two halves from the unmapped side
				//of things
				interfaces.remove(mappedData.interfaceData);
				signatures.remove(mappedData.signatureData);
				tmMapped.addRow(mapRow);
			}

			for(Iterator it = signatures.iterator(); it.hasNext(); ){
				SignatureData sigData = (SignatureData)it.next();
				String[] sigRow = new String[SIGTABLE_COLUMNS.length];
				
				sigRow[SIGTABLE_SIGNATURE_COLNUM] = sigData.signatureDescription;
				sigRow[SIGTABLE_SIGNATURE_DIRECTION_COLNUM] = sigData.signatureDirection;
				if(sigData.typeData != null){
					sigRow[SIGTABLE_SIGNATURE_TYPE_COLNUM] = sigData.typeData.interfaceTypeDescription;
				}
				else{
					sigRow[SIGTABLE_SIGNATURE_TYPE_COLNUM] = "(No type)";
				}
				tmUnmappedSignatures.addRow(sigRow);
			}

			for(Iterator it = interfaces.iterator(); it.hasNext(); ){
				InterfaceData ifaceData = (InterfaceData)it.next();
				String[] ifaceRow = new String[INTTABLE_COLUMNS.length];
				
				ifaceRow[INTTABLE_INTERFACE_COLNUM] = ifaceData.interfaceDescription;
				ifaceRow[INTTABLE_INTERFACE_DIRECTION_COLNUM] = ifaceData.interfaceDirection;
				if(ifaceData.typeData != null){
					ifaceRow[INTTABLE_INTERFACE_TYPE_COLNUM] = ifaceData.typeData.interfaceTypeDescription;
				}
				else{
					ifaceRow[INTTABLE_INTERFACE_TYPE_COLNUM] = "(No type)";
				}
				tmUnmappedInterfaces.addRow(ifaceRow);
			}
			
			currentMaps = (MappedData[])maps.toArray(new MappedData[0]);
			currentSignatures = (SignatureData[])signatures.toArray(new SignatureData[0]);
			currentInterfaces = (InterfaceData[])interfaces.toArray(new InterfaceData[0]);
		}
		
		validate();
		repaint();
	}
		
	public void refreshButtonStates(){
		bChangeMappedDirection.setEnabled(false);
		bChangeMappedType.setEnabled(false);
		bRenameMappedInterface.setEnabled(false);
		bRenameMappedSignature.setEnabled(false);
		bUnmap.setEnabled(false);
		bMap.setEnabled(false);
		bCreateInterface.setEnabled(false);
		bCreateSignature.setEnabled(false);
		bNewInterface.setEnabled(false);
		bNewSignature.setEnabled(false);
		bEditInterface.setEnabled(false);
		bEditSignature.setEnabled(false);
		lCurrentMapStatus.setText("N/A");
		
		if(currentBrickRef != null){
			bNewInterface.setEnabled(true);
		}
		if(currentBrickTypeRef != null){
			bNewSignature.setEnabled(true);
		}
		if(tMapped.getSelectedRowCount() != 0){
			bRenameMappedInterface.setEnabled(true);
			bRenameMappedSignature.setEnabled(true);
			bChangeMappedDirection.setEnabled(true);
			bChangeMappedType.setEnabled(true);
			bUnmap.setEnabled(true);
			String currentStatus = (String)tmMapped.getValueAt(tMapped.getSelectedRow(), MAPTABLE_STATUS_TYPE_COLNUM);
			lCurrentMapStatus.setText(currentStatus);
		}
		if(tUnmappedInterfaces.getSelectedRowCount() != 0){
			if(currentBrickTypeRef != null){
				bCreateSignature.setEnabled(true);
			}
			bEditInterface.setEnabled(true);
		}
		if(tUnmappedSignatures.getSelectedRowCount() != 0){
			bCreateInterface.setEnabled(true);
			bEditSignature.setEnabled(true);
		}
		if(tUnmappedInterfaces.getSelectedRowCount() != 0){
			if(tUnmappedSignatures.getSelectedRowCount() != 0){
				bMap.setEnabled(true);
			}
		}
		validate();
		repaint();
	}
	
	public void valueChanged(ListSelectionEvent evt){
		refreshButtonStates();
	}

	
	public static JComponent getNothingComponent(){
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		p.add(new JLabel("No architecture open. Open an architecture to begin."));
		return p;
	}

	public void closeDocument(){
		getContentPane().removeAll();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("Center", getNothingComponent());
		setWindowTitle(null);
		documentSource = null;
		validate();
		repaint();
	}
	
	public ObjRef getDocumentSource(){
		return documentSource;
	}
	
	public void setWindowTitle(String openFile){
		StringBuffer title = new StringBuffer();
		title.append(TypeWranglerC2Component.PRODUCT_NAME);
		if(openFile == null){
			title.append(" - [None]");
		}
		else{
			title.append(" - [").append(openFile).append("]");
		}
		setTitle(title.toString());
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(getDocumentSource() == null){
			return;
		}
		
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null) sourcePathString = sourcePath.toTagsOnlyString();
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null) targetPathString = targetPath.toTagsOnlyString();

		//System.out.println("sourcePathString: " + sourcePathString);
		//System.out.println("targetPathString: " + targetPathString);

		if((evt.getEventType() == XArchFlatEvent.CLEAR_EVENT) || (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT)){
			if((sourcePathString != null) && (sourcePathString.equals("xArch/archStructure"))){
				if((targetPathString != null) && (targetPathString.equals("component"))){
					ObjRef removedComponent = (ObjRef)evt.getTarget();
					if(removedComponent != null){
						if(removedComponent.equals(currentBrickRef)){
							closeDocument();
						}
					}
				}
				else if((targetPathString != null) && (targetPathString.equals("connector"))){
					ObjRef removedConnector = (ObjRef)evt.getTarget();
					if(removedConnector != null){
						if(removedConnector.equals(currentBrickRef)){
							closeDocument();
						}
					}
				}
				else if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes"))){
					if((targetPathString != null) && (targetPathString.equals("componentType"))){
						refreshGUIContents();
						return;
					}
					else if((targetPathString != null) && (targetPathString.equals("connectorType"))){
						refreshGUIContents();
						return;
					}
				}
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archStructure/component"))){
				refreshGUIContents();
				return;
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archStructure/connector"))){
				refreshGUIContents();
				return;
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/componentType"))){
				refreshGUIContents();
				return;
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/connectorType"))){
				refreshGUIContents();
				return;
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/interfaceType"))){
				refreshGUIContents();
				return;
			}
		}
		
		//System.out.println("sourcePath: " + sourcePathString);
		//System.out.println("targetPath: " + targetPathString);
		
		if(targetPathString == null){
			targetPathString = sourcePathString;
		}
		
		if((targetPathString != null) && (targetPathString.startsWith("xArch/archStructure/component"))){
			refreshGUIContents();
			return;
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archStructure/connector"))){
			refreshGUIContents();
			return;
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/componentType"))){
			refreshGUIContents();
			return;
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/connectorType"))){
			refreshGUIContents();
			return;
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/interfaceType"))){
			refreshGUIContents();
			return;
		}
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
	}

	public void openXArch(ObjRef xArchRef){
		setDocumentSource(xArchRef);
		String uri = xarch.getXArchURI(xArchRef);
		setWindowTitle(uri);
		initGUI();
	}
	
	//protected HashMap renderingHintsCache = new HashMap();
	
	public void openXArch(ObjRef xArchRef, ObjRef elementRef){
		openXArch(xArchRef);
		showElement(elementRef);
	}
	
	private void showElement(ObjRef elementRef){
	  if(xarch.isInstanceOf(elementRef, "edu.uci.isr.xarch.types.IComponent")){
			setCurrentBrick(elementRef);
	  }
	  else if(xarch.isInstanceOf(elementRef, "edu.uci.isr.xarch.types.IConnector")){
			setCurrentBrick(elementRef);
	  }
	}
	
	public void showID(String id){
		if(getDocumentSource() == null){
			return;
		}
		ObjRef ref = xarch.getByID(getDocumentSource(), id);
		showElement(ref);
	}
	
	public void actionPerformed(ActionEvent evt){
		ObjRef xArchRef = getDocumentSource();
		if(evt.getSource() == miClose){
			closeWindow();
		}
		else if(evt.getSource() == miHookOpenArchitecture){
			handleHookOpenArchitecture();
		}
		else if(evt.getSource() == miNewWindow){
			c2Component.newWindow();
		}
		else if(evt.getSource() == bInstance){
			StructureSelectorDialog bsd = new StructureSelectorDialog(this, xarch, xArchRef, 
				StructureSelectorDialog.SHOW_COMPONENTS | 
				StructureSelectorDialog.SHOW_CONNECTORS |
				StructureSelectorDialog.SELECTABLE_COMPONENTS | 
				StructureSelectorDialog.SELECTABLE_CONNECTORS);
			bsd.doPopup();
			ObjRef resultRef = bsd.getResult();
			if(resultRef != null){
				setCurrentBrick(resultRef);
			}
		}
		else if(evt.getSource() == bChangeType){
			doChangeType();
		}
		else if(evt.getSource() == bNewInterface){
			doNewInterface();
		}
		else if(evt.getSource() == bEditInterface){
			Rectangle bounds = bEditInterface.getBounds();
			mEditInterface.show(bEditInterface.getParent(), bounds.x, bounds.y + bounds.height);
		}
		else if(evt.getSource() == miRenameInterface){
			doRenameInterface();
		}
		else if(evt.getSource() == miChangeInterfaceDirection){
			doChangeInterfaceDirection();
		}
		else if(evt.getSource() == miChangeInterfaceType){
			doChangeInterfaceType();
		}
		else if(evt.getSource() == bNewSignature){
			doNewSignature();
		}
		else if(evt.getSource() == bEditSignature){
			Rectangle bounds = bEditSignature.getBounds();
			mEditSignature.show(bEditSignature.getParent(), bounds.x, bounds.y + bounds.height);
		}
		else if(evt.getSource() == miRenameSignature){
			doRenameSignature();
		}
		else if(evt.getSource() == miChangeSignatureDirection){
			doChangeSignatureDirection();
		}
		else if(evt.getSource() == miChangeSignatureType){
			doChangeSignatureType();
		}
		else if(evt.getSource() == bChangeMappedDirection){
			doChangeMappedDirection();
		}
		else if(evt.getSource() == bChangeMappedType){
			doChangeMappedType();
		}
		else if(evt.getSource() == bCreateInterface){
			doCreateInterface();
		}
		else if(evt.getSource() == bCreateSignature){
			doCreateSignature();
		}
		else if(evt.getSource() == bMap){
			doCreateMapping();
		}
		else if(evt.getSource() == bUnmap){
			doUnmap();
		}
		else if(evt.getSource() == bRenameMappedInterface){
			doRenameMappedInterface();
		}
		else if(evt.getSource() == bRenameMappedSignature){
			doRenameMappedSignature();
		}
		else if(evt.getSource() == bClose){
			closeWindow();
		}
	}
	
	public void doNewInterface(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef newInterfaceRef = xarch.create(typesContextRef, "interface");
		xarch.set(newInterfaceRef, "id", c2.util.UIDGenerator.generateUID("interface"));
		XadlUtils.setDescription(xarch, newInterfaceRef, "(New Interface)");
		xarch.add(currentBrickRef, "interface", newInterfaceRef);
	}
	
	public void doUnmap(){
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		
		int selectedRow = tMapped.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		MappedData currentMap = currentMaps[selectedRow];
		xarch.clear(currentMap.interfaceData.interfaceRef, "signature");
	}
	
	public void doCreateSignature(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		
		int selectedRow = tUnmappedInterfaces.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		InterfaceData currentInterface = currentInterfaces[selectedRow];
		
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef newSignatureRef = xarch.create(typesContextRef, "signature");
		String newSignatureID = c2.util.UIDGenerator.generateUID("signature");
		xarch.set(newSignatureRef, "id", newSignatureID);
		String newSignatureDescription = "(New Signature)";
		if(currentInterface.interfaceDescription != null){
			newSignatureDescription = guessGoodSignatureDescription(currentInterface.interfaceDescription);
		}
		XadlUtils.setDescription(xarch, newSignatureRef, newSignatureDescription); 
		
		if(currentInterface.interfaceDirection != null){
			XadlUtils.setDirection(xarch, newSignatureRef, currentInterface.interfaceDirection);
		}

		if(currentInterface.typeData != null){
			setInterfaceType(newSignatureRef, currentInterface.typeData.interfaceTypeRef);
		}
		
		xarch.add(currentBrickTypeRef, "signature", newSignatureRef);
		SignatureData sigData = new SignatureData();
		sigData.signatureRef = newSignatureRef;
		sigData.signatureID = newSignatureID;
		sigData.signatureDescription = newSignatureDescription;
		createMapping(currentInterface, sigData);
	}
	
	public void doCreateInterface(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		
		int selectedRow = tUnmappedSignatures.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		SignatureData currentSignature = currentSignatures[selectedRow];
		
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef newInterfaceRef = xarch.create(typesContextRef, "interface");
		String newInterfaceID = c2.util.UIDGenerator.generateUID("interface");
		xarch.set(newInterfaceRef, "id", newInterfaceID);
		String newInterfaceDescription = "(New Interface)";
		if(currentSignature.signatureDescription != null){
			newInterfaceDescription = guessGoodInterfaceDescription(currentSignature.signatureDescription);
		}
		
		XadlUtils.setDescription(xarch, newInterfaceRef, newInterfaceDescription);
		xarch.add(currentBrickRef, "interface", newInterfaceRef);
		
		if(currentSignature.signatureDirection != null){
			XadlUtils.setDirection(xarch, newInterfaceRef, currentSignature.signatureDirection);
		}
		
		if(currentSignature.typeData != null){
			setInterfaceType(newInterfaceRef, currentSignature.typeData.interfaceTypeRef);
		}
		
		InterfaceData ifaceData = new InterfaceData();
		ifaceData.interfaceRef = newInterfaceRef;
		ifaceData.interfaceID = newInterfaceID;
		ifaceData.interfaceDescription = newInterfaceDescription;
		createMapping(ifaceData, currentSignature);
	}
	
	public void doCreateMapping(){
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}

		int selectedRow = tUnmappedInterfaces.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		InterfaceData currentInterface = currentInterfaces[selectedRow];

		selectedRow = tUnmappedSignatures.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		SignatureData currentSignature = currentSignatures[selectedRow];
		createMapping(currentInterface, currentSignature);
	}
	
	private void createMapping(InterfaceData ifaceData, SignatureData sigData){
		ObjRef interfaceRef = ifaceData.interfaceRef;
		
		XadlUtils.setXLink(xarch, interfaceRef, "signature", sigData.signatureID);
	}
	
	public void doChangeInterfaceType(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		int selectedRow = tUnmappedInterfaces.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		InterfaceData currentInterface = currentInterfaces[selectedRow];

		TypeSelectorDialog tsd = new TypeSelectorDialog(this, xarch, xArchRef, TypeSelectorDialog.INTERFACE_TYPES);
		tsd.doPopup();
		ObjRef newInterfaceType = tsd.getSelectedTypeRef();
		if(newInterfaceType == null){
			return;
		}
		setInterfaceType(currentInterface.interfaceRef, newInterfaceType);
	}
	
	public void doChangeSignatureType(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickTypeRef == null){
			return;
		}
		int selectedRow = tUnmappedSignatures.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		SignatureData currentSignature = currentSignatures[selectedRow];
		TypeSelectorDialog tsd = new TypeSelectorDialog(this, xarch, xArchRef, TypeSelectorDialog.INTERFACE_TYPES);
		tsd.doPopup();
		ObjRef newInterfaceType = tsd.getSelectedTypeRef();
		if(newInterfaceType == null){
			return;
		}
		setInterfaceType(currentSignature.signatureRef, newInterfaceType);
	}
	
	public void doChangeMappedDirection(){
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		int selectedRow = tMapped.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		MappedData currentMap = currentMaps[selectedRow];
		changeMappedDirection(currentMap);
	}
	
	public void doChangeMappedType(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		int selectedRow = tMapped.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		MappedData currentMap = currentMaps[selectedRow];
		
		InterfaceData currentInterface = currentMap.interfaceData;
		SignatureData currentSignature = currentMap.signatureData;

		TypeSelectorDialog tsd = new TypeSelectorDialog(this, xarch, xArchRef, TypeSelectorDialog.INTERFACE_TYPES);
		tsd.doPopup();
		ObjRef newInterfaceType = tsd.getSelectedTypeRef();
		if(newInterfaceType == null){
			return;
		}
		setInterfaceType(currentInterface.interfaceRef, newInterfaceType);
		setInterfaceType(currentSignature.signatureRef, newInterfaceType);
	}

	private void setInterfaceType(ObjRef ref, ObjRef newInterfaceType){
		String newInterfaceTypeID = XadlUtils.getID(xarch, newInterfaceType);
		if(newInterfaceTypeID == null){
			JOptionPane.showMessageDialog(this, "Type has no ID.", "Can't set type.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		XadlUtils.setXLink(xarch, ref, "type", newInterfaceTypeID);
	}
	
	public void doRenameInterface(){
		if(currentBrickRef == null){
			return;
		}
		int selectedRow = tUnmappedInterfaces.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		InterfaceData currentInterface = currentInterfaces[selectedRow];
		renameInterface(currentInterface);
	}
	
	public void doChangeInterfaceDirection(){
		if(currentBrickRef == null){
			return;
		}
		int selectedRow = tUnmappedInterfaces.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		InterfaceData currentInterface = currentInterfaces[selectedRow];
		changeInterfaceDirection(currentInterface);
	}
	
	public void doRenameMappedInterface(){
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		
		int selectedRow = tMapped.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		MappedData currentMap = currentMaps[selectedRow];
		renameInterface(currentMap.interfaceData);
	}
	
	private void renameInterface(InterfaceData currentInterface){
		String newDescription = JOptionPane.showInputDialog(this, "Rename Interface", currentInterface.interfaceDescription); 
		if(newDescription == null){
			return;
		}
		XadlUtils.setDescription(xarch, currentInterface.interfaceRef, newDescription);
	}
		
	private void changeMappedDirection(MappedData currentMap){
		Object[] possibleValues = { "none", "in", "out", "inout" };
		String newDirection = (String)JOptionPane.showInputDialog(this, 
			"New Direction", "Change Direction",
			JOptionPane.QUESTION_MESSAGE, null,
			possibleValues, possibleValues[0]);

		if(newDirection == null){
			return;
		}
		setInterfaceDirection(currentMap.interfaceData, newDirection);
		setSignatureDirection(currentMap.signatureData, newDirection);
	}
	
	private void changeInterfaceDirection(InterfaceData currentInterface){
		Object[] possibleValues = { "none", "in", "out", "inout" };
		String newDirection = (String)JOptionPane.showInputDialog(this, 
			"New Direction", "Change Interface Direction",
			JOptionPane.QUESTION_MESSAGE, null,
			possibleValues, possibleValues[0]);

		if(newDirection == null){
			return;
		}
		setInterfaceDirection(currentInterface, newDirection);
	}
	
	private void setInterfaceDirection(InterfaceData currentInterface, String newDirection){
		XadlUtils.setDirection(xarch, currentInterface.interfaceRef, newDirection);
	}
	
	public void doRenameSignature(){
		if(currentBrickTypeRef == null){
			return;
		}
		int selectedRow = tUnmappedSignatures.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		SignatureData currentSignature = currentSignatures[selectedRow];
		renameSignature(currentSignature);
	}
	
	public void doRenameMappedSignature(){
		if(currentBrickRef == null){
			return;
		}
		if(currentBrickTypeRef == null){
			return;
		}
		
		int selectedRow = tMapped.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		MappedData currentMap = currentMaps[selectedRow];
		renameSignature(currentMap.signatureData);
	}
	
	public void doChangeSignatureDirection(){
		if(currentBrickTypeRef == null){
			return;
		}
		int selectedRow = tUnmappedSignatures.getSelectedRow();
		if(selectedRow == -1){
			return;
		}
		SignatureData currentSignature = currentSignatures[selectedRow];
		changeSignatureDirection(currentSignature);
	}
	
	private void renameSignature(SignatureData currentSignature){
		String newDescription = JOptionPane.showInputDialog(this, "Rename Signature", currentSignature.signatureDescription); 
		if(newDescription == null){
			return;
		}
		
		XadlUtils.setDescription(xarch, currentSignature.signatureRef, newDescription); 
	}
	
	private void changeSignatureDirection(SignatureData currentSignature){
		Object[] possibleValues = { "none", "in", "out", "inout" };
		String newDirection = (String)JOptionPane.showInputDialog(this, 
			"New Direction", "Change Interface Direction",
			JOptionPane.QUESTION_MESSAGE, null,
			possibleValues, possibleValues[0]);

		if(newDirection == null){
			return;
		}
		setSignatureDirection(currentSignature, newDirection);
	}
	
	private void setSignatureDirection(SignatureData currentSignature, String newDirection){
		XadlUtils.setDirection(xarch, currentSignature.signatureRef, newDirection);
	}
	
	public void doNewSignature(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickTypeRef == null){
			return;
		}
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef newSignatureRef = xarch.create(typesContextRef, "signature");
		xarch.set(newSignatureRef, "id", c2.util.UIDGenerator.generateUID("signature"));
		XadlUtils.setDescription(xarch, newSignatureRef, "(New Signature)");
		xarch.add(currentBrickTypeRef, "signature", newSignatureRef);
	}
	
	public void doChangeType(){
		ObjRef xArchRef = getDocumentSource();
		if(currentBrickRef == null){
			return;
		}
		
		int typeMask;
		if(xarch.isInstanceOf(currentBrickRef, "edu.uci.isr.xarch.types.IComponent")){
			typeMask = TypeSelectorDialog.COMPONENT_TYPES;
		}
		else if(xarch.isInstanceOf(currentBrickRef, "edu.uci.isr.xarch.types.IConnector")){
			typeMask = TypeSelectorDialog.CONNECTOR_TYPES;
		}
		else{
			return;
		}
		
		TypeSelectorDialog tsd = new TypeSelectorDialog(this, xarch, xArchRef, typeMask);
		tsd.doPopup();
		ObjRef newTypeRef = tsd.getSelectedTypeRef();
		if(newTypeRef == null){
			return;
		}
		String newTypeXArchID = XadlUtils.getID(xarch, newTypeRef);
		if(newTypeXArchID == null){
			JOptionPane.showMessageDialog(this, "Type has no ID.", "Can't set type.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		XadlUtils.setXLink(xarch, currentBrickRef, "type", newTypeXArchID);
		refreshGUIContents();
	}
	
	public void setCurrentBrick(ObjRef brickRef){
		this.currentBrickRef = brickRef;
		refreshGUIContents();
	}
	
	public void handleHookOpenArchitecture(){
		String[] urls = xarch.getOpenXArchURIs();
		if(urls.length == 0){
			JOptionPane.showMessageDialog(this, "No architectures open.", "No Architectures", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String result = (String)JOptionPane.showInputDialog(this, "Choose an Architecture", "Choose an Architecture", 
			JOptionPane.QUESTION_MESSAGE, null, urls, urls[0]);

		if(result == null){
			return;
		}
		if(result.equals(getDocumentSource())){
			//Document is already open in this window
			return;
		}
		ObjRef xArchRef = xarch.getOpenXArch(result);
		if(xArchRef != null){
			TypeWranglerFrame otherFrame = c2Component.getWindow(xArchRef);
			if(otherFrame != null){
				//It's already open in another window
				otherFrame.requestFocus();
				return;
			}
	
			try{
				closeDocument();
				openXArch(xArchRef);
				validate();
				repaint();
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
		}
	}
	
	public void setDocumentSource(ObjRef xArchRef){
		this.documentSource = xArchRef;
	}
	
	private static String replaceSuffix(String s, String oldSuffix, String newSuffix){
		if(s.endsWith(oldSuffix)){
			s = s.substring(0, s.length() - oldSuffix.length());
			return s + newSuffix;
		}
		return null;
	}
	
	public static String guessGoodSignatureDescription(String interfaceName){
		String signatureName = interfaceName;
		
		String s = null;
		if((s = replaceSuffix(signatureName, "iface", "sig")) != null) return s;
		if((s = replaceSuffix(signatureName, "Iface", "Sig")) != null) return s;
		if((s = replaceSuffix(signatureName, "IFACE", "SIG")) != null) return s;
		if((s = replaceSuffix(signatureName, "intf", "sig")) != null) return s;
		if((s = replaceSuffix(signatureName, "Intf", "Sig")) != null) return s;
		if((s = replaceSuffix(signatureName, "INTF", "SIG")) != null) return s;
		if((s = replaceSuffix(signatureName, "interface", "signature")) != null) return s;
		if((s = replaceSuffix(signatureName, "Interface", "Signature")) != null) return s;
		if((s = replaceSuffix(signatureName, "INTERFACE", "SIGNATURE")) != null) return s;
		return signatureName;
	}

	public static String guessGoodInterfaceDescription(String signatureName){
		String interfaceName = signatureName;
		
		String s = null;
		if((s = replaceSuffix(signatureName, "sig", "iface")) != null) return s;
		if((s = replaceSuffix(signatureName, "Sig", "Iface")) != null) return s;
		if((s = replaceSuffix(signatureName, "SIG", "IFACE")) != null) return s;
		if((s = replaceSuffix(signatureName, "signature", "interface")) != null) return s;
		if((s = replaceSuffix(signatureName, "Signature", "Interface")) != null) return s;
		if((s = replaceSuffix(signatureName, "SIGNATURE", "INTERFACE")) != null) return s;
		return interfaceName;
	}

}