import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class BasicRouter
{
	/**
	 * Simple server to take in a string of text 10 characters long, reverse it's order
	 * and then send it back to the client.
	 *
	 *
	 * @author Jordan Long, Chris Lashley
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
        System.out.println("Waiting for client...");

        while (true)
        {
        	Socket socket = server.accept();
        	RouterConnectionHandler c = new RouterConnectionHandler(socket);
        	c.start();
        }
    }
}

class RouterConnectionHandler extends Thread
{
    private Socket socket;
    Scanner input;
    PrintWriter output;
    public RouterConnectionHandler(Socket socket) throws IOException
    {
        this.socket = socket;
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream());

    }


    /**
     * Runs the individual thread.
     */
    public void run()
    {
    	/**
    	 * Continue to receive and send requests from Client until socket connection is terminated.
    	 */
    	while (socket.isConnected())
    	{

    		/**
			 *  Read message from Client. Prints to console for reference.
			 */

            String inputMsg = (input.nextLine());
            System.out.println("Client: " + inputMsg);


            /**
             * Perform string reversal.
             */

            String outputMsg = new StringBuffer(inputMsg).reverse().toString();

            /**
             *  Return message to client.
             */

            output.println(outputMsg);
            output.flush();
            System.out.println("Waiting for client...");
    	}
    }
}