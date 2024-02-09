package eu.lemondreams;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransfer {
    private static final int PORT_NUMBER = 9990;

    private static final  HashMap<Integer,String> order_of_files_recieved = new HashMap<>();

    private static String currentFileName= "";
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java FileTransfer <SEND/RECEIVE> <FILE_PATH> <INTERFACE_IP1> [<INTERFACE_IP2> ...]");
            System.exit(1);
        }

        String mode = args[0].toUpperCase();

        String filePath = args[1];
        String[] interfaceIps = Arrays.copyOfRange(args, 2, args.length);

        if (mode.equals("SEND")) {
            runSender(filePath,interfaceIps);
        } else if (mode.equals("RECEIVE")) {
            runReceiver(interfaceIps);
        } else {
            System.out.println("Invalid mode. Use SEND or RECEIVE.");
            System.exit(1);
        }
    }

    public static void runSender( String filePath,String... interfaceIps) {
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
            combineFiles(receiverThreads.size(), currentFileName);

            System.out.println("All receiver connections closed");
        } catch (IOException | InterruptedException e) {
            handleException("Error in runReceiver", e);
        }
    }
@Deprecated
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


    public static void combineFiles(int totalParts, String originalFileName) {
        try {
            List<FileInputStream> partInputStreams = new ArrayList<>();

            // Open FileInputStreams for each part
            for (int i = 1; i <= totalParts; i++) {
                String partFileName = "part" + i + "_" + originalFileName;
                partInputStreams.add(new FileInputStream(partFileName));
            }


            // Open FileOutputStream for the combined file
            try (FileOutputStream fileOutputStream = new FileOutputStream(originalFileName)) {
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
            for (int i = 1; i <= totalParts; i++) {
                String partFileName = "part" + i + "_" + originalFileName;
                new File(partFileName).delete();
            }

            System.out.println("Combined file received and saved as: " + originalFileName);
        } catch (IOException e) {
            handleException("Error in combineFiles", e);
        }
    }



    // Helper method to extract part number from the file name
    private static int extractPartNumber(FileInputStream fileInputStream)  {
        String fileName = null;
        try {
            fileName = ((FileInputStream) fileInputStream).getFD().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int underscoreIndex = fileName.lastIndexOf('_');
        int partNumber = Integer.parseInt(fileName.substring(underscoreIndex + 1, fileName.length() - currentFileName.length()));
        System.out.println("This part is"+ partNumber );


        return partNumber;
    }

    private static void handleException(String message, Exception e) {
        System.err.println(message);
        Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, e);
    }
}
