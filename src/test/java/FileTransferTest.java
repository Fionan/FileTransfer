import static eu.lemondreams.FileTransfer.combineFiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.jupiter.api.Test;

import eu.lemondreams.FileTransfer;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.io.*;
import java.util.Arrays;
import java.util.List;

class FileTransferTest {

    // Replace these with actual file paths and interface IPs for testing
    private static final String FILE_PATH = FileTransferTest.class.getResource("testFile1.txt").getPath().toString();
    private static final String INTERFACE_IP1 = "127.0.0.1";
    private static final String INTERFACE_IP2 = "127.0.0.1";

    private static final String INTERFACE_IP3 = "192.168.188.26";
    private static final String INTERFACE_IP4 = "192.168.188.28";

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Test
    void testSendAndReceiveFile() {
        // Create a new thread for the sender
        Thread senderThread = new Thread(() -> {
            FileTransfer.runSender(FILE_PATH,INTERFACE_IP3, INTERFACE_IP4 );
        });

        // Create a new thread for the receiver
        Thread receiverThread = new Thread(() -> {
            FileTransfer.runReceiver(INTERFACE_IP1,INTERFACE_IP2);
        });

        // Start both threads
        senderThread.start();
        receiverThread.start();

        // Wait for both threads to finish
        try {
            senderThread.join();
            receiverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Introduce a short delay to allow for file combination
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if the combined file is created successfully
        String combinedFilePath = "testFile1.txt";
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



    @Test
    void testCombineFiles() throws IOException {
        int totalParts = 3;
        String originalFileName = "testFile.txt";

        // Mock FileInputStreams
        FileInputStream part1Stream = mock(FileInputStream.class);
        FileInputStream part2Stream = mock(FileInputStream.class);
        FileInputStream part3Stream = mock(FileInputStream.class);

        // Mock FileOutputStream
        FileOutputStream fileOutputStream = mock(FileOutputStream.class);

        // Mock File objects
        File part1File = mock(File.class);
        File part2File = mock(File.class);
        File part3File = mock(File.class);

        // Stub FileInputStream behavior
        when(part1File.getName()).thenReturn("part1_" + originalFileName);
        when(part2File.getName()).thenReturn("part2_" + originalFileName);
        when(part3File.getName()).thenReturn("part3_" + originalFileName);

        // Stub FileInputStream read behavior
        when(part1Stream.read(any(byte[].class))).thenReturn(1024, -1);
        when(part2Stream.read(any(byte[].class))).thenReturn(1024, -1);
        when(part3Stream.read(any(byte[].class))).thenReturn(1024, -1);

        // Mock File list
        List<FileInputStream> partStreams = Arrays.asList(part1Stream, part2Stream, part3Stream);

        // Mock File.listFiles() behavior
        when(part1File.listFiles()).thenReturn(new File[]{});
        when(part2File.listFiles()).thenReturn(new File[]{});
        when(part3File.listFiles()).thenReturn(new File[]{});

        // Mock new File behavior
        whenNew(FileInputStream.class).thenReturn(part1Stream, part2Stream, part3Stream);
        whenNew(File.class).thenReturn(part1File, part2File, part3File);

        // Mock FileOutputStream behavior
        whenNew(FileOutputStream.class).thenReturn(fileOutputStream);

        // Run the method
        combineFiles(totalParts);

        // Verify interactions
        verify(part1Stream, times(1)).read(any(byte[].class));
        verify(part2Stream, times(1)).read(any(byte[].class));
        verify(part3Stream, times(1)).read(any(byte[].class));
        verify(fileOutputStream, times(3)).write(any(byte[].class), anyInt(), anyInt());
        verify(part1Stream, times(1)).close();
        verify(part2Stream, times(1)).close();
        verify(part3Stream, times(1)).close();
        verify(fileOutputStream, times(1)).close();

        // Verify that the combined file is created
        assertTrue(new java.io.File(originalFileName).exists());
    }



    // Helper method to capture System.out.println output
    private String systemOut() {
        return outputStream.toString().trim();
    }
}
