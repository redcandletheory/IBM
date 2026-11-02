package com.ibmi.iws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application root for IBM WebSphere Liberty 24.0.0.12+ runtime on IBM i.
 * Mounts all REST endpoints under /api/*
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
