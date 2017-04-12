import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
	/**
	 * A simple client that connects to a server on port 4446 and sends a message.
	 * This client application will ask for a 10 character input, which will be reversed
	 * by the server and send back.
	 *
	 * @author Jordan Long, Chris Lashley, Jeff Titanich, Kyle Shoop
	 *
	 */

	static int SOURCE = 1;  //Static Client ID
	static int PORT = 4446;

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
            /**
             * Create connection to server.
             * Uses localhost:4446 as the default settings
             * Initialize Scanner for user input.
             */
            InetAddress host = InetAddress.getLocalHost();
        	Socket socket = new Socket(host.getHostName(), 4446);

            /**
             * Creates a conditional loop where the client can only
             * send a max of 10 messages before terminating.
             */

            int data1=1;
    		int data2=1;
            while (true)
            {

                /**
                 * Send stored String to server.
                 * Receive Message.
                 */

                sendMessage(SOURCE, randomDestination(), data1, data2, socket);



                /**
                 * Receive Message from Router
                 */
                byte[] receiveMessage = receiveMessage(socket);



                /**
                 * Increase data by 1, sleep 2 seconds
                 */
                data1++;
                data2++;
                Thread.sleep(2000);
            }

            /**
             * Close all streams and finally the socket.
             */
            //socket.close();
            //userInput.close();
	}




	/**
	 * Sends message to a router. Creates a byte array with the 5 bytes of data.
	 *
	 * @param source Source Client ID
	 * @param destination Destination Client ID
	 * @param data1 Data packet to be sent
	 * @param data2Data packet to be sent
	 * @param socket
	 * @throws IOException
	 */
	public static void sendMessage(int source, int destination, int data1, int data2, Socket socket) throws IOException
	{
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

		byte[] message = {(byte) (source), (byte) (destination), 0, (byte) (data1), (byte) (data2)};
		message[2] = checksum(message);		//Calculate checksum and attach to message.

		System.out.println("Client sent message to Destination: " + message[1]);
		System.out.println("Data1: " + message[3]);
		System.out.println("Data2: " + message[4]);

		output.write(message);
        output.flush();
}

	/**
	 * Receives a message from the router.
	 * Will calculate checksum and compare it to the received checksum. If valid it will print the content of the
	 * message, if not it will display an error and discard the message.
	 *
	 * @param socket
	 * @return receiveMessage The message from the router.
	 * @throws IOException
	 */
	public static byte[] receiveMessage(Socket socket) throws IOException
	{
        DataInputStream input = new DataInputStream(socket.getInputStream());
		byte[] message= new byte[5];

		input.readFully(message, 0, 5);

        byte calculateChecksum = checksum(message);
        if(calculateChecksum == message[2])
        {
        	System.out.println("Client Received Message from Source: " + message[0]);
        	System.out.println("Checksum valid, message is not corrupt.");
    		System.out.println("Data1: " + message[3]);
    		System.out.println("Data2: " + message[4]);
        }
        else
        {
        	System.out.println("Checksum not vaild, message may be corrupt.");
        }

		return message;
}

	/**
	 * Calculates checksum of received message. Returns the value of the checksum.
	 * @param data Byte array of received message.
	 * @return The computed checksum value
	 */
	public static byte checksum(byte[] data)
	{
		int checksum = 256 - (data[0]+data[1]+data[3]+data[4]);
		return (byte)(checksum);
	}

	/**
	 * Generate a random client between 1 and 4.
	 * @return Destination Client ID
	 */
	public static byte randomDestination()
	{
		byte destination = (byte) (Math.random() * 4);
		return destination;
	}

}