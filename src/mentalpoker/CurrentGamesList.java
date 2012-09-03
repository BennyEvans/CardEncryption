package mentalpoker;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This games list is based on the demo available at:
 * http://docs.oracle.com/javase/tutorial/uiswing/examples/components/ListDemoProject/src/components/ListDemo.java
 * @author emilevictor & sun
 *
 */

public class CurrentGamesList extends JPanel
implements ListSelectionListener {
	
	private JList list;
	private DefaultListModel listModel;
	
	

	public CurrentGamesList()
	{
		super(new BorderLayout());
		
		listModel = new DefaultListModel();
		//Add elements to listModel here
		
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
        list.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(list);
        
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
