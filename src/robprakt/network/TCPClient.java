package robprakt.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 
 * @author Micha Halla
 * this class represents a TCP client
 */
class TCPClient extends NetEntity {
	
	/**
	 * the connection socket
	 */
//	private Socket socket;
	
	/**
	 * the ip to connect to
	 */
	private String ip;
	
	private int port;

	/**
	 * the constructor of the client
	 * @param ip the ip to connect to
	 */
	public TCPClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 *
	 * this method connects the client to a server
	 * @return true when connected, false on errors
	 */
	public boolean connect() {
		try {
			socket = new Socket(ip, port);
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Problems to connect, is server started?");
			return false;
		}
		initStreams(socket);
		return true;
	}

	/**
	 * this method closes the connection to a server
	 */
	@Override
	public void closeConnection() {
		if(socket!=null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("wasn't able to close connection");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * this method checks if the socket has been closed
	 * @return true if socket has been closed, otherwise false
	 */
	@Override
	public boolean isClosed() {
		return socket==null || socket.isClosed();
	}	
}