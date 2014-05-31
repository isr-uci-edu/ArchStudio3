/*
package archstudio.comp.selectordriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.UIDGenerator;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.*;

import archstudio.invoke.*;
//This is imported to the selector
import archstudio.comp.booleaneval.*;
import archstudio.comp.selector.*;
import archstudio.comp.archpruner.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

 * Author: Matt Critchlow
 * Created: Friday, February 14, 2003 4:16:26 PM
 * Modified: Friday, February 14, 2003 4:16:26 PM
 */


/*class SelectorDriverProgress extends JFrame implements ActionListener
{
	
		protected JProgressBar mainProgressBar;
		protected JPanel mainPanel;
		public SelectorDriverProgress()
		{
			super(PRODUCT_NAME + " " + "Progress Bar");
			init();
			addMessageProcessor(new SelectorDriverProgressMessageProcessor());
		}
	
	

	private void init()
		{
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (400);
			double ySize = (300);
			mainProgressBar = new JProgressBar();
			mainProgressBar.setStringPainted(true);
			JPanel tempPanel = new JPanel();
			tempPanel.setLayout( new FlowLayout(FlowLayout.LEFT));
			tempPanel.add(new JLabel("Progress: "));
			tempPanel.add(mainProgressBar);
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("Center", tempPanel);
			this.getContentPane().add(new JScrollPane(mainPanel));
			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());

			this.addWindowListener(new SelectorDriverProgressWindowAdapter());
	}
	public void updateProgress(SelectorDriverProgressMessage sdpmessage)
	{
				String message = sdpmessage.getAdditionalMessage();
				double tempTotal;
				if(message != null){
					mainProgressBar.setString(message);
				}
				mainProgressBar.setMaximum(sdpmessage.getUpperBound());
				mainProgressBar.setValue(sdpmessage.getCurrentValue());
				tempTotal = mainProgressBar.getPercent();
				if(message != null)
				{
					message = message + (String)tempTotal;
					mainProgressBar.setString(message);
				}
				else
					mainProgressBar.setString(message);
				this.repaint();
				if(sdpmessage.getUpperBound() == sdpmessage.getCurrentValue())
				{
					mainProgressBar.setValue(0);
					mainProgressBar.setString("Finished");
					this.repaint();
				}
		}
}
class SelectorDriverProgressWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				SelectorDriverProgress = null;
			}
		}
class SelectorDriverProgressMessageProcessor implements MessageProcessor
{
			public void handle(Message m){
				if(m instanceof SelectorDriverProgressMessage){
					SelectorDriverProgressMessage message = (SelectorDriverProgressMessage)m;
					String managedSystemURI = message.getManagedSystemURI();
					updateProgress(message);
					
					}
				}
			}
		}
*/
