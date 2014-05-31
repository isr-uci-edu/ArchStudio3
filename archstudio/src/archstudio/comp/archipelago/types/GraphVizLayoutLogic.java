package archstudio.comp.archipelago.types;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import archstudio.comp.archipelago.IHinted;
import archstudio.comp.graphlayout.CantFindGraphLayoutToolException;
import archstudio.comp.graphlayout.GraphLayout;
import archstudio.comp.graphlayout.GraphParameters;
import archstudio.comp.graphlayout.IGraphLayout;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.widgets.*;
import edu.uci.ics.xarchutils.ObjRef;

public class GraphVizLayoutLogic extends AbstractMainMenuLogic implements ActionListener{
	
	protected JMenuBar mainMenu;
	protected JMenu editMenu;
	protected JMenuItem miDoDotLayout;
	protected JMenuItem miDoNeatoLayout;
	protected JSeparator miSeparator;
	protected IGraphLayout gli;
	protected ObjRef structureRef;
	
	public GraphVizLayoutLogic(JMenuBar mainMenu, IGraphLayout gli, ObjRef structureRef){
		super(mainMenu);
		
		this.mainMenu = mainMenu;
		
		this.gli = gli;
		this.structureRef = structureRef;
		
		editMenu = WidgetUtils.getSubMenu(mainMenu, "Edit");
		if(editMenu == null){
			editMenu = new JMenu("Edit");
			WidgetUtils.setMnemonic(editMenu, 'E');
			mainMenu.add(editMenu);
		}
		
		miDoDotLayout = new JMenuItem("Auto-Layout using Dot");
		//miDoDotLayout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
		miDoDotLayout.addActionListener(this);
		
		miDoNeatoLayout = new JMenuItem("Auto-Layout using Neato");
		//miDoDotLayout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
		miDoNeatoLayout.addActionListener(this);
		
		editMenu.add(miDoDotLayout);
		editMenu.add(miDoNeatoLayout);
		miSeparator = new JSeparator();
		editMenu.add(miSeparator);
	}

	public void destroy(){
		editMenu.remove(miDoDotLayout);
		editMenu.remove(miDoNeatoLayout);
		editMenu.remove(miSeparator);
		if(editMenu.getItemCount() == 0){
			mainMenu.remove(editMenu);
		}
	}
	
