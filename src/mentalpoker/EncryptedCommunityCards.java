package mentalpoker;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * The Class EncryptedCommunityCards.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class EncryptedCommunityCards extends Passable<EncryptedCard> implements
		java.io.Serializable {

	/** The table of users who have decrypted the cards. */
	private ArrayList<User> usersDecryptedTable = new ArrayList<User>();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7536396101508111322L;

	
	/**
	 * Checks if a user had decrypted the cards.
	 *
	 * @param userID the user id
	 * @return true, if successful
	 */
	public boolean hasUserDecrypted(String userID) {
		for (int i = 0; i < usersDecryptedTable.size(); i++) {
			if (usersDecryptedTable.get(i).getID().equals(userID)) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Adds the user to decrypted table.
	 *
	 * @param usr the user
	 */
	public void addUserToDecryptedTable(User usr) {
		usersDecryptedTable.add(usr);
	}

	
	/**
	 * Compares these community cards with another.
	 *
	 * @param comCards the community cards
	 * @return true, if the same
	 */
	public boolean compareCards(EncryptedCommunityCards comCards) {
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			EncryptedCard card1 = comCards.data.get(i);
			EncryptedCard card2 = this.data.get(i);
			if (!Arrays.equals(card1.cardData, card2.cardData)) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * Compare the community cards decrypted user list.
	 *
	 * @param userList the user list
	 * @return true, if the same
	 */
	public boolean compareUserList(ArrayList<User> userList) {
		for (int i = 0; i < usersDecryptedTable.size(); i++) {
			if (!usersDecryptedTable.get(i).getID()
					.equals(userList.get(i).getID())) {
				return false;
			}
		}
		return true;
	}

	
	/**
	 * Number of users who have decrypted.
	 *
	 * @return the number of users
	 */
	public int numUsersDecrypted() {
		return usersDecryptedTable.size();
	}

	
	/**
	 * Gets the decrypted users list.
	 *
	 * @return the user list
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<User> getUserList() {
		return (ArrayList<User>) usersDecryptedTable.clone();
	}

	
	/**
	 * Sets the user list.
	 *
	 * @param userList the new user list
	 */
	@SuppressWarnings("unchecked")
	public void setUserList(ArrayList<User> userList) {
		usersDecryptedTable = (ArrayList<User>) userList.clone();
	}

}
