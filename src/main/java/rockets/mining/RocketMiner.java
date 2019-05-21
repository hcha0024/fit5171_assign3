package rockets.mining;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;


public class RocketMiner {
    private static Logger logger = LoggerFactory.getLogger(RocketMiner.class);

    private DAO dao;


    public RocketMiner(DAO dao) {
        this.dao = dao;
    }

    /**
     * Generic
     * <p>
     * Returns top-k highest key value in a map
     * @param m A map between an abitrary key and a comparable value
     * @param k    the number
     * @return the list of top k keys in increasing order
     */
    private <M extends Map<K, V>, V extends Comparable<V>, K> List<K> sortMapInDecreasingOrder(M m, int k) {
        return m.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    /**
     * Returns the top-k most active rockets, as measured by number of completed launches.
     *
     * @param k the number of rockets to be returned.
     * @return the list of k most active rockets.
     *
     *
     */
    public List<Rocket> mostLaunchedRockets(int k) {
        logger.info(String.format("Find most active %d rockets" , k));

        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<Rocket, Long> launchRate = launches.stream()
                .filter(launch -> launch.getLaunchOutcome() == Launch.LaunchOutcome.SUCCESSFUL)
                .collect(Collectors.groupingBy(Launch::getLaunchVehicle, Collectors.counting())
                );

        if (k < 0 || launchRate.size() < k) {
            throw new IllegalArgumentException("Less rockets retrieved than requested");
        }
        return sortMapInDecreasingOrder(launchRate, k);
    }

    /**
     * Implemented & tested!
     * <p>
     * Returns the top-k most reliable launch service providers as measured
     * by percentage of successful launches.
     *
     * @param k the number of launch service providers to be returned.
     * @return the list of k most reliable ones.
     */
    public List<LaunchServiceProvider> mostReliableLaunchServiceProviders(int k) {
        logger.info(String.format("Find most reliable %d launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);

        Map<LaunchServiceProvider, Double> launchRate = launches.stream()
                .collect(Collectors.groupingBy(Launch::getLaunchServiceProvider,
                        Collectors.mapping(Launch::getLaunchOutcome,
                                Collectors.averagingDouble(successful -> Launch.LaunchOutcome.SUCCESSFUL.equals(successful) ? 1 : 0)
                        )
                        )
                );
        if (k < 0 || launchRate.size() < k) {
            throw new IllegalArgumentException("Less launch service providers retrieved than requested");
        }
        return sortMapInDecreasingOrder(launchRate, k);

    }

    /**
     * <p>
     * Returns the top-k most recent launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most recent launches.
     */
    public List<Launch> mostRecentLaunches(int k) {
        logger.info(String.format("find most recent %d launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchDateComparator = (a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate());

        List<Launch> results = launches.stream()
                .sorted(launchDateComparator)
                .limit(k)
                .collect(Collectors.toList());

        if (k < 0 || results.size() < k) {
            throw new IllegalArgumentException("Less launches retrieved than requested");
        }
        return results;
    }

    /**
     * Implemented & tested!
     * <p>
     * Returns the dominant country who has the most launched rockets in an orbit.
     *
     * @param orbit the orbit
     * @return the country who sends the most rockets to the orbit
     */

    public String dominantCountry(String orbit) {
        logger.info(String.format("find most dominant country in orbit  %s" ,orbit));
        notBlank(orbit, "Cannot be Blank");
        notNull(orbit,"Cannot be Null");

        Collection<Launch> launches = dao.loadAll(Launch.class);
        Map<LaunchServiceProvider, Long> launchRate = launches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.SUCCESSFUL))
                .filter(launch -> launch.getOrbit().equals(orbit))
                .collect(Collectors.groupingBy(Launch::getLaunchServiceProvider, Collectors.counting()));

        Map<String, Long> rocketPerCountry = launchRate.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getCountry(),
                        Collectors.summingLong(Map.Entry::getValue)
                ));

        if (sortMapInDecreasingOrder(rocketPerCountry, 1).isEmpty()) {
            return "No Rocket in this Orbit";
        } else {
            return sortMapInDecreasingOrder(rocketPerCountry, 1).get(0);
        }
    }
    /**
     * Implemented & tested!
     * <p>
     * Returns the top-k most expensive launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most expensive launches.
     */
    public List<Launch> mostExpensiveLaunches(int k) {
        logger.info(String.format("find most expensive %d launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchPriceComparator = (a, b) -> a.getPrice().compareTo(b.getPrice());
        List<Launch> results = launches.stream()
                .sorted(Collections.reverseOrder(launchPriceComparator))
                .limit(k)
                .collect(Collectors.toList());

        if (k < 0 || results.size() < k) {
            throw new IllegalArgumentException("Less launches retrieved than requested");
        }
        return results;
    }

    /**
     * Implemented & tested!
     * <p>
     * Returns a list of launch service provider that has the top-k highest
     * sales revenue in a year.
     *
     * @param k    the number of launch service provider.
     * @param year the year in request
     * @return the list of k launch service providers who has the highest sales revenue.
     */
    public List<LaunchServiceProvider> highestRevenueLaunchServiceProviders(int k, int year) {
        logger.info(String.format("find most expensive %d launches", k));
        Collection<Launch> launches = dao.loadAll(Launch.class);

        Map<LaunchServiceProvider, BigDecimal> lspl = launches.stream()
                .filter(launch -> launch.getLaunchDate().getYear() == year)
                .collect(Collectors.groupingBy(Launch::getLaunchServiceProvider, Collectors.reducing(BigDecimal.ZERO , Launch::getPrice, BigDecimal::add)
                ));
        if (k < 0 || lspl.size() < k) {
            throw new IllegalArgumentException("Less launch service providers retrieved than requested");
        }
        return sortMapInDecreasingOrder(lspl, k);
    }

    // EXTENSION: ADDITIONAL FUNCTIONALITY
    /**
     * Implemented & tested!
     * <p>
     * Returns a list of k most recent failed launches
     * @param k    the number of launch.
     * @return the list of k most recent failed launches.
     */
    public List<Launch> mostRecentFailedLaunches(int k) {
        logger.info(String.format( "find %d most recent failed launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchComparator = (a, b) -> a.getLaunchDate().compareTo(b.getLaunchDate());

        List<Launch> result = launches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.FAILED))
                .sorted(launchComparator.reversed())
                .collect(Collectors.toList());

        if (k < 0 || result.size() < k) {
            throw new IllegalArgumentException("Less launches retrieved than requested");
        }
        return result.subList(0, k);
    }


    /**
     * 
     * <p>
     * Returns the top-k least reliable launch service providers as measured
     * by percentage of successful launches.
     *
     * @param k    the number of launch service provider.
     * @return the list of k launch service providers who are the least reliable
     */
    public List<LaunchServiceProvider> leastReliableLaunchServiceProviders(int k) {
        logger.info(String.format("find least reliable %d launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);

        Map<LaunchServiceProvider, Double> launchRate = launches.stream()
                .collect(Collectors.groupingBy(Launch::getLaunchServiceProvider,
                        Collectors.mapping(Launch::getLaunchOutcome,
                                Collectors.averagingDouble(failed -> Launch.LaunchOutcome.FAILED.equals(failed) ? 1 : 0)
                        )
                        )
                );
        if (k < 0 || launchRate.size() < k) {
            throw new IllegalArgumentException("Less launch service providers retrieved than requested");
        }
        return sortMapInDecreasingOrder(launchRate, k);
    }


    /**
     *
     * <p>
     * Returns the top-k most expensive failed launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k most expensive failed launches.
     */
    public List<Launch> mostExpensiveFailedLaunches(int k) {
        logger.info(String.format("find %d most expensive failed launches", k));
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchPriceComparator = (a, b) -> a.getPrice().compareTo(b.getPrice());

        List<Launch> result = launches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.FAILED))
                .sorted(Collections.reverseOrder(launchPriceComparator))
                .limit(k)
                .collect(Collectors.toList());

        if (k < 0 || result.size() < k) {
            throw new IllegalArgumentException("Less launches retrieved than requested");
        }
        return result;
    }


    /**
     *
     * <p>
     * Returns the top-k cheapest launches.
     *
     * @param k the number of launches to be returned.
     * @return the list of k cheapest launches.
     */
    public List<Launch> mostAffordableLaunches(int k) {
        logger.info(String.format("find %d most affordable successful launches" , k));
        Collection<Launch> launches = dao.loadAll(Launch.class);
        Comparator<Launch> launchPriceComparator = (a, b) -> a.getPrice().compareTo(b.getPrice());
        List<Launch> result = launches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.SUCCESSFUL))
                .sorted(launchPriceComparator)
                .limit(k)
                .collect(Collectors.toList());
        if (k < 0 || result.size() < k) {
            throw new IllegalArgumentException("Less launches retrieved than requested");
        }
        return result;
    }
}
