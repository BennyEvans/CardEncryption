package mentalpoker;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

/**
 * The Class RSAService.
 */
public class RSAService {
	
	/** The key size. */
	private static int KEY_SIZE = 1024;
	
	/** The max encryption size. */
	private static int MAX_ENC_SIZE = 128;
	
	/** The cipher. */
	private Cipher cipher;
	
	/** The decryption key. */
	private PrivateKey decKey;
	
	/** The encryption key. */
	private PublicKey encKey;
	
	/** Standard RSA Variables */
	private BigInteger p;
	private BigInteger q;
	private BigInteger n;
	private BigInteger on;
	private BigInteger e;
	private BigInteger d;
	
	
	/**
	 * Instantiates a new RSA service.
	 *
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 */
	public RSAService() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException {
		cipher = Cipher.getInstance("RSA");
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
	 * Instantiates a new RSA service given p and q. Used to make RSA commutative.
	 *
	 * @param gp the gp
	 * @param gq the gq
	 * @throws InvalidKeySpecException the invalid key spec exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 */
	public RSAService(BigInteger gp, BigInteger gq) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException {
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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
	 * Generate a large prime of size, size.
	 *
	 * @param size the size
	 * @return the large prime
	 */
	private BigInteger genPrime(int size){
		Random rnd = new Random();
		BigInteger bp = BigInteger.probablePrime(size, rnd);
		return bp;
	}
	
	
	/**
	 * Generate encryption and decryption keys.
	 * 
	 * I have used a pretty dodgy way of generating e which make the algorithm a little less secure.
	 * At the moment e is chosen as a prime so as to make it more likely to be coprime to on.
	 *
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws InvalidKeySpecException the invalid key spec exception
	 */
	private void genKeys() throws NoSuchAlgorithmException, InvalidKeySpecException{
		BigInteger tmp;
		BigInteger testRes;
		while(true){
			tmp = genPrime(KEY_SIZE/8);
			testRes = on.divideAndRemainder(tmp)[1];
			if (testRes.compareTo(BigInteger.ZERO) != 0){
				e = tmp;
				d = e.modInverse(on);
				RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(n,e);
				RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(n,d);
				KeyFactory fact = KeyFactory.getInstance("RSA");
				encKey = fact.generatePublic(pubKeySpec);
				decKey = fact.generatePrivate(privKeySpec);
				return;
			}
		}
	}
	
	
	/**
	 * Encrypt byte string.
	 *
	 * @param data the data
	 * @return the ciphertext
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public byte[] encrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		if (data.length > MAX_ENC_SIZE){
			//cannot not encrypt more than MAX_ENC_SIZE bytes
			return null;
		}
		cipher.init(Cipher.ENCRYPT_MODE, encKey);
		byte[] cText = cipher.doFinal(data);
		return cText;
	}
	
	/**
	 * Decrypt byte string.
	 *
	 * @param data the data
	 * @return the plaintext
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public byte[] decrypt(byte[] data) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
	    cipher.init(Cipher.DECRYPT_MODE, decKey);
	    byte[] dText = cipher.doFinal(data);
		return dText;
	}
	
	/**
	 * Encrypt a card.
	 *
	 * @param card the card
	 * @return the sealed object
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public SealedObject encryptCard(Card card) throws InvalidKeyException, IllegalBlockSizeException, IOException{
		cipher.init(Cipher.ENCRYPT_MODE, encKey);
		SealedObject so = new SealedObject(card, cipher);
		return so;
	}
	
	/**
	 * Decrypt a card.
	 *
	 * @param so the so
	 * @return the card
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 * @throws BadPaddingException the bad padding exception
	 */
	public Card decryptCard(SealedObject so) throws InvalidKeyException, IllegalBlockSizeException, IOException, ClassNotFoundException, BadPaddingException {
	    cipher.init(Cipher.DECRYPT_MODE, decKey);
	    Card card = (Card) so.getObject(cipher);
		return card;
	}
	

}
