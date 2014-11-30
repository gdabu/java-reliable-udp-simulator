import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author geoffdabu
 */
public class ReliableUDPHeader implements Serializable{

    private String data;
    private int PacketType; 
    private int WindowSize;
    private int SeqNum;
    private int AckNum;
    
    int srcPort;
    int dstPort;
    String srcAddress;
    String dstAddress;
    
    
    public ReliableUDPHeader(int PacketType, int WindowSize, String data, int SeqNum, int AckNum, int srcPort, String srcAddress, int dstPort, String dstAddress) {
        this.PacketType = PacketType;
        this.SeqNum = SeqNum;
        this.data = data;
        this.WindowSize = WindowSize;
        this.AckNum = AckNum;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.srcAddress = srcAddress;
        this.dstAddress = dstAddress;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public String getDstAddress() {
        return dstAddress;
    }
    
    public int getPacketType() {
        return PacketType;
    }

    public int getSeqNum() {
        return SeqNum;
    }

    public String getData() {
        return data;
    }

    public int getWindowSize() {
        return WindowSize;
    }

    public int getAckNum() {
        return AckNum;
    }
    
        public void setData(String data) {
        this.data = data;
    }

    public void setPacketType(int PacketType) {
        this.PacketType = PacketType;
    }

    public void setWindowSize(int WindowSize) {
        this.WindowSize = WindowSize;
    }

    public void setSeqNum(int SeqNum) {
        this.SeqNum = SeqNum;
    }

    public void setAckNum(int AckNum) {
        this.AckNum = AckNum;
    }    
}
