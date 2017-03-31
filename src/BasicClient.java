import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BasicClient
{
	/**
	 * A simple client that connects to a server on port 4446 and sends a message.
	 * This client application will ask for a 10 character input, which will be reversed
	 * by the server and send back.
	 * 
	 * @author Jordan Long, Chris Lashley
	 * 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
            /**
             * Create connection to server.
             * Uses localhost:4446 as the default settings
             * Initialize Scanner for user input.
             */
            InetAddress host = InetAddress.getLocalHost();
        	Socket socket = new Socket(host.getHostName(), 4446);
            Scanner userInput = new Scanner(System.in);
            Scanner input = new Scanner(socket.getInputStream());
            PrintWriter output = new PrintWriter(socket.getOutputStream());
            String sendMessage;
            String recieveMessage;

            /**
             * Creates a conditional loop where the client can only 
             * send a max of 10 messages before terminating.
             */
            int count = 0;
            while (count<10)
            {
            
            	/**
        		 * Get user input from Scanner and store in userInput variable.
        		 * Will then check length of string, if not exactly 10 will reject
        		 * and ask for new input.
        		 */
            	
        		System.out.println("Enter a 10 character message:");
        		sendMessage = userInput.nextLine();
        		
        		while(sendMessage.length()!=10)
        		{
        			System.out.println("Message length is not 10 characters, please reenter message: ");
        			sendMessage = userInput.nextLine();
        		}
                
        		
                /**
                 * Send stored String to server.
                 */
                output.println(sendMessage);
                output.flush();


                /**
                 * Receive reply from server and print to console.
                 */

                
                recieveMessage  = input.nextLine();
                System.out.println("Server: " + recieveMessage );
                output.flush();

                /**
                 * Increase count by 1, Client will quit after 10 messages sent.
                 */
                count++;
            }
            
            /**
             * Close all streams and finally the socket.
             */
            input.close();
            output.close();
            socket.close();
            userInput.close();
	}
}