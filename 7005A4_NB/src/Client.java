import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author geoffdabu
 * @author jeffwong
 */
public class Client {

    private static int totalPackets = 30;
    private static int windowSize = 5;
    private static int networkPort = 7005;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static InetAddress networkAddress;
    private static DatagramSocket clientLocalSocket;

    private static ArrayList<ReliableUDPHeader> packetQueue;     //contains all the packets to be sent
    private static ArrayList<ReliableUDPHeader> packetWindow;   //contains the first windowSized amount of packets from packetQueue
    private static ArrayList<ReliableUDPHeader> ackList;

    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        clientLocalSocket = new DatagramSocket(7004);
        packetQueue = new ArrayList();
        packetWindow = new ArrayList();
        ackList = new ArrayList();

        //initiate3WayHandShake();
        System.out.println("> Connection Established!\n");

        // QEUEING STATE
        //fill up the packetQueue with data packets
        for (int l = 0; l < totalPackets; l++) {
            packetQueue.add(new ReliableUDPHeader(3, windowSize, "DATA", l, 0));
        }

        // RELIABLE UDP TRANSMISSION
        // All the packets in the packetQeueu are sent to the server and acknowledged.
        int packetWindowSeqFloor;  // The sequence number of the first packet in the window.  
        int packetWindowSeqCeil;   // The sequence number of the last packet in the window.
        clientLocalSocket.setSoTimeout(5000); // Set the initial timeout phase duration

        // Begin Reliable UDP Transmission
        // Transmission ends when there are no data packets left to be sent.
        while (!packetQueue.isEmpty()) {

            //add a windowSized amount of packets from packetQueue to packetWindow
            for (int m = 0; m < Math.min(windowSize, packetQueue.size()); m++) {
                packetWindow.add(packetQueue.get(m));
            }

            // SEND STATE
            //
            // During the send state the client sends a window sized amount of packets
            System.out.println("\n> Sending...");

            long sendTime = System.currentTimeMillis();
            for (int i = 0; i < Math.min(windowSize, packetQueue.size()); i++) {

                //set reliable UDP header information
                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(packetWindow.get(i), networkAddress, networkPort);
                System.out.println("Sending Packet: SeqNum=" + packetWindow.get(i).getSeqNum() + ", AckNum=" + packetWindow.get(i).getAckNum());
                //if(i == 0)
                //{
                //    sendTime = System.currentTimeMillis();
                //}
                clientLocalSocket.send(sendPacket);
            }

            // RECEIVE STATE
            //
            // During the receive state the client listens for windowSized amount of packets.
            // If packets have been dropped during transmission then a timeout will occur
            // and the client will transition into the send state
            //determine lowest and highest sequence numbers of the window that was sent
            packetWindowSeqFloor = packetWindow.get(0).getSeqNum();
            packetWindowSeqCeil = packetWindow.get(Math.min(windowSize - 1, packetQueue.size() - 1)).getSeqNum();

            System.out.println("\n> Receiving...");
            for (int j = 0; j < Math.min(windowSize, packetQueue.size()); j++) {

                incomingByteBuffer = new byte[1024];
                incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

                //If a Timeout occurs then a new window of packets are to be sent 
                try {
                    clientLocalSocket.receive(incomingPacket);
                    //if (j == 0) {
                    //    long receiveTime = System.currentTimeMillis();
                    //    System.out.println("> RTT=" + (receiveTime-sendTime));
                    //    clientLocalSocket.setSoTimeout((int)(receiveTime - sendTime));
                    //}
                } catch (SocketTimeoutException to) {
                    System.out.println("> Packet Timeout");
                    
                    //break out of the receive state and begin sending
                    break;
                }

                // If an ACK packet is Received then analyze the header
                // Extract the ReliableUDPHeader from the ACK packet payload
                ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Received from Server: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());

                // If we receive an ACK that is for a packet outside of latest sent window then ignore it.
                if (incomingPacketHeader.getAckNum() - 1 < packetWindowSeqFloor || incomingPacketHeader.getAckNum() - 1 > packetWindowSeqCeil) {

                    continue;
                }

                // If we receive an ACK that is for a packet from the latest window sent add it to the ACK list
                // All the packets in the ACK list are removed from the packetQeueu so that the packet is'nt sent again
                ackList.add(incomingPacketHeader);

                // If we received all the ACKs for the latest window that was sent, then send the new window
                if (ackList.size() == packetWindow.size()) {
                    //break out of the receive state and begin sending                    
                    break;
                }
            }

            //Remove all the packets that have been ACK'ed successfully from the packetQueue
            Iterator<ReliableUDPHeader> q = packetQueue.iterator();

            for (ReliableUDPHeader p : ackList) {
                while (q.hasNext()) {
                    if (p.getAckNum() - 1 == q.next().getSeqNum()) {
                        q.remove();
                    }
                }
                q = packetQueue.iterator();
            }

            //reset both the packetWindow and ackList 
            packetWindow.clear();
            ackList.clear();
        }

        System.out.println("\n> ALL DATA SENT\n");

        // KILL STATE
        //
        // At this point all the data packets have been sent and 
        // the client attempts to kill the connection.
        // EOT packets are sent to the server until an EOT acknowldegement is received or
        // a specified number of EOT packets have been sent out (i.e. 5 EOT packets)
        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

        ReliableUDPHeader eotHeader = new ReliableUDPHeader(4, windowSize, "EOT", totalPackets, 0);
        DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(eotHeader, networkAddress, networkPort);

        boolean resendEOT = true;
        int repeatEOTsend = 0;

        do {
            System.out.println("Sending EOT Packet: SeqNum=" + eotHeader.getSeqNum() + ", AckNum=" + eotHeader.getAckNum());
            clientLocalSocket.send(sendPacket);
            repeatEOTsend += 1;
            try {
                clientLocalSocket.receive(incomingPacket);
                ReliableUDPHeader eotACK = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Receive EOT Ack: SeqNum=" + eotACK.getSeqNum() + ", AckNum=" + eotACK.getAckNum());
                System.out.println("\n>Connection Closed\n");
            } catch (SocketTimeoutException to) {
                continue;
            }
            resendEOT = false;
        } while (resendEOT && repeatEOTsend < 5);

        System.out.println("client done!");
    }
}
