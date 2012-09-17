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

public interface GamePlay {
	
	public void playerGamePlay(RSAService rsaService, ComService comServ, User gameUser, ArrayList<User> gameUsers, SigService sig, SearchGamesTask jgt) throws Exception;
	
	public void hostGamePlay(RSAService rsaService, ComService comServ, User gameUser, ArrayList<User> gameUsers, SigService sig, EncryptedDeck encDeck, HostGameTask hgt) throws Exception;
	
	
	/**
	 * Determine if the given user's hand will win.
	 * Takes all the game users, then analyses their hands
	 * in order to figure out who has won.
	 */
	public User determineWinner(CommunityCards cc, ArrayList<User> allGameUsers);
	
	public void init();

}
