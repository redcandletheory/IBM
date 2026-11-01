package com.ibmi.iws;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application root for IWS 3.0 runtime.
 * Mounts all REST endpoints under /api/*
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
