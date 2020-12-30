package robprakt.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import robprakt.network.TCPClient;

public class Controller {
	
	private MainFrame frame;
	
	/**
	 * client communicating with cutter-robot
	 */
	private TCPClient clientR1;
	
	/**
	 * client communicating with holder-robot
	 */
	private TCPClient clientR2;
	
	/**
	 * client communicating with tracking-system
	 */
	private TCPClient clientTS;
	
	
	
	public Controller(MainFrame frame) {
		this.frame = frame;
		
	}
	
	/**
	 * Getter for TCPClient for cutter-robot
	 * @return clientR1
	 */
	public TCPClient getClientR1() {
		return clientR1;
	}
	
	/**
	 * Getter for TCPClient for holder-robot
	 * @return clientR1
	 */
	public TCPClient getClientR2() {
		return clientR2;
	}
	
	/**
	 * Getter for TCPClient for tracking-system
	 * @return clientTS
	 */
	public TCPClient getClientTS() {
		return clientTS;
	}
	
	/**
	 * connect to Server
	 * @param ip the server ip for the specified connection
	 * @param port the port on which the server is running
	 * @param client TCPClient that is initially null. 3 different clients for each server
	 */
	protected boolean connect(String ip, int port, TCPClient client) {
		client = new TCPClient(ip, port);
		return client.connect();
	}
	
	/**
	 * close the connection to the server
	 */
	protected void closeConnection() {
		if(client!=null) {
			client.closeConnection();
		}
	}

	/**
	 * send command to server suitable to TCPClient by TCPClient
	 * @param command to send to server
	 * @param client TCPClient that is needed for Server
	 */
	protected boolean send(String command, TCPClient client) {
		if(client!=null) {
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
	protected String response(TCPClient client) {
		if(client!=null) {
			return client.receiveData();
		}
		return "ERROR, not connected to cutter-robot!";
	}
}
