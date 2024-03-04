package eu.lemondreams.net.connection;

import eu.lemondreams.net.tools.NetworkInfoPrinter;

import java.util.ArrayList;
import java.util.List;

import static eu.lemondreams.net.tools.NetworkChecker.*;

public class ConnectionAgreement {


    ConnectionOffer FOREIGN;
    ConnectionOffer LOCAL;

    private List localIpList;


    public ConnectionAgreement(ConnectionOffer FOREIGN) {
        this.FOREIGN = FOREIGN;

        this.localIpList = NetworkInfoPrinter.getIPv4IPList();


        List ipSharedNetworkList = ipMatchCheck(FOREIGN.getIpNetworks());

        if (ipSharedNetworkList.size() > 0) {

            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.RECEIVE,ipSharedNetworkList );


        } else {
            this.LOCAL = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.FAIL);
        }

    }

    private List ipMatchCheck(List foreignIpList) {

        ArrayList<String> validIp = new ArrayList<>();


        for (int i = 0; i < localIpList.size() ; i++) {

            for (int j = 0; j < foreignIpList.size() ; j++) {

                String tmpLocal = (String)localIpList.get(i);
                String tmpForeign = (String)foreignIpList.get(j);

                String localIP = extractIPAddressFromCIDR(tmpLocal);
                String foreignIP = extractIPAddressFromCIDR(tmpForeign);

                String localSubnet = extractSubnetMaskFromCIDR(tmpLocal);
                String foreignSubnet = extractSubnetMaskFromCIDR(tmpForeign);

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
