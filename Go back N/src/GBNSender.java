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

	private final static int SENDER_TIMEOUT = 1000;
	private final static int WINDOW_SIZE = 5; // On my local network this is the best window size
	
	private ArrayList<Packet> pendingPackets;
	private int seqNum; // Sequence number (integer)

	public GBNSender() {
		pendingPackets = new ArrayList<Packet>();
	}

	public final void unreliableReceive(byte[] buffer, int offset, int length) {

		final int receivedACK = SeqNum.toInt(buffer);
		
		if (receivedACK >= seqNum && length > 0) {

			Network.setTimeout(SENDER_TIMEOUT, this);
			
			// remove all packets up to the received sequence number
	        removeAckwnoleged(receivedACK);
	        
            Network.allowClose();
            Network.resumeSender();
            System.out.println("ACK " + receivedACK);
		} 
	}

	public final void timeoutExpired() {

		System.out.println("Re-sending all pending packets...\n");
		
        for (int i = 0; i < pendingPackets.size(); i++) {
			Network.unreliableSend(pendingPackets.get(i).getContent(), 0, pendingPackets.get(i).getLength());
			System.out.println("Re-sending packet: " + pendingPackets.get(i).getSeqNum());
		}
		
		System.out.println();
		Network.setTimeout(SENDER_TIMEOUT, this);
	}

	public final int reliableSend(final byte[] buffer, final int offset, final int length) {

		if(pendingPackets.size() <= WINDOW_SIZE) {

            Network.resumeSender();
            Network.allowClose();
            
            // Creates the new packet
            Packet packet = makePacket(seqNum, buffer, offset, length);
			
            Network.unreliableSend(packet.getContent(), 0, packet.getLength());
			Network.setTimeout(SENDER_TIMEOUT, this);
			
			return packet.getLength() - ( (int) packet.getContent()[0] + 1);
			
		} else {
			Network.blockSender();
			Network.disallowClose();
			return 0;
		}
	}

	/** 
	 * @param a sequence number
	 * Sends all pending packets based on cumulative ACKs 
	 */
	private final void removeAckwnoleged(final int seqNum) {
        
		for (int i = 0; i < pendingPackets.size(); i++) {
			
			if(pendingPackets.get(i).getSeqNum() <= seqNum) {
				pendingPackets.remove(i);
			}	
		}
	}
	
	/**
	 * This method creates packets ready to be sent through the network
	 * @param seq
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return a new packet
	 */
	private final Packet makePacket(final int seq, final byte[] buffer, int offset, final int length) {
		
		// Length of the sequence number (integer)
		int seqNumLength = SeqNum.toByte(seqNum).length; 
		int packetLength;
		
		if (length + seqNumLength + 1 > Network.MAX_PACKET_SIZE) {
			packetLength = Network.MAX_PACKET_SIZE;
		} else {
			packetLength = length + seqNumLength + 1;
		}
		
		byte[] data = new byte[Network.MAX_PACKET_SIZE + seqNumLength + 1];
		
		// transform sequence number into byte[]
		byte[] seqNum = SeqNum.toByte(this.seqNum); 
		// store the size of the sequence number
		data[0] = (byte) seqNumLength;
		
		// push the content and sequence number into the byte array
		for (int i = 1; i < packetLength; i++) {
			
			if(i < seqNumLength + 1) {
				data[i] = seqNum[i - 1];
			} else {
				data[i] = buffer[offset++];
			}
		}
		// Create a new packet
		Packet newPacket = new Packet(this.seqNum, packetLength, data);
		
		// Add to the pending packets
		pendingPackets.add(newPacket);
		this.seqNum++;
		
		return newPacket;
	}

}