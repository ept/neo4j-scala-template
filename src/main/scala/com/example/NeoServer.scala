package com.example

import java.util.Properties
import org.neo4j.api.core.EmbeddedNeo

/**
 * Wrapper around a singleton instance of Neo4j embedded server.
 */
object NeoServer {

  private var neo: EmbeddedNeo = null

  /**
   * Initialize Neo4j with configuration from a properties file.
   */
  def startup(config: Properties) {
    if (neo != null) neo.shutdown
    neo = new EmbeddedNeo(config.getProperty("path", "/tmp/neo4j"))
  }

  /**
   * Do a clean shutdown of Neo4j.
   */
  def shutdown {
    if (neo != null) neo.shutdown
    neo = null
  }

  /**
   * Execute instructions within a Neo4j transaction; rollback if exception is raised and
   * commit otherwise; and return the return value from the operation.
   */
  def exec[T<:AnyRef](operation: EmbeddedNeo => T): T = {
    val tx = neo.beginTx
    try {
      val ret = operation(neo)
      tx.success
      return ret
    } finally {
      tx.finish
    }
  }
}
