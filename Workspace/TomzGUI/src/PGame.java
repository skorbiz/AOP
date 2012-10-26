
public class PGame 
{
	private int[][] gameBoard = new int[6][7]; //Spillepladen, indsat i et 2 dim array
	private int latestUsedColoumn= 0;	//Hvilken kolonne der sidst er lagt en brik i
	private int latestUsedRow = 0;	//Hvilken r�kke der sidst er lagt en brik i
	private int latestTurn;		//Hvem der sidst har placeret en brik
	private int countCheckers; //Bruges til at t�lle hvor mange brikker der ligger ved siden af hinanden
	private int tur = 1;
	
	public PGame() //Constructor
	{

	}
	
	public void placeChecker(int row) 	//Placerer en brik p� nederste frie plads, 
	{											//i den valgte r�kke
		
		for (int i = 5; i > -1; i--)
		{
			if (gameBoard[i][row] == 0)
			{
				gameBoard[i][row] = tur;
				latestUsedColoumn = i;
				latestUsedRow = row;
				latestTurn = tur;
			
				
				if (tur == 1)
				{
					tur = 2;
				}
				
				else
				{
				tur = 1;	
				}
				
				break;
				
			}
		}
	}
	
	
	public int checkWin() 	//Tjekker om der er 4 p� stribe, og returnerer den spillers "brik-nummer"
							//der har vundet
	{
		if (latestUsedColoumn < 3)		//Tjekker om der er lagt tre brikker eller over
										//i samme r�kke, hvis der er, tjekkes der for om
										//der ligger 4 ens oven p� hinanden
		{
			if (gameBoard[latestUsedColoumn+1][latestUsedRow]== latestTurn && 
					gameBoard[latestUsedColoumn+2][latestUsedRow]== latestTurn &&
					gameBoard[latestUsedColoumn+3][latestUsedRow]== latestTurn)
			{
				return latestTurn;
			}
		}
		
		countCheckers = 0;		//Tjekker om der er 4 bikker ved siden af hianden.
		for (int i = 1 ; i < 7; i++)//Tjekker f�rst om brikken der tjekkes for er uden for pladen
		{							//da dette vil skabe en outofbounds exception.
									//derefter tjekker den felterne til venstre for den senest lagte
									//brik, indtil den n�r til spillepladens kant, eller der 
									//ikke ligger en passende brik
			if (latestUsedRow - i < 0)
			{
				break;
			}
			
			if (gameBoard[latestUsedColoumn][latestUsedRow-i] == latestTurn)
			{
				countCheckers++;
				
			}
			else
			{
				break;
			}
			
		}
		
		for (int i = 1 ; i < 7; i++)	//Tjekker til venstre for senest lagte brik, indtil
		{								//kanten eller tom plads/anden brik findes.
			if (latestUsedRow + i > 6)
			{
				break;
			}
			
			if (gameBoard[latestUsedColoumn][latestUsedRow+i] == latestTurn)
			{
				countCheckers++;
				
			}
			
			else
			{
				break;
			}
			

			
		}
		

		
		if (countCheckers > 2) //Hvis der er fundet 3 eller flere brikker ved siden af den senest
							   //lagte brik, returneres vinderes tal.	
		{
			return latestTurn;
		}
		
		countCheckers = 0;		
		for (int i = 1 ; i < 7; i++)
		{						
			if (latestUsedRow - i < 0 || latestUsedColoumn + i > 5 )
			{
				break;

			}
			
			if (gameBoard[latestUsedColoumn+i][latestUsedRow-i] == latestTurn)
			{
				countCheckers++;
				
			}
			else
			{
				break;
			}
			
		}
		
		for (int i = 1 ; i < 7; i++)	//Tjekker til venstre for senest lagte brik, indtil
		{								//kanten eller tom plads/anden brik findes.
			if (latestUsedRow + i > 6 || latestUsedColoumn - i < 0 )
			{
				break;
			}
			
			if (gameBoard[latestUsedColoumn-i][latestUsedRow+i] == latestTurn)
			{
				countCheckers++;
				
			}
			
			else
			{
				break;
			}

		}


		if (countCheckers > 2)
		{
			return latestTurn;
		}
		
		
		countCheckers = 0;
		
		for (int i = 1 ; i < 7; i++)
		{						
			if (latestUsedRow - i < 0 || latestUsedColoumn - i < 0 )
			{
				break;
			}
			
			if (gameBoard[latestUsedColoumn-i][latestUsedRow-i] == latestTurn)
			{
				countCheckers++;
				
			}
			else
			{
				break;
			}
		}
		
		
		for (int i = 1 ; i < 7; i++)
		{						
			if (latestUsedRow + i > 6 || latestUsedColoumn + i > 5 )
			{
				break;
			}
			
			if (gameBoard[latestUsedColoumn+i][latestUsedRow+i] == latestTurn)
			{
				countCheckers++;
				
			}
			else
			{
				break;
			}
		}
		
		if (countCheckers > 2)
		{
			return latestTurn;
		}
		
		
		
		
		
			return -1;
		
		
	}
	
	public int getTur()
	{
		return tur;
	}
	
	public int getColoumn()
	{
		return latestUsedColoumn;
	}
	
	public int getRow()
	{
		return latestUsedRow;
	}
	
	
	public int[][] getGameBoard()
	{
		return gameBoard;
	}
	
}
