import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author geoffdabu
 * @author jeffwong
 */
public class ConfigParser {
    
    private int clientPort;
    private int serverPort;
    private int netClientPort;
    private int netServerPort;

    String clientAddress;
    String serverAddress;
    String netAddress;

    int dropRate;
    int windowSize;
    int delayTime;
    int totalPackets;

    public ConfigParser(String configFile) throws Exception {

        Properties prop = new Properties();
        InputStream input = null;

        input = new FileInputStream(configFile);

        // load a properties file
        prop.load(input);

        clientPort = Integer.parseInt(prop.getProperty("clientPort"));
        serverPort = Integer.parseInt(prop.getProperty("serverPort"));
        netClientPort = Integer.parseInt(prop.getProperty("netClientPort"));
        netServerPort = Integer.parseInt(prop.getProperty("netServerPort"));

        clientAddress = prop.getProperty("clientAddress");
        serverAddress = prop.getProperty("serverAddress");
        netAddress = prop.getProperty("netAddress");

        dropRate = Integer.parseInt(prop.getProperty("dropRate"));
        windowSize = Integer.parseInt(prop.getProperty("windowSize"));
        delayTime = Integer.parseInt(prop.getProperty("netDelayTime"));
        totalPackets = Integer.parseInt(prop.getProperty("totalPackets"));
        
        
        input.close();
    }
    
    
    public int getClientPort() {
        return clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getNetClientPort() {
        return netClientPort;
    }

    public int getNetServerPort() {
        return netServerPort;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public int getDropRate() {
        return dropRate;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getDelayTime() {
        return delayTime;
    }
    
    public int getTotalPackets() {
        return totalPackets;
    }
}
