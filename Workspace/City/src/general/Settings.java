package general;

public class Settings 
{

	/**** GENERAL SETTINGS *****************************/
	public static int sizex = 1;
	public static int sizey = 1;
	
	/**** GUI SETTINGS *********************************/
	public static int sizexFrame = (sizex+1)*100;
	public static int sizeyFrame = (sizey+1)*100;
	public static String nameFrame = "Trafik simulering";
	
	
	/**** JADE CONTENT MESAGES *************************/
	public static String GuiToLaneRequestCars = "GUI requests number of cars in all lanes";
	public static String CrossToLaneRequestLocalID = "Cross request all lanes local ID";
	public static String CrossToLaneRequesOffers = "Requesting the price for all lanes";
	public static String CrossToLaneRequesSpaces = "Requesting the number of free spaces in queue from lane";
	public static String CrossToLaneRequestRetrieveVehicle = "Requesting vehicle from lane";
	public static String CrossToLaneRequestInsertVehicle = "Sending vehicle from lane";
	
	
	/**** SUPPORT FUNCTIONS ****************************/
	/**** Used generally *******************************/
	public static int covertLocalLaneNameToInt(String name)
	{		
		int temp = Integer.parseInt(name.substring(4));
		return temp;
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
	
	
	public static int outLane(int laneId, int crossId) 
	{
		int temp = -1;
		
		// special case if cross on edge
		if( laneId != 0 ) { // lane 0 is never output lane
			if( laneId<0 ) {
				if( crossId<=sizex && -1*crossId==laneId ) { // on top edge
					temp = 0;
//					System.out.print("SPECIAL0 ");
				}
				else if( crossId>=(sizex*sizey-sizex) && -1*(sizex+sizey+(crossId-1)%sizex+1)==laneId ) { // on down edge
					temp = 1;
//					System.out.print("SPECIAL1 ");
				}
				else if( (crossId-1)%sizex==0 && -1*(sizex+sizey+sizex+crossId/sizex)==laneId ) { // on left edge
					temp = 2;
//					System.out.print("SPECIAL2 ");
				}
				else if( crossId%sizey==0 && -1*(sizex+crossId/sizex)==laneId ) { // on right edge
					temp = 3;
//					System.out.print("SPECIAL3 ");
				}
			}
			
			// normal case if cross is not on edge
			else if( (crossId-sizex)*4-1==laneId ) { // up
				temp = 0;
//				System.out.print("NORMAL0 ");
			}
			else if( (crossId+sizex)*4-3==laneId ) { // down
				temp = 1;
//				System.out.print("NORMAL1 ");
			}
			else if( ((crossId-1)*4-2)==laneId ) { // left
				temp = 2;
//				System.out.print("NORMAL2 ");
			}
			else if( ((crossId+1)*4-4)==laneId ) { // right
				temp = 3;
//				System.out.print("NORMAL3 ");
			}
		}
		return temp;
	}
}
