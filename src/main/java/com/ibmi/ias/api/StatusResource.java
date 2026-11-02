package com.ibmi.ias.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check & runtime diagnostic endpoint.
 * Built for Integrated Application Server (IAS) on IBM WebSphere Liberty 24.0.0.12+ (IBM i V7R5 / V7R6).
 */
@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {

    @GET
    public Response getStatus() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "IBM i DCM Search Service");
        info.put("targetServer", "Integrated Application Server (IAS) / WebSphere Liberty Profile 24.0.0.12+");
        info.put("status", "UP");
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("timestamp", System.currentTimeMillis());

        return Response.ok(info).build();
    }
}
