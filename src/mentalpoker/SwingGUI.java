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
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import org.avis.client.InvalidSubscriptionException;

public class SwingGUI extends JPanel implements ActionListener {
	JPanel cards;
	protected JTextField usernameField;
	protected JButton startButton;
	protected JLabel usernameLabel;
	protected static JFrame frame;
	protected static JFrame joinOrHostGameFrame;
	static Poker poker;
	static JComboBox numberOfSlots;
	private JPanel slotChoiceLayout;
	private JPanel joinOrHostPagePanel;
	private JLabel warning;
	public HostGameTask hgt;
	ArrayList<JPanel> userBoxes;
	JPanel hostingScreenGridLayout;
	GridBagConstraints constr;

	//Names of the panes
	final static String usernameInputTitle = "usernameInputPane";
	final static String joinOrHostTitle = "joinOrHostPane";
	final static String slotChoiceTitle = "slotChoiceTitle";
	final static String hostingScreenGridLayoutTitle = "hostingScreenGridLayoutTitle";

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


		/**
		 * Hosting game screen (only seen if user is hosting game).
		 */
		
		hostingScreenGridLayout = new JPanel(new GridBagLayout());
		
		JLabel nowHosting = new JLabel("Now hosting, users will appear in the boxes as they connect");
		constr = new GridBagConstraints();
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridwidth = 3;
		constr.gridx = 0;
		constr.gridy = 0;
		constr.ipadx = 30;
		constr.ipady = 20;
		constr.anchor = GridBagConstraints.PAGE_START;
		hostingScreenGridLayout.add(nowHosting);
		
		userBoxes = new ArrayList<JPanel>();
		
		//Create user boxes for people, initially black.
		for (int i = 0; i < 10; i++)
		{
			JPanel temp = new JPanel();
			temp.setSize(25,25);
			temp.setBackground(Color.black);
			userBoxes.add(temp);
		}
		
		


		cards = new JPanel(new CardLayout());
		cards.add(usernameInputPaneGridLayout, usernameInputTitle);
		cards.add(joinOrHostPageOuterLayout,joinOrHostTitle);
		cards.add(slotChoiceLayout,slotChoiceTitle);
		cards.add(hostingScreenGridLayout,hostingScreenGridLayoutTitle);


		pane.add(cards, BorderLayout.WEST);
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
			hgt = new HostGameTask();
			hgt.execute();
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, hostingScreenGridLayoutTitle);
			
			int currentY = 1;
			int currentX = 0;
			
			constr.gridwidth = 25;
			constr.gridheight = 25;
			constr.ipadx = 10;
			constr.ipady = 10;
			System.out.println("Number of i: " + (SwingGUI.numberOfSlots.getSelectedIndex()+1));
			//Spawn a box for the number of slots available.
			for (int i = 0; i < (SwingGUI.numberOfSlots.getSelectedIndex()+1); i++)
			{
				if (currentX < 3)
				{
					constr.gridx = currentX;
					constr.gridy = currentY;
					currentX++;
				} else {
					currentX = 0;
					currentY++;
					constr.gridx = currentX;
					constr.gridy = currentY;
				}
				hostingScreenGridLayout.add(userBoxes.get(i), constr);
			}
			
			
		}
	}

	public class HostGameTask extends SwingWorker<ArrayList<User>, String> {

		private int numberOfPlayersCurrently = 0;
		
		public void publishDelegate(String message)
		{
			this.publish(message);
		}
		
		@Override
		protected ArrayList<User> doInBackground() throws Exception {
			try {
				SwingGUI.poker.StartGame(true,(SwingGUI.numberOfSlots.getSelectedIndex()+1),this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}


		@Override
		protected void process(List<String> messages) {
			String latestMessage = messages.get(messages.size()-1);
			System.out.println("Received message: " + latestMessage);
			String[] splitString = latestMessage.split(" ");
			
			if (splitString[0].equals("Now"))
			{
				//Ignore me
			} else {
				String username = splitString[0];
				numberOfPlayersCurrently++;
				userBoxes.get(numberOfPlayersCurrently-1).setBackground(Color.green);
				JLabel usernameLabel = new JLabel();
				userBoxes.get(numberOfPlayersCurrently-1).add(usernameLabel);
				usernameLabel.setForeground(Color.red);
			}
			
		}
	}


}
