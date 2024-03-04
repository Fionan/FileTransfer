package eu.lemondreams;

import eu.lemondreams.MenuMaker.Menu;
import eu.lemondreams.MenuMaker.MenuItem;
import eu.lemondreams.MenuMaker.MenuItemCondition;

import eu.lemondreams.net.connection.ConnectionAgreement;
import eu.lemondreams.net.connection.ConnectionOffer;
import eu.lemondreams.net.tools.IPAddressWithSubnet;
import eu.lemondreams.net.tools.NetworkAdvertiser;
import eu.lemondreams.net.tools.NetworkInfoPrinter;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static eu.lemondreams.net.tools.NetworkInfoPrinter.isValidIPv4;

public class InteractiveMenu {
    static final String SEND_CMD = "SEND";
    static final String RECEIVE_CMD = "RECEIVE";
    private static final String MODE_TYPE = "MODE_TYPE";
    private static final String INITIAL_MODE = "UN-DEFINED!";

    private static final String SOCKET_COUNT = "SOCKET_COUNT";

    public static void run() {

        Menu mainMenu = new Menu(Menu.MenuType.MAIN);

        mainMenu.setValue(MODE_TYPE, INITIAL_MODE);

        MenuItem modeTypeMi = new MenuItem("Select Mode Type SEND or RECEIVE", () -> {

            System.out.println("Input SEND MODE (s)end/(r)eceive)");
            String ans = mainMenu.getUserInputString();
            ans = ans.toUpperCase();
            if (ans.contains("S") || ans.contains("R")) {
                //we can continue

                if (ans.contains("S")) {
                    mainMenu.setValue(MODE_TYPE, SEND_CMD);
                    System.out.println("SEND Mode Selected");
                } else {
                    mainMenu.setValue(MODE_TYPE, RECEIVE_CMD);
                    System.out.println("RECEIVE Mode Selected");

                }

            } else {
                System.out.println("Invalid mode chosen");
            }

        });
        mainMenu.addMenuItem(modeTypeMi);

        //mainMenu.addMenuItemWithKVInput(Menu.InputType.INTEGER, "How many sockets will be used", SOCKET_COUNT);
        MenuItem getSocketConnectionCount = new MenuItem("How Many Sockets will be used", () -> {

            int socketCount = mainMenu.getUserInputInt();

            while (socketCount <= 0 || socketCount > 10) {
                System.out.println("Input a valid number of connections (1-10) ");
                socketCount = mainMenu.getUserInputInt();

            }
            mainMenu.setValue(SOCKET_COUNT, socketCount + "");


        }, new MenuItemCondition() {
            @Override
            public boolean isMet() {
                return (mainMenu.getValue(MODE_TYPE) != INITIAL_MODE);
            }
        });

        mainMenu.addMenuItem(getSocketConnectionCount);


        ArrayList<String> ipList = new ArrayList<>();
        MenuItem getIPaddresses = new MenuItem("Input IP addresses", () -> {

            int ipcount = Integer.parseInt(mainMenu.getValue(SOCKET_COUNT));

            for (int i = 1; i <= ipcount; i++) {
                System.out.println("Please input position " + i + " ipv4 address as xxx.xxx.xxx.xxx");
                String s = mainMenu.getUserInputString();

                while (!isValidIPv4(s)) {
                    System.out.println("Error found please input in valid IPv4 address or (e)xit");
                    s = mainMenu.getUserInputString();

                    if (s.toLowerCase().contains("e")) {
                        return;
                    }
                }

                ipList.add(s);

            }

        }, new MenuItemCondition() {
            @Override
            public boolean isMet() {
                return mainMenu.getValue(SOCKET_COUNT) != null;
            }
        });


        mainMenu.addMenuItem(getIPaddresses);


        MenuItem runReceiver = new MenuItem("RECEIVE File", () -> {

            System.out.println("Waiting for connection..");

            String[] ips = new String[ipList.size()];
            ips = ipList.toArray(ips);

            FileTransfer.runReceiver(ips);


        }, new MenuItemCondition() {
            @Override
            public boolean isMet() {
                boolean isValid = false;

                String mode = mainMenu.getValue(MODE_TYPE);

                if (mode.equalsIgnoreCase(RECEIVE_CMD)) {

                    if (ipList.size() > 0) {

                        isValid = true;
                    }

                }

                return isValid;

            }
        });

        mainMenu.addMenuItem(runReceiver);


        MenuItem runSender = new MenuItem(SEND_CMD, () -> {
            String[] ips = new String[ipList.size()];
            ips = ipList.toArray(ips);

            String filepath = getFilePathFromUser(mainMenu);
            FileTransfer.runSender(filepath, ips);


        }, new MenuItemCondition() {
            @Override
            public boolean isMet() {
                boolean isValid = false;

                String mode = mainMenu.getValue(MODE_TYPE);

                if (mode.equalsIgnoreCase(SEND_CMD)) {

                    if (ipList.size() > 0) {

                        isValid = true;
                    }

                }

                return isValid;

            }
        });

        mainMenu.addMenuItem(runSender);

        //add Auto menu
        addAutoMenu(mainMenu);

        //add it ot our config
        addConfigMenu(mainMenu);


        //Lets run our Menu
        mainMenu.run();


    }

