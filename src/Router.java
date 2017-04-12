import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread;
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


    public static void main(String[] args) throws IOException
    {
        /**
         * A server is created listening on port 4446. Each time a new
         * client connects to the server, a new thread is created
         * for that specific client.
         */

		@SuppressWarnings("resource") //Server is never closed
		ServerSocket server = new ServerSocket(4446);
        System.out.println("Server opened on port 4446");

        while (true)
        {
        	Socket socket = server.accept();
        	ConnectionHandler c = new ConnectionHandler(socket);
        	c.start();
        }
    }
}

class ConnectionHandler extends Thread
{
    private Socket socket;
    DataInputStream input;
    DataOutputStream output;
    public ConnectionHandler(Socket socket) throws IOException
    {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());

    }


    /**
     * Runs the individual thread.
     */
    public void run()
    {
    	/**
    	 * Continue to receive and send requests from Client until socket connection is terminated.
    	 */
    	while (socket.isConnected() && !socket.isClosed())
    	{

    		/**
			 *  Read message from Client. Prints to console for reference.
			 */

    		byte[] inputMsg= new byte[5];
    		try
    		{
				input.readFully(inputMsg, 0, 5);
			}
    		catch (IOException e1)
    		{
				e1.printStackTrace();
			}
    		System.out.println("Router Received Message from Client: " + inputMsg[0]);
    		System.out.println("Data1: " + inputMsg[3]);
    		System.out.println("Data2: " + inputMsg[4]);



            /**
             *  Return message to client.
             */

            try
            {
				output.write(inputMsg);
			}
            catch (IOException e)
            {
				e.printStackTrace();
            }

            System.out.println("Router Sent Message to Client: " + inputMsg[1]);
    		System.out.println("Data1: " + inputMsg[3]);
    		System.out.println("Data2: " + inputMsg[4]);
    	}
    }
}