	private static Thing findThing(BNAModel m, String findID){
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			String xArchID = (String)t.getProperty(IHinted.XARCHID_PROPERTY_NAME);
			if((xArchID != null) && (xArchID.equals(findID))){
				return t;
			}
		}
		return null;
	}
	
	private Point getAveragePoint(Point[] points){
		int xsum = 0; int ysum = 0;
		for(int i = 0; i < points.length; i++){
			xsum += points[i].x;
			ysum += points[i].y;
		}
		xsum /= points.length;
		ysum /= points.length;
		return new Point(xsum, ysum);
	}
	
	private static String[] getCompassPortIds(GraphLayout.AbstractPort[] ports, int direction){
		java.util.List ids = new ArrayList();
		for(int i = 0; i < ports.length; i++){
			if(ports[i] instanceof GraphLayout.CompassPort){
				GraphLayout.CompassPort cp = (GraphLayout.CompassPort)ports[i];
				if(cp.getDirection() == direction){
					ids.add(cp.getId());
				}
			}
		}
		return (String[])ids.toArray(new String[0]);
	}
	
	private static String[] getPlainPortIds(GraphLayout.AbstractPort[] ports){
		java.util.List ids = new ArrayList();
		for(int i = 0; i < ports.length; i++){
			if(ports[i] instanceof GraphLayout.PlainPort){
				ids.add(ports[i].getId());
			}
		}
		return (String[])ids.toArray(new String[0]);
	}
	
	public void actionPerformed(ActionEvent e){
		int selectedTool = IGraphLayout.TOOL_DOT;
		if(e.getSource() == miDoDotLayout){
			selectedTool = IGraphLayout.TOOL_DOT;
		}
		else if(e.getSource() == miDoNeatoLayout){
			selectedTool = IGraphLayout.TOOL_NEATO;
		}
		
		final int tool = selectedTool;
		final BNAComponent bna = getBNAComponent();
		if(bna == null){
			return;
		}
		
		final GraphParameters gp = GraphVizLayoutParametersDialog.showDialog(WidgetUtils.getAncestorFrame(bna), tool);
		if(gp == null){
			//User hit cancel
			return;
		}
		
		final ProgressDialog pd = new ProgressDialog(WidgetUtils.getAncestorFrame(bna), "Performing Auto-Layout", null);
		final JProgressBar progressBar = pd.getProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		
		Thread layoutThread = new Thread(){
			public void run(){
				//System.out.println("Running DOT");
				progressBar.setValue(5);
				progressBar.setString("Calculating Layout");
				GraphLayout gl = null;
				try{
					gl = gli.doLayout(tool, structureRef, gp);
				}
				catch(CantFindGraphLayoutToolException cfde){
					JOptionPane.showMessageDialog(null, cfde.getMessage(), "Can't find Layout Tool", JOptionPane.ERROR_MESSAGE);
					pd.doDone();
					return;
				}
				//System.out.println("Back");
		
				BNAModel model = bna.getModel();
				try{
					model.beginBulkChange();
					
					//Render at local origin so it doesn't get lost.
					int ox = bna.getCoordinateMapper().localXtoWorldX(0);
					int oy = bna.getCoordinateMapper().localYtoWorldY(0);
	
					boolean moveInterfaces = true;
					Boolean dontMoveInterfacesBool = (Boolean)gp.getProperty("dontmoveinterfaces");
					if((dontMoveInterfacesBool != null) && (dontMoveInterfacesBool.booleanValue() == true)){
						moveInterfaces = false;
					}
			
					progressBar.setValue(50);
					progressBar.setString("Laying out Bricks");
					
					int numNodes = gl.getNumNodes();
					for(int i = 0; i < numNodes; i++){
						//System.out.println("Looking for node # " + i);
						GraphLayout.Node node = gl.getNodeAt(i);
						String nodeId = node.getNodeId();
						//System.out.println("Node id = " + nodeId);
						for(Iterator it = model.getThingIterator(); it.hasNext(); ){
							Thing t = (Thing)it.next();
							String xArchID = (String)t.getProperty(IHinted.XARCHID_PROPERTY_NAME);
							if((xArchID != null) && (xArchID.equals(nodeId))){
								//Found it.
								//System.out.println("found it.");
								Rectangle newBounds = node.getBounds();
								newBounds.translate(ox, oy);
								//System.out.println("setting bounds on " + nodeId + " to " + newBounds);
								if(t instanceof IResizableBoxBounded){
									((IResizableBoxBounded)t).setBoundingBox(newBounds);
								}
						
								if(moveInterfaces){
									//System.out.println("we're moving interfaces!");
									//System.out.println("the brick " + nodeId);
									String[] northList = getCompassPortIds(node.getAllPorts(), GraphLayout.CompassPort.NORTH);
									String[] southList = getCompassPortIds(node.getAllPorts(), GraphLayout.CompassPort.SOUTH);
									String[] eastList = getCompassPortIds(node.getAllPorts(), GraphLayout.CompassPort.EAST);
									String[] westList = getCompassPortIds(node.getAllPorts(), GraphLayout.CompassPort.WEST);
									String[] unknownList = getPlainPortIds(node.getAllPorts());
							
									//System.out.println("northlist = " + c2.util.ArrayUtils.arrayToString(northList));
									//System.out.println("southlist = " + c2.util.ArrayUtils.arrayToString(southList));
									//System.out.println("eastlist = " + c2.util.ArrayUtils.arrayToString(eastList));
									//System.out.println("westlist = " + c2.util.ArrayUtils.arrayToString(westList));
									//System.out.println("unknownlist = " + c2.util.ArrayUtils.arrayToString(unknownList));
							
									for(int j = 0; j < northList.length; j++){
										String interfaceId = northList[j];
										//System.out.println("interface " + interfaceId);
										Thing interfaceThing = findThing(model, interfaceId);
										//System.out.println("found it");
										if((interfaceThing != null) && (interfaceThing instanceof EndpointThing)){
											double percentOffset = ((double)j + 1.0d) / ((double)northList.length + 1.0d);
											double offsetd = newBounds.width * percentOffset;
											int x = newBounds.x + (int)Math.round(offsetd);
									
											//System.out.println("bounds = " + newBounds);
											//System.out.println("x = " + x); 
											MoveEndpointLogic.moveEndpointTo((EndpointThing)interfaceThing, newBounds, x, newBounds.y);
											//System.out.println("iface bounds = " + ((EndpointThing)interfaceThing).getBoundingBox());
										}
									}
	
									//System.out.println("southlist = " + c2.util.ArrayUtils.arrayToString(southList));
							
									for(int j = 0; j < southList.length; j++){
										String interfaceId = southList[j];
										Thing interfaceThing = findThing(model, interfaceId);
										if((interfaceThing != null) && (interfaceThing instanceof EndpointThing)){
											double percentOffset = ((double)j + 1.0d) / ((double)southList.length + 1.0d);
											double offsetd = newBounds.width * percentOffset;
											int x = newBounds.x + (int)Math.round(offsetd); 
											//System.out.println("bounds = " + newBounds);
											//System.out.println("x = " + x); 
											MoveEndpointLogic.moveEndpointTo((EndpointThing)interfaceThing, newBounds, x, newBounds.y + newBounds.height);
											//System.out.println("iface bounds = " + ((EndpointThing)interfaceThing).getBoundingBox());
										}
									}
	
									///---------------
									for(int j = 0; j < westList.length; j++){
										String interfaceId = westList[j];
										Thing interfaceThing = findThing(model, interfaceId);
										if((interfaceThing != null) && (interfaceThing instanceof EndpointThing)){
											double percentOffset = ((double)j + 1.0d) / ((double)westList.length + 1.0d);
											double offsetd = newBounds.height * percentOffset;
											int y = newBounds.y + (int)Math.round(offsetd); 
											MoveEndpointLogic.moveEndpointTo((EndpointThing)interfaceThing, newBounds, newBounds.x, y);
										}
									}
	
									for(int j = 0; j < eastList.length; j++){
										String interfaceId = eastList[j];
										Thing interfaceThing = findThing(model, interfaceId);
										if((interfaceThing != null) && (interfaceThing instanceof EndpointThing)){
											double percentOffset = ((double)j + 1.0d) / ((double)eastList.length + 1.0d);
											double offsetd = newBounds.height * percentOffset;
											int y = newBounds.y + (int)Math.round(offsetd); 
											MoveEndpointLogic.moveEndpointTo((EndpointThing)interfaceThing, newBounds, newBounds.x + newBounds.width, y);
										}
									}
							
									//Unknowns are handled funny...since we don't know where to put them,
									//and DOT won't necessarily all point the edges at the same point (even
									//if they share a port), we need to sort of average the endpoints and
									//guess.
									for(int j = 0; j < unknownList.length; j++){
										String interfaceId = unknownList[j];
										//System.out.println("Unknown list had an interface:" + interfaceId);
										Thing interfaceThing = findThing(model, interfaceId);
										if((interfaceThing != null) && (interfaceThing instanceof EndpointThing)){
											//Iterate through the edges
									
											ArrayList pointList = new ArrayList();
									
											int numEdges = gl.getNumEdges();
											for(int k = 0; k < numEdges; k++){
												GraphLayout.Edge edge = gl.getEdgeAt(k);
												String port1id = edge.getPort1Id();
												String port2id = edge.getPort2Id();
												if(port1id.equals(interfaceId)){
													Point p = edge.getPointAt(0);
													p.translate(ox, oy);
													pointList.add(p);
												}
												else if(port2id.equals(interfaceId)){
													Point p = edge.getPointAt(edge.getNumPoints() - 1);
													p.translate(ox, oy);
													pointList.add(p);
												}
											}
											//System.out.println("pointList = " + pointList);
											if(pointList.size() > 0){
												Point[] points = (Point[])pointList.toArray(new Point[0]);
												Point avgPoint = getAveragePoint(points);
									
												MoveEndpointLogic.moveEndpointTo((EndpointThing)interfaceThing, newBounds, avgPoint.x, avgPoint.y);
											}
										}
									}
								}
							}
						}
					}
			
					progressBar.setValue(80);
					progressBar.setString("Laying out Links");
			
					boolean routeEdges = true;
					Boolean dontRouteLinksBool = (Boolean)gp.getProperty("dontroutelinks");
					if((dontRouteLinksBool != null) && (dontRouteLinksBool.booleanValue() == true)){
						routeEdges = false;
					}
			
					int numEdges = gl.getNumEdges();
					for(int i = 0; i < numEdges; i++){
						GraphLayout.Edge edge = gl.getEdgeAt(i);
						String edgeId = edge.getEdgeId();
						for(Iterator it = model.getThingIterator(); it.hasNext(); ){
							Thing t = (Thing)it.next();
							String xArchID = (String)t.getProperty(IHinted.XARCHID_PROPERTY_NAME);
							if((xArchID != null) && (xArchID.equals(edgeId))){
								//Found it.
								//System.out.println("found it.");
					
								if(t instanceof SplineThing){
									SplineThing rst = (SplineThing)t;
					
									//remove old middle points
									while(rst.getNumPoints() > 2){
										rst.removePointAt(1);
									}
						
									if(routeEdges){
										int numPoints = edge.getNumPoints();
										for(int j = (numPoints - 2); j >= 1; j--){
											Point p = edge.getPointAt(j);
											p.translate(ox, oy);
											rst.insertPointAt(p, 1);
										}
									}
								}
							}
						}		
					}
					progressBar.setValue(100);
					progressBar.setString("Done!");
				}finally{
					model.endBulkChange();
				}
				try{
					Thread.sleep(500);
				}
				catch(InterruptedException ie){}
				pd.doDone();
			}
		};
		
		pd.doPopup();
		layoutThread.start();
	}
	
	static class OverlapOption{
		protected String optionName;
		protected String displayName;
		
		public OverlapOption(String optionName, String displayName){
			this.optionName = optionName;
			this.displayName = displayName;
		}
		
		public String getOptionName(){
			return optionName;
		}
		
		public String getDisplayName(){
			return displayName;
		}
		
		public String toString(){
			return displayName;
		}
		
		public boolean equals(Object o){
			if(!(o instanceof OverlapOption)){
				return false;
			}
			return optionName.equals(((OverlapOption)o).getOptionName());
		}
	}
	
	static class GraphVizLayoutParametersDialog extends JDialog{
		protected GraphParameters gp;
		protected int tool;
		
		protected JTextField tfComponentHeight;
		protected JTextField tfComponentWidth;
		
		protected JTextField tfConnectorHeight;
		protected JTextField tfConnectorWidth;
		
		protected JTextField tfScale;
		
		protected JCheckBox cbDontRouteLinks;
		protected JCheckBox cbDontMoveInterfaces;
		
		//Dot-specific options
		protected JTextField tfNodeSep;
		protected JTextField tfRankSep;
		
		//Neato-specific options
		protected JComboBox cOverlapOptions;
		protected JTextField tfEdgeWeight;
		protected JTextField tfEdgeLength;
		
		protected JComboBox cAvailableStyles;
		protected JButton bLoadStyle;
		
		protected JButton bOK;
		protected JButton bCancel;
		
		public GraphVizLayoutParametersDialog(Frame f, int tool){
			super(f, "Layout Parameters...", true);
			this.tool = tool;
			JPanel mainDimensionsPanel = new JPanel();
			mainDimensionsPanel.setLayout(new GridLayout2(3, 3, 7, 7));
			mainDimensionsPanel.add(javax.swing.Box.createGlue());
			mainDimensionsPanel.add(new JLabel("Rel. Height"));
			mainDimensionsPanel.add(new JLabel("Rel. Width"));
			mainDimensionsPanel.add(new JLabel("Components"));
			tfComponentHeight = new JTextField(5);
			tfComponentWidth = new JTextField(5);
			mainDimensionsPanel.add(tfComponentHeight);
			mainDimensionsPanel.add(tfComponentWidth);

			mainDimensionsPanel.add(new JLabel("Connectors"));
			tfConnectorHeight = new JTextField(5);
			tfConnectorWidth = new JTextField(5);
			mainDimensionsPanel.add(tfConnectorHeight);
			mainDimensionsPanel.add(tfConnectorWidth);
			
			JPanel scalePanel = new JPanel();
			scalePanel.setLayout(new GridLayout2(1, 2, 7, 7));
			scalePanel.add(new JLabel("Scale"));
			tfScale = new JTextField(5);
			scalePanel.add(tfScale);
			
			JPanel dimensionsPanel = new JPanel();
			dimensionsPanel.setLayout(new BoxLayout(dimensionsPanel, BoxLayout.Y_AXIS));
			
			dimensionsPanel.add(mainDimensionsPanel);
			dimensionsPanel.add(javax.swing.Box.createVerticalStrut(8));
			dimensionsPanel.add(new HorizontalLine());
			dimensionsPanel.add(javax.swing.Box.createVerticalStrut(8));
			dimensionsPanel.add(new JPanelUL(scalePanel));
			
			JPanel dimensionsContainerPanel = new JPanel();
			dimensionsContainerPanel.setLayout(new BorderLayout());
			dimensionsContainerPanel.add("Center", new JPanelIS(dimensionsPanel, 8));
			dimensionsContainerPanel.setBorder(new TitledBorder("Dimensions"));
			
			cbDontRouteLinks = new JCheckBox("Don't Route Links");
			cbDontMoveInterfaces = new JCheckBox("Don't Move Interfaces");
			
			JPanel mainOptionsPanel = new JPanel();
			mainOptionsPanel.setLayout(new BoxLayout(mainOptionsPanel, BoxLayout.X_AXIS));
			
			mainOptionsPanel.add(cbDontRouteLinks);
			mainOptionsPanel.add(cbDontMoveInterfaces);
			
			JPanel optionsContainerPanel = new JPanel();
			optionsContainerPanel.setLayout(new BorderLayout());
			optionsContainerPanel.add("Center", new JPanelIS(new JPanelUL(mainOptionsPanel), 8));
			optionsContainerPanel.setBorder(new TitledBorder("Options"));
			
			JPanel dotOptionsContainerPanel = new JPanel();
			if(tool == IGraphLayout.TOOL_DOT){
				tfNodeSep = new JTextField("", 4);
				tfRankSep = new JTextField("", 4);

				JPanel sepPanel = new JPanel();
				sepPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				sepPanel.add(new JLabel("Node Sep:"));
				sepPanel.add(tfNodeSep);
				
				sepPanel.add(new JLabel("Rank Sep:"));
				sepPanel.add(tfRankSep);
				
				JPanel dotOptionsPanel = new JPanel();
				dotOptionsPanel.setLayout(new BoxLayout(dotOptionsPanel, BoxLayout.Y_AXIS));
				dotOptionsPanel.add(sepPanel);
				
				dotOptionsContainerPanel.setLayout(new BorderLayout());
				dotOptionsContainerPanel.add("Center", new JPanelIS(new JPanelUL(dotOptionsPanel), 8));
				dotOptionsContainerPanel.setBorder(new TitledBorder("Dot-Specific Options"));
			}
			
			JPanel neatoOptionsContainerPanel = new JPanel();
			if(tool == IGraphLayout.TOOL_NEATO){
				DefaultComboBoxModel cOverlapOptionsModel = new DefaultComboBoxModel();
				cOverlapOptionsModel.addElement(new OverlapOption("true", "Allow Overlap"));
				cOverlapOptionsModel.addElement(new OverlapOption("scale", "Uniform Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("scalexy", "X/Y Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("orthoxy", "X/Y Orthogonal Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("orthoyx", "Y/X Orthogonal Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("ortho", "X/Y Orthogonal+Heuristic"));
				cOverlapOptionsModel.addElement(new OverlapOption("ortho_yx", "Y/X Orthogonal+Heuristic"));
				cOverlapOptionsModel.addElement(new OverlapOption("porthoxy", "X/Y Pseudo-orthogonal Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("porthoyx", "Y/X Pseudo-orthogonal Scale"));
				cOverlapOptionsModel.addElement(new OverlapOption("portho", "X/Y Pseudo-orthogonal+Heuristic"));
				cOverlapOptionsModel.addElement(new OverlapOption("portho_yx", "Y/X Pseudo-orthogonal+Heuristic"));
				cOverlapOptionsModel.addElement(new OverlapOption("false", "Voronoi Technique"));
				cOverlapOptions = new JComboBox(cOverlapOptionsModel);
				
				JPanel overlapPanel = new JPanel();
				overlapPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				overlapPanel.add(new JLabel("Overlap Fix:"));
				overlapPanel.add(cOverlapOptions);
				
				tfEdgeWeight = new JTextField("1.0", 4);
				tfEdgeLength = new JTextField("2.0", 4);
				
				JPanel edgePanel = new JPanel();
				edgePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				edgePanel.add(new JLabel("Edge weight:"));
				edgePanel.add(tfEdgeWeight);
				edgePanel.add(new JLabel("Edge Length:"));
				edgePanel.add(tfEdgeLength);
							
				JPanel neatoOptionsPanel = new JPanel();
				neatoOptionsPanel.setLayout(new BoxLayout(neatoOptionsPanel, BoxLayout.Y_AXIS));
				neatoOptionsPanel.add(new JPanelUL(overlapPanel));
				neatoOptionsPanel.add(new JPanelUL(edgePanel));
				
				neatoOptionsContainerPanel.setLayout(new BorderLayout());
				neatoOptionsContainerPanel.add("Center", new JPanelIS(new JPanelUL(neatoOptionsPanel), 8));
				neatoOptionsContainerPanel.setBorder(new TitledBorder("Neato-Specific Options"));
			}
			
			JPanel mainLoadStylePanel = new JPanel();
			mainLoadStylePanel.setLayout(new BoxLayout(mainLoadStylePanel, BoxLayout.Y_AXIS));
			
			JPanel miniLoadStylePanel = new JPanel();
			miniLoadStylePanel.setLayout(new BoxLayout(miniLoadStylePanel, BoxLayout.X_AXIS));
			Map availableStyles = getAvailableStyles(tool);
			String[] styleNames = (String[])availableStyles.keySet().toArray(new String[0]);
			cAvailableStyles = new JComboBox(styleNames);
			cAvailableStyles.setSelectedItem("Generic");
			
			bLoadStyle = new JButton("Load Style");
			bLoadStyle.addActionListener(new StyleLoader());
			miniLoadStylePanel.add(new JPanelIS(cAvailableStyles, 3));
			miniLoadStylePanel.add(new JPanelIS(bLoadStyle, 3));
			
			mainLoadStylePanel.add(new JPanelUL(new JLabel("<HTML>To use default values, select a style and click 'Load Style'</HTML>")));
			mainLoadStylePanel.add(miniLoadStylePanel);
			
			JPanel loadStyleContainerPanel = new JPanel();
			loadStyleContainerPanel.setLayout(new BorderLayout());
			loadStyleContainerPanel.add("Center", new JPanelIS(new JPanelUL(mainLoadStylePanel), 8));
			loadStyleContainerPanel.setBorder(new TitledBorder("Load Values"));
			
			bOK = new JButton("OK");
			bOK.addActionListener(new OKAdapter());
			
			bCancel = new JButton("Cancel");
			bCancel.addActionListener(new WindowCloseAdapter());
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(bOK);
			buttonPanel.add(bCancel);
			
			this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
			this.getContentPane().add(new JPanelIS(new JPanelUL(dimensionsContainerPanel), 5));
			this.getContentPane().add(new JPanelIS(new JPanelUL(optionsContainerPanel), 5));
			if(tool == IGraphLayout.TOOL_DOT){
				this.getContentPane().add(new JPanelIS(new JPanelUL(dotOptionsContainerPanel), 5));
			}
			if(tool == IGraphLayout.TOOL_NEATO){
				this.getContentPane().add(new JPanelIS(new JPanelUL(neatoOptionsContainerPanel), 5));
			}
			this.getContentPane().add(new JPanelIS(new JPanelUL(loadStyleContainerPanel), 5));
			this.getContentPane().add(new JPanelIS(buttonPanel, 2));
			this.getContentPane().add(javax.swing.Box.createGlue());
			
			setParameters(new GraphParameters());
			this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			//this.setDefaultCloseOperation(
			//	JDialog.DISPOSE_ON_CLOSE);

			this.addWindowListener(new WindowCloseAdapter());
			
			//this.setSize(550, 500);
			this.pack();
			//this.setSize(getContentPane().preferredSize());
			WidgetUtils.centerInScreen(this);
		}

		class OKAdapter implements ActionListener{
			public void actionPerformed(ActionEvent evt){
				gp = buildGraphParameters(true);
				if(gp != null){
					doClose();
				}
			}
		}

		class WindowCloseAdapter extends WindowAdapter implements ActionListener{
			public void windowClosing(WindowEvent evt) {
				doClose();
			}
			
			public void actionPerformed(ActionEvent evt){
				doClose();
			}
		}
		
		public void doClose(){
			this.setVisible(false);
			this.dispose();
		}
		
		class StyleLoader implements ActionListener{
			public void actionPerformed(ActionEvent evt){
				String styleToLoad = (String)cAvailableStyles.getSelectedItem();
				Map availableStyles = getAvailableStyles(tool);
				GraphParameters gp = (GraphParameters)availableStyles.get(styleToLoad);
				if(gp != null){
					setParameters(gp);
				}
			}
		}
		
		public GraphParameters getGraphParameters(){
			return gp;
		}
		
		private GraphParameters buildGraphParameters(boolean throwDialogs){
			GraphParameters gp = new GraphParameters();

			try{
				gp.setRelativeComponentHeight(Double.parseDouble(tfComponentHeight.getText()));
				if(gp.getRelativeComponentHeight() < 0){
					throw new NumberFormatException();
				}
			}
			catch(Exception e){
				if(throwDialogs){
					JOptionPane.showMessageDialog(this, "Component height must be a positive floating-point number", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				}
				return null;
			}
			
			try{
				gp.setRelativeComponentWidth(Double.parseDouble(tfComponentWidth.getText()));
				if(gp.getRelativeComponentWidth() <= 0){
					throw new NumberFormatException();
				}
			}
			catch(Exception e){
				if(throwDialogs){
					JOptionPane.showMessageDialog(this, "Component width must be a positive floating-point number", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				}
				return null;
			}
			
			try{
				gp.setRelativeConnectorHeight(Double.parseDouble(tfConnectorHeight.getText()));
				if(gp.getRelativeConnectorHeight() <= 0){
					throw new NumberFormatException();
				}
			}
			catch(Exception e){
				if(throwDialogs){
					JOptionPane.showMessageDialog(this, "Connector height must be a positive floating-point number", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				}
				return null;
			}
			
			try{
				gp.setRelativeConnectorWidth(Double.parseDouble(tfConnectorWidth.getText()));
				if(gp.getRelativeConnectorWidth() <= 0){
					throw new NumberFormatException();
				}
			}
			catch(Exception e){
				if(throwDialogs){
					JOptionPane.showMessageDialog(this, "Connector width must be a positive floating-point number", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				}
				return null;
			}
			
			try{
				gp.setScale(Double.parseDouble(tfScale.getText()));
				if(gp.getScale() <= 0){
					throw new NumberFormatException();
				}
			}
			catch(Exception e){
				if(throwDialogs){
					JOptionPane.showMessageDialog(this, "Scale must be a positive floating-point number", 
						"Error", JOptionPane.ERROR_MESSAGE); 
				}
				return null;
			}
			
			gp.setProperty("dontroutelinks", new Boolean(cbDontRouteLinks.isSelected()));
			gp.setProperty("dontmoveinterfaces", new Boolean(cbDontMoveInterfaces.isSelected()));
			
			if(tool == IGraphLayout.TOOL_DOT){
				try{
					String nodeSepString = tfNodeSep.getText();
					if((nodeSepString != null) && (nodeSepString.trim().length() != 0)){
						double nodeSep = Double.parseDouble(nodeSepString);
						if(nodeSep <= 0){
							throw new NumberFormatException();
						}
						gp.setProperty("nodesep", nodeSepString);
					}
				}
				catch(Exception e){
					if(throwDialogs){
						JOptionPane.showMessageDialog(this, "Node separation must be blank, or a positive floating-point number", 
							"Error", JOptionPane.ERROR_MESSAGE); 
					}
					return null;
				}
				
				try{
					String rankSepString = tfRankSep.getText();
					if((rankSepString != null) && (rankSepString.trim().length() != 0)){
						double rankSep = Double.parseDouble(rankSepString);
						if(rankSep <= 0){
							throw new NumberFormatException();
						}
						gp.setProperty("ranksep", rankSepString);
					}
				}
				catch(Exception e){
					if(throwDialogs){
						JOptionPane.showMessageDialog(this, "Rank separation must be blank, or a positive floating-point number", 
							"Error", JOptionPane.ERROR_MESSAGE); 
					}
					return null;
				}
			}
			else if(tool == IGraphLayout.TOOL_NEATO){
				OverlapOption oo = (OverlapOption)cOverlapOptions.getSelectedItem();
				gp.setProperty("overlapOption", oo.getOptionName());
				
				try{
					double edgeWeight = Double.parseDouble(tfEdgeWeight.getText());
					if(edgeWeight <= 0){
						throw new NumberFormatException();
					}
					gp.setProperty("edgeWeight", new Double(edgeWeight));
				}
				catch(Exception e){
					if(throwDialogs){
						JOptionPane.showMessageDialog(this, "Edge weight must be a positive floating-point number", 
							"Error", JOptionPane.ERROR_MESSAGE); 
					}
					return null;
				}

				try{
					double edgeLength = Double.parseDouble(tfEdgeLength.getText());
					if(edgeLength <= 0){
						throw new NumberFormatException();
					}
					gp.setProperty("edgeLength", new Double(edgeLength));
				}
				catch(Exception e){
					if(throwDialogs){
						JOptionPane.showMessageDialog(this, "Edge length must be a positive floating-point number", 
							"Error", JOptionPane.ERROR_MESSAGE); 
					}
					return null;
				}
			}
			
			return gp;
		}
		
		public void setParameters(GraphParameters p){
			tfComponentHeight.setText(Double.toString(p.getRelativeComponentHeight()));
			tfComponentWidth.setText(Double.toString(p.getRelativeComponentWidth()));

			tfConnectorHeight.setText(Double.toString(p.getRelativeConnectorHeight()));
			tfConnectorWidth.setText(Double.toString(p.getRelativeConnectorWidth()));
			
			tfScale.setText(Double.toString(p.getScale()));
			
			Boolean dontRouteLinks = (Boolean)p.getProperty("dontroutelinks");
			if(dontRouteLinks == null){
				cbDontRouteLinks.setSelected(false);
			}
			else{
				cbDontRouteLinks.setSelected(dontRouteLinks.booleanValue());				
			}
			
			Boolean dontMoveInterfaces = (Boolean)p.getProperty("dontmoveinterfaces");
			if(dontMoveInterfaces == null){
				cbDontMoveInterfaces.setSelected(false);
			}
			else{
				cbDontMoveInterfaces.setSelected(dontMoveInterfaces.booleanValue());				
			}
			
			if(tool == IGraphLayout.TOOL_DOT){
				String nodeSep = (String)p.getProperty("nodesep");
				if(nodeSep != null){
					tfNodeSep.setText(nodeSep);
				}
				String rankSep = (String)p.getProperty("ranksep");
				if(rankSep != null){
					tfRankSep.setText(rankSep);
				}
			}
			else if(tool == IGraphLayout.TOOL_NEATO){
				String overlapOption = (String)p.getProperty("overlapOption");
				if(overlapOption != null){
					for(int i = 0; i < cOverlapOptions.getItemCount(); i++){
						OverlapOption oo = (OverlapOption)cOverlapOptions.getItemAt(i);
						String optionName = oo.getOptionName();
						if(optionName.equals(overlapOption)){
							cOverlapOptions.setSelectedIndex(i);
							break;
						}
					}
				}
				
				Double edgeWeightDouble = (Double)p.getProperty("edgeWeight");
				if(edgeWeightDouble != null){
					double edgeWeight = edgeWeightDouble.doubleValue();
					tfEdgeWeight.setText("" + edgeWeight);
				}
				
				Double edgeLengthDouble = (Double)p.getProperty("edgeLength");
				if(edgeLengthDouble != null){
					double edgeLength = edgeLengthDouble.doubleValue();
					tfEdgeLength.setText("" + edgeLength);
				}
			}
		}
		
		//Returns a String->GraphParameters map, where the string is the name.
		public static Map getAvailableStyles(int tool){
			HashMap m = new HashMap();
			GraphParameters defaultParameters = new GraphParameters();
			m.put("Generic", defaultParameters);
			
			GraphParameters niceParameters = new GraphParameters();
			niceParameters.setRelativeComponentWidth(4.0d);
			niceParameters.setRelativeComponentHeight(3.0d);
			niceParameters.setRelativeConnectorWidth(2.0d);
			niceParameters.setRelativeConnectorHeight(1.0d);
			niceParameters.setScale(30.0d);
			niceParameters.setProperty("dontroutelinks", new Boolean(true));
			
			if(tool == IGraphLayout.TOOL_DOT){
				niceParameters.setProperty("nodesep", "1.25");
				niceParameters.setProperty("ranksep", "1.25");
			}
			else if(tool == IGraphLayout.TOOL_NEATO){
				niceParameters.setProperty("overlapOption", "orthoxy");
				niceParameters.setProperty("edgeWeight", new Double(3.0));
				niceParameters.setProperty("edgeLength", new Double(3.0));
			}
			m.put("Nice", niceParameters);

			if(tool == IGraphLayout.TOOL_DOT){
				GraphParameters c2Parameters = new GraphParameters();
				c2Parameters.setRelativeComponentWidth(5.0d);
				c2Parameters.setRelativeComponentHeight(4.0d);
				c2Parameters.setRelativeConnectorWidth(25.0d);
				c2Parameters.setRelativeConnectorHeight(1.0d);
				c2Parameters.setScale(25.0d);
				c2Parameters.setProperty("dontmoveinterfaces", new Boolean(true));
				c2Parameters.setProperty("nodesep", "1.5");
				c2Parameters.setProperty("ranksep", "1.75");
				
				m.put("C2", c2Parameters);
			}

			return m;
		}
		
		public static GraphParameters showDialog(Frame parentFrame, int tool){
			GraphVizLayoutParametersDialog gvlpd = 
				new GraphVizLayoutParametersDialog(parentFrame, tool);
			gvlpd.setVisible(true);
			return gvlpd.getGraphParameters();
		}
	}
}

