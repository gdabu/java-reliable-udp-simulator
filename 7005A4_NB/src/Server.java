import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author geoffdabu
 * @authro jeffwong
 */
public class Server {

    private static int windowSize = 5;
    private static int networkPort = 7006;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static int srcPort;
    private static int dstPort;
    private static int netPort;
    private static String srcAddress;
    private static String dstAddress;
    private static String netAddress;

    private static InetAddress networkAddress;
    private static DatagramSocket serverLocalSocket;

    private static ArrayList<ReliableUDPHeader> receiveWindow;

    private static PrintStream out;

    public static void main(String args[]) throws Exception {

        //print log to file
        out = new PrintStream(new FileOutputStream("ServerLog.txt"));
        //System.setOut(out);

        //scan from config file        
        ConfigParser configFile;

        try {
            configFile = new ConfigParser("config.properties");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to open File");
            return;
        };

        //scan from config file
        srcPort = configFile.getServerPort();
        netPort = configFile.getNetServerPort();
        dstPort = configFile.getClientPort();
        srcAddress = configFile.getServerAddress();
        dstAddress = configFile.getClientAddress();
        netAddress = configFile.getNetAddress();

        networkAddress = InetAddress.getByName(netAddress);
        serverLocalSocket = new DatagramSocket(srcPort);

        System.out.println("Server Initiated\nConnection Established!\n\n");

        receiveWindow = new ArrayList();

        serverLocalSocket.setSoTimeout(2000);

        while (true) {

            incomingByteBuffer = new byte[1024];
            incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

            try {
                serverLocalSocket.receive(incomingPacket);
            } catch (SocketTimeoutException to) {
                continue;
            }
            //retrieve reliable UDP header information
            ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
            System.out.println("> Received:   Src=" + incomingPacketHeader.getSrcAddress() + ":" + incomingPacketHeader.srcPort
                    + ", Dst=" + incomingPacketHeader.getDstAddress() + ":" + incomingPacketHeader.getDstPort()
                    + ", SeqNum=" + incomingPacketHeader.getSeqNum()
                    + ", AckNum=" + incomingPacketHeader.getAckNum()
                    + ", Type=" + incomingPacketHeader.getPacketType()
                    + ":" + incomingPacketHeader.getData()
            );

            if (incomingPacketHeader.getPacketType() == 4) {
                incomingPacketHeader.setPacketType(5);
                incomingPacketHeader.setAckNum(incomingPacketHeader.getSeqNum() + 1);
                incomingPacketHeader.setSeqNum(0);
                incomingPacketHeader.setData("EOT ACK");
                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(incomingPacketHeader, networkAddress, networkPort);
                System.out.println("\n> Sending:   Src=" + incomingPacketHeader.getSrcAddress() + ":" + incomingPacketHeader.srcPort
                        + ", Dst=" + incomingPacketHeader.getDstAddress() + ":" + incomingPacketHeader.getDstPort()
                        + ", SeqNum=" + incomingPacketHeader.getSeqNum()
                        + ", AckNum=" + incomingPacketHeader.getAckNum()
                        + ", Type=" + incomingPacketHeader.getPacketType()
                        + ":" + incomingPacketHeader.getData()
                );
                serverLocalSocket.send(sendPacket);
                break;
            }

            //send packets
            ReliableUDPHeader outgoingHeader = new ReliableUDPHeader(2, windowSize, "ACK", 0, incomingPacketHeader.getSeqNum() + 1, srcPort, srcAddress, dstPort, dstAddress);//receiveWindow.get(j);

            DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(outgoingHeader, networkAddress, networkPort);
            System.out.println("> Sending:    Src=" + outgoingHeader.getSrcAddress() + ":" + outgoingHeader.srcPort
                    + ", Dst=" + outgoingHeader.getDstAddress() + ":" + outgoingHeader.getDstPort()
                    + ", SeqNum=" + outgoingHeader.getSeqNum()
                    + ", AckNum=" + outgoingHeader.getAckNum()
                    + ", Type=" + outgoingHeader.getPacketType()
                    + ":" + outgoingHeader.getData()
            );

            serverLocalSocket.send(sendPacket);

        }

        System.out.println("Server Program Complete!");

    }
}
