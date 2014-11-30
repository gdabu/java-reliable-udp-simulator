import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 *
 * @author geoffdabu
 * @author jeffwong
 */
class PacketRelayer implements Runnable {

    private int remoteReceiverPort;
    private InetAddress remoteReceiverAddress;

    private DatagramSocket localReceiveSocket;
    private DatagramSocket localTransmitSocket;

    private int dropPercentage = 0;

    private static int totalDropped = 0;

    private static int dropTotal = 0;
    private static int transmitTotal = 1;
    private static int delay = 0;

    public PacketRelayer(DatagramSocket localReceiveSocket,
            DatagramSocket localTransmitSocket,
            InetAddress remoteReceiverAddress,
            int remoteReceiverPort,
            int dropPercentage,
            int delay) {
        this.localReceiveSocket = localReceiveSocket;
        this.localTransmitSocket = localTransmitSocket;
        this.remoteReceiverAddress = remoteReceiverAddress;
        this.remoteReceiverPort = remoteReceiverPort;
        this.dropPercentage = dropPercentage;
    }

    public void run() {
        try {
            Random rand = new Random();
            byte[] transmitByteArray;
            int randomNumber = 0;

            while (true) {
                transmitByteArray = new byte[1024];

                DatagramPacket transmitPacket = new DatagramPacket(transmitByteArray, transmitByteArray.length);

                // Wait for packet from sender
                localReceiveSocket.receive(transmitPacket);
                ReliableUDPHeader transmitHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(transmitPacket);
                System.out.println("> Transmit:   Src=" + transmitHeader.getSrcAddress() + ":" + transmitHeader.getSrcPort()
                        + ", Dst=" + transmitHeader.getDstAddress() + ":" + transmitHeader.getDstPort()
                        + ", SeqNum=" + transmitHeader.getSeqNum()
                        + ", AckNum=" + transmitHeader.getAckNum()
                        + ", Type=" + transmitHeader.getPacketType()
                        + ":" + transmitHeader.getData()
                );
                // DROP STATE
                // Packets are dropped if the random generated number is less than or equal to
                // the user specified drop rate, and if the dropped packet to total packet ratio
                // is less than the specified drop percentage. 
                // 
                randomNumber = rand.nextInt(100) + 1;

                if ((dropTotal / transmitTotal) * 100 <= dropPercentage) {
                    if (randomNumber <= dropPercentage) {
                        ReliableUDPHeader droppedPacket = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(transmitPacket);
                        System.out.println("\n> Dropped:    Src=" + droppedPacket.getSrcAddress() + ":" + droppedPacket.getSrcPort()
                                + ", Dst=" + droppedPacket.getDstAddress() + ":" + droppedPacket.getDstPort()
                                + ", SeqNum=" + droppedPacket.getSeqNum()
                                + ", AckNum=" + droppedPacket.getAckNum()
                                + ", Type=" + droppedPacket.getPacketType()
                                + ":" + droppedPacket.getData()
                        );
                        System.out.println("TotalDrops: " + dropTotal);
                        System.out.println("TotalTransmits: " + transmitTotal + "\n");
                        dropTotal += 1;
                        continue;
                    }
                }

                Thread.sleep(delay);

                // SEND STATE
                // change packet address from receiver to destination address
                transmitPacket.setAddress(remoteReceiverAddress);
                transmitPacket.setPort(remoteReceiverPort);

                // Send packet to receiver
                localTransmitSocket.send(transmitPacket);
                transmitTotal += 1;
            }
        } catch (Exception e) {

        }
    }
}

public class Network {

    private static InetAddress clientAddress;
    private static InetAddress serverAddress;
    private static int clientRemotePort = 7004;
    private static int serverRemotePort = 7007;
    private static int clientPort = 7005;
    private static int serverPort = 7006;

    private static DatagramSocket clientSocket;
    private static DatagramSocket serverSocket;

    private static int packetDropPercentage = 0;
    private static int delay = 0;

    private static PrintStream out;

    public static void main(String args[]) throws Exception {

        //print log to file
        out = new PrintStream(new FileOutputStream("NetworkLog.txt"));
        //System.setOut(out);

        //scan from config file        
        ConfigParser configFile;

        try {
            configFile = new ConfigParser("config.properties");
        } catch (Exception e) {
            System.out.println("Unable to open File");
            return;
        }

        //scan from config file
        clientPort = configFile.getNetClientPort();
        serverPort = configFile.getNetServerPort();
        clientRemotePort = configFile.getClientPort();
        serverRemotePort = configFile.getServerPort();

        packetDropPercentage = configFile.getDropRate();
        delay = configFile.getDelayTime();

        //initialize (remote) client and server addresses 
        clientAddress = InetAddress.getByName(configFile.getClientAddress());
        serverAddress = InetAddress.getByName(configFile.getServerAddress());

        //initialize local sockets for client and server transmission
        clientSocket = new DatagramSocket(clientPort);
        serverSocket = new DatagramSocket(serverPort);

        System.out.println("> Initialize Network");
        System.out.println("> Approximate Drop Rate: " + packetDropPercentage + "%\n");

        //start client to server relay thread
        PacketRelayer sendToServer = new PacketRelayer(clientSocket, serverSocket, serverAddress, serverRemotePort, packetDropPercentage, delay);
        Thread sendToServerRelayThread = new Thread(sendToServer);
        sendToServerRelayThread.start();

        //start server to client relay thread
        PacketRelayer sendToClient = new PacketRelayer(serverSocket, clientSocket, clientAddress, clientRemotePort, packetDropPercentage, delay);
        Thread sendToClientRelayThread = new Thread(sendToClient);
        sendToClientRelayThread.start();

        //Wait for both  relay threads to end
        sendToServerRelayThread.join();
        sendToClientRelayThread.join();

        System.out.print("> Ending Network");
    }
}
