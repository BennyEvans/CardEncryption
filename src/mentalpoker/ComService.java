package mentalpoker;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.math.BigInteger;
import java.net.ConnectException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mentalpoker.SwingGUI.HostGameTask;
import mentalpoker.SwingGUI.SearchGamesTask;

import org.avis.client.*;
import org.avis.common.InvalidURIException;


/**
 * The Class ComService.
 */
public class ComService {

	/** The user current. */
	private User user;

	/**
	 *  Parent swingworker
	 *  This is used to call publish() on the parent!!!
	 */

	private HostGameTask hgt;

	private SearchGamesTask jgt;


	/** The game host. */
	private User gameHost;

	/** The elvin service. */
	private static Elvin elvin;

	/** The game subscription. */
	private Subscription gameSub;

	/** The current game members. */
	private ArrayList<User> currentGameMembers = new ArrayList<User>();

	/** The num responses. */
	private int numResponses;

	/** The available games. */
	private ArrayList<User> availableGames = new ArrayList<User>();

	/** The pq. */
	private BigInteger pq[] = null;

	/** The encrypted deck. */
	private EncryptedDeck encryptedDeck = null;

	/** The sig serv. */
	private SigService sigServ;

	/** The sig public key. */
	private final byte[] sigPublicKey;

	private int decryptionCount;

	private EncryptedHand encryptedHand;

	private Subscription decryptSub;
	private Subscription cheaterSub;

	/** The not type. */
	private static final String NOT_TYPE = "TYPE";
	private static final String GAME_ID = "GAMEID";
	private static final String ENCRYPT_DECK_REQUEST = "encDReq";
	private static final String ENCRYPT_DECK_REPLY = "encDRep";
	private static final String ENCRYPTED_DECK = "encDeck";
	private static final String ENCRYPTED_HAND = "encHand";
	private static final String REQ_USER = "userRequested";
	private static final String JOIN_GAME = "newGameResponse";
	private static final String NEW_GAME = "newGame";
	private static final String GAME_FULL = "gameFull";
	private static final String GAME_USERS = "gameUsers";
	private static final String START_GAME = "startGame";
	private static final String BROADCAST_PQ = "broadcastpq";
	private static final String BROADCAST_PQ_REPLY = "broadcastpqreply";
	private static final String PUB_KEY = "pubkkey";
	private static final String DECRYPT_HAND_REQUEST = "dechandreq";
	private static final String DECRYPT_HAND_REPLY = "dechandrep";
	private static final String SOURCE_USER = "userSource";
	private static final String SEND_ENCRYPTED_HAND = "sendEncHand";
	private static final String CHEATER = "cheater";
	private static final String SIGNATURE = "signature";

	/** Cheat Reasons */
	public static final int PUBLIC_KEY_CHANGED = 1;
	public static final int SIGNATURE_FAILED = 2;
	public static final int USER_TABLE_SIGNATURE_FAILED = 3;
	public static final int DECRYPT_REQUEST_ABUSE = 4;

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
	 * Find user by id.
	 *
	 * @param userID the users ID
	 * @return the user
	 */
	private User findUserByID(String userID) {
		for (int i = 0; i < currentGameMembers.size(); i++) {
			if (currentGameMembers.get(i).getID().equals(userID)) {
				return currentGameMembers.get(i);
			}
		}
		return null;
	}


	/**
	 * Byte array to public key.
	 *
	 * @param tmp the tmp
	 * @return the public key
	 */
	private PublicKey byteArrayToPublicKey(byte[] tmp) {
		PublicKey pubK = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(tmp);
		ObjectInput in;
		try{
			in = new ObjectInputStream(bis);
			pubK = (PublicKey) in.readObject(); 
			bis.close();
			in.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		return pubK;
	}



	/**
	 * Instantiates a new communication service.
	 *
	 * @param user the current user
	 * @param server the server
	 * @param sigServ the signature service
	 * @throws ConnectException the connect exception
	 * @throws InvalidURIException the invalid uri exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidKeyException 
	 */
	public ComService(User user, String server, SigService sigServ) throws ConnectException, InvalidURIException,
	IllegalArgumentException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {

		this.sigServ = sigServ;
		this.user = user;
		notificationHandle = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(this.sigServ.getPublicKey());
		} catch (IOException e1) {
			shutdown();
			System.exit(0);
		}

		sigPublicKey = bos.toByteArray();

		elvin = new Elvin(server);
		elvin.closeOnExit();
	}


