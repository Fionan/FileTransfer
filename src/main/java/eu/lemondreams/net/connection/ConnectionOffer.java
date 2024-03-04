package eu.lemondreams.net.connection;

import eu.lemondreams.net.tools.IPAddressWithSubnet;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectionOffer implements Serializable {
    private final float VERSION_NO= 1.0f;



    public enum CONNECTION_TYPE {
        SEND, RECEIVE,FAIL,AUTO,ACCEPT

    }
    private double versionNo;
    private CONNECTION_TYPE connectionType;
    private String hostname;
    private UUID uuid;

    private List<IPAddressWithSubnet> ipNetworks;

    public ConnectionOffer(double versionNo,
                           CONNECTION_TYPE connectionType,
                           String hostName,
                           UUID uuid,
                           List<IPAddressWithSubnet> ipNetworks )
    {
        this.versionNo = versionNo;
        this.connectionType =connectionType;
        this.hostname=hostName;
        this.uuid = uuid;
        this.ipNetworks = ipNetworks;

    }

    public ConnectionOffer(CONNECTION_TYPE connectionType, List<IPAddressWithSubnet> ipNetworks) {
        this.versionNo = VERSION_NO;
        this.connectionType =connectionType;
        this.ipNetworks = ipNetworks;
        this.uuid = UUID.randomUUID();

        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Host name not Found");
            this.hostname="undefined";
        }
    }

    // Getters and setters


    public ConnectionOffer(CONNECTION_TYPE connectionType) {
        this.connectionType = connectionType;
        this.versionNo = VERSION_NO;
        this.ipNetworks = new ArrayList<IPAddressWithSubnet>();
        this.uuid=UUID.randomUUID();
        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.err.println("Host name not Found");
            this.hostname="undefined";
        }
    }

    @Override
    public String toString() {
        return "ConnectionNegotiation{" +
                "versionNo=" + versionNo +
                ",connectionDesired=" + connectionType +
                ",hostname='" + hostname + '\'' +
                ",uuid=" + uuid +
                ",ipNetworks=" + ipNetworks +
                '}';
    }


    public float getVERSION_NO() {
        return VERSION_NO;
    }

    public double getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(double versionNo) {
        this.versionNo = versionNo;
    }

    public CONNECTION_TYPE getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(CONNECTION_TYPE connectionType) {
        this.connectionType = connectionType;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<IPAddressWithSubnet> getIpNetworks() {
        return ipNetworks;
    }

    public void setIpNetworks(List<IPAddressWithSubnet> ipNetworks) {
        this.ipNetworks = ipNetworks;
    }
}
