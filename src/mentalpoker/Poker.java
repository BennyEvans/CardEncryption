package mentalpoker;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;


/**
 * The Class Poker.
 */
public class Poker {
	
	/** The rsa service. */
	RSAService rsaService;
	public static String myUsername;
	
	/**
	 * Instantiates a new game.
	 *
	 * @param ip the ip to connect to
	 * @throws Exception the exception
	 */
	public Poker(String ip) throws Exception {
		rsaService = new RSAService();
		EncryptedDeck encDeck = null;

		if (ip == null){
			System.out.println("No IP specified. Waiting for connections...\n");
			encDeck = createDeck();
		}
		
		//test of commutative RSA (encrypting and decrypting in a different order)
		RSAService tmprsaService = new RSAService(rsaService.getP(), rsaService.getQ());
		
		//encrypt the already encrypted card again with another rsaservice (different key)
		EncryptedCard deCard =  tmprsaService.encryptEncCard(encDeck.encCards[0]);
		
		//decrypt with the first rsaService
		EncryptedCard ueCard = rsaService.decryptEncCard(deCard);
		
		//then decrypt with the second
		Card c = tmprsaService.decryptCard(ueCard);
		
		//if this prints jiberish then something has gone wrong else all is sweet =)
		System.out.println(String.valueOf(c.cardType) + " of " + c.suit);
		
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
	private EncryptedDeck createDeck() throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
		Deck deck = new Deck(rsaService);
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

}
