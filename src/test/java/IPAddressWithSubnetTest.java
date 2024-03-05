import eu.lemondreams.net.tools.IPAddressWithSubnet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

import eu.lemondreams.net.tools.IPAddressWithSubnet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class IPAddressWithSubnetTest {

    @Test
    public void testGetValidIPv4Addresses() {
        ArrayList<IPAddressWithSubnet> ipList = IPAddressWithSubnet.getValidIPv4Addresses();

        // Check that the list is not null
        assertNotNull(ipList);

        // Check that each IP address in the list is valid
        for (IPAddressWithSubnet ipAddress : ipList) {
            assertTrue(IPAddressWithSubnet.isValidIPv4(ipAddress.getIpAddress()));
        }
    }

    @Test
    public void testIsValidIPv4() {
        assertTrue(IPAddressWithSubnet.isValidIPv4("192.168.1.1"));
        assertTrue(IPAddressWithSubnet.isValidIPv4("10.0.0.1"));
        assertTrue(IPAddressWithSubnet.isValidIPv4("255.255.255.255"));

        assertFalse(IPAddressWithSubnet.isValidIPv4("192.168.1.256")); // Invalid octet
        assertFalse(IPAddressWithSubnet.isValidIPv4("192.168.1")); // Incomplete address
        assertFalse(IPAddressWithSubnet.isValidIPv4("192.168.1.1.1")); // Extra octet
        assertFalse(IPAddressWithSubnet.isValidIPv4("192.168.1.a")); // Non-numeric character
        assertFalse(IPAddressWithSubnet.isValidIPv4("300.200.100.50")); // Out of range octet
    }

    @Test
    public void testToString() {
        IPAddressWithSubnet ipAddress = new IPAddressWithSubnet("192.168.1.1", "255.255.255.0");
        assertEquals("192.168.1.1/255.255.255.0", ipAddress.toString());
    }

    @Test
    public void testGetIpAddress() {
        IPAddressWithSubnet ipAddress = new IPAddressWithSubnet("192.168.1.1", "255.255.255.0");
        assertEquals("192.168.1.1", ipAddress.getIpAddress());
    }

    @Test
    public void testGetSubnetMask() {
        IPAddressWithSubnet ipAddress = new IPAddressWithSubnet("192.168.1.1", "255.255.255.0");
        assertEquals("255.255.255.0", ipAddress.getSubnetMask());
    }

    @Test
    public void testToStringCIDR() {
        IPAddressWithSubnet ipAddress = new IPAddressWithSubnet("192.168.10.5", "255.255.0.0");
        assertEquals("192.168.10.5/16", ipAddress.toStringCIDR());


        IPAddressWithSubnet ipAddress2 = new IPAddressWithSubnet("192.168.10.5", "255.255.255.0");
        assertEquals("192.168.10.5/24", ipAddress2.toStringCIDR());


        IPAddressWithSubnet ipAddress3 = new IPAddressWithSubnet("10.17.10.5", "255.0.0.0");
        assertEquals("10.17.10.5/8", ipAddress3.toStringCIDR());

    }

}

