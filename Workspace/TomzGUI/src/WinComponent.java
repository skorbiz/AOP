
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent; 

import java.awt.*;



public class WinComponent extends JComponent 
{
	String who;

	
	public WinComponent(String aWho)
	{
			who = aWho;
	}
		
	public void paintComponent(Graphics g)
	{
		
		
		
		Graphics2D g2 = (Graphics2D) g;
		Rectangle boks = new Rectangle(90,80,180,30);
		g2.setColor(new Color(0,0,0));
		g2.fill(boks);
		g2.setFont(new Font("Courier", Font.BOLD, 20));
		g2.setColor(new Color(255,255,225));
		g2.drawString(who + " har vundet!", 100, 100);
	}

}