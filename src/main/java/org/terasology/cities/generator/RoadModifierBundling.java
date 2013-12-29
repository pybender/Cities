/*
 * Copyright 2013 MovingBlocks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terasology.cities.generator;

import java.util.Collection;
import java.util.Set;

import javax.vecmath.Point2i;
import javax.vecmath.Vector2d;

import org.terasology.cities.common.Point2iUtils;
import org.terasology.cities.model.Road;
import org.terasology.math.Vector2i;

import com.google.common.collect.Sets;

/**
 * Attraction forces act on road segments pulling close ones further together (until they overlap)
 * @author Martin Steiger
 */
public class RoadModifierBundling {

    /**
     * @param roads a set of all roads that act forces on each other
     * @return a new set of roads that contain a modified version of localRoads
     */
    public Set<Road> apply(Set<Road> roads) {

        Set<Road> newRoads = Sets.newHashSet();
        
        for (Road road : roads) {
            
            newRoads.add(attractRoads(road, roads));
        }

        return newRoads;
    }

    private Road attractRoads(Road road, Collection<Road> others) {

        Road newRoad = new Road(road.getStart(), road.getEnd());

        for (Point2i n : road.getPoints()) {
            Point2i np = new Point2i(n);

            for (Road other : others) {
                if (road.equals(other)) {
                    continue;
                }

                np.add(getInfluence(n, other.getPoints()));
            }

            newRoad.add(np);
        }

        return newRoad;
    }

    private Vector2i getInfluence(Point2i p, Collection<Point2i> points) {
        final double maxDist = 0.15;

        Vector2d influence = new Vector2d(0, 0);
        
        double fac = 0.02;
        
        for (Point2i op : points) {
            double distSq = Point2iUtils.distanceSquared(op, p);
            
            if (distSq < maxDist * maxDist) {
                double dist = Math.sqrt(distSq);
                double inf = (1.0 - dist / maxDist) * fac;
                Vector2d dir = new Vector2d(op.x, op.y);
                dir.x -= p.x;
                dir.y -= p.y;
                dir.scale(inf / dist);
                influence.add(dir);
            }
        }

        return new Vector2i((int) influence.x, (int) influence.y);
    }

}
