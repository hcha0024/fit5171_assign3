package rockets.mining;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.conversions.BigDecimalConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rockets.dataaccess.DAO;
import rockets.dataaccess.neo4j.Neo4jDAO;
import rockets.model.Launch;
import rockets.model.LaunchServiceProvider;
import rockets.model.Rocket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RocketMinerUnitTest {
    Logger logger = LoggerFactory.getLogger(RocketMinerUnitTest.class);

    private DAO dao;
    private RocketMiner miner;
    private List<Rocket> rockets;
    private List<LaunchServiceProvider> lsps;
    private List<Launch> launches;

    @BeforeEach
    public void setUp() {
        dao = mock(Neo4jDAO.class);
        miner = new RocketMiner(dao);
        rockets = Lists.newArrayList();

        lsps = Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),
                new LaunchServiceProvider("SpaceX", 2002, "USA"),
                new LaunchServiceProvider("ESA", 1975, "Europe")
        );

        // index of lsp of each rocket
        int[] lspIndex = new int[]{0, 1, 2, 2, 1};

        // 5 rockets
        for (int i = 0; i < 5; i++) {
            rockets.add(new Rocket("rocket_" + i, "USA", lsps.get(lspIndex[i])));
        }
        // month of each launch
        int[] months = new int[]{1, 6, 4, 3, 4, 11, 6, 5, 12, 5};

        // index of rocket of each launch
        int[] rocketIndex = new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 3};

        String[] orbits = new String[]{"Low Earth Orbit", "Medium Earth Orbit","High Earth Orbit","Geo-Synchronous Orbit", "Low Earth Orbit", "Medium Earth Orbit","High Earth Orbit","Geo-Synchronous Orbit","Low Earth Orbit", "Medium Earth Orbit" };

        // LaunchOutcome
        Launch.LaunchOutcome[] launchOutcomes =  new Launch.LaunchOutcome[]{Launch.LaunchOutcome.SUCCESSFUL, Launch.LaunchOutcome.SUCCESSFUL, Launch.LaunchOutcome.SUCCESSFUL, Launch.LaunchOutcome.SUCCESSFUL, Launch.LaunchOutcome.SUCCESSFUL, Launch.LaunchOutcome.FAILED, Launch.LaunchOutcome.FAILED, Launch.LaunchOutcome.FAILED, Launch.LaunchOutcome.FAILED, Launch.LaunchOutcome.FAILED  };

        // price of each launch
        int[] price = new int[]{999999999, 99999999, 9999999, 999999, 99999, 9999, 999, 99, 9, 1};


        // 10 launches
        launches = IntStream.range(0, 10).mapToObj(i -> {
            logger.info("create " + i + " launch in month: " + months[i]);
            Launch l = new Launch();
            l.setLaunchDate(LocalDate.of(2017, months[i], 1));
            l.setLaunchVehicle(rockets.get(rocketIndex[i]));
            l.setLaunchSite("VAFB");
            l.setOrbit("LEO");
            l.setLaunchServiceProvider(l.getLaunchVehicle().getManufacturer());
            l.setLaunchOutcome(launchOutcomes[i]);
            l.setPrice(BigDecimal.valueOf(price[i]));
            l.setOrbit(orbits[i]);
            spy(l);
            return l;
        }).collect(Collectors.toList());

        rockets.get(0).setLaunches(Sets.newHashSet(launches.subList(0, 4)));
        rockets.get(1).setLaunches(Sets.newHashSet(launches.subList(4, 7)));
        rockets.get(2).setLaunches(Sets.newHashSet(launches.subList(7, 9)));
        rockets.get(3).setLaunches(Sets.newHashSet(launches.get(9)));
        }

    //MOST RECENT LAUNCHES

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnTopMostRecentLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate()));
        List<Launch> loadedLaunches = miner.mostRecentLaunches(k);
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -100, 20})
    public void shouldThrowIllegalArgumentExceptionTopMostRecentLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> -a.getLaunchDate().compareTo(b.getLaunchDate()));
        assertThrows(IllegalArgumentException.class, () -> {List<Launch> loadedLaunches = miner.mostRecentLaunches(k);
            ;});

    }

    //MOST RELIABLE LAUNCH SERVICE PROVIDERS

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnMostReliableLaunchServiceProviders(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),  // 100%
                new LaunchServiceProvider("SpaceX", 2002, "USA"), // 33%
                new LaunchServiceProvider("ESA", 1975, "Europe") // 0%
        );
        List<LaunchServiceProvider> lspl = miner.mostReliableLaunchServiceProviders(k);
        //System.out.println(lspl.toString());
        assertEquals(k, lspl.size());
        assertEquals(resultList.subList(0, k), lspl);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -100 , 10 , 20})
    public void shouldThrowIllegalArgumentExceptionMostReliableLaunchServiceProviders(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),  // 100%
                new LaunchServiceProvider("SpaceX", 2002, "USA"), // 33%
                new LaunchServiceProvider("ESA", 1975, "Europe") // 0%
        );

        assertThrows(IllegalArgumentException.class, () -> {List<LaunchServiceProvider> lspl = miner.mostReliableLaunchServiceProviders(k);});
    }


    //MOST LAUNCHED ROCKETS

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, 50, 100})
    public void shouldThrowIllegalArgumentExceptionReturnMostLaunchedRockets(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Rocket> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                rockets.get(0),
                rockets.get(1)
        );

        assertThrows(IllegalArgumentException.class, () -> {List<Rocket> rockets = miner.mostLaunchedRockets(k);});


    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldReturnMostLaunchedRockets(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Rocket> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
               rockets.get(0),
                rockets.get(1)
        );
        List<Rocket> rockets = miner.mostLaunchedRockets(k);
        //System.out.println(rockets.toString());
        assertEquals(k, rockets.size());
        assertEquals(resultList.subList(0, k), rockets);

    }



    //MOST EXPENSIVE LAUNCHES


    @ParameterizedTest
    @ValueSource(ints = {-1, -2, 20, 100})
    public void shouldThrowIllegalArgumentExceptionMostExpensiveLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        assertThrows(IllegalArgumentException.class, () -> {List<Launch> loadedLaunches = miner.mostExpensiveLaunches(k);
            ;});


    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldReturnMostExpensiveLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches.sort((a, b) -> - a.getPrice().compareTo(b.getPrice()));
        List<Launch> loadedLaunches = miner.mostExpensiveLaunches(k);
        System.out.println(sortedLaunches.toString());
        System.out.println(loadedLaunches.toString());
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }


    //DOMINANT COUNTRIES

    @ParameterizedTest
    @ValueSource(strings = { "abcddcd", "random"})
    public void shouldReturnNoRocketDominantCountry(String orbit) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        assertEquals("No Rocket in this Orbit", miner.dominantCountry(orbit));


    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    public void shouldThrowExceptionWhenOrbitIsEmpty(String orbit) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> miner.dominantCountry(orbit));
        assertEquals("Cannot be Blank", exception.getMessage());
    }


    @ParameterizedTest
    @CsvSource({"Low Earth Orbit, USA", "Medium Earth Orbit, USA","High Earth Orbit, USA","Geo-Synchronous Orbit, USA"})
    //@ValueSource(strings = {"Low Earth Orbit", "Medium Earth Orbit","High Earth Orbit","Geo-Synchronous Orbit"})
    public void shouldReturnMostDominantCountry(String orbit, String results) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        String country = miner.dominantCountry(orbit);
        assertEquals(results, country);
    }



    //HIGHEST REVENUES LAUNCH SERVICE PROVIDERS

    @ParameterizedTest
    @ValueSource(ints = {1,2,3})
    public void shouldReturnHighestRevenueLaunchServiceProviders(int k){
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                new LaunchServiceProvider("ULA", 1990, "USA"),  // 1
                new LaunchServiceProvider("SpaceX", 2002, "USA"), // 2
                new LaunchServiceProvider("ESA", 1975, "Europe") // 3
        );
        List<LaunchServiceProvider> lspl = miner.highestRevenueLaunchServiceProviders(k, 2017);
        assertEquals(k,lspl.size());
        assertEquals(resultList.subList(0,k),lspl);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1,-100,-1000, 10 , 20})
    public void shouldThrowIllegalArgumentExceptionHighestRevenueLaunchServiceProvider(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        assertThrows(IllegalArgumentException.class, () -> {
            List<LaunchServiceProvider> lspl = miner.highestRevenueLaunchServiceProviders(k, 2017);
        });
    }



    //MOST RECENT FAILED LAUNCHES

    @ParameterizedTest
    @ValueSource(ints = {-2, -1, 10 , 20})
    public void shouldThrowIllegalArgumentExceptionMostRecentFailedLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedFailedLaunches = new ArrayList<>(launches);
        sortedFailedLaunches.sort(Comparator.comparing(Launch::getLaunchDate));
        assertThrows(IllegalArgumentException.class, () -> {
            List<Launch> failedLaunches = miner.mostRecentFailedLaunches(k);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnMostRecentFailedLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> minerFailedLaunches = miner.mostRecentFailedLaunches(k);
        List<Launch> sortedFailedLaunches = new ArrayList<>(launches);
        Comparator<Launch> launchComparator = (a, b) -> a.getLaunchDate().compareTo(b.getLaunchDate());
        sortedFailedLaunches.removeIf(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.SUCCESSFUL));
        sortedFailedLaunches.sort(launchComparator.reversed());
        sortedFailedLaunches = sortedFailedLaunches.subList(0, k);

        assertEquals(k, minerFailedLaunches.size());
        assertEquals(sortedFailedLaunches, minerFailedLaunches);
    }


    //LEAST RELIABLE SERVICE PROVIDERS
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    public void shouldReturnLeastReliableLaunchServiceProviders(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                new LaunchServiceProvider("ESA", 1975, "Europe"), // 0%
                new LaunchServiceProvider("SpaceX", 2002, "USA"), // 33%
                new LaunchServiceProvider("ULA", 1990, "USA")  // 100%
        );

        List<LaunchServiceProvider> lspl = miner.leastReliableLaunchServiceProviders(k);
        //System.out.println(lspl.toString());
        assertEquals(k, lspl.size());
        assertEquals(resultList.subList(0, k), lspl);

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -100 , 10 , 20})
    public void shouldThrowIllegalArgumentExceptionLeastReliableLaunchServiceProviders(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<LaunchServiceProvider> resultList = new ArrayList<>();
        resultList =  Arrays.asList(
                new LaunchServiceProvider("ESA", 1975, "Europe"), // 0%
                new LaunchServiceProvider("SpaceX", 2002, "USA"), // 33%
                new LaunchServiceProvider("ULA", 1990, "USA")  // 100%
        );
        assertThrows(IllegalArgumentException.class, () -> {List<LaunchServiceProvider> lspl = miner.leastReliableLaunchServiceProviders(k);});
    }


    //MOST FAILED EXPENSIVE LAUNCHES


    @ParameterizedTest
    @ValueSource(ints = {-1, -2, 20, 100})
    public void shouldThrowIllegalArgumentExceptionMostExpensiveFailedLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        //List<Launch> sortedLaunches = new ArrayList<>(launches);
        //sortedLaunches.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        assertThrows(IllegalArgumentException.class, () -> {List<Launch> loadedLaunches = miner.mostExpensiveFailedLaunches(k);
            ;});


    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldReturnMostExpensiveFailedLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches = sortedLaunches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.FAILED))
                .collect(Collectors.toList());
        sortedLaunches.sort((a, b) -> - a.getPrice().compareTo(b.getPrice()));


        List<Launch> loadedLaunches = miner.mostExpensiveFailedLaunches(k);

        System.out.println(sortedLaunches.toString());
        System.out.println(loadedLaunches.toString());
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);


    }

    //CHEAPEST LAUNCHES


    @ParameterizedTest
    @ValueSource(ints = {-1, -2, 20, 100})
    public void shouldThrowIllegalArgumentExceptionMostAffordableLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        //ist<Launch> sortedLaunches = new ArrayList<>(launches);
        //sortedLaunches.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        assertThrows(IllegalArgumentException.class, () -> {List<Launch> loadedLaunches = miner.mostAffordableLaunches(k);
            ;});


    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void shouldReturnMostAffordableLaunches(int k) {
        when(dao.loadAll(Launch.class)).thenReturn(launches);
        List<Launch> sortedLaunches = new ArrayList<>(launches);
        sortedLaunches = sortedLaunches.stream()
                .filter(launch -> launch.getLaunchOutcome().equals(Launch.LaunchOutcome.SUCCESSFUL))
                .collect(Collectors.toList());
        sortedLaunches.sort((a, b) ->  a.getPrice().compareTo(b.getPrice()));

        List<Launch> loadedLaunches = miner.mostAffordableLaunches(k);
        System.out.println(sortedLaunches.toString());
        System.out.println(loadedLaunches.toString());
        assertEquals(k, loadedLaunches.size());
        assertEquals(sortedLaunches.subList(0, k), loadedLaunches);
    }



}