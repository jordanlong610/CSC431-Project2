import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class ClientConnectionHandler extends Thread {
		byte source = 1, data1 = 1, data2 = 1;
		private Socket socket;
		Scanner input;
		PrintWriter output;

		public ClientConnectionHandler(Socket socket) throws IOException {
			this.socket = socket;
			input = new Scanner(socket.getInputStream());
			output = new PrintWriter(socket.getOutputStream());

		}

		/**
		 * Runs the individual thread.
		 */
		public void run() {
			byte destination = (byte) (Math.random() * 3);
			// byte checksum = computeChecksum(source, destination, data1,
			// data2);

			/**
			 * Send message
			 */
			byte[] sendBytes = sendMessage.getBytes();

			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			dOut.write(sendBytes);

			/**
			 * Receive reply from server and print to console.
			 */
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			byte[] message = new byte[10];
			dIn.readFully(message, 0, message.length);

			/**
			 * Close all streams and finally the socket.
			 */
			socket.close();
		}
	}