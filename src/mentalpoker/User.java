package mentalpoker;

import java.math.BigInteger;
import java.security.PublicKey;
import java.util.UUID;

/**
 * The Class User.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class User implements java.io.Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4529431771734590529L;
	
	/** The screen name of the user. */
	private String name;
	
	/** The id of the user. */
	private String id;
	
	/** The users decryption key. */
	private BigInteger decryptionKey;
	
	/** The users public key. */
	private PublicKey pubKey;
	
	/** The users hand. */
	private Hand usersHand;
	
	/** The users original hand. */
	private EncryptedHand usersOriginalHand;
	
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name of the user
	 * @param pubKey the users public key
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
	 * @param uuid the id of the user
	 * @param pubKey the users public key
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
	
	
	/**
	 * Sets the decryption key.
	 *
	 * @param key the new decryption key
	 */
	public void setDecryptionKey(BigInteger key){
		decryptionKey = new BigInteger(key.toString());
	}
	
	
	/**
	 * Gets the decryption key.
	 *
	 * @return the decryption key
	 */
	public BigInteger getDecryptionKey(){
		return new BigInteger(decryptionKey.toString());
	}
	
	
	/**
	 * Checks for decryption key.
	 *
	 * @return true, if successful
	 */
	public boolean hasDecryptionKey(){
		return !(decryptionKey == null);
	}
	
	
	/**
	 * Gets the public key.
	 *
	 * @return the public key
	 */
	public PublicKey getPublicKey(){
		return pubKey;
	}
	
	
	/**
	 * Gets the users hand.
	 *
	 * @return the users hand
	 */
	public Hand getUsersHand(){
		return usersHand;
	}
	
	
	/**
	 * Gets the users original hand.
	 *
	 * @return the users original hand
	 */
	public EncryptedHand getUsersOriginalHand(){
		return usersOriginalHand;
	}
	
	
	/**
	 * Sets the users hand.
	 *
	 * @param hand the new users hand
	 */
	public void setUsersHand(Hand hand){
		usersHand = hand;
	}
	
	
	/**
	 * Sets the users original hand.
	 *
	 * @param hand the new users original hand
	 */
	public void setUsersOriginalHand(EncryptedHand hand){
		usersOriginalHand = hand;
	}

}
