
import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    public static void main(String args[]) throws Exception
    {   
        
        DatagramSocket clientSocket = new DatagramSocket(7004);
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData_Bytes = new byte[1024];
        
        sendData_Bytes = ">Syn packet".getBytes();
        
        
        byte[] receive_ByteArray = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receive_ByteArray, receive_ByteArray.length);
        
        
        //syn is sent here,
        //start timer here
        
        DatagramPacket sendPacket = new DatagramPacket(sendData_Bytes, sendData_Bytes.length, IPAddress, 7005);
        for(int i = 0; i < 5; i++)
        {
            System.out.println("Sending Packet");
            clientSocket.send(sendPacket);
        }
        
        for(int i = 0; i < 5; i++)
        {
            clientSocket.receive(receivePacket);
            System.out.println(new String(receivePacket.getData()));
        }
    } 
}
