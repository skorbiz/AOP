package general;

public class Settings 
{
/**** GENERAL SETTINGS *****************************/
	public static int sizex = 2;
	public static int sizey = 2;
	public static int insertNewCarInterval = 3000;
	
/**** GUI SETTINGS *********************************/
	public static int sizexFrame = (sizex+1)*100;
	public static int sizeyFrame = (sizey+1)*100;
	public static String nameFrame = "Trafik simulering";
	
/**** CROSS SETTINGS *********************************/
	public static int modeSimple = 0; // for 50/50%
	public static int modeComplex = 1; // for complex.
	public static int modeForChangingTrafficDirection = modeSimple;
	public static int timeBetweenDirectionChange = 20000;//20000;//3500;
	public static int timeBetweenMovingVehicleSameDirection = 1500;
	public static int timeBetweenMovingVehicleUppersitDirection = 6000;
	public static String verticalDef = "v";
	public static String horizontalDef = "h";
	public static int procentForChangingDirection = 25;
	public static int maxVehicleInLane = 10;

/**** INSERT NEW CARS SETTINGS *********************************/
	public static boolean changeInsertionTimeEverySample = false;
	public static int changeInInsertionTimeEverySample = -250;
	
/**** TESTING SETTINGS *********************************/
	public static long timeBetweenSamplingsInMilliSeconds = 1000*60*5;
	
/**** DEBUG SETTINGS *********************************/
	public static boolean print = false;
	public static boolean printMoveVehicleInterval = false;
	public static boolean printTrafficDirection = false;
	public static boolean printStartOfNewSamples = true;
	
/**** JADE CONTENT MESAGES *************************/
	public static String GuiToLaneRequestCars 				= "GUI requests number of cars in all lanes";
	public static String GuiToCrossRequestLights 			= "GUI requests lights in all crosses";
	public static String CrossToLaneRequestLocalID 			= "Cross request all lanes local ID";
	public static String CrossToLaneRequesOffers 			= "Requesting the price for all lanes";
	public static String CrossToLaneRequesSpaces 			= "Requesting the number of free spaces in queue from lane";
	public static String CrossToLaneRequestRetrieveVehicle 	= "Requesting vehicle from lane";
	public static String CrossToLaneRequestInsertVehicle 	= "Sending vehicle to lane";
	public static String CrossToCrossInformEmergency		= "Inform about emergency vehicle in ingoing lane";
	public static String CrossToCrossInformEmergencyMoved	= "Inform about emergency vehicle has been moved";
	
	
/**** SUPPORT FUNCTIONS ****************************/
/**** Used generally *******************************/
	/**
	 * @param	name Local name of a lane agent
	 * @return 	The ID as an integer
	 */
	public static int covertLocalLaneNameToInt(String name)
	{		
		int temp = Integer.parseInt(name.substring(4));
		return temp;
	}
	
	/**
	 * @param	name Local name of a cross agent
	 * @return 	The ID as an integer
	 */
	public static int covertLocalCrossNameToInt(String name)
	{		
		int temp = Integer.parseInt(name.substring(5));
		return temp;
	}
	
/**** SUPPORT FUNCTIONS ***************************/
/**** For getting input and output lanes **********/

	/**
	 * @param   cross 	Integer with the cross which needs to find and neighboring cross
	 * @param	offset 	Number between 0 and 3. 0 = up, 1 = down, 2 = left and 3 = right
	 * @return 	The correct Lane ID with the givin offset in the given cross
	 */
	public static int getLane(int cross, int offset)
	{
		return (cross -1)*4 + offset;
	}
	
	/**
	 * Finds the neighboring cross/lanes in a specific directions if the neighbor is a special case outgoing lane.
	 *  If offset is 0 the neighboring cross found will be the cross below.
	 * If the output is 2 the cross will be the cross to the right and so on.
	 * @param   cross 	Integer with the cross which needs to find and neighboring cross
	 * @param	offset 	Number between 0 and 3. 0 = up, 1 = down, 2 = left and 3 = right
	 * @return 	Integer with the ID of the special case outgoing lane. 1 if it is no special case.
	 */
	private static int findTargetSpecialCase(int cross, int offset)
	{	
		int target = 1;

		if(cross <= sizex && offset == 1)						//Detect special case: target is outgoing lane up
			target = - getLane(cross, offset);				

		else if(cross > (sizex*(sizey-1) ) && offset == 0)		//Detect special case: target is outgoing lane down
			target = - getLane(cross, offset);
		
		else if(offset == 3)									//Detect special case: target is outgoing lane left
		{
			for(int i = 1; i < sizex*sizey; i += sizex)
				if(cross == i)
					target = - getLane(cross, offset);
		}
		else if(offset == 2)									//Detect special case: target is outgoing lane right
		{
			for(int i1 = sizex; i1 <= sizex*sizey; i1 +=sizex)
				if(cross == i1)
					target = - getLane(cross, offset);
		}
		if(sizey == 1 && offset == 0)							//Detect specia case if gird sizey == 1. Target is defined to be the -3 lane.
			target = -3;										//Problem arises because 0 equals -0
		
		return target;
	}	
	
