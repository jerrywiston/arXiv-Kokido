import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;


public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;
	private static final String[] searchTypeOptions = { "Titles", "Authors", "Abstracts" };
	private static final String[] filterOptions = { "Subjects", "Tags", "None" };
	private static final String[] sortOptions = { "Recent", "Date", "Alphabet" };
	private static final String[] orderOptions = { "Ascent", "Decent" };
	private static final String[] fieldOptions = { "All", "CS", "Econ", "EESS", "Math", "Phy", "Bio", "Fin", "Stat" };

	private GridBagLayout windowLayout;
	private JTabbedPane contentTabbedPane;
	private JScrollPane contentScrollPane;
	private JScrollPane profileScrollPane;
	private JScrollPane inspectorScrollPane;
	private JPanel backPanel;
	private JPanel inspectorPanel;
	private JPanel filterPanel;
	private JPanel searchPanel;
	private JPanel profileContentPanel;
	private JPanel contentPanel;
	private JPanel statPanel;
	private JPanel scalePanel;
	private ProfilePanel profilePanel;
	private JLabel statLabel;
	private float layoutScale;
	private JTextField searchText;
	private JTextField filterText;
	private List<ShadowPanel> itemList;
	private int width;
	private int height;
	private JComboBox<String> searchTypeComboBox;
	private JComboBox<String> fieldComboBox;
	private JComboBox<String> filterComboBox;
	private JComboBox<String> sortComboBox;
	private JComboBox<String> orderComboBox;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode root;
	private JTree tree;
	private String classType = "Subjects";
	private String sortType = "Recent";
	private String orderType = "Decent";
	ImageIcon icon;

	
	/*---------------------------------
	Constructor
	---------------------------------*/
	public MainWindow(final OperationManager opManager) {
		// Initialize parameters--------------------
		itemList = new ArrayList<ShadowPanel>();
		width = 1024;
		height = 768;
		layoutScale = 0.5f;
		icon = new ImageIcon("./kokido.png");
		icon = new ImageIcon(icon.getImage().getScaledInstance(64, 64, 0));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("arXiv-kokido");
		setSize(width, height);
		setIconImage(icon.getImage());

		// Create panels----------------------------
		createMenuBar(opManager);
		createInspectorPanel(opManager);
		createSearchPanel(opManager);
		createContentPanel(opManager);
		createStatePanel();
		createFilterPanel(opManager);
		createScalePanel();

		// Create the background panel--------------
		backPanel = new JPanel();
		windowLayout = new GridBagLayout();
		backPanel.setLayout(windowLayout);
		backPanel.setBackground(new Color(100, 100, 100, 255));

		// Set window layout------------------------
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1 - layoutScale;
		gbc.weighty = 0.06;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		backPanel.add(filterPanel, gbc);

		gbc.weightx = 1 - layoutScale;
		gbc.weighty = 0.93;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		backPanel.add(inspectorScrollPane, gbc);

		gbc.weightx = 0;
		gbc.weighty = 0.99;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		backPanel.add(scalePanel, gbc);

		gbc.weightx = layoutScale;
		gbc.weighty = 0.06;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		backPanel.add(searchPanel, gbc);

		gbc.weightx = layoutScale;
		gbc.weighty = 0.93;
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		backPanel.add(contentTabbedPane, gbc);

		gbc.weightx = 1.0;
		gbc.weighty = 0.01;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		backPanel.add(statPanel, gbc);
		add(backPanel);
	}

	
	/*---------------------------------
	Synchronize paper file info
	---------------------------------*/
	public void synchronize(OperationManager opManager) {
		List<String> fidList = opManager.findMissingFile();
		List<String> iidList = opManager.findMissingInfo();
		if (fidList.size() > 0) {
			String msg = "Missing " + fidList.size() + " PDFs ...";
			int option = JOptionPane.showOptionDialog(this, msg , "Synchronization",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, icon,
					new String[] { "Download PDF", "Delete Info" }, "Download PDF");
			opManager.fileSynchronize(fidList, (option==0) ? true : false);
		}
		
		if (iidList.size() > 0) {
			String msg = "Missing " + iidList.size() + " Infos ...";
			int option = JOptionPane.showOptionDialog(this, msg, "Synchronization",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, icon,
					new String[] { "Add Info", "Delete PDF" }, "Add Info");
			opManager.infoSynchronize(iidList, (option==0)?true:false);
		}
	}

	
	/*---------------------------------
	Create the menu bar
	---------------------------------*/
	private void createMenuBar(final OperationManager opManager) {
		final JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu toolMenu = new JMenu("Tools");

		// MenuItem: Exit---------------------------
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				opManager.SaveInfo();
				System.exit(0);
			}
		});

		// MenuItem: Search--------------------------
		JMenuItem searchMenuItem = new JMenuItem("Search");
		searchMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String searchType = (String) searchTypeComboBox.getSelectedItem();
				String searchField = (String) fieldComboBox.getSelectedItem();
				opManager.startSearch(searchText.getText(), searchType, searchField, 5, 0);
				contentTabbedPane.setSelectedIndex(0);
			}
		});

		// MenuItem: Clear----------------------------
		JMenuItem clearMenuItem = new JMenuItem("Clear");
		clearMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				clearItem();
				contentTabbedPane.setSelectedIndex(0);
			}
		});

		fileMenu.add(exitMenuItem);
		toolMenu.add(searchMenuItem);
		toolMenu.add(clearMenuItem);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(toolMenu);
		setJMenuBar(menuBar);
	}

	
	/*---------------------------------
	Create the inspector panel
	---------------------------------*/
	private void createInspectorPanel(final OperationManager opManager) {
		inspectorPanel = new ShadowPanel();
		inspectorPanel.setBackground(new Color(200, 200, 200, 200));
		inspectorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		inspectorPanel.setPreferredSize(new Dimension(100, height));
		inspectorPanel.setMinimumSize(new Dimension(100, height));

		inspectorScrollPane = new JScrollPane(inspectorPanel);
		inspectorScrollPane.setBackground(new Color(0, 0, 0, 0));
		inspectorScrollPane.setBorder(BorderFactory.createEmptyBorder());
		inspectorScrollPane.getViewport().setBackground(new Color(64, 64, 64, 255));
		inspectorScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		root = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setBackground(new Color(0, 0, 0, 0));
		tree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				GUIManager.refresh(inspectorPanel);

			}

			public void mouseReleased(MouseEvent e) {
				GUIManager.refresh(profilePanel);
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (node != null && node.isLeaf() && (String) node.getUserObject() != "Root") {
					String id = ((String) node.getUserObject()).split("\\[")[1].split("\\]")[0];
					setProfilePaperInfo(opManager.getInfo(id));
					contentTabbedPane.setSelectedIndex(1);
				}
			}
		});
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent e) {
				inspectorPanel.setPreferredSize(new Dimension(100, tree.getHeight() + 100));
				inspectorScrollPane.updateUI();
				GUIManager.refresh(inspectorPanel);
			}

			public void treeCollapsed(TreeExpansionEvent e) {
				inspectorPanel.setPreferredSize(new Dimension(100, tree.getHeight() + 100));
				inspectorScrollPane.updateUI();
				GUIManager.refresh(inspectorPanel);
			}
		});

		DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
		renderer.setTextSelectionColor(Color.orange);
		renderer.setBackgroundSelectionColor(Color.black);
		renderer.setBackground(new Color(0, 0, 0, 0));
		renderer.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
		renderer.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));

		inspectorPanel.add(tree);
	}

	
	/*---------------------------------
	Create the search panel
	---------------------------------*/
	private void createSearchPanel(final OperationManager opManager) 
	{
		searchPanel = new ShadowPanel();
		searchPanel.setBackground(new Color(50, 50, 50, 200));
		searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

		// Search text
		searchText = new JTextField(24);
		searchText.setPreferredSize(new Dimension(128, 32));
		searchText.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		searchPanel.add(searchText);

		// Search type label
		JLabel searchTypeLabel = new JLabel("<html><font size='4' face='Verdana' color='white'>Type:</font></html>");
		JLabel fieldLabel = new JLabel("<html><font size='4' face='Verdana' color='white'>Field:</font></html>");

		// Search type combo box
		searchTypeComboBox = new JComboBox<String>(searchTypeOptions);
		fieldComboBox = new JComboBox<String>(fieldOptions);

		// Search button
		JButton searchBtn = new JButton("Search");
		searchBtn.setPreferredSize(new Dimension(64, 32));
		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String searchType = (String) searchTypeComboBox.getSelectedItem();
				String searchField = (String) fieldComboBox.getSelectedItem();
				opManager.startSearch(searchText.getText(), searchType, searchField, 5, 0);
				contentTabbedPane.setSelectedIndex(0);
			}
		});

		searchPanel.add(searchTypeLabel);
		searchPanel.add(searchTypeComboBox);
		searchPanel.add(fieldLabel);
		searchPanel.add(fieldComboBox);
		searchPanel.add(searchBtn);
	}

	
	/*---------------------------------
	Create the content panel
	---------------------------------*/
	private void createContentPanel(final OperationManager opManager) 
	{
		contentPanel = new ShadowPanel();
		contentPanel.setBackground(new Color(150, 150, 150, 150));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		contentScrollPane = new JScrollPane(contentPanel);
		contentScrollPane.setBackground(new Color(0, 0, 0, 0));
		contentScrollPane.getViewport().setBackground(new Color(64, 64, 64, 255));
		contentScrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		contentScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e) 
			{
				JScrollBar scrollBar = (JScrollBar) e.getAdjustable();

				if (scrollBar.getOrientation() == Adjustable.VERTICAL && scrollBar.isVisible()
						&& scrollBar.getMaximum() - e.getValue() - scrollBar.getModel().getExtent() < 10) {
					String searchType = (String) searchTypeComboBox.getSelectedItem();
					String searchField = (String) fieldComboBox.getSelectedItem();
					opManager.startSearch(searchText.getText(), searchType, searchField, 5, itemList.size());
				}
			}
		});

		profileContentPanel = new ShadowPanel();
		profileContentPanel.setBackground(new Color(150, 150, 150, 150));
		profileContentPanel.setLayout(new BoxLayout(profileContentPanel, BoxLayout.Y_AXIS));

		profilePanel = new ProfilePanel(opManager);
		profilePanel.setVisible(false);
		profilePanel.setMinimumSize(new Dimension(100, height));
		profilePanel.setMaximumSize(new Dimension(2048, height));
		profilePanel.setPreferredSize(new Dimension((int) ((float) width * 0.75), height));
		profileContentPanel.add(profilePanel);

		profileScrollPane = new JScrollPane(profileContentPanel);
		profileScrollPane.setBackground(new Color(0, 0, 0, 0));
		profileScrollPane.getViewport().setBackground(new Color(64, 64, 64, 255));
		profileScrollPane.setBorder(BorderFactory.createEmptyBorder());
		profileScrollPane.getVerticalScrollBar().setUnitIncrement(16);

		contentTabbedPane = new JTabbedPane();
		contentTabbedPane.addTab("Online", contentScrollPane);
		contentTabbedPane.addTab("Local", profileScrollPane);
	}

	
	/*---------------------------------
	Create the state panel
	---------------------------------*/
	private void createStatePanel() {
		statPanel = new JPanel();
		statPanel.setBackground(new Color(200, 200, 200, 255));
		statPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		statLabel = new JLabel("<html><font size='4' face='Verdana'>&nbsp; Ready</font></html>");
		statPanel.add(statLabel);
	}

	
	/*---------------------------------
	Create the filter panel
	---------------------------------*/
	private void createFilterPanel(final OperationManager opManager) 
	{
		filterPanel = new ShadowPanel();
		filterPanel.setBackground(new Color(80, 80, 80, 200));
		filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

		// Search text
		filterText = new JTextField(12);
		filterText.setPreferredSize(new Dimension(128, 32));
		filterText.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));

		// Search button
		JButton searchBtn = new JButton("Search");
		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String str = filterText.getText();
				opManager.localSearch(str);
			}
		});

		// Filter labels
		JLabel filterLabel = new JLabel("<html><font size='3' face='Verdana' color='white'>Class:</font></html>");
		JLabel sortLabel = new JLabel("<html><font size='3' face='Verdana' color='white'>Sort:</font></html>");
		JLabel orderLabel = new JLabel("<html><font size='3' face='Verdana' color='white'>Order:</font></html>");

		// Filter combo box
		filterComboBox = new JComboBox<String>(filterOptions);
		filterComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classType = (String) filterComboBox.getSelectedItem();
				opManager.setClassSort(classType, sortType, orderType);
				opManager.refreshNode();
			}
		});

		// Sort combo box
		sortComboBox = new JComboBox<String>(sortOptions);
		sortComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sortType = (String) sortComboBox.getSelectedItem();
				opManager.setClassSort(classType, sortType, orderType);
				opManager.refreshNode();
			}
		});

		// Order combo box
		orderComboBox = new JComboBox<String>(orderOptions);
		orderComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				orderType = (String) orderComboBox.getSelectedItem();
				opManager.setClassSort(classType, sortType, orderType);
				opManager.refreshNode();
			}
		});

		filterPanel.add(filterText);
		filterPanel.add(searchBtn);
		filterPanel.add(filterLabel);
		filterPanel.add(filterComboBox);
		filterPanel.add(sortLabel);
		filterPanel.add(sortComboBox);
		filterPanel.add(orderLabel);
		filterPanel.add(orderComboBox);
	}

	
	/*---------------------------------
	Create the scale panel
	---------------------------------*/
	private void createScalePanel() {
		scalePanel = new JPanel();
		scalePanel.setBackground(new Color(0, 0, 0, 0));
		scalePanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
		scalePanel.addMouseListener(new MouseAdapter() {
			private Timer timer = null;
			private int prevX = 0;

			// Mouse pressed event---------------------
			public void mousePressed(MouseEvent me) {
				backPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));

				if (timer == null) {
					timer = new Timer();
					prevX = MouseInfo.getPointerInfo().getLocation().x;
				}

				timer.schedule(new TimerTask() {
					public void run() {
						int curX = MouseInfo.getPointerInfo().getLocation().x;
						setLayoutScale((float) (prevX - curX) / 800);
						prevX = curX;
					}
				}, 0, 10);
			}

			// Mouse released event---------------------
			public void mouseReleased(MouseEvent me) {
				backPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				if (timer != null) {
					timer.cancel();
					timer = null;
				}
			}
		});
	}

	
	/*---------------------------------
	Set layout scale
	---------------------------------*/
	public void setLayoutScale(float deltaScale) {
		layoutScale += deltaScale;
		if (layoutScale < 0)
			layoutScale = 0;
		if (layoutScale > 1)
			layoutScale = 1;

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1 - layoutScale;
		gbc.weighty = 0.06;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		windowLayout.setConstraints(filterPanel, gbc);

		gbc.weightx = 1 - layoutScale;
		gbc.weighty = 0.93;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		windowLayout.setConstraints(inspectorScrollPane, gbc);

		gbc.weightx = layoutScale;
		gbc.weighty = 0.06;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		windowLayout.setConstraints(searchPanel, gbc);

		gbc.weightx = layoutScale;
		gbc.weighty = 0.93;
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		windowLayout.setConstraints(contentTabbedPane, gbc);

		backPanel.setLayout(windowLayout);
		GUIManager.refresh(backPanel);
	}

	
	/*---------------------------------
	Add an item
	---------------------------------*/
	public void addItem(ItemPanel item) 
	{
		item.setShape((int) ((float) width * 0.75));
		itemList.add(item);

		int h = 100;
		for (ShadowPanel i : itemList)
			h += i.getPreferredSize().getHeight();

		contentPanel.add(item);
		contentPanel.setMinimumSize(new Dimension(100, h));
		contentPanel.setMaximumSize(new Dimension(2048, h));
		contentPanel.setPreferredSize(new Dimension((int) ((float) width * 0.75), h));
		contentScrollPane.updateUI();
		GUIManager.refresh(contentPanel);
	}

	
	/*---------------------------------
	clear all item
	---------------------------------*/
	public void clearItem() {
		itemList.clear();
		contentPanel.removeAll();
		contentPanel.setPreferredSize(new Dimension(100, 100));
		contentScrollPane.updateUI();
		GUIManager.refresh(contentPanel);
	}

	
	/*---------------------------------
	Set state message
	---------------------------------*/
	public void setState(String str) {
		statLabel.setText("<html><font size='4' face='Verdana'>&nbsp; " + str + "</font></html>");
	}

	
	/*---------------------------------
	Set profile paper info
	---------------------------------*/
	public void setProfilePaperInfo(PaperInfo info) {
		profilePanel.setPaperInfo(info);
		profilePanel.setVisible(true);
	}

	
	/*---------------------------------
	Add a tree node
	---------------------------------*/
	public void addTreeNode(String parentName, String name) {
		DefaultMutableTreeNode parentNode = findTreeNodeByName(root, parentName);
		if (parentNode != null) {
			parentNode.add(new DefaultMutableTreeNode(name));
			treeModel.reload();

			inspectorPanel.setPreferredSize(new Dimension(100, tree.getHeight() + 100));
			inspectorScrollPane.updateUI();
			GUIManager.refresh(inspectorPanel);
		}
	}

	
	/*---------------------------------
	Delete a tree node
	---------------------------------*/
	public void deleteTreeNode(String name) {
		DefaultMutableTreeNode node = findTreeNodeByName(root, name);
		if (node != null) {
			treeModel.removeNodeFromParent(node);
			treeModel.reload();
		}
	}

	
	/*---------------------------------
	Clear all tree node
	---------------------------------*/
	public void ClearTreeNode() {
		root.removeAllChildren();
		treeModel.reload();
	}

	
	/*---------------------------------
	Find a tree node
	---------------------------------*/
	public DefaultMutableTreeNode findTreeNodeByName(DefaultMutableTreeNode curNode, String name) {
		if (((String) curNode.getUserObject()).equals(name))
			return curNode;

		DefaultMutableTreeNode ret = null;
		if (!curNode.isLeaf()) {
			Enumeration<DefaultMutableTreeNode> children = curNode.children();
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode node = children.nextElement();
				ret = findTreeNodeByName(node, name);
			}
		}
		return ret;
	}

	
	/*---------------------------------
	Profile Panel visible
	---------------------------------*/
	public void setProfileVisible(boolean b) {
		profilePanel.setVisible(b);
	}
}