package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core._

import com.example.NeoServer

/**
 * A straightforward JSON resource where an entity maps directly to a Neo4j node.
 */
@Path("/neo_resource")
class ExampleNeoResource extends RequiredParam {

  /**
   * <tt>GET /neo_resource/&lt;id&gt;</tt> returns a JSON representation of the entity
   * with the given ID.
   */
  @GET @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def readJSON(@PathParam("id") node: NeoNodeParam) = {
    requiredParam("id", node)
    NeoServer.exec {
      neo => NeoJsonConverter.neoToJson(node.getNode(neo))
    }
  }

}
