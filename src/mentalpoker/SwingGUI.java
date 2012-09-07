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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.avis.client.InvalidSubscriptionException;

public class SwingGUI extends JPanel implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = -3047811815984570792L;
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
	public SearchGamesTask jgt;
	private ArrayList<JButton> userButtons;
	private ArrayList<JLabel> userLabels;
	private JPanel hostingScreenGridLayout;
	private GridBagConstraints hostGameGBConstraints;
	private JPanel hostGamePanel;
	private GridBagConstraints joinGameGBConstraints;
	private JPanel joinGamePanel;
	private Set gameHosters;
	private DefaultListModel listModel;
	private JList gamesList;
	private JTextField nameofHosterField;
	
	private SigService sig;

	//Names of the panes
	final static String usernameInputTitle = "usernameInputPane";
	final static String joinOrHostTitle = "joinOrHostPane";
	final static String slotChoiceTitle = "slotChoiceTitle";
	final static String hostingScreenGridLayoutTitle = "hostingScreenGridLayoutTitle";
	final static String joinGameScreenTitle = "joinGameScreenTitle";

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
		hostGameGBConstraints = new GridBagConstraints();
		hostGameGBConstraints.fill = GridBagConstraints.HORIZONTAL;
		hostGameGBConstraints.ipady = 40;
		hostGameGBConstraints.gridwidth = 3;
		hostGameGBConstraints.gridx = 0;
		hostGameGBConstraints.gridy = 0;
		
		//constr.ipadx = 30;
		//constr.ipady = 20;
		hostingScreenGridLayout.add(nowHosting,hostGameGBConstraints);
		
		userButtons = new ArrayList<JButton>();
		//userLabels = new ArrayList<JLabel>();
		
		//Create user boxes for people, initially black.
		for (int i = 0; i < 10; i++)
		{
			JButton temp = new JButton();
			userButtons.add(temp);
		}
		
		/**
		 * Join game panel, showing available games.
		 */
		
		joinGamePanel = new JPanel(new GridBagLayout());
		joinGameGBConstraints = new GridBagConstraints();
		
		JButton mainMenuButton = new JButton("Back to main menu");
		mainMenuButton.setActionCommand("backToMainMenu");
		mainMenuButton.addActionListener(this);
		joinGameGBConstraints.gridx = 0;
		joinGameGBConstraints.gridy = 0;
		joinGamePanel.add(mainMenuButton,joinGameGBConstraints);
		
		JLabel joinGameTitle = new JLabel("Available games will appear below");
		//joinGameGBConstraints.gridwidth = 3;
		joinGameGBConstraints.gridx = 1;
		joinGameGBConstraints.gridy = 0;
		joinGameGBConstraints.weighty = 1; //Move this to the bottom right element when it is added
		joinGameGBConstraints.weightx = 1; //Move this to the bottom right element when it is added
		joinGamePanel.add(joinGameTitle, joinGameGBConstraints);
		
		//! Set up the list
		
		joinGameGBConstraints.gridwidth = 4;
		joinGameGBConstraints.ipadx = 400;
		joinGameGBConstraints.anchor = GridBagConstraints.NORTHWEST;
		joinGameGBConstraints.gridx = 0;
		joinGameGBConstraints.gridy = 1;
		joinGameGBConstraints.gridheight = 1;
		listModel = new DefaultListModel();
		listModel.addElement("testingHost"); //DEBUG
		listModel.addElement("testingHost2"); //DEBUG
		gamesList = new JList(listModel);
		gamesList.addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(gamesList);
		joinGamePanel.add(scrollPane, joinGameGBConstraints);
		
		nameofHosterField = new JTextField();
		JButton joinGameButton = new JButton("Join game");
		joinGameGBConstraints.gridx = 0;
		joinGameGBConstraints.ipadx = 300;
		joinGameGBConstraints.gridy = 2;
		joinGameGBConstraints.gridwidth = 2;
		joinGameGBConstraints.gridheight = 1;
		joinGamePanel.add(nameofHosterField, joinGameGBConstraints);
		joinGameGBConstraints.gridx = 2;
		joinGameGBConstraints.ipadx = 0;
		joinGameGBConstraints.gridy = 2;
		joinGameGBConstraints.gridwidth = 1;
		joinGameGBConstraints.gridheight = 1;
		joinGamePanel.add(joinGameButton, joinGameGBConstraints);
		joinGameButton.setActionCommand("joinGameFromSearch");
		joinGameButton.addActionListener(this);
		
		cards = new JPanel(new CardLayout());
		cards.add(usernameInputPaneGridLayout, usernameInputTitle);
		cards.add(joinOrHostPageOuterLayout,joinOrHostTitle);
		cards.add(slotChoiceLayout,slotChoiceTitle);
		cards.add(hostingScreenGridLayout,hostingScreenGridLayoutTitle);
		cards.add(joinGamePanel,joinGameScreenTitle);


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


	@Override
	public void actionPerformed(ActionEvent arg0) {
		if ("setUsername".equals(arg0.getActionCommand()))
		{
			String username = usernameField.getText();
			if (!username.equals(""))
			{
				//usernameAccepted(username);
				CardLayout cl = (CardLayout)(cards.getLayout());
				cl.show(cards, joinOrHostTitle);
				try {
					poker = new Poker(username);
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
		} else if ("backToMainMenu".equals(arg0.getActionCommand())) {
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, joinOrHostTitle);
		}else if ("startGameWithSlots".equals(arg0.getActionCommand()))
		{
			hgt = new HostGameTask();
			hgt.execute();
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, hostingScreenGridLayoutTitle);
			frame.setSize(700,700);
			
			
			int currentY = 1;
			int currentX = 0;
			
			hostGameGBConstraints.gridwidth = 1;
			hostGameGBConstraints.gridheight = 1;

			
			System.out.println("Number of i: " + (SwingGUI.numberOfSlots.getSelectedIndex()+1));
			//Spawn a box for the number of slots available.
			for (int i = 0; i <= (SwingGUI.numberOfSlots.getSelectedIndex()); i++)
			{
				if (currentX <= 2)
				{
					hostGameGBConstraints.ipadx = 50;
					hostGameGBConstraints.gridx = currentX;
					hostGameGBConstraints.gridy = currentY;
					currentX++;
					hostGameGBConstraints.fill = GridBagConstraints.HORIZONTAL;
					userButtons.get(i).setText("EMPTY\n SLOT");
					hostingScreenGridLayout.add(userButtons.get(i), hostGameGBConstraints);
					System.out.println(hostGameGBConstraints.gridx + " " + hostGameGBConstraints.gridy);
				} else {
					currentX = 0;
					currentY++;
					hostGameGBConstraints.gridx = currentX;
					hostGameGBConstraints.gridy = currentY;
					hostGameGBConstraints.fill = GridBagConstraints.HORIZONTAL;
					userButtons.get(i).setText("EMPTY\n SLOT");
					hostingScreenGridLayout.add(userButtons.get(i), hostGameGBConstraints);
					System.out.println(hostGameGBConstraints.gridx + " " + hostGameGBConstraints.gridy);
				}
				
				
			}
			
			
		} else if ("joinGame".equals(arg0.getActionCommand()))
		{
			CardLayout cl = (CardLayout)(cards.getLayout());
			cl.show(cards, joinGameScreenTitle);
			jgt = new SearchGamesTask();
			jgt.execute();
			frame.setSize(500,450);
		} else if ("joinGameFromSearch".equals(arg0.getActionCommand()))
		{
			System.out.println("join game button pushed?");
			try {
				jgt.waitForInstructionsBuffer.put(nameofHosterField.getText());
			} catch (InterruptedException iex)
			{
				jgt.cancel(true);
				throw new RuntimeException("Unexpected interruption");
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
				userButtons.get(numberOfPlayersCurrently-1).setText(username);
				userButtons.get(numberOfPlayersCurrently-1).setBackground(Color.green);
			}
			
		}
	}
	
	public class SearchGamesTask extends SwingWorker<ArrayList<User>, ArrayList<User>> {

		public String gameHost = "";
		private int numberOfPlayersCurrently = 0;
		public BlockingQueue<String> waitForInstructionsBuffer = new ArrayBlockingQueue<String>(100);
		
		public void publishDelegate(ArrayList<User> availableGames)
		{
			this.publish(availableGames);
		}
		
		@Override
		protected ArrayList<User> doInBackground() throws Exception {
			ArrayList<User> userListLocal = SwingGUI.poker.com.joinGameOffMenu(this);
			
			return userListLocal;
		}


		@Override
		protected void process(List<ArrayList<User>> listOfAvailableGames) {
			//System.out.println("Process called");
			//We now have a list of all the users hosting available games.
			ArrayList<User> availableGames = listOfAvailableGames.get(listOfAvailableGames.size()-1);
			
			listModel.clear();
			
			for (User usr:availableGames)
			{
				listModel.addElement(usr.getUsername());
			}
			
			
			
			
		}
	}

	@Override
	/**
	 * This listener updates the value of the textbox with the game host's name
	 * when they are selected in the list.
	 */
	public void valueChanged(ListSelectionEvent event) {
		if (event.getSource() == gamesList && !event.getValueIsAdjusting())
		{
			String nameOfGameHost = (String)gamesList.getSelectedValue();
			if (nameOfGameHost != null)
			{
				nameofHosterField.setText(nameOfGameHost);
			}
		}
		
	}


}
