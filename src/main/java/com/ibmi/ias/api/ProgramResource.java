package com.ibmi.ias.api;

import com.ibmi.ias.service.ProgramCallerService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoint to trigger IBM i native programs (*PGM)
 * POST /api/program/call?lib=MYLIB&pgm=MYPGM&outLen=100
 * Hosted on Integrated Application Server (IAS) / WebSphere Liberty 24.0.0.12+.
 */
@Path("/program")
public class ProgramResource {

    private final ProgramCallerService callerService = new ProgramCallerService();

    @POST
    @Path("/call")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeProgram(
            @QueryParam("lib") String library,
            @QueryParam("pgm") String program,
            @QueryParam("outLen") @DefaultValue("100") int outLen,
            String inputPayload) {

        if (library == null || library.trim().isEmpty() || program == null || program.trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Query parameters 'lib' and 'pgm' are required.");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        try {
            Map<String, Object> result = callerService.callProgram(
                    library.trim(),
                    program.trim(),
                    inputPayload != null ? inputPayload : "",
                    outLen
            );
            return Response.ok(result).build();
        } catch (Exception ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Exception executing program: " + ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
    }
}
