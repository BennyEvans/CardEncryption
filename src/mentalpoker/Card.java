package mentalpoker;

/**
 * The Class Card.
 */
public class Card implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5548065718369249594L;

	/** The card types. */
	public static char[] CARD_TYPES = { '2', '3', '4', '5', '6', '7', '8', '9',
			'T', 'J', 'Q', 'K', 'A' };

	/** The suits. */
	public static String[] SUITS = { "spades", "hearts", "diamonds", "clubs" };

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

	/**
	 * Check if cards have garbage data.
	 *
	 * @return true, if garbage
	 */
	public boolean checkIfJibberish() {
		boolean suitCorrect = false;
		boolean ctCorrect = false;
		for (String s: SUITS){
			if (s.equals(suit)){
				suitCorrect = true;
				break;
			}
		}
		for (char c: CARD_TYPES){
			if (c == cardType){
				ctCorrect = true;
				break;
			}
		}
		if (suitCorrect && ctCorrect) {
			return false;
		}
		return true;
	}

}
