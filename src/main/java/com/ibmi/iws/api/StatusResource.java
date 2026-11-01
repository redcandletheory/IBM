package com.ibmi.iws.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check & runtime diagnostic endpoint.
 * Portable across IBM i V7R5, V7R6, and IWS 3.0 instances.
 */
@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    @GET
    public Response getStatus() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "IWS Portable Service");
        info.put("status", "UP");
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("timestamp", System.currentTimeMillis());

        return Response.ok(info).build();
    }
}
