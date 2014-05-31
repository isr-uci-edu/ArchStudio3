package edu.uci.ics.widgets;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.plaf.UIResource;

import com.l2fprod.common.swing.JFontChooser;

public class WidgetUtils{
	/*
	public static void main(String[] args){
		System.out.println(getImageIcon("archstudio/critics/res/error.gif"));
		System.out.println(getImageIcon("res/arrowdown.gif"));
	}
	*/
	
	public static final Font SERIF_PLAIN_SMALL_FONT = new Font("Serif", Font.PLAIN, 10);
	public static final Font SERIF_BOLD_SMALL_FONT = new Font("Serif", Font.BOLD, 10);
	public static final Font SERIF_ITALIC_SMALL_FONT = new Font("Serif", Font.ITALIC, 10);
	public static final Font SERIF_BOLDITALIC_SMALL_FONT = new Font("Serif", Font.BOLD + Font.ITALIC, 10);

	public static final Font SERIF_PLAIN_MEDIUM_FONT = new Font("Serif", Font.PLAIN, 12);
	public static final Font SERIF_BOLD_MEDIUM_FONT = new Font("Serif", Font.BOLD, 12);
	public static final Font SERIF_ITALIC_MEDIUM_FONT = new Font("Serif", Font.ITALIC, 12);
	public static final Font SERIF_BOLDITALIC_MEDIUM_FONT = new Font("Serif", Font.BOLD + Font.ITALIC, 12);
	
	public static final Font SERIF_PLAIN_BIG_FONT = new Font("Serif", Font.PLAIN, 14);
	public static final Font SERIF_BOLD_BIG_FONT = new Font("Serif", Font.BOLD, 14);
	public static final Font SERIF_ITALIC_BIG_FONT = new Font("Serif", Font.ITALIC, 14);
	public static final Font SERIF_BOLDITALIC_BIG_FONT = new Font("Serif", Font.BOLD + Font.ITALIC, 14);
	
	public static final Font SANSSERIF_PLAIN_TINY_FONT = new Font("SansSerif", Font.PLAIN, 8);
	public static final Font SANSSERIF_PLAIN_SMALL_FONT = new Font("SansSerif", Font.PLAIN, 10);
	public static final Font SANSSERIF_BOLD_SMALL_FONT = new Font("SansSerif", Font.BOLD, 10);
	public static final Font SANSSERIF_ITALIC_SMALL_FONT = new Font("SansSerif", Font.ITALIC, 10);
	public static final Font SANSSERIF_BOLDITALIC_SMALL_FONT = new Font("SansSerif", Font.BOLD + Font.ITALIC, 10);

	public static final Font SANSSERIF_PLAIN_MEDIUM_FONT = new Font("SansSerif", Font.PLAIN, 12);
	public static final Font SANSSERIF_BOLD_MEDIUM_FONT = new Font("SansSerif", Font.BOLD, 12);
	public static final Font SANSSERIF_ITALIC_MEDIUM_FONT = new Font("SansSerif", Font.ITALIC, 12);
	public static final Font SANSSERIF_BOLDITALIC_MEDIUM_FONT = new Font("SansSerif", Font.BOLD + Font.ITALIC, 12);
	
	public static final Font SANSSERIF_PLAIN_BIG_FONT = new Font("SansSerif", Font.PLAIN, 14);
	public static final Font SANSSERIF_BOLD_BIG_FONT = new Font("SansSerif", Font.BOLD, 14);
	public static final Font SANSSERIF_ITALIC_BIG_FONT = new Font("SansSerif", Font.ITALIC, 14);
	public static final Font SANSSERIF_BOLDITALIC_BIG_FONT = new Font("SansSerif", Font.BOLD + Font.ITALIC, 14);
	
	public static final Font MONOSPACE_PLAIN_SMALL_FONT = new Font("Monospaced", Font.PLAIN, 10);
	public static final Font MONOSPACE_BOLD_SMALL_FONT = new Font("Monospaced", Font.BOLD, 10);
	public static final Font MONOSPACE_ITALIC_SMALL_FONT = new Font("Monospaced", Font.ITALIC, 10);
	public static final Font MONOSPACE_BOLDITALIC_SMALL_FONT = new Font("Monospaced", Font.BOLD + Font.ITALIC, 10);

