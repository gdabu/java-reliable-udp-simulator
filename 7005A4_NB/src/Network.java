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
class PacketRelayer implements Runnable {

    private InetAddress remoteReceiverAddress;
    private int remoteReceiverPort;

    private DatagramSocket localReceiveSocket;
    private DatagramSocket localTransmitSocket;

    public PacketRelayer(DatagramSocket localReceiveSocket,
            DatagramSocket localTransmitSocket,
            InetAddress remoteReceiverAddress,
            int remoteReceiverPort) {
        this.localReceiveSocket = localReceiveSocket;
        this.localTransmitSocket = localTransmitSocket;
        this.remoteReceiverAddress = remoteReceiverAddress;
        this.remoteReceiverPort = remoteReceiverPort;
    }

    public void run() {
        try {
            byte[] transmitByteArray;
            while (true) {
                transmitByteArray = new byte[1024];

                DatagramPacket transmitPacket = new DatagramPacket(transmitByteArray, transmitByteArray.length);

                //Wait for packet from sender
                localReceiveSocket.receive(transmitPacket);
                System.out.println(">Packet Received from Sender");
                
                System.out.println(new String(transmitPacket.getData()));

                //change packet address from receiver to destination address
                transmitPacket.setAddress(remoteReceiverAddress);
                transmitPacket.setPort(remoteReceiverPort);

                //Send packet to receiver
                localTransmitSocket.send(transmitPacket);
                System.out.println(">Packet Sent to Receiver");
            }
        } catch (Exception e) {

        }
    }
}

public class Network {

    private static InetAddress clientAddress;
    private static InetAddress serverAddress;
    private static final int clientRemotePort   = 7004;
    private static final int serverRemotePort   = 7007;
    private static final int clientPort         = 7005;
    private static final int serverPort         = 7006;

    private static DatagramSocket clientSocket;
    private static DatagramSocket serverSocket;

    public static void main(String args[]) throws Exception {
        
        //initialize (remote) client and server addresses 
        clientAddress = InetAddress.getByName("localhost");
        serverAddress = InetAddress.getByName("localhost");
        
        //initialize local sockets for client and server transmission
        clientSocket = new DatagramSocket(clientPort);
        serverSocket = new DatagramSocket(serverPort);
        
        System.out.println(">Initialize Network");
        
        //start client to server relay thread
        PacketRelayer sendToServer              = new PacketRelayer(clientSocket, serverSocket, serverAddress, serverRemotePort);
        Thread sendToServerRelayThread          = new Thread(sendToServer);
        sendToServerRelayThread.start();        
        
        //start server to client relay thread
        PacketRelayer sendToClient              = new PacketRelayer(serverSocket, clientSocket, clientAddress, clientRemotePort);
        Thread sendToClientRelayThread          = new Thread(sendToClient);
        sendToClientRelayThread.start();
        
        //Wait for both  relay threads to end
        sendToServerRelayThread.join();
        sendToClientRelayThread.join();
        
        System.out.print(">Ending Network");
    }
}
