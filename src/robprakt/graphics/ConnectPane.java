package robprakt.graphics;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;

public class ConnectPane extends JPanel {
	
	private JTextField ipField;
	private JLabel label;
	private JButton connectR1;
	private Controller controller;
	private JButton connectR2;
	private JLabel connR1Label;
	private JLabel lblCurrentlyNotConnected;

	/**
	 * Create the frame.
	 */
	public ConnectPane(Controller controller) {
		this.controller = controller;
		
		setBounds(100, 100, 790, 410);
		setLayout(null);
		
		connectR1 = new JButton("Mit Laserschwert-Roboter Verbinden");
		connectR1.setFont(new Font("Calibri", Font.PLAIN, 20));
		connectR1.setBounds(10, 68, 341, 64);
		
		ipField = new JTextField();
		ipField.setEnabled(false);
		ipField.setBounds(66, 214, 220, 41);
		ipField.setFont(new Font("Calibri", Font.PLAIN, 20));
		ipField.setColumns(1);
		
		label = new JLabel("IP-Adresse des Roboters");
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		label.setLabelFor(label);
		label.setEnabled(false);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(66, 172, 220, 41);
		label.setFont(new Font("Calibri", Font.PLAIN, 20));
		

		add(connectR1);
		
		connectR2 = new JButton("Mit Halterungsroboter verbinden");
		connectR2.setFont(new Font("Calibri", Font.PLAIN, 20));
		connectR2.setBounds(420, 68, 341, 64);
		add(connectR2);
		add(ipField);
		add(label);
		
		connR1Label = new JLabel("Nicht Verbunden");
		connR1Label.setForeground(Color.RED);
		connR1Label.setFont(new Font("Calibri", Font.PLAIN, 20));
		connR1Label.setHorizontalAlignment(SwingConstants.CENTER);
		connR1Label.setLabelFor(connectR1);
		connR1Label.setBounds(10, 32, 341, 25);
		add(connR1Label);
		
		lblCurrentlyNotConnected = new JLabel("Nicht Verbunden");
		lblCurrentlyNotConnected.setFont(new Font("Calibri", Font.PLAIN, 20));
		lblCurrentlyNotConnected.setForeground(Color.RED);
		lblCurrentlyNotConnected.setHorizontalAlignment(SwingConstants.CENTER);
		lblCurrentlyNotConnected.setBounds(420, 32, 341, 25);
		add(lblCurrentlyNotConnected);
		
		
		connectR1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipField.getText();
				int port = 5000;
				//TODO: dynamically get Port from ipField
				if(controller.connect(ip, port)) {
					connR1Label.setText("Verbunden");
					connR1Label.setForeground(Color.green);
				}else {
					connR1Label.setText("Nicht Verbunden");
					connR1Label.setForeground(Color.RED);
				}
			}
		});
	}
}
