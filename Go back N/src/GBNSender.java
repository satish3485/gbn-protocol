import java.util.ArrayList;
import transport.Network;
import transport.Sender;
import transport.TimeoutAction;

/**
 * This class provides a basic Go back N Sender
 * @author Nicos Giuliani
 * @version 1.0
 */
public class GBNSender implements Sender, TimeoutAction {

	private final static int SENDER_TIMEOUT_MS = 1000;
	private final static int WINDOW_SIZE = 5;
	
	private ArrayList<Packet> pending;
	private int seqNum; // Sequence number (int)
	private int seqNumLength; // Length of the sequence number (int)

	private byte[] data;
	private int packetLength;

	public GBNSender() {
		
		seqNumLength = SeqNum.toByte(seqNum).length;
		pending = new ArrayList<Packet>();
		data = new byte[Network.MAX_PACKET_SIZE + seqNumLength + 1];
		packetLength = 0;
		
	}

	private final void makePacket(final int seq, final byte[] buffer, int offset, final int length) {

		if (length + seqNumLength + 1 > Network.MAX_PACKET_SIZE) {
			packetLength = Network.MAX_PACKET_SIZE;
		} else {
			packetLength = length + seqNumLength + 1;
		}
		
		/** size of the sequence number */
		data[0] = (byte) seqNumLength;

		/**  transform sequence number into byte[] */
		byte[] seqNum = SeqNum.toByte(this.seqNum); 

		/** push the sequence number into the byte array */
		for (int i = 0; i < seqNumLength; i++) {
			data[i + 1] = seqNum[i];
		}

		/** push the content into the byte array */	
		for (int i = seqNumLength + 1; i < packetLength; i++) {
			data[i] = buffer[offset++];
		}
		
		/** Put it in pending packets */
		pending.add(new Packet(this.seqNum, packetLength, data));
		
		this.seqNum++;
	}

	/** 
	 * @param sequence number
	 * Sends all pending packets based on cumulative ACKs 
	 */
	private final void slideWindow(final int seqNum) {
		
		for (int i = 0; i < pending.size(); i++) {
		
			if(pending.get(i).getSeqNum() <= seqNum) {
				pending.remove(i);
			}	
		}
	}

	public final void unreliableReceive(byte[] buffer, int offset, int length) {

		final int receivedACK = SeqNum.toInt(buffer);
		
		if (receivedACK >= seqNum) {

			Network.setTimeout(SENDER_TIMEOUT_MS, this);
	        slideWindow(receivedACK);
            Network.allowClose();
            Network.resumeSender();
            System.out.println("ACK n: " + receivedACK );
            
		} else if (pending.size() == WINDOW_SIZE) {
			System.out.println("The window is full, timeout approching...");
		}
	}

	public final void timeoutExpired() {

		System.out.println("Sender timeout. Sending all pending..");
		
		for (int i = 0; i < pending.size(); i++) {
			Network.unreliableSend(pending.get(i).getContent(), 0, pending.get(i).getLength());
			System.out.println("Re-sending packet: " + pending.get(i).getSeqNum());
		}
		Network.setTimeout(SENDER_TIMEOUT_MS, this);
		
	}

	public final int reliableSend(final byte[] buffer, final int offset, final int length) {

		if(pending.size() <= WINDOW_SIZE) {

            Network.resumeSender();
			makePacket(seqNum, buffer, offset, length);
			Network.unreliableSend(data, 0, packetLength);
			Network.setTimeout(SENDER_TIMEOUT_MS, this);
			
		} else {
			Network.blockSender();
			Network.disallowClose();
		}
		
		return packetLength - (seqNumLength + 1);
	}

}
