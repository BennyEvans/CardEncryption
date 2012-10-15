package mentalpoker;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The Class Deck.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class Deck {

	/** The deck. */
	private ArrayList<Card> cardDeck = new ArrayList<Card>();

	/** The number of cards in a deck. */
	public static int NUM_CARDS = 52;
	
	/**
	 * Instantiates a new deck.
	 */
	public Deck() {
		// generate the 52 cards and shuffle them
		for (int i = 0; i < Card.SUITS.length; i++) {
			for (int j = 0; j < Card.CARD_TYPES.length; j++) {
				Card card = new Card(Card.CARD_TYPES[j], Card.SUITS[i]);
				cardDeck.add(card);
			}
		}
		shuffleDeck();
	}

	/**
	 * Shuffle the deck.
	 */
	private void shuffleDeck() {
		Collections.shuffle(cardDeck);
	}

	/**
	 * Gets the card at index, index.
	 *
	 * @param index the index
	 * @return the card at index
	 */
	public Card getCardAtIndex(int index) {
		return cardDeck.get(index);
	}

}
