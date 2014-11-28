package edx;

import java.util.HashMap;

public class CategoryMap {
	private HashMap<String, String> map;
	public HashMap<String, String> getMap() {
		return map;
	}
	public void setMap(HashMap<String, String> map) {
		this.map = map;
	}
	
	public CategoryMap() {
	     map = new HashMap<String, String>();
		 map.put("xseries-courses", "310");
		 map.put("architecture", "309");
		 map.put("art-culture", "22");
		 map.put("biology-life-sciences", "10");
		 map.put("business-management", "13");
		 map.put("chemistry", "24");
		 map.put("communication", "301");
		 map.put("computer-science", "99");
		 map.put("economics-finance", "2");
		 map.put("education", "14");
		 map.put("electronics", "302");
		 map.put("energy-earth-sciences", "25");
		 map.put("engineering", "15");
		 map.put("environmental-studies", "303");
		 map.put("food-nutrition", "19");
		 map.put("health-safety", "8");
		 map.put("history", "304");
		 map.put("humanities", "6");
		 map.put("law", "21");
		 map.put("literature", "305");
		 map.put("math", "5");
		 map.put("medicine", "3");
		 map.put("music", "18");
		 map.put("philanthropy", "306");
		 map.put("philosophy-ethics", "307");
		 map.put("physics", "23");
		 map.put("science", "308");
		 map.put("social-sciences", "20");
		 map.put("statistics-data-analysis", "16");
	}
}
