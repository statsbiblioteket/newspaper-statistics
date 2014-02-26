package dk.statsbiblioteket.medieplatform.autonomous.iterator.statistics.model;

import java.util.Map;
import java.util.TreeMap;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.statistics.writer.StatisticWriter;

/**
 * Models the collected statistics for this collector.
 */
public class Statistics {
    protected final Map<StatisticsKey, Long> countMap = new TreeMap();
    protected final Map<StatisticsKey, WeightedMean> relativeCountMap = new TreeMap();
    protected final Map<StatisticsKey, Statistics> substatisticsMap = new TreeMap();

    /**
     * Adds a measurement to the current.
     * @param key The name of the measurement.
     * @param countToAdd The count for the measurement.
     */
    public void addCount(StatisticsKey key, Long countToAdd) {
        Long currentCount = 0L;
        if (countMap.containsKey(key)) {
            currentCount = countMap.get(key);
        }
        countMap.put(key, currentCount + countToAdd);
    }

    /**
     * Adds a weighted statistics. The numbers added here is added to the current
     * metric with the indicated name together with is weight.
     * <p>
     * Example: Node1 has OCR for 3 out of 5 words, Node2 has OCR for 45 out of 121 words.
     * This gives a cumulated OCR successrate of 48/126.
     * </p>
     * @param key The name for the the measurement.
     * @param countToAdd The relative count for the measurement.
     */
    public void addRelative(StatisticsKey key, WeightedMean countToAdd) {
        WeightedMean currentCount;
        if (relativeCountMap.containsKey(key)) {
            currentCount = relativeCountMap.get(key);
            relativeCountMap.put(key,
                   currentCount.add(countToAdd));
        } else {
            relativeCountMap.put(key, countToAdd);
        }
    }

    /**
     * Adds one statistics object to this statistics. This means each named measurement is added to this statistics
     * object.
     * <p>
     *     Used for adding child statistics to parent statistics.
     * </p>
     * @param statistics The statistics to add to this collector
     */
    public void addStatistic(Statistics statistics) {
        for (Map.Entry<StatisticsKey, Long> measurement : statistics.countMap.entrySet()) {
            addCount(measurement.getKey(), measurement.getValue());
        }
        for (Map.Entry<StatisticsKey, WeightedMean> measurement : statistics.relativeCountMap.entrySet()) {
            addRelative(measurement.getKey(), measurement.getValue());
        }
        for (Map.Entry<StatisticsKey, Statistics> measurement : statistics.substatisticsMap.entrySet()) {
            addSubstatistic(measurement.getKey(), measurement.getValue());
        }
    }

    /**
     * @return A summary of the statistics for this instance. The default implementation returns null, but subclasses
     * may implement a more informative summary.
     */
    public String getSummary() {
        return null;
    }

    /**
     * Adds one statistics object to this statistics as a sub statistics.
     * @param statisticsToAdd The statistics to add to this collector
     */
    public void addSubstatistic(StatisticsKey key, Statistics statisticsToAdd) {
        if (substatisticsMap.containsKey(key)) {
            substatisticsMap.get(key).addStatistic(statisticsToAdd);
        } else {
            substatisticsMap.put(key, statisticsToAdd);
        }
    }

    public void writeStatistics(StatisticWriter writer) {
        for (Map.Entry<StatisticsKey, Long> measurement : countMap.entrySet()) {
            writer.addStatistic(measurement.getKey(), measurement.getValue());
        }

        for (Map.Entry<StatisticsKey, WeightedMean> measurement : relativeCountMap.entrySet()) {
            writer.addStatistic(measurement.getKey(), measurement.getValue());
        }

        for (Map.Entry<StatisticsKey, Statistics> measurement : substatisticsMap.entrySet()) {
            writer.addNode(measurement.getKey().getType(), measurement.getValue().getSummary());
            measurement.getValue().writeStatistics(writer);
            writer.endNode();
        }
    }

}
