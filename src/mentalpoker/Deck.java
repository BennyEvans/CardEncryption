package mentalpoker;

import java.util.ArrayList;
import java.util.Collections;


/**
 * The Class Deck.
 */
public class Deck {
	
	/** The deck. */
	private ArrayList<Card> cardDeck = new ArrayList<Card>();
	
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
	public Deck(){
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
	
	
	public Card getCardAtIndex(int index){
		return cardDeck.get(index);
	}
	
}
