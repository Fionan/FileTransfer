package eu.lemondreams;

import com.sun.tools.javac.Main;
import eu.lemondreams.MenuMaker.Menu;
import eu.lemondreams.MenuMaker.MenuItem;
import eu.lemondreams.MenuMaker.MenuItemCondition;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileTransfer {
    private static int PORT_NUMBER = 59595;
    private static final String SEND_CMD = "SEND";
    private static final String RECEIVE_CMD = "RECEIVE";
    private static final String MODE_TYPE = "MODE_TYPE";
    private static final String INITIAL_MODE = "UN-DEFINED!";

    private static final String SOCKET_COUNT = "SOCKET_COUNT";
    private static final HashMap<Integer, String> order_of_files_received = new HashMap<>();

    private static String currentFileName = "";


    public static void main(String[] args) {
        if (args.length == 0) {
            interactiveMenu();
        } else {

            if (args.length < 3) {
                System.out.println("Usage: java FileTransfer <SEND/RECEIVE> <FILE_PATH> <INTERFACE_IP1> [<INTERFACE_IP2> ...]");
                System.exit(1);
            }

            String mode = args[0].toUpperCase();

            String filePath = args[1];
            String[] interfaceIps = Arrays.copyOfRange(args, 2, args.length);

            if (mode.equals(SEND_CMD)) {
                runSender(filePath, interfaceIps);
            } else if (mode.equals(RECEIVE_CMD)) {
                runReceiver(interfaceIps);
            } else {
                System.out.println("Invalid mode. Use SEND or RECEIVE.");
                System.exit(1);
            }

        }

    }

    public static void interactiveMenu() {

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

            runReceiver(ips);


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

            System.out.println("Please enter full path of the file you wish to send  e.g C:\\Directory\\File.ext");
            String filepath = mainMenu.getUserInputString();

            while (!isValidPath(filepath)) {
                System.out.println("File Not found");
                System.out.println("Please enter full path of the file you wish to send  e.g C:\\Directory\\File.ext");
                filepath = mainMenu.getUserInputString();
            }


            runSender(filepath, ips);


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

        Menu config = new Menu(mainMenu, "Settings & Config");

        config.addMenuItem(new MenuItem("View Hosts IPv4 addresses", () -> {
            List ipV4AddreesList = NetworkInfoPrinter.getIPv4IPList();
            System.out.println("These are the current ipV4 addresses on this host:\n");
            for (Object o : ipV4AddreesList) {
                System.out.println("" + o);

            }


        }));

        config.addMenuItem("View current port number", () -> {

            System.out.println("The current port Number is : " + PORT_NUMBER);


        });

        config.addMenuItem("Change Port number", () -> {
            System.out.println("Enter a Port number (49152-65535) :");
            int portNumber = mainMenu.getUserInputInt();

            if (portNumber > 1024 && portNumber < 65535) {
                //check if port is free
                if (NetworkInfoPrinter.isPortAvailable(portNumber)) {

                    PORT_NUMBER = portNumber;
                } else {
                    System.out.println("Invalid Port specified !");
                }
            } else {
                System.out.println("Invalid Port specified !");
            }

            System.out.println("The current port Number is : " + PORT_NUMBER);
        });


        MenuItem configMenu = new MenuItem("Settings & Config", config);
        mainMenu.addMenuItem(configMenu);

        mainMenu.run();


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


    private static List getIPCount(int count) {

        List ipNames = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ipNames.add("IP:" + i);
        }

        return ipNames;
    }


    public static void runSender(String filePath, String... interfaceIps) {
        try {
            List<Thread> senderThreads = new ArrayList<>();
            int totalInterfaces = interfaceIps.length;

            // Get the file size
            long fileSize = new File(filePath).length();

            // Calculate the chunk size for each interface
            long chunkSize = fileSize / totalInterfaces;

            // Create a thread for each interface
            for (int i = 0; i < totalInterfaces; i++) {
                final int index = i;

                // Calculate start and end positions for the chunk
                long startByte = index * chunkSize;
                long endByte = (index + 1 == totalInterfaces) ? fileSize : (index + 1) * chunkSize;

                int finalI = i;
                Thread thread = new Thread(() -> sendFile(interfaceIps[index], filePath, startByte, endByte, finalI));
                senderThreads.add(thread);
                thread.start();
            }

            // Wait for all sender threads to finish
            for (Thread thread : senderThreads) {
                thread.join();
            }

            System.out.println("All sender connections closed");
        } catch (InterruptedException e) {
            handleException("Error in runSender", e);
        }
    }

    private static void sendFile(String interfaceIp, String filePath, long startByte, long endByte, int partNumber) {
        try {
            Socket socket = new Socket(interfaceIp, PORT_NUMBER);

            synchronized (socket.getOutputStream()) {
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                // Send file name and size
                File file = new File(filePath);
                String fileNameWithPart = "part" + partNumber + "_" + file.getName();
                objectOutputStream.writeUTF(fileNameWithPart);
                objectOutputStream.writeLong(file.length());

                System.out.println("Sending file: " + fileNameWithPart + " (" + file.length() + " bytes)");

                // Send file content from start to end positions
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    fileInputStream.skip(startByte); // Move to the start position

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    long bytesRemaining = endByte - startByte;

                    while (bytesRemaining > 0 && (bytesRead = fileInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                        objectOutputStream.write(buffer, 0, bytesRead);
                        bytesRemaining -= bytesRead;
                    }
                }

                objectOutputStream.flush();
                System.out.println("File sent successfully: " + fileNameWithPart);

                socket.close();
            }
        } catch (IOException e) {
            handleException("Error in sendFile", e);
        }
    }

    public static void runReceiver(String... interfaceIps) {
        try {
            // Create a ServerSocket on the primary port
            ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
            System.out.println("Server listening on port " + PORT_NUMBER);

            List<Thread> receiverThreads = new ArrayList<>();

            // Accept connections on the main ServerSocket
            for (int i = 0; i < interfaceIps.length; i++) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connected to client: " + clientSocket.getInetAddress() + " on port " + clientSocket.getPort());

                final int index = i;
                Thread thread = new Thread(() -> receiveFile(clientSocket, "part" + index + "_"));

                receiverThreads.add(thread);
            }

            // Start all receiver threads
            for (Thread thread : receiverThreads) {
                thread.start();
            }

            // Wait for all receiver threads to finish
            for (Thread thread : receiverThreads) {
                thread.join();
            }

            // Close the main ServerSocket
            serverSocket.close();

            // Combine files after all receiver threads have finished
            combineFiles(receiverThreads.size());

            System.out.println("All receiver connections closed");
        } catch (IOException | InterruptedException e) {
            handleException("Error in runReceiver", e);
        }
    }

    @Deprecated
