import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class BasicClient {
	private static final int DURATION = 60;

	/**
	 *
	 */
	byte source, destination, checksum, data1, data2;

	/**
	 * A simple client that connects to a server on port 4446 and sends a
	 * message. This client application will ask for a 10 character input, which
	 * will be reversed by the server and send back.
	 *
	 * @author Jordan Long, Chris Lashley
	 * @throws InterruptedException
	 *
	 */
	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException
	{
		/**
		 * Create connection to server. Uses localhost:4446 as the default
		 * settings Initialize Scanner for user input.
		 */
		ServerSocket server = new ServerSocket(4446);
		System.out.println("Client Started");

		int timer = 0;
		while (timer < DURATION) {
			Thread.sleep(2000);
			Socket socket = server.accept();
			ClientConnectionHandler c = new ClientConnectionHandler(socket);
			c.start();
			timer += 2;
		}
		server.close();
	}

	public void readMessage(byte[] input)
	{
		source = input[0];
		destination = input[1];
		checksum = input[2];
		data1 = input[3];
		data2 = input[4];

		System.out.println("Source: " + source);
		System.out.println("Destination: " + destination);
		System.out.println("Checksum: " + checksum);
		System.out.println("Data1: " + data1);
		System.out.println("Data2: " + data2);
	}
}