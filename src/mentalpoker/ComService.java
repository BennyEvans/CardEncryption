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
	
	/** The available games. */
	private ArrayList<User> availableGames = new ArrayList<User>();
	
	/** The game host. */
	private User gameHost;

	//COULD JUST HAVE AN ENUM HERE
	
	private final String NOT_TYPE = "TYPE";
	private final String GAME_ID = "GAMEID";
	
	//join game
	private final String JOIN_GAME = "newGameResponse";
	
	//broadcast a game
	private final String NEW_GAME = "newGame";
	
	private final String GAME_FULL = "gameFull";
	
	private final String START_GAME = "startGame";
	
	//broadcast p and q
	private final String BROADCAST_PQ = "broadcastpq";
	
	//request a user to encrypt the deck
	private final String REQUEST_ENC_DECK = "requestEncDeck";
	
	//reply from a request to encrypt the deck
	private final String REPLY_ENC_DECK = "requestEncDeckReply";
	
	/** The notification handle. */
	ScheduledFuture<?> notificationHandle;

	/** The scheduler. */
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	
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
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InvalidSubscriptionException 
	 */
	public ArrayList<User> startNewGame(final int numberOfSlots) throws InterruptedException, InvalidSubscriptionException, IOException {
		
		//notify potential players that there is a game
		final Runnable notifyPotentialJoiners = new Runnable() {
			public void run() {
				if (currentGameMembers.size() >= numberOfSlots) {
					notificationHandle.cancel(true);
				} else {
					try {
						sendGameNotification();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			private void sendGameNotification() throws IOException {
				Notification gameNotification = new Notification();
				gameNotification.set(GAME_ID, user.getID());
				gameNotification.set("hostUsername", user.getUsername());
				gameNotification.set(NOT_TYPE, NEW_GAME);
				
				elvin.send(gameNotification);

			}
		};
		
		
		if (user.getUsername() == null) {
			System.err.println("You cannot have an empty username.");
			return null;
		}
		// Check that there is a sufficient number of slots specified
		if (numberOfSlots < 1) {
			System.err.println("You must specify at least one slot available.");
			return null;
		}

		// Subscribe to responses bearing my username. This will be useful after we actually advertise the game.
		gameSub = elvin.subscribe(NOT_TYPE + " == '" + JOIN_GAME +"' && " + GAME_ID + " == '"
							+ user.getID() + "'");


		System.out.println("Now hosting game...");

		//Receive requests to join the game.
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
					String numberOfSlotsLeft = Integer.toString(numberOfSlots - currentGameMembers.size());
					System.out.println(event.notification.getString("playerUsername")
							+ " connected... " + numberOfSlotsLeft + " slots left.");
					
					if (currentGameMembers.size() == numberOfSlots){
						//send start game notification
						Notification not = new Notification();
						not.set(NOT_TYPE, START_GAME);
						not.set(GAME_ID, user.getID());
						try {
							elvin.send(not);
						} catch (IOException e) {
							e.printStackTrace();
						}

						synchronized (gameSub) {
							gameSub.notify();
						}
					}
					
				} else {
					
					System.out.println("User attempted to join, but game is full.");
					Notification gameFullNotification = new Notification();
					gameFullNotification.set("requesterUsername", user.getUsername());
					gameFullNotification.set(NOT_TYPE, GAME_FULL);

					try {
						elvin.send(gameFullNotification);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});


		notificationHandle = scheduler.scheduleAtFixedRate(
				notifyPotentialJoiners, 498, 498, TimeUnit.MILLISECONDS);
		
		synchronized (gameSub) {
			//wait until game is full
			gameSub.wait();
		}
		
		//cleanup
		notificationHandle.cancel(true);
		gameSub.remove();
		scheduler.shutdown();
		
		//sleep for 500ms...
		//I'm not sure this is necessary but its just a precaution... is Elvin FIFO?
		//It's here to prevent the next command (send pq) being out of order and received
		//before the START_GAME notification.
		Thread.sleep(500);
		
		System.out.println("Starting Game!");

		ArrayList<User> tmp = new ArrayList<User>(currentGameMembers);
		currentGameMembers = null;
		return tmp;

	}

	
	/**
	 * Join game off menu.
	 *
	 * @return the list of users in the game
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InvalidSubscriptionException 
	 */
	public ArrayList<User> joinGameOffMenu() throws InterruptedException, InvalidSubscriptionException, IOException {
		
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
					System.out.println(String.valueOf(i) + " " + itr.next().getUsername());
					i++;
				}
				System.out.print("Choose a game (enter the number): ");

			}
		};

		System.out.println("Searching for available games...");
		availableGames.clear();

		// Subscribe to new game advertisement notifications
		gameAdvertisementSub = elvin.subscribe(NOT_TYPE + " == '" + NEW_GAME + "'");

		// Subscribe to game full notifications.
		gameFullSub = elvin.subscribe(NOT_TYPE + " == '" + GAME_FULL + "'");

		// Whenever a game notification is received, add it to the availableGames list if not in there already
		gameAdvertisementSub.addListener(new NotificationListener() {
			// This is called if we have a response requesting to join our game.
			public void notificationReceived(NotificationEvent event) {

				User tmpUser = new User(event.notification
						.getString("hostUsername"), event.notification
						.getString(GAME_ID));
				if (findGameHostByID(tmpUser.getID()) == null) {
					availableGames.add(tmpUser);
				}
			}

		});
		

		// If a game is reported as full, remove it from the available games list.
		gameFullSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent event) {
				// may need to change this
				User tmpUser = findGameHostByID(event.notification.getString(GAME_ID));
				if (tmpUser != null){
					availableGames.remove(tmpUser);
				}
			}

		});

		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		notificationHandle = scheduler.scheduleAtFixedRate(
				checkForAvailableGames, 1000, 3000, TimeUnit.MILLISECONDS);

		
		// Grab the hoster
		try {
			gameHost = availableGames.get(Integer.parseInt(br.readLine()));
		} catch (NumberFormatException e) {
			System.err.println("Your input was not a number!");
			currentGameMembers = null;
			scheduler.shutdown();
			notificationHandle.cancel(true);
			gameFullSub.remove();
			gameAdvertisementSub.remove();
			return null;
		}

		//cleanup
		scheduler.shutdown();
		notificationHandle.cancel(true);
		gameFullSub.remove();
		gameAdvertisementSub.remove();

		// At this point, we wish to notify the host that we wish to join their game.
		if (joinGame()){
			System.err.println("Error Joining Game.");
			currentGameMembers = null;
			return null;
		}
		
		System.out.println("Joined Game!");

		ArrayList<User> tmp = new ArrayList<User>(currentGameMembers);
		currentGameMembers = null;
		return tmp;

	}

	
	/**
	 * Join game.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public boolean joinGame() throws IOException, InterruptedException {

		if (gameHost.getUsername() == null) {
			System.out.println("Not able to get the game host's username.");
			return true;
		} else if (gameHost.getID() == null) {
			System.out.println("Not able to get the game host's id.");
			return true;
		} else if (user.getUsername() == null) {
			System.out.println("Not able to get the user's username.");
			return true;
		} else if (user.getID() == null) {
			System.out.println("Not able to get your ID.");
			return true;
		} else {
			
			Notification joinGameNotificationToHost = new Notification();
			
			//subscribe to start game notification
			final Subscription startSub = elvin.subscribe(NOT_TYPE + " == '" + START_GAME +"' && " + GAME_ID + " == '" + gameHost.getID() + "'");

			startSub.addListener(new NotificationListener() {
				public void notificationReceived(NotificationEvent e) {

						//notify the waiting thread that a message has arrived
						synchronized (startSub) {
							startSub.notify();
						}

	

				}
			});
			
			//construct the notification to send
			joinGameNotificationToHost.set("hostUsername", gameHost.getUsername());
			joinGameNotificationToHost.set(GAME_ID, gameHost.getID());
			joinGameNotificationToHost.set("playerUsername", user.getUsername());
			joinGameNotificationToHost.set("playerUUID", user.getID());
			joinGameNotificationToHost.set(NOT_TYPE, JOIN_GAME);

			synchronized (startSub) {
				//send notification
				elvin.send(joinGameNotificationToHost);

				//wait until received reply
				startSub.wait();
			}

			return false;
		}

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
