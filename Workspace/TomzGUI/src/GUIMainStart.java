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


public class GUIMainStart extends JFrame implements MouseListener
{
	JFrame frame = new JFrame();
	GUISettings settings = new GUISettings();
    String recieveSentence;
    PGame gameBoard = new PGame();
    int x;
    int sendPort = 9005;
    int recievePort = 9006;
	
	public static void main(String[] args) throws Exception
	{	
		GUIMainStart main = new GUIMainStart();
		main.frame.setTitle("4 På Stribe - Din tur");
		//TheListeners listeners = new TheListeners(main, main.settings);
		//main.frame.addMouseListener(listeners);

		
		
	}
	
	public GUIMainStart() throws Exception
	{					
		frame.setSize( settings.windowSizeX, settings.windowSizeY + 22);
		frame.setResizable(false);
		frame.setTitle(settings.name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null); //Center of screen
		frame.setBackground(Color.lightGray);
		frame.addMouseListener(this);
		
		
		
		
		drawNewGameBord();
	}
	
	public void drawCounter(int theX, int theY, int theColor)
	{
		GUIComponentCounter counter = new GUIComponentCounter(theX, theY, theColor, settings);

		frame.add(counter);
		frame.setVisible(true);	
	}
	
	public void drawNewGameBord()
	{
		GUIComponentGrid Grid = new GUIComponentGrid(settings);
		frame.add(Grid);
		frame.setVisible(true);
	}	
	
	public void drawWin(String who)
	{
		WinComponent Text = new WinComponent(who);
		frame.add(Text);
		frame.setVisible(true);
		frame.setTitle(who + "har vundet!");
		
	}
	
	public void send(int port, int row) throws Exception 
	{
		 Integer rowInt = row;
		 Socket clientSocket = new Socket("10.1.64.137", port);
		 DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		 String rowString = rowInt.toString();
		 outToServer.writeBytes(rowString);
		 clientSocket.close();
		
	}
	
	public int recieve(int port) throws Exception
	{
		ServerSocket welcomeSocket = new ServerSocket(port);
        Socket connectionSocket = welcomeSocket.accept();
        
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
       
        recieveSentence = inFromClient.readLine();
        
         int row = Integer.parseInt(recieveSentence);
        
        
        welcomeSocket.close();
        connectionSocket.close();
		
        return row;
		
	}

	public void mouseClicked(MouseEvent e) 
	{
		
		try 
		{
			send(sendPort,x);
		} 
		
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		try 
		{
			gameBoard.placeChecker(recieve(recievePort));
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
		}
		
		
		
		frame.setTitle("4 På Stribe - Din tur");

		
		
		int[] yOmvendt = {5,4,3,2,1,0};
		
		int yOmvendtInt = yOmvendt[gameBoard.getColoumn()]; 
		
		drawCounter(gameBoard.getRow(), yOmvendtInt , gameBoard.getTur());
		
		if (gameBoard.checkWin() == 1)
		{
			drawWin("Rød");
			frame.removeMouseListener(this);
		}
		
		if (gameBoard.checkWin() == 2)
		{
			drawWin("Blå");
			frame.removeMouseListener(this);
		}
		
		
	}


	public void mouseEntered(MouseEvent e) {}


	public void mouseExited(MouseEvent e) {}


	public void mousePressed(MouseEvent e) 
	{
		
		frame.setTitle("4 På Stribe - Venter på modstander");

		x = e.getX() / (settings.windowSizeX/settings.rowsX);
		
		gameBoard.placeChecker(x);
		
		int[] yOmvendt = {5,4,3,2,1,0};
		
		int yOmvendtInt = yOmvendt[gameBoard.getColoumn()]; 
		
		drawCounter(gameBoard.getRow(), yOmvendtInt , gameBoard.getTur());
		
		if (gameBoard.checkWin() == 1)
		{
			drawWin("Rød");
		}
		else if (gameBoard.checkWin() == 2)
		{
			drawWin("Blå");
		}
	}

	
	public void mouseReleased(MouseEvent e) 
	{}
	


	
	
	


}