	/**
	 * Finds the neighboring cross in a specific directions. If offset is 0 the neighboring cross found will be the cross below.
	 * If the output is 2 the cross will be the cross to the right and so on.
	 * Can only be used if it is known that no special cases will be found.
	 * @param   cross 	Integer with the cross which needs to find and neighboring cross
	 * @param	offset 	Number between 0 and 3. 0 = up, 1 = down, 2 = left and 3 = right
	 * @return 	Integer with ID of neighboring cross. 0 if offset is incorrect.
	 */
	private static int findTargetCrossNormalCase(int cross, int offset)
	{	//
		int targetCross = 0;

		if(offset == 0)
			targetCross = cross + sizex;
		
		else if(offset == 1)
			targetCross = cross - sizex;
	
		else if(offset == 2)
			targetCross = cross +1;
		
		else if(offset == 3)
			targetCross = cross +-1;
		
		return targetCross;
	}
	
	/**
	 * Gets a list of integers with all the identifiers of input lanes for a cross.
	 * @param  cross Integer with cross for which to get input lanes
	 * @return Integer array of IDs on input lanes for cross
	 */
	public static int[] getInputLanes(int cross)
	{
		int[] inputLanes = new int[4];
		for(int offset = 0; offset < 4; offset++)
			inputLanes[offset] = getLane(cross, offset);
		return inputLanes;
	}
	

	/**
	 * Gets a list of integers with all the identifiers of outgoing lanes for a cross.
	 * @param  cross Integer with cross for which to get outgoing lanes
	 * @return Integer array of IDs on outgoing lanes for cross
	 */
	public static int[] getOutputLanes(int cross)
	{
		int[] outputLanes = new int[4];
		outputLanes[0] = findTargetSpecialCase(cross, 0);		//Input all as special cases
		outputLanes[1] = findTargetSpecialCase(cross, 1);
		outputLanes[2] = findTargetSpecialCase(cross, 2);
		outputLanes[3] = findTargetSpecialCase(cross, 3);

		for(int offset = 0; offset < outputLanes.length; offset++)				//If not special case the value will be above 0
			if(outputLanes[offset] > 0)								//The array is therefore updated as normal case
			{
				int targetCross = findTargetCrossNormalCase(cross, offset);
				outputLanes[offset] = getLane(targetCross, offset);
			}
		
		return outputLanes;
	}
	
	/**
	 * Gets a list of integers with all the identifiers of special case (outer) outgoing lans.
	 * The outgoing lanes always have negative value.
	 * @return Integer list of the special case outgoing lanes numbers
	 */
	public static int[] getSpecialCaseOutputLanes()
	{
		int[] outputLanes = new int[ sizex*2 + sizey*2];

		int index = 0;
		for(int cross = 1; cross <= sizex*sizey; cross++)
			for(int offset = 0; offset < 4; offset++)
			{
				int temp = findTargetSpecialCase(cross, offset);
				if(temp < 0)
				{
					outputLanes[index] = temp;
					index++;
				}
			}
					
		return outputLanes;
	}
	
	/**
	 * Gets a list of integers with all the identifiers of outer input lanes.
	 * @return Integer list of the all outer input lanes numbers
	 */
	public static int[] getOuterInputLanes()
	{	
		int[] inpuLanes = new int[ sizex*2 + sizey*2];
		
		int index = 0;
		for(int i = 0; i < sizex; i++)
		{
			inpuLanes[index] = i*4;
			index++;
		}
		
		for(int i = 0; i < sizex; i++)
		{
			inpuLanes[index] = (sizex*(sizey-1)+i)*4+1;
			index++;
		}
		
		for(int i = 0; i < sizey; i++)
		{
			inpuLanes[index] = i*sizex*4+2;
			index++;
		}

		for(int i = 0; i < sizey; i++)
		{
			inpuLanes[index] = (i*sizex+sizex)*4-1;
			index++;
		}
		
		return inpuLanes;
	}	
	
/**** SUPPORT FUNCTIONS ***************************/
/**** for indexing input and output lanes into 4 **/
	public static int inLane(int laneId, int crossId) 
	{
		int temp = -1;
		
		if( laneId >= (crossId-1)*4 && laneId < crossId*4 ) 
			temp = laneId%4;
		
		return temp;
	}
}
