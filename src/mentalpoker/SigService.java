package mentalpoker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * The Class SigService.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 */
public class SigService {

	/** The cipher. */
	private Cipher cipher;

	/** The key. */
	private KeyPair key;

	/** The Constant CHEATER_NONCE. */
	public final static int CHEATER_NONCE = 1;
	
	/** The Constant HAVE_HAND_NONCE. */
	public final static int HAVE_HAND_NONCE = 2;
	
	/** The Constant VERIFY_COM_CARDS_NONCE. */
	public final static int VERIFY_COM_CARDS_NONCE = 3;
	
	/** The Constant REQUEST_COM_CARDS_NONCE. */
	public final static int REQUEST_COM_CARDS_NONCE = 4;
	
	/** The Constant RAW_HAND_NONCE. */
	public final static int RAW_HAND_NONCE = 4;

	
	/**
	 * Instantiates a new signature service.
	 * 
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws NoSuchPaddingException the no such padding exception
	 */
	public SigService() throws NoSuchAlgorithmException, NoSuchPaddingException {
		Security.addProvider(new BouncyCastleProvider());
		System.out.println("Generating signiture keys");
		KeyPairGenerator keyGen;
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		key = keyGen.generateKeyPair();
		System.out.println("Finished generating keys.");
	}

	
	/**
	 * Creates a signature from byte array.
	 *
	 * @param data the data
	 * @return the byte[] signature
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public byte[] createSignatureFromByteArray(final byte[] data)
			throws UnsupportedEncodingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException {

		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(data);
		byte[] digest = messageDigest.digest();

		cipher.init(Cipher.ENCRYPT_MODE, key.getPrivate());
		return cipher.doFinal(digest);

	}

	
	/**
	 * Validate signature from byte array.
	 *
	 * @param sig the sig
	 * @param data the data
	 * @param pKey the key
	 * @return true, if successful
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public boolean validateSignatureFromByteArray(final byte[] sig,
			final byte[] data, PublicKey pKey) throws NoSuchAlgorithmException {

		byte[] newd;
		byte[] ver;

		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		try {
			cipher.init(Cipher.DECRYPT_MODE, pKey);
			newd = cipher.doFinal(sig);
		} catch (Exception e) {
			return false;
		}

		messageDigest.update(data);
		ver = messageDigest.digest();

		// make sure they match
		if (newd.length > ver.length) {
			return false;
		}
		for (int i = 0; i < newd.length; ++i) {
			if (ver[i] != newd[i]) {
				return false;
			}
		}

		return true;
	}

	
	/**
	 * Creates a signature.
	 *
	 * @param <E> the element type
	 * @param toSign the element to sign
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public <E> void createSignature(Passable<E> toSign)
			throws UnsupportedEncodingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(toSign.data);
			bos.close();
			out.close();
		} catch (IOException e1) {
			System.exit(0);
			return;
		}

		final byte[] data = bos.toByteArray();
		toSign.signature = createSignatureFromByteArray(data);

	}

	/**
	 * Creates a verified signature from know text and a nonce.
	 *
	 * @param nonce the nonce
	 * @return the byte[] signature
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws InvalidKeyException the invalid key exception
	 * @throws IllegalBlockSizeException the illegal block size exception
	 * @throws BadPaddingException the bad padding exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public byte[] createVerifiedSignature(int nonce)
			throws UnsupportedEncodingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException {

		String toSign = "verified" + Integer.toString(nonce);
		final byte[] data = toSign.getBytes();

		return createSignatureFromByteArray(data);
	}

	
	/**
	 * Validate signature.
	 * 
	 * @param <E> the element type
	 * @param sig the sig
	 * @param pKey the key
	 * @return true, if successful
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public <E> boolean validateSignature(Passable<E> sig, PublicKey pKey)
			throws NoSuchAlgorithmException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(sig.data);
			bos.close();
			out.close();
		} catch (IOException e1) {
			System.exit(0);
			return false;
		}

		final byte[] data = bos.toByteArray();

		return validateSignatureFromByteArray(sig.signature, data, pKey);
	}

	
	/**
	 * Validate verified signature with known nonce.
	 *
	 * @param sig the sig
	 * @param pKey the key
	 * @param nonce the nonce
	 * @return true, if successful
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public boolean validateVerifiedSignature(final byte[] sig, PublicKey pKey,
			int nonce) throws NoSuchAlgorithmException {

		String toSign = "verified" + Integer.toString(nonce);
		final byte[] data = toSign.getBytes();

		return validateSignatureFromByteArray(sig, data, pKey);
	}

	
	/**
	 * Gets the public key.
	 * 
	 * @return the public key
	 */
	public PublicKey getPublicKey() {
		return key.getPublic();
	}
}
