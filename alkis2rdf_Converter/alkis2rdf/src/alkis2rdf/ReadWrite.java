package alkis2rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

public class ReadWrite {

	static final String owlFileName = "alkis.owl";

	// read owl file
	public OntModel readOWL() {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
		InputStream in = FileManager.get().open(owlFileName);
		model.read(in, "");
		return model;

	}

	// get xml file
	public static File getFile() throws Exception {
		JFileChooser xmlFile = new JFileChooser();
		File xml = null;
		FileNameExtensionFilter xmlFilter = new FileNameExtensionFilter("xml files (*.xml)", "xml");
		xmlFile.addChoosableFileFilter(xmlFilter);
		xmlFile.setFileFilter(xmlFilter);
		if (xmlFile.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			xml = xmlFile.getSelectedFile();
		} else {
			System.out.println("File read failed!");
			GUI.textArea.append("File read failed!");
		}
		return xml;
	}

	// read xml file
	public Element readXML(File xml) {
		Document doc = null;
		SAXBuilder builder = new SAXBuilder();
		try {
			doc = builder.build(xml);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Could not read File!");
		}
		Element element = doc.getRootElement();
		return element;
	}

	// Namespaces from xml file
	public HashMap<String, Namespace> xmlNamespaces(Element element) {

		List<Namespace> n = element.getNamespacesInScope();
		HashMap<String, Namespace> ns = new HashMap<String, Namespace>();
		for (Namespace i : n) {
			ns.put(i.getPrefix(), i);
		}
		return ns;
	}

	// create new rdf model
	public Model createModel(OntModel model, String base) {
		Model rdfm = ModelFactory.createDefaultModel();
		rdfm.setNsPrefix("alkis", model.getNsPrefixURI(""));
		rdfm.setNsPrefix("", base);
		rdfm.setNsPrefix("geo", "http://www.opengis.net/ont/geosparql#");
		rdfm.setNsPrefix("ngeo", "http://geovocab.org/geometry#");
		return rdfm;
	}

	// get EPSG Code
	public String getEPSG(Element element, HashMap<String, Namespace> ns) {
		String epsg = "";
		List<Element> koord = element.getChildren();
		for (Element y : koord) {
			if (y.getName().equals("koordinatenangaben")) {
				List<Element> koordref = y.getChildren();
				List<Element> koordrefsys = koordref.get(0).getChildren();
				String crs = koordrefsys.get(0).getAttributeValue("href", ns.get("xlink"));
				if (koordrefsys.get(2).getValue().equals("true")) {
					epsg = "258" + crs.substring(crs.length() - 2);
				}
			}
		}
		return epsg;
	}

	// write a rdf/xml or turtle file
	public void writeRDF(Model rdfm, File file, String ausgabe) {
		File rdf = new File("");
		try {
			OutputStream out;
			if (ausgabe == "Turtle") {
				rdf = new File(file.getAbsolutePath().replace(".xml", "_turtle.ttl"));
				out = new FileOutputStream(rdf);
				rdfm.write(out, "TURTLE");
			}

			if (ausgabe == "RDF/XML") {
				rdf = new File(file.getAbsolutePath().replace(".xml", "_rdf.rdf"));
				out = new FileOutputStream(rdf);
				rdfm.write(out, "RDF/XML-ABBREV", rdfm.getNsPrefixURI(""));
			}
			System.out.print("File write complete!");
			GUI.textArea.append("File was created under:\n" + rdf.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			GUI.textArea.append("No File created!");
		}
	}

}
