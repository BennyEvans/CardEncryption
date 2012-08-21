package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.avis.client.InvalidSubscriptionException;

/**
 * The Class Poker.
 */
public class Poker {
	
	/** The com. */
	ComService com;
	
	/** The game user. */
	private User gameUser;


	/**
	 * Instantiates a new game.
	 *
	 * @throws Exception the exception
	 */
	public Poker() throws Exception {
		
		setUsername();
		com = new ComService(gameUser, "elvin://elvin.students.itee.uq.edu.au");
		StartGame();
		return;

	}

	
	/**
	 * Start game.
	 *
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws InvalidSubscriptionException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	private void StartGame() throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidSubscriptionException, InterruptedException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException{
		//Menu choice becomes the integer chosen by the user.
		int menuChoice = MenuOptions.printMainMenu();
		boolean isGameHost = false;
		ArrayList<User> gameUsers = null;
		EncryptedDeck encDeck = null;
		RSAService rsaService;

		if (menuChoice == MenuOptions.HOST_GAME)
		{
			isGameHost = true;
			gameUsers = com.startNewGame(MenuOptions.startNewGameMenu());
		} else if (menuChoice == MenuOptions.JOIN_GAME) {
			gameUsers = com.joinGameOffMenu();
		} else if (menuChoice == Integer.MIN_VALUE)
		{
			System.err.println("Sorry, I was unable to recognise what your input as a number. Try numbers, like 1,2,3 etc.");
			StartGame();
			return;
		}
		
		if (gameUsers == null){
			//could call startGame() here but exit is good enough for now
			com.shutdown();
			System.exit(1);
			return;
		}
		
		System.out.println("\nGame Players:");
		for (int i = 0; i < gameUsers.size(); i++){
			System.out.println(gameUsers.get(i).getUsername() + " " + gameUsers.get(i).getID());
		}
		
		//once com.startNewGame or com.joinGameOffMenu have returned the game is ready...
		if (isGameHost){
			rsaService = new RSAService();
			//broadcast p and q
			com.broadcastPQ(rsaService.getP(), rsaService.getQ());
			//create and encrypt the deck
			encDeck = createDeck(gameUser, rsaService);
			//send the deck
			//for each user in gameUsers call com.requestEncDeck(user, encDeck)
			
			//TODO: add more here later... the above will do for now
			
		} else {
			//subscribe to PQ (com.waitPQ)
			//subscribe to receive the encrypted deck (com.waitEncryptedDeck)
			
			//TODO: add more here later... the above will do for now
		}
		
		return;
	}
	
	
	/**
	 * Creates the deck.
	 *
	 * @param user the user
	 * @return the encrypted deck
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 */
	private EncryptedDeck createDeck(User user, RSAService rsaServ) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		Deck deck = new Deck(rsaServ, user);
		EncryptedDeck encDeck = new EncryptedDeck();
		for (int i = 0; i < Deck.NUM_CARDS; i++){
			encDeck.encCards[i] = deck.getEncryptedCard(i);
		}
		return encDeck;
	}

	
	/**
	 * Sets the username.
	 */
	public void setUsername()
	{
		System.out.print("Enter your username: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			gameUser = new User(br.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UNABLE TO READ FROM COMMAND LINE");
		}
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			
			new Poker();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
