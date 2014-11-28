import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class Client {

    private static int windowSize = 5;
    private static int networkPort = 7005;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;
    
    private static InetAddress networkAddress;
    private static DatagramSocket clientLocalSocket;

    
    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        clientLocalSocket = new DatagramSocket(7004);
        
        initiate3WayHandShake();

        for (int i = 0; i < 10; i++) {
            //send packets
            ReliableUDPHeader additionalHeader = new ReliableUDPHeader(1, windowSize, "", 0, 0);
            //set reliable UDP header information
            DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
            System.out.println("Sending Packet");
            clientLocalSocket.send(sendPacket);
        }

        for (int j = 0; j < 10; j++) {
            //Receive packet
            incomingByteBuffer = new byte[1024];
            incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

            clientLocalSocket.receive(incomingPacket);
            //retrieve reliable UDP header information
            ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);

            System.out.println("Received from Server " + incomingPacketHeader.getPacketType());
        }
    }
    
    private static boolean initiate3WayHandShake() throws Exception
    {
        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);
        
        ReliableUDPHeader additionalHeader;
        
        //send Syn
        additionalHeader = new ReliableUDPHeader(0, windowSize, "", 0, 0);
        //set reliable UDP header information
        DatagramPacket synPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending Syn Packet");
        clientLocalSocket.send(synPacket);
        
        clientLocalSocket.receive(incomingPacket);
        //retrieve reliable UDP header information
        ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
        System.out.println("Received SynAck Packet");
        
        additionalHeader = new ReliableUDPHeader(2, windowSize, "", 1, 1);
        //set reliable UDP header information
        DatagramPacket ackPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending Ack Packet");
        clientLocalSocket.send(ackPacket);
        

        
        return true;
    }

}
