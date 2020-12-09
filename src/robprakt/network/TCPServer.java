package robprakt.network;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer extends NetEntity {

	ServerSocket server;
	
	public TCPServer(int port) {
		try {
			server = new ServerSocket(port);
			System.out.println("Server startet...");
		} catch (IOException e) {
			System.err.println("Could't init Server");
			e.printStackTrace();
		}
	}
	
	public void accept() {
		try {
			socket = server.accept();
			initStreams(socket);
		} catch (IOException e) {
			System.err.println("Error on accepting client");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void closeConnection(){
		if(socket!=null) {
			try {
				socket.close();
				server.close();
			} catch (IOException e) {
				System.err.println("wasn't able to close connection");
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isClosed() {
		return socket==null || socket.isClosed();
	}

}
