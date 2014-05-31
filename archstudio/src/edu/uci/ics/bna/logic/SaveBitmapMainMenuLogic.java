package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.thumbnail.Thumbnail;
import edu.uci.ics.widgets.GenericFileFilter;
import edu.uci.ics.widgets.WidgetUtils;

public class SaveBitmapMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	protected IRecentDirectory recentDirectory;
	protected JMenu viewMenu;
	protected JMenuItem miSavePNG;
	protected JSeparator miSeparator;
	
	public SaveBitmapMainMenuLogic(JMenuBar mainMenu){
		this(mainMenu, null);
	}
	
	public SaveBitmapMainMenuLogic(JMenuBar mainMenu, IRecentDirectory recentDirectory){
		super(mainMenu);
		this.recentDirectory = recentDirectory;
		viewMenu = WidgetUtils.getSubMenu(mainMenu, "View");
		if(viewMenu == null){
			viewMenu = new JMenu("View");
			WidgetUtils.setMnemonic(viewMenu, 'V');
			mainMenu.add(viewMenu);
		}
		
		miSavePNG = new JMenuItem("Save Diagram as PNG...");
		//miSavePNG.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.Event.CTRL_MASK));
		miSavePNG.addActionListener(this);
		
		viewMenu.add(miSavePNG);
		miSeparator = new JSeparator();
		viewMenu.add(miSeparator);
	}
	
	public void destroy(){
		viewMenu.remove(miSavePNG);
		viewMenu.remove(miSeparator);
		if(viewMenu.getItemCount() == 0){
			getMainMenu().remove(viewMenu);
		}
	}
	
	protected double getZoomToSave(){
		Object[] possibleValues = { "1600%", "800%", "500%", "450%", "400%", "350%", "300%", "250%", "200%", "175%", "150%", "125%", "100%", "90%", "80%", "75%", "70%", "60%", "50%", "40%", "30%", "25%", "20%", "10%", "Other..." };
		Object selectedValue = JOptionPane.showInputDialog(getBNAComponent(), 
			"Choose Zoom Percentage to Save At", "Choose Zoom",
		JOptionPane.INFORMATION_MESSAGE, null,
		possibleValues, "100%");
		
		if(selectedValue == null){
			return -1;
		}
		else{
			String s = (String)selectedValue;
			s = s.trim();
			if(s.startsWith("Other")){
				String newZoomValue = 
					JOptionPane.showInputDialog("Zoom Percentage");
				if(newZoomValue == null){
					return -1;
				}
				s = newZoomValue.trim();
			}
			
			if(s.endsWith("%")){
				s = s.substring(0, s.length() - 1);
				s = s.trim();
			}
			try{
				double d = Double.parseDouble(s);
				d /= 100.0d;
				if(d <= 0.0d){
					throw new NumberFormatException();
				}
				return d;
			}
			catch(NumberFormatException nfe){
				JOptionPane.showMessageDialog(getBNAComponent(), 
					"Value must be a positive nonzero number", "Error", JOptionPane.ERROR_MESSAGE);
				return -1;
			}
		}
	}
	
	protected File getFileToSave(){
		String startingDirectory = recentDirectory.getRecentDirectory();
		if(startingDirectory == null){
			startingDirectory = ".";
		}

		JFileChooser chooser = new JFileChooser(startingDirectory);

		GenericFileFilter xadlFileFilter = new GenericFileFilter();
		xadlFileFilter.addExtension("png");
		xadlFileFilter.setDescription("Portable Network Graphics");
		chooser.addChoosableFileFilter(xadlFileFilter);
		chooser.setFileFilter(xadlFileFilter);

		int returnVal = chooser.showSaveDialog(getBNAComponent());
		
		if(recentDirectory != null){
			File currentDir = chooser.getCurrentDirectory();
			if(currentDir != null){
				recentDirectory.setRecentDirectory(currentDir.getPath());
			}
		}
		
		if(returnVal == JFileChooser.APPROVE_OPTION){
			try{
				File f = chooser.getSelectedFile();
				String fileName = f.getName();
				if(!fileName.toLowerCase().endsWith(".png")){
					String pathName = f.getPath();
					pathName += ".png";
					f = new File(pathName);
				}
				if(f.exists()){
					int result = JOptionPane.showConfirmDialog(getBNAComponent(),
						"Overwrite Existing File?", "Confirm Overwrite",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(result != JOptionPane.YES_OPTION){
						return null;
					}
				}
				
				return f;
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(getBNAComponent(), e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == miSavePNG){
			double zoom = getZoomToSave();
			if(zoom == -1){
				return;
			}
			
			Thumbnail thumbnail = new Thumbnail(getBNAComponent(), getBNAComponent().getModel());
			Rectangle thumbnailWorldBounds = thumbnail.getThumbnailWorldBounds();
			if(zoom != 1.0d){
				thumbnailWorldBounds.width = (int)((double)thumbnailWorldBounds.width * zoom) + 1;
				thumbnailWorldBounds.height = (int)((double)thumbnailWorldBounds.height * zoom) + 1;
			}
			
			int numPixels = thumbnailWorldBounds.width * thumbnailWorldBounds.height;
			if((thumbnailWorldBounds.width > 5000) || (thumbnailWorldBounds.height > 5000)){
				int result = JOptionPane.showConfirmDialog(getBNAComponent(), 
					"This image will be " + thumbnailWorldBounds.width + "x" + thumbnailWorldBounds.height + 
					"pixels in size.  Continue saving?", "Big Image Warning", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.NO_OPTION){
					return;
				}
			}
			
			File f = getFileToSave();
			if(f == null){
				return;
			}
			
			try{
				BufferedImage bufferedImage = new BufferedImage(thumbnailWorldBounds.width + 10,
					thumbnailWorldBounds.height + 10, BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D g2d = bufferedImage.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

				g2d.setPaint(Color.WHITE);
				g2d.fillRect(0, 0, thumbnailWorldBounds.width + 10, thumbnailWorldBounds.height + 10);
				thumbnail.drawThumbnail(g2d, 4, 4, thumbnailWorldBounds.width, thumbnailWorldBounds.height);
				try{
					ImageIO.write(bufferedImage, "png", f);
				}
				catch(IOException ioe){
					JOptionPane.showMessageDialog(getBNAComponent(), ioe.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					ioe.printStackTrace();
				}
				bufferedImage.flush();
				bufferedImage = null;
			}
			catch(OutOfMemoryError oome){
				JOptionPane.showMessageDialog(getBNAComponent(), 
					"Out of memory; can't save image. Try a smaller zoom level.", "Out of Memory Saving", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
}

