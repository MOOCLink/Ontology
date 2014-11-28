package coursera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import com.google.gson.Gson;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;


public class CourseraJSON2RDF {
	private static String inputOntology;
	private static String categoryJSON;
	private static String universityJSON;
	private static String instructorJSON;
	private static String courseJSON;
	private static String sessionJSON;
	private static String outputFileName;
	private static String logFileName;
	private static final Gson gson = new Gson();
	
	/**
	 * Parse the command line arguments and update the instance variables. Command line arguments are 
	 * of the form <inputOntology> <categoryJSON> <universityJSON> <instructorJSON>
	 * <courseJSON> <sessionJSON> <outputFileName> <logFileName>
	 *
	 * @param args arguments
	 */
	public void parseArgs(String[] args) {
		inputOntology = args[0];
		categoryJSON = args[1];
		universityJSON = args[2];
		instructorJSON = args[3];
		courseJSON = args[4];
		sessionJSON = args[5];
		outputFileName = args[6];
		logFileName = args[7];
	}
	
	/**
	 * Convert JSON collected from the Coursera API into RDF
	 * using the MOOC OWL2 ontology.
	 *
	 * @param args arguments
	 */
	public void convertJSON2RDF(String[] args) {
		try {
			parseArgs(args);
			
			// Read in ontology model
			OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
			InputStream in = FileManager.get().open(inputOntology);
		    m.read(in, "RDF/XML");
		    
		    // Read in Coursera category JSON and write to model
		    BufferedReader br = new BufferedReader(
					new FileReader(categoryJSON));
			Category categoryJson = gson.fromJson(br, Category.class);
		    categoryJson.writeToRDF(m);
		    
		    // Read in Coursera university JSON and write to model
		    br = new BufferedReader(
					new FileReader(universityJSON));
		    University universityJson = gson.fromJson(br, University.class);
		    universityJson.writeToRDF(m);
		    
		    // Read in Coursera instructor JSON and write to model
		    br = new BufferedReader(
					new FileReader(instructorJSON));
		    Instructor instructorJson = gson.fromJson(br, Instructor.class);
		    instructorJson.writeToRDF(m);
		    
		    // Read in Coursera course JSON and write to model
		    br = new BufferedReader(
					new FileReader(courseJSON));
			Course courseJson = gson.fromJson(br, Course.class);
		    courseJson.writeToRDF(m);
		    
		    // Read in Coursera session JSON and write to model
		    br = new BufferedReader(
					new FileReader(sessionJSON));
			Session sessionJson = gson.fromJson(br, Session.class);
		    sessionJson.writeToRDF(m);
		    
		    // Output model as RDF
		    File file = new File(outputFileName);
		    FileOutputStream out = new FileOutputStream(file);
		    m.write(out);
		    out.close();
		} catch (JenaException je) {
		    System.out.println("ERROR: " + je.getMessage());
		    je.printStackTrace();
		    System.exit(0) ;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

//	public static void main(String[] args) throws IOException {
//			CourseraJSON2RDF r = new CourseraJSON2RDF();
//
//			//	Example instance variables:
//			String[] args2 = {"data/rdf/MOOC.owl", "data/coursera/categories.json",
//					"data/coursera/universities.json",	"data/coursera/instructors.json",
//					"data/coursera/courses.json", "data/coursera/sessions.json",
//				    "data/rdf/mooc_9_26.rdf", "log/json_conversion_log_9_26.txt"};
//			r.convertJSON2RDF(args2);
//	}
	
}
