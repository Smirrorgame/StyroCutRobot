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
import robprakt.cutting.CuttingLogic;
import robprakt.cutting.RobotMovement;
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
	private JPanel row2;
	private JPanel row3;
	
	/**
	 * command input text field for cutter-robot
	 */
	private JTextField cmdTxtR1;

	/**
	 * button for sending command from cmdTxtR1 text field to cutter-robot
	 */
	private JButton sendCmdR1;
	
	/**
	 * button for starting cutting process
	 */
	private JButton startCutting;
	
	/**
	 * button for starting controls of manual simulation
	 */
	private JButton manualSimulationNextStep;
	
	/**
	 * button for starting controls of manual simulation
	 */
	private JButton manualSimulationStepBack;
	
	/**
	 * if manual simulation is active, then this is true
	 */
	private boolean manualSimulationIsActive; //TODO: überflüssig
	
	/**
	 * response text field of cutter-robot
	 */
	private JTextPane responseFieldR1;

	/**
	 * the controller
	 */
	private Controller controller;
	
	/**
	 * List contains triangles from the STL file, that are describing the model.
	 * The number of triangles determines the number of cuts to completely cut the object.
	 */
	//TODO: Die Triangle müssen nach dem laden hier gespeichert werden.
	private ArrayList<Triangle> triangles;
	
	private int currentStepInManualSimulation = -1;
	
	/**
	 * Create the frame.
	 */
	public CuttingMenu(Controller controller) {
		this.controller = controller;
		
		
		//#########################
		//########COMPONENTS#######
		//#########################
		// splitting up cmdPane into 3 rows
		this.setLayout(new GridLayout(3,1));
		// generating JPanel with GridBagLayout
		row1 = new JPanel(new GridBagLayout());
		row2 = new JPanel(new GridBagLayout());
		row3 = new JPanel(new GridBagLayout());
		// adding rows to cmdPane
		add(row1);
		add(row2);
		add(row3);
		
		
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
		
		// for startCutting button
		Dimension startCuttingDim = new Dimension(Constants.mainFrameWidth*6/20,Constants.mainFrameHeight/20);
		GridBagConstraints startCuttingGBS = new GridBagConstraints();
		startCuttingGBS.gridx = 0;
		startCuttingGBS.gridy = 0;
		startCuttingGBS.anchor = GridBagConstraints.CENTER;
		startCuttingGBS.insets = new Insets(10,10,10,10);
		
		// for activateSimulationControl button
		Dimension activateSimulationControlDim = new Dimension(Constants.mainFrameWidth*6/20,Constants.mainFrameHeight/20);
		GridBagConstraints activateSimulationControlGBS = new GridBagConstraints();
		activateSimulationControlGBS.gridx = 0;
		activateSimulationControlGBS.gridy = 0;
		activateSimulationControlGBS.anchor = GridBagConstraints.CENTER;
		activateSimulationControlGBS.insets = new Insets(10,10,10,10);
		
		Dimension manualSimulationStepBackDim = new Dimension(Constants.mainFrameWidth*6/20,Constants.mainFrameHeight/20);
		GridBagConstraints manualSimulationStepBackGBS = new GridBagConstraints();
		manualSimulationStepBackGBS.gridx = 1;
		manualSimulationStepBackGBS.gridy = 0;
		manualSimulationStepBackGBS.anchor = GridBagConstraints.CENTER;
		manualSimulationStepBackGBS.insets = new Insets(10,10,10,10);
		
		
		
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
		
		// creating startCutting button
		startCutting = new JButton("START CUTTING");
		startCutting.setPreferredSize(startCuttingDim);
		startCutting.setFont(new Font("Arial", Font.BOLD, 11));
		
		// creating activateManualSimilation button
		manualSimulationNextStep = new JButton("SIMULATION NEXT STEP");
		manualSimulationNextStep.setPreferredSize(activateSimulationControlDim);
		manualSimulationNextStep.setFont(new Font("Arial", Font.BOLD, 11));
		
		
		manualSimulationStepBack = new JButton("SIMULATION STEP BACK");
		manualSimulationStepBack.setPreferredSize(manualSimulationStepBackDim);
		manualSimulationStepBack.setFont(new Font("Arial", Font.BOLD, 11));
		
		//adding components to rows
		//text fields for commands
		row1.add(cmdTxtR1,cmdTxtGBS1);
		//send-buttons
		row1.add(sendCmdR1,sendCmdGBS1);
		//response-text-pane
		row1.add(responseFieldR1,responseFieldGBS1);
		
		//startCutting button
		row2.add(startCutting,startCuttingGBS);
		
		//manualSimulationNextStep
		row3.add(manualSimulationNextStep,activateSimulationControlGBS);
		
		row3.add(manualSimulationStepBack,manualSimulationStepBackGBS);
		
		//#########################
		//########LISTENERS########
		//#########################
		//add function to "send command"-buttons --> creating ActionListeners
		//cutter-robot
		ActionListener actionListenerSendCmdR1 = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getTriangles();
			}
		};
		
		ActionListener actionListenerStartCutting = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// check if a working connection to robot servers has been established
				TCPClient clientR1 = controller.getClientR1();
				TCPClient clientR2 = controller.getClientR2();
				if(clientR1 == null || clientR2 == null) throw new NullPointerException(
						"[CuttingMenu] At least one TCP-Client is not initialized.");
		//		if(!controller.send("IsAdept", clientR1) && !controller.send("IsAdept", clientR2)) throw new IllegalStateException(
		//				"[CuttingMenu] At least one connection to a robot server is not healthy. Check IP-address and port of connection.");
				//TODO: Beim praktischen Test an den Roboter Servern gerne auskommentieren.
				// check if there is already a cutting process running, if so the START-CUTTING button is disabled
				if(!CuttingLogic.isCuttingActive()) {
					CuttingLogic cuttingLogic = new CuttingLogic(clientR1,clientR2,triangles);
					try {
						cuttingLogic.cut();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		
		/**
		 * ActionListener for manual simulation button
		 * sets manualSimulationIsActive to true
		 */
		ActionListener actionListenerManualSimulationStatus = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!CuttingLogic.isCuttingActive()) {
					manualSimulationIsActive = true;
			
					if(!CuttingLogic.isCuttingActive())
					{
						manualSimulationIsActive = true;
			        	if(false) currentStepInManualSimulation--;
			        	if(currentStepInManualSimulation < RobotMovement.commandsDuringCutting.size()) currentStepInManualSimulation++;
			        	//should only be executed, when cutting process has been finished
					    controller.send(RobotMovement.commandsDuringCutting.get(currentStepInManualSimulation), RobotMovement.clientForEachCommand.get(currentStepInManualSimulation));
					    controller.response(RobotMovement.clientForEachCommand.get(currentStepInManualSimulation));
					}
						
				}
			}
		};
		
		ActionListener actionListenerManualSimulationStatusDecrease = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!CuttingLogic.isCuttingActive()) {
					manualSimulationIsActive = true;
			
					if(!CuttingLogic.isCuttingActive())
					{
						manualSimulationIsActive = true;
			        	if(currentStepInManualSimulation > 0) currentStepInManualSimulation--;
			        	
			        	//should only be executed, when cutting process has been finished
					    controller.send(RobotMovement.commandsDuringCutting.get(currentStepInManualSimulation), RobotMovement.clientForEachCommand.get(currentStepInManualSimulation));
					    controller.response(RobotMovement.clientForEachCommand.get(currentStepInManualSimulation));
					}
						
				}
			}
		};
		
		//add ActionListeners to JButtons
		sendCmdR1.addActionListener(actionListenerSendCmdR1);
		startCutting.addActionListener(actionListenerStartCutting);
		manualSimulationNextStep.addActionListener(actionListenerManualSimulationStatus);
		manualSimulationStepBack.addActionListener(actionListenerManualSimulationStatusDecrease);
	}
	
	/**
	 * send command of a specified text field to the server
	 * @param serverType defines which server to communicate with
	 */
	private void getTriangles() {
		JFileChooser fileChooser = new JFileChooser(new File("."));
		fileChooser.setLocale(Locale.GERMANY);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("STL 3D Object Files", "stl");
		fileChooser.setFileFilter(filter);
		int choice = fileChooser.showOpenDialog(null);
		if(choice == JFileChooser.APPROVE_OPTION) {
			try {
				this.triangles = (ArrayList<Triangle>) STLParser.parseSTLFile(fileChooser.getSelectedFile().toPath());
				for(Triangle t: this.triangles) {
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