	public static final Font MONOSPACE_PLAIN_MEDIUM_FONT = new Font("Monospaced", Font.PLAIN, 12);
	public static final Font MONOSPACE_BOLD_MEDIUM_FONT = new Font("Monospaced", Font.BOLD, 12);
	public static final Font MONOSPACE_ITALIC_MEDIUM_FONT = new Font("Monospaced", Font.ITALIC, 12);
	public static final Font MONOSPACE_BOLDITALIC_MEDIUM_FONT = new Font("Monospaced", Font.BOLD + Font.ITALIC, 12);
	
	public static final Font MONOSPACE_PLAIN_BIG_FONT = new Font("Monospaced", Font.PLAIN, 14);
	public static final Font MONOSPACE_BOLD_BIG_FONT = new Font("Monospaced", Font.BOLD, 14);
	public static final Font MONOSPACE_ITALIC_BIG_FONT = new Font("Monospaced", Font.ITALIC, 14);
	public static final Font MONOSPACE_BOLDITALIC_BIG_FONT = new Font("Monospaced", Font.BOLD + Font.ITALIC, 14);
	
	public static final int NORTHWEST = 0;
	public static final int NORTH = 1;
	public static final int NORTHEAST = 2;
	public static final int EAST = 3;
	public static final int SOUTHEAST = 4;
	public static final int SOUTH = 5;
	public static final int SOUTHWEST = 6;
	public static final int WEST = 7;

	public static final int FACING_NORTH = 101;
	public static final int FACING_EAST = 102;
	public static final int FACING_SOUTH = 103;
	public static final int FACING_WEST = 104;
	
	public static JScrollPane adjustScrollPaneMovement(JScrollPane p){
		p.getHorizontalScrollBar().setUnitIncrement(10);
		p.getVerticalScrollBar().setUnitIncrement(10);
		return p;
	}
	
	public static long calcLineLength(int x1, int y1, int x2, int y2){
		int dx = x2 - x1;
		int dy = y2 - y1;
		return Math.round(Math.sqrt((dx * dx) + (dy * dy)));
	}
	
	public static Frame getAncestorFrame(Component c){
		Component comp = c;
		while(true){
			if(comp instanceof Frame){
				return (Frame)comp;
			}
			else{
				comp = comp.getParent();
				if(comp == null){
					return null;
				}
			}
		}
	}
	
	public static Dialog getAncestorDialog(Component c){
		Component comp = c;
		while(true){
			if(comp instanceof Dialog){
				return (Dialog)comp;
			}
			else{
				comp = comp.getParent();
				if(comp == null){
					return null;
				}
			}
		}
	}
	
	public static void centerInScreen(java.awt.Window w){
		// Get toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		// Get size
		Dimension screenDimension = toolkit.getScreenSize();
		Dimension windowDimension = w.getSize();
		
		int screenCenterX = screenDimension.width / 2;
		int screenCenterY = screenDimension.height / 2;
		
		int ulx = screenCenterX - (windowDimension.width / 2);
		int uly = screenCenterY - (windowDimension.height / 2);
		w.setLocation(ulx, uly);
	}
	
	public static void centerInFrame(java.awt.Window parent, java.awt.Window child){
		if(parent == null){
			centerInScreen(child);
			return;
		}
		Rectangle parentBounds = parent.getBounds();
		int parentCenterX = parentBounds.x + (parentBounds.width / 2);
		int parentCenterY = parentBounds.y + (parentBounds.height / 2);

		Dimension childDimension = child.getSize();
		int ulx = parentCenterX - (childDimension.width / 2);
		int uly = parentCenterY - (childDimension.height / 2);

		child.setLocation(ulx, uly);
	}
	
