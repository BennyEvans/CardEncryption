package mentalpoker;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * The Class RSAService.
 */
public class RSAService {
	
	/** The key size. */
	private static int KEY_SIZE = 1024;
	
	/** The max encryption size. */
	private static int MAX_ENC_SIZE = 128;
	
	/** Standard RSA Variables. */
	private BigInteger p;
	private BigInteger q;
	private BigInteger n;
	private BigInteger on;
	private BigInteger e;
	private BigInteger d;
	
	
	/**
	 * Instantiates a new rsa service.
	 *
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 * @throws NoSuchProviderException the no such provider exception
	 */
	public RSAService() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		p = genPrime(KEY_SIZE/2);
		q = genPrime(KEY_SIZE/2);
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
	public RSAService(BigInteger gp, BigInteger gq) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
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
	 * Gets the p value.
	 *
	 * @return the p value
	 */
	public BigInteger getP(){
		return new BigInteger(this.p.toString());
	}
	
	
	/**
	 * Gets the q value.
	 *
	 * @return the q value
	 */
	public BigInteger getQ(){
		return new BigInteger(this.q.toString());
	}
	
	
	/**
	 * Generates a prime.
	 *
	 * @param size the size
	 * @return the big integer
	 */
	private BigInteger genPrime(int size){
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
	private void genKeys() throws NoSuchAlgorithmException, InvalidKeySpecException{
		BigInteger tmp;
		BigInteger testRes;
		while(true){
			tmp = genPrime(KEY_SIZE/2);
			testRes = on.divideAndRemainder(tmp)[1];
			if (testRes.compareTo(BigInteger.ZERO) != 0){
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
	public byte[] encrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		if (data.length > MAX_ENC_SIZE){
			System.err.println("Data to long to encrypt. Reason: Possible cheating! Fix: Exit");
			System.exit(1);
		}
		
		RSAKeyParameters kp = new RSAKeyParameters(false, n, e);
		RSAEngine engine = new RSAEngine();
		engine.init(true, kp);
		byte[] cText = engine.processBlock(data, 0, data.length);
		return cText;
	}
	
	
	/**
	 * Decrypts a byte String
	 *
	 * @param data the data to decrypt
	 * @return the decrypted data
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public byte[] decrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		if (data.length > MAX_ENC_SIZE){
			System.err.println("Data to long to decrypt. Reason: Possible cheating! Fix: Exit");
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
	public EncryptedCard encryptCard(Card card) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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
	public Card decryptCard(EncryptedCard encCard) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
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
	public EncryptedCard encryptEncCard(EncryptedCard card) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
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
	public EncryptedCard decryptEncCard(EncryptedCard encCard) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		EncryptedCard card = new EncryptedCard();
		card.cardData = decrypt(encCard.cardData);
		return card;
	}
	
}
