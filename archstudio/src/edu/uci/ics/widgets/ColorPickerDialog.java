package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ColorPickerDialog extends JDialog{

	protected ColorScheme[] colorSchemes;
	
	protected Color oldSelectedColor;
	protected Color selectedColor;
	
	protected JComboBox cbColorScheme;
	protected JPanel pColorButtons;
	protected JTextField tfHexColor;
	protected JLabel lHexColor;
	protected JButton bHexColorOK;
	
	public static Color showDialog(Frame owner, String title, ColorScheme[] colorSchemes, Color oldSelectedColor){
		ColorPickerDialog cpd = new ColorPickerDialog(owner, title, colorSchemes, oldSelectedColor);
		WidgetUtils.centerInFrame(owner, cpd);
		cpd.setVisible(true);
		return cpd.getSelectedColor();
	}
	
	public static Color showDialog(Dialog owner, String title, ColorScheme[] colorSchemes, Color oldSelectedColor){
		ColorPickerDialog cpd = new ColorPickerDialog(owner, title, colorSchemes, oldSelectedColor);
		WidgetUtils.centerInFrame(owner, cpd);
		cpd.setVisible(true);
		return cpd.getSelectedColor();
	}
	
	public ColorPickerDialog(Frame owner, String title, ColorScheme[] colorSchemes, Color oldSelectedColor){
		super(owner, title, true);
		this.colorSchemes = colorSchemes;
		this.oldSelectedColor = oldSelectedColor;
		this.selectedColor = null;
		init();
	}
	
	public ColorPickerDialog(Dialog owner, String title, ColorScheme[] colorSchemes, Color oldSelectedColor){
		super(owner, title, true);
		this.colorSchemes = colorSchemes;
		this.oldSelectedColor = oldSelectedColor;
		this.selectedColor = null;
		init();
	}
	
	protected void init(){
		JPanel mainPanel = new JPanel();
		
		cbColorScheme = new JComboBox(colorSchemes);
		cbColorScheme.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setupColorButtonPanel();
				}
			}
		);
		
		JPanel schemeSelectionPanel = new JPanel();
		schemeSelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		schemeSelectionPanel.add(new JLabel("Color Scheme: "));
		schemeSelectionPanel.add(cbColorScheme);
		
		pColorButtons = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel selectBySchemePanel = new JPanel();
		selectBySchemePanel.setLayout(new BoxLayout(selectBySchemePanel, BoxLayout.Y_AXIS));
		selectBySchemePanel.add(schemeSelectionPanel);
		
		JPanel pp = new JPanel();
		pp.setLayout(new FlowLayout(FlowLayout.CENTER));
		pp.add(pColorButtons);
		
		selectBySchemePanel.add(pp);
		
		JPanel customColorButtonPanel = new JPanel();
		customColorButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JButton bSelectCustomColor = new JButton("Custom Color...");
		bSelectCustomColor.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					doSelectCustomColor();
				}
			}
		);
		customColorButtonPanel.add(bSelectCustomColor);
		
		JPanel hexColorPanel = new JPanel();
		hexColorPanel.setLayout(new BoxLayout(hexColorPanel, BoxLayout.Y_AXIS));
		
		JPanel hexColorSelectionPanel = new JPanel();
		hexColorSelectionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		tfHexColor = new JTextField(6);
		String hex = "000000";
		if(oldSelectedColor != null){
			hex = WidgetUtils.getHexColorString(oldSelectedColor);
		}
		tfHexColor.setText(hex);
		tfHexColor.getDocument().addDocumentListener(
		  new DocumentListener() {
		    public void changedUpdate(DocumentEvent e){
		    	setupHexColorLabel();
		    }
		    public void insertUpdate(DocumentEvent e) {
		    	setupHexColorLabel();
		    }
		    public void removeUpdate(DocumentEvent e) {
		    	setupHexColorLabel();
		    }
		  }
		);
		tfHexColor.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					bHexColorOK.doClick();
				}
			}
		);
		lHexColor = new JLabel("?");

		hexColorSelectionPanel.add(new JLabel("Hex: #"));
		hexColorSelectionPanel.add(tfHexColor);
		hexColorSelectionPanel.add(lHexColor);
		
		JPanel hexColorButtonPanel = new JPanel();
		hexColorButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bHexColorOK = new JButton("OK");
		bHexColorOK.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					Color c = getHexColor();
					if(c != null){
						doDone(c);
					}
				}
			}
		);
		hexColorButtonPanel.add(bHexColorOK);
		
		hexColorPanel.add(hexColorSelectionPanel);
		hexColorPanel.add(hexColorButtonPanel);
		
		JPanel customPanel = new JPanel();
		customPanel.setLayout(new GridLayout(1, 2));
		customPanel.add(new JPanelTL(customColorButtonPanel, "Custom Color", 2));
		customPanel.add(new JPanelTL(hexColorPanel, "Hex Color", 2));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton bCancel = new JButton("Cancel");
		bCancel.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					doDone(null);
				}
			}
		);
		buttonPanel.add(bCancel);
		
		mainPanel.add(new JPanelTL(selectBySchemePanel, "Select by Scheme", 2));
		mainPanel.add(customPanel);
		mainPanel.add(buttonPanel);
		
		setupColorButtonPanel();
		setupHexColorLabel();
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(new JPanelIS(mainPanel, 4));
		this.pack();
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					doDone(null);
				}
			}
		);
		
		WidgetUtils.validateAndRepaintInAWTThread(this);
	}
	
	protected void setupColorButtonPanel(){
		ColorScheme selectedColorScheme = (ColorScheme)cbColorScheme.getSelectedItem();
		pColorButtons.removeAll();
		if(selectedColorScheme == null){
			pColorButtons.setLayout(new BorderLayout());
			pColorButtons.add("Center", new JLabel("No color scheme selected."));
		}
		else{
			pColorButtons.setLayout(new GridLayout(selectedColorScheme.getNumSets(), 
				selectedColorScheme.getNumVariants()));
			for(int i = 0; i < selectedColorScheme.getNumSets(); i++){
				for(int j = 0; j < selectedColorScheme.getNumVariants(); j++){
					final Color c = selectedColorScheme.getColor(i, j);
					Color trimColor = Color.BLACK;
					if((oldSelectedColor != null) && (c.equals(oldSelectedColor))){
						trimColor = Color.RED;
					}
					Icon colorIcon = WidgetUtils.getColorIcon(c, trimColor, 16, 16);
					JButton bColor = new JButton(colorIcon);
					bColor.addActionListener(
						new ActionListener(){
							public void actionPerformed(ActionEvent evt){
								doDone(c);
							}
						}
					);
					pColorButtons.add(bColor);
				}
			}
		}
		WidgetUtils.validateAndRepaintInAWTThread(this);
	}
	
	protected Color getHexColor(){
		String hexString = tfHexColor.getText();
		if(hexString.length() != 6){
			return null;
		}
		else{
			try{
				int hexint = Integer.parseInt(hexString, 16);
				Color c = new Color(hexint);
				return c;
			}
			catch(NumberFormatException nfe){
			}
		}
		return null;
	}
	
	protected void setupHexColorLabel(){
		Color selectedColor = getHexColor();
		if(selectedColor == null){
			lHexColor.setIcon(null);
			lHexColor.setText("?");
			bHexColorOK.setEnabled(false);
		}
		else{
			Icon colorIcon = WidgetUtils.getColorIcon(selectedColor, Color.BLACK, 20, 20);
			lHexColor.setIcon(colorIcon);
			lHexColor.setText(null);
			bHexColorOK.setEnabled(true);
		}
		WidgetUtils.validateAndRepaintInAWTThread(this);
	}
	
	protected void doSelectCustomColor(){
		Color oc = oldSelectedColor;
		if(oc == null){
			oc = Color.GRAY;
		}
		Color c = JColorChooser.showDialog(this, "Choose Custom Color", oc);
		if(c != null){
			doDone(c);
		}
	}
	
	protected void doDone(Color c){
		this.selectedColor = c;
		this.setVisible(false);
		this.dispose();
	}
	
	public Color getSelectedColor(){
		return selectedColor;
	}
	
}
