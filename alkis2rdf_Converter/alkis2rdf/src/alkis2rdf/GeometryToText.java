package alkis2rdf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.jdom2.Element;

public class GeometryToText {

	// extract coordinates from points and lines
	public static String getPoLi(List<Element> geoList) {
		StringBuffer geo = new StringBuffer();
		for (Element v : geoList) {
			List<Element> geoList2 = v.getChildren();
			for (Element s : geoList2) {
				geo.append(s.getValue() + " ");
			}
		}
		String koord = "";
		String[] list = geo.toString().replace("\n", "").replace("\t", "").trim().split(" ");
		if (list.length == 1) {
			koord = list[0];
		} else if (list.length % 2 == 0) {
			for (int w = 0; w < list.length; w = w + 2) {
				if (koord != "")
					koord += ", ";
				koord += list[w] + " " + list[w + 1];
			}
		} else {
			for (int w = 0; w < list.length; w = w + 3) {
				if (koord != "")
					koord += ", ";
				koord += list[w] + " " + list[w + 1] + " " + list[w + 2];
			}
		}
		return koord;
	}

	// extract coordinates from surfaces
	public static String getPoly(Element polyElement) {
		StringBuffer poly = new StringBuffer();
		String koord = "";
		List<Element> polyList1 = polyElement.getChildren(); 			// patches
		List<Element> polyList2 = polyList1.get(0).getChildren(); 		// PolygonPatch
		List<Element> polyList3 = polyList2.get(0).getChildren(); 		// exterior + interior

		for (Element z : polyList3) {
			List<Element> polyList4 = z.getChildren(); 					// Ring
			List<Element> polyList5 = polyList4.get(0).getChildren(); 	// curveMember
			for (Element x : polyList5) {
				poly.append(x.getValue() + " ");
			}
			if (koord != "")
				koord += ",";
			koord += "(";
			String[] l = poly.toString().replace("\n", "").replace("\t", "").trim().split(" ");
			List<String> list = new ArrayList<String>();
			for (int r = 0; r < l.length; r++) {
				if (!l[r].isEmpty()) {
					list.add(l[r]);
				}
			}
			for (int w = 0; w < list.size(); w = w + 2) {
				if (w == 0) {
					koord += list.get(w) + " " + list.get(w + 1) + ", ";
				} else if (list.get(w).equals(list.get(w - 2)) && list.get(w + 1).equals(list.get(w - 1))) {
				} else {
					koord += list.get(w) + " " + list.get(w + 1);
					if (w < list.size() - 2)
						koord += ", ";
				}
			}
			koord += ")";
		}
		return koord;
	}

	// create well known text from coordinates and add to rdf model
	public static void getWKT(String koord, Resource individual, String geometry) {
		String wkt = "<http://www.opengis.net/def/crs/EPSG/0/" + GUI.epsg + "> " + geometry + "(" + koord + ")";
		String name = UUID.randomUUID().toString();
		Literal lit = GUI.model.createTypedLiteral(wkt, "http://www.opengis.net/ont/geosparql#" + "wktLiteral");
		OntClass ontGeo = GUI.model.getOntClass("http://www.opengis.net/ont/geosparql#" + "Geometry");
		Property prop1 = GUI.model.getOntProperty("http://www.opengis.net/ont/geosparql#" + "hasGeometry");
		Property prop2 = GUI.model.getOntProperty("http://www.opengis.net/ont/geosparql#" + "asWKT");
		Resource ind2 = GUI.rdfm.createResource(GUI.rdfm.getNsPrefixURI("") + name, ontGeo);
		individual.addProperty(prop1, ind2);
		ind2.addLiteral(prop2, lit);
	}

}
