package robprakt.graphics;

import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

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
	 * Controller for actions
	 */
	private Controller controller;
	
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
		
		// tabbedPane as basic pane for navigating between menus
		tabbedContentPane = new JTabbedPane();
		tabbedContentPane.setBackground(Color.LIGHT_GRAY);
		setContentPane(tabbedContentPane);
		
		// creating container hierarchy for menus
		connectionMenu = new connectionMenu(controller);
		cmdMenu = new cmdMenu(controller);
		
		// adding menus to tabbedPane
		tabbedContentPane.add("connections",connectionMenu);
		tabbedContentPane.add("commands",cmdMenu);
		
		revalidate();
	}
}
