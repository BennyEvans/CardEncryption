package mentalpoker;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The Class EncryptedDeck.
 */
public class EncryptedDeck implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7737144048645072557L;

	/** The encrypted cards. */
	public ArrayList<EncryptedCard> encCards = new ArrayList<EncryptedCard>();
	
	public ArrayList<User> usersEncrypted = new ArrayList<User>();
	
	public void shuffleDeck(){
		 Collections.shuffle(encCards);
	}
	
	// Called at the end of the game
	public boolean validateDeck(BigInteger p, BigInteger q){
		//make sure each user has a decryption key
		//Iterator<User> itr = usersEncrypted.iterator();
		
		return false;
	}
	
	
}
