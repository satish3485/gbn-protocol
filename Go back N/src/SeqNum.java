

public final class SeqNum {

	private SeqNum() {}

	/**
	 * Convert the int to byte array.
	 */
	public final static byte[] toByte(final int number) {

		final int byteNum = (40 - Integer.numberOfLeadingZeros(number < 0 ? ~number : number)) / 8;
		final byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++) {
			byteArray[3 - n] = (byte) (number >>> (n * 8));
		}
		return (byteArray);
	}

	/**
	 * Convert the byte array to an int.
	 */
	public final static int toInt(final byte[] b) {
		return toInt(b, 0);
	}

	/**
	 * Convert the byte array to an int starting from the given offset.
	 */
	public final static int toInt(final byte[] b, final int offset) {
		
		int value = 0;
		
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		
		return value;
	}

	public final static int getSeqNum(final byte[] buffer) {
		
		final byte[] temp = new byte[(int) buffer[0]];
		
		for (int i = 0; i < temp.length; i++) {
			temp[i] = buffer[i + 1];
		}
		
		return toInt(temp);
		
	}

}
