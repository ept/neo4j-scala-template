package com.example.models

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import org.codehaus.jettison.json.JSONObject
import org.neo4j.graphdb._

import com.eptcomputing.neo4j.{Neo4jConverters, Neo4jServer}

/**
 * This is an example of how you can write good and expressive tests/specs using
 * the scalatest spec framework. This spec tests the <tt>Stats</tt> model
 * directly, without going through the REST API. For an example of testing full
 * API request/response cycles, see <tt>NeoResourceTest</tt>.
 */
@RunWith(classOf[JUnitRunner])
class StatsSpec extends Spec with ShouldMatchers with Neo4jConverters {

  import Predicates._

  describe("Stats model") {

    it("should return an empty object if the node has no relationships") {
      Neo4jServer.exec { neo =>
        val node = neo.createNode
        new Stats(node).toJSON.length should equal(0)
      }
    }

    it("should return the number of neighbours") {
      Neo4jServer.exec { neo =>
        val node = neo.createNode
        (1 to 3) foreach { _ => node --> KNOWS --> neo.createNode }
        new Stats(node).toJSON.getInt("depth_1") should equal(3)
      }
    }

    it("should not count a node twice") {
      Neo4jServer.exec { neo =>
        val start = neo.createNode
        val end = neo.createNode
        start --> "KNOWS" --> neo.createNode --> "KNOWS" --> end
        start --> "KNOWS" --> neo.createNode --> "KNOWS" --> end
        val stats = new Stats(start).toJSON
        stats.getInt("depth_1") should equal(2)
        stats.getInt("depth_2") should equal(1)
      }
    }

    it("should not follow inbound relationships") {
      Neo4jServer.exec { neo =>
        val node = neo.createNode
        neo.createNode --> KNOWS --> node --> KNOWS --> neo.createNode
        new Stats(node).toJSON.getInt("depth_1") should equal(1)
      }
    }

    it("should not follow relationships of a different type") {
      Neo4jServer.exec { neo =>
        val node = neo.createNode
        node --> "KNOWS" --> neo.createNode
        node --> "LIKES" --> neo.createNode
        new Stats(node).toJSON.getInt("depth_1") should equal(1)
      }
    }

    it("should count only the shortest path to each reachable node") {
      Neo4jServer.exec { neo =>
        val start = neo.createNode
        val reachByTwoPaths = neo.createNode
        start --> KNOWS --> neo.createNode --> KNOWS --> reachByTwoPaths
        start --> KNOWS --> neo.createNode --> KNOWS --> neo.createNode --> KNOWS --> reachByTwoPaths
        val stats = new Stats(start).toJSON
        stats.getInt("depth_1") should equal(2)
        stats.getInt("depth_2") should equal(2)
        stats.has("depth_3") should equal(false)
      }
    }
  }
}
