package mentalpoker;

import java.util.ArrayList;

/**
 * The Class Passable.
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

}
