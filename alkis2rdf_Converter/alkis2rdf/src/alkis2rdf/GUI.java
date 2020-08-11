package alkis2rdf;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jdom2.Element;
import org.jdom2.Namespace;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Font;

public class GUI implements ActionListener {

	JFrame frame;
	JPanel panel;
	JButton create;
	JButton loadBtn;
	JLabel label;
	int count;
	public static Converter ac = new Converter();
	public static ReadWrite read = new ReadWrite();

	public static OntModel model;
	public static Element element;
	public static HashMap<String, Namespace> ns;
	public static Model rdfm, baseModel;
	public static String epsg, mNs;
	public static File xml;
	static ExtendedIterator<OntProperty> ontProp;
	private JComboBox<String> comboBox;
	private JLabel lblNewLabel;
	private JTextField baseField;
	static JTextArea textArea;
	static JScrollPane scroll;
	static List<OntProperty> pList;
	static List<String> s;

	// create interface
	public GUI() {
		frame = new JFrame();
		panel = new JPanel();
		
		panel.setBorder(new EmptyBorder(20, 20, 20, 20));
		frame.getContentPane().add(panel, BorderLayout.EAST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 89, 89, 89, 89, 89 };
		gbl_panel.rowHeights = new int[] { 23, 23, 23, 23, 23, 23, 23 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		loadBtn = new JButton("Load File");
		loadBtn.setVerticalAlignment(SwingConstants.TOP);

		// action click on Load button
		loadBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					xml = ReadWrite.getFile();
					label.setText(xml.getName());
					textArea.setText(null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				textArea.append("Read file ...");
				element = read.readXML(xml);
				ns = read.xmlNamespaces(element);
				System.out.println("File read!");
				textArea.setText("File read!");
			}
		});

		GridBagConstraints gbc_loadBtn = new GridBagConstraints();
		gbc_loadBtn.fill = GridBagConstraints.BOTH;
		gbc_loadBtn.insets = new Insets(0, 0, 5, 5);
		gbc_loadBtn.gridx = 0;
		gbc_loadBtn.gridy = 0;
		panel.add(loadBtn, gbc_loadBtn);
		label = new JLabel();
		label.setText("File < 200 MB");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridwidth = 2;
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);

		create = new JButton("Create RDF");
		create.addActionListener(this);

		lblNewLabel = new JLabel("Base URI:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		baseField = new JTextField();
		baseField.setText("http://example.org/");
		baseField.setToolTipText("Base URI for the RDF file");

		GridBagConstraints gbc_baseField = new GridBagConstraints();
		gbc_baseField.gridwidth = 4;
		gbc_baseField.insets = new Insets(0, 0, 5, 5);
		gbc_baseField.fill = GridBagConstraints.HORIZONTAL;
		gbc_baseField.gridx = 1;
		gbc_baseField.gridy = 1;
		panel.add(baseField, gbc_baseField);
		baseField.setColumns(10);
		GridBagConstraints gbc_create = new GridBagConstraints();
		gbc_create.fill = GridBagConstraints.BOTH;
		gbc_create.insets = new Insets(0, 0, 5, 5);
		gbc_create.gridx = 0;
		gbc_create.gridy = 2;
		panel.add(create, gbc_create);

		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "RDF/XML", "Turtle" }));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.BOTH;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		panel.add(comboBox, gbc_comboBox);

		textArea = new JTextArea();
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridheight = 4;
		gbc_textArea.gridwidth = 5;
		gbc_textArea.insets = new Insets(0, 0, 5, 5);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 3;
		panel.add(textArea, gbc_textArea);
		scroll = new JScrollPane(textArea);
		GridBagConstraints gbc_scroll = new GridBagConstraints();
		gbc_scroll.gridheight = 4;
		gbc_scroll.gridwidth = 5;
		gbc_scroll.fill = GridBagConstraints.BOTH;
		gbc_scroll.insets = new Insets(0, 0, 5, 5);
		gbc_scroll.gridx = 0;
		gbc_scroll.gridy = 3;
		panel.add(scroll, gbc_scroll);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("ALKIS2RDF");
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String args[]) {
		model = read.readOWL(); 									// read ontology
		mNs = model.getNsPrefixURI(""); 							// get namespaces
		baseModel = model.getBaseModel(); 							// create rdf model of ontology
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		new GUI(); 													// open GUI
	}

	// action click on Create button
	@Override
	public void actionPerformed(ActionEvent arg0) {
		String base = baseField.getText(); 							// get base URI
		if (base.isEmpty() || base.equals("")) {
			textArea.append("Please give a Base URI\n");
			return;
		} else if (xml == null) {
			textArea.append("Choose a file!\n");
			return;
		} else {
			textArea.setText("");
			rdfm = read.createModel(model, base); 					// create rdf model
			epsg = read.getEPSG(element, ns); 						// get EPSG Code from coordinates
			ac.convertData(); 										// convert data
			String ausgabe = (String) comboBox.getSelectedItem(); 	// get selected format
			read.writeRDF(rdfm, xml, ausgabe);
		}
	}
}