	public static void setWindowPosition(java.awt.Window w, int compassPoint, int offsetPercentage){
		// Get toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		// Get size
		Dimension screenDimension = toolkit.getScreenSize();
		Dimension windowDimension = w.getSize();
		
		int screenCenterX = screenDimension.width / 2;
		int screenCenterY = screenDimension.height / 2;
		
		int culx = screenCenterX - (windowDimension.width / 2);
		int culy = screenCenterY - (windowDimension.height / 2);
		
		int ulx, uly;
		
		double offset = ((double)offsetPercentage) / 100.0d;
		
		int offsetX = (int)((double)(screenDimension.width / 2) * offset);
		int offsetY = (int)((double)(screenDimension.height / 2) * offset);
		
		switch(compassPoint){
			case NORTHWEST:
			default:
			ulx = 0 + offsetX;
			uly = 0 + offsetY;
			break;
			
			case NORTH:
			ulx = culx;
			uly = 0 + offsetY;
			
			case NORTHEAST:
			ulx = screenDimension.width - windowDimension.width - offsetX;
			uly = 0 + offsetY;
			
			case WEST:
			ulx = 0 + offsetX;
			uly = culy;
			
			case EAST:
			ulx = screenDimension.width - windowDimension.width - offsetX;
			uly = culy;
			
			case SOUTHWEST:
			ulx = 0 - offsetX;
			uly = screenDimension.height - windowDimension.height - offsetY;
			
			case SOUTH:
			ulx = culx;
			uly = screenDimension.height - windowDimension.height - offsetY;
			
			case SOUTHEAST:
			ulx = screenDimension.width - windowDimension.width - offsetX;
			uly = screenDimension.height - windowDimension.height - offsetY;
		}
		
		w.setLocation(ulx, uly);
	}
	
	public static ImageIcon getImageIcon(String resourceName){
		InputStream resourceInputStream = null;
		resourceInputStream = WidgetUtils.class.getResourceAsStream(resourceName);
		if(resourceInputStream == null){
			resourceInputStream = ClassLoader.getSystemResourceAsStream(resourceName);
		}
		if(resourceInputStream == null){
			return null;
		}
		return getImageIcon(resourceInputStream);
	}
	
