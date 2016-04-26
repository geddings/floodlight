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

package net.floodlightcontroller.topology.web;

import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.ITopologyService.ROUTE_METRIC;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class RouteMetrics extends ServerResource {
    private static final Logger log = LoggerFactory.getLogger(RouteMetrics.class);

    @Put
    @Post
    public Map<String, String> changeMetric() {
        ITopologyService topology =
                (ITopologyService)getContext().getAttributes().
                        get(ITopologyService.class.getCanonicalName());

        String metric = (String) getRequestAttributes().get("metric");
        metric = metric.trim().toLowerCase();

        ROUTE_METRIC type;

        if (metric.equals("latency")) {
            type = ROUTE_METRIC.LATENCY;
        } else if (metric.equals("utilization")) {
            type = ROUTE_METRIC.UTILIZATION;
        } else if (metric.equals("hopcount")) {
            type = ROUTE_METRIC.HOPCOUNT;
        } else {
            log.error("Invalid input {}", metric);
            return Collections.singletonMap("error", "invalid route metric " + metric);
        }

        if (topology.setRouteMetric(type) != type) {
            log.error("Failed to set valid route metric {}. Bug?", metric);
            return Collections.singletonMap("error", "failed to set valid route metric " + metric);
        }

        log.debug("Set route metric to {}", metric);
        return Collections.singletonMap("success", "route metric set to " + metric);
    }
}

