package mentalpoker;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * The Class Deck.
 */
public class Deck {
	
	/** The deck. */
	private ArrayList<Card> cardDeck = new ArrayList<Card>();
	
	/** The rsa service. */
	private RSAService rsaService;
	
	/** The current user. */
	private User user;
	
	/** The number of cards in a deck. */
	public static final int NUM_CARDS = 52;
	
	//T == 10
	/** The card types. */
	private static char [] cardTypes = {'2','3','4','5','6','7','8','9','T','J','Q','K','A'};
	
	/** The suits. */
	private static String[] suits = {"spades", "hearts", "diamonds", "clubs"};
	
	/**
	 * Instantiates a new deck.
	 *
	 * @param rsaService the rsa service
	 */
	public Deck(RSAService rsaService, User user){
		this.rsaService = rsaService;
		//generate the 52 cards and shuffle them
		for (int i = 0; i < suits.length; i++){
			for (int j = 0; j < cardTypes.length; j++){
				Card card = new Card(cardTypes[j], suits[i]);
				cardDeck.add(card);
			}
		}
		shuffleDeck();
	}
	
	/**
	 * Shuffle the deck.
	 */
	private void shuffleDeck(){
		 Collections.shuffle(cardDeck);
	}
	
	/**
	 * Gets an encrypted card at index, index.
	 *
	 * @param index the index
	 * @return the encrypted card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws BadPaddingException 
	 */
	public EncryptedCard getEncryptedCard(int index) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		EncryptedCard tmp = this.rsaService.encryptCard(cardDeck.get(index));
		tmp.addEncUser(user);
		return tmp;
	}

}
