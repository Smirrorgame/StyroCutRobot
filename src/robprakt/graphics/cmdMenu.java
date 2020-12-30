package robprakt.graphics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import robprakt.Constants;

/**
 * Class cmdMenu contains components for the command-menu. It implements functionality to send commands to servers.
 * @author Micha
 * @author Moritz
 */
public class cmdMenu {
	
	/**
	 * cmdPane contains components for command manager
	 */
	private JPanel cmdPane;
	
	/**
	 * cmdPane is split up into 3 JPanel rows containing components for communication
	 */
	private JPanel row1;
	private JPanel row2;
	private JPanel row3;
	
	/**
	 * command input text field for cutter-robot
	 */
	private JTextField cmdTxtR1;
	
	/**
	 * command input text field for holder-robot
	 */
	private JTextField cmdTxtR2;
	
	/**
	 * command input text field for tracking-system
	 */
	private JTextField cmdTxtTS;
	
	/**
	 * status if the text field is focused
	 */
	private boolean focusTxtR1;
	
	/**
	 * status if the text field is focused
	 */
	private boolean focusTxtR2;
	
	/**
	 * status if the text field is focused
	 */
	private boolean focusTxtTS;

	/**
	 * button for sending command from cmdTxtR1 text field to cutter-robot
	 */
	private JButton sendCmdR1;

	/**
	 * button for sending command from cmdTxtR2 text field to holder-robot
	 */
	private JButton sendCmdR2;
	
	/**
	 * button for sending command from cmdTxtTS text field to tracking-system
	 */
	private JButton sendCmdTS;
	
	/**
	 * response text field of cutter-robot
	 */
	private JTextPane responseFieldR1;
	
	/**
	 * response text field of holder-robot
	 */
	private JTextPane responseFieldR2;

	/**
	 * response text field of tracking-system
	 */
	private JTextPane responseFieldTS;

	/**
	 * the controller
	 */
	private Controller controller;
	
