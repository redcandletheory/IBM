package com.ibmi.ias.api;

import com.ibmi.ias.service.DcmSearchService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST API for IBM i Digital Certificate Manager (DCM) Return Code Search & Deconstruction.
 * Hosted on Integrated Application Server (IAS) / WebSphere Liberty 24.0.0.12+.
 */
@Path("/dcm")
@Produces(MediaType.APPLICATION_JSON)
public class DcmSearchResource {

    private final DcmSearchService searchService = new DcmSearchService();

    @GET
    @Path("/decode")
    public Response decodeSingle(@QueryParam("symptom") String symptom) {
        if (symptom == null || symptom.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Query parameter 'symptom' is required.\"}")
                    .build();
        }
        Map<String, Object> result = searchService.decode(symptom);
        return Response.ok(result).build();
    }

    @POST
    @Path("/search")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    public Response searchBatch(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Request body cannot be empty.\"}")
                    .build();
        }

        String[] lines = payload.split("\\r?\\n");
        List<Map<String, Object>> results = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                results.add(searchService.decode(line));
            }
        }
        return Response.ok(results).build();
    }
}
