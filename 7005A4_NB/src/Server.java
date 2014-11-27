
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
    public static void main(String args[]) throws Exception
    {
        DatagramSocket clientSocket     = new DatagramSocket(7007);
        
        byte[] receiveByteArray         = new byte[1024];
        
        DatagramPacket receivePacket    = new DatagramPacket(receiveByteArray, receiveByteArray.length);
        
        byte[] sendByteArray            = new byte[1024];
        DatagramPacket sendPacket;
        
        
        for(int i = 0; i < 5; i++)
        {
            System.out.println(">Waiting for Syn");
            clientSocket.receive(receivePacket);

            System.out.println(">Syn Packet Received from Client");
            System.out.println(new String(receivePacket.getData()));
            
        }
        
        sendByteArray = "geoffs back from the dead".getBytes();
        sendPacket = new DatagramPacket(sendByteArray, sendByteArray.length, InetAddress.getByName("localhost"), 7006);
        
        for(int j = 0; j < 5; j++)
        {
            System.out.println(">Sending packet to client");            
            clientSocket.send(sendPacket);
        }
    }
}
