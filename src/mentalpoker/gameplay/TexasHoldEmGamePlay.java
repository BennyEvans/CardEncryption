package mentalpoker.gameplay;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import mentalpoker.ComService;
import mentalpoker.CommunityCards;
import mentalpoker.Deck;
import mentalpoker.EncryptedCommunityCards;
import mentalpoker.EncryptedDeck;
import mentalpoker.EncryptedHand;
import mentalpoker.Hand;
import mentalpoker.RSAService;
import mentalpoker.SigService;
import mentalpoker.SwingGUI.HostGameTask;
import mentalpoker.SwingGUI.SearchGamesTask;
import mentalpoker.gameplay.handevaluator.Cards;
import mentalpoker.gameplay.handevaluator.Evaluate;
import mentalpoker.User;


/**
 * The Class TexasHoldEmGamePlay.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class TexasHoldEmGamePlay implements GamePlay {

	/*
	 * Texas hold em' Gameplay
	 * 
	 * Rules: http://en.wikipedia.org/wiki/Texas_hold_'em
	 * 
	 * 
	 * Hand Ranks:
	 * 
	 * Royal flush (ace-high straight of one suit) Straight flush (straight of
	 * entirely one suit) Four of a kind (four cards of the same kind) Full
	 * house (3 of a kind, and a pair) Flush (five cards of the same suit)
	 * Straight (five cards of sequential rank) Three-of-a-kind (3 cards of same
	 * rank) Two pair (2 pairs of 2 cards of the same rank) One pair (two cards
	 * of the same rank) High card (if there are no pairs, the person with the
	 * highest card wins)
	 */
	

	/** The Constant SUIT. */
	private static final int SUIT = 1;
	
	/** The Constant CARDTYPE. */
	private static final int CARDTYPE = 0;

	
	/* (non-Javadoc)
	 * @see mentalpoker.gameplay.GamePlay#playerGamePlay(mentalpoker.RSAService, mentalpoker.ComService, mentalpoker.User, java.util.ArrayList, mentalpoker.SigService, mentalpoker.SwingGUI.SearchGamesTask)
	 */
	@Override
	public void playerGamePlay(RSAService rsaService, ComService comServ,
			User gameUser, ArrayList<User> gameUsers, SigService sig,
			SearchGamesTask jgt) throws InvalidKeyException,
			InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchPaddingException, NoSuchProviderException,
			IllegalBlockSizeException, BadPaddingException, IOException,
			InterruptedException {

		EncryptedHand myHand;
		EncryptedCommunityCards encComCards;

		PublicKey gameHostsPubKey = gameUsers.get(0).getPublicKey();

		// take requests to decrypt a hand
		comServ.decryptEncryptedHands(rsaService, gameUsers.size() - 1);

		// need to pass in the game hosts public key... the game host
		comServ.waitEncryptedDeck(rsaService, gameHostsPubKey);
		System.out
				.println("Got encrypted deck and encrypted again with my key.");

		// wait for cards
		myHand = comServ.waitEncryptedHand();
		gameUser.setUsersOriginalHand(myHand);

		System.out.println("Got my cards!");
		
		//Add additional encryption on top of requests.... used to prevent replay attacks
		RSAService overEncryption = new RSAService(rsaService.getP(), rsaService.getQ());
		myHand = overEncryption.encryptEncHand(myHand);

		// copy and randomly shuffle the user list to distribute requests
		@SuppressWarnings("unchecked")
		ArrayList<User> tmpGameUsers = (ArrayList<User>) gameUsers.clone();
		Collections.shuffle(tmpGameUsers);
		for (Iterator<User> usr = tmpGameUsers.iterator(); usr.hasNext();) {
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())) {
				sig.createSignature(myHand);
				myHand = comServ.requestDecryptHand(myHand, tmpUser);
			}

		}

		myHand = rsaService.decyrptEncHand(myHand);
		Hand hand = overEncryption.decyrptHand(myHand);
		gameUser.setUsersHand(hand);

		String card1 = Character.toString(hand.data.get(0).cardType) + "-"
				+ new String(hand.data.get(0).suit);
		String card2 = Character.toString(hand.data.get(1).cardType) + "-"
				+ new String(hand.data.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);

		jgt.waitForInstructionsBuffer.put(card1);
		jgt.waitForInstructionsBuffer.put(card2);

		// subscribe to next lot of notification
		comServ.decryptCommunityCards(rsaService, gameUsers.size() - 1, false);

		// notify have hand and wait for community cards
		encComCards = comServ.listenForCommunityCards();

		// now stop decrypting hands
		comServ.stopDecryptingHands();

		// start decrypting the community cards
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();) {
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())) {
				sig.createSignature(encComCards);
				encComCards = comServ.requestDecryptComCards(encComCards,
						tmpUser);
			} else {
				User duplicateUser = new User(gameUser.getUsername(),
						gameUser.getID(), gameUser.getPublicKey());
				encComCards.addUserToDecryptedTable(duplicateUser);
			}

		}

		CommunityCards comCards = rsaService.decyrptComCards(encComCards);
		System.out.println("Community Cards:");
		String communityCardsToBeSentToJoiner = "";
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			communityCardsToBeSentToJoiner = communityCardsToBeSentToJoiner
					.concat(Character.toString(comCards.data.get(i).cardType)
							+ "-" + new String(comCards.data.get(i).suit) + " ");
			System.out
					.println(Character.toString(comCards.data.get(i).cardType)
							+ "-" + new String(comCards.data.get(i).suit));
		}

		// start listening for users hands
		comServ.listenUsersHands();

		// verify community cards
		System.out.println("Verifying community cards!");
		comServ.verifyCommunityCards(comCards);
		System.out.println("Community cards verified!");

		// now stop decrypting community cards
		comServ.stopDecryptingComCards();

		jgt.waitForInstructionsBuffer.put(communityCardsToBeSentToJoiner);

		// now broadcast you hand and wait for other hands
		comServ.broadcastMyHand(gameUser);
		ArrayList<User> userHands = comServ.blockUntilHaveUsersHands();
		
		System.out.println("\nUsers Cards:");
		
		for (int i = 0; i < userHands.size(); i++) {
			User tmpUser = userHands.get(i);
			Hand tmpHand = tmpUser.getUsersHand();
			if (tmpUser.getID().equals(gameUser.getID())) {
				System.out.println("Username: Me");
			} else {
				System.out.println("Username: " + tmpUser.getUsername().toString());
			}
			System.out.println("First Card: "
					+ Character.toString(tmpHand.data.get(0).cardType) + "-"
					+ tmpHand.data.get(0).suit.toString());
			System.out.println("Second Card: "
					+ Character.toString(tmpHand.data.get(1).cardType) + "-"
					+ tmpHand.data.get(1).suit.toString());
		}

		// now determine the winner and check the winners cards
		User winner = determineWinner(comCards, userHands);

		if (!rsaService.checkWinnersHand(gameUser, userHands, winner,
				rsaService)) {
			comServ.callCheat(ComService.HAND_VERIFICATION_FAILED);
		}

		System.out.println("Winner was: " + new String(winner.getUsername()));
		jgt.waitForInstructionsBuffer.put(winner.getUsername());

	}

	
	/* (non-Javadoc)
	 * @see mentalpoker.gameplay.GamePlay#hostGamePlay(mentalpoker.RSAService, mentalpoker.ComService, mentalpoker.User, java.util.ArrayList, mentalpoker.SigService, mentalpoker.EncryptedDeck, mentalpoker.SwingGUI.HostGameTask)
	 */
	@Override
	public void hostGamePlay(RSAService rsaService, ComService comServ,
			User gameUser, ArrayList<User> gameUsers, SigService sig,
			EncryptedDeck encDeck, HostGameTask hgt)
			throws InvalidKeyException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, IOException,
			InterruptedException, InvalidKeySpecException,
			NoSuchPaddingException, NoSuchProviderException {

		EncryptedHand myHand = null;
		EncryptedCommunityCards encComCards;
		EncryptedCommunityCards originalEncCommunityCards;

		// for each user in gameUsers request to encrypt the deck
		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();) {
			User tmpUser = usr.next();

			if (!tmpUser.getID().equals(gameUser.getID())) {
				// sign the message
				sig.createSignature(encDeck);

				// request user to encrypt the deck
				encDeck = comServ.requestEncDeck(tmpUser, encDeck);

				// check the reply was from the user
				if (!sig.validateSignature(encDeck, tmpUser.getPublicKey())) {
					comServ.callCheat(ComService.SIGNATURE_FAILED);
					comServ.shutdown();
					System.exit(2);
				}
			}
		}

		// String tmpStr = new String(encDeck.data.get(0).cardData);
		// System.out.println("First Card: " + tmpStr.toString());
		System.out.println("All Users have encrypted the deck!");

		// take requests to decrypt a hand
		comServ.decryptEncryptedHands(rsaService, gameUsers.size() - 1);

		// subscribe to notifications for users have their decrypted hands
		comServ.subscribeUsersFinished(ComService.FINISHED_DEC_HAND);

		// choose random cards for each user
		ArrayList<Integer> chosenCards = new ArrayList<Integer>();
		Random rnd;

		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();) {
			EncryptedHand hand = new EncryptedHand();
			User tmpUser = usr.next();
			int count = 0;
			rnd = new Random();

			Integer tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));

			while (count < Hand.NUM_CARDS) {
				while (chosenCards.contains(tmpInt)) {
					tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));
				}
				chosenCards.add(tmpInt);
				hand.data.add(encDeck.data.get(tmpInt));
				count++;
			}
			if (tmpUser.getID().equals(gameUser.getID())) {
				System.out.println("Got my cards!");
				// these are my cards
				myHand = hand;
				gameUser.setUsersOriginalHand(hand);
			} else {
				// send the hand to the user
				sig.createSignature(hand);
				comServ.sendEncryptedHand(tmpUser, hand);
			}
		}

		System.out.println("Every user has their cards now!");
		
		//Add additional encryption on top of requests.... used to prevent replay attacks
		RSAService overEncryption = new RSAService(rsaService.getP(), rsaService.getQ());
		myHand = overEncryption.encryptEncHand(myHand);

		// copy and randomly shuffle the user list to distribute requests
		@SuppressWarnings("unchecked")
		ArrayList<User> tmpGameUsers = (ArrayList<User>) gameUsers.clone();
		Collections.shuffle(tmpGameUsers);
		for (Iterator<User> usr = tmpGameUsers.iterator(); usr.hasNext();) {
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())) {
				sig.createSignature(myHand);
				myHand = comServ.requestDecryptHand(myHand, tmpUser);
			}

		}
		myHand = rsaService.decyrptEncHand(myHand);
		Hand hand = overEncryption.decyrptHand(myHand);
		gameUser.setUsersHand(hand);

		String card1 = Character.toString(hand.data.get(0).cardType) + "-"
				+ new String(hand.data.get(0).suit);
		String card2 = Character.toString(hand.data.get(1).cardType) + "-"
				+ new String(hand.data.get(1).suit);
		System.out.println("My Cards: " + card1 + " " + card2);

		// Provide the cards back to the user.
		hgt.publishDelegate("HAVECARDS1c2n90801280c498n12904c80912c490102984nc1 "
				+ card1 + " " + card2);

		// choose 5 random cards as the community cards
		int count = 0;
		rnd = new Random();
		encComCards = new EncryptedCommunityCards();
		Integer tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));

		while (count < CommunityCards.NUM_CARDS) {
			while (chosenCards.contains(tmpInt)) {
				tmpInt = new Integer(rnd.nextInt(Deck.NUM_CARDS - 1));
			}
			chosenCards.add(tmpInt);
			encComCards.data.add(encDeck.data.get(tmpInt));
			count++;
		}
		originalEncCommunityCards = encComCards;
		// sign the cards
		sig.createSignature(encComCards);

		// sit and block here until everyone has their plaintext cards
		if (!comServ.blockUntilUsersFinished()) {
			System.out.println("Timeout!");
			comServ.shutdown();
			System.exit(0);
		}

		// now stop decrypting hands
		comServ.stopDecryptingHands();

		// subscribe to next lot of notifications
		comServ.decryptCommunityCards(rsaService, gameUsers.size() - 1, true);
		comServ.subscribeUsersFinished(ComService.FINISHED_DEC_COM_CARDS);

		// send out community cards
		comServ.sendEncryptedComCards(encComCards);
		// System.out.println(new String(encComCards.data.get(0).cardData));

		for (Iterator<User> usr = gameUsers.iterator(); usr.hasNext();) {
			User tmpUser = usr.next();
			if (!tmpUser.getID().equals(gameUser.getID())) {
				sig.createSignature(encComCards);
				encComCards = comServ.requestDecryptComCards(encComCards,
						tmpUser);
			} else {
				User duplicateUser = new User(gameUser.getUsername(),
						gameUser.getID(), gameUser.getPublicKey());
				encComCards.addUserToDecryptedTable(duplicateUser);
			}

		}

		CommunityCards comCards = rsaService.decyrptComCards(encComCards);
		System.out.println("Community Cards:");
		// Provide the community cards back to the user
		String cardsMessageSentBackToHost = "COMMUNITYCARDS_J1c20921098n08v290n8102v890n1203v";

		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			System.out
					.println(Character.toString(comCards.data.get(i).cardType)
							+ "-" + new String(comCards.data.get(i).suit));
			cardsMessageSentBackToHost = cardsMessageSentBackToHost.concat(" "
					+ Character.toString(comCards.data.get(i).cardType) + "-"
					+ new String(comCards.data.get(i).suit));

		}

		if (!comServ.blockUntilUsersFinished()) {
			System.out.println("Timeout!");
			comServ.shutdown();
			System.exit(0);
		}

		hgt.publishDelegate(cardsMessageSentBackToHost);

		// now stop decrypting community cards
		comServ.stopDecryptingComCards();

		// start listening for users hands
		comServ.listenUsersHands();

		// send out the community cards
		sig.createSignature(comCards);
		System.out.println("Sending Raw Community cards!");
		comServ.sendRawCommunityCards(comCards);
		System.out.println("Users all agreed with community cards!");

		// this is a protection mechanism for 2 player games
		// on > 2 player games users protect one another
		if (!comServ.getFirstUsersCommunityCards().compareCards(
				originalEncCommunityCards)) {
			comServ.callCheat(ComService.DECRYPT_REQUEST_ABUSE);
		}

		// now broadcast you hand and wait for other hands
		comServ.broadcastMyHand(gameUser);

		ArrayList<User> userHands = comServ.blockUntilHaveUsersHands();
		
		System.out.println("\nUsers Cards:");
		
		for (int i = 0; i < userHands.size(); i++) {
			User tmpUser = userHands.get(i);
			Hand tmpHand = tmpUser.getUsersHand();
			if (tmpUser.getID().equals(gameUser.getID())) {
				System.out.println("Username: Me");
			} else {
				System.out.println("Username: " + tmpUser.getUsername().toString());
			}
			System.out.println("First Card: "
					+ Character.toString(tmpHand.data.get(0).cardType) + "-"
					+ tmpHand.data.get(0).suit.toString());
			System.out.println("Second Card: "
					+ Character.toString(tmpHand.data.get(1).cardType) + "-"
					+ tmpHand.data.get(1).suit.toString());
		}

		// now determine the winner and check the winners cards
		User winner = determineWinner(comCards, userHands);

		if (!rsaService.checkWinnersHand(gameUser, userHands, winner,
				rsaService)) {
			comServ.callCheat(ComService.HAND_VERIFICATION_FAILED);
		}

		System.out.println("Winner was: " + new String(winner.getUsername()));
		hgt.publishDelegate("WINNER189290128490182498124kjsafdl "
				+ winner.getUsername());

		Thread.sleep(2500);
		comServ.shutdown();
		return;

	}

	
	/* (non-Javadoc)
	 * @see mentalpoker.gameplay.GamePlay#determineWinner(mentalpoker.CommunityCards, java.util.ArrayList)
	 */
	@Override
	public User determineWinner(CommunityCards cc, ArrayList<User> allGameUsers) {

		// Put all the cards and their suits in an array for easy access.
		ArrayList<ArrayList<String>> commCardsArray = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			ArrayList<String> card = new ArrayList<String>();
			card.add(Character.toString(cc.data.get(i).cardType));
			card.add(cc.data.get(i).suit);
			commCardsArray.add(card);
		}

		Cards board = new Cards(CommunityCards.NUM_CARDS);
		for (ArrayList<String> ccard : commCardsArray) {
			board.add(new mentalpoker.gameplay.handevaluator.Card(
					mentalpoker.gameplay.handevaluator.Card.Rank.parse(ccard
							.get(CARDTYPE)),
					mentalpoker.gameplay.handevaluator.Card.Suit.parse(ccard
							.get(SUIT))));
		}

		Cards[] allUsersCards = new Cards[allGameUsers.size()];

		int usersCardsIndex = 0;
		for (User u : allGameUsers) {
			/**
			 * In this section, we convert the cards into arrays for easy
			 * iteration
			 */
			String userCard1 = Character
					.toString(u.getUsersHand().data.get(0).cardType);
			String userCard2 = Character
					.toString(u.getUsersHand().data.get(1).cardType);

			// Card 1 of user's hand
			ArrayList<String> card1 = new ArrayList<String>();
			card1.add(userCard1);
			card1.add(u.getUsersHand().data.get(0).suit);

			// Card 2 of user's hand
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
			// Adding the first card
			userCards1.add(new mentalpoker.gameplay.handevaluator.Card(
					mentalpoker.gameplay.handevaluator.Card.Rank.parse(card1
							.get(CARDTYPE)),
					mentalpoker.gameplay.handevaluator.Card.Suit.parse(card1
							.get(SUIT))));
			// Second card
			userCards1.add(new mentalpoker.gameplay.handevaluator.Card(
					mentalpoker.gameplay.handevaluator.Card.Rank.parse(card2
							.get(CARDTYPE)),
					mentalpoker.gameplay.handevaluator.Card.Suit.parse(card2
							.get(SUIT))));

			allUsersCards[usersCardsIndex++] = userCards1;

		}

		double[] odds = Evaluate.evaluateWinningOdds(allUsersCards, board,
				new Cards());

		for (int cardIndex = 0; cardIndex < allGameUsers.size(); cardIndex++) {
			if (odds[cardIndex] == 1.0d) {
				return allGameUsers.get(cardIndex);
			}
		}

		// something went wrong
		return null;
	}

	
	/* (non-Javadoc)
	 * @see mentalpoker.gameplay.GamePlay#init()
	 */
	@Override
	public void init() {
		Deck.NUM_CARDS = 52;
		CommunityCards.NUM_CARDS = 5;
		Hand.NUM_CARDS = 2;
	}

}
