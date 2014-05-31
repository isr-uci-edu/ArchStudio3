package archstudio.comp.perftest;

import archstudio.invoke.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.xarchutils.*;
import archstudio.comp.xarchtrans.*;
//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;


public class PerformanceTestC2Component extends AbstractC2DelegateBrick implements InvokableBrick{
	public static final String PRODUCT_NAME = "Performance Test Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	protected XArchFlatTransactionsInterface xarch;

	protected PerformanceTestFrame ptFrame = null;
	
	public PerformanceTestC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"ArchStudio 3 Development/Performance Test", 
			"A back-of-the-envelope performance tester for ArchStudio 3");
	}

	//This gets called automatically when we get an invoke message from
	//the invoker.
	public void invoke(InvokeMessage m){
		//Let's open a new window.
		newWindow();
	}

	public void newWindow(){
		//This makes sure we only have one active window open.
		if(ptFrame == null){
			ptFrame = new PerformanceTestFrame();
		}
		else{
			ptFrame.requestFocus();
		}
	}

	class PerformanceTestFrame extends JFrame implements ActionListener{
		protected JButton bRunTest;
		protected JPopupMenu mRunTest;
		protected JMenuItem miRunReadTest;
		protected JMenuItem miRunWriteTest;
		protected JMenuItem miRunNonbulkReadTest;
		protected JMenuItem miRunBulkReadTest;
		protected JLabel lResults;
		protected JProgressBar pbProgress;
		
		private static final int READ_ITERATIONS = 2000;
		private static final int WRITE_ITERATIONS = 200;
		private static final int BULK_ITERATIONS = 200;
		
		public PerformanceTestFrame(){
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			archstudio.Branding.brandFrame(this);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (360);
			double ySize = (150);
			double xPos = (screenSize.getWidth() * 0.50);
			double yPos = (screenSize.getHeight() * 0.50);

			pbProgress = new JProgressBar(0, READ_ITERATIONS);
			lResults = new JLabel("<HTML>[No Results Yet]</HTML>");
			
			bRunTest = new JButton("Run Test...");
			bRunTest.addActionListener(this);
			
			mRunTest = new JPopupMenu();
			
			miRunReadTest = new JMenuItem("Read Test");
			miRunReadTest.addActionListener(this);
			mRunTest.add(miRunReadTest);

			miRunWriteTest = new JMenuItem("Write Test");
			miRunWriteTest.addActionListener(this);
			mRunTest.add(miRunWriteTest);
			
			miRunNonbulkReadTest = new JMenuItem("Non-Bulk Read Test");
			miRunNonbulkReadTest.addActionListener(this);
			mRunTest.add(miRunNonbulkReadTest);
			
			miRunBulkReadTest = new JMenuItem("Bulk Read Test");
			miRunBulkReadTest.addActionListener(this);
			mRunTest.add(miRunBulkReadTest);

			JPanel centerPanel = new JPanel();
			centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
			
			centerPanel.add(pbProgress);
			centerPanel.add(lResults);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPanel.add(bRunTest);
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("North", new JLabel("Performance Test"));
			mainPanel.add("Center", centerPanel);
			mainPanel.add("South", buttonPanel);
			this.getContentPane().add(mainPanel);

			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());

			this.addWindowListener(new PerformanceTestWindowAdapter());
		}
		
		class PerformanceTestWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent e){
				destroy();
				dispose();
				setVisible(false);
				ptFrame = null;
			}
		}
		
		public synchronized void doWriteTest(){
			int iterations = WRITE_ITERATIONS;
			pbProgress.setMaximum(iterations);
			pbProgress.setValue(0);
			validate();
			paint(getGraphics());
			ObjRef xArchRef = xarch.createXArch("urn:PerformanceTest");
			ObjRef instanceContextRef = xarch.createContext(xArchRef, "Instance");
			ObjRef archInstanceRef = xarch.createElement(instanceContextRef, "ArchInstance");
			xarch.add(xArchRef, "Object", archInstanceRef);

			//Begin the test
			long startTime = System.currentTimeMillis();
			Transaction t = xarch.createTransaction(xArchRef);
			
			for(int i = 0; i < iterations; i++){
				//xarch.getAll(archInstanceRef, "ComponentInstance");
				ObjRef compInstanceRef = xarch.create(instanceContextRef, "ComponentInstance");
				xarch.set(t, compInstanceRef, "Id", "Component_" + i);
				xarch.add(t, archInstanceRef, "ComponentInstance", compInstanceRef);
				
				pbProgress.setValue(i);
				//validate();
				paint(getGraphics());
			}
			xarch.commit(t);
			long endTime = System.currentTimeMillis();
			double index = (double)(endTime - startTime) / (double)iterations;
			lResults.setText("<HTML>Performance Index (Lower=Better): " + index + "</HTML>");
			xarch.close("urn:PerformanceTest");
		}
		
		public synchronized void doReadTest(){
			int iterations = READ_ITERATIONS;
			pbProgress.setMaximum(iterations);
			pbProgress.setValue(0);
			validate();
			paint(getGraphics());
			ObjRef xArchRef = xarch.createXArch("urn:PerformanceTest");
			ObjRef instanceContextRef = xarch.createContext(xArchRef, "Instance");
			ObjRef archInstanceRef = xarch.createElement(instanceContextRef, "ArchInstance");
			xarch.add(xArchRef, "Object", archInstanceRef);
			for(int i = 0; i < 20; i++){
				ObjRef compInstanceRef = xarch.create(instanceContextRef, "ComponentInstance");
				xarch.set(compInstanceRef, "Id", "Component_" + i);
				xarch.add(archInstanceRef, "ComponentInstance", compInstanceRef);
			}
			
			//Begin the test
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < iterations; i++){
				xarch.getAll(archInstanceRef, "ComponentInstance");
				
				pbProgress.setValue(i);
				//validate();
				paint(getGraphics());
			}
			long endTime = System.currentTimeMillis();
			double index = (double)(endTime - startTime) / (double)iterations;
			lResults.setText("<HTML>Performance Index (Lower=Better): " + index + "</HTML>");
			xarch.close("urn:PerformanceTest");
		}
		
		public synchronized void doNonbulkReadTest(){
			int iterations = BULK_ITERATIONS;
			pbProgress.setMaximum(iterations);
			pbProgress.setValue(0);
			validate();
			paint(getGraphics());
			ObjRef xArchRef = xarch.createXArch("urn:PerformanceTest");
			ObjRef instanceContextRef = xarch.createContext(xArchRef, "Instance");
			ObjRef archInstanceRef = xarch.createElement(instanceContextRef, "ArchInstance");
			xarch.add(xArchRef, "Object", archInstanceRef);
			for(int i = 0; i < 20; i++){
				ObjRef compInstanceRef = xarch.create(instanceContextRef, "ComponentInstance");
				xarch.set(compInstanceRef, "Id", "Component_" + i);
				xarch.add(archInstanceRef, "ComponentInstance", compInstanceRef);
				
				ObjRef descriptionRef = xarch.create(instanceContextRef, "Description");
				xarch.set(descriptionRef, "Value", "Description" + i);
				xarch.set(compInstanceRef, "Description", descriptionRef);
			}
			
			//Begin the test
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < iterations; i++){
				ObjRef[] refs = xarch.getAll(archInstanceRef, "ComponentInstance");
				for(int j = 0; j < refs.length; j++){
					//System.out.println("getting desc!");
					ObjRef descriptionRef = (ObjRef)xarch.get(refs[j], "Description");
					String value = (String)xarch.get(descriptionRef, "Value");
					//System.out.println(value);
				}
				pbProgress.setValue(i);
				//validate();
				paint(getGraphics());
			}
			long endTime = System.currentTimeMillis();
			double index = (double)(endTime - startTime) / (double)iterations;
			lResults.setText("<HTML>Performance Index (Lower=Better): " + index + "</HTML>");
			xarch.close("urn:PerformanceTest");
		}
		
		public synchronized void doBulkReadTest(){
			int iterations = BULK_ITERATIONS;
			pbProgress.setMaximum(iterations);
			pbProgress.setValue(0);
			validate();
			paint(getGraphics());
			ObjRef xArchRef = xarch.createXArch("urn:PerformanceTest");
			ObjRef instanceContextRef = xarch.createContext(xArchRef, "Instance");
			ObjRef archInstanceRef = xarch.createElement(instanceContextRef, "ArchInstance");
			xarch.add(xArchRef, "Object", archInstanceRef);
			for(int i = 0; i < 20; i++){
				ObjRef compInstanceRef = xarch.create(instanceContextRef, "ComponentInstance");
				xarch.set(compInstanceRef, "Id", "Component_" + i);
				xarch.add(archInstanceRef, "ComponentInstance", compInstanceRef);
				
				ObjRef descriptionRef = xarch.create(instanceContextRef, "Description");
				xarch.set(descriptionRef, "Value", "Description" + i);
				xarch.set(compInstanceRef, "Description", descriptionRef);
			}
			
			//Begin the test
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < iterations; i++){
				XArchBulkQuery q = new XArchBulkQuery(archInstanceRef);
				q.addQueryPath("componentInstance*/description/value");
				XArchBulkQueryResults qr = xarch.bulkQuery(q);
				XArchFlatQueryInterface xarchBulk = new XArchBulkQueryResultProxy(xarch, qr);
				
				ObjRef[] refs = xarchBulk.getAll(archInstanceRef, "ComponentInstance");
				for(int j = 0; j < refs.length; j++){
					ObjRef descriptionRef = (ObjRef)xarchBulk.get(refs[j], "Description");
					String value = (String)xarchBulk.get(descriptionRef, "Value");
					//System.out.println(value);
				}
				pbProgress.setValue(i);
				//validate();
				paint(getGraphics());
			}
			long endTime = System.currentTimeMillis();
			double index = (double)(endTime - startTime) / (double)iterations;
			lResults.setText("<HTML>Performance Index (Lower=Better): " + index + "</HTML>");
			xarch.close("urn:PerformanceTest");
		}
		
		public synchronized void actionPerformed(ActionEvent evt){
			if(evt.getSource() == bRunTest){
				mRunTest.show(bRunTest.getParent(), bRunTest.getBounds().x, bRunTest.getBounds().y + bRunTest.getBounds().height);
			}
			else if(evt.getSource() == miRunReadTest){
				doReadTest();
			}
			else if(evt.getSource() == miRunWriteTest){
				doWriteTest();
			}
			else if(evt.getSource() == miRunNonbulkReadTest){
				doNonbulkReadTest();
			}
			else if(evt.getSource() == miRunBulkReadTest){
				doBulkReadTest();
			}
		}
	}
}
