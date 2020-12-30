package robprakt.graphics;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;


/**
 * ConnectPane contains graphical elements of the connection manager.
 * It implements buttons and text fields to connect to the robots and tracking system.
 * 
 * @author Micha Halla
 * @author Moritz Franz
 */
public class connectionMenu extends JPanel {
	
	private Controller controller;
	
	/**
	 * connectPane contains components for connection manager
	 */
	private JPanel connectPane;
	
	/**
	 * button for connecting to cutter robot
	 */
	private JButton connectR1;
	
	/**
	 * button for connecting to holder robot
	 */
	private JButton connectR2;
	
	/**
	 * button for connecting to tracking system server
	 */
	private JButton connectTS;
	
	/**
	 * text field for IP address of cutter robot
	 */
	private JTextField ipFieldR1;
	
	/**
	 * text field for IP address of holder robot
	 */
	private JTextField ipFieldR2;
	
	/**
	 * text field for IP address of tracking system
	 */
	private JTextField ipFieldTS;
	
	/**
	 * label for headline of ipFieldR1
	 */
	private JLabel labelR1;
	
	/**
	 * label for headline of ipFieldR2
	 */
	private JLabel labelR2;
	
	/**
	 * label for headline of ipFieldTS
	 */
	private JLabel labelTS;

	/**
	 * label for connection status for cutter robot
	 */
	private JLabel connR1Label;

	/**
	 * label for connection status for holder robot
	 */
	private JLabel connR2Label;

	/**
	 * label for connection status for tracking system
	 */
	private JLabel connTSLabel;

	
	private GridBagLayout layout = new GridBagLayout();
	
	
	
	/**
	 * Create the frame.
	 */
	public connectionMenu(Controller controller) {
		this.controller = controller;
		
		connectPane = new JPanel(layout);
		connectPane.add(new JButton("123412341234"));
		
		setLayout(null);
		connectR1 = new JButton("connect to cutter");
		connectR1.setFont(new Font("Calibri", Font.PLAIN, 20));
		connectR1.setBounds(0, 0, 341, 64);
		connectR1.setVisible(true);
		
		ipFieldR1 = new JTextField();
		ipFieldR1.setEnabled(true);
		ipFieldR1.setBounds(66, 214, 220, 41);
		ipFieldR1.setFont(new Font("Calibri", Font.PLAIN, 20));
		ipFieldR1.setColumns(1);
		
		labelR1 = new JLabel("IP address");
		labelR1.setVerticalAlignment(SwingConstants.BOTTOM);
		labelR1.setLabelFor(ipFieldR1);
		labelR1.setEnabled(false);
		labelR1.setHorizontalAlignment(SwingConstants.CENTER);
		labelR1.setBounds(66, 172, 220, 41);
		labelR1.setFont(new Font("Calibri", Font.PLAIN, 20));
		
		
		
		connectR2 = new JButton("connect to holder");
		connectR2.setFont(new Font("Calibri", Font.PLAIN, 20));
		connectR2.setBounds(420, 68, 341, 64);
//		add(connectR1);
//		add(connectR2);
//		add(ipFieldR1);
//		add(labelR1);
		
		connR1Label = new JLabel("not connected");
		connR1Label.setForeground(Color.RED);
		connR1Label.setFont(new Font("Calibri", Font.PLAIN, 20));
		connR1Label.setHorizontalAlignment(SwingConstants.CENTER);
		connR1Label.setLabelFor(connectR1);
		connR1Label.setBounds(10, 32, 341, 25);
//		add(connR1Label);
		
		connR2Label = new JLabel("not connected");
		connR2Label.setFont(new Font("Calibri", Font.PLAIN, 20));
		connR2Label.setForeground(Color.RED);
		connR2Label.setHorizontalAlignment(SwingConstants.CENTER);
		connR2Label.setBounds(420, 32, 341, 25);
//		add(connR2Label);
		
		connectR1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipFieldR1.getText();
				int port = 5005;
				//TODO: dynamically get Port from ipField
				if(controller.connect(ip, port)) {
					connR1Label.setText("connected");
					connR1Label.setForeground(Color.green);
				}else {
					connR1Label.setText("not connected");
					connR1Label.setForeground(Color.RED);
				}
			}
		});
	}
	
	/**
	 * Getter for connectPane.
	 * @return connectPane
	 */
	protected JPanel getConnectPane() {
		return connectPane;
	}
}