	public static ImageIcon getImageIcon(InputStream resourceInputStream){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte buf[] = new byte[2048];
			while(true){
				int len = resourceInputStream.read(buf, 0, buf.length);
				if(len != -1){
					baos.write(buf, 0, len);
				}
				else{
					resourceInputStream.close();
					baos.flush();
					baos.close();
					return new ImageIcon(baos.toByteArray());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Point calcPointOnLineAtDist (Point Point1, Point Point2, int Dist){
		double A = Point2.getX()-Point1.getX();
		double B = -(Point2.getY()-Point1.getY()); //negate for graphic coords
		if (A == 0) {
			if(B < 0){
				return new Point(Point1.x, Point1.y+Dist);
			}
			else{
				return new Point(Point1.x, Point1.y-Dist);
			}
		}
		double angle = Math.atan(B/A);
		double a = Dist * Math.cos(angle);
		double b = Dist * Math.sin(angle);
		int ai = (int)Math.round(a);
		int bi = (int)Math.round(b);
		
		//System.out.println("B= " + B);
		//System.out.println("bist = " + b);
		if (A > 0) {
			return new Point((int)(Point1.x+ai), (int)(Point1.y-bi));
		}
		else {
			return new Point((int)(Point1.x-ai), (int)(Point1.y+bi));
		}
	}
	
	public static Point calcPointOnRay(Point Point1, double angle, int Dist){
		double a = Dist * Math.cos(angle);
		int ai = (int)Math.round(a);
		double b = Dist * Math.sin(angle);
		int bi = (int)Math.round(b);
		return new Point((int)(Point1.x+ai), (int)(Point1.y-bi));
	}
	
	public static JMenu getSubMenu(JMenuBar mb, String label){
		int itemCount = mb.getMenuCount();
		for(int i = 0; i < itemCount; i++){
			JMenu m = mb.getMenu(i);
			if(m != null){
				String s = m.getText();
				if((s != null) && (s.equals(label))){
					return m;
				}
			}
		}
		return null;
	}
	
	public static JMenuItem getMenuItem(JMenu m, String label){
		int itemCount = m.getItemCount();
		for(int i = 0; i < itemCount; i++){
			JMenuItem mi = m.getItem(i);
			if(mi != null){
				String s = mi.getText();
				if((s != null) && (s.equals(label))){
					return mi;
				}
			}
		}
		return null;
	}
			
	public static void setMnemonic(AbstractButton b, char ch){
		int keyEvent = -1;
		switch(ch){
		case 'A':
			keyEvent = KeyEvent.VK_A;
			break;
		case 'B':
			keyEvent = KeyEvent.VK_B;
			break;
		case 'C':
			keyEvent = KeyEvent.VK_C;
			break;
		case 'D':
			keyEvent = KeyEvent.VK_D;
			break;
		case 'E':
			keyEvent = KeyEvent.VK_E;
			break;
		case 'F':
			keyEvent = KeyEvent.VK_F;
			break;
		case 'G':
			keyEvent = KeyEvent.VK_G;
			break;
		case 'H':
			keyEvent = KeyEvent.VK_H;
			break;
		case 'I':
			keyEvent = KeyEvent.VK_I;
			break;
		case 'J':
			keyEvent = KeyEvent.VK_J;
			break;
		case 'K':
			keyEvent = KeyEvent.VK_K;
			break;
		case 'L':
			keyEvent = KeyEvent.VK_L;
			break;
		case 'M':
			keyEvent = KeyEvent.VK_M;
			break;
		case 'N':
			keyEvent = KeyEvent.VK_N;
			break;
		case 'O':
			keyEvent = KeyEvent.VK_O;
			break;
		case 'P':
			keyEvent = KeyEvent.VK_P;
			break;
		case 'Q':
			keyEvent = KeyEvent.VK_Q;
			break;
		case 'R':
			keyEvent = KeyEvent.VK_R;
			break;
		case 'S':
			keyEvent = KeyEvent.VK_S;
			break;
		case 'T':
			keyEvent = KeyEvent.VK_T;
			break;
		case 'U':
			keyEvent = KeyEvent.VK_U;
			break;
		case 'V':
			keyEvent = KeyEvent.VK_V;
			break;
		case 'W':
			keyEvent = KeyEvent.VK_W;
			break;
		case 'X':
			keyEvent = KeyEvent.VK_X;
			break;
		case 'Y':
			keyEvent = KeyEvent.VK_Y;
			break;
		case 'Z':
			keyEvent = KeyEvent.VK_Z;
			break;
		case '0':
			keyEvent = KeyEvent.VK_0;
			break;
		case '1':
			keyEvent = KeyEvent.VK_1;
			break;
		case '2':
			keyEvent = KeyEvent.VK_2;
			break;
		case '3':
			keyEvent = KeyEvent.VK_3;
			break;
		case '4':
			keyEvent = KeyEvent.VK_4;
			break;
		case '5':
			keyEvent = KeyEvent.VK_5;
			break;
		case '6':
			keyEvent = KeyEvent.VK_6;
			break;
		case '7':
			keyEvent = KeyEvent.VK_7;
			break;
		case '8':
			keyEvent = KeyEvent.VK_8;
			break;
		case '9':
			keyEvent = KeyEvent.VK_9;
			break;
		default:
			throw new IllegalArgumentException("Invalid key.");
		}
		
		b.setMnemonic(keyEvent);
		int index = b.getText().indexOf(ch);
		if(index != -1){
			b.setDisplayedMnemonicIndex(index);
		}
		else{
			index = b.getText().toUpperCase().indexOf(ch);
			if(index != -1){
				b.setDisplayedMnemonicIndex(index);
			}
		}
	}
	
	public static void addKeyListenerToAll(Container c, KeyListener kl){
		c.addKeyListener(kl);
		int numComponents = c.getComponentCount();
		for(int i = 0; i < numComponents; i++){
			Component comp = c.getComponent(i);
			if(comp instanceof Container){
				addKeyListenerToAll((Container)comp, kl);
			}
			else{
				comp.addKeyListener(kl);
			}
		}
	}
	
	
	static class MutableTreeNodeComparator implements Comparator{
		public int compare(Object o1, Object o2){
			javax.swing.tree.MutableTreeNode tn1 = (javax.swing.tree.MutableTreeNode)o1;
			javax.swing.tree.MutableTreeNode tn2 = (javax.swing.tree.MutableTreeNode)o2;
			return tn1.toString().compareToIgnoreCase(tn2.toString());
		}
	}
	
	static MutableTreeNodeComparator defaultMutableTreeNodeComparator =
		new MutableTreeNodeComparator();
	
	public static void addTreeNodeAlphabetically(javax.swing.tree.MutableTreeNode parent, javax.swing.tree.MutableTreeNode child){
		addTreeNodeAlphabetically(parent, child, defaultMutableTreeNodeComparator);
	}
	
	public static void addTreeNodeAlphabetically(javax.swing.tree.MutableTreeNode parent, javax.swing.tree.MutableTreeNode child, Comparator comparator){
		int numChildren = parent.getChildCount();
		for(int i = 0; i < numChildren; i++){
			javax.swing.tree.TreeNode tn = parent.getChildAt(i);
			//String s = tn.toString().toUpperCase();
			if(comparator.compare(child, tn) < 0){
				parent.insert(child, i);
				return;
			}
		}
		parent.insert(child, numChildren);
	}
	
	public static java.awt.Component[] getHierarchyRecursive(java.awt.Component c){
		ArrayList l = new ArrayList();
		l.add(c);
		if(c instanceof java.awt.Container){
			getHierarchyRecursive(l, (java.awt.Container)c);
		}
		return (java.awt.Component[])l.toArray(new java.awt.Component[0]);
	}
	
	private static void getHierarchyRecursive(ArrayList l, java.awt.Container c){
		java.awt.Component[] children = c.getComponents();
		for(int i = 0; i < children.length; i++){
			l.add(children[i]);
			if(children[i] instanceof java.awt.Container){
				getHierarchyRecursive(l, (java.awt.Container)children[i]);
			}
		}
	}

	public static Polygon createIsocolesTriangle(Rectangle boundingBox, int facing){
		int x1, x2, x3;
		int y1, y2, y3;
		
		switch(facing){
		case FACING_NORTH:
			x1 = boundingBox.x + (boundingBox.width / 2);
			y1 = boundingBox.y;
			x2 = boundingBox.x;
			y2 = boundingBox.y + boundingBox.height;
			x3 = boundingBox.x + boundingBox.width;
			y3 = boundingBox.y + boundingBox.height;
			break;
		case FACING_SOUTH:
			x1 = boundingBox.x + (boundingBox.width / 2);
			y1 = boundingBox.y + boundingBox.height;			
			x2 = boundingBox.x;
			y2 = boundingBox.y;
			x3 = boundingBox.x + boundingBox.width;
			y3 = boundingBox.y;
			break;
		case FACING_EAST:
			x1 = boundingBox.x + boundingBox.width;
			y1 = boundingBox.y + (boundingBox.height / 2);
			x2 = boundingBox.x;
			y2 = boundingBox.y;
			x3 = boundingBox.x;
			y3 = boundingBox.y + boundingBox.height;
			break;
		case FACING_WEST:
			x1 = boundingBox.x;
			y1 = boundingBox.y + (boundingBox.height / 2);
			x2 = boundingBox.x + boundingBox.width;
			y2 = boundingBox.y;
			x3 = boundingBox.x + boundingBox.width;
			y3 = boundingBox.y + boundingBox.height;
			break;
		default:
			throw new IllegalArgumentException("Invalid facing");
		}
		
		Polygon triangle = new Polygon();
		triangle.addPoint(x1, y1);
		triangle.addPoint(x2, y2);
		triangle.addPoint(x3, y3);
		triangle.addPoint(x1, y1);
		return triangle;		
	}
	
	public static Rectangle expand(Rectangle r, int pixels){
		Rectangle r2 = new Rectangle(r);
		r2.x -= pixels;
		r2.y -= pixels;
		r2.width += pixels + pixels;
		r2.height += pixels + pixels;
		return r2;
	}

	public static Rectangle contract(Rectangle r, int pixels){
		Rectangle r2 = new Rectangle(r);
		r2.x += pixels;
		r2.y += pixels;
		r2.width -= pixels + pixels;
		r2.height -= pixels + pixels;
		return r2;
	}
	
	public static JButton getPopupMenuButton(String text, Icon icon, JMenuItem[] menuItems){
		JButton b = new JButton(text, icon){
			public void setMargin(Insets m){
				Insets newInsets = new Insets(m.top + 1, m.left + 1, m.bottom + 1, m.right + 12);
				super.setMargin(newInsets);
			}
			
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D)g.create();
				Rectangle triangleBounds = new Rectangle(getWidth() - 15, (getHeight() / 2) - 2, 9, 5);
				if(isEnabled()){
					g2d.setPaint(Color.BLACK);
				}
				else{
					g2d.setPaint(Color.GRAY);
				}
				g2d.fill(createIsocolesTriangle(triangleBounds, FACING_SOUTH));
			}
		};
		
		b.setHorizontalAlignment(SwingConstants.LEFT);
		
		if((menuItems == null) || (menuItems.length == 0)){
			b.setEnabled(false);
		}
		else{
			final JPopupMenu menu = new JPopupMenu();
			for(int i = 0; i < menuItems.length; i++){
				menu.add(menuItems[i]);
			}
			
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					JButton theButton = (JButton)evt.getSource();
					menu.show(theButton, 0, theButton.getHeight());
				}
			});
		}
		return b;
	}
	
	public static void validateAndRepaintInAWTThread(final Container c){
		Runnable r = new Runnable(){
			public void run(){
				c.validate();
				c.repaint();
			}
		};
		SwingUtilities.invokeLater(r);
	}
	
	
	public static boolean isDark(Color c){
		int red = c.getRed();
		int green = c.getGreen();
		int blue = c.getBlue();
		return (red + green + blue) <= (500);
	}
	
	public static String getHexColorString(Color c){
		String redString = Integer.toHexString(c.getRed());
		String greenString = Integer.toHexString(c.getGreen());
		String blueString = Integer.toHexString(c.getBlue());
		if(redString.length() == 1) redString = "0" + redString;
		if(greenString.length() == 1) greenString = "0" + greenString;
		if(blueString.length() == 1) blueString = "0" + blueString;
		return redString + greenString + blueString;
	}
	
	public static Icon getColorIcon(Color c, Color trimColor, int height, int width){
		return new ColorIcon(c, trimColor, height, width);
	}
	
	public static Icon getCyclicColorIcon(Color[] colorCycle, Color trimColor, int height, int width){
		return new RainbowColorIcon(colorCycle, trimColor, height, width);
	}
	
	public static Icon getRainbowColorIcon(Color trimColor, int height, int width){
		return new RainbowColorIcon(trimColor, height, width);
	}
	
	static class ColorIcon implements Icon{
		protected Color color;
		protected Color trimColor;
		protected int height;
		protected int width;
		
		public ColorIcon(Color color, Color trimColor, int height, int width){
			this.color = color;
			this.trimColor = trimColor;
			this.height = height;
			this.width = width;
		}
		
		public int getIconHeight(){
			return width;
		}

		public int getIconWidth(){
			return height;
		}
		
		public void paintIcon(Component c, Graphics g, int x, int y){
			Graphics g2 = g.create();
			g2.setColor(color);
			g2.fillRect(x + 1, y + 1, width - 2, height - 2);
			g2.setColor(trimColor);
			g2.drawRect(x + 1, y + 1, width - 2, height - 2);
		}
	}

	static class RainbowColorIcon extends ColorIcon{
		protected Color[] cycle;
		
		public static final Color[] DEFAULT_CYCLE = new Color[]{
			Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA
		};
		
		public RainbowColorIcon(Color trimColor, int height, int width){
			super(null, trimColor, height, width);
			this.cycle = DEFAULT_CYCLE;
		}
		
		public RainbowColorIcon(Color[] cycle, Color trimColor, int height, int width){
			super(null, trimColor, height, width);
			this.cycle = cycle;
		}
		
		public void paintIcon(Component c, Graphics g, int x, int y){
			Graphics2D g2 = (Graphics2D)g.create();
			g2.setClip(x, y, width, height);
			int imax = (width > height) ? width : height;
			imax -= 2;
			imax *= 2;
			for(int i = 0; i < imax; i++){
				g2.setColor(cycle[(i/2) % cycle.length]);
				g2.drawLine(x + i + 1, y + 1, x + 1, y + i + 1);
			}
			g2.setColor(trimColor);
			g2.drawRect(x + 1, y + 1, width - 2, height - 2);
		}
	}
	
	public static Font displayFontChooserDialog(Component parent, String title, Font startingFont){
		return JFontChooser.showDialog(parent, title, startingFont);
	}
}
