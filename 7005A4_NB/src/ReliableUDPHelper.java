
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 *
 * This class provides helper functions, specifically for the implementation of ReliableUDPHelper
 * 
 * The ReliableUDPHeader is stored into the payload of a regular UDP packet, these helper methods 
 * make it easier for the ReliableUDPHeader to be stored and extracted into/from the payload.
 * 
 * 
 * @author geoffdabu
 */
public class ReliableUDPHelper {
    /**
     * 
     * Extracts object from the packet payload
     * 
     * @param incomingPacket
     * @return
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static Object extractObjectFromPacket(DatagramPacket incomingPacket) throws IOException, ClassNotFoundException {
        byte[] data = incomingPacket.getData();
        
        ByteArrayInputStream byteIStream = new ByteArrayInputStream(data);
        ObjectInputStream objIStream = new ObjectInputStream(byteIStream);
        
        return objIStream.readObject();
    }

    /**
     * 
     * Converts an object into a packet which is ready to be sent to the remote host
     * 
     * The object is converted into a byte array, and is stored into the payload of outgoing packet 
     * 
     * @param objToBeSent
     * @param remoteAddress
     * @param remotePort
     * @return
     * @throws IOException 
     */
    public static DatagramPacket storeObjectIntoPacketPayload(Object objToBeSent, InetAddress remoteAddress, int remotePort) throws IOException 
    {
        //Converts the object into a Byte Array
        ByteArrayOutputStream byteOStream   = new ByteArrayOutputStream();
        ObjectOutputStream objOStream       = new ObjectOutputStream(byteOStream);
        objOStream.writeObject(objToBeSent);
        byte[] outgoingByteArray            = byteOStream.toByteArray();
        
        //Store Byte Array into the payload of the outgoing packet
        DatagramPacket packet = new DatagramPacket(outgoingByteArray, outgoingByteArray.length, remoteAddress, remotePort);
        
        return packet;
    }    
}
