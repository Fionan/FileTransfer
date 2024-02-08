import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import eu.lemondreams.FileTransfer;

class FileTransferTest {

    // Replace these with actual file paths and interface IPs for testing
    private static final String FILE_PATH = FileTransferTest.class.getResource("testFile1.txt").getPath().toString();
    private static final String INTERFACE_IP1 = "localhost";
    private static final String INTERFACE_IP2 = "localhost";

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Test
    void testSendAndReceiveFile() {
        // Create a new thread for the sender
        Thread senderThread = new Thread(() -> {
            FileTransfer.runSender(INTERFACE_IP1, INTERFACE_IP2, FILE_PATH);
        });

        // Create a new thread for the receiver
        Thread receiverThread = new Thread(() -> {
            FileTransfer.runReceiver();
        });

        // Start both threads
        senderThread.start();
        receiverThread.start();

        // Wait for both threads to finish
        try {
            senderThread.join();
            System.out.println("Sender completed");

            receiverThread.join();
            System.out.println("Reciever completed");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the combined file is created successfully
        String combinedFilePath = "combinedFile.txt";
        assertTrue(new java.io.File(combinedFilePath).exists());

        // Additional assertions can be added based on specific requirements
    }

    @Test
    void testInvalidFilePath() {
        // Provide an invalid file path
        String invalidFilePath = "invalid/path/to/file.txt";

        // Create a new thread for the sender with an invalid file path
        Thread senderThread = new Thread(() -> {
            FileTransfer.runSender(INTERFACE_IP1, INTERFACE_IP2, invalidFilePath);
        });

        // Start the sender thread
        senderThread.start();

        // Wait for the sender thread to finish
        try {
            senderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Assert that an error message is printed for an invalid file path
        assertTrue(systemOut().contains("Error: File does not exist or cannot be read."));
    }

    // Helper method to capture System.out.println output
    private String systemOut() {
        return outputStream.toString().trim();
    }
}
