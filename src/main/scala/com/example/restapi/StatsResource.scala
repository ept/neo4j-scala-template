package com.example.restapi

import javax.ws.rs.Path
import org.codehaus.jettison.json.JSONObject
import org.neo4j.graphdb.{GraphDatabaseService, Node}

import com.example.models.Stats

/**
 * Example of a read-only resource which performs a query (graph traversal) and returns
 * a result object as JSON. This class defines the API, while the <tt>Stats</tt> model
 * class implements the actual query.
 */
@Path("/stats")
class StatsResource extends com.eptcomputing.neo4j.rest.Neo4jResource {

  def read(neo: GraphDatabaseService, node: Node) = new Stats(node).toJSON

  def create(neo: GraphDatabaseService, json: JSONObject) = throw new Exception("not allowed")

  def update(neo: GraphDatabaseService, existing: Node, newValue: JSONObject) = throw new Exception("not allowed")

  def delete(neo: GraphDatabaseService, node: Node) = throw new Exception("not allowed")
}
