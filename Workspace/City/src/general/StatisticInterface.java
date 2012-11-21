package general;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import general.Settings;
import vehicle.Vehicle;

public class StatisticInterface
{
	ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
	
	private long startTime = System.currentTimeMillis();
	private long endTime = System.currentTimeMillis();
	private long runTime = 0;
		
	private double TotalCarsThroug = 0;
	private double TotalWaitTime = 0;
	
	private double ThroughputCars = 0;
	private double ThroughputWaitTime = 0;
	
	private double meanWaitTime = 0;
	private double varianceWaitTime = 0;
	
	private int sampleNumber = 0;

	
	/***** Constructer - Singleton pattern **********/
	private static StatisticInterface statisticClass = new StatisticInterface();

	public static StatisticInterface getInstance() 
	{
		return statisticClass;
	}
	
	private StatisticInterface() 
	{
		//writeStatisticToFile();
	}
	
	/***** Support functions *************************/
	public void addVehicle(Vehicle vehicle)
	{
		vehicles.add(vehicle);
	}
	
	public void checkAndInisiateNewSample()
	{
		if(startTime + Settings.timeBetweenSamplingsInMilliSeconds < System.currentTimeMillis())
			inisiateNewSample();
	}
	
	private void inisiateNewSample()
	{
		if(Settings.printStartOfNewSamples)
			System.out.println("Statistics started new sample");
		calculateAndSaveStatistics(); 	// Writes the statistic of the current sample
		
		sampleNumber++;
		vehicles.clear();				// Resets parameters for next sample
		startTime += Settings.timeBetweenSamplingsInMilliSeconds;
		endTime = 0;
		runTime = 0;
		TotalCarsThroug = 0;
		TotalWaitTime = 0;
		ThroughputCars = 0;
		ThroughputWaitTime = 0;
		meanWaitTime = 0;
		varianceWaitTime = 0;

		
	}
	
	
	public void calculateAndSaveStatistics()
	{
		endTime = System.currentTimeMillis();
		runTime = (endTime - startTime)/1000;
		
		TotalCarsThroug = vehicles.size();
		
		TotalWaitTime = 0;
		for(int i = 0; i < vehicles.size(); i++)
			TotalWaitTime += vehicles.get(i).getTotalWaitTime()/1000;
		
		ThroughputCars =     TotalCarsThroug / ( (double) runTime );
		ThroughputWaitTime = TotalWaitTime / ( (double) runTime );
		
		meanWaitTime = TotalWaitTime / TotalCarsThroug;
		
		varianceWaitTime = 0;
		for(int i = 0; i < vehicles.size(); i++)
			varianceWaitTime += Math.pow( (((double)vehicles.get(i).getTotalWaitTime()) /1000) - meanWaitTime , 2.0);
		varianceWaitTime *= 1/TotalCarsThroug;
		
		varianceWaitTime = Math.sqrt(varianceWaitTime);
		
		
		writeStatisticToFile();
	}

	private void writeStatisticToFile()
	{
		try
		{
		// Create file 
		FileWriter fstream = new FileWriter("sample" + Integer.toString(sampleNumber) +".txt");
		BufferedWriter out = new BufferedWriter(fstream);

		writeLineInFile(out, "Sample Number:      ", sampleNumber);
		writeLineInFile(out, "Sample time:      ", Settings.timeBetweenSamplingsInMilliSeconds /1000);

		writeLineInFile(out, "Insert cars [ms]: ", Settings.insertNewCarInterval);
		if(Settings.changeInsertionTimeEverySample == true)
		{
			out.write("Insertion time \t\tchanges \n");
			writeLineInFile(out, "time changes [ms]: ", Settings.changeInInsertionTimeEverySample);
		}
		else
			out.write("Insertion time \t\tconstant \n");

		
		if(Settings.modeForChangingTrafficDirection == 0)
		{
			out.write("Cross changes: \t\tsimple mode \n");
			writeLineInFile(out, "Change time:       ", Settings.modeForChangingTrafficDirection);
		}
		else
			out.write("Cross changes: \t\tcomplex mode \n");
		
		writeLineInFile(out, "Moving vehicle [ms]: ", Settings.timeBetweenMovingVehicleSameDirection);
		writeLineInFile(out, "Change direction [ms]: ", Settings.timeBetweenMovingVehicleUppersitDirection);

		//writeLineInFile(out, "Start time:     ", (int) Start time);
		//writeLineInFile(out, "End time: ", (int) End time);
		writeLineInFile(out, "Grid size x:       ", Settings.sizex);
		writeLineInFile(out, "Grid size y:       ", Settings.sizey);

		writeLineInFile(out, "Run time:       ", (int) runTime);

		writeLineInFile(out, "Total cars through: ", TotalCarsThroug);
		writeLineInFile(out, "Total wait time: ", TotalWaitTime);

		writeLineInFile(out, "Throughput cars: ", ThroughputCars);
		writeLineInFile(out, "Throughput wait time: ", ThroughputWaitTime);

		writeLineInFile(out, "Wait time mean: ", meanWaitTime);
		writeLineInFile(out, "Wait time variance: ", varianceWaitTime);

		out.write("Vehicles wait time \n");
		for(int i = 0; i < vehicles.size(); i++)
		{
			out.write(Integer.toString((int) (vehicles.get(i).getTotalWaitTime()/1000)));
			out.write(",");
		}
		//Close the output stream
		out.close();
		}catch (Exception e){}
	}
	
	private void writeLineInFile(BufferedWriter out, String s, int value) throws IOException
	{
		out.write(s);
		out.write("\t");
		out.write(Integer.toString(value));
		out.write("\n");
	}
	
	private void writeLineInFile(BufferedWriter out, String s, double value) throws IOException
	{
		out.write(s);
		out.write("\t");
		out.write(Double.toString(value));
		out.write("\n");
	}
	
	
}	