package robprakt.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controller {
	
	private MainFrame frame;
	
	public Controller(MainFrame frame) {
		this.frame = frame;
		
		
		
		frame.btnMenu.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.cmdPane.setVisible(false);
				frame.menuPane.setVisible(true);
				frame.revalidate();			
			}
		});
		
		frame.btnCmd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.cmdPane.setVisible(true);
				frame.menuPane.setVisible(false);
				frame.revalidate();			
			}
		});
		
		
		
		
	}
	
	

}
