package mentalpoker;

/**
 * The Class Card.
 */
public class Card implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5548065718369249594L;

	/** The card type. */
	public char cardType;

	/** The suit. */
	public String suit;

	/**
	 * Instantiates a new card.
	 * 
	 * @param cardType the card type
	 * @param suit the suit
	 */
	public Card(char cardType, String suit) {
		this.cardType = cardType;
		this.suit = suit.toString();
	}

}
