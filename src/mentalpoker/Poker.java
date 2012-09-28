package mentalpoker;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mentalpoker.SwingGUI.HostGameTask;
import mentalpoker.SwingGUI.SearchGamesTask;
import mentalpoker.gameplay.GamePlay;
import mentalpoker.gameplay.TexasHoldEmGamePlay;

import org.avis.client.InvalidSubscriptionException;


/**
 * The Class Poker.
 */
public class Poker {

	/** The game user. */
	private static User gameUser;

	/** The signature service. */
	private static SigService sig;

	/** The communication service. */
	ComService comServ;


	/**
	 * Instantiates a new game of poker.
	 *
	 * @param username the username
	 * @throws Exception the exception
	 */
	public Poker(String username) throws Exception {

		setUsername(username);
		comServ = new ComService(gameUser,
				"elvin://elvin.students.itee.uq.edu.au", sig);
		return;

	}


	/**
	 * Start game.
	 *
	 * @param isGameHost the player is the host
	 * @param slots the number of player slots
	 * @param hgt the hgt
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws InvalidSubscriptionException the invalid subscription exception
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public void StartGame(boolean isGameHost, int slots,
			HostGameTask hgt) throws InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException, InvalidSubscriptionException,
			InterruptedException, IOException, InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			NoSuchProviderException {

		ArrayList<User> gameUsers = null;
		if (isGameHost) {
			gameUsers = comServ.startNewGame(slots, hgt);
		}

		if (gameUsers == null) {
			comServ.shutdown();
			System.exit(1);
			return;
		}

		System.out.println("\nGame Players:");
		for (int i = 0; i < gameUsers.size(); i++) {
			System.out.println(gameUsers.get(i).getUsername() + " "
					+ gameUsers.get(i).getID());
		}
		System.out.println("");

		if (isGameHost) {
			playGameAsHost(gameUsers, hgt);
		}

		return;
	}


	/**
	 * Play game as host.
	 *
	 * @param gameUsers the game users
	 * @param hgt the hgt
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 */
	public void playGameAsHost(ArrayList<User> gameUsers, HostGameTask hgt)
			throws InterruptedException, IOException, InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			NoSuchProviderException, InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException {
		EncryptedDeck encDeck;
		RSAService rsaService = new RSAService();

		// if more types of poker were added this would be a parameter and
		// SwingUI would instantiate it
		GamePlay thGamePlay = new TexasHoldEmGamePlay();
		thGamePlay.init();

		gameUser.setDecryptionKey(rsaService.getD());

		// broadcast p and q
		comServ.broadcastPQ(rsaService.getP(), rsaService.getQ(),
				gameUsers.size());
		System.out.println(rsaService.getP().toString() + "\n"
				+ rsaService.getQ().toString());

		// create and encrypt the deck
		encDeck = createDeck(rsaService);

		try {
			thGamePlay.hostGamePlay(rsaService, comServ, gameUser, gameUsers,
					sig, encDeck, hgt);

		} catch (Exception e) {
			System.err.println("Error in host gameplay.");
			System.exit(0);
		}

		comServ.shutdown();
		return;

	}

	
	/**
	 * Play game as player.
	 *
	 * @param gameUsers the game users
	 * @param jgt the jgt
	 * @throws InvalidSubscriptionException the invalid subscription exception
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void playGameAsPlayer(ArrayList<User> gameUsers, SearchGamesTask jgt)
			throws InvalidSubscriptionException, InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			NoSuchProviderException, InterruptedException, IOException {

		RSAService rsaService = comServ.waitPQ();
		gameUser.setDecryptionKey(rsaService.getD());

		// if more types of poker were added this would be a parameter and
		// SwingUI would instantiate it
		GamePlay thGamePlay = new TexasHoldEmGamePlay();
		thGamePlay.init();

		try {
			thGamePlay.playerGamePlay(rsaService, comServ, gameUser, gameUsers,
					sig, jgt);
		} catch (Exception e) {
			System.err.println("Error in player gameplay.");
			System.exit(0);
		}

		comServ.shutdown();
		return;

	}

	
	/**
	 * Creates the deck.
	 *
	 * @param rsaServ the rsa serv
	 * @return the encrypted deck
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 */
	private EncryptedDeck createDeck(RSAService rsaServ)
			throws InvalidKeyException, BadPaddingException,
			IllegalBlockSizeException {
		Deck deck = new Deck();
		EncryptedDeck encDeck = rsaServ.encryptDeck(deck);
		return encDeck;
	}

	
	/**
	 * Sets the users username.
	 *
	 * @param setUsernameString the new username
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setUsername(String setUsernameString) throws IOException {
		gameUser = new User(setUsernameString, sig.getPublicKey());
	}

	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {

		try {
			sig = new SigService();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				SwingGUI.showGUI();

			}
		});

	}

	
	/**
	 * Gets the game user.
	 * 
	 * @return the game user
	 */
	public User getGameUser() {
		return gameUser;
	}

	
	/**
	 * Gets the game user username.
	 * 
	 * @return the game user username
	 */
	public String getGameUserUsername() {
		return gameUser.getUsername();
	}

	
	/**
	 * Sets the game username.
	 *
	 * @param username the new game username
	 */
	public static void setGameUsername(String username) {
		gameUser = new User(username, sig.getPublicKey());
	}

}
