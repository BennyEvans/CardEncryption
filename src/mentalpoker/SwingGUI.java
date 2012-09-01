package mentalpoker;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.avis.client.InvalidSubscriptionException;

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
	private JLabel warning;
	
	//Names of the panes
	final static String usernameInputTitle = "usernameInputPane";
	final static String joinOrHostTitle = "joinOrHostPane";
	final static String slotChoiceTitle = "slotChoiceTitle";
	
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
		
		warning = new JLabel("You must provide a username");
		warning.setForeground(Color.red);
		warning.setVisible(false);
		
		usernameInputPaneGridLayout.add(usernameInputButtons);
		usernameInputPaneGridLayout.add(warning);
		usernameInputPaneGridLayout.setBorder(new EmptyBorder(10, 10, 10, 10));
	
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
		slotChoiceLayout.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel hostingGameTitle = new JLabel("Set up your game...");
		JLabel numberOfSlotsInstruction = new JLabel("Select a number of slots to use:");
		
		String[] slots = {"1","2","3","4","5","6","7","8","9","10"};
		numberOfSlots = new JComboBox(slots);
		JButton startGameButton = new JButton("Start Game");
		startGameButton.setActionCommand("startGameWithSlots");
		startGameButton.addActionListener(this);
		slotChoiceLayout.add(hostingGameTitle);
		slotChoiceLayout.add(numberOfSlotsInstruction);
		slotChoiceLayout.add(numberOfSlots);
		slotChoiceLayout.add(startGameButton);
		slotChoiceLayout.setVisible(false); //This is hidden, will be shown later.
		slotChoiceLayout.setLayout(new BoxLayout(slotChoiceLayout, BoxLayout.Y_AXIS));
		
		joinOrHostPageOuterLayout.add(joinOrHostPagePanel);
		//joinOrHostPageOuterLayout.add(slotChoiceLayout);
		
		
		cards = new JPanel(new CardLayout());
		cards.add(usernameInputPaneGridLayout, usernameInputTitle);
		cards.add(joinOrHostPageOuterLayout,joinOrHostTitle);
		cards.add(slotChoiceLayout,slotChoiceTitle);
		
		
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
			if (!username.equals(""))
			{
				usernameAccepted(username);
				CardLayout cl = (CardLayout)(cards.getLayout());
				cl.show(cards, joinOrHostTitle);
				try {
					poker = new Poker();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				warning.setVisible(true);
			}
			
			
		} else if ("hostGame".equals(arg0.getActionCommand()))
		{
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, slotChoiceTitle);
		} else if ("startGameWithSlots".equals(arg0.getActionCommand()))
		{
			try {
				poker.StartGame(true,(numberOfSlots.getSelectedIndex()+1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