/*
    private static void receive(ServerSocket serverSocket) {
        try {
            List<Thread> threads = new ArrayList<>();

            // Accept connections on the main ServerSocket
            Socket clientSocket = serverSocket.accept();

            System.out.println("Connected to client: " + clientSocket.getInetAddress() + " on port " + clientSocket.getPort());

            // Create thread for receiving on the accepted socket
            Thread thread = new Thread(() -> receiveFile(clientSocket, "part"));
            threads.add(thread);
            thread.start();

            // Wait for the receiver thread to finish
            for (Thread t : threads) {
                t.join();
            }

            // Combine files after all receiver threads have finished
            combineFiles(threads.size(), currentFileName);
        } catch (IOException | InterruptedException e) {
            handleException("Error in receive", e);
        }
    }

*/

    private static void receiveFile(Socket socket, String partPrefix) {
        try {
            synchronized (socket.getInputStream()) {
                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // Read file name and size
                String fileNameWithPart = objectInputStream.readUTF();
                long fileSize = objectInputStream.readLong();

                // Extract part number and original file name
                String[] fileNameParts = fileNameWithPart.split("_", 2);
                int partNumber = Integer.parseInt(fileNameParts[0].substring(4)); // assuming "part" prefix
                String originalFileName = fileNameParts[1];

                System.out.println("Receiving file part " + partNumber + " of: " + originalFileName + " (" + fileSize + " bytes)");

                // Read file content
                byte[] buffer = new byte[1024];
                FileOutputStream fileOutputStream = new FileOutputStream(fileNameWithPart);
                int bytesRead;

                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                System.out.println("File part received successfully: " + fileNameWithPart);
            }
        } catch (IOException e) {
            handleException("Error in receiveFile", e);
        }
    }


    public static void combineFiles(int totalParts) {
        try {
            List<FileInputStream> partInputStreams = new ArrayList<>();

            // Get a list of part files based on the naming convention
            List<File> partFiles = new ArrayList<>();
            for (int i = 0; i < totalParts; i++) {
                String partFileName = "part" + i + "_";
                File[] files = new File(".").listFiles((dir, name) -> name.startsWith(partFileName));
                if (files != null && files.length > 0) {
                    partFiles.add(files[0]); // Assuming there's only one file matching the prefix
                }
            }

            // Sort part files based on part number
            partFiles.sort(Comparator.comparingInt(f -> extractPartNumber(f.getName())));

            // Open FileInputStreams for each part
            for (File partFile : partFiles) {
                partInputStreams.add(new FileInputStream(partFile));
            }

            // Open FileOutputStream for the combined file
            try (FileOutputStream fileOutputStream = new FileOutputStream(partFiles.get(0).getName().substring(6))) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Write content of all parts to the combined file
                for (FileInputStream partInputStream : partInputStreams) {
                    while ((bytesRead = partInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    partInputStream.close(); // Close each part's input stream
                }
            }

            // Delete temporary part files
            for (File partFile : partFiles) {
                partFile.delete();
            }

            System.out.println("Combined file received and saved as: " + partFiles.get(0).getName().substring(5));
        } catch (IOException e) {
            handleException("Error in combineFiles", e);
        }
    }

    // Helper method to extract the part number from the file name
    private static int extractPartNumber(String fileName) {
        int underscoreIndex = fileName.indexOf('_');
        int partIndex = fileName.indexOf("part") + 4;
        return Integer.parseInt(fileName.substring(partIndex, underscoreIndex));
    }


    // Helper method to extract part number from the file name
    private static int extractPartNumber(FileInputStream fileInputStream) {
        String fileName = null;
        try {
            fileName = ((FileInputStream) fileInputStream).getFD().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int underscoreIndex = fileName.lastIndexOf('_');
        int partNumber = Integer.parseInt(fileName.substring(underscoreIndex + 1, fileName.length() - currentFileName.length()));
        System.out.println("This part is" + partNumber);


        return partNumber;
    }

    private static void handleException(String message, Exception e) {
        System.err.println(message);
        Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, e);
    }
}
