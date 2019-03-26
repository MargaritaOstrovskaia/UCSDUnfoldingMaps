package module6;

import java.awt.Frame;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development MOOC team
 * @author Margarita Ostrovskaia (M.O.)
 * Date: March 18, 2019
 */
public class AirportMap extends PApplet {
	UnfoldingMap map;
	
	private Location defaultLocation;
	private boolean hideInternational = false;
	private boolean hideLocal = false;
	private String textFindCity = "";
	private int currentZoom = 2;
	
	private PImage imgBackground;
	private PImage imgZoom;
	private PImage imgZoomIn;
	private PImage imgZoomOut;

	private CommonMarker clickedMarker;
	
	private CommonMarker lastSelected;
	private Integer lastSelectedX;
	private Integer lastSelectedY;
	private String lastSelectedTitle;
	
	private List<Marker> airportList;
	private List<Marker> routeList;
	
	public void setup() {
		size(1025, 600, OPENGL);
		
		map = new UnfoldingMap(this, 225, 25, 750, 550);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// load icons
		PImage imgPlane = loadImage("plane.png");
		imgBackground = loadImage("background.jpg");
		imgZoom = loadImage("zoom.png");
		imgZoomIn = loadImage("zoom-in.png");
		imgZoomOut = loadImage("zoom-out.png");
		
		// set Applet title
	    Frame title = (Frame)this.getParent().getParent();
	    title.setTitle("Airports Map");
		changeLastSelected(null);
		
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		for(PointFeature feature : features) {
			String code = feature.getProperty("code").toString().replace("\"", "");
			if(!code.isEmpty()) {
				AirportMarker m = new AirportMarker(feature);
				m.setIconImage(imgPlane);
				m.setRadius(5);
				airportList.add(m);
				airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
			}
		}
		map.addMarkers(airportList);
		
		routeList = new ArrayList<Marker>();
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			sl.setHidden(true);
			routeList.add(sl);
		}
		map.addMarkers(routeList);
		
