package com.example.models

import scala.collection.JavaConversions._
import java.util.logging.Logger
import org.codehaus.jettison.json.JSONObject
import org.neo4j.graphdb._

/**
 * Example of a read-only model object. It is initialised with a Neo4j node and counts how many
 * other nodes are reachable from that node by outgoing "KNOWS" relationships, grouped by length
 * of relationship chain.
 *
 * Example tests for this class are given in <tt>StatsSpec</tt>.
 */
class Stats(startNode: Node) {
  private val log = Logger.getLogger(this.getClass.getName)

  // Traverse the graph
  private val traverser = startNode.traverse(Traverser.Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH,
    ReturnableEvaluator.ALL_BUT_START_NODE, Predicates.KNOWS, Direction.OUTGOING)

  private var countByDepth = Map[Int,Int]()

  // Count how many times each depth has occurred
  for (node <- traverser) {
    val depth = traverser.currentPosition.depth
    countByDepth += depth -> (countByDepth.getOrElse(depth, 0) + 1)
  }

  /** Converts this object to JSON. */
  def toJSON = {
    val json = new JSONObject
    for ((key, value) <- countByDepth) json.put("depth_" + key, value)
    json
  }
}
