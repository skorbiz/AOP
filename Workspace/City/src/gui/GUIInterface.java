package gui;

import java.awt.Color;
import java.net.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JComponent;
import javax.swing.JFrame;

import general.Settings;
import general.StatisticInterface;

public class GUIInterface 
{
	private JFrame frame = new JFrame();

	private GUIComponentLights lights;
	private GUIComponentCars cars;
	
	//Data containg lights and cars information
	int[] lightsData = new int[Settings.sizex*Settings.sizey];
	int[] carsData = new int[Settings.sizex*Settings.sizey*4];
	
	public GUIInterface()
	{
		for(int i = 0; i < carsData.length; i++)
			carsData[i] = i;
		
		createFrame();
		drawLights();
		drawCars();
	}

	private void createFrame()
	{	
		//Creates The Frame
		frame.setSize( Settings.sizexFrame, Settings.sizeyFrame +22);
		frame.setResizable(false);
		frame.setTitle(Settings.nameFrame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null); //Center of screen
		frame.setBackground(Color.lightGray);
		
		//Draw the roads
		GUIComponentRoads Roads = new GUIComponentRoads();
		frame.add(Roads);		
		frame.setVisible(true);	
		
		//Set close oporation
		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				StatisticInterface stat = StatisticInterface.getInstance();
				stat.calculateAndSaveStatistics();
				System.exit(0);
			}
		});
	}
	
	//Update the drawn lights
	public void updateLights(int[] theLights)
	{
		mergeDataArrays(theLights, lightsData);
		frame.remove(lights);
		drawLights();
	}

	//Draw the lights
	private void drawLights()
	{
		lights = new GUIComponentLights(lightsData);
		frame.add(lights);
		frame.setVisible(true);
	}

	//Update the drawn cars
	public void updateCars(int[] theCars)
	{
		mergeDataArrays(theCars, carsData);
		frame.remove(cars);
		drawCars();
	}
	
	//Draw cars
	private void drawCars()
	{
		cars = new GUIComponentCars(carsData);
		frame.add(cars);
		frame.setVisible(true);
	}
	
	
	//Merge old data set with a new one
	private void mergeDataArrays(int[] inputNew, int[] outputOld)
	{
		if(inputNew.length != outputOld.length)
			System.out.println("GUI failded in merging data arrays");
		
		for(int i = 0; i < inputNew.length; i++)
			if(inputNew[i] != -1)
				outputOld[i] = inputNew[i];
	}
	
}

