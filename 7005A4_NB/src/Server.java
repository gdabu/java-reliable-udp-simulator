import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author geoffdabu
 */
public class Server {

    private static int windowSize;
    private static int networkPort = 7006;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static InetAddress networkAddress;
    private static DatagramSocket serverLocalSocket;

    private static ArrayList<ReliableUDPHeader> receiveWindow;

    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        serverLocalSocket = new DatagramSocket(7007);

        waitFor3WayHandShake();
        System.out.println("Connection Established!");

        receiveWindow = new ArrayList();

        while (true) {
            for (int i = 0; i < windowSize; i++) {
                //Receive packet
                incomingByteBuffer = new byte[1024];
                incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

                serverLocalSocket.receive(incomingPacket);
                //retrieve reliable UDP header information
                ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Received from Client: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());

                receiveWindow.add(incomingPacketHeader);
                if(incomingPacketHeader.getPacketType() == 4)
                {
                    break;
                }
            }


            for (int j = 0; j < Math.min(windowSize, receiveWindow.size()); j++) {

                //send packets
                ReliableUDPHeader outgoingHeader = new ReliableUDPHeader(2, windowSize, "", receiveWindow.get(j).getAckNum(), receiveWindow.get(j).getSeqNum() + 1);//receiveWindow.get(j);

                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(outgoingHeader, networkAddress, networkPort);
                System.out.println("Sending Packet: SeqNum=" + outgoingHeader.getSeqNum() + ", AckNum=" + outgoingHeader.getAckNum());
                serverLocalSocket.send(sendPacket);
            }
            
            receiveWindow.clear();
        }

    }

    private static boolean waitFor3WayHandShake() throws Exception {
        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

        ReliableUDPHeader additionalHeader;
        ReliableUDPHeader incomingPacketHeader;

        //Receive Syn
        serverLocalSocket.receive(incomingPacket);
        //retrieve reliable UDP header information
        incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
        windowSize = incomingPacketHeader.getWindowSize();
        System.out.println("Received Syn Packet: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());

        //send SynAck
        additionalHeader = new ReliableUDPHeader(1, windowSize, "", 0, incomingPacketHeader.getSeqNum() + 1);
        //set reliable UDP header information
        DatagramPacket synAckPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending SynAck Packet: SeqNum=" + additionalHeader.getSeqNum() + ", AckNum=" + additionalHeader.getAckNum());
        serverLocalSocket.send(synAckPacket);

        //receive Ack
        serverLocalSocket.receive(incomingPacket);
        //retrieve reliable UDP header information
        incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
        System.out.println("Received Ack Packet: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());

        return true;
    }
}
