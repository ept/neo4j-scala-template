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
   *     "KNOWS": [ {"_end": 1235, "property": "value"}, {"_end": 1236} ],
   *     "LIKES": {"_end": 1237}
   *   },
   *   "_in": {
   *     "KNOWS": {"_start": 1233}
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
      obj.accumulate(rel.getType.name, {
        val relObj = new JSONObject
        relObj.put(if (rel.getStartNode == node) "_end" else "_start", rel.getOtherNode(node).getId)
        neoPropertiesToJson(rel, relObj)
        relObj
      })
    }
    obj
  }


  /**
   * Parses properties and relationships out of a JSON object in the form as documented
   * on <tt>neoToJson</tt>. If <tt>existingNode</tt> is given, the properties and relationships
   * of that node are updated to match the JSON description; if null, a new node is created.
   * In either case, the up-to-date node is returned.
   */
  def jsonToNeo(json: JSONObject, neo: NeoService, existingNode: Node): Node = {
    val node = if (existingNode == null) neo.createNode else existingNode
    // Keep track of any of the node's properties + relationships not present in the JSON
    val deleteProp = new scala.collection.mutable.HashSet[String]
    if (existingNode != null) for (key <- node.getPropertyKeys) deleteProp += key
    // Update node with properties from JSON
    for (key <- json.keys.asInstanceOf[java.util.Iterator[String]]) {
      if (!(Array("_id", "_in", "_out") contains key)) {
          node.setProperty(key, json.get(key))
          deleteProp -= key
      }
    }
    // Delete any unused properties
    for (key <- deleteProp) node.removeProperty(key)
    node
  }

  /** Implicitly convert a Java iterable to a Scala iterator. */
  implicit def java2scala[T](iter: java.lang.Iterable[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter.iterator)
  
  /** Implicitly convert a Java iterator to a Scala iterator. */
  implicit def java2scala[T](iter: java.util.Iterator[T]): scala.Iterator[T] =
    new scala.collection.jcl.MutableIterator.Wrapper(iter)
}
