package udacity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rdf.Prefixes;

import com.google.gson.*;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;


public class UdacityCourse {
	private List<UdacityCourse> elements;
	private List<String> name, project, about, instructor, school, startDate, url, length, prereqs, video, syllabus;
	private String courseCode, effort;
	
	public List<UdacityCourse> getElements() {
		return elements;
	}

	public void setElements(List<UdacityCourse> elements) {
		this.elements = elements;
	}
	
    public OntModel writeToRDF(OntModel m) {
			Prefixes p = new Prefixes();
		    Date date = new Date();
		    Timestamp timestamp = new Timestamp(date.getTime());

		    try {
		    	// Set up log file
		    	File logFile = new File("log/udacity/course_log_" + timestamp);
			    FileOutputStream log = new FileOutputStream(logFile);
			    String header = "New Coursera courses:\n" + timestamp.toString() 
			    		+ "\n-------------------------\n";
			    log.write(header.getBytes());
			    
			    // Get ontology classes & properties
			    OntClass courseClass = m.getOntClass(p.mooc + "Course");
			    Property pName = m.getProperty(p.schema + "name");
			    Property pId = m.getProperty(p.mooc + "Course_ID");
			    Property pVideo = m.getProperty(p.schema + "video");
			    Property pLanguage = m.getProperty(p.schema, "inLanguage");
			    Property pAbout = m.getProperty(p.schema, "about");
			    Property pDuration = m.getProperty(p.mooc + "Duration");
			    Property pWorkload = m.getProperty(p.schema, "timeRequired");
			    Property pStartDate = m.getProperty(p.mooc + "Start_Date");
			    Property pRecommendedBg = m.getProperty(p.mooc + "Recommended_Background");
			    Property pCourseSyl = m.getProperty(p.mooc + "Course_Syllabus");
			    Property pTeaches = m.getProperty(p.mooc + "teaches");
			    Property pIsTaughtBy = m.getProperty(p.mooc + "isTaughtBy");
			    Property pHasCategory = m.getProperty(p.mooc + "hasCategory");
			    Property pIncludesCourse = m.getProperty(p.mooc + "includesCourse");
			    
			    OntClass person = m.getOntClass(p.schema + "Person");
			    Property pInstructorName = m.getProperty(p.schema + "name");
			    Property pInstructorAffiliation = m.getProperty(p.schema + "affiliation");
			    
			    String catURI = p.mooc + "category_12";
	    		String csURI = p.mooc + "category_99";
			    
			    for(int i = 0; i < elements.size(); i ++)
			    {
			    	UdacityCourse e = elements.get(i);
					String eName = e.name.get(0).replaceAll(" ", "_");
					String eId = e.courseCode;
					String eVideo = listToString(e.video);
					String eLanguage = "English";
					String eAbout = listToString(e.about);
					String eDuration = listToString(e.length);
					String eWorkload = e.effort;
					String eRecommendedBg = listToString(e.prereqs);
					String eCourseSyl = listToString(e.syllabus);
					String eInstructor = listToString(e.instructor);
					String eSchool = listToString(e.school);
					String eStartDate = listToString(e.startDate);
					
					Map<Property, String> propToElement = new HashMap<Property, String>();
					propToElement.put(pId, eId);
					propToElement.put(pVideo, eVideo);
					propToElement.put(pLanguage, eLanguage);
					propToElement.put(pAbout, eAbout);
					propToElement.put(pDuration, eDuration);
					propToElement.put(pRecommendedBg, eRecommendedBg);
					propToElement.put(pCourseSyl, eCourseSyl);
					propToElement.put(pStartDate, eStartDate);
					
					// Check to see if course exists
					// Name scheme: http://sebk.me/MOOC.owl#coursera<ID>
					String uri = p.mooc + "udacity_course_" + eId;
					if(m.getIndividual(uri) == null) {
				    	// Create an individual
					    Individual course = m.createIndividual(uri, courseClass);
					    
					    // Assign MOOC properties
					    for(Map.Entry<Property, String> entry : propToElement.entrySet()) {
					    	if(!entry.getValue().isEmpty() && entry.getValue() != null) {
					    		course.addProperty(entry.getKey(), entry.getValue());
					    	}
					    }
					    
					    // Assign category (Udacity courses are all Software Engineering / CS)
			    		if(m.getIndividual(catURI) != null) {
			    			Individual category = m.getIndividual(catURI);
			    			category.addProperty(pIncludesCourse, uri);
			    			course.addProperty(pHasCategory, catURI);
			    			
			    			category = m.getIndividual(csURI);
			    			category.addProperty(pIncludesCourse, uri);
			    			course.addProperty(pHasCategory, csURI);
			    		}
					    
					    // Assign schema properties
					    m.setNsPrefix("schema", p.schema);
					    if(!eName.isEmpty()) {
					    	course.addProperty(pName, eName);
					    }
					    
					    if(!eWorkload.isEmpty()) {
					    	course.addProperty(pWorkload, eWorkload);
					    }
					    
					    // Create instructor
					    String instURI = p.mooc + "udacity_instructor_" + eId;
					    Individual instructor;
					    if(m.getIndividual(instURI) == null) 
					    	instructor = m.createIndividual(instURI, person);
					    else 
					    	instructor = m.getIndividual(instURI);
					    
					    // Assign instructor properties
					    instructor.addProperty(pInstructorName, eInstructor);
				    	instructor.addProperty(pInstructorAffiliation, eSchool);
				    	
				    	// Assign instructor, course object properties
				    	instructor.addProperty(pTeaches, uri);
		    			course.addProperty(pIsTaughtBy, instURI);
		    			
		    			// Log entry
					    String entry = "Category Name: " + eName + "\nID: " + eId;
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
    
    public String listToString(List<String> ls) {
    	String s = "";
    	for(int i = 0; i < ls.size(); i++) {
    		s += ls.get(i);
    	}
    	return s;
    }

	public static void main(String[] args) 
		{
	    	final Gson gson = new Gson();
			try{
				String inputFileName = "data/rdf/mooc_7_10.rdf";
				OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
				InputStream in = FileManager.get().open(inputFileName);
			    m.read(in, "RDF/XML");
			    
			    BufferedReader br = new BufferedReader(
						new FileReader("data/udacity/udacity.json"));
		        UdacityCourse courseJson = gson.fromJson( br , UdacityCourse.class);
		        List<UdacityCourse> courses = courseJson.getElements();

			    courseJson.writeToRDF(m);
			    
			    File file = new File("data/rdf/mooc_7_10_uu.rdf");
			    FileOutputStream out = new FileOutputStream(file);
			    m.write(out);
			    out.close();
		    
			} catch (JenaException je) {
			    System.out.println("ERROR: " + je.getMessage());
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
