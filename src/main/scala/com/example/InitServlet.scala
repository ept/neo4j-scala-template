package com.example

import java.util.logging.Logger
import javax.servlet.http.HttpServlet

class InitServlet extends HttpServlet {
  val log = Logger.getLogger(this.getClass.getName) 

  override def init {
    log.info("Initializing Neo4J server.")
    // Do initialization
  }
}
