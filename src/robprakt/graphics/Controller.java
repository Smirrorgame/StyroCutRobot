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
	 * connect to Server
	 * @param ip the server ip
	 * @param port the port on which the server is running
	 */
	protected boolean connect(String ip, int port) {
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
	 * send command to cutter-robot
	 * @param command
	 */
	protected boolean sendR1(String command) {
		if(clientR1!=null) {
			clientR1.sendData(command);
			return true;
		}
		return false;
	}
	
	/**
	 * send command to holder-robot
	 * @param command
	 */
	protected boolean sendR2(String command) {
		if(clientR2!=null) {
			clientR2.sendData(command);
			return true;
		}
		return false;
	}
	
	/**
	 * send command to tracking-system
	 * @param command
	 */
	protected boolean sendTS(String command) {
		if(clientTS!=null) {
			clientTS.sendData(command);
			return true;
		}
		return false;
	}
	
	/**
	 * receive Message from cutter-robot
	 * @return the received message
	 */
	protected String responseR1() {
		if(clientR1!=null) {
			return clientR1.receiveData();
		}
		return "ERROR, not connected to cutter-robot!";
	}
	
	/**
	 * receive Message from holder-robot
	 * @return the received message
	 */
	protected String responseR2() {
		if(clientR2!=null) {
			return clientR2.receiveData();
		}
		return "ERROR, not connected to holder-robot!";
	}
	
	/**
	 * receive Message from tracking-system
	 * @return the received message
	 */
	protected String responseTS() {
		if(clientTS!=null) {
			return clientTS.receiveData();
		}
		return "ERROR, not connected to tracking-system!";
	}

}
