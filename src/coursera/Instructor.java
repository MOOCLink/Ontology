package coursera;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Date;

import rdf.Prefixes;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;


public class Instructor {
	private List<Instructor> elements;
    private String firstName, lastName;
    private Integer id;
    private Link links;
    
    public List<Instructor> getElements() { return elements; }
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Link getLinks() { return links; }
    
    public void setElements(List<Instructor> elements) { this.elements = elements; }
	public void setId(int id) { this.id = id; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
	public void setLinks(Link links) { this.links = links; }
	
	class Link {
		public List<Integer> universities;
		public List<Integer> getUniversities() { return universities; }
		public void setUniversities(List<Integer> universities) { this.universities = universities; }
	}
	
	public OntModel writeToRDF(OntModel m) {
		Prefixes p = new Prefixes();
	    Date date = new Date();
	    Timestamp timestamp = new Timestamp(date.getTime());

	    try {
	    	// Set up log file
	    	File logFile = new File("log/coursera/instructor/instructor_log_" + timestamp);
		    FileOutputStream log = new FileOutputStream(logFile);
		    String header = "New Coursera instructors:\n" + timestamp.toString() 
		    		+ "\n-------------------------\n\n";
		    log.write(header.getBytes());
		    
		    // Get ontology properties
		    OntClass person = m.getOntClass(p.schema + "Person");
		    Property pName = m.getProperty(p.schema + "name");
		    Property pAffiliation = m.getProperty(p.schema + "affiliation");
		    
		    for(int i = 0; i < elements.size(); i++)
		    {
		    	Instructor e = elements.get(i);
		    	
				// Check to see if Instructor exists
				if(m.getIndividual(p.mooc + "coursera_instructor_" + Integer.toString(e.id)) == null)
				{
			    	// Create an individual
				    Individual instructor = m.createIndividual(p.mooc + "coursera_instructor_" + Integer.toString(e.id), person);
				    
				    // Assign name
				    if(!e.firstName.isEmpty() && !e.lastName.isEmpty()) {
					    m.setNsPrefix("schema", p.schema);
					    String name = e.firstName + " " + e.lastName;
					    instructor.addProperty(pName, name);
					    
					    // Log entry
					    String entry = "Instructor Name: " + name + "\nID: " + Integer.toString(e.id) + "\n\n";
					    log.write(entry.getBytes());
				    }
				    
				    // Assign affiliation
				    if(e.links.getUniversities() != null) {
				    	List<Integer> uniIds = e.links.getUniversities();
				    	for(int j = 0; j < uniIds.size(); j++) {
				    		String uniId = Integer.toString(uniIds.get(j));
				    		String uniURI = p.mooc + "coursera_university_" + uniId;
				    		if(m.getIndividual(uniURI) != null) {
				    			m.setNsPrefix("schema", p.schema);
				    			instructor.addProperty(pAffiliation, uniURI);
				    		}
				    	}
				    }
				}
		    }
		    log.close();
		} catch (JenaException je) {
		    System.out.println("ERROR" + je.getMessage());
		    je.printStackTrace();
		    System.exit(0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
	}
    
    public String toString() {
    	return Integer.toString(id);
    }
}