package environment;

import general.Settings;
import java.util.ArrayList;
import vehicle.Vehicle;

public class LaneInterface 
{
	ArrayList<Vehicle> vehicleInLane = new ArrayList<Vehicle>();
	
	public LaneInterface() 
	{
	}
	
	public int getNumberOfVehicles() 
	{
		return vehicleInLane.size();
	}
	
	public int getNumberOfFreeSpacesForVehicles() 
	{
		return Settings.maxVehicleInLane-vehicleInLane.size();
	}
	
	public int getPrice() 
	{
		int price = -1;
		for(int i = 0; i < vehicleInLane.size(); i++)				
			price += vehicleInLane.get(i).getWaitTime();

		return price/1000;
	}
	
	public boolean insertVehicle(Vehicle vehicle) 
	{
		if( vehicleInLane.size() < Settings.maxVehicleInLane )
			return vehicleInLane.add(vehicle);
		else 
			return false;
	}
	
	public Vehicle retrieveVehicle() 
	{	
		if(vehicleInLane.size() == 0)										//No vehicles in que
			return null;
		
		if(vehicleInLane.size() >= 2)										//If the vehicle behind the vehicle to be transfered has reached the traffic light,
			vehicleInLane.get(1).putVehicleInQue();							//it will be put in the state 'inQue' and have a speed of zero
		
		boolean vehicleReadyToTransfer;										//Update the front vehicle to see if its ready to transfer
		vehicleReadyToTransfer = vehicleInLane.get(0).transferVehicle();	//The vehicle is updated such that the parameters fit the next que
		
		if(vehicleReadyToTransfer == false)									//Return null if the vehicle is not ready to transfer
			return null;											
		
		return vehicleInLane.remove(0);
	}		
}