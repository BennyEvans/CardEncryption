package mentalpoker;

import java.util.Collections;

/**
 * The Class EncryptedDeck.
 */
public class EncryptedDeck extends Passable<EncryptedCard> implements
		java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7737144048645072557L;

	/**
	 * Shuffle deck.
	 */
	public void shuffleDeck() {
		Collections.shuffle(data);
	}

}