	/**
	 * Shutdown.
	 */
	public void shutdown(){
		stopDecryptingHands();
		stopListeningForCheaters();
		elvin.close();
	}


	/**
	 * Starts a new game. First creates the subscription to the responses, then
	 * sends out a new notification of the available game.
	 *
	 * @param numberOfSlots the number of slots
	 * @return the list of users in the game
	 * @throws InterruptedException the interrupted exception
	 * @throws InvalidSubscriptionException the invalid subscription exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ArrayList<User> startNewGame(final int numberOfSlots) throws InterruptedException, InvalidSubscriptionException, IOException {

		gameHost = user;

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
				gameNotification.set(GAME_ID, gameHost.getID());
				gameNotification.set("hostUsername", gameHost.getUsername());
				gameNotification.set(NOT_TYPE, NEW_GAME);
				gameNotification.set(PUB_KEY, sigPublicKey);

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
				+ gameHost.getID() + "'");


		System.out.println("Now hosting game...");

		//Receive requests to join the game.
		gameSub.addListener(new NotificationListener() {
			// This is called if we emile a response requesting to join our game.
			public void notificationReceived(NotificationEvent event) {
				if (currentGameMembers.size() < numberOfSlots) {

					// This means that the notification by the client that they want to join in
					// must have a field named "playerUsername" with their own username included.
					User tmpUser = new User(event.notification.getString("playerUsername"),
							event.notification.getString("playerUUID"),
							byteArrayToPublicKey((byte[]) event.notification.get(PUB_KEY))
							);
					currentGameMembers.add(tmpUser);
					String numberOfSlotsLeft = Integer.toString(numberOfSlots - currentGameMembers.size());
					System.out.println(event.notification.getString("playerUsername")
							+ " connected... " + numberOfSlotsLeft + " slots left.");

					if (currentGameMembers.size() == numberOfSlots){
						//send start game notification
						Notification not = new Notification();
						UserList ul = new UserList();

						currentGameMembers.add(user);
						ul.users = new ArrayList<User>(currentGameMembers);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutput out = null;
						try {
							out = new ObjectOutputStream(bos);
							out.writeObject(ul);
						} catch (IOException e1) {
							shutdown();
							System.exit(-2);
						}   

						byte[] tmpBytes = bos.toByteArray();
						try {
							listenCheat(true);
						} catch (Exception e2) {
							shutdown();
							System.exit(-2);
						}
						not.set(NOT_TYPE, START_GAME);
						not.set(GAME_ID, gameHost.getID());
						not.set(GAME_USERS, tmpBytes);
						try {
							not.set(SIGNATURE, sigServ.createSignatureFromByteArray(tmpBytes));
						} catch (Exception e1) {
							shutdown();
							System.exit(-2);
						}
						//user table alter test
						//tmpBytes[1] = 5;
						//not.set(GAME_USERS, tmpBytes);

						try {
							elvin.send(not);
						} catch (IOException e) {
							shutdown();
							System.exit(-2);
						}

						try {
							out.close();
							bos.close();
						} catch (IOException e) {
							//do nothing
						}

						synchronized (gameSub) {
							gameSub.notify();
						}
					}

				} else {

					System.out.println("User attempted to join, but game is full.");
					Notification gameFullNotification = new Notification();
					gameFullNotification.set(NOT_TYPE, GAME_FULL);
					gameFullNotification.set(GAME_ID, gameHost.getID());				

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

		//sleep for 250ms...
		//I'm not sure this is necessary but its just a precaution... is Elvin FIFO?
		//It's here to prevent the next command (send pq) being out of order and received
		//before the START_GAME notification.
		Thread.sleep(250);

		//send game full to other users not in the game
		Notification gameFullNotification = new Notification();
		gameFullNotification.set(NOT_TYPE, GAME_FULL);
		gameFullNotification.set(GAME_ID, gameHost.getID());				
		elvin.send(gameFullNotification);

		Thread.sleep(50);

		System.out.println("Starting Game!");

		availableGames = null;
		return currentGameMembers;

	}



	/**
	 * Join game off menu.
	 *
	 * @return the list of users in the game
	 * @throws InterruptedException the interrupted exception
	 * @throws InvalidSubscriptionException the invalid subscription exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ArrayList<User> joinGameOffMenu() throws InterruptedException, InvalidSubscriptionException, IOException {

		Subscription gameFullSub;
		Subscription gameAdvertisementSub;
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
						.getString(GAME_ID),
						byteArrayToPublicKey((byte[]) event.notification.get(PUB_KEY)));
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
			System.err.println("Your input was not a number.");
			currentGameMembers = null;
			availableGames = null;
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
			availableGames = null;
			return null;
		}
		return currentGameMembers;
	}



	/**
	 * Join game.
	 *
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
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

					byte[] tmpBytes = (byte[]) e.notification.get(GAME_USERS);

					byte[] hostSignature = (byte[]) e.notification.get(SIGNATURE);
					try {
						if (!sigServ.validateSignatureForByteArray(hostSignature, tmpBytes, gameHost.getPublicKey())){
							callCheat(USER_TABLE_SIGNATURE_FAILED);
						}
					} catch (Exception e2) {
						//could not validate signature
						shutdown();
						System.exit(-1);
					}

					ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
					ObjectInput in;
					UserList ul = null;
					try {
						in = new ObjectInputStream(bis);
						ul = (UserList) in.readObject(); 
						bis.close();
						in.close();
					} catch (Exception e1) {
						shutdown();
						System.exit(-2);
					}

					if (ul == null){
						currentGameMembers = null;
					} else {
						currentGameMembers = ul.users;
						User tmpUser = findUserByID(user.getID());
						if (tmpUser == null){
							//wasn't in the game list... maybe we missed out =(
							currentGameMembers = null;
						} else {
							if (!tmpUser.getPublicKey().equals(user.getPublicKey())){
								//someone has changed your public key... call cheat
								try {
									callCheat(PUBLIC_KEY_CHANGED);
								} catch (Exception e1) {
									shutdown();
									System.exit(-1);
								}
							}
						}
					}


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
			joinGameNotificationToHost.set(PUB_KEY, sigPublicKey);

			synchronized (startSub) {
				//send notification
				elvin.send(joinGameNotificationToHost);

				//wait until received reply
				System.out.println("Waiting for subscription");
				startSub.wait();
			}

			//start listening for cheating
			listenCheat(false);

			startSub.remove();
			if (currentGameMembers == null){
				//wasn't in the game list
				return true;
			}
			return false;
		}

	}


	/**
	 * Broadcast p and q used for commutative RSA.
	 *
	 * @param p the p
	 * @param q the q
	 * @param numUsers the num users
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void broadcastPQ(final BigInteger p, final BigInteger q, final int numUsers) throws InterruptedException, IOException {

		final Subscription pqSub = elvin.subscribe(NOT_TYPE + " == '" + BROADCAST_PQ_REPLY +"' && " + GAME_ID + " == '" + gameHost.getID() + "'");
		Notification pqnot = new Notification();
		numResponses = 1;

		pqSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				numResponses++;
				if (numResponses >= numUsers){
					synchronized (pqSub) {
						pqSub.notify();
					}
				}
			}
		});

		pqnot.set(GAME_ID, gameHost.getID());
		pqnot.set(NOT_TYPE, BROADCAST_PQ);
		pqnot.set("p", p.toString());
		pqnot.set("q", q.toString());
		elvin.send(pqnot);

		synchronized (pqSub) {
			elvin.send(pqnot);
			//wait until received reply
			pqSub.wait();
		}

		//short sleep before returning
		Thread.sleep(100);

		return;
	}


	/**
	 * Wait for p and q.
	 *
	 * @return p and q
	 * @throws InterruptedException the interrupted exception
	 * @throws InvalidSubscriptionException the invalid subscription exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public RSAService waitPQ() throws InterruptedException, InvalidSubscriptionException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {

		final Subscription pqSub = elvin.subscribe(NOT_TYPE + " == '" + BROADCAST_PQ +"' && " + GAME_ID + " == '" + gameHost.getID() + "'");
		Notification pqnot = new Notification();
		RSAService ret;

		pqSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				pq = new BigInteger[2];
				pq[0] = new BigInteger(e.notification.getString("p"));
				pq[1] = new BigInteger(e.notification.getString("q"));

				//notify the waiting thread that a message has arrived
				synchronized (pqSub) {
					pqSub.notify();
				}
			}
		});

		synchronized (pqSub) {
			//wait until received reply
			pqSub.wait();
		}

		ret = new RSAService(pq[0], pq[1]);

		System.out.println(pq[0].toString() + "\n" + pq[1].toString());

		pqnot.set(GAME_ID, gameHost.getID());
		pqnot.set(NOT_TYPE, BROADCAST_PQ_REPLY);
		elvin.send(pqnot);

		pqSub.remove();

		return ret;

	}


	/**
	 * Request a user to encrypt the deck.
	 *
	 * @param reqUser the user to request from
	 * @param encDeck the encrypted deck
	 * @return the new encrypted deck after reqUser has encrypted
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public EncryptedDeck requestEncDeck(User reqUser, EncryptedDeck encDeck) throws IOException, InterruptedException {

		Notification not = new Notification();
		encryptedDeck = null;
		final Subscription encSub = elvin.subscribe(NOT_TYPE + " == '" + ENCRYPT_DECK_REPLY +"' && " + GAME_ID + " == '" + gameHost.getID() + "'");

		encSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				byte[] tmpBytes = (byte[]) e.notification.get(ENCRYPTED_DECK);

				ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
				ObjectInput in;
				try {
					in = new ObjectInputStream(bis);
					encryptedDeck = (EncryptedDeck) in.readObject(); 
					bis.close();
					in.close();
				} catch (Exception e1) {
					shutdown();
					System.exit(-2);
				}

				//notify the waiting thread that a message has arrived
				synchronized (encSub) {
					encSub.notify();
				}


			}
		});

		//construct the notification to send
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		out = new ObjectOutputStream(bos);
		out.writeObject(encDeck);

		byte[] tmpBytes = bos.toByteArray();

		not.set(NOT_TYPE, ENCRYPT_DECK_REQUEST);
		not.set(GAME_ID, gameHost.getID());
		not.set(REQ_USER, reqUser.getID());
		not.set(ENCRYPTED_DECK, tmpBytes);

		synchronized (encSub) {
			//send notification
			elvin.send(not);
			System.out.println("Sending encrypt deck request to user - " + reqUser.getID());

			//wait until received reply
			encSub.wait();
		}

		encSub.remove();

		return encryptedDeck;
	}


	/**
	 * Wait for a request to encrypt the deck.
	 *
	 * @param rsaService the rsa service
	 * @param sig the signature service
	 * @param pubKey the Public Key of the game Host
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InterruptedException the interrupted exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public boolean waitEncryptedDeck(RSAService rsaService, final PublicKey pubKey) throws IOException, InterruptedException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException {
		Notification not = new Notification();
		encryptedDeck = null;
		final Subscription encSub = elvin.subscribe(NOT_TYPE + " == '" + ENCRYPT_DECK_REQUEST +"' && " + GAME_ID + " == '" + gameHost.getID() + "' && " + REQ_USER + " == '" + user.getID() + "'");

		encSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				//System.out.println("Got Request!");

				byte[] tmpBytes = (byte[]) e.notification.get(ENCRYPTED_DECK);

				ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
				ObjectInput in;
				try {
					in = new ObjectInputStream(bis);
					encryptedDeck = (EncryptedDeck) in.readObject(); 
					bis.close();
					in.close();
					if (!sigServ.validateSignature(encryptedDeck, pubKey)){
						//sig failed!
						callCheat(SIGNATURE_FAILED);
						encryptedDeck = null;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				//notify the waiting thread that a message has arrived
				synchronized (encSub) {
					encSub.notify();
				}


				//notify the waiting thread that a message has arrived
				synchronized (encSub) {
					encSub.notify();
				}

			}
		});

		synchronized (encSub) {
			//wait until received reply
			encSub.wait();
		}


		//construct the notification to send
		if (encryptedDeck == null){
			encSub.remove();
			return false;
		}

		//encrypt and shuffle the deck
		EncryptedDeck encDeck = rsaService.encryptEncDeck(encryptedDeck, user);
		encDeck.shuffleDeck();

		//sign the deck
		sigServ.createSignature(encDeck);

		//String tmpStr = new String(encDeck.data.get(0).cardData);
		//System.out.println("First Card: " + tmpStr.toString());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		out = new ObjectOutputStream(bos);
		out.writeObject(encDeck);

		byte[] tmpBytes = bos.toByteArray();

		not.set(NOT_TYPE, ENCRYPT_DECK_REPLY);
		not.set(GAME_ID, gameHost.getID());
		not.set(REQ_USER, user.getID());
		not.set(ENCRYPTED_DECK, tmpBytes);

		elvin.send(not);

		encSub.remove();
		return true;

	}


	/**
	 * Sends each user their fully encrypted hand.
	 *
	 * @param usr the user
	 * @param hand the hand
	 * @throws IOException 
	 */
	public void sendEncryptedHand(User usr, EncryptedHand hand) throws IOException{
		Notification not = new Notification();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		out = new ObjectOutputStream(bos);
		out.writeObject(hand);

		System.out.println("Sending encrypted hand - " + new String(usr.getID()));

		byte[] tmpBytes = bos.toByteArray();


		not.set(NOT_TYPE, SEND_ENCRYPTED_HAND);
		not.set(GAME_ID, gameHost.getID());
		not.set(REQ_USER, usr.getID());
		not.set(ENCRYPTED_HAND, tmpBytes);

		elvin.send(not);

		bos.close();
		out.close();
		return;
	}


