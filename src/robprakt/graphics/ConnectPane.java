package robprakt.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class ConnectPane extends JPanel {

	/**
	 * Create the frame.
	 */
	public ConnectPane() {
		setBounds(100, 100, 800, 600);
		setLayout(null);
		
		JButton connectR1 = new JButton("Verbinden mit Laser-Roboter");
		connectR1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		connectR1.setBounds(10, 243, 200, 91);
		add(connectR1);
		
		JButton connectR2 = new JButton("Verbinden mit Halterungs-Roboter");
		connectR2.setBounds(549, 243, 200, 91);
		add(connectR2);
		connectR2.setEnabled(false);
		
		JButton anderes = new JButton("Anderes?! Noch undefiniert");
		anderes.setBounds(280, 240, 200, 97);
		anderes.setEnabled(false);
		add(anderes);
		
		
		connectR1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Hier verbindet man sich dann mit dem Laser Roboter");
			}
		});
	}
}
