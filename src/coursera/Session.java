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

public class Session {
	private List<Session> elements;
    public String status, name, durationString, homeLink, eligibleForSignatureTrack, active, eligibleForCertificates;
    public Integer id, startYear, startDay, startMonth, courseId;
    
    public void setElements(List<Session> elements) { this.elements = elements; }
	public List<Session> getElements() { return elements; }
	
	public OntModel writeToRDF(OntModel m) {
		Prefixes p = new Prefixes();
	    Date date = new Date();
	    Timestamp timestamp = new Timestamp(date.getTime());

	    try {
	    	// Set up log file
	    	File logFile = new File("log/coursera/session/session_log_" + timestamp);
		    FileOutputStream log = new FileOutputStream(logFile);
		    String header = "New Coursera sessions:\n" + timestamp.toString() 
		    		+ "\n-------------------------\n\n";
		    log.write(header.getBytes());
		    
		    // Get ontology properties
		    OntClass cSession = m.getOntClass(p.mooc + "Session");
		    Property pName = m.getProperty(p.schema + "name");
		    Property pSessionStatus = m.getProperty(p.mooc + "Session_Status");
		    Property pId = m.getProperty(p.mooc + "Session_ID");
		    Property pActive = m.getProperty(p.mooc + "Active");
		    Property pStartDate = m.getProperty(p.mooc + "Start_Date");
		    Property pEligibleTrack = m.getProperty(p.mooc + "Eligible_for_Signature_Track");
		    Property pEligibleCertificate = m.getProperty(p.mooc + "Eligible_for_Certificates");
		    Property pDuration = m.getProperty(p.mooc + "Duration");
		    Property pBelongsToCourse = m.getProperty(p.mooc + "belongsToCourse");
		    Property pHasSession = m.getProperty(p.mooc + "hasSession");
		    
		    for(int i = 0; i < elements.size(); i ++) {
		    	Session e = elements.get(i);
		    	String uri = p.mooc + "coursera_session_" + Integer.toString(e.id);
		    	
				// Check to see if University exists
				if(m.getIndividual(uri) == null) {
					Individual session = m.createIndividual(uri,  cSession);
					
					// Assign session status
					if(!e.status.isEmpty())
						session.addProperty(pSessionStatus, e.status);
					
					// Assign id
					if(e.id != null)
						session.addProperty(pId, Integer.toString(e.id));
					
					// Assign active
					if(!e.active.isEmpty())
						session.addProperty(pActive, e.active);
					
					// Assign startDate
					if(e.startDay != null) {
						String startDate = Integer.toString(e.startMonth) + "/" + Integer.toString(e.startDay) + "/" + Integer.toString(e.startYear);
						session.addProperty(pStartDate, startDate);
					}
					
					// Assign eligibleForSignatureTrack
					if(e.eligibleForSignatureTrack != null)
						session.addProperty(pEligibleTrack, e.eligibleForSignatureTrack);
					
					// Assign eligibleForCertificate
					if(e.eligibleForCertificates != null)
						session.addProperty(pEligibleCertificate, e.eligibleForCertificates);
					
					// Assign durationString
					if(e.durationString != null)
						session.addProperty(pDuration, e.durationString);
					
					// Assign belongsToCourse
					String courseURI = p.mooc + "coursera_course_" + Integer.toString(e.courseId);
					if(m.getIndividual(courseURI) != null) {
			    		session.addProperty(pBelongsToCourse, courseURI);
		    			Individual course = m.getIndividual(courseURI);
		    			course.addProperty(pHasSession, uri);
			    	}
					
					// Assign session name
					if(!e.name.isEmpty())
						m.setNsPrefix("schema", p.schema);
						session.addProperty(pName, e.name);
				}
		    }
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
//		return String.format("name:%s,id:%d,instructor:%s,aboutTheInstructor:%s,video:%s,language:%s", name, id, instructor, aboutTheInstructor, video, language);
    }
}