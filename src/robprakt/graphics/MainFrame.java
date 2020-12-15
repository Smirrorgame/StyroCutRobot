package robprakt.graphics;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import robprakt.network.TCPClient;

public class MainFrame extends JFrame {
	
	/**
	 * Panels
	 */
	private JLayeredPane layeredPane;
	
	protected CmdPane cmdPane;
	
	protected ConnectPane connectPane;
	
	private JPanel contentPane;

	/**
	 * Buttons
	 */
	protected JButton btnCmd, btnConnect;

	/**
	 * Controller for actions
	 */
	private Controller controller;
	
	
	/**
	 * Network Entities
	 */
	private TCPClient client;
	
	/**
	 * Create the main frame.
	 */
	public MainFrame(String title) {
		setResizable(false);
		setTitle(title);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		// controller erstellen
		controller = new Controller(this);
		

		contentPane = new JPanel();
		contentPane.setBackground(Color.LIGHT_GRAY);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// Knopf zum Öffnen des Verbindungsmenüs
		btnConnect = new JButton("Verbinden");
		btnConnect.setBounds(210, 11, 170, 70);
		contentPane.add(btnConnect);
		// Knopf zum Öffnen des Kommandomenüs
		btnCmd = new JButton("Direkte Kommandos");
		btnCmd.setBounds(390, 11, 170, 70);
		contentPane.add(btnCmd);
		
		//containerpanel für cmd und connect layout
		layeredPane = new JLayeredPane();
		layeredPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		layeredPane.setBounds(0, 150, 784, 410);
		layeredPane.setLayout(new CardLayout(0, 0));
		
		connectPane = new ConnectPane(controller);
		layeredPane.add(connectPane, "name_266637540072800");
		connectPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		connectPane.setLayout(null);
		
		cmdPane = new CmdPane(controller);
		layeredPane.add(cmdPane, "name_266637553746400");
		cmdPane.setLayout(null);
		
		contentPane.add(layeredPane);
		revalidate();
		
		//Listener initialisieren
		controller.initialListeners();
	}
}
