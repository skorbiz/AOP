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
	
	
	public GUIInterface()
	{
		int[] lightsDefault = new int[settings.sizex*settings.sizey];
		int[] carsDefault = new int[settings.sizex*settings.sizey*4];
		for(int i = 0; i < carsDefault.length; i++)
			carsDefault[i] = i;
		
		createFrame();
		drawLights(lightsDefault);
		drawCars(carsDefault);
		
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
		//frame.addMouseListener(this);
		
		//Draw the roads
		GUIComponentRoads Roads = new GUIComponentRoads(settings);
		frame.add(Roads);		
		frame.setVisible(true);	
	}
	
	//Update the drawn cars
	public void updateLights(int[] theLights)
	{
		frame.remove(lights);
		drawCars(theLights);
	}

	//Draw the lights
	private void drawLights(int[] theLights)
	{
		lights = new GUIComponentLights(settings, theLights);
		frame.add(lights);
		frame.setVisible(true);
	}

	//Update the drawn cars
	public void updateCars(int[] theCars)
	{
		frame.remove(cars);
		drawCars(theCars);
	}
	
	//Draw cars
	private void drawCars(int[] theCars)
	{
		cars = new GUIComponentCars(settings, theCars);
		frame.add(cars);
		frame.setVisible(true);
	}
	
}

