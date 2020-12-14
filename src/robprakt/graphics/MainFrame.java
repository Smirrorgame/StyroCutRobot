package robprakt.graphics;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.border.EmptyBorder;
import java.awt.CardLayout;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class MainFrame extends JFrame {
	
	/**
	 * Panels
	 */
	private JLayeredPane layeredPane;
	
	protected CmdPane cmdPane;
	
	protected ConnectPane menuPane;
	
	private JPanel contentPane;

	/**
	 * Buttons
	 */
	protected JButton btnCmd, btnMenu;

	/**
	 * Controller for actions
	 */
	private Controller controller;
	
	
	/**
	 * Create the main frame.
	 */
	public MainFrame(String title) {
		setTitle(title);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		btnMenu = new JButton("Verbinden");
		btnMenu.setBounds(173, 11, 170, 70);
		contentPane.add(btnMenu);
		
		btnCmd = new JButton("Direkte Kommandos");
		btnCmd.setBounds(374, 11, 170, 70);
		contentPane.add(btnCmd);
		
		layeredPane = new JLayeredPane();
		layeredPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		layeredPane.setBounds(0, 150, 784, 410);
		layeredPane.setLayout(new CardLayout(0, 0));
		
		menuPane = new ConnectPane();
		layeredPane.add(menuPane, "name_266637540072800");
		menuPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		cmdPane = new CmdPane();
		layeredPane.add(cmdPane, "name_266637553746400");
		cmdPane.setLayout(null);
		
		contentPane.add(layeredPane);
		// setting the controller
		controller = new Controller(this);
		revalidate();
	}
}
