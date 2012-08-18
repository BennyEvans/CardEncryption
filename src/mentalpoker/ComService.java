package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.net.ConnectException;

import org.avis.client.*;
import org.avis.common.InvalidURIException;

public class ComService {

	private User user;
	private static Elvin elvin;
	private String server;
	private Subscription gameSub;
	private Subscription gameAdvertisementSub;
	private Subscription gameFullSub;
	private ArrayList<User> currentGameMembers = new ArrayList<User>();
	//private ArrayList<String> currentGameMembers = new ArrayList<String>();
	private boolean thereHasBeenAnError = false;
	private ArrayList<User> availableGames = new ArrayList<User>();
	//private HashSet<String> availableGames = new HashSet<String>();
	//private String chosenGameHostUsername = "";
	private User gameHost;
	boolean gameHasFilled = false;
	ScheduledFuture<?> notificationHandle;
	
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);
	
	
	private User findGameHostByID(String gameHostID){
		for (int i = 0; i < availableGames.size(); i++){
			if (availableGames.get(i).getID().equals(gameHostID)){
				return availableGames.get(i);
			}
		}
		return null;
	}
	
	
	
	public ComService(User user) throws ConnectException, InvalidURIException, IllegalArgumentException, IOException
	{
		this.user = user;
		this.server = "elvin://elvin.students.itee.uq.edu.au";
		notificationHandle = null;
		
		elvin = new Elvin(server);
		elvin.closeOnExit();
	}
	
	/**
	 * Starts a new game. First creates the subscription to the responses, then sends out
	 * a new notification of the available game.
	 * @return
	 */
	public boolean startNewGame(final int numberOfSlots)
	{
		gameHasFilled = false;
		if (user.getUsername() == null)
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
			gameSub = elvin.subscribe("request == 'newGame' && hostUUID == '"+user.getID()+"'");
		} catch (Exception e) {
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
				if (currentGameMembers.size() < numberOfSlots)
				{
					//This means that the notification by the client that they want to join in
					//must have a field named "playerUsername" with their own username included.
					User tmpUser = new User(event.notification.getString("playerUsername"), event.notification.getString("playerUUID"));
					currentGameMembers.add(tmpUser);
					String numberOfSlotsLeft = Integer.toString(numberOfSlots - currentGameMembers.size());
					System.out.println(event.notification.getString("playerUsername") + " connected... " + numberOfSlotsLeft + " slots left.");
				} else {
					System.out.println("DEBUG: Attempt to join, but either did not match username or is full.");
	            	Notification gameFullNotification = new Notification();
	            	gameFullNotification.set("requesterUsername", user.getUsername());
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
		
		//New timer code.
		
		final Runnable notifyPotentialJoiners = new Runnable() {
			public void run()
			{
            	if (currentGameMembers.size() >= numberOfSlots)
            	{
            		notificationHandle.cancel(true);
            		gameHasFilled = true;
            	} else {
                	sendGameNotification();
            	}
			}
			
			private void sendGameNotification() {
            	Notification gameNotification = new Notification();
        		gameNotification.set("hostUUID", user.getID());
        		gameNotification.set("hostUsername", user.getUsername());
        		gameNotification.set("request", "newGame");
        		try {
        			elvin.send(gameNotification);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			thereHasBeenAnError = true;
        		}
            }
		};
		
		notificationHandle = scheduler.scheduleAtFixedRate(notifyPotentialJoiners, 1000,1500,TimeUnit.MILLISECONDS);
		
		
		try {
			scheduler.awaitTermination(6, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			thereHasBeenAnError = true;
			e.printStackTrace();
		}
		
		if (!thereHasBeenAnError)
		{
			return true;
		} else {
			thereHasBeenAnError = false;
			return false;
		}
	}
	
	public boolean joinGameOffMenu()
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
		gameAdvertisementSub.addListener(new NotificationListener() {
			//This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event)
			{
				
				//WHY IS THIS NULL AFTER JOINING A GAME???
				//Because the notification being put in doesn't use the string "hostUsername".
				System.out.println(event.notification.getString("hostUsername"));
				System.out.println(event.notification.getString("hostUUID"));
				User tmpUser = new User(event.notification.getString("hostUsername"), event.notification.getString("hostUUID"));
				if (findGameHostByID(tmpUser.getID()) == null ) {
					availableGames.add(tmpUser);
				}
			}
			
		});
		
		/**
		 * If a game is reported as full, remove it from the available games list.
		 */
		gameFullSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent event)
			{
				//may need to change this
				User tmpUser = new User(event.notification.getString("hostUsername"), event.notification.getString("hostUUID"));
				availableGames.remove(tmpUser);
			}
			
		});
		

		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		/**
		 * We want to repeatedly clear the screen,
		 * and print out the available games and a prompt asking which username to connect to.
		 */
		
		
		final Runnable checkForAvailableGames = new Runnable() {
			public void run()
			{
				MiscHelper.clearConsole();
            	writeCurrentAvailableGames();
			}
			
			private void writeCurrentAvailableGames()
			{
				System.out.println("Games available:");
				Iterator<User> itr = availableGames.iterator();
				int i = 0;
				while (itr.hasNext())
				{
					System.out.println(String.valueOf(i) + " " + itr.next().getUsername());
					i++;
				}
				System.out.print("Choose a game (enter the number): ");

			}
		};
		
		notificationHandle = scheduler.scheduleAtFixedRate(checkForAvailableGames, 300,500,TimeUnit.MILLISECONDS);
		
		//Grab the username of the hoster
		try {

			gameHost = availableGames.get(Integer.parseInt(br.readLine()));
			if (gameHost == null){
				System.out.println("returned NULL!");
			}
			notificationHandle.cancel(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//unable to read from command line
			return false;
		} catch (NumberFormatException e)
		{
			System.err.println("Your input was not a number!");
			return false;
		}
		
		/*
		 * At this point, we wish to notify the host that we wish to join their game.
		 */
		
    	joinGame();
		
		//Or cancel with a timeout of 6 minutes if it goes on too long.
		try {
			scheduler.awaitTermination(6, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			thereHasBeenAnError = true;
			e.printStackTrace();
		}
		
		
		if (!thereHasBeenAnError)
		{
			
			return true;
		} else {
			thereHasBeenAnError = false;
			//error
			return false;
		}
	}
	
	public void joinGame()
	{

		if (gameHost.getUsername() == null)
		{
			System.out.println("Not able to get the game host's username.");
		} else if (gameHost.getID() == null)
		{
			System.out.println("Not able to get the game host's id.");
		} else if (user.getUsername() == null)
		{
			System.out.println("Not able to get the user's username.");
		} else if (user.getID() == null)
		{
			System.out.println("Not able to get your ID.");
		} else {
			Notification joinGameNotificationToHost = new Notification();
	    	joinGameNotificationToHost.set("hostUsername", gameHost.getUsername());
	    	joinGameNotificationToHost.set("hostUUID", gameHost.getID());
	    	joinGameNotificationToHost.set("playerUsername", user.getUsername());
	    	joinGameNotificationToHost.set("playerUUID", user.getID());
	    	joinGameNotificationToHost.set("request", "newGame");
			try {
				elvin.send(joinGameNotificationToHost);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				thereHasBeenAnError = true;
			}
		}

		
		//TODO: Expand this to stay within this method until game has been set up.
		
	}
	
	
}
