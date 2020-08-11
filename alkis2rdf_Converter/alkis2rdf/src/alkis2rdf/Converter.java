package alkis2rdf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jdom2.Element;

public class Converter {

	public static ReadWrite read = new ReadWrite();
	static int i;
	static Resource aa;
	static String s;
	static ResIterator res1, res2;
	static Boolean b;
	
	// extract fatureMember elements from xml and convert content to rdf
	public void convertData() {
		Element enthaelt = GUI.element.getChild("enthaelt", GUI.ns.get(""));
		Element collection = enthaelt.getChild("FeatureCollection", GUI.ns.get("wfs"));
		List<Element> featureMembers = collection.getChildren();
		Resource individual = null;
		featureMembers.stream().forEach(f -> {
			if (f.getName().equals("featureMember")) {
				List<Element> fMElements = f.getChildren();
				getAllElements(fMElements, individual);
			} else {
				// Bounding Box
			}
		});
	}

	// get all Elements of a resource
	public static void getAllElements(List<Element> rElements, Resource individual) {
		Property property = null;
		getAllElements(rElements, individual, property);
	}

	private static void getAllElements(List<Element> rElements, Resource individual, Property property) {
		rElements.stream().forEach(r -> {
			String elName = r.getName();
			List<Element> list = r.getChildren();
			switch (elName) {

			// ignore external links
			case "zeigtAufExternes":
				break;

			case "AA_Modellart":
				red(list, individual, property, elName);
				break;
				
			case "AA_Lebenszeitintervall":
				red(list, individual, property, elName);
				break;
				
			case "position":
				geometry(individual, list.get(0));
				break;
			
			default:
				Property prop = null;
				Resource owlClass = null;
				if (GUI.baseModel.contains(GUI.baseModel.getResource(GUI.mNs + elName), RDF.type, OWL.Class)) {
					owlClass = GUI.baseModel.getResource(GUI.mNs + elName);
				} else if (GUI.baseModel.contains(GUI.baseModel.getResource(GUI.mNs + elName), RDF.type,
						OWL.DatatypeProperty)
						|| GUI.baseModel.contains(GUI.baseModel.getResource(GUI.mNs + elName), RDF.type,
								OWL.ObjectProperty)) {
					prop = GUI.baseModel.getProperty(GUI.mNs + elName);
				}
				
				if (list.isEmpty()) {

					// link
					String link = r.getAttributeValue("href", GUI.ns.get("xlink"));
					if (link != null && prop != null) {
						Resource ind1 = GUI.rdfm
								.createResource(GUI.rdfm.getNsPrefixURI("") + link.replace("urn:adv:oid:", ""));
						individual.addProperty(prop, ind1);
						break;
					}

					// add data property and literal
					else if (prop != null) {
						NodeIterator n = GUI.baseModel.listObjectsOfProperty(GUI.baseModel.getResource(GUI.mNs + elName), RDFS.range); 
						s = "";
						n.forEachRemaining(f->{
							s = f.toString();
						});
						individual.addLiteral(prop, GUI.rdfm.createTypedLiteral(r.getValue(),s));
						break;
					} else {
						// Qualitätsmerkmale
						if (elName.equals("AX_LI_ProcessStep_OhneDatenerhebung_Description")
								|| elName.equals("DateTime") || elName.equals("CharacterString")
								|| elName.equals("CI_RoleCode")
								|| elName.equals("AX_LI_ProcessStep_Punktort_Description")
								|| elName.equals("AX_Datenerhebung_Punktort") || elName.equals("AX_Datenerhebung")
								|| elName.equals("Record")) {
							NodeIterator n = GUI.baseModel.listObjectsOfProperty(GUI.baseModel.getResource(property.getURI()), RDFS.range); 
							s = "";
							n.forEachRemaining(f->{
								s = f.toString();
							});
							individual.addLiteral(property, GUI.rdfm.createTypedLiteral(r.getValue(),s));
							break;
						} else
							fehler(elName);
					}
				}

				// add Resource and object property
				else {
					if (owlClass != null) {
						String name = r.getAttributeValue("id", GUI.ns.get("gml"));
						if (name == null)
							name = UUID.randomUUID().toString();
						Resource ind1 = GUI.rdfm.createResource(GUI.rdfm.getNsPrefixURI("") + name, owlClass);
						if (property != null)
							
							
							individual.addProperty(property, ind1);
						getAllElements(list, ind1);
						break;
					} else if (prop != null) {
						getAllElements(list, individual, prop);
						break;
					} else
						fehler(elName);
				}
			}
		});
	}

	// elements not in ontology
	private static void fehler(String element) {
		System.out.println("No class found for this element: " + element);
		GUI.textArea.append("No class found for this element: " + element + "\n");
	}

	// convert geometry
	private static void geometry(Resource individual, Element e) {
		List<Element> list = e.getChildren();
		String koord = "";
		String art = "";
		String elName = e.getName();
		switch (elName) {
		case "Point":
			List<Element> list1 = new ArrayList<Element>();
			list1.add(e);
			koord = GeometryToText.getPoLi(list1);
			art = "POINT";
			break;

		case "MultiPoint":
			koord = GeometryToText.getPoLi(list);
			art = "MULTIPOINT";
			break;

		case "Curve":
			koord = GeometryToText.getPoLi(list);
			art = "LINESTRING";
			break;

		case "MultiCurve":
			for (Element x : list) {
				List<Element> mList = new ArrayList<Element>();
				mList.add(x);
				if (koord != "")
					koord += ",";
				koord += "(" + GeometryToText.getPoLi(mList) + ")";
			}
			art = "MULTILINESTRING";
			break;

		case "CompositeCurve":
			for (Element x : list) {
				List<Element> mList = new ArrayList<Element>();
				mList.add(x);
				if (koord != "")
					koord += ",";
				koord += "(" + GeometryToText.getPoLi(mList) + ")";
			}
			art = "MULTILINESTRING";
			break;

		case "Surface":
			koord = GeometryToText.getPoly(e);
			art = "POLYGON";
			break;

		case "MultiSurface":
			for (Element a : list) {
				List<Element> polyList = a.getChildren(); 			// Surface
				if (koord != "")
					koord += ",";
				koord += "(" + GeometryToText.getPoly(polyList.get(0)) + ")";
			}
			art = "MULTIPOLYGON";
			break;

		default:
			fehler(elName);
		}
		GeometryToText.getWKT(koord, individual, art);
	}
	
	private static void red(List<Element> list, Resource individual, Property property, String elName) {
		ResIterator rd = GUI.rdfm.listSubjectsWithProperty(GUI.baseModel.getProperty(GUI.mNs +list.get(0).getName()),list.get(0).getValue());
		i = 0;
		rd.forEachRemaining(f->{
			i++;
			aa = f;
		});
		if(i == 0) {
			Resource ind1 = GUI.rdfm.createResource(GUI.rdfm.getNsPrefixURI("") + UUID.randomUUID().toString(), GUI.baseModel.getResource(GUI.mNs + elName));
			individual.addProperty(property, ind1);
			NodeIterator n = GUI.baseModel.listObjectsOfProperty(GUI.baseModel.getResource(GUI.mNs + list.get(0).getName()), RDFS.range); 
			s = "";
			n.forEachRemaining(f->{
				s = f.toString();
			});
			ind1.addLiteral(GUI.baseModel.getProperty(GUI.mNs +list.get(0).getName()), GUI.rdfm.createTypedLiteral(list.get(0).getValue(),s));
		}
		else if(i == 1) {
			individual.addProperty(property, aa);
		}
		else {
			System.out.println("Fehler");
		}
		
	}
}
