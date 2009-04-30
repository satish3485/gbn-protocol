import transport.Network;
import transport.Receiver;
import transport.TimeoutAction;

/**
 * This class provides a basic Go back N Receiver
 * @author Nicos Giuliani
 * @version 1.0
 */
public class GBNReceiver implements Receiver, TimeoutAction {

	private final static int RECEIVER_TIMEOUT = 3000;
	private int expSeqNum; // the expected sequence number

	public GBNReceiver() {}

	public final void unreliableReceive(final byte[] buffer, final int offset, final int length) {
		
		if(SeqNum.getSeqNum(buffer) == expSeqNum) {
			
			System.out.println("Received valid packet: " + expSeqNum);

			Network.cancelTimeout(this);
			Network.reliableReceive(buffer, offset + (int) buffer[0] + 1, length - ((int) buffer[0] + 1));
			
			// Send ACK
			Network.unreliableSend(SeqNum.toByte(expSeqNum), 0, SeqNum.toByte(expSeqNum).length);			
			Network.disallowClose();
			Network.setTimeout(RECEIVER_TIMEOUT, this);
			expSeqNum++;

		} else {
			System.out.println("Received invalid packet: " + SeqNum.getSeqNum(buffer) + ".\nExpected: " + expSeqNum + "\n");
			
			Network.cancelTimeout(this);
			// Received an invalid packet, ACK the precedent.
			Network.unreliableSend(SeqNum.toByte(expSeqNum), 0, SeqNum.toByte(expSeqNum).length);
			Network.disallowClose();
			Network.setTimeout(RECEIVER_TIMEOUT, this);
			
		}

	}
	
	public final void timeoutExpired() {

		try {
			System.out.println("Receiver timeout! Disconnection...");
			Network.cancelTimeout(this);
			Network.allowClose();
			Network.disconnect();
		} catch (InterruptedException e) {
			// We should not arrive here, we'll make this faster.
			Network.setTimeout(RECEIVER_TIMEOUT / 2, this);
			e.printStackTrace();
		}
	}	
}
