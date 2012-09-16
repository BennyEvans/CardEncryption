package mentalpoker;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.UUID;

/**
 * The Class User.
 */
public class User implements java.io.Serializable{
	
	private static final long serialVersionUID = -4529431771734590529L;
	private String name;
	private String id;
	private BigInteger decryptionKey;
	private PublicKey pubKey;
	private Hand usersHand;
	private EncryptedHand usersOriginalHand;
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name of the user
	 */
	public User(String name, PublicKey pubKey){
		this.pubKey = pubKey;
		this.name = name.toString();
		this.id = UUID.randomUUID().toString();
		decryptionKey = null;
	}
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name of the user
	 * @param uuid the uuid of the user
	 */
	public User(String name, String uuid, PublicKey pubKey){
		this.pubKey = pubKey;
		this.name = name.toString();
		this.id = uuid.toString();
	}
	
	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername(){
		return this.name.toString();
	}
	
	/**
	 * Gets the user id.
	 *
	 * @return the id
	 */
	public String getID(){
		return this.id.toString();
	}
	
	public void setDecryptionKey(BigInteger key){
		decryptionKey = new BigInteger(key.toString());
	}
	
	public BigInteger getDecryptionKey(){
		return new BigInteger(decryptionKey.toString());
	}
	
	public boolean hasDecryptionKey(){
		return !(decryptionKey == null);
	}
	
	public PublicKey getPublicKey(){
		return pubKey;
	}
	
	public Hand getUsersHand(){
		return usersHand;
	}
	
	public EncryptedHand getUsersOriginalHand(){
		return usersOriginalHand;
	}
	
	public void setUsersHand(Hand hand){
		usersHand = hand;
	}
	
	public void setUsersOriginalHand(EncryptedHand hand){
		usersOriginalHand = hand;
	}

}
