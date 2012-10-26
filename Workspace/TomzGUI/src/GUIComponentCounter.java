import java.awt.geom.Ellipse2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent; 


public class GUIComponentCounter extends JComponent {
		
	int x;
	int y;
	GUISettings settings;
	Color color;
	
	public GUIComponentCounter(int theX, int theY,int aColor, GUISettings theSettings)
	{
		settings = theSettings;	
		x = theX;
		y = theY;
		
		if(aColor == 0) //Farven hvis der ikke er nogen brik
			color = color.darkGray; 

		if(aColor == 1)
			color = color.blue;

		if(aColor == 2)
			color = color.red;		
	}
		
	public void paintComponent(Graphics g)
	{
		int brede = settings.windowSizeX/settings.rowsX;
		int højde = settings.windowSizeY/settings.rowsY;
		int xPosition = x * (settings.windowSizeX/settings.rowsX);
		int yPosition = -2 +	(settings.windowSizeY - settings.windowSizeY/settings.rowsY ) +  - y * (settings.windowSizeY/settings.rowsY); //Positionen er relativ til �verste h�jre hj�rne
	
		Graphics2D g2 = (Graphics2D) g;
		Ellipse2D.Double ellipse = new Ellipse2D.Double(xPosition, yPosition, brede, højde);
		g2.setPaint(color);
		g2.draw(ellipse);
		g2.fill(ellipse);	
	}

}