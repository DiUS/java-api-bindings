package com.springsense.disambig;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An aggregate of the meaning neighbours for expanding queries after they have been disambiguated.
 */
public class MeaningNeighbours {

    private Map<String, List<Neighbour>> neighbours;

    private MeaningNeighbours() {
        neighbours = new HashMap<String, List<Neighbour>>();
    }

    static MeaningNeighbours loadFromCsv(File file) {
        try {
            MeaningNeighbours meaningNeighbours = new MeaningNeighbours();

            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine = null;
            while ((nextLine = reader.readNext()) != null) {
                loadCsvLine(nextLine, meaningNeighbours);
            }

            return meaningNeighbours;
        } catch (IOException ex) {
            throw new RuntimeException("Could not load meaning neighbours from CSV due to an error", ex);
        }
    }

    private static void loadCsvLine(String[] nextLine, MeaningNeighbours meaningNeighbours) throws NumberFormatException {
        String meaningId = normaliseMeaningId(nextLine[0]);
        List<Neighbour> neighbours = new ArrayList<Neighbour>((nextLine.length - 1) / 2);

        for (int i = 1; i < nextLine.length; i += 2) {
            if ((i + 1) < nextLine.length) {
                String neighbourMeaningId = nextLine[i];
                String neighbourDistanceStr = nextLine[i + 1];
                Neighbour neighbour = new Neighbour(normaliseMeaningId(neighbourMeaningId), Double.parseDouble(neighbourDistanceStr));
                neighbours.add(neighbour);
            }
        }
        meaningNeighbours.getNeighbours().put(meaningId, neighbours);
    }

    static private String normaliseMeaningId(String meaningId) {
        return meaningId.replace('.', '_');
    }

    public List<Neighbour> getNeighboursForMeaning(String meaningId) {
        return Collections.unmodifiableList(getNeighbours().get(meaningId));
    }

    public int size() {
        return neighbours.size();
    }

    private Map<String, List<Neighbour>> getNeighbours() {
        return neighbours;
    }

    public static class Neighbour {

        private final String meaningId;
        private final double distance;

        protected Neighbour(String meaningId, double distance) {
            this.meaningId = meaningId;
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public String getMeaningId() {
            return meaningId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Neighbour other = (Neighbour) obj;
            if ((this.meaningId == null) ? (other.meaningId != null) : !this.meaningId.equals(other.meaningId)) {
                return false;
            }
            if (this.distance != other.distance) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.meaningId != null ? this.meaningId.hashCode() : 0);
            hash = 59 * hash + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
            return hash;
        }


    }
}
