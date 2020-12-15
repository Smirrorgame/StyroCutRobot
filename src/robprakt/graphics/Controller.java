package robprakt.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import robprakt.network.TCPClient;

public class Controller {
	
	private MainFrame frame;
	
	private TCPClient client;
	
	public Controller(MainFrame frame) {
		this.frame = frame;
		
	}
	
	/**
	 * Setup of initial Action Listeners
	 */
	protected void initialListeners() {
		frame.btnConnect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.cmdPane.setVisible(false);
				frame.connectPane.setVisible(true);
				frame.revalidate();			
			}
		});
		
		frame.btnCmd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.connectPane.setVisible(false);
				frame.cmdPane.setVisible(true);
				frame.revalidate();			
			}
		});
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
	 * send command to Robot
	 * @param command the command
	 */
	protected boolean send(String command) {
		if(client!=null) {
			client.sendData(command);
			return true;
		}
		return false;
	}
	
	/**
	 * receive Message from Roboter
	 * @return the received message
	 */
	protected String response() {
		if(client!=null) {
			return client.receiveData();
		}
		return "ERROR, not Connected!";
	}

}
