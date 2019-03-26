package module6;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Object;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development MOOC team
 * @author Margarita Ostrovskaia (M.O.)
 * Date: March 18, 2019
 */
public class AirportMarker extends CommonMarker {
	public static List<SimpleLinesMarker> routes;
	private PImage img;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	}

	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		if(this.isHidden())
			return;
		
		// set plain marker as image
		if (img != null)
			pg.image(img, x, y, 20, 10);
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {}
	
	// set plain image
	public void setIconImage(PImage image) {
		this.img = image;
	}
	
	public String getTitle() {
		String result = "";
		
		String name = this.getProperty("name").toString();
		if(!name.isEmpty() && name.length()>0)
			result += String.format("Airport name: %s\n", name);
		
		String code = this.getProperty("code").toString();
		if(!code.isEmpty() && code.length()>0)
			result += String.format("Airport code: %s\n", code);
		
		String country = this.getProperty("country").toString();
		if(!country.isEmpty() && country.length()>0)
			result += String.format("Country name: %s\n", country);
		
		String city = this.getProperty("city").toString();
		if(!city.isEmpty() && city.length()>0)
			result += String.format("City name: %s\n", city);

		return result;
	}
}