
package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class TestTable {

	public static void main(String[] args){
		String tableString =
		  "<table cols=2 rows=4 border=.5>" +
   		"<tr>" +
	  	  "<td colspan=2 bgcolor=#444444>Header Row</td>" +
		  "</tr>" +
		  "<tr>" +
		    "<td>One</td><td>Value of One</td>" +
		  "</tr>" +
  		"<tr bgcolor=#ffffff>" +
	  	  "<td>Two</td><td>Value of Two</td>" +
  		"</tr>" +
	  	"<tr>" +
		    "<td>Three<br>Four Five</td><td>Value of ThreeFourFive</td>" +
		  "</tr>" +
		  "</table>";
		  
		 JLabel l = new JLabel("<html>" + tableString + "</html>"){
		 	public void paint(Graphics g){
		 		Graphics2D g2d = (Graphics2D)g;
		 		Graphics2D gStyle = (Graphics2D)g2d.create();
		 		gStyle.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		 		gStyle.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		 		gStyle.setTransform(AffineTransform.getScaleInstance(0.75d, 0.75d));
		 		super.paint(gStyle);
		 	}
		 };
		 
		 JFrame f = new JFrame();
		 f.setSize(500, 400);
		 f.getContentPane().setLayout(new BorderLayout());
		 f.getContentPane().add("Center", l);
		 f.setVisible(true);
	}

}
