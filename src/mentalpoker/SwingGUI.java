package mentalpoker;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SwingGUI extends JPanel implements ActionListener {
	JPanel cards;
	protected JTextField usernameField;
	protected JButton startButton;
	protected JLabel usernameLabel;
	protected static JFrame frame;
	protected static JFrame joinOrHostGameFrame;
	private Poker poker;
	private JComboBox numberOfSlots;
	private JPanel slotChoiceLayout;
	private JPanel joinOrHostPagePanel;
	
	//Names of the panes
	final static String usernameInputTitle = "usernameInputPane";
	final static String joinOrHostTitle = "joinOrHostPane";
	
	public void addComponentToPane(Container pane)
	{
		
		/**
		 * Username input card
		 */
		JPanel usernameInputPaneLine1 = new JPanel();
		usernameLabel = new JLabel("Give us your username:");
		usernameField = new JTextField(20);
		startButton = new JButton();
		startButton.setText("Set Username");
		startButton.addActionListener(this);
		usernameInputPaneLine1.add(usernameLabel);
		usernameInputPaneLine1.add(usernameField);
		
		
		JPanel usernameInputPaneGridLayout = new JPanel(new GridLayout(0,1));
		usernameInputPaneGridLayout.add(usernameInputPaneLine1);
		
		JPanel usernameInputButtons = new JPanel();
		JButton continueButton = new JButton("Continue");
		continueButton.setActionCommand("setUsername");
		continueButton.addActionListener(this);
		usernameInputButtons.add(continueButton);
		
		usernameInputPaneGridLayout.add(usernameInputButtons);
	
		/**
		 * Game choice card
		 */
		
		JPanel joinOrHostPageOuterLayout = new JPanel(new GridLayout(0,1));
		joinOrHostPagePanel = new JPanel();
		
		JButton joinGame = new JButton("Join Game");
		JButton hostGame = new JButton("Host Game");
		joinGame.setActionCommand("joinGame");
		hostGame.setActionCommand("hostGame");
		joinGame.addActionListener(this);
		hostGame.addActionListener(this);
		joinOrHostPagePanel.add(hostGame);
		joinOrHostPagePanel.add(joinGame);
		
		//Create a textbox and input button to display the game host choices
		slotChoiceLayout = new JPanel(new GridLayout(0,1));
		String[] slots = {"1","2","3","4","5","6","7","8","9","10"};
		numberOfSlots = new JComboBox(slots);
		JButton startGameButton = new JButton("Start Game");
		startGameButton.setActionCommand("startGameWithSlots");
		startGameButton.addActionListener(this);
		slotChoiceLayout.add(numberOfSlots);
		slotChoiceLayout.add(startGameButton);
		slotChoiceLayout.setVisible(false); //This is hidden, will be shown later.
		
		joinOrHostPageOuterLayout.add(joinOrHostPagePanel);
		joinOrHostPageOuterLayout.add(slotChoiceLayout);
		
		
		cards = new JPanel(new CardLayout());
		cards.add(usernameInputPaneGridLayout, usernameInputTitle);
		cards.add(joinOrHostPageOuterLayout,joinOrHostTitle);
		
		
		pane.add(cards, BorderLayout.CENTER);
	}
	
	public static void showGUI()
	{
		frame = new JFrame("COMS4507 mental poker: Ben Evans and Emile Victor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension(400,400));
		
		SwingGUI sgui = new SwingGUI();
		
		sgui.addComponentToPane(frame.getContentPane());
		 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	public void usernameAccepted(String username)
	{
		Poker.setGameUser(new User(username));
		
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if ("setUsername".equals(arg0.getActionCommand()))
		{
			String username = usernameField.getText();
			usernameAccepted(username);
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, joinOrHostTitle);
			try {
				poker = new Poker();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else if ("hostGame".equals(arg0.getActionCommand()))
		{
			joinOrHostPagePanel.setVisible(false);
			
			slotChoiceLayout.setVisible(true);
		}
	}
	
}
