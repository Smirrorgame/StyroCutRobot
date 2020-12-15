package robprakt.graphics;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class CmdPane extends JPanel {
	
	/**
	 * command input text field
	 */
	private JTextField cmdTxt;
	
	/**
	 * Robot response text field
	 */
	private JTextPane responseField;

	/**
	 * the controller
	 */
	private Controller controller;
	/**
	 * Create the frame.
	 */
	public CmdPane(Controller controller) {
		this.controller = controller;
		setBounds(100, 100, 790, 410);
		setLayout(null);
		
		cmdTxt = new JTextField();
		cmdTxt.setFont(new Font("Calibri", Font.PLAIN, 30));
		cmdTxt.setToolTipText("Schreibe hier deinen Befehl für den Roboter");
		cmdTxt.setBounds(10, 112, 567, 50);
		add(cmdTxt);
		cmdTxt.setColumns(1);
		
		JButton btnSend = new JButton("Senden");
		btnSend.setBounds(623, 112, 123, 50);
		add(btnSend);
		
		JLabel label = new JLabel("Sende deine Befehle an den Roboter");
		label.setFont(new Font("Calibri", Font.PLAIN, 30));
		label.setBounds(10, 11, 567, 90);
		add(label);
		
		responseField = new JTextPane();
		responseField.setEditable(false);
		responseField.setText("Antwort vom Roboter");
		responseField.setToolTipText("Antwort vom Roboter");
		responseField.setFont(new Font("Calibri", Font.PLAIN, 30));
		responseField.setBounds(10, 270, 567, 50);
		add(responseField);
		
		
		cmdTxt.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendMsg();
				}
			}
			
			@Override
			public void keyTyped(KeyEvent e) {
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendMsg();
			}
		});
	}
	
	/**
	 * Sendet den Inhalt des Commando-Fensters an den verbundenen Roboter
	 */
	private void sendMsg() {
		String s;
		if((s=cmdTxt.getText()).length()>0) {
			cmdTxt.setText("");
			if (!controller.send(s)) {
				responseField.setText("An Error Occured, maybe not connected?");
				return;
			}
			String response = controller.response();
			if(response.contains("disconnected")){
				responseField.setText("Die Verbindung wurde getrennt!");
			}else {
				responseField.setText(response);
			}
		}
	}
}
