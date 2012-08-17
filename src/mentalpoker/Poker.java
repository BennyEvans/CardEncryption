package mentalpoker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 * The Class Poker.
 */
public class Poker {
	
	/** The rsa service. */
	RSAService rsaService;
	ComService com;
	public static String myUsername;
	
	/**
	 * Instantiates a new game.
	 *
	 * @param ip the ip to connect to
	 * @throws Exception the exception
	 */
	public Poker(String ip) throws Exception {
		
		rsaService = new RSAService();
		com = new ComService();
		
		User user = new User("Bob");
		
		EncryptedDeck encDeck = null;

		if (ip == null){
			System.out.println("No IP specified. Waiting for connections...\n");
			encDeck = createDeck(user);
		}
		
		//test of commutative RSA (encrypting and decrypting in a different order)
		RSAService tmprsaService = new RSAService(rsaService.getP(), rsaService.getQ());
		
		System.out.println("Data after first encryption step: " + new String(encDeck.encCards[0].cardData) + "\n");
		
		//encrypt the already encrypted card again with another rsaservice (different key)
		EncryptedCard deCard =  tmprsaService.encryptEncCard(encDeck.encCards[0]);
		System.out.println("Data after second encryption step: " + new String(deCard.cardData) + "\n");
		
		//decrypt with the first rsaService
		EncryptedCard ueCard = rsaService.decryptEncCard(deCard);
		System.out.println("Data after decryption with first key (used in first encryption step): " + new String(ueCard.cardData) + "\n");
		
		//then decrypt with the second
		Card c = tmprsaService.decryptCard(ueCard);
		
		//if this prints jiberish then something has gone wrong else all is sweet =)
		System.out.println("Data after decryption with second key (used in second encryption step): " + String.valueOf(c.cardType) + " of " + c.suit);
		
		MiscHelper.clearConsole();
		
		//Get player's username.
		setUsername();
		
		//Testing startNewGame.
		com.startNewGame(5);
		
	}
	
	/**
	 * Creates the deck.
	 *
	 * @return the encrypted deck
	 * @throws BadPaddingException 
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private EncryptedDeck createDeck(User user) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		Deck deck = new Deck(rsaService, user);
		EncryptedDeck encDeck = new EncryptedDeck();
		for (int i = 0; i < Deck.NUM_CARDS; i++){
			encDeck.encCards[i] = deck.getEncryptedCard(i);
		}
		return encDeck;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			String ip = null;
			if (args.length == 1){
				ip = args[0];
			}
			new Poker(ip);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setUsername()
	{
		System.out.print("Enter your username: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			myUsername = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("UNABLE TO READ FROM COMMAND LINE");
		}
	}

}
