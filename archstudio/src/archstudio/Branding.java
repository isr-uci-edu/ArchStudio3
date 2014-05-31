package archstudio;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.JPanelMS;
import edu.uci.ics.widgets.JPanelWL;
import edu.uci.ics.widgets.WidgetUtils;

public class Branding {

	private Branding(){}

	private static SplashScreen splashScreen = new SplashScreen();
	private static JWindow wSplashScreen = null;
	
	private static SmallSplashScreen smallSplashScreen = new SmallSplashScreen();
	private static JWindow wSmallSplashScreen = null;
	
	private static ImageIcon icon64;
	private static ImageIcon icon32;
	private static ImageIcon icon16;
	
	static{
		icon64 = WidgetUtils.getImageIcon("archstudio/res/icon-64x64.png");
		icon32 = WidgetUtils.getImageIcon("archstudio/res/icon-32x32.png");
		icon16 = WidgetUtils.getImageIcon("archstudio/res/icon-16x16.png");
	}
	
	public static void brandFrame(JFrame f){
		f.setIconImage(icon16.getImage());
	}
	
	public static String readCopyrightMessage(){
		String resourceName = "archstudio/res/copyright-message.txt";
		try{
			InputStream resourceInputStream = null;

			resourceInputStream = Branding.class.getResourceAsStream(resourceName);

			if(resourceInputStream == null){
				resourceInputStream = ClassLoader.getSystemResourceAsStream(resourceName);
			}
			if(resourceInputStream == null){
				return null;
			}
				
			BufferedReader br = new BufferedReader(new InputStreamReader(resourceInputStream));
			String line = br.readLine();
			br.close();
			return line;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	static class DeveloperCreditBlock{
		public String header;
		public ArrayList names;
	}
	
	public static DeveloperCreditBlock[] readDeveloperCredits(){
		String resourceName = "archstudio/res/developer-credits.txt";
		ArrayList developerCreditBlocks = new ArrayList();
		try{
			ArrayList strings = new ArrayList();
				
			InputStream resourceInputStream = null;

			resourceInputStream = Branding.class.getResourceAsStream(resourceName);

			if(resourceInputStream == null){
				resourceInputStream = ClassLoader.getSystemResourceAsStream(resourceName);
			}
			if(resourceInputStream == null){
				return null;
			}
				
			BufferedReader br = new BufferedReader(new InputStreamReader(resourceInputStream));
			
			DeveloperCreditBlock dcb = null;
			
			String line = null;
			do{
				line = br.readLine();
				if(line != null){
					line = line.trim();
					if(!line.equals("")){
						if(line.startsWith("#")){
							dcb = new DeveloperCreditBlock();
							dcb.header = line.substring(1);
							dcb.names = new ArrayList();
							developerCreditBlocks.add(dcb);
						}
						else{
							dcb.names.add(line);
						}
					}
				}
			}while(line != null); 
			br.close();
			return (DeveloperCreditBlock[])developerCreditBlocks.toArray(new DeveloperCreditBlock[0]);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/*
	public static void main(String[] args){
		showSplashScreen();
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException ie){}
		setSplashScreenProgressValue(75);
		setSplashScreenProgressString("hiya");
		try{
			Thread.sleep(2000);
		}
		catch(InterruptedException ie){}
		hideSplashScreen();
		System.exit(0);
	}
	*/
	
	public static void splash(int msec){
		final int time = msec;
		Runnable r = new Runnable(){
			public void run(){
				showSplashScreen();
				try{
					Thread.sleep(time);
				}
				catch(InterruptedException ie){}
				hideSplashScreen();
			}
		};
		Thread th = new Thread(r);
		th.start();
		
	}

	public static void splashSmall(int msec){
		final int time = msec;
		Runnable r = new Runnable(){
			public void run(){
				showSmallSplashScreen();
				try{
					Thread.sleep(time);
				}
				catch(InterruptedException ie){}
				hideSmallSplashScreen();
			}
		};
		Thread th = new Thread(r);
		th.start();
	}
	
	public synchronized static void showSmallSplashScreen(){
		if(wSmallSplashScreen == null){
			wSmallSplashScreen = new JWindow();
			wSmallSplashScreen.getContentPane().setLayout(new BorderLayout());
			wSmallSplashScreen.getContentPane().add("Center", smallSplashScreen);
		
			wSmallSplashScreen.setSize(smallSplashScreen.getWidth(), smallSplashScreen.getHeight());
			WidgetUtils.centerInScreen(wSmallSplashScreen);
			wSmallSplashScreen.addMouseListener(
				new MouseAdapter(){
					public void mouseClicked(MouseEvent evt){
						hideSmallSplashScreen();
					}
				}
			);
			wSmallSplashScreen.setVisible(true);
		}
		else{
			wSplashScreen.toFront();
		}
	}

	public synchronized static void hideSmallSplashScreen(){
		if(wSmallSplashScreen != null){
			wSmallSplashScreen.setVisible(false);
			wSmallSplashScreen.dispose();
			wSmallSplashScreen = null;
		}
	}

	public synchronized static void showSplashScreen(){
		if(wSplashScreen == null){
			wSplashScreen = new JWindow();
			wSplashScreen.getContentPane().setLayout(new BorderLayout());
			wSplashScreen.getContentPane().add("Center", splashScreen);
		
			wSplashScreen.setSize(splashScreen.getWidth(), splashScreen.getHeight());
			WidgetUtils.centerInScreen(wSplashScreen);
			wSplashScreen.addMouseListener(
				new MouseAdapter(){
					public void mouseClicked(MouseEvent evt){
						hideSplashScreen();
					}
				}
			);
			wSplashScreen.setVisible(true);
		}
		else{
			wSplashScreen.toFront();
		}
	}
	
	public synchronized static void hideSplashScreen(){
		if(wSplashScreen != null){
			wSplashScreen.setVisible(false);
			wSplashScreen.dispose();
			wSplashScreen = null;
		}
	}
	
	public static void setSplashScreenProgressString(String progressString){
		splashScreen.setProgressString(progressString);
	}

	public static void setSplashScreenProgressValue(int progressValue){
		splashScreen.setProgressValue(progressValue);
	}
	
	static class SmallSplashScreen extends JPanel{
		protected ImageIcon splashScreenImageIcon = null;
		protected int width;
		protected int height;

		public SmallSplashScreen(){
			splashScreenImageIcon = 
				WidgetUtils.getImageIcon("archstudio/res/small-splash-screen.png");
			width = splashScreenImageIcon.getIconWidth();
			height = splashScreenImageIcon.getIconHeight();

			String copyrightMessage = Branding.readCopyrightMessage();
			JLabel copyrightLabel = new JLabel(copyrightMessage){
				public void paint(Graphics g){
					Object oldHint = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
						RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					super.paint(g);
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
						oldHint);
				}
			};
			copyrightLabel.setFont(WidgetUtils.SANSSERIF_PLAIN_TINY_FONT);
			copyrightLabel.setForeground(Color.WHITE);
			
			this.setLayout(new BorderLayout());
			JPanelIS clp = new JPanelIS(copyrightLabel, 3);
			clp.setOpaque(false);
			this.add("South", clp);
		}
		
		public int getHeight(){
			return height;
		}
		
		public int getWidth(){
			return width;
		}

		public void paintComponent(Graphics g1){
			Graphics2D g = (Graphics2D)g1;
			
			g.drawImage(splashScreenImageIcon.getImage(), 0, 0, splashScreenImageIcon.getImageObserver());
		}
	}
	
	static class SplashScreen extends JPanel{
		
		public static final int INIT_X = 15;
		public static final int INIT_Y = 155;
		
		protected ImageIcon splashScreenImageIcon = null;
		protected int width;
		protected int height;
		protected DeveloperCreditBlock[] developerCreditBlocks;
		protected String copyrightMessage;

		protected ProgressPanel pb;
		
		public SplashScreen(){
			splashScreenImageIcon = 
				WidgetUtils.getImageIcon("archstudio/res/splash-screen.png");
			width = splashScreenImageIcon.getIconWidth();
			height = splashScreenImageIcon.getIconHeight();
			developerCreditBlocks = Branding.readDeveloperCredits();
			copyrightMessage = Branding.readCopyrightMessage();
			
			this.setLayout(null);

			StringBuffer versionLabel = new StringBuffer(
				"<html><font style=\"font-family: SansSerif; font-weight: normal; font-size: 11pt\">"
			);
			versionLabel.append("<font style=\"font-weight:bold\">");
			versionLabel.append("Version:");
			versionLabel.append("</font> ");
			versionLabel.append("build ");
			versionLabel.append(VersionInfo.getVersion("[unofficial build]"));
			versionLabel.append("</font></html>");
			
			if(developerCreditBlocks != null){
				StringBuffer developerCreditLabel = new StringBuffer(
					"<html><font style=\"font-family: SansSerif; font-weight: normal; font-size: 9pt\">"
				);

				for(int i = 0; i < developerCreditBlocks.length; i++){
					developerCreditLabel.append("<font style=\"font-weight:bold\">");
					developerCreditLabel.append(developerCreditBlocks[i].header);
					developerCreditLabel.append(": ");
					developerCreditLabel.append("</font>");
					for(Iterator it = developerCreditBlocks[i].names.iterator(); it.hasNext(); ){
						String name = (String)it.next();
						developerCreditLabel.append(name);
						if(it.hasNext()) developerCreditLabel.append(", ");
					}
					developerCreditLabel.append("<br>");
				}
				developerCreditLabel.append("</font></html>");

				JPanel p = new JPanel(){
					public Insets getInsets(){
						return new Insets(5,5,5,5);
					}
				};
				p.setLayout(new BorderLayout());
				p.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				p.setOpaque(false);

				int inset = 0;
				p.setBounds(INIT_X + inset, INIT_Y + inset, getWidth() - (INIT_X * 2) - (inset * 2), getHeight() - INIT_Y - 15 - (inset * 2));

				JLabel lVersion =
					new JLabel(versionLabel.toString()){
					public void paint(Graphics g){
						Object oldHint = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
						((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						super.paint(g);
						((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
							oldHint);
					}
				};
				lVersion.setForeground(Color.WHITE);
			
				JPanel versionWLPanel = new JPanelWL(lVersion, p);
				versionWLPanel.setOpaque(false);
				
				JLabel lCredits = 
					new JLabel(developerCreditLabel.toString()){
						public void paint(Graphics g){
							Object oldHint = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
							((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
								RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
							super.paint(g);
							((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
								oldHint);
						}
					};
				lCredits.setForeground(Color.WHITE);
				
				JPanel creditsWLPanel = new JPanelWL(lCredits, p);
				creditsWLPanel.setOpaque(false);

				//Font font = new Font("Serif", Font.PLAIN, 30);
				//creditsWLPanel.setFont(font);

				JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				mainPanel.setOpaque(false);
				mainPanel.add(versionWLPanel);
				mainPanel.add(Box.createVerticalStrut(4));
				mainPanel.add(creditsWLPanel);
				
				//p.add("North", creditsWLPanel);
				p.add("North", mainPanel);
				
				JLabel copyrightLabel = new JLabel(copyrightMessage){
					public void paint(Graphics g){
						Object oldHint = ((Graphics2D)g).getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
						((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
						super.paint(g);
						((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
							oldHint);
					}
				};
				copyrightLabel.setFont(WidgetUtils.SANSSERIF_PLAIN_TINY_FONT);
				copyrightLabel.setForeground(Color.WHITE);
				
				p.add("South", copyrightLabel);
				
				JPanel progressPanel = new JPanel(){
					public Insets getInsets(){
						return new Insets(10, 10, 10, 10); 
					}
				};
				progressPanel.setOpaque(false);
				progressPanel.setLayout(new BorderLayout());
				
				pb = new ProgressPanel();
				pb.setForeground(Color.WHITE);
				pb.setBackground(Color.WHITE);
				pb.setOpaque(false);
				progressPanel.add("Center", pb);
				
				p.add("Center", progressPanel);
				
				this.add(p);
			}
		}
		
		public void setProgressString(String progressString){
			pb.setString(progressString);
		}
		
		public void setProgressValue(int value){
			pb.setValue(value);
		}
		
		public int getHeight(){
			return height;
		}
		
		public int getWidth(){
			return width;
		}

		public void paintComponent(Graphics g1){
			Graphics2D g = (Graphics2D)g1;
			
			g.drawImage(splashScreenImageIcon.getImage(), 0, 0, splashScreenImageIcon.getImageObserver());
		}
	}
		
	static class ProgressPanel extends JPanel{
		private JLabel label = new JLabel(){
			public void paint(Graphics g){
				Graphics g2 = g.create();
				g2.setXORMode(Color.BLACK);
				super.paint(g2);
				//g.setXORMode(null);
			}
		};
		private int value = 0;

		public ProgressPanel(){
			this.setLayout(new FlowLayout(FlowLayout.CENTER));
			label.setForeground(Color.WHITE);
			this.add(label);
		}
			
		public void setValue(int val){
			this.value = val;
			validate();
			repaint();
		}
			
		public void setString(String s){
			label.setText(s);
			validate();
			repaint();
		}
			
		public void paintComponent(Graphics g1){
			Graphics2D g = (Graphics2D)g1;
				
			int width = getBounds().width;
			int height = getBounds().height;
			
			if(height > 25) height = 25;
			
			float percent = (float)value / 100.0f;
			float frw = (float)width * percent;
			int rw = Math.round(frw);
				
			Paint oldPaint = g.getPaint();
			g.setPaint(Color.WHITE);
			g.fillRect(0, 0, rw, height);
			g.setPaint(oldPaint);
		}
	}

}
