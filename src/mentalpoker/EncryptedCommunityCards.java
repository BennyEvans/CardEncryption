package mentalpoker;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The Class EncryptedCommunityCards.
 */
public class EncryptedCommunityCards extends Passable<EncryptedCard> implements
		java.io.Serializable {
	
	private ArrayList<User> usersDecryptedTable = new ArrayList<User>();

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7536396101508111322L;
	
	public boolean hasUserDecrypted(String userID){
		for(int i = 0; i < usersDecryptedTable.size(); i++){
			if (usersDecryptedTable.get(i).getID().equals(userID)){
				return true;
			}
		}
		return false;
	}
	
	public void addUserToDecryptedTable(User usr){
		usersDecryptedTable.add(usr);
	}
	
	public boolean compareCards(EncryptedCommunityCards comCards){
		//compare cards
		for (int i = 0; i< CommunityCards.NUM_CARDS; i++){
			EncryptedCard card1 = comCards.data.get(i);
			EncryptedCard card2 = this.data.get(i);
			if (!Arrays.equals(card1.cardData, card2.cardData)){
				return false;
			}
		}
		return true;
	}
	
	public boolean compareUserList(ArrayList<User> userList){
		//compare users
		for (int i = 0; i < usersDecryptedTable.size(); i++){
			if (!usersDecryptedTable.get(i).getID().equals(userList.get(i).getID())){
				return false;
			}
		}
		return true;
	}
	
	public int numUsersDecrypted(){
		return usersDecryptedTable.size();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<User> getUserList(){
		return (ArrayList<User>) usersDecryptedTable.clone();
	}
	
	@SuppressWarnings("unchecked")
	public void setUserList(ArrayList<User> userList){
		usersDecryptedTable = (ArrayList<User>) userList.clone();
	}

}