	/**
	 * Wait to get your fully encrypted hand and decrypt it by requesting from each user.
	 *
	 * @return the encrypted hand
	 * @throws IOException 
	 * @throws InvalidSubscriptionException 
	 * @throws InterruptedException 
	 */
	public EncryptedHand waitEncryptedHand(final PublicKey pKey) throws InvalidSubscriptionException, IOException, InterruptedException{
		final Subscription eSub = elvin.subscribe(NOT_TYPE + " == '" + SEND_ENCRYPTED_HAND +"' && " + GAME_ID + " == '" + gameHost.getID() + "' && " + REQ_USER + " == '" + user.getID() + "'");
		encryptedHand = null;
		System.out.println("Waiting for encrypted hand - " + new String(user.getID()));

		eSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				System.out.println("Got waitEncryptedHand request.");

				byte[] tmpBytes = (byte[]) e.notification.get(ENCRYPTED_HAND);

				ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
				ObjectInput in;
				try {
					in = new ObjectInputStream(bis);
					encryptedHand = (EncryptedHand) in.readObject(); 
					bis.close();
					in.close();
					if (!sigServ.validateSignature(encryptedHand, pKey)){
						//sig failed!
						callCheat(SIGNATURE_FAILED);
						encryptedDeck = null;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				//notify the waiting thread that a message has arrived
				synchronized (eSub) {
					eSub.notify();
				}


				//notify the waiting thread that a message has arrived
				synchronized (eSub) {
					eSub.notify();
				}

			}
		});

