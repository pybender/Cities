/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.cities.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import javax.vecmath.Point2i;

import org.junit.Before;
import org.junit.Test;
import org.terasology.cities.AreaInfo;
import org.terasology.cities.CityTerrainComponent;
import org.terasology.cities.model.Site;
import org.terasology.commonworld.CachingFunction;
import org.terasology.commonworld.Sector;
import org.terasology.commonworld.Sectors;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.commonworld.heightmap.NoiseHeightMap;
import org.terasology.commonworld.symmetry.Symmetries;
import org.terasology.commonworld.symmetry.Symmetry;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Tests {@link SiteFinderRandom}
 * @author Martin Steiger
 */
public class SymmetricSiteFinderTest  {

    private static Function<Site, Point2i> site2pos = new Function<Site, Point2i>() {

        @Override
        public Point2i apply(Site input) {
            return input.getPos();
        }
        
    };

    private SymmetricSiteFinder symFinder;
    private Symmetry symmetry = Symmetries.alongPositiveDiagonal();

    private final int minPerSector = 2;
    private final int maxPerSector = 5;

    @Before
    public void setup() {
        String seed = "asd";
        
        int minSize = 10;
        int maxSize = 100;
        
        final CityTerrainComponent config = new CityTerrainComponent();
        final HeightMap heightMap = HeightMaps.symmetric(new NoiseHeightMap(seed), symmetry);
        final Function<Sector, AreaInfo> sectorInfos = CachingFunction.wrap(new Function<Sector, AreaInfo>() {

            @Override
            public AreaInfo apply(Sector input) {
                return new AreaInfo(config, heightMap);
            }
        }); 
        
        SiteFinderRandom cpr = new SiteFinderRandom(seed, sectorInfos, minPerSector, maxPerSector, minSize, maxSize);

        symFinder = new SymmetricSiteFinder(cpr, symmetry);
    }
    
    @Test
    public void testDifferent() {

        Sector sectorA = Sectors.getSector(4, 8);
        Sector sectorB = Sectors.getSector(symmetry.getMirrored(4, 8));

        Set<Site> sitesA = symFinder.apply(sectorA);
        Set<Site> sitesB = symFinder.apply(sectorB);
        
        assertMirrored(sitesA, sitesB);
    }

    @Test
    public void testSame() {

        Sector sectorA = Sectors.getSector(1, 1);
        Sector sectorB = Sectors.getSector(symmetry.getMirrored(1, 1));

        assertEquals(sectorA, sectorB);
        
        Set<Site> sitesA = symFinder.apply(sectorA);
        assertMirrored(sitesA, sitesA);
    }
    
    private void assertMirrored(Set<Site> sitesA, Set<Site> sitesB) {
        Collection<Point2i> posA = Collections2.transform(sitesA, site2pos);
        Collection<Point2i> posB = Collections2.transform(sitesB, site2pos);
        
        for (Point2i pos : posA) {
            Point2i mirrored = symmetry.getMirrored(pos);
            assertTrue(posB.contains(mirrored));
        }
    }
}
