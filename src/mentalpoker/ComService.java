package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.net.ConnectException;

import org.avis.client.*;
import org.avis.common.InvalidURIException;

public class ComService {

	private static Elvin elvin;
	private String server;
	private Subscription gameSub;
	private Subscription gameAdvertisementSub;
	private Subscription gameFullSub;
	private Timer gameNotificationTimer = new Timer();
	private boolean gameIsFullOrWeAreHappy = false;
	private ArrayList<String> currentGameMembers = new ArrayList<String>();
	private boolean thereHasBeenAnError = false;
	private HashSet<String> availableGames = new HashSet<String>();
	private String chosenGameHostUsername = "";
	
	public ComService()
	{
		server = "elvin://elvin.students.itee.uq.edu.au";
		
		try {
			elvin = new Elvin(server);
			elvin.closeOnExit();
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (InvalidURIException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts a new game. First creates the subscription to the responses, then sends out
	 * a new notification of the available game.
	 * @return
	 */
	public boolean startNewGame(final int numberOfSlots)
	{
		if (Poker.myUsername == null)
		{
			System.err.println("You cannot have an empty username.");
			return false;
		}
		//Check that there is a sufficient number of slots specified
		if (numberOfSlots < 1)
		{
			System.err.println("You must specify at least one slot available.");
			return false;
		}
		
		//Reset the error boolean variable.
		thereHasBeenAnError = false;
		
		//Subscribe to responses bearing my username. This will be useful after we actually advertise the game.
		try {
			gameSub = elvin.subscribe ("request == 'joinGame' && hostersUsername == '"+Poker.myUsername+"'");
		} catch (InvalidSubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			thereHasBeenAnError = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			thereHasBeenAnError = true;
		}
		
		System.out.println("Now hosting game...");
		
		/**
		 * Receive requests to join the game.
		 */
		gameSub.addListener(new NotificationListener() {
			//This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event)
			{
				if (event.notification.getString("hostersUsername").equals(Poker.myUsername) &&
						(currentGameMembers.size() < numberOfSlots))
				{
					//This means that the notification by the client that they want to join in
					//must have a field named "playerUsername" with their own username included.
					currentGameMembers.add(event.notification.getString("playerUsername"));
					String numberOfSlotsLeft = Integer.toString(numberOfSlots - currentGameMembers.size());
					System.out.println(event.notification.getString("playerUsername") + " connected... " + numberOfSlotsLeft + " slots left.");
				} else {
	            	Notification gameFullNotification = new Notification();
	            	gameFullNotification.set("requesterUsername", Poker.myUsername);
	            	gameFullNotification.set("gameStatus", "full");	
	            	try {
						elvin.send(gameFullNotification);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						thereHasBeenAnError = true;
					}
				}
			}
		});
		
		/**
		 * At 1500ms intervals, notify potential players that there is a game available.
		 */
		gameNotificationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	if (currentGameMembers.size() >= numberOfSlots)
            	{
            		gameNotificationTimer.cancel();
            	} else {
                	sendGameNotification();
            	}
            }
            private void sendGameNotification() {
            	Notification gameNotification = new Notification();
        		gameNotification.set("requesterUsername", Poker.myUsername);
        		gameNotification.set("request", "newGame");
        		try {
        			elvin.send(gameNotification);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			thereHasBeenAnError = true;
        		}
            }
        }, 1000,1500);
		
		if (!thereHasBeenAnError)
		{
			return true;
		} else {
			thereHasBeenAnError = false;
			return false;
		}
	}
	
	public String joinGame()
	{
		thereHasBeenAnError = false;
		System.out.println("Searching for available games...");
		availableGames.clear();

		//Subscribe to new game advertisement notifications
		try {
			gameAdvertisementSub = elvin.subscribe ("request == 'newGame'");
		} catch (InvalidSubscriptionException e) {

			e.printStackTrace();
			thereHasBeenAnError = true;
		} catch (IOException e) {

			e.printStackTrace();
			thereHasBeenAnError = true;
		}
		
		//Subscribe to game full notifications.
		try {
			gameFullSub = elvin.subscribe("gameStatus == 'full'");
		} catch (InvalidSubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			thereHasBeenAnError = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			thereHasBeenAnError = true;
		}
		
		
		//Whenever a game notification is received, add it to the availableGames hashSet
		/**
		 * Receive requests to join the game.
		 */
		gameAdvertisementSub.addListener(new NotificationListener() {
			//This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event)
			{
				availableGames.add(event.notification.getString("hostersUsername"));
			}
			
		});
		
		/**
		 * If a game is reported as full, remove it from the available games list.
		 */
		gameFullSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent event)
			{
				availableGames.remove(event.notification.getString("hostersUsername"));
			}
			
		});
		

		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		
		
		
		/**
		 * We want to repeatedly clear the screen,
		 * and print out the available games and a prompt asking which username to connect to.
		 */
		
		gameNotificationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	MiscHelper.clearConsole();
            	writeCurrentAvailableGames();
            }

            private void writeCurrentAvailableGames()
            {
            	System.out.println("Games available:");
            	Iterator itr = availableGames.iterator();
            	while (itr.hasNext())
            	{
            		System.out.println(itr.next());
            	}
            	System.out.println("Choose a game (enter the username of the player hosting): ");
            	
            }
        }, 1000,1500);
		
		
		try {
			chosenGameHostUsername = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "UNABLE TO READ FROM COMMAND LINE";
		}
	         
		
		
		if (!thereHasBeenAnError)
		{
			return chosenGameHostUsername;
		} else {
			thereHasBeenAnError = false;
			return "ERR";
		}
	}
	
	
	
	
	
	
}
