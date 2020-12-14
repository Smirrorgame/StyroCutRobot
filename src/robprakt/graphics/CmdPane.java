package robprakt.graphics;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;

public class CmdPane extends JPanel {
	private JTextField cmdTxt;

	/**
	 * Create the frame.
	 */
	public CmdPane() {
		setBounds(100, 100, 800, 600);
		setLayout(null);
		
		cmdTxt = new JTextField();
		cmdTxt.setFont(new Font("Calibri", Font.PLAIN, 30));
		cmdTxt.setToolTipText("Schreibe hier deinen Befehl für den Roboter");
		cmdTxt.setBounds(10, 189, 567, 75);
		add(cmdTxt);
		cmdTxt.setColumns(10);
		
		JButton btnSend = new JButton("Senden");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnSend.setBounds(623, 189, 123, 75);
		add(btnSend);
		
		JLabel label = new JLabel("Sende deine Befehle an den Roboter");
		label.setFont(new Font("Calibri", Font.PLAIN, 30));
		label.setBounds(10, 88, 567, 90);
		add(label);
		
		
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
		if((s=cmdTxt.getText()).length()>0) System.out.println("Sending message:\n"+s);
	}
}
