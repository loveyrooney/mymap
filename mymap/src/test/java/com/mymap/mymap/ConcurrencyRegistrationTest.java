package com.mymap.mymap;

import com.mymap.domain.clusters.dto.FilteredBusDTO;
import com.mymap.domain.clusters.dto.JourneyDTO;
import com.mymap.domain.clusters.dto.MarkerClusterDTO;
import com.mymap.domain.clusters.service.BusFilterService;
import com.mymap.domain.clusters.service.ClustersService;
import com.mymap.domain.clusters.repository.MarkerClusterRepository;
import com.mymap.domain.clusters.repository.FilteredBusRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyRegistrationTest {

    @Autowired
    private ClustersService clustersService;

    @Autowired
    private BusFilterService busFilterService;

    @Autowired
    private MarkerClusterRepository markerClusterRepository;
    
    @Autowired
    private FilteredBusRepository filteredBusRepository;

    private Long testJourneyNo;

    @BeforeEach
    public void setup() {
        // Create a clean test journey
        JourneyDTO dto = JourneyDTO.builder()
                .fromName("TestStart").toName("TestEnd")
                .fromBus(new String[]{"13550"})
                .tfBus(new String[]{"01126"})
                .toBus(new String[]{"03144"})
                .userNo(2L)
                .build();
        
        testJourneyNo = clustersService.createJourney(dto);
        
        // Ensure no leftover clusters/buses for this journey
        markerClusterRepository.deleteAllByJourneyNo(testJourneyNo);
        filteredBusRepository.deleteAllByJourneyNo(testJourneyNo);
    }

    @AfterEach
    public void teardown() {
        if (testJourneyNo != null) {
            clustersService.deleteMarkerCluster(testJourneyNo);
            clustersService.deleteFilteredBus(testJourneyNo);
            clustersService.deleteJourneyByNo(testJourneyNo);
        }
    }

    @Test
    public void testConcurrentAbstractCluster() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Integer> resultsSizes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    // Use the fresh journey ID
                    List<MarkerClusterDTO> results = clustersService.abstractCluster(testJourneyNo);
                    resultsSizes.add(results.size());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();
        executorService.shutdown();

        System.out.println("Results sizes: " + resultsSizes);
        if (!resultsSizes.isEmpty()) {
            int firstSize = resultsSizes.get(0);
            for (int size : resultsSizes) {
                assertEquals(firstSize, size, "Data interference detected in abstractCluster!");
            }
        }
    }

    @Test
    public void testRouteCase2BusFilter() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Integer> resultsSizes = Collections.synchronizedList(new ArrayList<>());
        
        JourneyDTO journey = JourneyDTO.builder()
                .no(testJourneyNo)
                .fromBus(new String[]{"13550"})
                .tfBus(new String[]{"01126"})
                .toBus(new String[]{"03144"})
                .build();
        
        // We need clusters to exist for runBusFilter to work (it calls findByArsId internally)
        List<MarkerClusterDTO> clusters = clustersService.abstractCluster(testJourneyNo);
        clustersService.createMarkerCluster(clusters);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    List<FilteredBusDTO> results = busFilterService.runBusFilter(journey, 2);
                    resultsSizes.add(results.size());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();
        executorService.shutdown();

        System.out.println("RouteCase 2 Bus Filter Results sizes: " + resultsSizes);
        if (!resultsSizes.isEmpty()) {
            int firstSize = resultsSizes.get(0);
            for (int size : resultsSizes) {
                assertEquals(firstSize, size, "Data interference detected in runBusFilter!");
            }
        }
    }

    @Test
    public void testDirectBusFilter() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Integer> resultsSizes = Collections.synchronizedList(new ArrayList<>());
        
        // Define a direct journey (no tfBus)
        JourneyDTO journey = JourneyDTO.builder()
                .no(testJourneyNo)
                .fromBus(new String[]{"13550"})
                .tfBus(null) // Transfer bus is null
                .toBus(new String[]{"03144"})
                .build();
        
        // Cluster for direct path
        List<MarkerClusterDTO> clusters = clustersService.abstractCluster(testJourneyNo);
        clustersService.createMarkerCluster(clusters);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    // routeCase 1 is direct
                    List<FilteredBusDTO> results = busFilterService.runBusFilter(journey, 1);
                    resultsSizes.add(results.size());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();
        executorService.shutdown();

        System.out.println("Direct(RouteCase 1) Bus Filter Results sizes: " + resultsSizes);
        if (!resultsSizes.isEmpty()) {
            int firstSize = resultsSizes.get(0);
            for (int size : resultsSizes) {
                assertEquals(firstSize, size, "Data interference detected in Direct(RouteCase 1) runBusFilter!");
            }
        }
    }

    @Test
    public void testRouteCase3BusFilter() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Integer> resultsSizes = Collections.synchronizedList(new ArrayList<>());
        
        // Define a journey for routeCase 3 (Transfer -> Arrival)
        JourneyDTO journey = JourneyDTO.builder()
                .no(testJourneyNo)
                .fromBus(null) 
                .tfBus(new String[]{"01126"})
                .toBus(new String[]{"03144"})
                .build();
        
        List<MarkerClusterDTO> clusters = clustersService.abstractCluster(testJourneyNo);
        clustersService.createMarkerCluster(clusters);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    List<FilteredBusDTO> results = busFilterService.runBusFilter(journey, 3);
                    resultsSizes.add(results.size());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();
        executorService.shutdown();

        System.out.println("RouteCase 3 Bus Filter Results sizes: " + resultsSizes);
        if (!resultsSizes.isEmpty()) {
            int firstSize = resultsSizes.get(0);
            for (int size : resultsSizes) {
                assertEquals(firstSize, size, "Data interference detected in RouteCase 3 runBusFilter!");
            }
        }
    }

    @Test
    public void testRouteCase4BusFilter() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        List<Integer> resultsSizes = Collections.synchronizedList(new ArrayList<>());
        
        // Define a full journey for routeCase 4 (From -> TF -> To)
        JourneyDTO journey = JourneyDTO.builder()
                .no(testJourneyNo)
                .fromBus(new String[]{"13550"})
                .tfBus(new String[]{"01126"})
                .toBus(new String[]{"03144"})
                .build();
        
        List<MarkerClusterDTO> clusters = clustersService.abstractCluster(testJourneyNo);
        clustersService.createMarkerCluster(clusters);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    List<FilteredBusDTO> results = busFilterService.runBusFilter(journey, 4);
                    resultsSizes.add(results.size());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        latch.countDown();
        finishLatch.await();
        executorService.shutdown();

        System.out.println("RouteCase 4 Bus Filter Results sizes: " + resultsSizes);
        if (!resultsSizes.isEmpty()) {
            int firstSize = resultsSizes.get(0);
            for (int size : resultsSizes) {
                assertEquals(firstSize, size, "Data interference detected in RouteCase 4 runBusFilter!");
            }
        }
    }
}
