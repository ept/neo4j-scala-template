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
   * from that document, and returns it in a JSON representation which includes
   * the new entity's ID.
   */
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def createJSON(json: JSONObject) = {
    NeoServer.exec {
      neo => neoToJson(jsonToNeo(json, neo, null))
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
