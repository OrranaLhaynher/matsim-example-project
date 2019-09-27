package org.matsim.project.population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.shape.random.RandomPointsBuilder;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.opengis.feature.simple.SimpleFeature;

public class ShapeFile {

	public static Point getRandomPointInFeature(Random rnd, SimpleFeature ft) {
        Point p = null;
		double x, y;
		do {
			x = ft.getBounds().getMinX() + rnd.nextDouble() * (ft.getBounds().getMaxX() - ft.getBounds().getMinX());
			y = ft.getBounds().getMinY() + rnd.nextDouble() * (ft.getBounds().getMaxY() - ft.getBounds().getMinY());
			p = MGC.xy2Point(x, y);
		} while (((Geometry) ft.getDefaultGeometry()).contains(p));
		return p;
	}

    public static Coord getCoordInGeometry(SimpleFeature ft) {
		//landcover <- area toda
		double x, y;
		Point point;
		GeometryFactory geometryFactory = new GeometryFactory();
		Random random = new Random();
		Geometry regrion = (Geometry) ft.getDefaultGeometry();
		
		// if the landcover feature is in the correct region generate a random coordinate within the bounding box of the
		// landcover feature. Repeat until a coordinate is found which is actually within the landcover feature.
		do {
			Envelope envelope = regrion.getEnvelopeInternal();

			x = envelope.getMinX() + envelope.getWidth() * random.nextDouble();
			y = envelope.getMinY() + envelope.getHeight() * random.nextDouble();
			point = geometryFactory.createPoint(new Coordinate(x, y));
		} while (point == null || !regrion.contains(point));

		return new Coord(x, y);
	}

    public static Coord getCoordInGeometry(ArrayList<SimpleFeature> t) {
        Iterator<SimpleFeature> iter = t.iterator(); 
        Collections.shuffle(t); 
    	
        RandomPointsBuilder randomPointsBuilder = new RandomPointsBuilder(new GeometryFactory());
        randomPointsBuilder.setNumPoints(1);
        randomPointsBuilder.setExtent( (Geometry)iter.next().getDefaultGeometry());
        Coordinate coordinate = randomPointsBuilder.getGeometry().getCoordinates()[0];
        return MGC.coordinate2Coord(coordinate);
	}
    
    public static SimpleFeatureCollection correctFeatures(SimpleFeatureCollection fc){
        SimpleFeatureIterator iterator=fc.features();
        DefaultFeatureCollection outVector = new DefaultFeatureCollection();
        Geometry gm = null;
        while(iterator.hasNext()){
            SimpleFeature sf=iterator.next();
            gm=(Geometry)sf.getDefaultGeometry();
            sf.setDefaultGeometry(gm);
            outVector.add(sf);
   
        }
        return outVector;
    }
    

}