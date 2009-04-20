import java.util.ArrayList;
import transport.Network;
import transport.Sender;
import transport.TimeoutAction;

public class GBNSender implements Sender, TimeoutAction {

	private static final int SENDER_TIMEOUT_MS = 1000;
	private static final int WINDOW_SIZE = 15;
	
	private ArrayList<Packet> pending;
	private int seqNum;
	private int seqNumLength;

	private int base;
	private byte[] data_pkt;
	private int data_pkt_len;

	public GBNSender() {
		
		seqNumLength = SeqNum.toByte(seqNum).length;
		pending = new ArrayList<Packet>();
		data_pkt = new byte[Network.MAX_PACKET_SIZE + seqNumLength + 1];
		data_pkt_len = 0;
		setBase(0);
		
	}

	private final void make_packet(final int seq, final byte[] buffer, int offset, final int length) {

		if (length + seqNumLength + 1 > Network.MAX_PACKET_SIZE) {
			data_pkt_len = Network.MAX_PACKET_SIZE;
		} else {
			data_pkt_len = length + seqNumLength + 1;
		}
		
		/** size of the sequence number */
		data_pkt[0] = (byte) seqNumLength;

		/**  transform sequence number into byte[] */
		byte[] seqNum = SeqNum.toByte(this.seqNum); 

		/** push the sequence number into the byte array */
		for (int i = 0; i < seqNumLength; i++) {
			data_pkt[i + 1] = seqNum[i];
		}

		/** push the content into the byte array */	
		for (int i = seqNumLength + 1; i < data_pkt_len; i++) {
			data_pkt[i] = buffer[offset++];
		}
		
		/** Put it in pending packets */
		pending.add(new Packet(this.seqNum, data_pkt_len, data_pkt));
		
		this.seqNum++;

	}

	/** Slides the window based on cumulative ACKs */
	private final void slideWindow(final int seqNum) {
		
		for (int i = 0; i < pending.size(); i++) {
		
			if(pending.get(i).getSeqNum() <= seqNum){
				pending.remove(i);
			}
			
		}
		setBase(seqNum);
	}

	public final void unreliableReceive(byte[] buffer, int offset, int length) {

		final int receivedSeqNum = SeqNum.toInt(buffer);
		
		if (length > 0 && receivedSeqNum >= seqNum && pending.size() <= WINDOW_SIZE) {

			System.out.println("Received ACK n. " + receivedSeqNum);
			slideWindow(receivedSeqNum);
			Network.cancelTimeout(this);
			Network.allowClose();
			Network.resumeSender();
			
		} else if(receivedSeqNum < seqNum) {
			System.out.println("Received wrong ACK n: " + receivedSeqNum + " instead of: " + seqNum);
		} else if (pending.size() >= WINDOW_SIZE){
			System.out.println("The window is full, timeout approching...");
		}

	}

	public final void timeoutExpired() {

		Network.cancelTimeout(this);
		Network.setTimeout(SENDER_TIMEOUT_MS, this);
		
		for (int i = 0; i < pending.size(); i++) {
			System.out.println("Sender timeout, re-sending packet: " + pending.get(i).getSeqNum());
			Network.unreliableSend(pending.get(i).getContent(), 0, pending.get(i).getLength());
		}
		
	}

	public final int reliableSend(final byte[] buffer, final int offset, final int length) {

		Network.blockSender();
		Network.disallowClose();
		make_packet(seqNum, buffer, offset, length);
		Network.setTimeout(SENDER_TIMEOUT_MS, this);
		Network.unreliableSend(data_pkt, 0, data_pkt_len);
		
		return data_pkt_len - (seqNumLength + 1);

	}

	/**
	 * @param base the base to set
	 */
	public final void setBase(int base) {
		this.base = base;
	}

	/**
	 * @return the base
	 */
	public final int getBase() {
		return base;
	}
}
