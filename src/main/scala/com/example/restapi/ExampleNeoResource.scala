package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core._

import org.codehaus.jettison.json.JSONObject

import com.example.NeoServer
import NeoJsonConverter._

/**
 * A straightforward JSON resource where an entity maps directly to a Neo4j node.
 */
@Path("/neo_resource")
class ExampleNeoResource extends RequiredParam {

  /**
   * <tt>POST /neo_resource</tt> with a JSON document as body creates a new entity
   * from that document, and returns a HTTP 201 "Created" response with a Location
   * header indicating the URL of the newly created entity.
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createJSON(json: JSONObject) = {
    NeoServer.exec { neo =>
      val node = jsonToNeo(json, neo, null)
      val uri = UriBuilder.fromResource(this.getClass).path("{id}").build(new java.lang.Long(node.getId))
      Response.created(uri).entity(neoToJson(node)).build
    }
  }

  /**
   * <tt>GET /neo_resource/&lt;id&gt;</tt> returns a JSON representation of the entity
   * with the given ID.
   */
  @GET @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def readJSON(@PathParam("id") node: NeoNodeParam) = {
    requiredParam("id", node)
    NeoServer.exec {
      neo => neoToJson(node.getNode(neo))
    }
  }
}
