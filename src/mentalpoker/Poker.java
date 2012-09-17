package mentalpoker;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mentalpoker.SwingGUI.HostGameTask;
import mentalpoker.SwingGUI.SearchGamesTask;

import org.avis.client.InvalidSubscriptionException;

import com.sampullara.poker.Cards;
import com.sampullara.poker.Evaluate;

/**
 * The Class Poker.
 */
public class Poker {
	
	
	public static int SUIT = 1;
	public static int CARDTYPE = 0;
	
	public static ArrayList<String> royals;

	
	/** The game user. */
	private static User gameUser;
	
	private static SigService sig;
	/** The com. */
	ComService comServ;


	/**
	 * Instantiates a new game.
	 *
	 * @throws Exception the exception
	 */
	public Poker(String username) throws Exception {
		
		/**
		 * For game determination
		 */
		
		royals = new ArrayList<String>();
		
		
		royals.add("J");
		royals.add("Q");
		royals.add("K");
		royals.add("A");
		

		
		//com = new ComService(gameUser, "elvin://elvin.students.itee.uq.edu.au", sig);
		//return;

		setUsername(username);
		comServ = new ComService(gameUser, "elvin://elvin.students.itee.uq.edu.au", sig);
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
		if (isGameHost)
		{
			gameUsers = comServ.startNewGame(slots, hgt);
		}
		
		if (gameUsers == null){
			//could call startGame() here but exit is good enough for now
			comServ.shutdown();
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
			playGameAsHost(gameUsers, hgt);
		}
		
		return null;
	}
	
	public void playGameAsHost(ArrayList<User> gameUsers, HostGameTask hgt) throws IOException, InterruptedException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException{
		EncryptedDeck encDeck = null;
		RSAService rsaService = new RSAService();
		gameUser.setDecryptionKey(rsaService.getD());
		EncryptedHand myHand = null;
		EncryptedCommunityCards encComCards;
		EncryptedCommunityCards originalEncCommunityCards;

		//broadcast p and q
		comServ.broadcastPQ(rsaService.getP(), rsaService.getQ(), gameUsers.size());
		System.out.println(rsaService.getP().toString() + "\n" + rsaService.getQ().toString());
		//create and encrypt the deck
		encDeck = createDeck(rsaService);
		
	
		//for each user in gameUsers request to encrypt the deck
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();

			if (!tmpUser.getID().equals(gameUser.getID())){
				//sign the message
				sig.createSignature(encDeck);
				
				//request user to encrypt the deck
				encDeck = comServ.requestEncDeck(tmpUser, encDeck);
				
				//check the reply was from the user
				if (!sig.validateSignature(encDeck, tmpUser.getPublicKey())){
					comServ.callCheat(ComService.SIGNATURE_FAILED);
					comServ.shutdown();
					System.exit(2);
				}
			}
		}

		//String tmpStr = new String(encDeck.data.get(0).cardData);
		//System.out.println("First Card: " + tmpStr.toString());
		System.out.println("All Users have encrypted the deck!");
		
		//take requests to decrypt a hand
		comServ.decryptEncryptedHands(rsaService, gameUsers.size()-1);
		
