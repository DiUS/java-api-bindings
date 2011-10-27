/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.springsense.disambig;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MeaningNeighboursTest {
    private static MeaningNeighbours meaningNeighbours = null;

    public MeaningNeighboursTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        meaningNeighbours = MeaningNeighbours.loadFromCsv(new File("src/test/java/com/springsense/disambig/test_neighbours.csv"));
    }

    @Test
    public void testLoadFromCsv() {
        assertEquals(10, meaningNeighbours.size());

        MeaningNeighbours.Neighbour[] expectedNeighboursForCelestialNavigation = {
            new MeaningNeighbours.Neighbour("navigation_n_01", 0.677024722099),
            new MeaningNeighbours.Neighbour("dead_reckoning_n_02", 0.895686626434)
        };
        assertArrayEquals(expectedNeighboursForCelestialNavigation, meaningNeighbours.getNeighboursForMeaning("celestial_navigation_n_01").toArray());

        MeaningNeighbours.Neighbour[] expectedNeighboursForZurvanism = {
            new MeaningNeighbours.Neighbour("heresy_n_02", 0.769569039345)
        };

        assertArrayEquals(expectedNeighboursForZurvanism, meaningNeighbours.getNeighboursForMeaning("zurvanism_n_02").toArray());
    }

    @Test
    public void testReplaceAll() {
    	assertEquals("TAFESA Vocational units in Small Business - Open Universities Australia",
    			"TAFESA?Vocational units in Small Business - Open Universities Australia".replaceAll("\\?", " "));
    	assertEquals("Monash Postgraduate units in Primary Education - Open Universities Australia",
    			"Monash?Postgraduate units in Primary Education - Open Universities Australia".replaceAll("\\?", " "));
    }
}