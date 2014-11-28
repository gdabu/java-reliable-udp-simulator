
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

    public ReliableUDPHeader(int PacketType, int WindowSize, String data, int SeqNum, int AckNum) {
        this.PacketType = PacketType;
        this.SeqNum = SeqNum;
        this.data = data;
        this.WindowSize = WindowSize;
        this.AckNum = AckNum;
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
    
}
