package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.SwingUtilities;

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
			isGameHost = false;
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
			System.out.println(rsaService.getP().toString() + "\n" + rsaService.getQ().toString());
			//create and encrypt the deck
			encDeck = createDeck(gameUser, rsaService);
			
			//NEED TO CREATE A COPY OF encDeck for validation?
			
			//for each user in gameUsers request to encrypt the deck
			for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
				User tmpUser = usr.next();
				if (!tmpUser.getID().equals(gameUser.getID())){
					encDeck = com.requestEncDeck(tmpUser, encDeck);
				}
			}

			System.out.println("All Users have encrypted the deck!");
			
			//TODO: add more here later... the above will do for now
			
		} else {
			//get p and q
			BigInteger tmp[] = com.waitPQ();
			BigInteger p = tmp[0];
			BigInteger q = tmp[1];
			System.out.println(p.toString() + "\n" + q.toString());
			
			//create rsaservice with given p and q
			rsaService = new RSAService(p, q);
			com.waitEncryptedDeck(rsaService);
			
			System.out.println("Got encrypted deck!");
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
		Deck deck = new Deck();
		EncryptedDeck encDeck = rsaServ.encryptDeck(deck, user);
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
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	SwingGUI.showGUI();

            	/*
            	 * This stuff is disabled for the moment because it includes its own loops.
            	 */
            	//    			try {
            	//					new Poker();
            	//				} catch (Exception e) {
            	//					// TODO Auto-generated catch block
            	//					e.printStackTrace();
            	//				}
            }
        });
	}
	
}
