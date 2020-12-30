package robprakt.graphics;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import robprakt.Constants;

import java.awt.Color;
import java.awt.Dimension;


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
	 * row of connection Menu for cutter-robot
	 */
	private JPanel row1;
	
	/**
	 * row of connection Menu for holder-robot
	 */
	private JPanel row2;
	
	/**
	 * row of connection Menu for tracking-system
	 */
	private JPanel row3;
	
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
	 * Create the frame.
	 */
	public connectionMenu(Controller controller) {
		this.controller = controller;
		
		//#########################
		//########COMPONENTS#######
		//#########################

		//set GridLayout to connectionMenu
		this.setLayout(new GridLayout(3,1));
		
		//create rows of connectionMenu with GridBagLayout
		row1 = new JPanel(new GridBagLayout());
		row2 = new JPanel(new GridBagLayout());
		row3 = new JPanel(new GridBagLayout());
		
		//adding rows to connectionMenu
		add(row1);
		add(row2);
		add(row3);
		
		//creating GridBagConstraints for...
		//...connect-buttons
		//...IP-fields
		Dimension connectBtn = new Dimension(Constants.mainFrameWidth/5,Constants.mainFrameHeight/5);
		GridBagConstraints connectGBS = new GridBagConstraints();
		connectGBS.gridx = 1;
		connectGBS.gridy = 0;
		connectGBS.insets = new Insets(10,10,10,10);
		connectGBS.anchor = GridBagConstraints.CENTER;
		// for "send command" buttons
		Dimension ipFieldsDim = new Dimension(Constants.mainFrameWidth*6/10,Constants.mainFrameHeight/20);
		GridBagConstraints ipFieldsGBS = new GridBagConstraints();
		ipFieldsGBS.gridx = 0;
		ipFieldsGBS.gridy = 0;
		
		//creating buttons (buttons are red, cause initially there's no connection)
		//cutter-robot
		connectR1 = new JButton("<html><center>connect to<br>CUTTER-ROBOT<br><b>STATUS:<br>not connected</b></center></html>");
		connectR1.setPreferredSize(connectBtn);
		connectR1.setFont(new Font("Arial", Font.PLAIN, 15));
		connectR1.setBackground(Color.RED);
		//holder-robot
		connectR2 = new JButton("<html><center>connect to<br>HOLDER-ROBOT<br><b>STATUS:<br>not connected</b></center></html>");
		connectR2.setPreferredSize(connectBtn);
		connectR2.setFont(new Font("Arial", Font.PLAIN, 15));
		connectR2.setBackground(Color.RED);
		//tracking-system
		connectTS = new JButton("<html><center>connect to<br>TRACKING<br><b>STATUS:<br>not connected</b></center></html>");
		connectTS.setPreferredSize(connectBtn);
		connectTS.setFont(new Font("Arial", Font.PLAIN, 15));
		connectTS.setBackground(Color.RED);
		
		
		//creating IP fields
		//cutter-robot
		ipFieldR1 = new JTextField("Enter IP-address for CUTTER-ROBOT");
		ipFieldR1.setFont(new Font("Arial", Font.PLAIN, 12));
		ipFieldR1.setForeground(Color.LIGHT_GRAY);
		ipFieldR1.setPreferredSize(ipFieldsDim);
		//holder-robot
		ipFieldR2 = new JTextField("Enter IP-address for HOLDER-ROBOT");
		ipFieldR2.setFont(new Font("Arial", Font.PLAIN, 12));
		ipFieldR2.setForeground(Color.LIGHT_GRAY);
		ipFieldR2.setPreferredSize(ipFieldsDim);
		//tracking-system
		ipFieldTS = new JTextField("Enter IP-address for TRACKING-SYSTEM");
		ipFieldTS.setFont(new Font("Arial", Font.PLAIN, 12));
		ipFieldTS.setForeground(Color.LIGHT_GRAY);
		ipFieldTS.setPreferredSize(ipFieldsDim);
		
		
		//adding components to rows
		//buttons
		row1.add(connectR1,connectGBS);
		row2.add(connectR2,connectGBS);
		row3.add(connectTS,connectGBS);
		//text fields
		row1.add(ipFieldR1,ipFieldsGBS);
		row2.add(ipFieldR2,ipFieldsGBS);
		row3.add(ipFieldTS,ipFieldsGBS);

		//#########################
		//########LISTENERS########
		//#########################
		//creating FocusListener for IP fields
		//cutter-robot
		FocusListener FocusListenerR1 = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if(ipFieldR1.getText().equals("Enter IP-address for CUTTER-ROBOT")) ipFieldR1.setText("");
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(ipFieldR1.getText().equals("")) ipFieldR1.setText("Enter IP-address for CUTTER-ROBOT");;
			}
		};
		
		//holder-robot
		FocusListener FocusListenerR2 = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if(ipFieldR2.getText().equals("Enter IP-address for HOLDER-ROBOT")) ipFieldR2.setText("");
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(ipFieldR2.getText().equals("")) ipFieldR2.setText("Enter IP-address for HOLDER-ROBOT");;
			}
		};
		//tracking-system
		FocusListener FocusListenerTS = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				if(ipFieldTS.getText().equals("Enter IP-address for TRACKING-SYSTEM")) ipFieldTS.setText("");
			}

			@Override
			public void focusLost(FocusEvent e) {
				if(ipFieldTS.getText().equals("")) ipFieldTS.setText("Enter IP-address for TRACKING-SYSTEM");;
			}
		};
		
		//adding FocusListeners to text fields
		ipFieldR1.addFocusListener(FocusListenerR1);
		ipFieldR2.addFocusListener(FocusListenerR2);
		ipFieldTS.addFocusListener(FocusListenerTS);
		
		
		//creating ActionListeners for each button
		//cutter-robot
		ActionListener actionListenerbtnR1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipFieldR1.getText();
				int port = 5005;
				if(controller.connect(ip,port, controller.getClientR1())) {
					connectR1.setText("<html><center>connect to<br>CUTTER-ROBOT<br><b>STATUS:<br>CONNECTED</b></center></html>");
					connectR1.setForeground(Color.GREEN);
				}else {
					connectR1.setText("<html><center>connect to<br>CUTTER-ROBOT<br><b>STATUS:<br>FAILED</b></center></html>");
					connectR1.setBackground(Color.RED);
				}
			}
		};
		//holder-robot
		ActionListener actionListenerbtnR2 = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipFieldR2.getText();
				int port = 5005;
				if(controller.connect(ip, port, controller.getClientR1())) {
					connectR2.setText("<html><center>connect to<br>HOLDER-ROBOT<br><b>STATUS:<br>CONNECTED</b></center></html>");
					connectR2.setForeground(Color.GREEN);
				}else {
					connectR2.setText("<html><center>connect to<br>HOLDER-ROBOT<br><b>STATUS:<br>FAILED</b></center></html>");
					connectR2.setBackground(Color.RED);
				}
			}
		};
		//tracking-system
		ActionListener actionListenerbtnTS = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = ipFieldTS.getText();
				int port = 5005;
				if(controller.connect(ip, port, controller.getClientTS())) {
					connectTS.setText("<html><center>connect to<br>TRACKING-SYSTEM<br><b>STATUS:<br>CONNECTED</b></center></html>");
					connectTS.setForeground(Color.GREEN);
				}else {
					connectTS.setText("<html><center>connect to<br>TRACKING-SYSTEM<br><b>STATUS:<br>FAILED</b></center></html>");
					connectTS.setBackground(Color.RED);
				}
			}
		};
		
		//assigning ActionListeners to each button
		connectR1.addActionListener(actionListenerbtnR1);
		connectR2.addActionListener(actionListenerbtnR2);
		connectTS.addActionListener(actionListenerbtnTS);
	}
}
