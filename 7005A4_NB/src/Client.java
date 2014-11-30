import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
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

    private static int totalPackets;
    private static int windowSize;
    private static int netPort;
    private static int waitTime;

    private static int srcPort;
    private static int dstPort;
    private static String srcAddress;
    private static String dstAddress;

    private static InetAddress netAddress;
    private static DatagramSocket clientLocalSocket;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static ArrayList<ReliableUDPHeader> packetQueue;    //contains all the packets to be sent
    private static ArrayList<ReliableUDPHeader> packetWindow;   //contains the first windowSized amount of packets from packetQueue
    private static ArrayList<ReliableUDPHeader> ackList;
    
    private static PrintStream out;
    
    public static void main(String args[]) throws Exception {

        //print log to file
        out = new PrintStream(new FileOutputStream("ClientLog.txt"));
        //System.setOut(out);
        
        
        //Get information from config file
        ConfigParser configFile;

        try {
            configFile = new ConfigParser("config.properties");
        } catch (Exception e) {
            System.out.println("Unable to open File");
            return;
        }

        //scan from config file
        srcPort = configFile.getClientPort();
        netPort = configFile.getNetClientPort();
        dstPort = configFile.getServerPort();
        srcAddress = configFile.getClientAddress();
        dstAddress = configFile.getServerAddress();
        netAddress = InetAddress.getByName(configFile.getNetAddress());
        clientLocalSocket = new DatagramSocket(srcPort);

        totalPackets = configFile.getTotalPackets();
        windowSize = configFile.getWindowSize();
        waitTime = configFile.getDelayTime() * 3;

        packetQueue = new ArrayList<ReliableUDPHeader>();
        packetWindow = new ArrayList<ReliableUDPHeader>();
        ackList = new ArrayList<ReliableUDPHeader>();

                
        System.out.println("Client Initiated\nConnection Established!\n");

        // QEUEING STATE
        //fill up the packetQueue with data packets
        for (int l = 0; l < totalPackets; l++) {
            packetQueue.add(new ReliableUDPHeader(3, windowSize, "DATA", l, 0, srcPort, srcAddress, dstPort, dstAddress));
        }

        // RELIABLE UDP TRANSMISSION
        // All the packets in the packetQeueu are sent to the server and acknowledged.
        int packetWindowSeqFloor;  // The sequence number of the first packet in the window.  
        int packetWindowSeqCeil;   // The sequence number of the last packet in the window.
        clientLocalSocket.setSoTimeout(waitTime); // Set the initial timeout phase duration

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

            for (int i = 0; i < Math.min(windowSize, packetQueue.size()); i++) {

                //set reliable UDP header information
                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(packetWindow.get(i), netAddress, netPort);
                System.out.println("Src=" + packetWindow.get(i).getSrcAddress() +":"+ packetWindow.get(i).srcPort
                        + ", Dst=" + packetWindow.get(i).getDstAddress() + ":" + packetWindow.get(i).getDstPort()
                        + ", SeqNum=" + packetWindow.get(i).getSeqNum() 
                        + ", AckNum=" + packetWindow.get(i).getAckNum()
                        + ", Type=" + packetWindow.get(i).getPacketType()
                        + ":" + packetWindow.get(i).getData()
                );

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

                } catch (SocketTimeoutException to) {
                    System.out.println("> Packet Timeout");

                    //break out of the receive state and begin sending
                    break;
                }

                // If an ACK packet is Received then analyze the header
                // Extract the ReliableUDPHeader from the ACK packet payload
                ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Src=" + incomingPacketHeader.getSrcAddress() +":"+ incomingPacketHeader.srcPort
                        + ", Dst=" + incomingPacketHeader.getDstAddress() + ":" + incomingPacketHeader.getDstPort()
                        + ", SeqNum=" + incomingPacketHeader.getSeqNum() 
                        + ", AckNum=" + incomingPacketHeader.getAckNum()
                        + ", Type=" + incomingPacketHeader.getPacketType()
                        + ":" + incomingPacketHeader.getData()
                );
                
                // If we receive an ACK that is for a packet outside of latest sent window then ignore it.
                if (incomingPacketHeader.getAckNum() - 1 < packetWindowSeqFloor || incomingPacketHeader.getAckNum() - 1 > packetWindowSeqCeil) {
                    //Buggy - This occurs when a timeout occurs but the packet hasnt
                    // been dropped. Congestion.
                    System.out.println(">Dup Ack from Server: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());
                    System.out.println("Src=" + incomingPacketHeader.getSrcAddress() +":"+ incomingPacketHeader.srcPort
                        + ", Dst=" + incomingPacketHeader.getDstAddress() + ":" + incomingPacketHeader.getDstPort()
                        + ", SeqNum=" + incomingPacketHeader.getSeqNum() 
                        + ", AckNum=" + incomingPacketHeader.getAckNum()
                        + ", Type=" + incomingPacketHeader.getPacketType()
                        + ":" + incomingPacketHeader.getData()
                    );
                    System.out.println(">Premature Timeout occured");
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

        // KILL STATE
        //
        // At this point all the data packets have been sent and 
        // the client attempts to kill the connection.
        // EOT packets are sent to the server until an EOT acknowldegement is received or
        // a specified number of EOT packets have been sent out (i.e. 5 EOT packets)
        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

        ReliableUDPHeader eotHeader = new ReliableUDPHeader(4, windowSize, "EOT", totalPackets, 0, srcPort, srcAddress, dstPort, dstAddress);
        DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(eotHeader, netAddress, netPort);

        boolean resendEOT = true;
        int repeatEOTsend = 0;

        //Persist on sending the EOT packet until you receive an EOT ack 
        //or until you tried to send the EOT packet atleast 5 times
        System.out.println("\n> Sending...");
        do {
            System.out.println("Src=" + eotHeader.getSrcAddress() +":"+ eotHeader.srcPort
                        + ", Dst=" + eotHeader.getDstAddress() + ":" + eotHeader.getDstPort()
                        + ", SeqNum=" + eotHeader.getSeqNum() 
                        + ", AckNum=" + eotHeader.getAckNum()
                        + ", Type=" + eotHeader.getPacketType()
                        + ":" + eotHeader.getData());
            clientLocalSocket.send(sendPacket);
            repeatEOTsend += 1;
            try {
                clientLocalSocket.receive(incomingPacket);
                ReliableUDPHeader eotACK = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                if (eotACK.getPacketType() != 5) {
                    continue;
                }
                System.out.println("\n> Receiving...\nSrc=" + eotACK.getSrcAddress() +":"+ eotACK.srcPort
                        + ", Dst=" + eotACK.getDstAddress() + ":" + eotACK.getDstPort()
                        + ", SeqNum=" + eotACK.getSeqNum() 
                        + ", AckNum=" + eotACK.getAckNum()
                        + ", Type=" + eotACK.getPacketType()
                        + ":" + eotACK.getData()
                );
                System.out.println("\n>Connection Closed\n");
            } catch (SocketTimeoutException to) {
                continue;
            }
            resendEOT = false;
        } while (resendEOT && repeatEOTsend < 5);

        System.out.println("client done!");
    }
}
