package mentalpoker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.net.ConnectException;

import org.avis.client.*;
import org.avis.common.InvalidURIException;

public class ComService {

	private static Elvin elvin;
	private String server;
	private static String myUsername;
	private Subscription gameSub;
	private Timer gameNotificationTimer = new Timer();
	private boolean gameIsFullOrWeAreHappy = false;
	private ArrayList<String> currentGameMembers = new ArrayList<String>();
	
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
		//Subscribe to responses bearing my username. This will be useful after we actually advertise the game.
		try {
			gameSub = elvin.subscribe ("request == 'joinGame' && hostersUsername == '"+Poker.myUsername+"'");
		} catch (InvalidSubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
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
				}
			}
		});
		

		gameNotificationTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	sendGameNotification();
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
        		}
            }
        }, 1000,1500);
		

		return true;
	}
	
	
	
	
	
	
}
