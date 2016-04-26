/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.test.MockFloodlightProvider;
import net.floodlightcontroller.core.test.MockSwitchManager;
import net.floodlightcontroller.core.test.MockThreadPoolService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.debugcounter.MockDebugCounterService;
import net.floodlightcontroller.debugevent.IDebugEventService;
import net.floodlightcontroller.debugevent.MockDebugEventService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.routing.RouteId;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.topology.TopologyInstance;
import net.floodlightcontroller.topology.TopologyManager;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyInstanceTest {
    protected static Logger log = LoggerFactory.getLogger(TopologyInstanceTest.class);
    protected TopologyManager topologyManager;
    protected FloodlightModuleContext fmc;
    protected ILinkDiscoveryService linkDiscovery;
    protected MockFloodlightProvider mockFloodlightProvider;

    protected int DIRECT_LINK = 1;
    protected int MULTIHOP_LINK = 2;
    protected int TUNNEL_LINK = 3;

    @Before 
    public void SetUp() throws Exception {
        fmc = new FloodlightModuleContext();
        linkDiscovery = EasyMock.createMock(ILinkDiscoveryService.class);
        mockFloodlightProvider = new MockFloodlightProvider();
        fmc.addService(IFloodlightProviderService.class, mockFloodlightProvider);
        fmc.addService(IOFSwitchService.class, new MockSwitchManager());
        fmc.addService(ILinkDiscoveryService.class, linkDiscovery);
        fmc.addService(IDebugCounterService.class, new MockDebugCounterService());
        fmc.addService(IDebugEventService.class, new MockDebugEventService());
        MockThreadPoolService tp = new MockThreadPoolService();
        topologyManager = new TopologyManager();
        fmc.addService(IThreadPoolService.class, tp);
        topologyManager.init(fmc);
        tp.init(fmc);
        tp.startUp(fmc);
    }

    protected void verifyClusters(int[][] clusters) {
        verifyClusters(clusters, true);
    }

    protected void verifyClusters(int[][] clusters, boolean tunnelsEnabled) {
        List<DatapathId> verifiedSwitches = new ArrayList<DatapathId>();

        // Make sure the expected cluster arrays are sorted so we can
        // use binarySearch to test for membership
        for (int i = 0; i < clusters.length; i++)
            Arrays.sort(clusters[i]);

        TopologyInstance ti = 
                topologyManager.getCurrentInstance(tunnelsEnabled);
        Set<DatapathId> switches = ti.getSwitches();

        for (DatapathId sw: switches) {
            if (!verifiedSwitches.contains(sw)) {

                int[] expectedCluster = null;

                for (int j = 0; j < clusters.length; j++) {
                    if (Arrays.binarySearch(clusters[j], (int)sw.getLong()) >= 0) {
                        expectedCluster = clusters[j];
                        break;
                    }
                }
                if (expectedCluster != null) {
                    Set<DatapathId> cluster = ti.getSwitchesInOpenflowDomain(sw);
                    assertEquals(expectedCluster.length, cluster.size());
                    for (DatapathId sw2: cluster) {
                        assertTrue(Arrays.binarySearch(expectedCluster, (int)sw2.getLong()) >= 0);
                        verifiedSwitches.add(sw2);
                    }
                }
            }
        }
    }

    protected void 
    verifyExpectedBroadcastPortsInClusters(int [][][] ebp) {
        verifyExpectedBroadcastPortsInClusters(ebp, true);
    }

    protected void 
    verifyExpectedBroadcastPortsInClusters(int [][][] ebp, 
                                           boolean tunnelsEnabled) {
        NodePortTuple npt = null;
        Set<NodePortTuple> expected = new HashSet<NodePortTuple>();
        for(int i=0; i<ebp.length; ++i) {
            int [][] nptList = ebp[i];
            expected.clear();
            for(int j=0; j<nptList.length; ++j) {
                npt = new NodePortTuple(DatapathId.of(nptList[j][0]), OFPort.of(nptList[j][1]));
                expected.add(npt);
            }
            TopologyInstance ti = topologyManager.getCurrentInstance(tunnelsEnabled);
            Set<NodePortTuple> computed = ti.getBroadcastNodePortsInCluster(npt.nodeId);
            log.info("computed: {}", computed);
            if (computed != null)
                assertTrue(computed.equals(expected));
            else if (computed == null)
                assertTrue(expected.isEmpty());
        }
    }

    public void createTopologyFromLinks(int [][] linkArray) throws Exception {
        ILinkDiscovery.LinkType type = ILinkDiscovery.LinkType.DIRECT_LINK;

        // Use topologymanager to write this test, it will make it a lot easier.
        for (int i = 0; i < linkArray.length; i++) {
            int [] r = linkArray[i];
            if (r[4] == DIRECT_LINK)
                type= ILinkDiscovery.LinkType.DIRECT_LINK;
            else if (r[4] == MULTIHOP_LINK)
                type= ILinkDiscovery.LinkType.MULTIHOP_LINK;
            else if (r[4] == TUNNEL_LINK)
                type = ILinkDiscovery.LinkType.TUNNEL;

            topologyManager.addOrUpdateLink(DatapathId.of(r[0]), OFPort.of(r[1]), DatapathId.of(r[2]), OFPort.of(r[3]), U64.ZERO, type);
        }
        topologyManager.createNewInstance();
    }

    public void CaseyIsABoss(int [][] linkArray, int [] latency) throws Exception {
        ILinkDiscovery.LinkType type = ILinkDiscovery.LinkType.DIRECT_LINK;

        // Use topologymanager to write this test, it will make it a lot easier.
        for (int i = 0; i < linkArray.length; i++) {
            int [] r = linkArray[i];
            if (r[4] == DIRECT_LINK)
                type= ILinkDiscovery.LinkType.DIRECT_LINK;
            else if (r[4] == MULTIHOP_LINK)
                type= ILinkDiscovery.LinkType.MULTIHOP_LINK;
            else if (r[4] == TUNNEL_LINK)
                type = ILinkDiscovery.LinkType.TUNNEL;

            //Check for valid latency
            int lat = latency[i];
            if(lat < 0 || lat > 10000)
                lat = 10000;


            topologyManager.addOrUpdateLink(DatapathId.of(r[0]), OFPort.of(r[1]), DatapathId.of(r[2]), OFPort.of(r[3]), U64.of(lat), type);
        }
        topologyManager.createNewInstance();
    }

    public TopologyManager getTopologyManager() {
        return topologyManager;
    }

    @Test
    public void testClusters() throws Exception {
        TopologyManager tm = getTopologyManager();
        {
            int [][] linkArray = { 
                                  {1, 1, 2, 1, DIRECT_LINK}, 
                                  {2, 2, 3, 2, DIRECT_LINK},
                                  {3, 1, 1, 2, DIRECT_LINK},
                                  {2, 3, 4, 2, DIRECT_LINK},
                                  {3, 3, 4, 1, DIRECT_LINK}
            };
            int [][] expectedClusters = {
                                         {1,2,3}, 
                                         {4}
            };
            createTopologyFromLinks(linkArray);
            verifyClusters(expectedClusters);
        }

        {
            int [][] linkArray = { 
                                  {5, 3, 6, 1, DIRECT_LINK} 
            };
            int [][] expectedClusters = {
                                         {1,2,3}, 
                                         {4},
                                         {5},
                                         {6}
            };
            createTopologyFromLinks(linkArray);

            verifyClusters(expectedClusters);
        }

        {
            int [][] linkArray = { 
                                  {6, 1, 5, 3, DIRECT_LINK} 
            };
            int [][] expectedClusters = {
                                         {1,2,3}, 
                                         {4},
                                         {5,6}
            };
            createTopologyFromLinks(linkArray);

            verifyClusters(expectedClusters);
        }

        {
            int [][] linkArray = { 
                                  {4, 2, 2, 3, DIRECT_LINK} 
            };
            int [][] expectedClusters = {
                                         {1,2,3,4},
                                         {5,6}
            };
            createTopologyFromLinks(linkArray);

            verifyClusters(expectedClusters);
        }
        {
            int [][] linkArray = { 
                                  {4, 3, 5, 1, DIRECT_LINK} 
            };
            int [][] expectedClusters = {
                                         {1,2,3,4},
                                         {5,6}
            };
            createTopologyFromLinks(linkArray);

            verifyClusters(expectedClusters);
        }
        {
            int [][] linkArray = { 
                                  {5, 2, 2, 4, DIRECT_LINK} 
            };
            int [][] expectedClusters = {
                                         {1,2,3,4,5,6}
            };
            createTopologyFromLinks(linkArray);

            verifyClusters(expectedClusters);
        }

        //Test 2.
        {
            int [][] linkArray = { 
                                  {3, 2, 2, 2, DIRECT_LINK}, 
                                  {2, 1, 1, 1, DIRECT_LINK},
                                  {1, 2, 3, 1, DIRECT_LINK},
                                  {4, 1, 3, 3, DIRECT_LINK},
                                  {5, 1, 4, 3, DIRECT_LINK},
                                  {2, 4, 5, 2, DIRECT_LINK}
            };
            int [][] expectedClusters = {
                                         {1,2,3,4,5,6}
            };
            createTopologyFromLinks(linkArray);
            verifyClusters(expectedClusters);
        }

        // Test 3. Remove links
        {
            tm.removeLink(DatapathId.of(5), OFPort.of((short)3), DatapathId.of(6), OFPort.of((short)1));
            tm.removeLink(DatapathId.of(6), OFPort.of((short)1), DatapathId.of(5), OFPort.of((short)3));

            int [][] expectedClusters = {
                                         {1,2,3,4,5},
            };
            topologyManager.createNewInstance();
            verifyClusters(expectedClusters);
        }

        // Remove Switch
        {
            tm.removeSwitch(DatapathId.of(4));
            int [][] expectedClusters = {
                                         {1,2,3,5},
            };
            topologyManager.createNewInstance();
            verifyClusters(expectedClusters);
        }
    }

    @Test
    public void testLoopDetectionInSingleIsland() throws Exception {

        int [][] linkArray = {
                              {1, 1, 2, 1, DIRECT_LINK},
                              {2, 1, 1, 1, DIRECT_LINK},
                              {1, 2, 3, 1, DIRECT_LINK},
                              {3, 1, 1, 2, DIRECT_LINK},
                              {2, 2, 3, 2, DIRECT_LINK},
                              {3, 2, 2, 2, DIRECT_LINK},
                              {3, 3, 4, 1, DIRECT_LINK},
                              {4, 1, 3, 3, DIRECT_LINK},
                              {4, 2, 6, 2, DIRECT_LINK},
                              {6, 2, 4, 2, DIRECT_LINK},
                              {4, 3, 5, 1, DIRECT_LINK},
                              {5, 1, 4, 3, DIRECT_LINK},
                              {5, 2, 6, 1, DIRECT_LINK},
                              {6, 1, 5, 2, DIRECT_LINK},

        };
        int [][] expectedClusters = {
                                     {1, 2, 3, 4, 5, 6}
        };
        int [][][] expectedBroadcastPorts = {
                                             {{1,1}, {2,1}, {1,2}, {3,1}, {3,3}, {4,1}, {4,3}, {5,1}, {4,2}, {6,2}},
        };

        createTopologyFromLinks(linkArray);
        topologyManager.createNewInstance();
        verifyClusters(expectedClusters);
        verifyExpectedBroadcastPortsInClusters(expectedBroadcastPorts);
    }

    @Test
    public void testLoopDetectionWithIslands() throws Exception {

        //      +-------+             +-------+
        //      |       |   TUNNEL    |       |
        //      |   1  1|-------------|1  2   |
        //      |   2   |             |   2   |
        //      +-------+             +-------+
        //          |                     |
        //          |                     |
        //      +-------+                 |
        //      |   1   |                 |
        //      |   3  2|-----------------+
        //      |   3   |
        //      +-------+
        //
        //
        //      +-------+
        //      |   1   |   TUNNEL
        //      |   4  2|----------------+
        //      |   3   |                |
        //      +-------+                |
        //          |                    |
        //          |                    |
        //      +-------+             +-------+
        //      |   1   |             |   2   |
        //      |   5  2|-------------|1  6   |
        //      |       |             |       |
        //      +-------+             +-------+
        {
            int [][] linkArray = {
                                  {1, 1, 2, 1, DIRECT_LINK},
                                  {2, 1, 1, 1, DIRECT_LINK},
                                  {1, 2, 3, 1, DIRECT_LINK},
                                  {3, 1, 1, 2, DIRECT_LINK},
                                  {2, 2, 3, 2, DIRECT_LINK},
                                  {3, 2, 2, 2, DIRECT_LINK},

                                  {4, 2, 6, 2, DIRECT_LINK},
                                  {6, 2, 4, 2, DIRECT_LINK},
                                  {4, 3, 5, 1, DIRECT_LINK},
                                  {5, 1, 4, 3, DIRECT_LINK},
                                  {5, 2, 6, 1, DIRECT_LINK},
                                  {6, 1, 5, 2, DIRECT_LINK},

            };

            int [][] expectedClusters = {
                                         {1, 2, 3}, 
                                         {4, 5, 6}
            };
            int [][][] expectedBroadcastPorts = {
                                                 {{1,1}, {2,1}, {1,2}, {3,1}},
                                                 {{4,3}, {5,1}, {4,2}, {6,2}},
            };

            createTopologyFromLinks(linkArray);
            topologyManager.createNewInstance();
            verifyClusters(expectedClusters);
            verifyExpectedBroadcastPortsInClusters(expectedBroadcastPorts);
        }

        //      +-------+             +-------+
        //      |       |    TUNNEL   |       |
        //      |   1  1|-------------|1  2   |
        //      |   2   |             |   2   |
        //      +-------+             +-------+
        //          |                     |
        //          |                     |
        //      +-------+                 |
        //      |   1   |                 |
        //      |   3  2|-----------------+
        //      |   3   |
        //      +-------+
        //          | 
        //          |   TUNNEL
        //          |
        //      +-------+
        //      |   1   |    TUNNEL
        //      |   4  2|----------------+
        //      |   3   |                |
        //      +-------+                |
        //          |                    |
        //          |                    |
        //      +-------+             +-------+
        //      |   1   |             |   2   |
        //      |   5  2|-------------|1  6   |
        //      |       |             |       |
        //      +-------+             +-------+

        {
            int [][] linkArray = {
                                  {3, 3, 4, 1, DIRECT_LINK},
                                  {4, 1, 3, 3, DIRECT_LINK},

            };
            int [][] expectedClusters = {
                                         {1, 2, 3, 4, 5, 6}
            };
            int [][][] expectedBroadcastPorts = {
                                                 {{1,1}, {2,1}, {1,2}, {3,1},
                                                  {3,3}, {4,1}, {4,3}, {5,1},
                                                  {4,2}, {6,2}},
            };

            createTopologyFromLinks(linkArray);
            topologyManager.createNewInstance();
            verifyClusters(expectedClusters, false);
            verifyExpectedBroadcastPortsInClusters(expectedBroadcastPorts);
        }
    }

    @Test
    public void testLinkRemovalOnBroadcastDomainPorts() throws Exception {
        {
            int [][] linkArray = {
                                  {1, 1, 2, 1, DIRECT_LINK},
                                  {2, 1, 1, 1, DIRECT_LINK},
                                  {1, 2, 3, 1, DIRECT_LINK},
                                  {3, 1, 1, 2, DIRECT_LINK},
                                  {2, 2, 3, 2, DIRECT_LINK},
                                  {3, 2, 2, 2, DIRECT_LINK},
                                  {1, 1, 3, 2, DIRECT_LINK},
                                  // the last link should make ports
                                  // (1,1) and (3,2) to be broadcast
                                  // domain ports, hence all links
                                  // from these ports must be eliminated.
            };

            int [][] expectedClusters = {
                                         {1, 3}, {2},
            };
            createTopologyFromLinks(linkArray);
            topologyManager.createNewInstance();
            if (topologyManager.getCurrentInstance() instanceof TopologyInstance)
                verifyClusters(expectedClusters);
        }
        {
            int [][] linkArray = {
                                  {1, 2, 3, 2, DIRECT_LINK},
                                  // the last link should make ports
                                  // (1,1) and (3,2) to be broadcast
                                  // domain ports, hence all links
                                  // from these ports must be eliminated.
            };

            int [][] expectedClusters = {
                                         {1}, {3}, {2},
            };
            createTopologyFromLinks(linkArray);
            topologyManager.createNewInstance();
            if (topologyManager.getCurrentInstance() instanceof TopologyInstance)
                verifyClusters(expectedClusters);
        }
    }

    @Test
    public void testGetRoutes() throws Exception{
        TopologyManager tm = getTopologyManager();

        DatapathId one = DatapathId.of(1);
        DatapathId two = DatapathId.of(2);
        DatapathId three = DatapathId.of(3);
        DatapathId four = DatapathId.of(4);
        DatapathId five = DatapathId.of(5);
        DatapathId six = DatapathId.of(6);

        // 1 - hop count
        // 3 - latency
        TopologyManager.routeMetrics = 3;

        //Get all paths based on latency. These will
        //be used in the assertion statements below
        int [][] linkArray = {
                {1, 1, 2, 1, DIRECT_LINK},
                {1, 2, 3, 1, DIRECT_LINK},
                {2, 2, 3, 2, DIRECT_LINK},
        };

        int [] lat = {1,50,1};
        CaseyIsABoss(linkArray, lat);
        topologyManager.createNewInstance();
        ArrayList<Route> lat_paths = topologyManager.getRoutes(one, three, 2);
        log.info("Links: {}", topologyManager.getAllLinks());
        log.info("Low Lat Road: {}", lat_paths.get(0));
        log.info("High Lat Road: {}", lat_paths.get(1));

        //Get hop count paths for use in assertion statements
        TopologyManager.routeMetrics = 1;
        CaseyIsABoss(linkArray, lat);
        topologyManager.createNewInstance();
        ArrayList<Route> hop_paths = topologyManager.getRoutes(one, three, 2);
        log.info("Links: {}", topologyManager.getAllLinks());
        log.info("Low Hop Road: {}", lat_paths.get(0));
        log.info("High Hop Road: {}", lat_paths.get(1));

        ///////////////////////////////////////////////////////////////////////
        //Check if routes equal what the expected output should be
        TopologyManager.routeMetrics = 3;
        Integer k = 2;

        int [] lat1 = {1,50,1};
        CaseyIsABoss(linkArray, lat1);
        topologyManager.createNewInstance();
        ArrayList<Route> r1 = topologyManager.getRoutes(one, three, k);
        log.info("r1: {}", r1.get(0));
        log.info("paths.get(0): {}", lat_paths.get(0));
        assertTrue((r1.get(0)).equals(lat_paths.get(0)));
        assertTrue((r1.get(1)).equals(lat_paths.get(1)));

        ////////////////////////////////////////////////////////////////////////////////////
        //Check output with bottom latency = -100.

        topologyManager.clearCurrentTopology();
        TopologyManager.routeMetrics = 3;
        int [] lat2 = {1,-100,1};
        CaseyIsABoss(linkArray, lat2);
        topologyManager.createNewInstance();


        ////////////////////////////////////////////////////////////////////////////////////
        //Check output with bottom latency = 25000.
        topologyManager.clearCurrentTopology();
        int [] lat3 = {1,25000,1};
        CaseyIsABoss(linkArray, lat3);
        topologyManager.createNewInstance();


        ///////////////////////////////////////////////////////////////////////
        //Create topology from presentation
        topologyManager.clearCurrentTopology();
        TopologyManager.routeMetrics = 1;
        k = 1000;
        int [][] linkArray2 = {
                {1, 1, 2, 1, DIRECT_LINK},
                {1, 2, 4, 1, DIRECT_LINK},
                {2, 2, 3, 1, DIRECT_LINK},
                {3, 3, 5, 2, DIRECT_LINK},
                {3, 4, 6, 2, DIRECT_LINK},
                {4, 2, 2, 3, DIRECT_LINK},
                {4, 3, 3, 2, DIRECT_LINK},
                {4, 4, 5, 1, DIRECT_LINK},
                {5, 3, 6, 1, DIRECT_LINK},
        };

        int [] lat4 = {3,2,4,2,1,1,2,3,2};
        CaseyIsABoss(linkArray2, lat4);
        topologyManager.createNewInstance();

        log.info("Links: {}", topologyManager.getAllLinks());
        //Call getRoutes

        ArrayList<Route> r = topologyManager.getRoutes(one, six, k);

        for(int i = 0; i< r.size(); i++) {
            log.info("GEDDDDDDDDINGGGGGGGGGSSSSS! Route: {}", r.get(i));
        }

        //Create topology from presentation

        topologyManager.clearCurrentTopology();
        TopologyManager.routeMetrics = 3;
        k = 7;
        CaseyIsABoss(linkArray2, lat4);
        topologyManager.createNewInstance();

        //log.info("Links: {}", topologyManager.getAllLinks());
        //Call getRoutes

        ArrayList<Route> r2 = topologyManager.getRoutes(one, six, k);

        for(int i = 0; i< r2.size(); i++) {
            log.info("GEDDDDDDDDINGGGGGGGGGSSSSS! Route: {}", r2.get(i));
        }


        NodePortTuple one1 = new NodePortTuple(one, OFPort.of(1));
        NodePortTuple one2 = new NodePortTuple(one, OFPort.of(2));

        NodePortTuple two1 = new NodePortTuple(two, OFPort.of(1));
        NodePortTuple two2 = new NodePortTuple(two, OFPort.of(2));
        NodePortTuple two3 = new NodePortTuple(two, OFPort.of(3));

        NodePortTuple three1 = new NodePortTuple(three, OFPort.of(1));
        NodePortTuple three2 = new NodePortTuple(three, OFPort.of(2));
        NodePortTuple three3 = new NodePortTuple(three, OFPort.of(3));
        NodePortTuple three4 = new NodePortTuple(three, OFPort.of(4));

        NodePortTuple four1 = new NodePortTuple(four, OFPort.of(1));
        NodePortTuple four2 = new NodePortTuple(four, OFPort.of(2));
        NodePortTuple four3 = new NodePortTuple(four, OFPort.of(3));
        NodePortTuple four4 = new NodePortTuple(four, OFPort.of(4));

        NodePortTuple five1 = new NodePortTuple(five, OFPort.of(1));
        NodePortTuple five2 = new NodePortTuple(five, OFPort.of(2));
        NodePortTuple five3 = new NodePortTuple(five, OFPort.of(3));

        NodePortTuple six1 = new NodePortTuple(six, OFPort.of(1));
        NodePortTuple six2 = new NodePortTuple(six, OFPort.of(2));

        List<NodePortTuple> route0 = new ArrayList<NodePortTuple>();
        route0.add(one1);
        route0.add(two1);
        route0.add(two2);
        route0.add(three1);
        route0.add(three4);
        route0.add(six2);
        Route root0 = new Route(one, six);
        root0.setPath(route0);

        ArrayList<NodePortTuple> route1 = new ArrayList<NodePortTuple>();
        route1.add(one2);
        route1.add(four1);
        route1.add(four3);
        route1.add(three2);
        route1.add(three4);
        route1.add(six2);
        Route root1 = new Route(one, six);
        root1.setPath(route1);

        ArrayList<NodePortTuple> route2 = new ArrayList<NodePortTuple>();
        route2.add(one2);
        route2.add(four1);
        route2.add(four4);
        route2.add(five1);
        route2.add(five3);
        route2.add(six1);
        Route root2 = new Route(one, six);
        root2.setPath(route2);

        ArrayList<NodePortTuple> route3 = new ArrayList<NodePortTuple>();
        route3.add(one1);
        route3.add(two1);
        route3.add(two2);
        route3.add(three1);
        route3.add(three3);
        route3.add(five2);
        route3.add(five3);
        route3.add(six1);
        Route root3 = new Route(one, six);
        root3.setPath(route3);

        ArrayList<NodePortTuple> route4 = new ArrayList<NodePortTuple>();
        route4.add(one2);
        route4.add(four1);
        route4.add(four3);
        route4.add(three2);
        route4.add(three3);
        route4.add(five2);
        route4.add(five3);
        route4.add(six1);
        Route root4 = new Route(one, six);
        root4.setPath(route4);

        ArrayList<NodePortTuple> route5 = new ArrayList<NodePortTuple>();
        route5.add(one2);
        route5.add(four1);
        route5.add(four2);
        route5.add(two3);
        route5.add(two2);
        route5.add(three1);
        route5.add(three4);
        route5.add(six2);
        Route root5 = new Route(one, six);
        root5.setPath(route5);

        ArrayList<NodePortTuple> route6 = new ArrayList<NodePortTuple>();
        route6.add(one2);
        route6.add(four1);
        route6.add(four2);
        route6.add(two3);
        route6.add(two2);
        route6.add(three1);
        route6.add(three3);
        route6.add(five2);
        route6.add(five3);
        route6.add(six1);
        Route root6 = new Route(one, six);
        root6.setPath(route6);
    }
}
