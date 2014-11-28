import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

    private static int windowSize = 5;
    private static int networkPort = 7006;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static InetAddress networkAddress;
    private static DatagramSocket serverLocalSocket;

    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        serverLocalSocket = new DatagramSocket(7007);
        
        waitFor3WayHandShake();

        for (int i = 0; i < 10; i++) {
            //Receive packet
            incomingByteBuffer = new byte[1024];
            incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

            serverLocalSocket.receive(incomingPacket);
            //retrieve reliable UDP header information
            ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
            System.out.println("Received from Client: " + incomingPacketHeader.getPacketType());
        }

        for (int j = 0; j < 10; j++) {
            //send packets
            ReliableUDPHeader additionalHeader = new ReliableUDPHeader(2, windowSize, "", 0, 0);

            DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
            System.out.println("Sending Packet");
            serverLocalSocket.send(sendPacket);
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
        System.out.println("Received Syn Packet");
        
        //send SynAck
        additionalHeader = new ReliableUDPHeader(1, windowSize, "", 0, 1);
        //set reliable UDP header information
        DatagramPacket synPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending SynAck Packet");
        serverLocalSocket.send(synPacket);

        //receive Ack
        serverLocalSocket.receive(incomingPacket);
        //retrieve reliable UDP header information
         incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
        System.out.println("Received Ack Packet");

        return true;
    }
}
