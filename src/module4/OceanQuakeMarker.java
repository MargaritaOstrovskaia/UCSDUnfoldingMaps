package module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Margarita Ostrovskaia
 * Date: March 11, 2019
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	private int sizeRect = 5;
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
		
		float mag = this.getMagnitude();
		if(mag >= 5)
			sizeRect = 15;
		else if (mag >= 4)
			sizeRect = 10;
	}

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		// Drawing a centered square for Ocean earthquakes
		// DO NOT set the fill color.  That will be set in the EarthquakeMarker
		// class to indicate the depth of the earthquake.
		// Simply draw a centered square.
		
		// HINT: Notice the radius variable in the EarthquakeMarker class
		// and how it is set in the EarthquakeMarker constructor
		
		// TODO: Implement this method
		// M.O.
		pg.rect(x-(sizeRect/2), y-(sizeRect/2), sizeRect, sizeRect);
	}
}