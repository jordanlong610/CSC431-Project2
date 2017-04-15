import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A simple client that connects to a server on port 4446 and sends a message.
 * This client application will ask for a 10 character input, which will be reversed
 * by the server and send back.
 *
 * @author Jordan Long, Chris Lashley, Jeff Titanich, Kyle Shoop
 */
public class Client
{

	//	static int SOURCE = randomDestination();  // testing purposes
	static int SOURCE = 1;  //Static Client ID
	static int CPORT = 4446;	// client port for sending msgs out
	static int SPORT = 4447;	// server port for receiving msgs

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
		/**
		 * Create connection to server.
		 * Uses localhost:4446 as the default settings
		 * Initialize Scanner for user input.
		 */
		InetAddress host = InetAddress.getLocalHost();
		Socket socket = new Socket(host.getHostName(), CPORT);

		/**
		 * Creates a conditional loop where the client can only
		 * send a max of 10 messages before terminating.
		 */
		int data1 = 1;
		int data2 = 1;
		long timeLastMsg = System.currentTimeMillis();

		/**
		 * A server is created listening for msgs on port 4447. Each time a new
		 * msg comes, a new thread is created as the router sends it to the client
		 * Send msg out to random client via router if last msg was sent over 2s ago.
		 * increment data value each time a msg is sent
		 */
		@SuppressWarnings("resource") //Server is never closed
		ServerSocket server = new ServerSocket(SPORT);
		//		Router r = new Router();
		//		Client c = new Client();
		while (true)
		{
			// checks to see if 2 seconds has passed, if so, sends a new message to random client
			long currentTime = System.currentTimeMillis();
			long timeDif = currentTime - timeLastMsg;
			if (timeDif > 2000)
			{
				sendMessage(SOURCE, randomDestination(), data1, data2, socket);
				timeLastMsg = currentTime;
				data1++;
				data2++;

				//				receiveMessage(socket);
			}

			// listen for msgs and let msg handler create a new thread to handle incoming msgs
			Socket ssocket = server.accept();
			//        	MessageHandler mh  = new MessageHandler(socket, r);
			//        	MessageHandler mh  = new MessageHandler(socket, c);
			MessageHandler mh  = new MessageHandler(socket);
			mh.start();

			/**
			 * Receive any messages from Router
			 */
			//			receiveMessage(socket);
		}

		/**
		 * Close all streams and finally the socket.
		 */
		//socket.close();
		//userInput.close();
	}

	/**
	 * Sends message to a router. Creates a byte array with the 5 bytes of data.
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
		System.out.println("");

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
	public static void receiveMessage(Socket socket) throws IOException
	{
		DataInputStream input = new DataInputStream(socket.getInputStream());
		byte[] message = new byte[5];

		input.readFully(message, 0, 5);

		byte calculateChecksum = checksum(message);
		if(calculateChecksum == message[2])
		{
			System.out.println("Client Received Message from Source: " + message[0]);
			System.out.println("Checksum valid, message is not corrupt.");
			System.out.println("Data1: " + message[3]);
			System.out.println("Data2: " + message[4]);
			System.out.println("");
		}
		else
		{
			System.out.println("Checksum not vaild, message may be corrupt.");
		}
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
		byte destination = (byte) (Math.random() * 4 + 1);
		// clients shouldnt send to themselves
		if (destination == SOURCE)
			while (destination == SOURCE)
				destination = (byte) (Math.random() * 4 + 1);
		return destination;
	}
}

/**
 * class for receiving msgs from the router sent from other clients
 * @author Chris
 */
class MessageHandler extends Thread
{
	private Socket socket;
	//    private Router router;
	//    private Client client;
	DataInputStream input;
	DataOutputStream output;
	//    public MessageHandler(Socket socket, Router r) throws IOException
	//    public MessageHandler(Socket socket, Client c) throws IOException
	public MessageHandler(Socket socket) throws IOException
	{
		this.socket = socket;
		//        this.router = r;
		//        this.client = c;
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());

	}


	/**
	 * Here is where we read in a packet and then close the socket.
	 */
	public void run()
	{
		while (socket.isConnected() && !socket.isClosed())
		{
			try
			{
				receiveMessage();
				input.close();
				output.close();
				socket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
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
	 *  Read message from router via another client. Prints to console for reference.
	 */

	public void receiveMessage()
	{
		byte[] message = new byte[5];
		try
		{
			input.readFully(message, 0, 5);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
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
	}
}