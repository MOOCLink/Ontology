package coursera;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import rdf.Prefixes;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.JenaException;

public class Course {
	private List<Course> elements;
	private Link links;
    private String name, instructor, aboutTheInstructor, video, language, shortDescription, aboutTheCourse, estimatedClassWorkload,
    recommendedBackground, courseFormat, shortName, courseSyllabus;
    private Integer id;
    
    public List<Course> getElements() { return elements; }
    
    public void setElements(List<Course> elements) { this.elements = elements; }
    
    class Link {
    	public List<Integer> instructors;
    	public List<Integer> categories;
    	
    	public List<Integer> getInstructors() { return instructors; }
    	public List<Integer> getCategories() { return categories; }
    	
    	public void setInstructors(List<Integer> instructors) { this.instructors = instructors; }
    	public void setCategories(List<Integer> categories) { this.categories = categories; }
    }
    
    public OntModel writeToRDF(OntModel m) {
		Prefixes p = new Prefixes();
	    Date date = new Date();
	    Timestamp timestamp = new Timestamp(date.getTime());
	    int courseCount = 0;

	    try {
	    	// Set up log file
	    	File logFile = new File("log/coursera/course/course_log_" + timestamp);
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
		    Property pWorkload = m.getProperty(p.schema, "timeRequired");
		    Property pRecommendedBg = m.getProperty(p.mooc + "Recommended_Background");
		    Property pCourseFmt = m.getProperty(p.mooc + "Course_Format");
		    Property pCourseSyl = m.getProperty(p.mooc + "Course_Syllabus");
		    Property pHasCategory = m.getProperty(p.mooc + "hasCategory");
		    Property pIncludesCourse = m.getProperty(p.mooc + "includesCourse");
		    Property pTeaches = m.getProperty(p.mooc + "teaches");
		    Property pIsTaughtBy = m.getProperty(p.mooc + "isTaughtBy");
		    
		    for(int i = 0; i < elements.size(); i ++)
		    {
		    	Course e = elements.get(i);
				String eName = e.name.replaceAll(" ", "_");
				
				// Check to see if course exists
				// Name scheme: http://sebk.me/MOOC.owl#coursera<ID>
				String uri = p.mooc + "coursera_course_" + Integer.toString(e.id);
				if(m.getIndividual(uri) == null) {
			    	// Create an individual
				    Individual course = m.createIndividual(uri, courseClass);
				    
				    // Assign instructor
				    if(e.links.getInstructors() != null) {
				    	List<Integer> instIds = e.links.getInstructors();
				    	for(int j = 0; j < instIds.size(); j++) {
				    		String instId = Integer.toString(instIds.get(j));
				    		String instURI = p.mooc + "coursera_instructor_" + instId;
				    		if(m.getIndividual(instURI) != null) {
				    			Individual instructor = m.getIndividual(instURI);
				    			instructor.addProperty(pTeaches, uri);
				    			course.addProperty(pIsTaughtBy, instURI);
				    		}
				    	}
				    }
				    
				    
				    // Assign video link -> schema.org/video
				    if(!e.video.isEmpty()) {
					    String eVideo = "https://www.youtube.com/watch?v=" + e.video;
					    course.addProperty(pVideo, eVideo);
				    }
				    
				    // Assign language -> schema.org/inLanguage
				    if(!e.language.isEmpty())
				    	course.addProperty(pLanguage, e.language);
				    
				    // Assign aboutTheCourse -> schema.org/about
				    if(!e.aboutTheCourse.isEmpty())
				    	course.addProperty(pAbout, e.aboutTheCourse);
				    
				    // Assign estimatedClassWorkload -> schema.org/timeRequired
				    m.setNsPrefix("schema", p.schema);
				    course.addProperty(pWorkload, e.estimatedClassWorkload);
				    
				    // Assign name
				    eName = eName.replaceAll("_", " ");
				    eName = eName.replaceAll("C#", "C_Sharp"); // hard-code fix
				    course.addProperty(pName, eName);
				    
				    // Assign category
				    if(e.links.getCategories() != null) {
				    	List<Integer> catIds = e.links.getCategories();
				    	boolean cs = false;
				    	for(int j = 0; j < catIds.size(); j++) {
				    		String catId = Integer.toString(catIds.get(j));
				    		String catURI = p.mooc + "category_" + catId;
				    		
				    		if(m.getIndividual(catURI) != null) {
				    			Individual category = m.getIndividual(catURI);
				    			category.addProperty(pIncludesCourse, uri);
				    			course.addProperty(pHasCategory, catURI);
				    		}
				    		
				    		// If CS course, add to category_99 & vice-versa
			    			if((catURI.contains("_1") || catURI.contains("_17") || 
			    					catURI.contains("_12") || catURI.contains("_11")) && cs == false) {
			    				Individual category = m.getIndividual(p.mooc + "category_99");
			    				category.addProperty(pIncludesCourse, uri);
			    				course.addProperty(pHasCategory, p.mooc + "category_99");
			    				cs = true;
			    			}
				    	}
				    }
				    
				    // Assign link to reviews -> schema.org/discussionURL
				    
				    // Assign rating -> schema.org/aggregateRating
				    
				    // Assign publisher -> schema.org/publisher
				    
				    // Assign difficulty -> Difficulty
				    
				    // Assign ID -> Course_ID
				    if(e.id != null) {
				    	course.addProperty(pId, Integer.toString(e.id));
				    }
				    
				    // Assign recommendedBackground -> Recommended_Background
				    if(!e.recommendedBackground.isEmpty()) {
				    	course.addProperty(pRecommendedBg, e.recommendedBackground);
				    }
				    
				    // Assign courseFormat -> Course_Format
				    if(!e.courseFormat.isEmpty()) {
				    	course.addProperty(pCourseFmt, e.courseFormat);
				    }
				    
				    // Assign courseSyllabus -> Course_Syllabus
//				    if(e.courseSyllabus != null) {
//				    	course.addProperty(pCourseSyl, e.courseSyllabus);
//				    }
				    
				    // Coursera API currently returns syllabus JSON element with HTML tags 
				    // intact. This should ideally be cleansed in getCourseraCourses.py
				    // (perhaps somewhere else in the pipeline) before conversion into RDF.
				    
				    // Log entry
				    String entry = "Individual Name: " + "coursera_course_" + Integer.toString(e.id) + "\nCourse Name: " + eName + "\nID: " + Integer.toString(e.id) + "\n\n";
				    log.write(entry.getBytes());
				    courseCount++;
				}
		    }
		    String total = Integer.toString(courseCount) + " courses added."; 
		    log.write(total.getBytes());
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