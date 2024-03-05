FileTransfer

FileTransfer is a command-line, menu-driven program designed to facilitate the transfer of files between two devices on a local network using one or more shared networks. The program is particularly useful for low-speed networks with multiple network connections, allowing files to be split and rejoined over the networks.
Modes of Operation
Mode 1: Command Line Arguments

To use the program with command line arguments, users should follow the syntax:

bash

java -jar FileTransfer.jar <SEND/RECEIVE> <FILE_PATH> <INTERFACE_IP1> [<INTERFACE_IP2> ...]

Example:

    Sender (HOST1): java -jar FileTransfer.jar SEND C:\INPUT\Directory\File.dat 172.16.10.5 192.168.0.5
    Receiver (HOST2): java -jar FileTransfer.jar RECEIVE C:\OUTPUT\Directory\File.dat 172.16.10.1 192.168.0.51

Ensure that IPs are known, and each host has a valid route to the other to avoid data corruption or transmission failures.
Mode 2: Manual

Similar to command line limitation, users may utilize the menu to view host IP addresses. Additionally, users have the option to change the port number in the settings.
Mode 3: Auto Mode

In Auto Mode, users can select either auto send or auto receive. The program first broadcasts a message over all local networks, indicating the IP addresses and networks they are part of. It then waits briefly for a broadcast reply. If successful, the file is sent to matching networks only.

Example:

    HOST1 (part of networks): 192.168.0.1/24, 172.16.0.3/24, 10.10.10.5/16
    HOST2 (part of networks): 192.168.0.5/24, 10.10.10.15/16

    HOST1 sends a broadcast.
    HOST2 responds with its IPs in the matching network.
    HOST1 sends the file split into parts.
    HOST2 joins and saves the received file.

License

This software is free to use. The creator is not responsible for any consequences resulting from its usage.
Created by

    Creator: <username>
    Date: <today's date>

Additional Information

Feel free to explore and modify the code as needed. Contributions and feedback are welcome. If you encounter any issues or have suggestions for improvement, please open an issue on GitHub.