package mentalpoker;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SwingGUI extends JPanel implements ActionListener {

	protected JTextField usernameField;
	protected JButton startButton;
	protected JLabel usernameLabel;
	
	public SwingGUI() {
		super(new GridBagLayout());
		
		usernameLabel = new JLabel("Give us your username:");
		usernameField = new JTextField(20);
		startButton = new JButton();
		startButton.setText("Set Username");
		startButton.addActionListener(this);
		
		GridBagConstraints constr = new GridBagConstraints();
		constr.gridwidth = GridBagConstraints.REMAINDER;
		constr.fill = GridBagConstraints.HORIZONTAL;
		add(usernameLabel, constr);
		add(usernameField, constr);
		add(startButton,constr);
	    
	}
	
	
	public static void showGUI()
	{
		JFrame frame = new JFrame("COMS4507 mental poker: Ben Evans and Emile Victor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(400,400));
		frame.add(new SwingGUI());
		 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		String username = usernameField.getText();
		System.out.println(username);
		//Do stuff with it.
	}
	
}
