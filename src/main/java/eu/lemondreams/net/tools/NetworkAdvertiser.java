package eu.lemondreams.net.tools;

import eu.lemondreams.net.connection.ConnectionOffer;
import org.apache.commons.net.util.SubnetUtils;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkAdvertiser {
    public static final int DEFAULT_TIMEOUT = 5;
    private final int PORT;
    private List<IPAddressWithSubnet> ipAddresses;

    public NetworkAdvertiser(int PORT, List<IPAddressWithSubnet> ipAddresses) {
        this.PORT = PORT;
        this.ipAddresses = ipAddresses;
    }


    public boolean sendBroadcast(ConnectionOffer negotiation) {
        try {


            // Iterate over the list of CIDR addresses
            for (IPAddressWithSubnet cidr : negotiation.getIpNetworks()) {

                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);

                //safety check on loopback address
                if (cidr.getIpAddress().equalsIgnoreCase("127.0.0.1")) {
                    continue;
                }

                SubnetUtils subnetUtils = new SubnetUtils(cidr.toString());
                String broadcastAddress = subnetUtils.getInfo().getBroadcastAddress();

                InetAddress broadcastInetAddress = InetAddress.getByName(broadcastAddress);

                oos.writeObject(negotiation);
                oos.flush();

                byte[] data = byteArrayOutputStream.toByteArray();

                DatagramPacket packet = new DatagramPacket(data, data.length, broadcastInetAddress, PORT);
                System.out.println("UDP packet sent to " + broadcastInetAddress + " on " + PORT + "  now...");
                socket.send(packet);

                // Reset the ByteArrayOutputStream for the next iteration
                byteArrayOutputStream.reset();

                oos.close();
                socket.close();
            }


            System.out.println("Sent.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Method to listen for broadcast in PASSIVE mode
    public ConnectionOffer listenForBroadcastWithTimeout(int timeoutSeconds) {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));

            byte[] buffer = new byte[1024];

            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(timeoutSeconds)) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    socket.receive(packet);

                    byte[] receivedData = packet.getData();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivedData);
                    ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);

                    try {
                        ConnectionOffer negotiation = (ConnectionOffer) ois.readObject();
                        System.out.println("Received ConnectionNegotiation: " + negotiation);
                        return negotiation;


                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    ois.close();
                } catch (SocketTimeoutException e) {

                    socket.close();
                    return new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.FAIL);

                }
            }

            socket.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.FAIL);
    }


    // Method for the PAIR-Join mode
    public void pairJoin() {
        // Implement the logic for pairing hosts and agreeing on shared networks
    }

    public static void main(String[] args) throws InterruptedException {
        List<IPAddressWithSubnet> ipAddresses = new ArrayList<>();
        ipAddresses.add(new IPAddressWithSubnet("10.17.17.4", "255.255.255.0"));
        //ipAddresses.add("192.168.188.26/24");
        // Add more IP addresses as needed

        NetworkAdvertiser networkAdvertiser = new NetworkAdvertiser(9990, ipAddresses);
        //public ConnectionNegotiation(List<String> ipNetworks, UUID uuid, double versionNo)
        ConnectionOffer cn = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.SEND, ipAddresses);


        // Example: Send broadcast in ACTIVE mode
        Thread thread = new Thread(() -> {
            networkAdvertiser.listenForBroadcastWithTimeout(30);
        });
        thread.start();

        networkAdvertiser.sendBroadcast(cn);

        // Example: Listen for broadcast in PASSIVE mode (you may run this in a separate thread)
        // networkAdvertiser.listenForBroadcast();

        // Example: Execute PAIR-Join mode
        // networkAdvertiser.pairJoin();
    }
}
