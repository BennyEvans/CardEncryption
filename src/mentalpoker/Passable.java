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
 */
public class Passable implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3090666232050760788L;
	
	/** The data. */
	public ArrayList<EncryptedCard> data = new ArrayList<EncryptedCard>();
	
	/** The signature. */
	public byte[] signature;
	
	public byte[] writeObject() throws IOException{
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
	
	public static Passable readObject(byte[] tmpBytes) throws IOException, ClassNotFoundException{
		Passable ret;
		ByteArrayInputStream bis = new ByteArrayInputStream(tmpBytes);
		ObjectInput in;
		in = new ObjectInputStream(bis);
		ret = (Passable) in.readObject(); 
		bis.close();
		in.close();
		return ret;
	}

}