    public static void addAutoMenu(Menu mainMenu) {

        Menu auto = new Menu(mainMenu, "Auto SEND / RECEIVE MENU");

        MenuItem autoSend = new MenuItem("Run AUTO send", () -> {

            ArrayList<IPAddressWithSubnet> getLocalIps = IPAddressWithSubnet.getValidIPv4Addresses();

            NetworkAdvertiser networkAdvertiser = new NetworkAdvertiser(FileTransfer.PORT_NUMBER, getLocalIps);

            ConnectionOffer sendCO = new ConnectionOffer(ConnectionOffer.CONNECTION_TYPE.SEND, getLocalIps);

            //Check if we have success
            if (networkAdvertiser.sendBroadcast(sendCO)) {

                ConnectionOffer replYCO = networkAdvertiser.listenForBroadcastWithTimeout(NetworkAdvertiser.DEFAULT_TIMEOUT);

                //TEST
/*
                replYCO = new ConnectionOffer(1.0,
                        ConnectionOffer.CONNECTION_TYPE.RECEIVE,
                        "FionanMain", UUID.randomUUID(),
                        List.of(new IPAddressWithSubnet("192.168.188.26",
                                "255.255.255.0")
                        )
                );
*/
                //END OF TEST


                if (replYCO.getConnectionType() == ConnectionOffer.CONNECTION_TYPE.RECEIVE) {

                    System.out.println("Success !!");

                    // Now we have a successful ConnectionTYpe receive

                    String filepath = getFilePathFromUser(mainMenu);


                    ArrayList<String> ipList = new ArrayList<>();

                    for (IPAddressWithSubnet ipAddressWithSubnet : replYCO.getIpNetworks()) {

                        ipList.add(ipAddressWithSubnet.getIpAddress());

                    }

                    String[] ips = new String[replYCO.getIpNetworks().size()];

                    for (int i = 0; i < ips.length; i++) {
                        ips[i] = replYCO.getIpNetworks().get(i).getIpAddress();


                    }

                    //SEND ing start
                    FileTransfer.runSender(filepath, ips);


                } else {

                    System.out.println("AH here leave it out!!!");

                }


            } else {
                System.out.println("Sometimes packets don't send! Big man");

            }


        });

        auto.addMenuItem(autoSend);


        MenuItem autoReceive = new MenuItem("Auto Receive", () -> {

            ArrayList<IPAddressWithSubnet> getLocalIps = IPAddressWithSubnet.getValidIPv4Addresses();
            NetworkAdvertiser networkAdvertiser = new NetworkAdvertiser(FileTransfer.PORT_NUMBER, getLocalIps);

            ConnectionOffer receiveCO =networkAdvertiser.listenForBroadcastWithTimeout(NetworkAdvertiser.DEFAULT_TIMEOUT);

           if(receiveCO.getConnectionType()== ConnectionOffer.CONNECTION_TYPE.SEND){

               ConnectionAgreement connectionAgreement = new ConnectionAgreement(receiveCO);

               if(connectionAgreement.getLOCAL().getConnectionType()== ConnectionOffer.CONNECTION_TYPE.RECEIVE){
                   //GOLD route
                   networkAdvertiser.sendBroadcast(connectionAgreement.getLOCAL());

                   String[] ips = new String[connectionAgreement.getLOCAL().getIpNetworks().size()];

                   for (int i = 0; i < ips.length; i++) {
                       ips[i] = connectionAgreement.getLOCAL().getIpNetworks().get(i).getIpAddress();

                   }

                   FileTransfer.runReceiver(ips);

               }else{

                   // BAD ROUTE
                   networkAdvertiser.sendBroadcast(connectionAgreement.getLOCAL());

               }



           }




        });

        auto.addMenuItem(autoReceive);

        MenuItem autoMenu = new MenuItem("Auto SEND / RECEIVE MENU", auto);

        mainMenu.addMenuItem(autoMenu);
    }


    public static void addConfigMenu(Menu mainMenu) {
        Menu config = new Menu(mainMenu, "Settings & Config");

        config.addMenuItem(new MenuItem("View Hosts IPv4 addresses", () -> {
            List ipV4AddreesList = IPAddressWithSubnet.getValidIPv4Addresses();
            System.out.println("These are the current ipV4 addresses on this host:\n");
            for (Object o : ipV4AddreesList) {
                System.out.println("" + o);

            }


        }));

        config.addMenuItem("View current port number", () -> {

            System.out.println("The current port Number is : " + FileTransfer.PORT_NUMBER);


        });

        config.addMenuItem("Change Port number", () -> {
            System.out.println("Enter a Port number (49152-65535) :");
            int portNumber = mainMenu.getUserInputInt();

            if (portNumber > 1024 && portNumber < 65535) {
                //check if port is free
                if (NetworkInfoPrinter.isPortAvailable(portNumber)) {

                    FileTransfer.PORT_NUMBER = portNumber;
                } else {
                    System.out.println("Invalid Port specified !");
                }
            } else {
                System.out.println("Invalid Port specified !");
            }

            System.out.println("The current port Number is : " + FileTransfer.PORT_NUMBER);
        });


        MenuItem configMenu = new MenuItem("Settings & Config", config);
        mainMenu.addMenuItem(configMenu);
    }


    public static boolean isValidPath(String pathString) {
        try {
            // Convert the string to a Path object
            Path path = Paths.get(pathString);

            // Check if the path exists
            return Files.exists(path);
        } catch (InvalidPathException | NullPointerException e) {
            // Handle invalid path format or null input
            return false;
        }
    }


    public static String getFilePathFromUser(Menu mainMenu) {
        System.out.println("Please enter full path of the file you wish to send  e.g C:\\Directory\\File.ext");
        String filepath = mainMenu.getUserInputString();

        while (!isValidPath(filepath)) {
            System.out.println("File Not found");
            System.out.println("Please enter full path of the file you wish to send  e.g C:\\Directory\\File.ext");
            filepath = mainMenu.getUserInputString();
        }
        return filepath;
    }


}
