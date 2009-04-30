/**
 * This class provide a basis Packet structure.
 * @author Nicos Giuliani
 * @version 1.0
 */
public final class Segment {
	
	private final int seqNum;
	private final int length;
	private final byte[] content;
	
	Segment(final int seqNum, final int length, final byte[] content) {
		this.seqNum = seqNum;
		this.length = length;
		this.content = content;
	}

	/**
	 * @return the seqNum
	 */
	public final int getSeqNum() {
		return seqNum;
	}

	/**
	 * @return the length
	 */
	public final int getLength() {
		return length;
	}

	/**
	 * @return the content
	 */
	public final byte[] getContent() {
		return content;
	}
	
}
