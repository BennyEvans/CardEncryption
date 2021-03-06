package mentalpoker;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * The Class RSAService.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class RSAService {

	/** The key size. */
	private static final int KEY_SIZE = 512;

	/** The max encryption size. */
	private static final int MAX_ENC_SIZE = 128;

	
	/* Standard RSA Variables. */
	
	/** The p. */
	private BigInteger p;
	
	/** The q. */
	private BigInteger q;
	
	/** The n. */
	private BigInteger n;
	
	/** The on. */
	private BigInteger on;

	/** The encryption key. */
	private BigInteger e = null;

	/** The decryption key. */
	private BigInteger d;

	
	/**
	 * Instantiates a new rsa service.
	 * 
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public RSAService() throws InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			NoSuchProviderException {
		
		Security.addProvider(new BouncyCastleProvider());
		p = genPrime(KEY_SIZE);
		q = genPrime(KEY_SIZE);
		n = p.multiply(q);
		BigInteger qSub = q.subtract(BigInteger.ONE);
		BigInteger pSub = p.subtract(BigInteger.ONE);
		on = pSub.multiply(qSub);

		genKeys();
		return;
	}

	
	/**
	 * Instantiates a new rsa service with given p and q.
	 * 
	 * @param gp the given p
	 * @param gq the given q
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public RSAService(BigInteger gp, BigInteger gq)
			throws InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchPaddingException, NoSuchProviderException {
		
		Security.addProvider(new BouncyCastleProvider());
		this.p = new BigInteger(gp.toString());
		this.q = new BigInteger(gq.toString());
		n = p.multiply(q);
		BigInteger qSub = q.subtract(BigInteger.ONE);
		BigInteger pSub = p.subtract(BigInteger.ONE);
		on = pSub.multiply(qSub);
		genKeys();
		return;
	}

	
	/**
	 * Instantiates a new rsa service. This constructor is only to be used
	 * during validation. It only allows decryption.
	 * 
	 * @param gp the given p
	 * @param gq the given q
	 * @param gd the given decryption key
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public RSAService(BigInteger gp, BigInteger gq, BigInteger gd)
			throws InvalidKeySpecException, NoSuchAlgorithmException,
			NoSuchPaddingException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		this.p = new BigInteger(gp.toString());
		this.q = new BigInteger(gq.toString());
		n = p.multiply(q);
		BigInteger qSub = q.subtract(BigInteger.ONE);
		BigInteger pSub = p.subtract(BigInteger.ONE);
		on = pSub.multiply(qSub);
		this.d = new BigInteger(gd.toString());
		return;
	}

	
	/**
	 * Gets the p prime.
	 * 
	 * @return the p value
	 */
	public BigInteger getP() {
		return new BigInteger(this.p.toString());
	}

	
	/**
	 * Gets the q prime.
	 * 
	 * @return the q value
	 */
	public BigInteger getQ() {
		return new BigInteger(this.q.toString());
	}

	
	/**
	 * Gets the decryption key.
	 *
	 * @return the d value
	 */
	public BigInteger getD() {
		return new BigInteger(this.d.toString());
	}

	
	/**
	 * Generates a large prime.
	 * 
	 * @param size the size of the prime
	 * @return the big integer prime
	 */
	private BigInteger genPrime(int size) {
		Random rnd = new Random();
		BigInteger bp = BigInteger.probablePrime(size, rnd);
		return bp;
	}

	
	/**
	 * Generates the keys.
	 * 
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws InvalidKeySpecException the invalid key spec exception
	 */
	private void genKeys() throws NoSuchAlgorithmException,
			InvalidKeySpecException {
		
		BigInteger tmp;
		BigInteger testRes;
		while (true) {
			tmp = genPrime(KEY_SIZE);
			testRes = on.divideAndRemainder(tmp)[1];
			if (testRes.compareTo(BigInteger.ZERO) != 0) {
				e = tmp;
				d = e.modInverse(on);
				return;
			}
		}
	}

	
	/**
	 * Encrypts a byte string.
	 * 
	 * @param data the data to encrypt
	 * @return the encrypted data
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private byte[] encrypt(byte[] data) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		if (data.length > MAX_ENC_SIZE) {
			System.err
					.println("Data to long to encrypt. Reason: Possible cheating! Fix: Exit");
			System.exit(1);
		}

		RSAKeyParameters kp = new RSAKeyParameters(false, n, e);
		RSAEngine engine = new RSAEngine();
		engine.init(true, kp);
		byte[] cText = engine.processBlock(data, 0, data.length);
		return cText;
	}

	
	/**
	 * Decrypts a byte String.
	 * 
	 * @param data the data to decrypt
	 * @return the decrypted data
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private byte[] decrypt(byte[] data) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		if (data.length > MAX_ENC_SIZE) {
			System.err
					.println("Data to long to decrypt. Reason: Possible cheating! Fix: Exit");
			System.exit(1);
		}

		RSAKeyParameters kp = new RSAKeyParameters(true, n, d);
		RSAEngine engine = new RSAEngine();
		engine.init(false, kp);
		byte[] dText = engine.processBlock(data, 0, data.length);
		return dText;
	}

	
	/**
	 * Encrypts a card.
	 * 
	 * @param card the card to encrypt
	 * @return the encrypted card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private EncryptedCard encryptCard(Card card) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		if (e == null) {
			System.err
					.println("Cannot encrypt when using decrypting constructor RSAService(gp, gq, gd)");
			return null;
		}
		String encCardData = String.valueOf(card.cardType) + card.suit;
		EncryptedCard encCard = new EncryptedCard();
		encCard.cardData = encrypt(encCardData.getBytes());
		return encCard;
	}

	
	/**
	 * Decrypts a card.
	 * 
	 * @param encCard the encrypted card
	 * @return the decrypted card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private Card decryptCard(EncryptedCard encCard) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		String data = new String(decrypt(encCard.cardData));
		char type = data.charAt(0);
		String suit = data.substring(1).toString();
		Card card = new Card(type, suit);
		return card;
	}

	
	/**
	 * Encrypts an already encrypted card.
	 * 
	 * @param card the encrypted card
	 * @return the encrypted, encrypted card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private EncryptedCard encryptEncCard(EncryptedCard card)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		if (e == null) {
			System.err
					.println("Cannot encrypt when using decrypting constructor RSAService(gp, gq, gd)");
			return null;
		}
		EncryptedCard encCard = new EncryptedCard();
		encCard.cardData = encrypt(card.cardData);
		return encCard;
	}

	
	/**
	 * Decrypts an encrypted, encrypted card.
	 * 
	 * @param encCard the encrypted, encrypted card
	 * @return the encrypted card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	private EncryptedCard decryptEncCard(EncryptedCard encCard)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		EncryptedCard card = new EncryptedCard();
		card.cardData = decrypt(encCard.cardData);
		return card;
	}

	
	/**
	 * Encrypt an encrypted deck.
	 * 
	 * @param encDeck the encrypted deck
	 * @return the encrypted, encrypted deck
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public EncryptedDeck encryptEncDeck(EncryptedDeck encDeck)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		if (e == null) {
			System.err
					.println("Cannot encrypt when using decrypting constructor RSAService(gp, gq, gd)");
			return null;
		}
		for (int i = 0; i < Deck.NUM_CARDS; i++) {
			EncryptedCard tmp = encDeck.data.get(i);
			encDeck.data.set(i, encryptEncCard(tmp));
		}
		return encDeck;
	}
	
	
	/**
	 * Decyrpt an encrypted hand.
	 *
	 * @param hand the hand
	 * @return the encrypted hand
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public EncryptedHand decyrptEncHand(EncryptedHand hand)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		EncryptedHand ret = new EncryptedHand();
		for (int i = 0; i < Hand.NUM_CARDS; i++) {
			EncryptedCard tmp = hand.data.get(i);
			ret.data.add(decryptEncCard(tmp));
		}
		return ret;
	}

	
	/**
	 * Decyrpt a hand.
	 *
	 * @param hand the hand
	 * @return the hand
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public Hand decyrptHand(EncryptedHand hand) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		Hand ret = new Hand();
		for (int i = 0; i < Hand.NUM_CARDS; i++) {
			EncryptedCard tmp = hand.data.get(i);
			ret.data.add(decryptCard(tmp));
		}
		return ret;
	}

	
	/**
	 * Decyrpt encrypted, encrypted community cards.
	 *
	 * @param cards the cards
	 * @return the encrypted community cards
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public EncryptedCommunityCards decyrptEncComCards(
			EncryptedCommunityCards cards) throws InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		
		EncryptedCommunityCards ret = new EncryptedCommunityCards();
		ret.setUserList(cards.getUserList());
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			EncryptedCard tmp = cards.data.get(i);
			ret.data.add(decryptEncCard(tmp));
		}
		return ret;
	}

	
	/**
	 * Decyrpt community cards.
	 *
	 * @param cards the cards
	 * @return the community cards
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public CommunityCards decyrptComCards(EncryptedCommunityCards cards)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		CommunityCards ret = new CommunityCards();
		for (int i = 0; i < CommunityCards.NUM_CARDS; i++) {
			EncryptedCard tmp = cards.data.get(i);
			ret.data.add(decryptCard(tmp));
		}
		return ret;
	}

	
	/**
	 * Encrypt a deck.
	 * 
	 * @param deck the deck
	 * @return the encrypted deck
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public EncryptedDeck encryptDeck(Deck deck)
			throws InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException {
		
		if (e == null) {
			System.err
					.println("Cannot encrypt when using decrypting constructor RSAService(gp, gq, gd)");
			return null;
		}
		EncryptedDeck encDeck = new EncryptedDeck();
		for (int i = 0; i < Deck.NUM_CARDS; i++) {
			EncryptedCard tmp = encryptCard(deck.getCardAtIndex(i));
			if (i >= encDeck.data.size()) {
				encDeck.data.add(tmp);
			} else {
				encDeck.data.set(i, tmp);
			}

		}
		return encDeck;
	}
	
	/**
	 * Encrypt an encrypted hand.
	 *
	 * @param encHand the encrypted hand
	 * @return the encrypted hand
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public EncryptedHand encryptEncHand(EncryptedHand encHand) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		if (e == null) {
			System.err
					.println("Cannot encrypt when using decrypting constructor RSAService(gp, gq, gd)");
			return null;
		}
		EncryptedHand ret = new EncryptedHand();
		for (int i = 0; i < Hand.NUM_CARDS; i++) {
			EncryptedCard tmp = encryptEncCard(encHand.data.get(i));
			ret.data.add(tmp);
		}
		return ret;
	}
	
	
	/**
	 * Verifies the winners hand.
	 *
	 * @param gameUser the game user
	 * @param userHands the users hands
	 * @param winner the winner
	 * @param rsaServ the rsa service
	 * @return true, if verified
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public boolean checkWinnersHand(User gameUser, ArrayList<User> userHands, User winner, RSAService rsaServ) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Hand uHand = winner.getUsersHand();
		Hand dHand;
		EncryptedHand eHand = winner.getUsersOriginalHand();
		BigInteger p = rsaServ.getP();
		BigInteger q = rsaServ.getQ();
		for (User usr: userHands){
			if (usr.getID().equals(gameUser.getID())){
				continue;
			}
			RSAService tmpRSA = new RSAService(p, q, usr.getDecryptionKey());
			eHand = tmpRSA.decyrptEncHand(eHand);
		}
		dHand = rsaServ.decyrptHand(eHand);
		
		//now compare the hands
		for(int i = 0; i < Hand.NUM_CARDS; i++){
			if(dHand.data.get(i).checkIfJibberish()){
				//cards are not valid
				return false;
			}
			if (uHand.data.get(i).cardType != dHand.data.get(i).cardType){
				return false;
			}
			if (!uHand.data.get(i).suit.equals(dHand.data.get(i).suit)){
				return false;
			}
		}
		System.out.println("Winners hand verified as:");
		for(int i = 0; i < Hand.NUM_CARDS; i++){
			System.out.println(Character.toString(dHand.data.get(i).cardType) + "-" + new String(dHand.data.get(i).suit));
		}
		return true;
	}
	

}
