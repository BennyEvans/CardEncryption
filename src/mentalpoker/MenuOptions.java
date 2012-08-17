package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MenuOptions {
	
	private static String choiceString = "";
	
	public static int printMainMenu()
	{
		MiscHelper.clearConsole();
		int choiceInteger;
		
		System.out.println("Welcome to Mental Poker. Your options:");
		System.out.println("1. Host a game");
		System.out.println("2. Join a game");
		System.out.print("Your choice (enter a number):");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			choiceString = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UNABLE TO READ FROM COMMAND LINE");
		}
		
		
		try {
			choiceInteger = Integer.parseInt(choiceString);
		} catch (NumberFormatException e)
		{
			choiceInteger = Integer.MIN_VALUE;
		}
		
		return choiceInteger;
		
	}
	
	public static int startNewGameMenu()
	{
		MiscHelper.clearConsole();
		System.out.print("How many slots do you want to make available: ");
		
		int numberOfSlots = Integer.MIN_VALUE;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			choiceString = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UNABLE TO READ FROM COMMAND LINE");
		}
		
		try {
			numberOfSlots = Integer.parseInt(choiceString);
		} catch (NumberFormatException e)
		{
			numberOfSlots = Integer.MIN_VALUE;
		}
		
		return numberOfSlots;
		
	}
	
}
