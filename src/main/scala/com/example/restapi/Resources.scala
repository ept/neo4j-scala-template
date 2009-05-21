package com.example.restapi

import com.sun.jersey.api.json.{JSONConfiguration, JSONJAXBContext}
import javax.ws.rs.ext.{ContextResolver, Provider}
import javax.xml.bind.JAXBContext

import org.neo4j.api.core._
import org.codehaus.jettison.json.{JSONObject, JSONArray, JSONException}

/**
 * Defines the conversion of bean objects to/from JSON.
 */
@Provider
class JAXBContextResolver extends ContextResolver[JAXBContext] {
  private val context = new JSONJAXBContext(JSONConfiguration.natural.build, "com.example.models")
  override def getContext(objectType: Class[_]): JAXBContext = context
}

/**
 * Provides helpers for converting Neo4j nodes to/from JSON.
 */
object NeoJsonConverter {
  /**
   * Serialises a Neo4j node into a JSON object of the following form:
   * <pre>{
   *   "_id": 1234,
   *   "property1": "value1",
   *   "property2": 42,
   *   "_out": {
   *     "KNOWS": [ {"_end": 1235, "property": "value"} ],
   *     "LIKES": [ {"_end": 1236} ]
   *   },
   *   "_in": {
   *     "KNOWS": [ {"_start": 1233} ]
   *   }
   * }</pre>
   */
  def neoToJson(node: Node): JSONObject = {
    val obj = new JSONObject
    obj.put("_id", node.getId)
    neoPropertiesToJson(node, obj)
    obj.put("_out", neoRelationshipsToJson(node, Direction.OUTGOING))
    obj.put("_in",  neoRelationshipsToJson(node, Direction.INCOMING))
    obj
  }

  /**
   * Adds all properties of a given node or relationship to a JSONObject in a
   * <tt>key:value</tt> style.
   */
  private def neoPropertiesToJson(container: PropertyContainer, obj: JSONObject) {
    for (key <- container.getPropertyKeys) {
      obj.put(key, container.getProperty(key))
    }
  }

  /**
   * Creates a JSON object whose keys are relationship types, and whose values are
   * JSON arrays of objects representing all the relationships of that type.
   */
  private def neoRelationshipsToJson(node: Node, direction: Direction): JSONObject = {
    val obj = new JSONObject
    for (rel <- node.getRelationships(direction)) {
      val relName = rel.getType.name

      // Get or create the JSONArray
      val arr = try {
        obj.getJSONArray(relName)
      } catch {
        case e: JSONException =>
          val newArr = new JSONArray
          obj.put(relName, newArr)
          newArr
      }

      // Add the relationship object to the array
      arr.put {
        val relObj = new JSONObject
        relObj.put(if (rel.getStartNode == node) "_end" else "_start", rel.getOtherNode(node).getId)
        neoPropertiesToJson(rel, relObj)
        relObj
      }
    }
    obj
  }

  /**
   * Convert a Java iterable to a Scala iterator.
   */
  implicit def java2scala[T](iter: java.lang.Iterable[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter.iterator)
}