		defaultLocation = map.getCenter();
	}
	
	public void draw() {
		if (imgBackground != null)
			image(imgBackground, 0, 0);
		
		int x = 225;
		int y = 25;

		fill(0, 0, 0);
		rect(x, y, 750, 550);
		map.draw();
		
		if (imgZoomIn != null && imgZoomOut != null && imgZoom != null) {
			fill(135,206,235);
			rect(x+755, y, 40, 25*7, 5);//980
			
			image(imgZoomIn, x+760, y+25);// 985
			image(imgZoomOut, x+760, y+25*3);
			image(imgZoom, x+760, y+25*5);
		}
		
		drawShowHideButtons();
		drawSwitchButtons();
		drawAirportTitle();
	}

	private void drawShowHideButtons() {
		int x = 25;
		int y = 25;
		int rectSize = 15;

		fill(135,206,235);
		rect(x, y, 190, 100, 5);
		
		fill(25,25,112);
		textSize(18);
		text("Show/Hide Airports", x+5, y+5);
		textSize(12);
		
		// show/hide International Airports
		x = x + 5;
		y = y + 40;

		fill(175,238,238);
		rect(x, y, rectSize, rectSize);
		
		textAlign(PConstants.LEFT, PConstants.TOP);
		fill(0);
		if(hideInternational) {
			line(x, y, x+rectSize, y+rectSize);
			line(x+rectSize, y, x, y+rectSize);
			text("Show Internat. Airports", x+rectSize+5 , y+2);
		}
		else {
			text("Hide Internat. Airports", x+rectSize+5 , y+2);
		}
		
		// show/hide Local Airports
		y = y + 25;

		fill(175,238,238);
		rect(x, y, rectSize, rectSize);
		
		textAlign(PConstants.LEFT, PConstants.TOP);
		fill(0);
		if(hideLocal) {
			line(x, y, x+rectSize, y+rectSize);
			line(x+rectSize, y, x, y+rectSize);
			text("Show Local Airports", x+rectSize+5 , y+2);
		}
		else {
			text("Hide Local Airports", x+rectSize+5 , y+2);
		}
	}

	private void drawSwitchButtons() {
		int x = 25;
		int y = 140;

		fill(135,206,235);
		rect(x, y, 190, 110, 5);
		
		fill(25,25,112);
		textSize(18);
		text("Type airport name:", x+5, y+5);
		
		textSize(14);
		text(textFindCity, x+15, y+43);
		line(x+10, y+60, x+180, y+60);
		
		fill(175,238,238);
		rect(x+15, y+70, 75, 30, 5);
		rect(x+100, y+70, 75, 30, 5);
		
		fill(25,25,112);
		textSize(16);
		text("Find", x+35, y+75);
		text("Clear", x+120, y+75);
	}
	
	private void drawAirportTitle() {
		if (lastSelected==null)
			return;
		
		if(!overButton(225, 25, 750, 550))
			return;
		
		pushStyle();	
		rectMode(PConstants.CORNER);
		
		textSize(12);
		stroke(110);
		fill(153,204,255);
		rect(lastSelectedX, lastSelectedY+15, textWidth(lastSelectedTitle)+25, getRowCount(lastSelectedTitle)*20, 5);
			
		textAlign(PConstants.LEFT, PConstants.TOP);
		fill(0);
		text(lastSelectedTitle, lastSelectedX+5 , lastSelectedY+20);
			
		popStyle();
	}
	
	private int getRowCount(String str) {
		Pattern pattern = Pattern.compile("[\n]");
	    Matcher matcher = pattern.matcher(str);
		int result = 0;
	    while(matcher.find())
	    		result++;
		
		return result;
	}
	
	private boolean overButton(int x, int y, int width, int height)  {
		if (mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height)
			return true;
		else 
			return false;
	}
	
	@Override
	public void mousePressed() {
		int rectSize = 15;
		
		// show/hide International Airports
		if(overButton(30, 75, rectSize, rectSize)) {
			hideInternational = !hideInternational;
			
			for (Marker airport : airportList) 
				if (airport.getProperty("type") == "I")
					airport.setHidden(hideInternational);
		}
		
		// show/hide Local Airports
		if(overButton(30, 90, rectSize, rectSize)) {
			hideLocal = !hideLocal;
			
			for (Marker airport : airportList) 
				if (airport.getProperty("type") == "L")
					airport.setHidden(hideLocal);
		}
		
		// set Zoom In
		if(overButton(985, 50, 24, 24))
			setMapZoom(1);
		
		// set Zoom Out
		if(overButton(985, 100, 24, 24))
			setMapZoom(-1);
		
		// set Zoom 100%
		if(overButton(985, 150, 24, 24))
			setMapZoom(0);
		
		// find or clear airport
		if(overButton(40, 210, 75, 30)) {
			findAirportByName();
		}

		if(overButton(125, 210, 75, 30)) {
			clearText();
		}
	}

	private void clearText() {
		textFindCity = "";
		setLastStatus();
	}

	private void findAirportByName() {
		for (Marker airport : airportList) {
			String name = airport.getProperty("name").toString().toLowerCase();
			boolean isContains = name.contains(textFindCity.toLowerCase());
			
			if(isContains)
				airport.setHidden(false);
			else
				airport.setHidden(true);
		}
		
		hideRoutes();
	}
	

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		//super.mouseWheelMoved(e);
	}
	
	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			changeLastSelected(null);
		}
		else { // show airport title
			for (Marker airport : airportList) {
				CommonMarker marker = (CommonMarker)airport;
				if (marker.isInside(map,  mouseX, mouseY) && !marker.isHidden()) {
					marker.setSelected(true);
					changeLastSelected(marker);
					
					return;
				}
			}
		}
	}

	@Override
	public void mouseClicked()
	{
		checkAirportsForClick();
	}
	
	private void checkAirportsForClick()
	{
		if(!overButton(225, 25, 750, 550))
			return;

		for (Marker marker : airportList) {
			boolean isInside = marker.isInside(map, mouseX, mouseY);
			
			if (!marker.isHidden() && isInside) {
				clickedMarker = (CommonMarker)marker;
				String codeSelectedAirport = clickedMarker.getProperty("code").toString();

				// hide another airports
				for (Marker airport : airportList)
					if (airport != clickedMarker)
						airport.setHidden(true);
				
				// show routes from clicked airport
				for (Marker route : routeList) {
					String codeSource = route.getProperty("sourceCode").toString();
					
					if (codeSource.equals(codeSelectedAirport)) {
						route.setHidden(false);
						String codeDestination = route.getProperty("destinationCode").toString();
						
						// show destination airports
						for (Marker destination : airportList) {
							String codeDestinationAirport = destination.getProperty("code").toString();
							
							if (codeDestinationAirport.equals(codeDestination))
								destination.setHidden(false);
						}
					}
					else {
						route.setHidden(true);
					}
				}
				return;
			}
		}
		
		// not clicked airport marker
		clickedMarker = null;
		setLastStatus();
	}
	
	private void setLastStatus() {
		for (Marker airport : airportList) 
			if (airport.getProperty("type") == "I")
				airport.setHidden(hideInternational);
			else
				airport.setHidden(hideLocal);
		
		hideRoutes();
	}
	
	private void hideRoutes() {
		for (Marker route : routeList)
			route.setHidden(true);
	}
	
	private void changeLastSelected(CommonMarker marker) {
		lastSelected = marker;
			
		if (lastSelected != null) {
			lastSelectedX = mouseX;
			lastSelectedY = mouseY;
			lastSelectedTitle = ((AirportMarker)marker).getTitle();
		}
		else {
			lastSelectedX = null;
			lastSelectedY = null;
			lastSelectedTitle = null;
		}
	}
	
	private void setMapZoom(int zoom) {
		int zoomMin = 2;
		int zoomMax = 8;
		
		if(zoom == 0) {
			currentZoom = zoomMin;
			map.zoomAndPanTo(currentZoom, defaultLocation);
			return;
		}
		
		int zoomLevel = zoom + map.getZoomLevel();
		Location loc = map.getCenter();
		
		if (zoomMax >= zoomLevel && zoomLevel >= zoomMin)
			currentZoom = zoomLevel;
		else if (zoomLevel > zoomMax)
			currentZoom = zoomMax;
		else if (zoomLevel < zoomMin)
			currentZoom = zoomMin;
		
		map.zoomAndPanTo(currentZoom, loc);
	}

	public void keyPressed() {
		textFindCity = textFindCity + key;
	}
}