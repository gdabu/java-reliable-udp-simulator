/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author geoffdabu
 */
public class ReliableUDPConnectionHelper {
    /*
    private static boolean initiate3WayHandShake() throws Exception {
        incomingByteBuffer = new byte[1024];
        incomingPacket = new DatagramPacket(incomingByteBuffer, incomingByteBuffer.length);

        ReliableUDPHeader additionalHeader;

        //send Syn
        additionalHeader = new ReliableUDPHeader(0, windowSize, "", 0, 0);
        //set reliable UDP header information
        DatagramPacket synPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending Syn Packet: SeqNum=" + additionalHeader.getSeqNum() + ", AckNum=" + additionalHeader.getAckNum());
        clientLocalSocket.send(synPacket);

        clientLocalSocket.receive(incomingPacket);
        //retrieve reliable UDP header information
        ReliableUDPHeader incomingPacketHeader = (ReliableUDPHeader) ReliableUDPHelper.extractObjectFromPacket(incomingPacket);
        System.out.println("Received SynAck Packet: SeqNum=" + incomingPacketHeader.getSeqNum() + ", AckNum=" + incomingPacketHeader.getAckNum());

        additionalHeader = new ReliableUDPHeader(2, windowSize, "", 1, incomingPacketHeader.getAckNum());
        //set reliable UDP header information
        DatagramPacket ackPacket = ReliableUDPHelper.storeObjectIntoPacketPayload(additionalHeader, networkAddress, networkPort);
        System.out.println("Sending Ack Packet: SeqNum=" + additionalHeader.getSeqNum() + ", AckNum=" + additionalHeader.getAckNum());
        clientLocalSocket.send(ackPacket);

        return true;
    }
    */
    
    /*
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
    */
    
}
