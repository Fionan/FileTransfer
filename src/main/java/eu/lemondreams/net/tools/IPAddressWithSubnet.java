package eu.lemondreams.net.tools;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPAddressWithSubnet implements Serializable {
    private String ipAddress;
    private String subnetMask;

    public IPAddressWithSubnet(String ipAddress, String subnetMask) {
        this.ipAddress = ipAddress;
        this.subnetMask = subnetMask;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }
    public String toStringCIDR() {
        // Get the prefix length from the subnet mask
        int prefixLength = calculatePrefixLength(subnetMask);

        return ipAddress + "/" + prefixLength;
    }

    private int calculatePrefixLength(String subnetMask) {
        String[] maskParts = subnetMask.split("\\.");
        int prefixLength = 0;

        for (String part : maskParts) {
            int octet = Integer.parseInt(part);
            prefixLength += Integer.bitCount(octet); // Count the number of set bits in the octet
        }

        return prefixLength;
    }

    @Override
    public String toString() {
        return ipAddress + "/" + subnetMask;
    }

    public static ArrayList<IPAddressWithSubnet> getValidIPv4Addresses() {
        ArrayList<IPAddressWithSubnet> ipList = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();

                    if (address instanceof java.net.Inet4Address && isValidIPv4(address.getHostAddress())) {
                        InterfaceAddress interfaceAddress = getInterfaceAddress(networkInterface, (java.net.Inet4Address) address);


                        if (address.isLoopbackAddress()) {
                            //System.out.println("Skipping Loopback address: " + address.getHostAddress());

                            continue;

                        }


                        if (interfaceAddress != null) {
                            ipList.add(new IPAddressWithSubnet(address.getHostAddress(), getPrefixLength(interfaceAddress)));
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return ipList;
    }

    private static InterfaceAddress getInterfaceAddress(NetworkInterface networkInterface, java.net.Inet4Address address) {
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            if (interfaceAddress.getAddress().equals(address)) {
                return interfaceAddress;
            }
        }
        return null;
    }

    private static String getPrefixLength(InterfaceAddress interfaceAddress) {
        return String.valueOf(interfaceAddress.getNetworkPrefixLength());
    }

    public static boolean isValidIPv4(String ipAddress) {
        // Define the IPv4 address pattern
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // Compile the regular expression
        Pattern pattern = Pattern.compile(ipv4Pattern);

        // Create a matcher with the given IP address
        Matcher matcher = pattern.matcher(ipAddress);

        // Check if the IP address matches the pattern
        return matcher.matches();
    }

    public static void main(String[] args) {
        ArrayList<IPAddressWithSubnet> ipList = getValidIPv4Addresses();

        // Print the result
        for (IPAddressWithSubnet ipAddress : ipList) {
            System.out.println(ipAddress);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPAddressWithSubnet that = (IPAddressWithSubnet) o;
        return Objects.equals(ipAddress, that.ipAddress) && Objects.equals(subnetMask, that.subnetMask);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, subnetMask);
    }
}
