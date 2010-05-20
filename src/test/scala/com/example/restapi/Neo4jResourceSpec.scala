package com.example.restapi

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import com.eptcomputing.neo4j.JerseyConverters

import org.codehaus.jettison.json.{JSONArray, JSONException, JSONObject}
import com.sun.jersey.test.framework.JerseyTest
import com.sun.jersey.api.client.UniformInterfaceException


/**
 * Example of how you can write tests which drive your REST API. When these tests run,
 * an embedded web container is automatically created and the requests executed against
 * that server.
 */
@RunWith(classOf[JUnitRunner])
class Neo4jResourceSpec extends JerseyTest("com.example.restapi")
                        with FlatSpec with ShouldMatchers with BeforeAndAfterAll with JerseyConverters {

  // Run Jersey server while running tests
  override def beforeAll { setUp }
  override def afterAll  { tearDown }

  // Helper which creates a new entity via the API, and returns its ID.
  def createEntity(entity: JSONObject) = {
    val created = resource.path("/neo_resource").contentType("application/json").postResponse(entity)
    created.getStatus should equal(201)
    created.getLocation.getPath.replaceAll(".*/", "").toLong
  }

  // Helper which creates a JSON object from a list of key-value pairs
  def json(pairs: Tuple2[String, Any]*) = {
    val obj = new JSONObject
    for ((key, value) <- pairs.toList) obj.put(key, value.asInstanceOf[AnyRef])
    obj
  }


  "A Neo4jResource" should "return the new resource URL on POST" in {
    val id = createEntity(json(("key", "value")))
    val read = resource.path("/neo_resource/%d".format(id)).getJSON
    read.get("key") should equal("value")
  }


  it should "update resource properties on PUT" in {
    // Create new entity
    val id = createEntity(json(("one", 1), ("two", 2), ("three", 3)))

    // Delete one, update two, leave three unchanged, add four
    val updated = json(("two", 22), ("three", 3), ("four", 4))
    val readBack = resource.path("/neo_resource/%d".format(id)).contentType("application/json").putJSON(updated)

    // Also do a separate read, and make sure both have the right contents
    val readSeparate = resource.path("/neo_resource/%d".format(id)).getJSON
    for (read <- Array(readBack, readSeparate)) {
      evaluating { read.getInt("one") } should produce [JSONException]
      read.getInt("two") should equal(22)
      read.getInt("three") should equal(3)
      read.getInt("four") should equal(4)
    }
  }


  it should "update relationships on PUT" in {
    // 1 <-- 2 <--> 3  and  1 <-- 3
    val one = createEntity(json())
    val two = createEntity(json(("_out", json(("ONE_TWO", one)))))
    val three = createEntity(json(
      ("_in",  json(("TWO_THREE", two))),
      ("_out", json(("TWO_THREE", two), ("ONE_TWO", json(("_end", one), ("foo", "bar")))))
    ))
    val four = createEntity(json())

    // Update to: 1 <--> 2 --> 3  and  1 <-- 3  and  2 --> 4
    val twoUpdate = json(
      ("_in",  json(("ONE_TWO", json(("_start", one), ("foo", "bar"))))),
      ("_out", json(("ONE_TWO", one), ("TWO_THREE", (new JSONArray).put(three).put(four))))
    )
    val readBack = resource.path("/neo_resource/%d".format(two)).contentType("application/json").putJSON(twoUpdate)

    // Also do a separate read, and make sure both have the right contents
    val readSeparate = resource.path("/neo_resource/%d".format(two)).getJSON
    for (read <- Array(readBack, readSeparate)) {
      val in = read.getJSONObject("_in")
      val out = read.getJSONObject("_out")

      in.getJSONObject("ONE_TWO").getInt("_start") should equal(one)
      in.getJSONObject("ONE_TWO").getString("foo") should equal("bar")
      out.getJSONObject("ONE_TWO").getInt("_end")  should equal(one)

      evaluating { in.getJSONObject("TWO_THREE") } should produce [JSONException]
      evaluating { out.getJSONObject("ONE_TWO").getString("foo") } should produce [JSONException]

      Set(
        out.getJSONArray("TWO_THREE").getJSONObject(0).getInt("_end"),
        out.getJSONArray("TWO_THREE").getJSONObject(1).getInt("_end")
      ) should equal(Set(three, four))
    }
  }


  it should "delete a resource on DELETE" in {
    // Create two new entities with a relationship
    val id = createEntity(json(("key", "value")))
    createEntity(json(("something", "else"), ("_out", json(("KNOWS", id)))))

    // Delete the first and check that it has gone
    val response = resource.path("/neo_resource/%d".format(id)).deleteJSON
    response.get("key") should equal("value")

    val thrown = evaluating {
      resource.path("/neo_resource/%d".format(id)).getJSON
    } should produce [UniformInterfaceException]
    thrown.getResponse.getStatus should equal(404)
  }
}
