package gui;

import general.Settings;

import java.awt.*;
import javax.swing.JComponent; 

public class GUIComponentCars extends JComponent
{
	public int sizeX;
	public int sizeY;
	public int windowSizeX;
	public int windowSizeY;
	
	int[] cars;
	
	public GUIComponentCars(Settings settings, int[] theCars)
	{
		sizeX = settings.sizex;
		sizeY = settings.sizey;
		windowSizeX = settings.sizexFrame;
		windowSizeY = settings.sizeyFrame;
		
		cars = theCars;
	}
		
	public void paintComponent(Graphics g)
	{

		Graphics2D g2 = (Graphics2D) g;

		g2.setFont(new Font("Courier", Font.BOLD, 12));
		g2.setColor(Color.black);
		
		double stepSizeX = windowSizeX/(sizeX+1);
		double stepSizeY = windowSizeY/(sizeY+1);
		int loops = 0;
		for(int i = (int) stepSizeX; i < windowSizeX; i += stepSizeX)
			for(int j = (int) stepSizeY; j < windowSizeY; j += stepSizeY)
			{
				g2.drawString(Integer.toString(cars[loops  ]), i-15, j-15);		//TOP
				g2.drawString(Integer.toString(cars[loops+1]), i+1	, j+25);	//BOTTOM
				g2.drawString(Integer.toString(cars[loops+2]), i-30, j+10);		//LEFT
				g2.drawString(Integer.toString(cars[loops+3]), i+15, j-1);		//RIGHT
				loops+=4;
			}
		
		

	}

		
}
