package mentalpoker;

import javax.crypto.SealedObject;

/**
 * The Class EncryptedDeck.
 */
public class EncryptedDeck implements java.io.Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7737144048645072557L;

	
	/** The encrypted cards. */
	public SealedObject[] encCards = new SealedObject[Deck.NUM_CARDS];
	
}
