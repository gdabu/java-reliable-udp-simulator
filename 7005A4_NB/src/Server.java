import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 */
public class Server {

    private static int windowSize = 5;
    private static int networkPort = 7006;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static InetAddress networkAddress;
    private static DatagramSocket serverLocalSocket;

    private static ArrayList<ReliableUDPHeader> receiveWindow;

    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        serverLocalSocket = new DatagramSocket(7007);

        //waitFor3WayHandShake();
        System.out.println("Connection Established!");

        receiveWindow = new ArrayList();
        
        serverLocalSocket.setSoTimeout(2000);

        while (true) {

                incomingByteBuffer = new byte[1024];
                incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

                try{
                    serverLocalSocket.receive(incomingPacket);
                }catch(SocketTimeoutException to)
                {
                    continue;
                }
                //retrieve reliable UDP header information
                ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Received from Client: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());



                
                if(incomingPacketHeader.getPacketType() == 4)
                {
                    System.out.println("EOT packet received");
                    incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                    incomingPacketHeader.setPacketType(4);
                    incomingPacketHeader.setAckNum(incomingPacketHeader.getSeqNum() + 1);
                    incomingPacketHeader.setSeqNum(0);
                    DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(incomingPacketHeader, networkAddress, networkPort);
                    System.out.println("Sending Packet: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());
                    serverLocalSocket.send(sendPacket);
                    break;
                }
                
                


                //send packets
                ReliableUDPHeader outgoingHeader = new ReliableUDPHeader(2, windowSize, "", 0, incomingPacketHeader.getSeqNum() + 1);//receiveWindow.get(j);

                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(outgoingHeader, networkAddress, networkPort);
                System.out.println("Sending Packet: SeqNum=" + outgoingHeader.getSeqNum() + ", AckNum=" + outgoingHeader.getAckNum());
                serverLocalSocket.send(sendPacket);

            

        }

        System.out.println("Server Program Complete!");

    }
}
