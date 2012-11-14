package general;

import general.Settings;
import vehicle.Vehicle;

public class StatisticInterface
{
	private long startTime = System.currentTimeMillis();

	/***** Constructer - Singleton pattern **********/
	private static StatisticInterface statisticClass = new StatisticInterface();

	public static StatisticInterface getInstance() 
	{
		return statisticClass;
	}
	
	private StatisticInterface() 
	{
	}
	
	/***** Support functions *************************/
	
	public void addVehicle(Vehicle vehicle)
	{
		System.out.println(" -------------------- VEHICLE ADDED TO STATISTIC");
	}
}	

/** SPØRGSMÅL
* Hvad sker der med ulæste beskeder?
* 
**/
