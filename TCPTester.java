import java.io.*;
import java.net.*;
import java.util.*;

public class TCPTester {

    public static void main(String[] args) throws IOException {
        TCPTester tt = new TCPTester();
        if (args.length == 1) {
            // Server mode
            int port = Integer.parseInt(args[0]);
            tt.startServer("tcp", port);
        } else if (args.length == 2) {
            // Client mode
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            tt.startClient("tcp", host, port);
        } else {
            System.out.println("Usage: java TCPTester port_number");
            System.out.println("or");
            System.out.println("java TCPTester machine_to_connect port_number");
            System.exit(1);
        }
    }

    public void startClient(String protocol, String host, int port) throws IOException {
        if (protocol.equals("tcp")) {
            startTCPClient(host, port);
        } else if (protocol.equals("udp")) {
            startUDPClient(host, port);
        } else {
            throw new IllegalArgumentException("Error: Invalid protocol");
        }
    }

    public void startServer(String protocol, int port) throws IOException {
        if (protocol.equals("tcp")) {
            startTCPServer(port);
        } else if (protocol.equals("udp")) {
            startUDPServer(port);
        } else {
            throw new IllegalArgumentException("Error: Invalid protocol");
        }
    }

    public void startTCPClient(String host, int port) {

        String oneByteMessage = "a";
        String oneHundredByteMessage = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String twoHundredByteMessage = oneHundredByteMessage + oneHundredByteMessage;
        String threeHundredByteMessage = twoHundredByteMessage + oneHundredByteMessage;
        String fourHundredByteMessage = threeHundredByteMessage + oneHundredByteMessage;
        String fiveHundredByteMessage = fourHundredByteMessage + oneHundredByteMessage;
        String sixHundredByteMessage = fiveHundredByteMessage + oneHundredByteMessage;
        String sevenHundredByteMessage = sixHundredByteMessage + oneHundredByteMessage;
        String eightHundredByteMessage = sevenHundredByteMessage + oneHundredByteMessage;
        String nineHundredByteMessage = eightHundredByteMessage + oneHundredByteMessage;
        String thousandByteMessage = nineHundredByteMessage + oneHundredByteMessage;
        String[] messages = { oneByteMessage, oneHundredByteMessage, twoHundredByteMessage, threeHundredByteMessage,
                fourHundredByteMessage, fiveHundredByteMessage, sixHundredByteMessage, sevenHundredByteMessage,
                eightHundredByteMessage, nineHundredByteMessage, thousandByteMessage };
        try (Socket clientSocket = new Socket(host, port);
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

            // Measure RTT
            System.out.println("MEASURING RTT -------------------------------------");
            ArrayList<Double> rtts = new ArrayList<Double>();

            for (String message : messages) {
                byte[] messageBytes = message.getBytes();
                out.writeInt(messageBytes.length);
                out.write(messageBytes);
                out.flush();
                long startTime = System.nanoTime();

                int length = in.readInt();
                byte[] responseBytes = new byte[length];
                in.readFully(responseBytes);

                long endTime = System.nanoTime();
                double rtt = (endTime - startTime) / 1000000.0;
                System.out.println("Message size: " + message.length() + " bytes");
                System.out.println("RTT: " + rtt + " ms\n");
                rtts.add(rtt);
            }
            System.out.println("RTTS:");
            for (Double rtt : rtts) {
                System.out.println(rtt);
            }

            System.out.println("\nMEASURING Throughput 1kb, 2kb, 3kb, ..., 3kb-----------");
            // Measure throughput 1kb, 2kb, 3kb, ..., 32kb
            ArrayList<Double> throughputs = new ArrayList<Double>();
            for (int i = 1; i <= 32; i++) {
                int messageSize = i * 1024; // in kb
                String message = String.format("%0" + messageSize + "d", 0).replace('0',
                        'a');
                byte[] messageBytes = message.getBytes();

                long startTime = System.nanoTime();
                out.writeInt(messageBytes.length);
                out.write(messageBytes);
                out.flush();

                int length = in.readInt();
                byte[] responseBytes = new byte[length];
                in.readFully(responseBytes);
                long endTime = System.nanoTime();

                double duration = (endTime - startTime) / 1000000.0; // divide by 1000000 to
                // get milliseconds.
                double throughput = (messageSize * 2) / (duration * 1000); // Calculate
                // throughput in Mbps, 2
                // for send and receive, 1024 for
                // kb to mb, 1000 for ms to s

                System.out.println("Message size: " + messageSize + " bytes");
                System.out.println("Throughput: " + throughput + " kbps\n");

                throughputs.add(throughput);
            }
            System.out.println("Throughputs:");
            for (Double throughput : throughputs) {
                System.out.println(throughput);
            }

            // Measure throughput of 1MB in different sized chunks i.e 1kb, 2kb, 4kb, ...,
            // 1MB

            final int MB = 1024; // 1024kb = 1MB
            int[] messageSizes = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 }; // in kb
            ArrayList<Double> throughputAverageAverage = new ArrayList<Double>();

            for (int messageSize : messageSizes) {
                System.out.println("\nMEASURING Throughput of 1MB in " + messageSize + "kb chunks -----------");
                int numChunks = MB / messageSize;
                ArrayList<Double> throughputs2 = new ArrayList<Double>();
                for (int i = 1; i <= numChunks; i++) {
                    int sizeInBytes = messageSize * 1024;
                    String message = String.format("%0" + sizeInBytes + "d", 0).replace('0', 'a');
                    byte[] messageBytes = message.getBytes();

                    long startTime = System.nanoTime();
                    out.writeInt(messageBytes.length);
                    out.write(messageBytes);
                    out.flush();

                    int length = in.readInt();
                    byte[] responseBytes = new byte[length];
                    in.readFully(responseBytes);
                    long endTime = System.nanoTime();

                    double duration = (endTime - startTime) / 1000000.0; // divide by 1000000 to get milliseconds.
                    double throughput = (messageSize * 2) / (duration * 1000); // Calculate throughput in kbps;
                    // 2 for send and receive, 1000 for milliseconds to seconds

                    // System.out.println("Message size: " + messageSize + " kb");
                    // System.out.println("Throughput: " + throughput + " Mbps\n");

                    throughputs2.add(throughput);
                }
                double average = 0;
                System.out.println("Throughputs:");
                for (Double throughput : throughputs2) {
                    // System.out.println(throughput);
                    average += throughput;

                }
                System.out.println("Average: " + average / throughputs2.size() + " kbps\n");
                throughputAverageAverage.add(average);

            }
            for (Double avg : throughputAverageAverage) {
                System.out.println(avg);
            }

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to: " + host);
            System.exit(1);
        }
    }

    public void startUDPClient(String host, int port) {
        // working on it
    }

    private static void startTCPServer(int port) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP server started and listening on port " + port);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

                    System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

                    while (true) {
                        int length;
                        try {
                            length = in.readInt();
                        } catch (EOFException e) {
                            break;
                        }
                        byte[] receivedBytes = new byte[length];
                        in.readFully(receivedBytes);
                        System.out.println("Received message of size: " + length + " bytes");
                        out.writeInt(receivedBytes.length);
                        out.write(receivedBytes);
                        out.flush();
                    }
                }
            }
        }
    }

    public void startUDPServer(int port) {
        // working on it
    }
}