package robprakt.graphics;

import java.awt.Color;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import robprakt.Constants;
import robprakt.cutting.STLParser;
import robprakt.cutting.Triangle;
import robprakt.network.TCPClient;

/**
 * Class cmdMenu extends JPanel and contains components for the command-menu.
 * It implements functionality to send commands to servers.
 * @author Micha
 * @author Moritz
 */
public class CuttingMenu extends JPanel{
	
	/**
	 * cmdPane is split up into 3 JPanel rows containing components for communication
	 */
	private JPanel row1;
	
	/**
	 * command input text field for cutter-robot
	 */
	private JTextField cmdTxtR1;
	
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
	 * response text field of cutter-robot
	 */
	private JTextPane responseFieldR1;

	/**
	 * the controller
	 */
	private Controller controller;
	
	/**
	 * The MainFrame of the GUI.
	 */
	private MainFrame mainFrame;
	
	/**
	 * Create the frame.
	 */
	public CuttingMenu(Controller controller, MainFrame m) {
		this.controller = controller;
		this.mainFrame = m;
		
		
		//#########################
		//########COMPONENTS#######
		//#########################
		// splitting up cmdPane into 3 rows
		this.setLayout(new GridLayout(3,1));
		// generating JPanel with GridBagLayout
		row1 = new JPanel(new GridBagLayout());
		// adding rows to cmdPane
		add(row1);
		
		
		// defining dimensions and constraints of the components
		// for cmdTxt text fields
		Dimension cmdTxtDim = new Dimension(Constants.mainFrameWidth*24/32,Constants.mainFrameHeight/20);
		GridBagConstraints cmdTxtGBS1 = new GridBagConstraints();
		cmdTxtGBS1.gridx = 0;
		cmdTxtGBS1.gridy = 0;
		cmdTxtGBS1.anchor = GridBagConstraints.CENTER;
		
		
		// for "send command" buttons
		Dimension sendCmdDim = new Dimension(Constants.mainFrameWidth*3/20,Constants.mainFrameHeight/20);
		GridBagConstraints sendCmdGBS1 = new GridBagConstraints();
		sendCmdGBS1.gridx = 1;
		sendCmdGBS1.gridy = 0;
		sendCmdGBS1.insets = new Insets(10,10,10,10);
		
		
		// for response fields
		Dimension responseFieldDim = new Dimension(Constants.mainFrameWidth*24/32,Constants.mainFrameHeight*19/100);
		GridBagConstraints responseFieldGBS1 = new GridBagConstraints();
		responseFieldGBS1.gridx = 0;
		responseFieldGBS1.gridy = 1;
		
		
		// creating text fields
		// cutter-robot
		cmdTxtR1 = new JTextField("-- Can be leaved empty --");
		cmdTxtR1.setPreferredSize(cmdTxtDim);
		cmdTxtR1.setFont(new Font("Arial", Font.PLAIN, 12));
		cmdTxtR1.setForeground(Color.LIGHT_GRAY);
		
		
		// creating send-buttons
		// cutter-robot
		sendCmdR1 = new JButton("Get Cutting Triangles");
		sendCmdR1.setPreferredSize(sendCmdDim);
		sendCmdR1.setFont(new Font("Arial", Font.BOLD, 11));
		
		
		//creating response-fields
		//cutter-robot
		responseFieldR1 = new JTextPane();
		responseFieldR1.setPreferredSize(responseFieldDim);
		responseFieldR1.setEditable(false);
		
		//adding components to rows
		//text fields for commands
		row1.add(cmdTxtR1,cmdTxtGBS1);
		//send-buttons
		row1.add(sendCmdR1,sendCmdGBS1);
		//response-text-pane
		row1.add(responseFieldR1,responseFieldGBS1);
		
		
		//add function to "send command"-buttons --> creating ActionListeners
		//cutter-robot
		ActionListener actionListenerSendCmdR1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTriangles();
			}
		};
		
		//add ActionListeners to JButtons
		sendCmdR1.addActionListener(actionListenerSendCmdR1);
	}
	
	/**
	 * send command of a specified text field to the server
	 * @param serverType defines which server to communicate with
	 */
	private void getTriangles() {
		ArrayList<Triangle> triangles;
		JFileChooser fileChooser = new JFileChooser(new File("."));
		fileChooser.setLocale(Locale.GERMANY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("STL 3D Object Files", "stl");
		fileChooser.setFileFilter(filter);
		int choice = fileChooser.showOpenDialog(null);
		if(choice == JFileChooser.APPROVE_OPTION) {
			try {
				triangles = (ArrayList<Triangle>) STLParser.parseSTLFile(fileChooser.getSelectedFile().toPath());
				for(Triangle t: triangles) {
					System.out.println(t);
				}
			} catch (IOException e) {
				System.err.println("Error on reading STL File");
				e.printStackTrace();
			}
		}else {
		   System.err.println("No File Chosen!");
		   return;
	   }
	}
}
