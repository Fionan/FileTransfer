package eu.lemondreams.net.connection;

import eu.lemondreams.net.tools.IPAddressWithSubnet;

import java.util.ArrayList;

import static eu.lemondreams.net.tools.NetworkChecker.*;

public class ConnectionAgreement {


    ConnectionOffer FOREIGN;
    ConnectionOffer LOCAL;

    private ArrayList<IPAddressWithSubnet> localIpList;


    public ConnectionAgreement(ConnectionOffer FOREIGN) {
        this.FOREIGN = FOREIGN;

        this.localIpList = IPAddressWithSubnet.getValidIPv4Addresses();


        ArrayList<IPAddressWithSubnet> ipSharedNetworkList = ipMatchCheck(FOREIGN.getIpNetworks());

        if (ipSharedNetworkList == null) {
            System.err.println("Ip Shared Network = null");
            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.FAIL);
            return;
        }


        if (ipSharedNetworkList.size() > 0) {

            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.RECEIVE, ipSharedNetworkList);


        } else {

        }

    }

    private ArrayList<IPAddressWithSubnet> ipMatchCheck(ArrayList<IPAddressWithSubnet> foreignIpList) {

        ArrayList<IPAddressWithSubnet> validIps = new ArrayList<>();

        for (int i = 0; i < localIpList.size(); i++) {
            for (int j = 0; j < foreignIpList.size(); j++) {

                IPAddressWithSubnet localIP = localIpList.get(i);

                IPAddressWithSubnet foreignIP = foreignIpList.get(j);


                String localSubnet = extractSubnetMaskFromCIDR(localIP.toString());
                String foreignSubnet = extractSubnetMaskFromCIDR(foreignIP.toString());

                if (areInSameNetwork(localIP.getIpAddress(), foreignIP.getIpAddress(), localSubnet)) {
                    if (areInSameNetwork(localIP.getIpAddress(), foreignIP.getIpAddress(), foreignSubnet)) {
                        validIps.add(localIP);
                    }
                }


            }
        }


        return validIps;

    }

    public ConnectionOffer getLOCAL() {
        return LOCAL;
    }

    public void setLOCAL(ConnectionOffer LOCAL) {
        this.LOCAL = LOCAL;
    }
}
