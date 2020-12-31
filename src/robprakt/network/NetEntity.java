package robprakt.network;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 * this class represents a network entity such as client or a server
 */
abstract class NetEntity {
	
	/**
	 * the socket 
	 */
	protected Socket socket;
	
	/**
	 * the input reader
	 */
	protected DataInputStream in;
	
	/**
	 * the output writer
	 */
	protected PrintWriter out;
		
	/**
	 * the constructor of the network entity
	 */
	public NetEntity() {
		
	}

	
	/**
	 * initializes the in- and output-Streams and associates them with a given socket
	 * @param socket the socket to associate the streams with
	 */
	public void initStreams(Socket socket) {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * this method closes the connection
	 * @throws IOException the exception when something goes wrong closing the connection
	 */
	public abstract void closeConnection() throws IOException;
	
	/**
	 * this method is for sending data through the network
	 * 
	 * @param data the data to send
	 */
	public void sendData(String data) {
		if(out!=null) {
			// Man koennte auch  data+"\n" schreiben, wird aber momentan woanders abgefangen
			out.write(data);
			out.flush();
		}
	}
	
	/**
	 * the method is for receiving data through the network
	 * @return the data which has been received
	 */
	public String receiveData() {
		byte[] buffer = new byte[500];    //If you handle larger data use a bigger buffer size
		int read;
		try {
			if(in!=null) {
				while((read = in.read(buffer)) != -1) {
					return new String(buffer);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.exit(0);
			return "You have been disconnected";
		}
		
		return null;
	}
	
	/**
	 * checks if the connection has ever been closed
	 * @return true if connection has ever been closed, otherwise false
	 */
	public abstract boolean isClosed();

}
