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

public class GUImain 
{
	private JFrame frame = new JFrame();
	private GUISettings settings = new GUISettings();

	private GUIComponentLights lights;
	private GUIComponentCars cars;
	
	public static void main(String[] args) throws Exception
	{
		GUImain main = new GUImain();
	}
	
	public GUImain() throws Exception
	{
		int[] lightsDefault = new int[settings.sizex*settings.sizey];
		int[] carsDefault = new int[settings.sizex*settings.sizey*4];
		for(int i = 0; i < carsDefault.length; i++)
			carsDefault[i] = i;
		
		createFrame();
		drawLights(lightsDefault);
		drawCars(carsDefault);
		
	}
	
	public void updateFrame(int[] theLights, int[] theCars)
	{
		frame.remove(lights);
		frame.remove(cars);
		drawLights(theLights);
		drawCars(theCars);
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
	
	//Draw the lights
	private void drawLights(int[] theLights)
	{
		lights = new GUIComponentLights(settings, theLights);
		frame.add(lights);
		frame.setVisible(true);
	}
	
	//Draw cars
	private void drawCars(int[] theCars)
	{
		cars = new GUIComponentCars(settings, theCars);
		frame.add(cars);
		frame.setVisible(true);
	}
	
}

