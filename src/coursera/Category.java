package coursera;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import java.util.Date;

import rdf.Prefixes;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;


public class Category {
	private List<Category> elements;
    private String name, shortName, description;
    private int id;
    
    public List<Category> getElements() { return elements; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public String getDescription() { return description; }
    
    public void setElements(List<Category> elements) { this.elements = elements; }
	public void setId(int id) { this.id = id; }
	public void setName(String name) { this.name = name; }
	public void setShortName(String shortName) { this.shortName = shortName; }
	public void setDescription(String description) { this.description = description; }
	
	public OntModel writeToRDF(OntModel m) {
		Prefixes p = new Prefixes();
	    Date date = new Date();
	    Timestamp timestamp = new Timestamp(date.getTime());

	    try {
	    	// Set up log file
	    	File logFile = new File("log/coursera/category/category_log_" + timestamp);
		    FileOutputStream log = new FileOutputStream(logFile);
		    String header = "New Coursera categories:\n" + timestamp.toString() 
		    		+ "\n-------------------------\n\n";
		    log.write(header.getBytes());
		    
		    OntClass category = m.getOntClass(p.mooc + "Category");
		    
		    for(int i = 0; i < elements.size(); i ++)
		    {
				// Retrieve element name, id, shortName
				String eName = elements.get(i).name.replaceAll(" ", "_");
				String eId = Integer.toString(elements.get(i).id);
				String eShortName = elements.get(i).shortName;
				
				// Check to see if category exists
				if(m.getIndividual(p.mooc + eId) == null)
				{
			    	// Create an individual
				    Individual CS = m.createIndividual(p.mooc + "category_" + eId, category);
				    
				    // Assign ID
				    Property pId = m.getProperty(p.mooc + "Category_ID");
				    CS.addProperty(pId, eId);
				    
				    // Assign shortName
				    Property pShortName = m.getProperty(p.mooc + "Short_Name");
				    CS.addProperty(pShortName, eShortName);
				    
				    // Assign name
				    m.setNsPrefix("schema", p.schema);
				    eName = eName.replaceAll("_", " ");
				    Property pName = m.getProperty(p.schema + "name");
				    CS.addProperty(pName, eName);
				    
				    // Log entry
				    String entry = "Category Name: " + eName + "\nID: " + eId + "\nShort Name: " + eShortName + "\n\n";
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
    
    public static void main(String[] args) 
	{
    	final Gson gson = new Gson();
		try{
			String inputFileName = "data/rdf/MOOC.owl";
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
			InputStream in = FileManager.get().open(inputFileName);
		    m.read(in, "RDF/XML");
		    
		    BufferedReader br = new BufferedReader(
					new FileReader("data/coursera/categories.json"));
			Category categoryJson = gson.fromJson(br, Category.class);
		    categoryJson.writeToRDF(m);
		    
		    File file = new File("data/rdf/mooc_7_1.rdf");
		    FileOutputStream out = new FileOutputStream(file);
		    m.write(out);
		    out.close();
	    
		} catch (JenaException je) {
		    System.out.println("ERROR" + je.getMessage());
		    je.printStackTrace();
		    System.exit(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}