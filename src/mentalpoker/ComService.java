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
import java.math.BigInteger;
import java.net.ConnectException;

import org.avis.client.*;
import org.avis.common.InvalidURIException;

/**
 * The Class ComService.
 */
public class ComService {

	/** The user current. */
	private User user;
	
	/** The elvin service. */
	private static Elvin elvin;
	
	/** The elvin server. */
	private String server;
	
	/** The game subscription. */
	private Subscription gameSub;
	
	/** The game advertisement subscription. */
	private Subscription gameAdvertisementSub;
	
	/** The game full subscription. */
	private Subscription gameFullSub;
	
	/** The current game members. */
	private ArrayList<User> currentGameMembers = new ArrayList<User>();
	
	/** There has been an error var. */
	private boolean thereHasBeenAnError = false;
	
	/** The available games. */
	private ArrayList<User> availableGames = new ArrayList<User>();
	
	/** The game host. */
	private User gameHost;
	
	/** The game has filled var. */
	boolean gameHasFilled = false;
	
	/** The notification handle. */
	ScheduledFuture<?> notificationHandle;

	/** The scheduler. */
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);
	

	// TODO: If the StartGame and joinGameOffMenu functions don't already block,
	// we need to make them block
	// TODO: Make the StartGame and joinGameOffMenu functions return the list of game Members
	// TODO: Clean up after a game has been created (all slots filled) ie.
	// remove all subscriptions, stop schedulers, clear arrays etc.

	
	/**
	 * Find game host by id.
	 *
	 * @param gameHostID the game host id
	 * @return the game host
	 */
	private User findGameHostByID(String gameHostID) {
		for (int i = 0; i < availableGames.size(); i++) {
			if (availableGames.get(i).getID().equals(gameHostID)) {
				return availableGames.get(i);
			}
		}
		return null;
	}

	
	/**
	 * Instantiates a new communication service.
	 *
	 * @param user the current user
	 * @throws ConnectException the connect exception
	 * @throws InvalidURIException the invalid uri exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ComService(User user) throws ConnectException, InvalidURIException,
			IllegalArgumentException, IOException {
		this.user = user;
		this.server = "elvin://elvin.students.itee.uq.edu.au";
		notificationHandle = null;

		elvin = new Elvin(server);
		elvin.closeOnExit();
	}

	
	/**
	 * Starts a new game. First creates the subscription to the responses, then
	 * sends out a new notification of the available game.
	 *
	 * @param numberOfSlots the number of slots
	 * @return the list of users in the game
	 */
	public ArrayList<User> startNewGame(final int numberOfSlots) {
		gameHasFilled = false;
		if (user.getUsername() == null) {
			System.err.println("You cannot have an empty username.");
			return null;
		}
		// Check that there is a sufficient number of slots specified
		if (numberOfSlots < 1) {
			System.err.println("You must specify at least one slot available.");
			return null;
		}

		// Reset the error boolean variable.
		thereHasBeenAnError = false;

		// Subscribe to responses bearing my username. This will be useful after
		// we actually advertise the game.
		try {
			gameSub = elvin
					.subscribe("request == 'newGameResponse' && hostUUID == '"
							+ user.getID() + "'");
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
			// This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event) {
				if (currentGameMembers.size() < numberOfSlots) {
					
					// This means that the notification by the client that they want to join in
					// must have a field named "playerUsername" with their own username included.
					User tmpUser = new User(event.notification
							.getString("playerUsername"), event.notification
							.getString("playerUUID"));
					currentGameMembers.add(tmpUser);
					String numberOfSlotsLeft = Integer.toString(numberOfSlots
							- currentGameMembers.size());
					System.out.println(event.notification
							.getString("playerUsername")
							+ " connected... "
							+ numberOfSlotsLeft + " slots left.");
				} else {
					System.out
							.println("DEBUG: Attempt to join, but either did not match username or is full.");
					Notification gameFullNotification = new Notification();
					gameFullNotification.set("requesterUsername",
							user.getUsername());
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
		 * At 1500ms intervals, notify potential players that there is a game
		 * available.
		 */

		// New timer code.
		final Runnable notifyPotentialJoiners = new Runnable() {
			public void run() {
				if (currentGameMembers.size() >= numberOfSlots) {
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

		notificationHandle = scheduler.scheduleAtFixedRate(
				notifyPotentialJoiners, 1000, 3000, TimeUnit.MILLISECONDS);

		try {
			scheduler.awaitTermination(6, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			thereHasBeenAnError = true;
			e.printStackTrace();
		}

		if (!thereHasBeenAnError) {
			return null;
		} else {
			thereHasBeenAnError = false;
			// return the members in the game
			ArrayList<User> tmp = new ArrayList<User>(currentGameMembers);
			currentGameMembers = null;
			return tmp;
		}
	}

	
	/**
	 * Join game off menu.
	 *
	 * @return the list of users in the game
	 */
	public ArrayList<User> joinGameOffMenu() {
		thereHasBeenAnError = false;
		System.out.println("Searching for available games...");
		availableGames.clear();

		// Subscribe to new game advertisement notifications
		try {
			gameAdvertisementSub = elvin.subscribe("request == 'newGame'");
		} catch (InvalidSubscriptionException e) {

			e.printStackTrace();
			thereHasBeenAnError = true;
		} catch (IOException e) {

			e.printStackTrace();
			thereHasBeenAnError = true;
		}

		// Subscribe to game full notifications.
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

		// Whenever a game notification is received, add it to the availableGames hashSet
		
		gameAdvertisementSub.addListener(new NotificationListener() {
			// This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event) {

				User tmpUser = new User(event.notification
						.getString("hostUsername"), event.notification
						.getString("hostUUID"));
				if (findGameHostByID(tmpUser.getID()) == null) {
					availableGames.add(tmpUser);
				}
			}

		});

		/**
		 * If a game is reported as full, remove it from the available games
		 * list.
		 */
		gameFullSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent event) {
				// may need to change this
				User tmpUser = new User(event.notification
						.getString("hostUsername"), event.notification
						.getString("hostUUID"));
				availableGames.remove(tmpUser);
			}

		});

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		/**
		 * We want to repeatedly clear the screen, and print out the available
		 * games and a prompt asking which username to connect to.
		 */

		final Runnable checkForAvailableGames = new Runnable() {
			public void run() {
				MiscHelper.clearConsole();
				writeCurrentAvailableGames();
			}

			private void writeCurrentAvailableGames() {
				System.out.println("Games available:");
				Iterator<User> itr = availableGames.iterator();
				int i = 0;
				while (itr.hasNext()) {
					System.out.println(String.valueOf(i) + " "
							+ itr.next().getUsername());
					i++;
				}
				System.out.print("Choose a game (enter the number): ");

			}
		};

		notificationHandle = scheduler.scheduleAtFixedRate(
				checkForAvailableGames, 300, 3000, TimeUnit.MILLISECONDS);

		// Grab the username of the hoster
		try {

			gameHost = availableGames.get(Integer.parseInt(br.readLine()));
			if (gameHost == null) {
				System.out.println("returned NULL!");
			}
			notificationHandle.cancel(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// unable to read from command line
			return null;
		} catch (NumberFormatException e) {
			System.err.println("Your input was not a number!");
			return null;
		}


		// At this point, we wish to notify the host that we wish to join their game.


		joinGame();

		// Or cancel with a timeout of 6 minutes if it goes on too long.
		try {
			scheduler.awaitTermination(6, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			thereHasBeenAnError = true;
			e.printStackTrace();
		}

		if (!thereHasBeenAnError) {

			ArrayList<User> tmp = new ArrayList<User>(currentGameMembers);
			currentGameMembers = null;
			return tmp;

		} else {
			thereHasBeenAnError = false;
			// error
			return null;
		}
	}

	
	/**
	 * Join game.
	 */
	public void joinGame() {

		if (gameHost.getUsername() == null) {
			System.out.println("Not able to get the game host's username.");
		} else if (gameHost.getID() == null) {
			System.out.println("Not able to get the game host's id.");
		} else if (user.getUsername() == null) {
			System.out.println("Not able to get the user's username.");
		} else if (user.getID() == null) {
			System.out.println("Not able to get your ID.");
		} else {
			Notification joinGameNotificationToHost = new Notification();
			joinGameNotificationToHost.set("hostUsername",
					gameHost.getUsername());
			joinGameNotificationToHost.set("hostUUID", gameHost.getID());
			joinGameNotificationToHost
					.set("playerUsername", user.getUsername());
			joinGameNotificationToHost.set("playerUUID", user.getID());
			joinGameNotificationToHost.set("request", "newGameResponse");
			try {
				elvin.send(joinGameNotificationToHost);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				thereHasBeenAnError = true;
			}
		}

		// TODO: Expand this to stay within this method until game has been set up.

	}

	
	/**
	 * Broadcast p and q used for commutative RSA.
	 *
	 * @param p the p
	 * @param q the q
	 */
	public void broadcastPQ(BigInteger p, BigInteger q) {
		// TODO: create a notification to broadcast p and q to all players.
		return;
	}

	
	/**
	 * Wait for p and q.
	 *
	 * @return p and q
	 */
	public BigInteger[] waitPQ() {
		// TODO: subscribe to PQ notifications and return when received.
		return null;
	}

	
	/**
	 * Request a user to encrypt the deck.
	 *
	 * @param reqUser the user to request from
	 * @param encDeck the encrypted deck
	 * @return the new encrypted deck after reqUser has encrypted
	 */
	public EncryptedDeck requestEncDeck(User reqUser, EncryptedDeck encDeck) {
		// TODO: create a notification directed to reqUser with EncryptedDeck
		// TODO: wait until reqUser has replied with new EncryptedDeck and return it
		return null;
	}

	
	/**
	 * Wait for a request to encrypt the deck.
	 */
	public void waitEncryptedDeck() {
		// TODO: subscribe to notifications for your turn to encrypt the deck and return
		return;
	}

}
