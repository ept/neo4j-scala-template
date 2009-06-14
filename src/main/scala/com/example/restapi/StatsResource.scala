package com.example.restapi

import javax.ws.rs.Path
import org.codehaus.jettison.json.JSONObject
import org.neo4j.api.core.{NeoService, Node}

import com.example.models.Stats

/**
 * Example of a read-only resource which performs a query (graph traversal) and returns
 * a result object as JSON. This class defines the API, while the <tt>Stats</tt> model
 * class implements the actual query.
 */
@Path("/stats")
class StatsResource extends com.eptcomputing.neo4j.rest.NeoResource {

  def read(neo: NeoService, node: Node) = new Stats(node).toJSON

  def create(neo: NeoService, json: JSONObject) = throw new Exception("not allowed")

  def update(neo: NeoService, existing: Node, newValue: JSONObject) = throw new Exception("not allowed")

  def delete(neo: NeoService, node: Node) = throw new Exception("not allowed")
}
