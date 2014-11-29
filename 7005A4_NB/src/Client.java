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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

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

    private static int totalPackets = 30;
    private static int windowSize = 5;
    private static int networkPort = 7005;

    private static byte[] incomingByteBuffer;
    private static DatagramPacket incomingPacket;

    private static InetAddress networkAddress;
    private static DatagramSocket clientLocalSocket;

    private static ArrayList<ReliableUDPHeader> allPackets;     //contains all the packets to be sent

    private static ArrayList<ReliableUDPHeader> packetWindow;   //contains the first windowSized amount of packets from allPackets

    private static int ackWaitTime = 5;

    public static void main(String args[]) throws Exception {
        networkAddress = InetAddress.getByName("localhost");
        clientLocalSocket = new DatagramSocket(7004);

        allPackets = new ArrayList();
        packetWindow = new ArrayList();

        //initiate3WayHandShake();
        System.out.println("Connection Established!");

        int l;
        //fill up the allPackets arraylist with all the packets that are to be sent
        for (l = 0; l < totalPackets; l++) {
            allPackets.add(new ReliableUDPHeader(3, windowSize, "", l, 0));
        }

        //send/wait for all the packets and all their acks
        while (!allPackets.isEmpty()) {

            //add a windowSized amount of packets from allPackets to packetWindow
            for (int m = 0; m < Math.min(windowSize, allPackets.size()); m++) {
                packetWindow.add(allPackets.get(m));
            }

            //determine lowest and highest sequence numbers
            int x = packetWindow.get(0).getSeqNum();
            int y = packetWindow.get(Math.min(windowSize - 1, allPackets.size() - 1)).getSeqNum();

            //start timer here
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < Math.min(windowSize, allPackets.size()); i++) {
                //send packets

                //set reliable UDP header information
                DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(packetWindow.get(i), networkAddress, networkPort);
                System.out.println("Sending Packet: SeqNum=" + packetWindow.get(i).getSeqNum() + ", AckNum=" + packetWindow.get(i).getAckNum());
                clientLocalSocket.send(sendPacket);
            }

            ArrayList<ReliableUDPHeader> AckList = new ArrayList();
            clientLocalSocket.setSoTimeout(2000);

            for (int j = 0; j < Math.min(windowSize, allPackets.size()); j++) {
            //while (System.currentTimeMillis() - startTime < ackWaitTime * 1000) {

                //Receive packet
                incomingByteBuffer = new byte[1024];
                incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

                try {
                    clientLocalSocket.receive(incomingPacket);
                } catch (SocketTimeoutException to) {
                    System.out.println("Packet Timeout");
                    break;
                }

                //retrieve reliable UDP header information
                ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Received from Server: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());
                if (incomingPacketHeader.getAckNum() - 1 < x || incomingPacketHeader.getAckNum() - 1 > y) {
                    continue;
                }

                AckList.add(incomingPacketHeader);

                if (AckList.size() == packetWindow.size()) {
                    break;
                }
            }

            System.out.println("out of receiving loop :" + (System.currentTimeMillis() - startTime));

            //Remove all the packets that have been successfully ACKed from the queue
            Iterator<ReliableUDPHeader> q = allPackets.iterator();

            for (ReliableUDPHeader p : AckList) {
                while (q.hasNext()) {
                    if (p.getAckNum() - 1 == q.next().getSeqNum()) {
                        q.remove();
                    }
                }
                q = allPackets.iterator();
            }

            AckList.clear();
            packetWindow.clear();

        }

        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);
        //add the EOT packet to the end of the queue
        ReliableUDPHeader eotHeader = new ReliableUDPHeader(4, windowSize, "", totalPackets, 0);

        DatagramPacket sendPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(eotHeader, networkAddress, networkPort);

        boolean resendEOT = true;
        int repeatEOTsend = 0;
        
        do {
            System.out.println("Sending EOT Packet: SeqNum=" + eotHeader.getSeqNum() + ", AckNum=" + eotHeader.getAckNum());
            clientLocalSocket.send(sendPacket);
            repeatEOTsend += 1;
            try{
                clientLocalSocket.receive(incomingPacket);
                ReliableUDPHeader eotACK = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
                System.out.println("Receive EOT Ack: SeqNum=" + eotACK.getSeqNum() + ", AckNum=" + eotACK.getAckNum());

            }catch(SocketTimeoutException to){
                continue;
            }
            resendEOT = false;
        } while (resendEOT && repeatEOTsend < 5);

        System.out.println("client done!");
    }
}
