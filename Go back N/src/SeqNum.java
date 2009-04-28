/**
 * This class provide a basis library to handle integers and bytes.
 * @author Nicos Giuliani
 * @version 1.0
 */
public final class SeqNum {

	private SeqNum() {}

	/**
	 * Convert an integer to byte array.
	 * Notes: from http://snippets.dzone.com/posts/show/93, slightly modified.
	 * @param number
	 * @return a byte array
	 */
	public final static byte[] toByte(final int number) {

		final int byteNum = (40 - Integer.numberOfLeadingZeros(number < 0 ? ~number : number)) / 8;
		final byte[] byteArray = new byte[4];

		for (int i = 0; i < byteNum; i++) {
			byteArray[3 - i] = (byte) (number >>> (i * 8));
		}
		return byteArray;
	}

	/**
	 * Convert the byte array to an integer.
	 * @param b
	 * @return an integer
	 */
	public final static int toInt(final byte[] b) {
		return toInt(b, 0);
	}

	/**
	 * Convert the byte array to an integer starting from the given offset.
	 * @param b
	 * @param offset
	 * @return an integer
	 */
	public final static int toInt(final byte[] b, final int offset) {

		int value = 0;

		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	/**
	 * Get an array of bytes (a sequence number) and converts it into an integer.
	 * @param buffer
	 * @return sequence number (integer)
	 */
	public final static int getSeqNum(final byte[] buffer) {

		final byte[] temp = new byte[(int) buffer[0]];

		for (int i = 0; i < temp.length; i++) {
			temp[i] = buffer[i + 1];
		}

		return toInt(temp);
	}
}
