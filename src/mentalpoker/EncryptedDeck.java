package mentalpoker;

import java.io.Serializable;

/**
 * The Class EncryptedDeck.
 */
public class EncryptedDeck implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7737144048645072557L;

	/** The encrypted cards. */
	public EncryptedCard[] encCards = new EncryptedCard[Deck.NUM_CARDS];
	
}
