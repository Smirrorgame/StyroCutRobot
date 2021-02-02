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
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;

import robprakt.cutting.TransformCoords;
import robCalibration.QR24;
import robprakt.Constants;
import robprakt.network.TCPClient;

public class CalibrationMenu extends JPanel{
	
	/**
	 * GUI Controller
	 */
	private Controller controller;
	
	/**
	 * MainFrame of GUI. Used for accessing buttons of connectionMenu.
	 */
	private MainFrame mainFrame;
	
	/**
	 * calibration object
	 */
	private QR24 calibration;
	
	/**
	 * Text field for new number of measurements
	 */
	private JFormattedTextField txtNumMsr;
	
	/**
	 * Button for setting new value for number of measurements [QR24]
	 */
	private JButton numBtn;
	
	/**
	 * Button for setting new workspace midpoint [QR24]
	 */
	private JButton midpointBtn;
	
	/**
	 * Buttons for starting calibration sequence.
	 */
	private JButton btnCalR1,btnCalR2;
	

	public CalibrationMenu(Controller c,MainFrame m, QR24 cal) {
		this.controller = c;
		this.calibration = cal;
		this.mainFrame = m;
		
		//#########################
		//########COMPONENTS#######
		//#########################
		
		//creating grid for calibration-tab
		this.setLayout(new GridLayout(3,1));
		
		//creating rows for the grid with GridBagLayout
		JPanel row1 = new JPanel(new GridBagLayout());
		JPanel row2 = new JPanel(new GridBagLayout());
		JPanel row3 = new JPanel(new GridBagLayout());
		
		//adding rows to CalibrationMenu
		this.add(row1);
		this.add(row2);
		this.add(row3);
		
		
		//defining GridBagLayout
		//text field for number of measurements
		Dimension txtFieldDim = new Dimension(Constants.mainFrameWidth/20,Constants.mainFrameHeight/20);
		GridBagConstraints txtGBS = new GridBagConstraints();
		txtGBS.gridx = 0;
		txtGBS.gridy = 0;
		Insets insets = new Insets(10,10,10,10);
		txtGBS.insets = insets;
		//button for number of measurements
		Dimension numBtnDim = new Dimension(Constants.mainFrameWidth/4,Constants.mainFrameHeight/6);
		GridBagConstraints numBtnGBS = new GridBagConstraints();
		numBtnGBS.gridx = 1;
		numBtnGBS.gridy = 0;
		
		//button for setting new localWorkspaceMidpoint
		Dimension midpointBtnDim = new Dimension(Constants.mainFrameWidth/3,Constants.mainFrameHeight/5);
		GridBagConstraints midPointBtnGBS = new GridBagConstraints();
		midPointBtnGBS.gridx = 0;
		midPointBtnGBS.gridy = 0;
		
		
		//buttons for calibration START
		//cutter-robot
		Dimension btnCalDim = new Dimension(Constants.mainFrameHeight/3,Constants.mainFrameHeight/10);
		GridBagConstraints btnCalGBS1 = new GridBagConstraints();
		btnCalGBS1.gridx = 0;
		btnCalGBS1.gridy = 0;
		btnCalGBS1.insets = insets;
		//holder-robot
		GridBagConstraints btnCalGBS2 = new GridBagConstraints();
		btnCalGBS2.gridx = 1;
		btnCalGBS2.gridy = 0;
		btnCalGBS2.insets = insets;
		
		
		//format for integer text fields
		NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setAllowsInvalid(false);
		
		
		//creating text fields for number of measurements
		txtNumMsr = new JFormattedTextField(format);
		txtNumMsr.setPreferredSize(txtFieldDim);
		txtNumMsr.setFont(new Font("Arial", Font.PLAIN, 12));
		//creating button for entering new number of measurement value
		numBtn = new JButton("<html><center>Set new NUMBER of Measurements<br><b>CURRENT VALUE:<br>" + Constants.DEFAULT_NUM_MEASUREMENTS + "</b></center></html>");
		numBtn.setPreferredSize(numBtnDim);
		numBtn.setFont(new Font("Arial", Font.PLAIN, 15));
		
		//creating button for setting new working point
		midpointBtn = new JButton("<html><center>Setting current CUTTER-ROBOT<br>pose to <b>midpoint</b>.</center></html>");
		midpointBtn.setPreferredSize(midpointBtnDim);
		midpointBtn.setFont(new Font("Arial", Font.PLAIN, 15));
		
		//creating buttons for starting calibration of robots
		btnCalR1 = new JButton("<html><center><b>START CALIBRATION</b><br><i>CUTTER-ROBOT</i></center></html>");
		btnCalR2 = new JButton("<html><center><b>START CALIBRATION</b><br><i>HOLDER-ROBOT</i></center></html>");
		btnCalR1.setPreferredSize(btnCalDim);
		btnCalR2.setPreferredSize(btnCalDim);
		btnCalR1.setFont(new Font("Arial", Font.PLAIN, 15));
		btnCalR2.setFont(new Font("Arial", Font.PLAIN, 15));
		
		//adding components calibrationMenu
		row1.add(txtNumMsr,txtGBS);
		row1.add(numBtn,numBtnGBS);
		row2.add(midpointBtn,midPointBtnGBS);
		row3.add(btnCalR1,btnCalGBS1);
		row3.add(btnCalR2,btnCalGBS2);
		
		//#########################
		//########LISTENERS########
		//#########################

		//ActionListener for changing number of measurements
		ActionListener actionListenerNumMeasure = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//parse from String to int
				int newNum = Constants.DEFAULT_NUM_MEASUREMENTS; 
				try
				    {
					 newNum = Integer.parseInt(txtNumMsr.getText().trim());
				      System.out.println("new number of measurements = " + newNum);
				    }
				    catch (NumberFormatException nfe)
				    {
				      System.out.println("[Calibration Menu] NumberFormatException: The String couldn't be converted to an Integer.");
				    }
				Color clr = calibration.setNumberOfMeasurements(newNum) ? Color.GREEN : Color.RED;
				numBtn.setText("<html><center>Set new NUMBER of Measurements<br><b>CURRENT VALUE:<br>" + newNum + "</b></center></html>");
				numBtn.setBackground(clr);
			}
		};
		//setting localWorkspaceMidpoint
		ActionListener actionListeneMidpointBtn = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//send pose request to cutter-robot
				TCPClient client = controller.getClientR1();
				if(!controller.send("GetPositionHomRowWise", client)) {
					midpointBtn.setText("<html><center><b>SENDING TO ROBOT FAILED</b><br>Setting current CUTTER-ROBOT<br>pose to <b>midpoint</b>.</center></html>");
					midpointBtn.setBackground(Color.RED);
					return;
				};
				//if send was successful -> save homogeneous matrix in localWorkspaceMidpoint of QR24 calibration object
				String response = controller.response(client);
				if(response==null) {
					System.out.println("[CalibrationMenu] Error, check connection to cutting Robot!");
					midpointBtn.setBackground(Color.RED);
					return;
				}
				double[] doubleArray = Constants.convertPoseDataToDoubleArray(response, 0);
				calibration.setLocalWorkspaceMidpoint(doubleArray);
				calibration.setInitialMarkerPose(doubleArray);
				TransformCoords.initialWorkspacePositionRelCutterRobot = new ArrayRealVector(new double[] {doubleArray[3],doubleArray[3+4],doubleArray[3+4+4]});
				midpointBtn.setBackground(Color.GREEN);
				//TODO: Stelle sicher, dass das Setzen des Mittelpunkts des Arbeitsraums nach dem Start einer Kalibrierung
				//TODO: nicht nochmal durchgeführt werden kann.
			}
		};
		//starting calibration process for cutter-robot
		ActionListener actionListenerbtnCalR1 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TCPClient client = controller.getClientR1();
				try {
					if(calibration.measuring(client)) {
						btnCalR1.setBackground(Color.GREEN);
						System.out.println("########## measuring finished #############");
						RealMatrix[] XY = calibration.calibrate();
						calibration.printTable(XY[0]);
						calibration.printTable(XY[1]);
						TransformCoords.cutterRobotToTrackingSystem = XY[1]; //setting matrix for cutting-procedure
						return;
					}
					btnCalR1.setBackground(Color.RED);
					return;
				} catch (InterruptedException e1) {
					System.out.println("During calibration something went wrong. Please visit: [QR24 --> measuring] ");
				} catch (Exception e1) {
					System.out.println("During calibration something went wrong. Please visit: [QR24 --> calibrate] ");
					e1.printStackTrace();
				}
			}
		};
		//starting calibration process for holder-robot
		ActionListener actionListenerbtnCalR2 = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TCPClient client = controller.getClientR2();
				try {
					if(calibration.measuring(client)) {
						btnCalR2.setBackground(Color.GREEN);
						System.out.println("########## measuring finished #############");
						RealMatrix[] XY = calibration.calibrate();
						calibration.printTable(XY[0]);
						calibration.printTable(XY[1]);
						TransformCoords.holderRobotToTrackingSystem = XY[1]; //setting matrix for cutting-procedure
						return;
					}
					btnCalR2.setBackground(Color.RED);
					return;
				} catch (InterruptedException e1) {
					System.out.println("During calibration something went wrong. Please visit: [QR24 --> measuring]");
				} catch (Exception e1) {
					System.out.println("During calibration something went wrong. Please visit: [QR24 --> calibrate] ");
					e1.printStackTrace();
				}
			}
		};
		
		//adding actionListeners to buttons
		numBtn.addActionListener(actionListenerNumMeasure);
		midpointBtn.addActionListener(actionListeneMidpointBtn);
		btnCalR1.addActionListener(actionListenerbtnCalR1);
		btnCalR2.addActionListener(actionListenerbtnCalR2);
	}
}
