package edx;

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

import edx.CategoryMap;

public class EdXCourse {
	private List<EdXCourse> elements;
	private List<String> name, startDate, about, school, length, prereqs, video, courseCode, instructor, effort;
	private String url, category;
	
	public List<EdXCourse> getElements() {
		return elements;
	}

	public void setElements(List<EdXCourse> elements) {
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
			    Property pStartDate = m.getProperty(p.mooc + "Start_Date");
			    Property pWorkload = m.getProperty(p.schema, "timeRequired");
			    Property pRecommendedBg = m.getProperty(p.mooc + "Recommended_Background");
			    Property pTeaches = m.getProperty(p.mooc + "teaches");
			    Property pIsTaughtBy = m.getProperty(p.mooc + "isTaughtBy");
			    Property pHasCategory = m.getProperty(p.mooc + "hasCategory");
			    Property pIncludesCourse = m.getProperty(p.mooc + "includesCourse");
			    
			    OntClass person = m.getOntClass(p.schema + "Person");
			    Property pInstructorName = m.getProperty(p.schema + "name");
			    Property pInstructorAffiliation = m.getProperty(p.schema + "affiliation");
			    
			    CategoryMap cMapClass = new CategoryMap();
			    HashMap<String, String> cMap = cMapClass.getMap();
			    
			    for(int i = 0; i < elements.size(); i ++)
			    {
			    	EdXCourse e = elements.get(i);
					String eName = e.name.get(0).replaceAll(" ", "_");
					String eId = e.courseCode.get(0);
					String eVideo = listToString(e.video);
					String eLanguage = "English";
					String eAbout = listToString(e.about);
					String eDuration = listToString(e.length);
					String eWorkload = listToString(e.effort);
					String eRecommendedBg = listToString(e.prereqs);
					String eSchool = listToString(e.school);
					String eStartDate = listToString(e.startDate);
					
					Map<Property, String> propToElement = new HashMap<Property, String>();
					propToElement.put(pId, eId);
					propToElement.put(pVideo, eVideo);
					propToElement.put(pLanguage, eLanguage);
					propToElement.put(pAbout, eAbout);
					propToElement.put(pDuration, eDuration);
					propToElement.put(pRecommendedBg, eRecommendedBg);
					propToElement.put(pStartDate, eStartDate);
					
					// Check to see if course exists
					// Name scheme: http://sebk.me/MOOC.owl#coursera<ID>
					String uri = p.mooc + "edx_course_" + eId;
					if(m.getIndividual(uri) == null) {
				    	// Create an individual
					    Individual course = m.createIndividual(uri, courseClass);
					    
					    // Assign MOOC properties
					    for(Map.Entry<Property, String> entry : propToElement.entrySet()) {
					    	if(!entry.getValue().isEmpty() && entry.getValue() != null) {
					    		course.addProperty(entry.getKey(), entry.getValue());
					    	}
					    }
					    
					    // Assign category (Udacity courses are all Software Engineering)
					    if(e.category != null) {
				    		String catId = cMap.get(e.category);
				    		String catURI = p.mooc + "category_" + catId;
					    		
				    		if(m.getIndividual(catURI) != null) {
				    			Individual category = m.getIndividual(catURI);
				    			category.addProperty(pIncludesCourse, uri);
				    			course.addProperty(pHasCategory, catURI);
				    		}
				    	}
					    
					    // Assign schema properties
					    m.setNsPrefix("schema", p.schema);
					    eName = e.name.get(0).replaceAll("_", " ");
					    if(!eName.isEmpty()) {
					    	course.addProperty(pName, eName);
					    }
					    
					    if(!eWorkload.isEmpty()) {
					    	course.addProperty(pWorkload, eWorkload);
					    }
					    
					    // Create instructor
					    String instURI = p.mooc + "edx_instructor_" + eId;
					    Individual instructor;
					    if(m.getIndividual(instURI) == null) 
					    	instructor = m.createIndividual(instURI, person);
					    else 
					    	instructor = m.getIndividual(instURI);
					    
					    for(int j = 0; j < e.instructor.size(); j++) {
					    	// Assign instructor properties
						    instructor.addProperty(pInstructorName, e.instructor.get(j));
					    	instructor.addProperty(pInstructorAffiliation, eSchool);
					    	
					    	// Assign instructor, course object properties
					    	instructor.addProperty(pTeaches, uri);
			    			course.addProperty(pIsTaughtBy, instURI);
					    }
		    			
		    			// Log entry
					    String entry = "Category Name: " + eName + "\nID: " + eId + "\n";
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
				String inputFileName = "data/rdf/mooc_7_10_uu.rdf";
				OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, null);
				InputStream in = FileManager.get().open(inputFileName);
			    m.read(in, "RDF/XML");
			    
			    BufferedReader br = new BufferedReader(
						new FileReader("data/edx/edx.json"));
		        EdXCourse courseJson = gson.fromJson( br , EdXCourse.class);
		        List<EdXCourse> courses = courseJson.getElements();

			    courseJson.writeToRDF(m);
			    
			    File file = new File("data/rdf/mooc_7_10_edx.rdf");
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
