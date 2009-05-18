package com.example

import org.neo4j.api.core.EmbeddedNeo

object NeoServer {
  val neo = new EmbeddedNeo("/tmp/neo4j")
  
  def exec[T<:AnyRef](operation: EmbeddedNeo => T): T = {
    val tx = neo.beginTx()
    try {
      val ret = operation(neo)
      tx.success()
      return ret
    } finally {
      tx.finish();
    }
  }

  // somehow catch shutdown and run: neo.shutdown
}
