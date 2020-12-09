package robprakt.graphics;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import robprakt.Main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class MainFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public MainFrame(String title) {
		//setType(Type.POPUP);
		setTitle(title);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton connect = new JButton("Verbinden");
		connect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				connect();
			}
		});
		contentPane.add(connect);
		
		JButton calibrate = new JButton("Kalibrieren");
		contentPane.add(calibrate);
		calibrate.setEnabled(false);
		
		JButton sendCommands = new JButton("Befehle senden");
		sendCommands.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("DUMMY!!!");
				System.out.println("Sending commands...");
			}
		});
		contentPane.add(sendCommands);
	}
	
	void connect() {
		System.out.println("DUMMY!!!");
		System.out.println("connected");
	}
}
