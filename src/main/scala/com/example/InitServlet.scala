package com.example

import javax.servlet.http.HttpServlet
import com.eptcomputing.neo4j.Neo4jServer

/**
 * This servlet is automatically initialized when the web app is started.
 * Here we set up the Neo4j server for the rest of the application to use.
 */
class InitServlet extends HttpServlet {
  override def init = Neo4jServer.startup(getServletContext)
  override def destroy = Neo4jServer.shutdown
}
