package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

public class MenuOptions {
	
	private static String choiceString = "";
	
	public static int printMainMenu()
	{
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
		
		
		/*
		 * Code acquired from
		 * http://stackoverflow.com/questions/174502/string-to-int-in-java-likely-bad-data-need-to-avoid-exceptions
		 */
		NumberFormat format = NumberFormat.getIntegerInstance(locale);
		
		
		
		
	}
}
