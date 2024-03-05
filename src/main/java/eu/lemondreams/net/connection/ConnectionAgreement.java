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

    @Deprecated
    private ArrayList<IPAddressWithSubnet> ipMatchCheck_old(ArrayList<IPAddressWithSubnet> foreignIpList) {

        ArrayList<String> validIp = new ArrayList<>();


        for (int i = 0; i < localIpList.size(); i++) {

            for (int j = 0; j < foreignIpList.size(); j++) {

                String tmpLocal = localIpList.get(i).getIpAddress();
                String tmpForeign = foreignIpList.get(j).getIpAddress();

                System.out.println(tmpLocal + " : " + tmpForeign);

                String localIP = extractIPAddressFromCIDR(tmpLocal);
                String foreignIP = extractIPAddressFromCIDR(tmpForeign);

                System.out.println(localIP + " : " + foreignIP);


                String localSubnet = extractSubnetMaskFromCIDR(String.valueOf(localIpList.get(i)));
                String foreignSubnet = extractSubnetMaskFromCIDR(String.valueOf(foreignIpList.get(j)));


                if (foreignSubnet.equalsIgnoreCase(localSubnet)) {

                    if (areInSameNetwork(localIP, foreignIP, localSubnet)) {

                        validIp.add(tmpLocal);

                    }

                }
            }

        }

        return null;

    }

    public ConnectionOffer getLOCAL() {
        return LOCAL;
    }

    public void setLOCAL(ConnectionOffer LOCAL) {
        this.LOCAL = LOCAL;
    }
}
