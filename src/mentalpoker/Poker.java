package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

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
	private static User gameUser;


	/**
	 * Instantiates a new game.
	 *
	 * @throws Exception the exception
	 */
	public Poker() throws Exception {
		
		//setUsername();
		com = new ComService(getGameUser(), "elvin://elvin.students.itee.uq.edu.au");
		//StartGame();
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
		System.out.println("");
		

		if (isGameHost){
			playGameAsHost(gameUsers);
		} else {
			 playGameAsPlayer(gameUsers);
		}
		
		return;
	}
	
	private void playGameAsHost(ArrayList<User> gameUsers) throws IOException, InterruptedException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException{
		EncryptedDeck encDeck = null;
		RSAService rsaService = new RSAService();
		EncryptedHand myHand;

		//broadcast p and q
		com.broadcastPQ(rsaService.getP(), rsaService.getQ(), gameUsers.size());
		System.out.println(rsaService.getP().toString() + "\n" + rsaService.getQ().toString());
		//create and encrypt the deck
		encDeck = createDeck(getGameUser(), rsaService);
	
		//for each user in gameUsers request to encrypt the deck
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(getGameUser().getID())){
				encDeck = com.requestEncDeck(tmpUser, encDeck);
			}
		}

		//String tmpStr = new String(encDeck.encCards.get(0).cardData);
		//System.out.println("First Card: " + tmpStr.toString());
		System.out.println("All Users have encrypted the deck!");
		
		//choose random cards for each user
		ArrayList<Integer> chosenCards = new ArrayList<Integer>();
		Random rnd;
		
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			EncryptedHand hand = new EncryptedHand();
			User tmpUser = usr.next();
			int count = 0;
			rnd = new Random();
			
			Integer tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));

			while (count < Hand.NUM_CARDS){
				while (chosenCards.contains(tmpInt)){
					tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));
				}
				chosenCards.add(tmpInt);
				hand.encCards.add(encDeck.encCards.get(tmpInt));
				count++;
			}
			if (!tmpUser.getID().equals(getGameUser().getID())){
				//these are my cards
				myHand = hand;
			} else {
				com.sendEncryptedHand(tmpUser, hand);
			}
		}
		
		return;
	}
	
	
	private void playGameAsPlayer(ArrayList<User> gameUsers) throws InvalidSubscriptionException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InterruptedException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		RSAService rsaService = com.waitPQ();
		EncryptedHand myHand;

		com.waitEncryptedDeck(rsaService);
		System.out.println("Got encrypted deck and encrypted again with my key!");
		
		//wait for cards
		myHand = com.waitEncryptedHand();
		
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
			setGameUser(new User(br.readLine()));
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
		
    	/*
    	 * This stuff is disabled for the moment because it includes its own loops.
    	 */
		/*try {
    		new Poker();
    	} catch (Exception e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
		System.exit(0);*/
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	SwingGUI.showGUI();

            }
        });
	}


	public static User getGameUser() {
		return gameUser;
	}


	public static void setGameUser(User gameUserl) {
		gameUser = gameUserl;
	}
	
}