		//subscribe to notifications for users have their decrypted hands
		comServ.subscribeUsersFinished(ComService.FINISHED_DEC_HAND);
		
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
				gameUser.setUsersOriginalHand(hand);
			} else {
				//send the hand to the user
				sig.createSignature(hand);
				comServ.sendEncryptedHand(tmpUser, hand);
			}
		}
		
		System.out.println("Every user has their cards now!");
		
		//copy and randomly shuffle the user list to distribute requests
		@SuppressWarnings("unchecked")
		ArrayList<User> tmpGameUsers = (ArrayList<User>) gameUsers.clone();
		Collections.shuffle(tmpGameUsers);
		for (Iterator<User> usr = tmpGameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(myHand);
				myHand = comServ.requestDecryptHand(myHand, tmpUser);
			}
			
		}
		
		Hand hand = rsaService.decyrptHand(myHand);
		gameUser.setUsersHand(hand);
		
		String card1 = Character.toString(hand.data.get(0).cardType) + "-" + new String(hand.data.get(0).suit);
		String card2 = Character.toString(hand.data.get(1).cardType) + "-" + new String(hand.data.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);
		
		
		
		//Provide the cards back to the user.
		hgt.publishDelegate("HAVECARDS1c2n90801280c498n12904c80912c490102984nc1 " + card1 + " " + card2);
		
		
		//choose 5 random cards as the community cards
		int count = 0;
		rnd = new Random();
		encComCards = new EncryptedCommunityCards();
		Integer tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));

		while (count < CommunityCards.NUM_CARDS){
			while (chosenCards.contains(tmpInt)){
				tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));
			}
			chosenCards.add(tmpInt);
			encComCards.data.add(encDeck.data.get(tmpInt));
			count++;
		}
		originalEncCommunityCards = encComCards;
		//sign the cards
		sig.createSignature(encComCards);
		
		//sit and block here until everyone has their plaintext cards
		if (!comServ.blockUntilUsersFinished()){
			System.out.println("Timeout!");
			comServ.shutdown();
			System.exit(0);
		}
		
		//now stop decrypting hands
		comServ.stopDecryptingHands();
		
		//subscribe to next lot of notifications
		comServ.decryptCommunityCards(rsaService, gameUsers.size()-1, true);
		comServ.subscribeUsersFinished(ComService.FINISHED_DEC_COM_CARDS);
		
		//send out community cards
		comServ.sendEncryptedComCards(encComCards);
		//System.out.println(new String(encComCards.data.get(0).cardData));
		
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(encComCards);
				encComCards = comServ.requestDecryptComCards(encComCards, tmpUser);
			} else {
				User duplicateUser = new User(gameUser.getUsername(), gameUser.getID(), gameUser.getPublicKey());
				encComCards.addUserToDecryptedTable(duplicateUser);
			}
			
		}
		
		CommunityCards comCards = rsaService.decyrptComCards(encComCards);
		System.out.println("Community Cards:");
		//Provide the community cards back to the user
		String cardsMessageSentBackToHost = "COMMUNITYCARDS_J1c20921098n08v290n8102v890n1203v";
		
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++){
			System.out.println(Character.toString(comCards.data.get(i).cardType) + "-" + new String(comCards.data.get(i).suit));
			cardsMessageSentBackToHost = cardsMessageSentBackToHost.concat(" " + Character.toString(comCards.data.get(i).cardType) + "-" + new String(comCards.data.get(i).suit));
			
		}
		
		if (!comServ.blockUntilUsersFinished()){
			System.out.println("Timeout!");
			comServ.shutdown();
			System.exit(0);
		}
		
		hgt.publishDelegate(cardsMessageSentBackToHost);
		
		//now stop decrypting community cards
		comServ.stopDecryptingComCards();
		
		//start listening for users hands
		comServ.listenUsersHands();
		
		//send out the community cards
		sig.createSignature(comCards);
		System.out.println("Sending Raw Community cards!");
		comServ.sendRawCommunityCards(comCards);
		System.out.println("Users all agreed with community cards!");
		
		//this is a protection mechanism for 2 player games
		// on > 2 player games users protect one another
		if (!comServ.getFirstUsersCommunityCards().compareCards(originalEncCommunityCards)){
			comServ.callCheat(ComService.DECRYPT_REQUEST_ABUSE);
		}
		
		//now broadcast you hand and wait for other hands
		comServ.broadcastMyHand(gameUser);
		
		ArrayList<User> userHands = comServ.blockUntilHaveUsersHands();
		for (int i = 0; i < userHands.size(); i++){
			User tmpUser = userHands.get(i);
			Hand tmpHand = tmpUser.getUsersHand();
			if (tmpUser.getID().equals(gameUser.getID())){
				System.out.println("ME");
			} else {
				System.out.println(tmpUser.getUsername().toString());
			}
			System.out.println("First Card: " + Character.toString(tmpHand.data.get(0).cardType) + "-" + tmpHand.data.get(0).suit.toString());
			System.out.println("Second Card: " + Character.toString(tmpHand.data.get(1).cardType) + "-" + tmpHand.data.get(1).suit.toString());
		}
		
		//now determine the winner and check the winners cards
		User winner = determineWinner(comCards, userHands);
		
		if (!checkWinnersHand(userHands, winner, rsaService)){
			comServ.callCheat(ComService.HAND_VERIFICATION_FAILED);
		}

		System.out.println("Winner was: " + new String(winner.getUsername()));
		hgt.publishDelegate("WINNER189290128490182498124kjsafdl " + winner.getUsername());
		
		Thread.sleep(2500);
		comServ.shutdown();
		return;
	}
	
	
	
	public void playGameAsPlayer(ArrayList<User> gameUsers, SearchGamesTask jgt) throws InvalidSubscriptionException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InterruptedException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		RSAService rsaService = comServ.waitPQ();
		gameUser.setDecryptionKey(rsaService.getD());
		EncryptedHand myHand;
		EncryptedCommunityCards encComCards;
		
		PublicKey gameHostsPubKey = gameUsers.get(0).getPublicKey();

		//take requests to decrypt a hand
		comServ.decryptEncryptedHands(rsaService, gameUsers.size()-1);
		
		//need to pass in the game hosts public key... the game host 
		comServ.waitEncryptedDeck(rsaService, gameHostsPubKey);
		System.out.println("Got encrypted deck and encrypted again with my key.");
		
		//wait for cards
		myHand = comServ.waitEncryptedHand();
		gameUser.setUsersOriginalHand(myHand);
		
		System.out.println("Got my cards!");
		
		//copy and randomly shuffle the user list to distribute requests
		@SuppressWarnings("unchecked")
		ArrayList<User> tmpGameUsers = (ArrayList<User>) gameUsers.clone();
		Collections.shuffle(tmpGameUsers);
		for (Iterator<User> usr = tmpGameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(myHand);
				myHand = comServ.requestDecryptHand(myHand, tmpUser);
			}
			
		}
		
		Hand hand = rsaService.decyrptHand(myHand);
		gameUser.setUsersHand(hand);
		
		String card1 = Character.toString(hand.data.get(0).cardType) + "-" + new String(hand.data.get(0).suit);
		String card2 = Character.toString(hand.data.get(1).cardType) + "-" + new String(hand.data.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);
		
		jgt.waitForInstructionsBuffer.put(card1);
		jgt.waitForInstructionsBuffer.put(card2);
		
		//subscribe to next lot of notification
		comServ.decryptCommunityCards(rsaService, gameUsers.size()-1, false);
		
		//notify have hand and wait for community cards
		encComCards = comServ.listenForCommunityCards();
		
		//now stop decrypting hands
		comServ.stopDecryptingHands();
		
		//start decrypting the community cards
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();){
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())){
				sig.createSignature(encComCards);
				encComCards = comServ.requestDecryptComCards(encComCards, tmpUser);
			} else {
				User duplicateUser = new User(gameUser.getUsername(), gameUser.getID(), gameUser.getPublicKey());
				encComCards.addUserToDecryptedTable(duplicateUser);
			}
			
		}
		
		CommunityCards comCards = rsaService.decyrptComCards(encComCards);
		System.out.println("Community Cards:");
		String communityCardsToBeSentToJoiner = "";
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++){
			communityCardsToBeSentToJoiner = communityCardsToBeSentToJoiner.concat(Character.toString(comCards.data.get(i).cardType) + "-" + new String(comCards.data.get(i).suit) + " ");
			System.out.println(Character.toString(comCards.data.get(i).cardType) + "-" + new String(comCards.data.get(i).suit));
		}

		//start listening for users hands
		comServ.listenUsersHands();
		
		//verify community cards
		System.out.println("Verifying community cards!");
		comServ.verifyCommunityCards(comCards);
		System.out.println("Community cards verified!");
		
		//now stop decrypting community cards
		comServ.stopDecryptingComCards();
		
		jgt.waitForInstructionsBuffer.put(communityCardsToBeSentToJoiner);
		
		//now broadcast you hand and wait for other hands
		comServ.broadcastMyHand(gameUser);
		ArrayList<User> userHands = comServ.blockUntilHaveUsersHands();
		for (int i = 0; i < userHands.size(); i++){
			User tmpUser = userHands.get(i);
			Hand tmpHand = tmpUser.getUsersHand();
			if (tmpUser.getID().equals(gameUser.getID())){
				System.out.println("ME");
			} else {
				System.out.println(tmpUser.getUsername().toString());
			}
			System.out.println("First Card: " + Character.toString(tmpHand.data.get(0).cardType) + "-" + tmpHand.data.get(0).suit.toString());
			System.out.println("Second Card: " + Character.toString(tmpHand.data.get(1).cardType) + "-" + tmpHand.data.get(1).suit.toString());
		}
		
		//now determine the winner and check the winners cards
		User winner = determineWinner(comCards, userHands);
		
		if (!checkWinnersHand(userHands, winner, rsaService)){
			comServ.callCheat(ComService.HAND_VERIFICATION_FAILED);
		}

		System.out.println("Winner was: " + new String(winner.getUsername()));
		jgt.waitForInstructionsBuffer.put(winner.getUsername());
		
		comServ.shutdown();
		return;
	}
	
	
	/**
	 * Creates the deck.
	 *
	 * @param rsaServ the RSAService
	 * @return the encrypted deck
	 * @throws InvalidKeyException the invalid key exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 */
	private EncryptedDeck createDeck(RSAService rsaServ) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		Deck deck = new Deck();
		EncryptedDeck encDeck = rsaServ.encryptDeck(deck);
		return encDeck;
	}

	
	/**
	 * Sets the username.
	 */
	public void setUsername(String setUsernameString) throws IOException
	{
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


	public User getGameUser() {
		return gameUser;
	}
	
	public String getGameUserUsername() {
		return gameUser.getUsername();
	}


	public static void setGameUsername(String username) {
		gameUser = new User(username, sig.getPublicKey());
	}
	
	/**
	 * Determine if the given user's hand will win.
	 * Takes all the game users, then analyses their hands
	 * in order to figure out who has won.
	 * @param cc
	 * @param gU
	 * @return
	 */
	private User determineWinner(CommunityCards cc, ArrayList<User> allGameUsers)
	{
		

		
		/**
		 * Royal flush (ace-high straight of one suit)
		 * Straight flush (straight of entirely one suit)
		 * Four of a kind (four cards of the same kind)
		 * Full house (3 of a kind, and a pair)
		 * Flush (five cards of the same suit)
		 * Straight (five cards of sequential rank)
		 * Three-of-a-kind (3 cards of same rank)
		 * Two pair (2 pairs of 2 cards of th same rank)
		 * One pair (two cards of the same rank)
		 * High card (if there are no pairs, the person with the highest card wins)
		 */
		
		//Put all the cards and their suits in an array for easy access.
		ArrayList<ArrayList<String>> commCardsArray = new ArrayList<ArrayList<String>>();
			for (int i = 0; i < CommunityCards.NUM_CARDS; i++){
			ArrayList<String> card = new ArrayList<String>();
			card.add(Character.toString(cc.data.get(i).cardType));
			card.add(cc.data.get(i).suit);
			commCardsArray.add(card);	
		}
			
		Cards board = new Cards(5);
		for (ArrayList<String> ccard : commCardsArray)
		{
			board.add(new com.sampullara.poker.Card(com.sampullara.poker.Card.Rank.parse(ccard.get(Poker.CARDTYPE)),
					com.sampullara.poker.Card.Suit.parse(ccard.get(Poker.SUIT))));
		}
			
		Cards[] allUsersCards = new Cards[allGameUsers.size()];
		
		int usersCardsIndex = 0;	
		for (User u:allGameUsers)
		{
			/**
			 * In this section, we convert the cards into arrays for easy iteration
			 */
			String userCard1 = Character.toString(u.getUsersHand().data.get(0).cardType);
			String userCard2 = Character.toString(u.getUsersHand().data.get(1).cardType);
			
			//Card 1 of user's hand
			ArrayList<String> card1 = new ArrayList<String>();
			card1.add(userCard1);
			card1.add(u.getUsersHand().data.get(0).suit);
			
			//Card 2 of user's hand
			ArrayList<String> card2 = new ArrayList<String>();
			card2.add(userCard2);
			card2.add(u.getUsersHand().data.get(1).suit);
			
			ArrayList<ArrayList<String>> userCards = new ArrayList<ArrayList<String>>();
			userCards.add(card1);
			userCards.add(card2);
			
			/**
			 * Using the pokerengine made available by Sam Pullara
			 */
			
			Cards userCards1 = new Cards(2);
			//Adding the first card
			userCards1.add(new com.sampullara.poker.Card(com.sampullara.poker.Card.Rank.parse(card1.get(Poker.CARDTYPE)),
					com.sampullara.poker.Card.Suit.parse(card1.get(Poker.SUIT))));
			//Second card
			userCards1.add(new com.sampullara.poker.Card(com.sampullara.poker.Card.Rank.parse(card2.get(Poker.CARDTYPE)),
					com.sampullara.poker.Card.Suit.parse(card2.get(Poker.SUIT))));
			
			allUsersCards[usersCardsIndex++] = userCards1;
			
		}
		
		double[] odds = Evaluate.evaluateWinningOdds(allUsersCards, board, new Cards());
		
		Map<Object,User> userRankings = new HashMap<Object,User>();	
		for (int cardIndex = 0; cardIndex < allGameUsers.size(); cardIndex++) {
			userRankings.put(new Double(odds[cardIndex]), allGameUsers.get(cardIndex));
		}
		
		return (User) userRankings.values().toArray()[0];
		

		
	}
	
	private boolean checkWinnersHand(ArrayList<User> userHands, User winner, RSAService rsaServ) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Hand uHand = winner.getUsersHand();
		Hand dHand;
		EncryptedHand eHand = winner.getUsersOriginalHand();
		BigInteger p = rsaServ.getP();
		BigInteger q = rsaServ.getQ();
		for (User usr: userHands){
			if (usr.getID().equals(gameUser.getID())){
				continue;
			}
			RSAService tmpRSA = new RSAService(p, q, usr.getDecryptionKey());
			eHand = tmpRSA.decyrptEncHand(eHand);
		}
		dHand = rsaServ.decyrptHand(eHand);
		
		//now compare the hands
		for(int i = 0; i < Hand.NUM_CARDS; i++){
			if(dHand.data.get(i).checkIfJibberish()){
				//cards are not valid
				return false;
			}
			if (uHand.data.get(i).cardType != dHand.data.get(i).cardType){
				return false;
			}
			if (!uHand.data.get(i).suit.equals(dHand.data.get(i).suit)){
				return false;
			}
		}
		System.out.println("Winners hand verified as:");
		for(int i = 0; i < Hand.NUM_CARDS; i++){
			System.out.println(Character.toString(dHand.data.get(i).cardType) + "-" + new String(dHand.data.get(i).suit));
		}
		return true;
	}
	
}