		synchronized (eSub) {
			//wait until received reply
			eSub.wait();
		}

		eSub.remove();

		return encryptedHand;
	}


	//THIS FUNCTION NEEDS TO BE CALLED BEFORE waitEncryptedHand()
	//asynchronous function - takes requests to decrypt an encrypted hand
	/**
	 * Asynchronous function - takes requests to decrypt an encrypted hand
	 *
	 * @param rsaService the rsa service
	 * @throws IOException 
	 * @throws InvalidSubscriptionException 
	 */
	public void decryptEncryptedHands(final RSAService rsaService, final int numRequests) throws InvalidSubscriptionException, IOException{
		//should setup an asynchronous subscription and cancel when 
		//all we have decrypted all users encrypted hands

		decryptionCount = 0;

		decryptSub = elvin.subscribe(NOT_TYPE + " == '" + DECRYPT_HAND_REQUEST +"' && " + GAME_ID + " == '" + gameHost.getID() + "' && " + REQ_USER + " == '" + user.getID() + "'");

		decryptSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				System.out.println("Got decrypt request.");

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutput out = null;
				EncryptedHand encHand = null;
				Notification not;

				User userRequesting = findUserByID(e.notification.getString(SOURCE_USER));

				if (userRequesting == null){
					return;
				}

				decryptionCount++;
				if (decryptionCount > numRequests){
					//more decryption requests than expected... someone is trying to cheat
					try {
						callCheat(DECRYPT_REQUEST_ABUSE);
					} catch (Exception e1) {
						shutdown();
						System.exit(-1);
					}
				}

				byte[] tmpBytes = (byte[]) e.notification.get(ENCRYPTED_HAND);

				ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
				ObjectInput in;
				try {
					in = new ObjectInputStream(bis);
					encHand = (EncryptedHand) in.readObject(); 
					bis.close();
					in.close();
					if (!sigServ.validateSignature(encHand, userRequesting.getPublicKey())){
						//sig failed!
						callCheat(SIGNATURE_FAILED);
						System.exit(0);
					}
					//decrypt and sign
					encHand = rsaService.decyrptEncHand(encHand);
					sigServ.createSignature(encHand);
					bis.close();
					in.close();
				} catch (Exception e1) {
					shutdown();
					System.exit(0);
				}

				try {
					out = new ObjectOutputStream(bos);
					out.writeObject(encHand);
				} catch (IOException e1) {
					shutdown();
					System.exit(1);
				}

				byte[] retBytes = bos.toByteArray();

				not = new Notification();
				not.set(NOT_TYPE, DECRYPT_HAND_REPLY);
				not.set(GAME_ID, gameHost.getID());
				not.set(REQ_USER, userRequesting.getID());
				not.set(ENCRYPTED_HAND, retBytes);

				try {
					elvin.send(not);
				} catch (IOException e2) {
					shutdown();
					System.exit(1);
				}

				try {
					bos.close();
					out.close();
				} catch (IOException e1) {
					shutdown();
					System.exit(1);
				}

			}
		});

		return;
	}

	public void stopDecryptingHands(){
		try{
			decryptSub.remove();
		} catch (Exception e){
			//do nothing
		}
		decryptSub = null;
	}

	public EncryptedHand requestDecryptHand(EncryptedHand encHand, final User usr) throws InvalidSubscriptionException, IOException, InterruptedException{


		final Subscription encSub = elvin.subscribe(NOT_TYPE + " == '" + DECRYPT_HAND_REPLY +"' && " + GAME_ID + " == '" + gameHost.getID() + "' && " + REQ_USER + " == '" + user.getID() + "'");
		Notification not;
		encryptedHand = null;

		encSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				System.out.println("Got reply from requesting user to decrypt hand.");

				byte[] tmpBytes = (byte[]) e.notification.get(ENCRYPTED_HAND);

				ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
				ObjectInput in;
				try {
					in = new ObjectInputStream(bis);
					encryptedHand = (EncryptedHand) in.readObject(); 
					bis.close();
					in.close();
					if (!sigServ.validateSignature(encryptedHand, usr.getPublicKey())){
						//sig failed!
						callCheat(SIGNATURE_FAILED);
						System.exit(0);
					}
					bis.close();
					in.close();
				} catch (Exception e1) {
					shutdown();
					System.exit(0);
				}

				synchronized (encSub) {
					encSub.notify();
				}


			}
		});

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(encHand);

		byte[] retBytes = bos.toByteArray();

		not = new Notification();
		not.set(NOT_TYPE, DECRYPT_HAND_REQUEST);
		not.set(GAME_ID, gameHost.getID());
		not.set(SOURCE_USER, user.getID());
		not.set(REQ_USER, usr.getID());
		not.set(ENCRYPTED_HAND, retBytes);

		synchronized (encSub) {
			elvin.send(not);
			//wait until received reply
			encSub.wait();
		}

		bos.close();
		out.close();
		encSub.remove();

		return encryptedHand;
	}


	/**
	 * Listen for cheat notifications.
	 * @throws IOException 
	 * @throws InvalidSubscriptionException 
	 */
	public void listenCheat(final boolean isHost) throws InvalidSubscriptionException, IOException{
		//in here set up a subscription to cheat
		//if a cheat message is received then check the verification to assure its actually
		//from a user in this game and not a random spoofing cheat notifications
		//if its genuine display the reason and exit

		cheaterSub = elvin.subscribe(NOT_TYPE + " == '" + CHEATER +"' && " + GAME_ID + " == '" + gameHost.getID() + "'");

		cheaterSub.addListener(new NotificationListener() {
			public void notificationReceived(NotificationEvent e) {

				String userid = e.notification.getString(SOURCE_USER);

				if (user.getID().equals(userid)){
					//ignore your own cheater notifications
					return;
				}
				User tmpUser = findUserByID(userid);

				if (tmpUser == null){
					//not in our list so return
					return;
				}

				System.out.println("Got cheater notification!");

				try {
					if (sigServ.validateVerifiedSignature((byte[]) e.notification.get(SIGNATURE), tmpUser.getPublicKey())){
						//signature validated so this is a real cheat notification
						int reason = e.notification.getInt("reason");
						if (isHost){
							System.out.println("Cheater notification was genuine! - Relaying notification!");
							//wait 1 second and relay the message... people trust you
							Thread.sleep(1000);
							callCheat(reason);
						} else {
							System.out.println("Cheater Detected - Cheater notification was genuine!");
						}
						switch(reason){
						case PUBLIC_KEY_CHANGED:
							System.out.println("Reason: Someone has attempted to change a users public key.");
							break;
						case SIGNATURE_FAILED:
							System.out.println("Reason: A user found a signature that failed.");
							break;
						case DECRYPT_REQUEST_ABUSE:
							System.out.println("Reason: A user is trying to exploit the decryption of other users cards.");
							break;
						case USER_TABLE_SIGNATURE_FAILED:
							System.out.println("Reason: The user table sent (with users in the game and their public keys) is reported to be altered. The signature failed.");
							break;
						}
					}
				} catch (Exception e1) {
					//do nothing
				}
			}
		});

		return;

	}


	/**
	 * Call out a cheater by broadcasting a notification.
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws InvalidKeyException 
	 */
	public void callCheat(int reason) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException{
		System.out.println("Cheater Detected!");
		switch(reason){
		case PUBLIC_KEY_CHANGED:
			System.out.println("Reason: Someone has attempted to change your public key.");
			break;
		case SIGNATURE_FAILED:
			System.out.println("Reason: Signature verification has failed - someone may have altered a message.");
			break;
		case DECRYPT_REQUEST_ABUSE:
			System.out.println("Reason: A user has tried to exploit your decryption of other users cards.");
			break;
		case USER_TABLE_SIGNATURE_FAILED:
			System.out.println("Reason: The user table sent (with users in the game and their public keys) has been altered. The signature failed.");
			break;
		}

		Notification not = new Notification();
		not.set(NOT_TYPE, CHEATER);
		not.set(GAME_ID, gameHost.getID());
		not.set(SOURCE_USER, user.getID());
		not.set("reason", reason);
		not.set(SIGNATURE, sigServ.createVerifiedSignature());

		elvin.send(not);
		shutdown();
		System.exit(-1);

	}

	public void stopListeningForCheaters(){
		try{
			cheaterSub.remove();
		} catch (Exception e){
			//do nothing
		}
		cheaterSub = null;
	}


}
