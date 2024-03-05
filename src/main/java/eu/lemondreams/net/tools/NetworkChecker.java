package eu.lemondreams.net.tools;

import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkChecker {

    public static boolean areInSameNetwork(String ipAddress1, String ipAddress2, String subnetMask) {
        InetAddress address1;
        InetAddress address2;

        try {
            address1 = InetAddress.getByName(ipAddress1);
            address2 = InetAddress.getByName(ipAddress2);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            // Handle the exception as needed
            return false;
        }

        if (isCIDRNotation(subnetMask)) {
            int subnetPrefixLength = extractPrefixLength(subnetMask);
            byte[] network1 = calculateNetworkAddress(address1, subnetPrefixLength);
            byte[] network2 = calculateNetworkAddress(address2, subnetPrefixLength);

            return isEqual(network1, network2);
        } else {
            byte[] network1 = calculateNetworkAddress(address1, subnetMask);
            byte[] network2 = calculateNetworkAddress(address2, subnetMask);

            return isEqual(network1, network2);
        }
    }

    private static byte[] calculateNetworkAddress(InetAddress ipAddress, String subnetMask) {
        byte[] ipBytes = ipAddress.getAddress();
        byte[] maskBytes;
        try {
            maskBytes = InetAddress.getByName(subnetMask).getAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        byte[] networkAddress = new byte[ipBytes.length];
        for (int i = 0; i < ipBytes.length; i++) {
            networkAddress[i] = (byte) (ipBytes[i] & maskBytes[i]);
        }

        return networkAddress;
    }

    private static byte[] calculateNetworkAddress(InetAddress ipAddress, int subnetPrefixLength) {
        byte[] ipBytes = ipAddress.getAddress();

        // Calculate subnet mask using prefix length
        byte[] maskBytes = new byte[ipBytes.length];
        for (int i = 0; i < ipBytes.length; i++) {
            int shift = 8 - Math.min(subnetPrefixLength, 8);
            maskBytes[i] = (byte) (0xFF << shift);
            subnetPrefixLength -= 8;
        }

        byte[] networkAddress = new byte[ipBytes.length];
        for (int i = 0; i < ipBytes.length; i++) {
            networkAddress[i] = (byte) (ipBytes[i] & maskBytes[i]);
        }

        return networkAddress;
    }

    private static boolean isEqual(byte[] array1, byte[] array2) {
        if (array1.length != array2.length) {
            return false;
        }

        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }

        return true;
    }

    private static boolean isCIDRNotation(String subnetMask) {
        Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+");
        Matcher matcher = pattern.matcher(subnetMask);
        return matcher.matches();
    }

    private static int extractPrefixLength(String cidr) {
        String[] parts = cidr.split("/");
        return Integer.parseInt(parts[1]);
    }



    public static String extractIPAddressFromCIDR(String cidr) {
        String[] parts = cidr.split("/");
        return parts[0];
    }

    public static String extractSubnetMaskFromCIDR(String cidr) {
        try {
            SubnetUtils subnetUtils = new SubnetUtils(cidr);
            return subnetUtils.getInfo().getNetmask();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid CIDR notation: " + cidr, e);
        }
    }


    public static void main(String[] args) {
        String ipAddress1 = "192.168.5.5";
        String ipAddress2 = "192.168.5.50";
        String subnetMask = "192.168.5.0/16";

        if (areInSameNetwork(ipAddress1, ipAddress2, subnetMask)) {
            System.out.println("The IP addresses are in the same network.");
        } else {
            System.out.println("The IP addresses are not in the same network.");
        }
    }
}

