package mentalpoker;

import java.io.IOException;
import java.net.ConnectException;

import org.avis.client.*;
import org.avis.common.InvalidURIException;

public class ComService {

	private static Elvin elvin;
	private String server;
	private static String myUsername;
	private Subscription gameSub;
	
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
	public boolean startNewGame()
	{
		//Subscribe to responses bearing my username. This will be useful after we actually advertise the game.
		try {
			gameSub = elvin.subscribe ("Request == 'newGame' && OriginalUsername == '"+Poker.myUsername+"'");
		} catch (InvalidSubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		gameSub.addListener(new NotificationListener() {
		
	      public void notificationReceived(NotificationEvent event)
	      {
	        System.out.print ((char)event.notification.getInt ("Typed-Character"));
	      }
	    });
		
		
		Notification gameNotification = new Notification();
		gameNotification.set("requesterUsername", Poker.myUsername);
		gameNotification.set("request", "newGame");
		try {
			elvin.send(gameNotification);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean requestConnectionToUsername(String username)
	{
		//Create new subscription to an appropriate response
		
		
		
		//Then send notification with remote username and my username.
		
		//Cancel subscription
		return true;
	}
	
	
	
	
	
	
}
