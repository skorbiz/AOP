import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import javax.swing.JComponent; 

public class GUIComponentGrid extends JComponent
{

	int rowsX;
	int rowsY;
	int windowSizeX;
	int windowSizeY;
		
	public GUIComponentGrid(GUISettings settings)
	{
		rowsX = settings.rowsX;
		rowsY = settings.rowsY;
		windowSizeX = settings.windowSizeX;
		windowSizeY = settings.windowSizeY;
	}
		
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		for(int i = 0; i < windowSizeX; i += windowSizeX/rowsX)
		{
			Line2D.Double line = new Line2D.Double(i, 0, i, windowSizeY);
			g2.draw(line);
		}
		
		for(int i = 0; i < windowSizeY; i += windowSizeY/rowsY)
		{
			Line2D.Double line = new Line2D.Double(0, i, windowSizeX, i);
			g2.draw(line);
		}
	}

		
}
