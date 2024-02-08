package eu.lemondreams;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileTransfer {
    private static final int PORT_NUMBER = 9990;

    private static String currentFileName= "";
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java FileTransfer <SEND/RECEIVE> <INTERFACE_IP1> <INTERFACE_IP2> <FILE_PATH>");
            System.exit(1);
        }

        String mode = args[0].toUpperCase();

        String filePath = args[1];
        String interfaceIp1 = args[2];
        String interfaceIp2 = args[3];


        if (mode.equals("SEND")) {
            runSender(interfaceIp1, interfaceIp2, filePath);
        } else if (mode.equals("RECEIVE")) {
            runReceiver();
        } else {
            System.out.println("Invalid mode. Use SEND or RECEIVE.");
            System.exit(1);
        }
    }

    public static void runSender(String interfaceIp1, String interfaceIp2, String filePath) {
        try {
            // Establish separate sockets for each network interface
            Socket socket1 = new Socket(interfaceIp1, PORT_NUMBER);
            Socket socket2 = new Socket(interfaceIp2, PORT_NUMBER + 1);

            // Check if the file exists and can be read
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                System.err.println("Error: File does not exist or cannot be read.");
                return;
            }

            // Create threads for sending on each socket
            Thread thread1 = new Thread(() -> sendFile(socket1, filePath, 0, 0.5));
            Thread thread2 = new Thread(() -> sendFile(socket2, filePath, 0.5, 1.0));

            // Start both threads
            thread1.start();
            thread2.start();

            // Wait for both threads to finish
            thread1.join();
            thread2.join();

            // Close sockets
            socket1.close();
            socket2.close();

            System.out.println("Connection closed");
        } catch (IOException | InterruptedException e) {
            handleException("Error in runSender", e);
        }
    }

    public static void runReceiver() {
        try {
            ServerSocket serverSocket1 = new ServerSocket(PORT_NUMBER);
            ServerSocket serverSocket2 = new ServerSocket(PORT_NUMBER + 1);

            System.out.println("Server listening on ports " + PORT_NUMBER + " and " + (PORT_NUMBER + 1));

            boolean running = true;

            while (running) {
                // Accept connections on both sockets
                Socket clientSocket1 = serverSocket1.accept();
                Socket clientSocket2 = serverSocket2.accept();

                System.out.println("Connected to client: " + clientSocket1.getInetAddress() + " on port " + clientSocket1.getPort());
                System.out.println("Connected to client: " + clientSocket2.getInetAddress() + " on port " + clientSocket2.getPort());

                // Create threads for receiving on each socket
                Thread thread1 = new Thread(() -> receiveFile(clientSocket1, "part1_"));
                Thread thread2 = new Thread(() -> receiveFile(clientSocket2, "part2_"));

                // Start both threads
                System.out.println("Starting Threads");
                thread1.start();
                thread2.start();

                // Wait for both threads to finish
                thread1.join();
                thread2.join();

                System.out.println("Threads should have joined now");

                clientSocket1.close();
                clientSocket2.close();

                System.out.println("Connections closed");
                running = false;

                combineFiles("part1_","part2_",currentFileName,currentFileName);

            }
        } catch (IOException | InterruptedException e) {
            handleException("Error in runReceiver", e);
        }
    }

    private static void sendFile(Socket socket, String filePath, double startPercentage, double endPercentage) {
        try {
            synchronized (socket.getOutputStream()) {
                OutputStream outputStream = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                // Send file name and size
                File file = new File(filePath);
                objectOutputStream.writeUTF(file.getName());
                objectOutputStream.writeLong(file.length());

                System.out.println("Sending file: " + file.getName() + " (" + file.length() + " bytes)");

                // Calculate start and end positions for the specified percentage range
                long startByte = calculateFileDivision(file.length(), startPercentage);
                long endByte = calculateFileDivision(file.length(), endPercentage);

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
                System.out.println("File sent successfully: " + file.getName());
            }
        } catch (IOException e) {
            handleException("Error in sendFile", e);
        }
    }

    private static long calculateFileDivision(long fileSize, double percentage) {
        return (long) (fileSize * percentage);
    }



    private static void combineFiles(String part1Prefix, String part2Prefix, String originalFileName, String combinedFileName) {

        String part1FileName = part1Prefix + originalFileName;
        String part2FileName = part2Prefix + originalFileName;

        System.out.println(part1Prefix);
        System.out.println(part2Prefix);

        try (FileOutputStream fileOutputStream = new FileOutputStream(combinedFileName);
             FileInputStream part1InputStream = new FileInputStream(part1FileName);
             FileInputStream part2InputStream = new FileInputStream(part2FileName)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Write content of part1 file to combined file
            while ((bytesRead = part1InputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            // Write content of part2 file to combined file
            while ((bytesRead = part2InputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            handleException("Error in combineFiles", e);
        }

        // Delete temporary part files
        new File(part1FileName).delete();
        new File(part2FileName).delete();
    }


    private static void receiveFile(Socket socket, String partPrefix) {
        try {
            synchronized (socket.getInputStream()) {
                InputStream inputStream = socket.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                // Read file name and size
                String fileName = objectInputStream.readUTF();
                currentFileName = fileName;
            long fileSize = objectInputStream.readLong();

                System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

                // Read file content
                byte[] buffer = new byte[1024];
                FileOutputStream fileOutputStream = new FileOutputStream(partPrefix + fileName);
                int bytesRead;

                while ((bytesRead = objectInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                fileOutputStream.close();
                System.out.println("File part received successfully: " + fileName);
            }
        } catch (IOException e) {
            handleException("Error in receiveFile", e);
        }
    }



    private static void handleException(String message, Exception e) {
        System.err.println(message);
        Logger.getLogger(FileTransfer.class.getName()).log(Level.SEVERE, null, e);
    }
}
