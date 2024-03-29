package eu.lemondreams.net.tools;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkInfoPrinter {


     public static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // If the server socket is successfully created, the port is available
            return true;
        } catch (Exception e) {
            // Port is not available (either in use or other issues)
            return false;
        }
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

}
