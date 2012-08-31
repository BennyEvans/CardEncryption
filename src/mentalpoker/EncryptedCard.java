package mentalpoker;

import java.util.ArrayList;

/**
 * The Class EncryptedCard.
 */
public class EncryptedCard implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7822886716123630716L;
	
	/** The List of all users who have encrypted this card. */
	private ArrayList<User> usersEncrypted = new ArrayList<User>();
	
	/** The card data. */
	public byte[] cardData;
	
	
	/**
	 * Adds the user to the list of users who have encrypted the card.
	 *
	 * @param user the user
	 */
	public void addEncUser(User user){
		usersEncrypted.add(user);
	}
	
	/**
	 * Removes the a user from the list of user who have encrypted the card.
	 *
	 * @param user the user
	 */
	public void removeEncUser(User user){
		usersEncrypted.remove(user);
	}
	
	/**
	 * Gets the list of users who have encrypted this card.
	 *
	 * @return the user encrypted list
	 */
	public ArrayList<User> getUserEncryptedList(){
		//would be better to return an iterator or something
		return usersEncrypted;
	}
	
}
