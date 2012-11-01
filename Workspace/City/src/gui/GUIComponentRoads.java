package gui;

import general.Settings;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JComponent; 

public class GUIComponentRoads extends JComponent
{
	int sizeX;
	int sizeY;
	int windowSizeX;
	int windowSizeY;
		
	public GUIComponentRoads(Settings settings)
	{
		sizeX = settings.sizex;
		sizeY = settings.sizey;
		windowSizeX = settings.sizexFrame;
		windowSizeY = settings.sizeyFrame;
	}
		
	public void paintComponent(Graphics g)
	{
		
		Graphics2D g2 = (Graphics2D) g;
		
		//Draw vertical roads
		double stepSizeX = windowSizeX/(sizeX+1); 
		for(int i = (int) stepSizeX; i < windowSizeX; i += stepSizeX)
		{
			Line2D.Double line = new Line2D.Double(i, 0, i, windowSizeY);
			g2.draw(line);
		}

		//Draw vertical roads
		double stepSizeY = windowSizeY/(sizeY+1); 
		for(int i = (int) stepSizeY; i < windowSizeY; i += stepSizeY)
		{
			Line2D.Double line = new Line2D.Double(0,i, windowSizeX, i);
			g2.draw(line);
		}
	}

		
}
