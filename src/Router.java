import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;


public class Router
{
	/**
	 * Router that takes in message from either its connected client, or from another router.
	 * It will read the message and send it to its correct destination router or client.
	 *
	 *
	 * @author Jordan Long, Chris Lashley, Jeff Titanich, Kyle Shoop
	 */


    public static void main(String[] args) throws IOException
    {
        /**
         * A Router is created listening on port 4446. Each time a new
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

@SuppressWarnings("unused")
class RouterHandler extends Thread
{
    private Socket socket;
    private Router router;
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
     * Run method for each thread. While the socket is connected, it will read received packets, print to console, 
     * and then forward to another router or it's connected client.
     */
    public void run()
    {
    	while (socket.isConnected() && !socket.isClosed())
    	{
    		byte[] message = receiveMessage();
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
	 *  @return message The received message from a client.
	 */

    public byte[] receiveMessage()
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

		return message;
    }




    /**
     *  Forward a message to another Router or Client.
     */

    public void sendMessage(byte[] message)
    {
        int destination = message[1];
        int currentRouter = 0; //not true

    	if(destination == currentRouter)
        {
        	//send to client
        }
    	else
    	{
    		//send to router
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
	 * Routing table for messages to be sent.
	 * 
	 * @param message received message from router.
	 * @return router the correct router.
	 */
	public int routingTable(byte[] message)
	{
		int source = message[0];
		int destination = message[1];
		int route = 0;

		/**
		 *  If current client is destination router no need to use table.
		 */
		if (source == destination)
			return source;
		else
		{
			/**
			 *  use table to find which router to send to get message to destination
			 */
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