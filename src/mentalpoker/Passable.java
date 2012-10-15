package mentalpoker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * The Class Passable.
 * 
 * @author Benjamin Evans
 * @author Emile Victor
 * @version 1.0
 *
 * @param <E> the element type
 */
public class Passable<E> implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3090666232050760788L;

	/** The data. */
	public ArrayList<E> data = new ArrayList<E>();

	/** The signature. */
	public byte[] signature;

	
	/**
	 * Write object. Converts a Passable Object into a byte array.
	 *
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte[] writeObject() throws IOException {
		byte[] tmpBytes;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		out = new ObjectOutputStream(bos);
		out.writeObject(this);
		tmpBytes = bos.toByteArray();
		bos.close();
		out.close();
		return tmpBytes;
	}

	
	/**
	 * Read object. Converts a byte array representation of a Passable Object
	 * into a Passable<?> Object.
	 *
	 * @param tmpBytes the tmp bytes
	 * @return the passable
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static Passable<?> readObject(byte[] tmpBytes) throws IOException,
			ClassNotFoundException {
		
		Passable<?> ret;
		ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
		ObjectInput in;
		in = new ObjectInputStream(bis);
		ret = (Passable<?>) in.readObject();
		bis.close();
		in.close();
		return ret;
	}

}
