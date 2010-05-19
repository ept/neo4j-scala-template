package com.example.restapi

import javax.ws.rs.Path
import com.eptcomputing.neo4j.rest.SimpleNeo4jResource

@Path("/neo_resource")
class NeoResource extends SimpleNeo4jResource
