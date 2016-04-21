package com.shravan.project.movingcars.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Car resource to fetch details of the car. Currently not being used, but can
 * be used later as a REST layer to fetch car details
 * 
 * @author shravanshetty
 *
 */
@Path("car")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarResource {

    @GET
    @Path("ping")
    public String ping() {

        return "pong";
    }
}
