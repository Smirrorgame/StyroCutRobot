package robprakt.graphics;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import robCalibration.QR24;
import robprakt.Constants;

public class MainFrame extends JFrame {
	
	/**
	 * Tabbed ContentPane for managing menus
	 */
	private JTabbedPane tabbedContentPane;
	
	/**
	 * Contains graphics structure for the connection menu
	 */
	private connectionMenu connectionMenu;
	
	/**
	 * Contains graphics structure for the command menu
	 */
	private cmdMenu cmdMenu;
	
	/**
	 * Contains graphics structure for the calibration menu
	 */
	private CalibrationMenu calibrationMenu;
	
	/**
	 * Contains graphics structure for the cutting menu
	 */
	private CuttingMenu cuttingMenu;
	
	/**
	 * Controller for actions
	 */
	private Controller controller;
	
	/**
	 * calibration contains functions for calibrating robots
	 */
	private QR24 calibration;
	
	/**
	 * Create the main frame.
	 */
	public MainFrame(String title) {
		
		//settings for window
		setResizable(false);
		setTitle(title);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(	(int) Toolkit.getDefaultToolkit().getScreenSize().getWidth()-Constants.mainFrameWidth,
					(int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2-Constants.mainFrameHeight/2,
					Constants.mainFrameWidth, Constants.mainFrameHeight);

		// create controller
		controller = new Controller(this);
		
		//create calibration object
		calibration = new QR24(controller);
		
		// tabbedPane as basic pane for navigating between menus
		tabbedContentPane = new JTabbedPane();
		tabbedContentPane.setBackground(Color.LIGHT_GRAY);
		setContentPane(tabbedContentPane);
		
		// creating container hierarchy for menus
		connectionMenu = new connectionMenu(controller);
		cmdMenu = new cmdMenu(controller, this);
		calibrationMenu = new CalibrationMenu(controller, this, calibration);
		cuttingMenu = new CuttingMenu(controller, this);
		
		// adding menus to tabbedPane
		tabbedContentPane.add("Connections",connectionMenu);
		tabbedContentPane.add("Commands",cmdMenu);
		tabbedContentPane.add("Calibration",calibrationMenu);
		tabbedContentPane.add("Cutting",cuttingMenu);
		
		revalidate();
		
	}
	
	/**
	 * Getter for the connectionMenu.
	 * Used for updating connection status buttons.
	 * @return connectionMenu contains GUI components of the connection tab
	 */
	protected connectionMenu getConnectionMenu() {
		return this.connectionMenu;
	}
}
