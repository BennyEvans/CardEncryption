package mentalpoker;

import java.io.Serializable;
import java.util.UUID;

/**
 * The Class User.
 */
public class User implements Serializable{
	
	private static final long serialVersionUID = -4529431771734590529L;
	private String name;
	private String id;
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name of the user
	 */
	public User(String name){
		this.name = name.toString();
		this.id = UUID.randomUUID().toString();
	}
	
	/**
	 * Instantiates a new user.
	 *
	 * @param name the name of the user
	 * @param uuid the uuid of the user
	 */
	public User(String name, String uuid){
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

}
