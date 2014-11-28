
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    
    public static void main(String args[]) throws Exception
    {
        InetAddress NetworkAddress = InetAddress.getByName("localhost");
        DatagramSocket serverLocalSocket = new DatagramSocket(7007);
        
        //Receive packet
        byte[] incomingByteArray         = new byte[1024]; 
        DatagramPacket incomingPacket    = new DatagramPacket(incomingByteArray, incomingByteArray.length);
        
        serverLocalSocket.receive(incomingPacket);
        byte[] data = incomingPacket.getData();
        
        ByteArrayInputStream byteIStream = new ByteArrayInputStream(data);
        ObjectInputStream objIStream = new ObjectInputStream(byteIStream);
        
        ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) objIStream.readObject();
        
        
        
        //send packets
        ReliableUDPHeader additionalHeader = new ReliableUDPHeader(1, windowSize, "", 0, 0);
        ByteArrayOutputStream byteOStream = new ByteArrayOutputStream();
        ObjectOutputStream objOStream = new ObjectOutputStream(byteOStream);
        objOStream.writeObject(additionalHeader);
        
        byte[] outgoingByteArray = byteOStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(outgoingByteArray, outgoingByteArray.length, NetworkAddress, 7006);
        
        System.out.println("Sending Packet");
        serverLocalSocket.send(sendPacket);
    }
}