	/**
	 * Create the frame.
	 */
	public cmdMenu(Controller controller) {
		this.controller = controller;
		
		
		//#########################
		//########COMPONENTS#######
		//#########################
		// splitting up cmdPane into 3 rows
		cmdPane = new JPanel(new GridLayout(3,1));
		// generating JPanel with GridBagLayout
		row1 = new JPanel(new GridBagLayout());
		row2 = new JPanel(new GridBagLayout());
		row3 = new JPanel(new GridBagLayout());
		// adding rows to cmdPane
		cmdPane.add(row1);
		cmdPane.add(row2);
		cmdPane.add(row3);
		
		
		// defining dimensions and constraints of the components
		// for cmdTxt text fields
		Dimension cmdTxtDim = new Dimension(Constants.mainFrameWidth*24/32,Constants.mainFrameHeight/20);
		GridBagConstraints cmdTxtGBS = new GridBagConstraints();
		cmdTxtGBS.gridx = 0;
		cmdTxtGBS.gridy = 0;
		cmdTxtGBS.anchor = GridBagConstraints.CENTER;
		// for "send command" buttons
		Dimension sendCmdDim = new Dimension(Constants.mainFrameWidth*3/20,Constants.mainFrameHeight/20);
		GridBagConstraints sendCmdGBS = new GridBagConstraints();
		sendCmdGBS.gridx = 1;
		sendCmdGBS.gridy = 0;
		sendCmdGBS.insets = new Insets(10,10,10,10);
		// for response fields
		Dimension responseFieldDim = new Dimension(Constants.mainFrameWidth*24/32,Constants.mainFrameHeight*19/100);
		GridBagConstraints responseFieldGBS = new GridBagConstraints();
		responseFieldGBS.gridx = 0;
		responseFieldGBS.gridy = 1;
		
		
		// creating text fields
		// cutter-robot
		cmdTxtR1 = new JTextField("command here - CUTTER-ROBOT");
		cmdTxtR1.setPreferredSize(cmdTxtDim);
		cmdTxtR1.setFont(new Font("Arial", Font.PLAIN, 12));
		cmdTxtR1.setToolTipText("enter command - CUTTER-ROBOT");
		// holder-robot
		cmdTxtR2 = new JTextField("command here - HOLDER-ROBOT");
		cmdTxtR2.setPreferredSize(cmdTxtDim);
		cmdTxtR2.setFont(new Font("Arial", Font.PLAIN, 12));
		cmdTxtR2.setToolTipText("enter command - HOLDER-ROBOT");
		// tracking-system
		cmdTxtTS = new JTextField("command here - TRACKING-SYSTEM");
		cmdTxtTS.setPreferredSize(cmdTxtDim);
		cmdTxtTS.setFont(new Font("Arial", Font.PLAIN, 12));
		cmdTxtTS.setToolTipText("enter command - TRACKING-SYSTEM");
		
		
		// creating send-buttons
		// cutter-robot
		sendCmdR1 = new JButton("<html>send command<br>CUTTER-ROBOT</html>");
		sendCmdR1.setPreferredSize(sendCmdDim);
		sendCmdR1.setFont(new Font("Arial", Font.BOLD, 11));
		// holder-robot
		sendCmdR2 = new JButton("<html>send command<br>HOLDER-ROBOT</html>");
		sendCmdR2.setPreferredSize(sendCmdDim);
		sendCmdR2.setFont(new Font("Arial", Font.BOLD, 11));
		// tracking-system
		sendCmdTS = new JButton("<html>send command<br>TRACKING</html>");
		sendCmdTS.setPreferredSize(sendCmdDim);
		sendCmdTS.setFont(new Font("Arial", Font.BOLD, 11));
		
		
		//creating response-fields
		//cutter-robot
		responseFieldR1 = new JTextPane();
		responseFieldR1.setPreferredSize(responseFieldDim);
		responseFieldR1.setEditable(false);
		//holder-robot
		responseFieldR2 = new JTextPane();
		responseFieldR2.setPreferredSize(responseFieldDim);
		responseFieldR2.setEditable(false);
		//tracking-system
		responseFieldTS = new JTextPane();
		responseFieldTS.setPreferredSize(responseFieldDim);
		responseFieldTS.setEditable(false);
		
		//adding components to rows
		//text fields for commands
		row1.add(cmdTxtR1,cmdTxtGBS);
		row2.add(cmdTxtR2,cmdTxtGBS);
		row3.add(cmdTxtTS,cmdTxtGBS);
		//send-buttons
		row1.add(sendCmdR1,sendCmdGBS);
		row2.add(sendCmdR2,sendCmdGBS);
		row3.add(sendCmdTS,sendCmdGBS);
		//response-text-pane
		row1.add(responseFieldR1,responseFieldGBS);
		row2.add(responseFieldR2,responseFieldGBS);
		row3.add(responseFieldTS,responseFieldGBS);
		
		
		
		//#########################
		//########LISTENERS########
		//#########################
		//creating FocusListener for each text field, so by pressing ENTER-KEY not all text fields trigger
		//cutter-robot
		FocusListener FocusListenerR1 = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				focusTxtR1 = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				focusTxtR1 = false;
			}
		};
		
		//cutter-robot
		FocusListener FocusListenerR2 = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				focusTxtR2 = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				focusTxtR2 = false;
			}
		};
		//cutter-robot
		FocusListener FocusListenerTS = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				focusTxtTS = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				focusTxtTS = false;
			}
		};
		
		//adding FocusListeners to text fields
		cmdTxtR1.addFocusListener(FocusListenerR1);
		cmdTxtR2.addFocusListener(FocusListenerR2);
		cmdTxtTS.addFocusListener(FocusListenerTS);
		
		
		//creating KeyListener for each text field, so the command can be send via ENTER-KEY
		//cutter-robot
		KeyListener cmdTxtKeyListenerR1 = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && focusTxtR1) sendCmd("cutter-robot");
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		};
		//holder-robot
		KeyListener cmdTxtKeyListenerR2 = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && focusTxtR2) sendCmd("holder-robot");
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		};
		//tracking-system
		KeyListener cmdTxtKeyListenerTS = new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER && focusTxtTS) sendCmd("tracking");
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		};
		
		//add KeyListeners to text fields
		cmdTxtR1.addKeyListener(cmdTxtKeyListenerR1);
		cmdTxtR2.addKeyListener(cmdTxtKeyListenerR2);
		cmdTxtTS.addKeyListener(cmdTxtKeyListenerTS);
		
		
		//add function to "send command"-buttons --> creating ActionListeners
		//cutter-robot
		ActionListener actionListenerSendCmdR1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCmd("cutter-robot");
			}
		};
		//holder-robot
		ActionListener actionListenerSendCmdR2 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCmd("holder-robot");
			}
		};
		//tracking-system
		ActionListener actionListenerSendCmdTS = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCmd("tracking");
			}
		};
		
		//add ActionListeners to JButtons
		sendCmdR1.addActionListener(actionListenerSendCmdR1);
		sendCmdR2.addActionListener(actionListenerSendCmdR2);
		sendCmdTS.addActionListener(actionListenerSendCmdTS);
	}
	
	/**
	 * Getter for cmdPane.
	 * @return cmdPane
	 */
	protected JPanel getCmdPane() {
		return cmdPane;
	}
	
	
	
	/**
	 * send command of a specified text field to the server
	 * @param serverType defines which server to communicate with
	 */
	private void sendCmd(String serverType) {
		String cmd;
		//get command
		if (serverType.equals("cutter-robot")) {
			cmd = cmdTxtR1.getText();
		} else if (serverType.equals("holder-robot")) {
			cmd = cmdTxtR2.getText();
		} else {
			cmd = cmdTxtTS.getText();
		}
		
		//if there is nothing to send, do nothing
		if (cmd.length() == 0) return; 
		
		//reset text field and send command if possible and write response into suitable response field
		//cutter-robot
		if (serverType.equals("cutter-robot")) {
			cmdTxtR1.setText("");
			if (!controller.sendR1(cmd)) {
				responseFieldR1.setText("An Error occured, maybe not connected?");
				return;
			}
			String response = controller.responseR1();
			if(response.contains("disconnected")){
				responseFieldR1.setText("Connection has been closed!");
			}else {
				responseFieldR1.setText(response);
			}
			//holder-robot
		} else if (serverType.equals("holder-robot")) {
			cmdTxtR2.setText("");
			if (!controller.sendR2(cmd)) {
				responseFieldR2.setText("An Error occured, maybe not connected?");
				return;
			}
			String response = controller.responseR2();
			if(response.contains("disconnected")){
				responseFieldR2.setText("Connection has been closed!");
			}else {
				responseFieldR2.setText(response);
			}
			//tracking-system
		} else {
			cmdTxtTS.setText("");
			if (!controller.sendTS(cmd)) {
				responseFieldTS.setText("An Error occured, maybe not connected?");
				return;
			}
			String response = controller.responseTS();
			if(response.contains("disconnected")){
				responseFieldTS.setText("Connection has been closed!");
			}else {
				responseFieldTS.setText(response);
			}
		}
	}
}
