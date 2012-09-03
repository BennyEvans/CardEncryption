package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mentalpoker.SwingGUI.HostGameTask;

import org.avis.client.InvalidSubscriptionException;

/**
 * The Class Poker.
 */
public class Poker {
	
	/** The com. */
	ComService com;
	
	/** The game user. */
	private static User gameUser;
	
	private static SigService sig;


	/**
	 * Instantiates a new game.
	 *
	 * @throws Exception the exception
	 */
	public Poker() throws Exception {
		
		//setUsername();
		com = new ComService(gameUser, "elvin://elvin.students.itee.uq.edu.au", sig);
		//StartGame();
		return;

	}

	
	/**
	 * Start game.
	 * @return 
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
	public ArrayList<User> StartGame(boolean isGameHost, int slots, HostGameTask hgt) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidSubscriptionException, InterruptedException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException{
		//Menu choice becomes the integer chosen by the user.
		//int menuChoice = MenuOptions.printMainMenu();
		ArrayList<User> gameUsers = null;
		
		//send your public key to anyone who requests it
		//com.acceptPubKeySigRequests(sig.getPublicKey());
		if (isGameHost)
		{
			gameUsers = com.startNewGame(MenuOptions.startNewGameMenu(), hgt);
		}
		
		if (gameUsers == null){
			//could call startGame() here but exit is good enough for now
			com.shutdown();
			System.exit(1);
			return null;
		}
		
		System.out.println("\nGame Players:");
		for (int i = 0; i < gameUsers.size(); i++){
			//System.out.println(gameUsers.get(i).getUsername() + " " + gameUsers.get(i).getID() + new String(gameUsers.get(i).getPublicKey().getEncoded()));
			System.out.println(gameUsers.get(i).getUsername() + " " + gameUsers.get(i).getID());
		}
		System.out.println("");
		

		if (isGameHost){
			playGameAsHost(gameUsers);
		} else {
			 playGameAsPlayer(gameUsers);
		}
		
		return null;
	}
	
	private void playGameAsHost(ArrayList<User> gameUsers) throws IOException, InterruptedException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException{
		EncryptedDeck encDeck = null;
		RSAService rsaService = new RSAService();
		EncryptedHand myHand = null;

		//broadcast p and q
		com.broadcastPQ(rsaService.getP(), rsaService.getQ(), gameUsers.size());
		System.out.println(rsaService.getP().toString() + "\n" + rsaService.getQ().toString());
		//create and encrypt the deck
		encDeck = createDeck(gameUser, rsaService);
		
	
		//for each user in gameUsers request to encrypt the deck
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();

			if (!tmpUser.getID().equals(gameUser.getID())){
				//sign the message
				sig.createSignature(encDeck);
				
				//request user to encrypt the deck
				encDeck = com.requestEncDeck(tmpUser, encDeck);
				
				//check the reply was from the user
				if (!sig.validateSignature(encDeck, tmpUser.getPublicKey())){
					com.callCheat();
					com.shutdown();
					System.exit(2);
				}
			}
		}

		//String tmpStr = new String(encDeck.data.get(0).cardData);
		//System.out.println("First Card: " + tmpStr.toString());
		System.out.println("All Users have encrypted the deck!");
		
		//take requests to decrypt a hand
		com.decryptEncryptedHands(sig, rsaService, gameUsers, gameUsers.size()-1);
		
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
				hand.data.add(encDeck.data.get(tmpInt));
				count++;
			}
			if (tmpUser.getID().equals(gameUser.getID())){
				System.out.println("Got my cards!");
				//these are my cards
				myHand = hand;
			} else {
				sig.createSignature(hand);
				com.sendEncryptedHand(tmpUser, hand);
			}
		}
		
		System.out.println("Every user has their cards now!");
		
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(myHand);
				myHand = com.requestDecryptHand(myHand, tmpUser, sig);
			}
			
		}
		
		Hand hand = rsaService.decyrptHand(myHand);
		
		String card1 = Character.toString(hand.cards.get(0).cardType) + "-" + new String(hand.cards.get(0).suit);
		String card2 = Character.toString(hand.cards.get(1).cardType) + "-" + new String(hand.cards.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);
		
		//sit and block here until everyone has said gameover
		Thread.sleep(5000);
		return;
	}
	
	
	private void playGameAsPlayer(ArrayList<User> gameUsers) throws InvalidSubscriptionException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InterruptedException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		RSAService rsaService = com.waitPQ();
		EncryptedHand myHand;
		
		PublicKey gameHostsPubKey = gameUsers.get(gameUsers.size()-1).getPublicKey();

		//take requests to decrypt a hand
		com.decryptEncryptedHands(sig, rsaService, gameUsers, gameUsers.size()-1);
		
		//need to pass in the game hosts public key... the game host 
		com.waitEncryptedDeck(rsaService, sig, gameHostsPubKey);
		System.out.println("Got encrypted deck and encrypted again with my key!");
		
		//wait for cards
		myHand = com.waitEncryptedHand(sig, gameHostsPubKey);
		
		System.out.println("Got my cards!");
		
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(myHand);
				myHand = com.requestDecryptHand(myHand, tmpUser, sig);
			}
			
		}
		
		Hand hand = rsaService.decyrptHand(myHand);
		String card1 = Character.toString(hand.cards.get(0).cardType) + "-" + new String(hand.cards.get(0).suit);
		String card2 = Character.toString(hand.cards.get(1).cardType) + "-" + new String(hand.cards.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);
		
		//sit and block here until everyone has said gameover
		Thread.sleep(5000);
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
			gameUser = new User(br.readLine(), sig.getPublicKey());
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
		try {
			sig = new SigService();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	SwingGUI.showGUI();

            }
        });
	}


	public static User getGameUser() {
		return gameUser;
	}


	public static void setGameUsername(String username) {
		gameUser = new User(username, sig.getPublicKey());
	}
	
}
