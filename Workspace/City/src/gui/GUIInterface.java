package gui;

import java.awt.Color;
import java.net.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JComponent;
import javax.swing.JFrame;

import general.Settings;

public class GUIInterface 
{
	private JFrame frame = new JFrame();
	private Settings settings = new Settings();

	private GUIComponentLights lights;
	private GUIComponentCars cars;
	
	//Data containg lights and cars information
	int[] lightsData = new int[settings.sizex*settings.sizey];
	int[] carsData = new int[settings.sizex*settings.sizey*4];
	
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
		frame.setSize( settings.sizexFrame, settings.sizeyFrame +22);
		frame.setResizable(false);
		frame.setTitle(settings.nameFrame);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null); //Center of screen
		frame.setBackground(Color.lightGray);
		
		//Draw the roads
		GUIComponentRoads Roads = new GUIComponentRoads(settings);
		frame.add(Roads);		
		frame.setVisible(true);	
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
		lights = new GUIComponentLights(settings, lightsData);
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
		cars = new GUIComponentCars(settings, carsData);
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

