package gui;

import general.Settings;




import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent; 


public class GUIComponentLights extends JComponent {
		
	int sizeX;
	int sizeY;
	int windowSizeX;
	int windowSizeY;
	
	int width = 4;
	int height = width;
	
	int[] lights;
	
	public GUIComponentLights(int[] theLights)
	{
		sizeX = Settings.sizex;
		sizeY = Settings.sizey;
		windowSizeX = Settings.sizexFrame;
		windowSizeY = Settings.sizeyFrame;
		lights = theLights;
		
	}
		
	public void paintComponent(Graphics g)
	{	
		Graphics2D g2 = (Graphics2D) g;

		double stepSizeX = windowSizeX/(sizeX+1);
		double stepSizeY = windowSizeY/(sizeY+1);
		int loops = 0;

		for(int i = (int) stepSizeY; i < windowSizeY; i += stepSizeY)
			for(int j = (int) stepSizeX; j < windowSizeX; j += stepSizeX)
			{
				Ellipse2D.Double ellipseTop 	= new Ellipse2D.Double(i-2, j-6, width, height);
				Ellipse2D.Double ellipseButtom 	= new Ellipse2D.Double(i-2, j+2, width, height);
				Ellipse2D.Double ellipseLeft 	= new Ellipse2D.Double(i-6, j-2, width, height);
				Ellipse2D.Double ellipseRight 	= new Ellipse2D.Double(i+2, j-2, width, height);
	
				Color colorUD = Color.red;
				Color colorLR = Color.red;
				if(lights[loops] == 0)
					colorUD = Color.green;
				else
					colorLR = Color.green;
				loops++;
				
				g2.setPaint(colorUD);				
				g2.draw(ellipseTop);
				g2.fill(ellipseTop);
				g2.draw(ellipseButtom);
				g2.fill(ellipseButtom);
				
				g2.setPaint(colorLR);
				g2.draw(ellipseLeft);
				g2.fill(ellipseLeft);
				g2.draw(ellipseRight);
				g2.fill(ellipseRight);
			}

	}

}