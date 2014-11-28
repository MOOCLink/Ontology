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
import com.hp.hpl.jena.shared.JenaException;


public class University {
	private List<University> elements;
    private String name, website;
    private Integer id;
    
    public List<University> getElements() { return elements; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getWebsite() { return website; }
    
    public void setElements(List<University> elements) { this.elements = elements; }
	public void setId(int id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setWebsite(String website) { this.website = website; }
	
	public OntModel writeToRDF(OntModel m) {
		Prefixes p = new Prefixes();
	    Date date = new Date();
	    Timestamp timestamp = new Timestamp(date.getTime());

	    try {
	    	// Set up log file
	    	File logFile = new File("log/coursera/university/university_log_" + timestamp);
		    FileOutputStream log = new FileOutputStream(logFile);
		    String header = "New Coursera universities:\n" + timestamp.toString() 
		    		+ "\n-------------------------\n\n";
		    log.write(header.getBytes());
		    
		    // Get ontology properties
		    OntClass organization = m.getOntClass(p.schema + "Organization");
		    Property pName = m.getProperty(p.schema + "name");
		    Property pURL = m.getProperty(p.schema + "url");
		    Property pId = m.getProperty(p.mooc +"Organization_ID");
		    
		    
		    for(int i = 0; i < elements.size(); i ++)
		    {
		    	University e = elements.get(i);
		    	String uri = p.mooc + "coursera_university_" + Integer.toString(e.id);
		    	
				// Check to see if University exists
				if(m.getIndividual(uri) == null)
				{
			    	// Create an individual
				    Individual university = m.createIndividual(uri, organization);
				    
				    // Assign ID
				    if(e.id != null) {
				    	university.addProperty(pId, Integer.toString(e.id));
				    }
				    
				    // Assign url
				    if(!e.website.isEmpty()) {
				    	m.setNsPrefix("schema", p.schema);
				    	university.addProperty(pURL, e.website);
				    }
				    
				    // Assign name
				    if(!e.name.isEmpty()) {
					    m.setNsPrefix("schema", p.schema);
					    university.addProperty(pName, e.name);
				    }
				    
				    // Log entry
				    String entry = "University name: " + e.name + "\nID: " + Integer.toString(e.id) + "\nWebsite: " 
				    + e.website + "\n\n";
				    log.write(entry.getBytes());
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