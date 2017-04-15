import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Router
{
	/**
	 * Simple server to take in a string of text 10 characters long, reverse it's order
	 * and then send it back to the client.
	 *
	 *
	 * @author Jordan Long, Chris Lashley, Jeff Titanich, Kyle Shoop
	 */
	public int routeNum = 1;	// 1-4 router id range all must be different

	public static void main(String[] args) throws IOException
	{
		/**
		 * A server is created listening on port 4446. Each time a new
		 * client connects to the server, a new thread is created
		 * for that specific client.
		 */

		@SuppressWarnings("resource") //Server is never closed
		ServerSocket server = new ServerSocket(4446);
		Router r = new Router();
		System.out.println("Router Started: "+ server);

		while (true)
		{
			Socket socket = server.accept();
			RouterHandler rh  = new RouterHandler(socket, r);
			rh.start();
		}
	}
}

class RouterHandler extends Thread
{
	private static int SPORT = 4446;	// server port (to receive from client)
	private static int CPORT = 4447;	// client port (to send to client)
	private static Socket socket;
	private Router router;
	private int routeId = 1;	// 1-4 router hop id number, each must be different
	DataInputStream input;
	DataOutputStream output;
	public RouterHandler(Socket socket, Router r) throws IOException
	{
		this.socket = socket;
		this.router = r;
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());

	}


	/**
	 * Here is where we read in a packet, print to console, and then forward to another router.
	 */
	public void run()
	{
		while (socket.isConnected() && !socket.isClosed())
		{
			try
			{
				receiveMessage();
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
	 *  Read message from Client. Prints to console for reference.
	 * @throws IOException
	 */

	public void receiveMessage() throws IOException
	{
		byte[] message= new byte[5];
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
			System.out.println("Router Received Message from Source: " + message[0]);
			System.out.println("Checksum valid, message is not corrupt.");
			System.out.println("Data1: " + message[3]);
			System.out.println("Data2: " + message[4]);

			//Send message to another client or router
			sendMessage(message);
		}
		else
		{
			System.out.println("Checksum not vaild, message may be corrupt.");
		}
	}

	/**
	 *  Forward a message to another client.
	 * @throws IOException
	 */

	public void sendMessage(byte[] message) throws IOException
	{
		int destination = (int) message[1];
		int nextHop = routingTable(message);

		if(destination == nextHop)
		{
			//send to client
			forwardMessage((int) message[0], (int) message[1], (int) message[3], (int) message[4], SPORT, nextHop, true);
		}
		else
		{
			//send to router
			forwardMessage((int) message[0], (int) message[1], (int) message[3], (int) message[4], CPORT, nextHop, false);
		}

		try
		{
			output.write(message);
			output.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Forwards message.
	 * @param source Source Client ID
	 * @param destination Destination Client ID
	 * @param data1 Data packet to be sent
	 * @param data2 Data packet to be sent
	 * @param port the port the message needs to be sent via
	 * @param hop the router for the msg destination
	 * @param client boolean that is true if the current router is for the destination client, false if needs to be routed
	 * @throws IOException
	 */
	public static void forwardMessage(int source, int destination, int data1, int data2, int port, int hop, boolean client) throws IOException
	{
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		byte[] message = {(byte) (source), (byte) (destination), 0, (byte) (data1), (byte) (data2)};
		message[2] = checksum(message);		//Calculate checksum and attach to message.

		if (client)
		{
			// needs to go to client on different port
			InetAddress host = InetAddress.getLocalHost();
			Socket destSocket = new Socket(host.getHostName(), SPORT);
			output = new DataOutputStream(destSocket.getOutputStream());
			System.out.println("Router forwarded message to Destination: " + message[1]);
		}
		else
		{
			System.out.println("Router forwarded message to Router: " + hop);
		}
		System.out.println("Data1: " + message[3]);
		System.out.println("Data2: " + message[4]);
		System.out.println("");

		output.write(message);
		output.flush();
	}

	/**
	 * Routing table for messages to be sent.
	 * @param message
	 * @return router the correct router.
	 */
	public int routingTable(byte[] message)
	{
		int source = message[0];
		int destination = message[1];
		int route = 0;

		// if this is destination router no need to use table
		if (routeId == destination)
			return routeId;
		else
		{
			// use table to find which router to send to get msg to destination
			//Router 1
			if(source == 1)
			{
				if(destination == 1)
				{
					route = 1;
				}
				else if(destination == 2)
				{
					route = 2;
				}
				else if(destination == 3)
				{
					route = 2;
				}
				else if(destination == 4)
				{
					route = 4;
				}
			}
			//Router 2
			else if(source == 2)
			{
				if(destination == 1)
				{
					route = 1;
				}
				else if(destination == 2)
				{
					route = 2;
				}
				else if(destination == 3)
				{
					route = 3;
				}
				else if(destination == 4)
				{
					route = 3;
				}
			}
			//Router 3
			else if(source == 3)
			{
				if(destination == 1)
				{
					route = 4;
				}
				else if(destination == 2)
				{
					route = 2;
				}
				else if(destination == 3)
				{
					route = 3;
				}
				else if(destination == 4)
				{
					route = 4;
				}
			}
			//Router 4
			else if(source == 4)
			{
				if(destination == 1)
				{
					route = 1;
				}
				else if(destination == 2)
				{
					route = 1;
				}
				else if(destination == 3)
				{
					route = 3;
				}
				else if(destination == 4)
				{
					route = 4;
				}
			}
		}
		return route;
	}
}