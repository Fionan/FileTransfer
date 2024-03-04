package eu.lemondreams.net.connection;

import eu.lemondreams.net.tools.IPAddressWithSubnet;
import eu.lemondreams.net.tools.NetworkInfoPrinter;

import java.util.ArrayList;
import java.util.List;

import static eu.lemondreams.net.tools.NetworkChecker.*;

public class ConnectionAgreement {


    ConnectionOffer FOREIGN;
    ConnectionOffer LOCAL;

    private ArrayList<IPAddressWithSubnet> localIpList;


    public ConnectionAgreement(ConnectionOffer FOREIGN) {
        this.FOREIGN = FOREIGN;

        this.localIpList = IPAddressWithSubnet.getValidIPv4Addresses();


        ArrayList<IPAddressWithSubnet> ipSharedNetworkList = ipMatchCheck(FOREIGN.getIpNetworks());

        if(ipSharedNetworkList==null){
            System.err.println("Ip Shared Network = null");
            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.FAIL);
            return;
        }


        if (ipSharedNetworkList.size() > 0) {

            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.RECEIVE,ipSharedNetworkList );


        } else {

        }

    }

    private ArrayList<IPAddressWithSubnet> ipMatchCheck(ArrayList<IPAddressWithSubnet> foreignIpList) {

        ArrayList<String> validIp = new ArrayList<>();


        for (int i = 0; i < localIpList.size() ; i++) {

            for (int j = 0; j < foreignIpList.size() ; j++) {

                String tmpLocal = localIpList.get(i).getIpAddress();
                String tmpForeign = foreignIpList.get(j).getIpAddress();

                System.out.println(tmpLocal +" : "+tmpForeign);

                String localIP = extractIPAddressFromCIDR(tmpLocal);
                String foreignIP = extractIPAddressFromCIDR(tmpForeign);

                System.out.println(localIP + " : "+foreignIP);


                String localSubnet = extractSubnetMaskFromCIDR(String.valueOf(localIpList.get(i)));
                String foreignSubnet = extractSubnetMaskFromCIDR(String.valueOf(foreignIpList.get(j)));





                if(foreignSubnet.equalsIgnoreCase(localSubnet)) {

                    if (areInSameNetwork(localIP, foreignIP, localSubnet)){

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
