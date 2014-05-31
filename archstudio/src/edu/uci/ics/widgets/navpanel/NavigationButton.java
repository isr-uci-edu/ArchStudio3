package edu.uci.ics.widgets.navpanel;

import edu.uci.ics.widgets.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class NavigationButton extends CustomButton{

	public static final int BACK_BUTTON = 100;
	public static final int FORWARD_BUTTON = 200;

	public static final int SECTION_MAIN = 50;
	public static final int SECTION_AUX = 75;
	
	public static final String CLICKED_SECTION_MAIN = "clickedMain";
	public static final String CLICKED_SECTION_AUX = "clickedAux";

	protected int buttonType = BACK_BUTTON;

	protected int mainArrowDimension = 32;
	protected int selectedSection = SECTION_MAIN;

	public NavigationButton(int buttonType){
		super();
		setOpaque(false);
		this.buttonType = buttonType;
		ToolTipManager.sharedInstance().registerComponent(this);
	}
	
	protected void addAdapters(){
		this.addFocusListener(new CustomButtonFocusAdapter());
		this.addMouseListener(new NavigationButtonMouseAdapter());
		this.addKeyListener(new NavigationButtonKeyAdapter());
	}
	
	public Point getPopupMenuPoint(){
		return new Point(0, mainArrowDimension);
	}
	
	public String getToolTipText(){
		if(buttonType == BACK_BUTTON){
			return "Go Back";
		}
		else if(buttonType == FORWARD_BUTTON){
			return "Go Forward";
		}
		return null;
	}

	public String getToolTipText(MouseEvent me){
		setupSection(me);
		if(buttonType == BACK_BUTTON){
			if(selectedSection == SECTION_AUX){
				return "Select Previous";
			}
			return "Go Back";
		}
		else if(buttonType == FORWARD_BUTTON){
			if(selectedSection == SECTION_AUX){
				return "Select Subsequent";
			}
			return "Go Forward";
		}
		return null;
	}
	
	protected void setupSection(MouseEvent me){
		Rectangle mainRect = getMainBounds(NavigationButton.this.getSize());
		Rectangle auxRect = getAuxBounds(NavigationButton.this.getSize());
		if(mainRect.contains(me.getPoint())){
			selectedSection = SECTION_MAIN;
		}
		else if(auxRect.contains(me.getPoint())){
			selectedSection = SECTION_AUX;
		}
	}

	protected class NavigationButtonMouseAdapter extends MouseAdapter{
		public NavigationButtonMouseAdapter(){
			super();
		}
		
		public void mouseEntered(MouseEvent me){
			mouseOver = true;
			setupSection(me);
			invalidate();
			repaint();
		}
		
		public void mouseExited(MouseEvent me){
			mouseOver = false;
			setupSection(me);
			invalidate();
			repaint();
		}
		
		public void mousePressed(MouseEvent me){
			mouseDown = true;
			setupSection(me);
			invalidate();
			repaint();
		}
		
		public void mouseReleased(MouseEvent me){
			mouseDown = false;
			setupSection(me);
			invalidate();
			repaint();
		}
		
		public void mouseClicked(MouseEvent me){
			//System.out.println("mouseClicked!");
			setupSection(me);
			if(!isEnabled()){
				return;
			}
			String command = (selectedSection == SECTION_MAIN) ? CLICKED_SECTION_MAIN : CLICKED_SECTION_AUX;
			ActionEvent evt = new ActionEvent(NavigationButton.this, ActionEvent.ACTION_FIRST, command);
			fireActionEvent(evt);
		}
	}
	
	protected class NavigationButtonKeyAdapter extends KeyAdapter{
		public NavigationButtonKeyAdapter(){
			super();
		}

		public void keyPressed(KeyEvent ke){
			int key = ke.getKeyCode();
			if(key == KeyEvent.VK_SPACE){
				if(hasKeyboardFocus){
					mouseDown = true;
					selectedSection = SECTION_MAIN;
					invalidate();
					repaint();
				}
			}
			else if(key == KeyEvent.VK_DOWN){
				if(hasKeyboardFocus){
					ke.consume();
					mouseDown = true;
					selectedSection = SECTION_AUX;
					invalidate();
					repaint();
				}
			}
		}
		
		public void keyReleased(KeyEvent ke){
			int key = ke.getKeyCode();
			if(key == KeyEvent.VK_SPACE){
				if(hasKeyboardFocus){
					mouseDown = false;
					invalidate();
					repaint();
					ActionEvent evt = new ActionEvent(NavigationButton.this, ActionEvent.ACTION_FIRST, CLICKED_SECTION_MAIN);
					fireActionEvent(evt);
				}
			}
			else if(key == KeyEvent.VK_DOWN){
				if(hasKeyboardFocus){
					ke.consume();
					mouseDown = false;
					invalidate();
					repaint();
					ActionEvent evt = new ActionEvent(NavigationButton.this, ActionEvent.ACTION_FIRST, CLICKED_SECTION_AUX);
					fireActionEvent(evt);
				}
			}
		}
	}
	
	public void drawIcon(Graphics g) {
	}
	
	private Rectangle getMainBounds(Dimension bounds){
		int mainWidth = (int)((float)bounds.width * 0.60f);
		int auxWidth = bounds.width - mainWidth;
		
		Rectangle mainBounds = new Rectangle();
		mainBounds.x = 0;
		mainBounds.y = 0;
		mainBounds.width = mainWidth;
		mainBounds.height = bounds.height;
		return mainBounds;
	}
	
	private Rectangle getAuxBounds(Dimension bounds){
		int mainWidth = (int)((float)bounds.width * 0.60f);
		int auxWidth = bounds.width - mainWidth;

		Rectangle auxBounds = new Rectangle();
		auxBounds.x = mainWidth;
		auxBounds.y = 0;
		auxBounds.width = auxWidth;
		auxBounds.height = bounds.height;
		
		return auxBounds;
	}
	
	public void drawMainIcon(Graphics g, Color c1, Color c2, int offset){
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle mainBounds = getMainBounds(getSize());
		
		int facing = buttonType == BACK_BUTTON ? WidgetUtils.FACING_WEST : WidgetUtils.FACING_EAST;
		
		Rectangle mainArrowBounds = WidgetUtils.contract(mainBounds, 4);
		mainArrowBounds.x += offset;
		mainArrowBounds.y += offset;
		Shape mainArrow = WidgetUtils.createIsocolesTriangle(mainArrowBounds, facing);
		
		g2d.setStroke(new BasicStroke(1.0f));
		
		if((c1 != null) && (c2 != null)){
			g2d.setPaint(new GradientPaint(0, 0, c1, mainBounds.width, mainBounds.height, c2));
			g2d.fill(mainArrow);
			g2d.setPaint(Color.BLACK);
		}
		else{
			g2d.setColor(Color.GRAY);
		}
		g2d.draw(mainArrow);
	}
	
	public void drawAuxIcon(Graphics g, Color c1, Color c2, int offset){
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			RenderingHints.VALUE_ANTIALIAS_ON);
		//g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
		//	shouldAntialiasText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		Rectangle auxBounds = getAuxBounds(getSize());
		
		int centerAuxX = auxBounds.x + (auxBounds.width / 2);
		int centerAuxY = auxBounds.y + (auxBounds.height / 2);
		
		Rectangle auxArrowBounds = new Rectangle(centerAuxX - 3 + offset, centerAuxY - 2 + offset, 6, 5);
		Shape auxArrow = WidgetUtils.createIsocolesTriangle(auxArrowBounds, WidgetUtils.FACING_SOUTH);

		if((c1 != null) && (c2 != null)){
			g2d.setPaint(Color.BLACK);
		}
		else{
			g2d.setColor(Color.GRAY);
		}
		g2d.fill(auxArrow);
	}
	
	public void setMainArrowDimension(int mainArrowDimension){
		this.mainArrowDimension = mainArrowDimension;
	}
	
	public int getMainArrowDimension(){
		return mainArrowDimension;
	}
	
	public void paintDisabled(Graphics g){
		drawMainIcon(g, null, null, 0);
		drawAuxIcon(g, null, null, 0);
		/*
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setPaint(Color.LIGHT_GRAY);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0.50f));
		g2d.fillRect(0, 0, getSize().width, getSize().height);
		*/
	}
	
	public void paintPushedOut(Graphics g){
		if(!isEnabled()){
			paintDisabled(g);
			return;
		}
		if(hasKeyboardFocus()){
			paintLightened(g);
			return;
		}
		drawMainIcon(g, Colors.PALE_COBALT, Colors.DARK_COBALT, 0);
		drawAuxIcon(g, Colors.PALE_COBALT, Colors.DARK_COBALT, 0);
	}
	
	public void paintLightened(Graphics g){
		if(!isEnabled()){
			paintDisabled(g);
			return;
		}
		drawMainIcon(g, Colors.PALE_COBALT, Colors.MEDIUM_COBALT, 0);
		drawAuxIcon(g, Colors.PALE_COBALT, Colors.MEDIUM_COBALT, 0);

		Graphics2D g2d = (Graphics2D)g.create();
		Rectangle mainBounds = getMainBounds(getSize());
		Rectangle auxBounds = getAuxBounds(getSize());
		
		//Border border = new EtchedBorder(EtchedBorder.RAISED);
		Border border = new BevelBorder(BevelBorder.RAISED);
		border.paintBorder(this, g, mainBounds.x, mainBounds.y, mainBounds.width, mainBounds.height);
		border.paintBorder(this, g, auxBounds.x, auxBounds.y, auxBounds.width, auxBounds.height);

		if(hasKeyboardFocus){
			Border lineBorder = new LineBorder(Colors.MEDIUM_COBALT);
			lineBorder.paintBorder(this, g, 0, 0, getSize().width, getSize().height);
		}
	}

	public void paintPushedIn(Graphics g) {
		if(!isEnabled()){
			paintDisabled(g);
			return;
		}
		Graphics2D g2d = (Graphics2D)g.create();
		Rectangle mainBounds = getMainBounds(getSize());
		Rectangle auxBounds = getAuxBounds(getSize());
		
		//Border border = new EtchedBorder(EtchedBorder.LOWERED);
		Border loweredBorder = new BevelBorder(BevelBorder.LOWERED);
		Border raisedBorder = new BevelBorder(BevelBorder.RAISED);
		
		if(selectedSection == SECTION_MAIN){
			drawMainIcon(g, Colors.DARK_COBALT, Colors.PALE_COBALT, 1);
			drawAuxIcon(g, Colors.PALE_COBALT, Colors.DARK_COBALT, 0);
			loweredBorder.paintBorder(this, g, mainBounds.x, mainBounds.y, mainBounds.width, mainBounds.height);
			raisedBorder.paintBorder(this, g, auxBounds.x, auxBounds.y, auxBounds.width, auxBounds.height);
		}
		else{
			drawMainIcon(g, Colors.PALE_COBALT, Colors.MEDIUM_COBALT, 0);
			drawAuxIcon(g, Colors.DARK_COBALT, Colors.PALE_COBALT, 1);
			raisedBorder.paintBorder(this, g, mainBounds.x, mainBounds.y, mainBounds.width, mainBounds.height);
			loweredBorder.paintBorder(this, g, auxBounds.x, auxBounds.y, auxBounds.width, auxBounds.height);
		}
		if(hasKeyboardFocus){
			Border lineBorder = new LineBorder(Colors.MEDIUM_COBALT);
			lineBorder.paintBorder(this, g, 0, 0, getSize().width, getSize().height);
		}
	}

	public Dimension getPreferredSize(){
		return new Dimension((int)(mainArrowDimension * 1.40f), mainArrowDimension);
	}

	public Dimension getMaximumSize(){
		return new Dimension((int)(mainArrowDimension * 1.40f), mainArrowDimension);
	}
}
