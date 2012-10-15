package mentalpoker.gameplay;

import java.util.ArrayList;

import mentalpoker.ComService;
import mentalpoker.CommunityCards;
import mentalpoker.EncryptedDeck;
import mentalpoker.RSAService;
import mentalpoker.SigService;
import mentalpoker.User;
import mentalpoker.SwingGUI.HostGameTask;
import mentalpoker.SwingGUI.SearchGamesTask;


/**
 * The Interface GamePlay.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public interface GamePlay {

	
	/**
	 * Play the player game play.
	 *
	 * @param rsaService the rsa service
	 * @param comServ the communications service
	 * @param gameUser the game user
	 * @param gameUsers the game users
	 * @param sig the signature service
	 * @param jgt the jgt
	 * @throws Exception the exception
	 */
	public void playerGamePlay(RSAService rsaService, ComService comServ,
			User gameUser, ArrayList<User> gameUsers, SigService sig,
			SearchGamesTask jgt) throws Exception;

	
	/**
	 * Host game play.
	 *
	 * @param rsaService the rsa service
	 * @param comServ the communications service
	 * @param gameUser the game user
	 * @param gameUsers the game users
	 * @param sig the signature service
	 * @param encDeck the encrypted deck
	 * @param hgt the hgt
	 * @throws Exception the exception
	 */
	public void hostGamePlay(RSAService rsaService, ComService comServ,
			User gameUser, ArrayList<User> gameUsers, SigService sig,
			EncryptedDeck encDeck, HostGameTask hgt) throws Exception;

	
	/**
	 * Determine the game winner. Takes all the game users, then
	 * analyses their hands in order to figure out who has won.
	 *
	 * @param cc the community cards
	 * @param allGameUsers the all game users
	 * @return the user who is the winner
	 */
	public User determineWinner(CommunityCards cc, ArrayList<User> allGameUsers);

	
	/**
	 * Initialises the game play.
	 */
	public void init();

}
