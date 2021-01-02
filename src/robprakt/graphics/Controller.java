package robprakt.graphics;

import robprakt.network.TCPClient;

public class Controller {
	
	private MainFrame frame;
	
	/**
	 * client communicating with cutter-robot
	 */
	private TCPClient clientR1 = new TCPClient();
	
	/**
	 * client communicating with holder-robot
	 */
	private TCPClient clientR2 = new TCPClient();
	
	/**
	 * client communicating with tracking-system
	 */
	private TCPClient clientTS = new TCPClient();
	
	public Controller(MainFrame frame) {
		this.frame = frame;	
	}
	
	/**
	 * Getter for TCPClient for cutter-robot
	 * @return clientR1
	 */
	public TCPClient getClientR1() {
		return this.clientR1;
	}
	
	/**
	 * Getter for TCPClient for holder-robot
	 * @return clientR1
	 */
	public TCPClient getClientR2() {
		return this.clientR2;
	}
	
	/**
	 * Getter for TCPClient for tracking-system
	 * @return clientTS
	 */
	public TCPClient getClientTS() {
		return this.clientTS;
	}
	
	/**
	 * connect to Server
	 * @param ip the server ip for the specified connection
	 * @param port the port on which the server is running
	 * @param client TCPClient that is initially null. 3 different clients for each server
	 */
	protected boolean connect(String ip, int port, TCPClient client) {
		client.setIP(ip);
		client.setPort(port);
		return client.connect();
	}

	/**
	 * send command to server suitable to TCPClient by TCPClient
	 * @param command to send to server
	 * @param client TCPClient that is needed for Server
	 */
	public boolean send(String command, TCPClient client) {
		if(client!=null) {
			if(client == clientTS) command = command + "\n"; //necessary for tracking system server communication
			client.sendData(command);
			return true;
		}
		return false;
	}
	
	/**
	 * receive message from server to TCPClient
	 * @param client contains the client that communicates with the server
	 * @return the received message
	 */
	public String response(TCPClient client) {
		if(client!=null) {
			return client.receiveData();
		}
		return "ERROR, not connected to cutter-robot!";
	}
}
