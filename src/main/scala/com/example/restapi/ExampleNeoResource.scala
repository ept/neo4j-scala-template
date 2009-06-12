package com.example.restapi

import java.util.logging.Logger
import javax.ws.rs._
import javax.ws.rs.core._

import org.codehaus.jettison.json.JSONObject

import com.example.NeoServer
import NeoJsonConverter._

/**
 * A straightforward Create/Read/Update/Delete JSON resource where an entity maps
 * directly to a Neo4j node.
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

  /**
   * <tt>PUT /neo_resource/&lt;id&gt;</tt> with a JSON document as body replaces an
   * existing entity with the contents of that document. Returns the same as you would
   * get from a subsequent <tt>GET</tt> of the same URL.
   */
  @PUT @Path("/{id}")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  def updateJSON(@PathParam("id") node: NeoNodeParam, json: JSONObject) = {
    requiredParam("id", node)
    NeoServer.exec {
      neo => neoToJson(jsonToNeo(json, neo, node.getNode(neo)))
    }
  }

  /**
   * <tt>DELETE /neo_resource/&lt;id&gt;</tt> deletes the entity with the given ID.
   * Returns a JSON representation of the entity that was deleted.
   */
  @DELETE @Path("/{id}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  def deleteJSON(@PathParam("id") node: NeoNodeParam) = {
    requiredParam("id", node)
    NeoServer.exec { neo =>
      val json = neoToJson(node.getNode(neo))
      node.deleteNode(neo)
      json
    }
  }
}
