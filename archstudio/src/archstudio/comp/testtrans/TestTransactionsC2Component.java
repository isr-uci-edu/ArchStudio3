package archstudio.comp.testtrans;

//Includes classes that will help to make our component
//"Invokable" from the ArchStudio File Manager/Invoker
import archstudio.invoke.*;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support xArchADT use
import archstudio.comp.xarchtrans.*;
import edu.uci.ics.xarchutils.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class TestTransactionsC2Component extends AbstractC2DelegateBrick implements InvokableBrick{
	public static final String PRODUCT_NAME = "Test Transactions Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	//This XArchFlatInterface is an EPC interface implemented in another component,
	//in this case xArchADT.  Because this component uses c2.fw's EBI wrapper mechanism, 
	//we can call functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatTransactionsInterface xarch;
	
	//The main application window
	protected TestTransactionsFrame hwFrame = null;

	public TestTransactionsC2Component(Identifier id){
		super(id);
		hwFrame = null;

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(hwFrame != null){
					hwFrame.updateOpenURLs();
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);

		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"ArchStudio 3 Development/Test Transactions", 
			"A component to test that transactions work in ArchStudio 3.");
	}

	//This gets called automatically when we get an invoke message from
	//the invoker.
	public void invoke(InvokeMessage m){
		//Let's open a new window.
		newWindow();
	}

	public void newWindow(){
		//This makes sure we only have one active window open.
		if(hwFrame == null){
			hwFrame = new TestTransactionsFrame();
		}
		else{
			hwFrame.requestFocus();
		}
	}
	
	class TestArchitectureThread extends Thread{
		String url;
		
		public TestArchitectureThread(String url){
			this.url = url;
		}
		
		public void run(){
			doTestArchitecture();
		}
		
		public void doTestArchitecture(){
			if(url == null){
				return;
			}
			ObjRef xArchRef = xarch.getOpenXArch(url);
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef archStructureRef = xarch.getElement(typesContextRef, "ArchStructure", xArchRef);
			ObjRef[] componentRefs = xarch.getAll(archStructureRef, "Component");
			for(int i = 0; i < componentRefs.length; i++){
				String id = (String)xarch.get(componentRefs[i], "id");
				System.out.println("Component: " + id);
			}
			
			Transaction t = xarch.createTransaction(xArchRef);
			xarch.set(t, componentRefs[0], "id", "Wakka Wakka!");
			xarch.set(t, componentRefs[1], "id", "Wonkamania!");
			
			componentRefs = xarch.getAll(archStructureRef, "Component");
			for(int i = 0; i < componentRefs.length; i++){
				String id = (String)xarch.get(componentRefs[i], "id");
				System.out.println("Component: " + id);
			}
			
			xarch.commit(t);
	
			componentRefs = xarch.getAll(archStructureRef, "Component");
			for(int i = 0; i < componentRefs.length; i++){
				String id = (String)xarch.get(componentRefs[i], "id");
				System.out.println("Component: " + id);
			}
			
			System.out.println("Test #1 complete.");
			
			System.out.println("Creating transaction.");
			t = xarch.createTransaction(xArchRef);
			System.out.println("Removing.");
			xarch.remove(t, archStructureRef, "Component", componentRefs[0]);
			System.out.println("Getting all.");
			componentRefs = xarch.getAll(archStructureRef, "Component");
	
			System.out.println("Iterating.");
			for(int i = 0; i < componentRefs.length; i++){
				String id = (String)xarch.get(componentRefs[i], "id");
				System.out.println("Component: " + id);
			}
	
			xarch.commit(t);
			System.out.println("Getting all.");
			componentRefs = xarch.getAll(archStructureRef, "Component");
	
			System.out.println("Iterating.");
			for(int i = 0; i < componentRefs.length; i++){
				String id = (String)xarch.get(componentRefs[i], "id");
				System.out.println("Component: " + id);
			}
			System.out.println("Test #2 complete.");
			
		}
	}
	
	public void testArchitecture(String url){
		new TestArchitectureThread(url).start();
	}
	
	class TestTransactionsFrame extends JFrame{
		
		protected JList urlList;
		protected JButton testButton;
		
		public TestTransactionsFrame(){
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			archstudio.Branding.brandFrame(this);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (200);
			double ySize = (100);
			double xPos = (screenSize.getWidth() * 0.70);
			double yPos = (screenSize.getHeight() * 0.70);

			urlList = new JList(new DefaultListModel());
			JPanel mainPanel = new JPanel();
	
			testButton = new JButton("Test this architecture.");
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("North", new JLabel("Open URLs:"));
			mainPanel.add("Center", urlList);
			mainPanel.add("South", testButton);
			
			testButton.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						testArchitecture((String)urlList.getSelectedValue());
					}
				}
			);
			
			this.getContentPane().add(new JScrollPane(mainPanel));

			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());

			this.addWindowListener(new TestTransactionsWindowAdapter());
			
			//Go get the initial list of URLs.
			updateOpenURLs();
		}
		
		class TestTransactionsWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent e){
				destroy();
				dispose();
				setVisible(false);
				hwFrame = null;
			}
		}				
		
		public void updateOpenURLs(){
			DefaultListModel lm = (DefaultListModel)urlList.getModel();
			lm.removeAllElements();
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			//System.out.println("Calling getOpen...");
			String[] urls = xarch.getOpenXArchURIs();
			//System.out.println("Returned");
			for(int i = 0; i < urls.length; i++){
				lm.addElement(urls[i]);
			}
			repaint();
			validate();
		}
	}

}
