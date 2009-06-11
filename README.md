Neo4j/Scala/Jersey project template
===================================

* You want to develop an application using the [Neo4j](http://neo4j.org)
  open source graph database?
* You want to write your code in beautiful [Scala](http://www.scala-lang.org)?
* You want to expose your database through a nice clean REST API, implemented
  with [Jersey/JSR311]()?
* You want to get up and running quickly?

Then you've come to the right place. This repository contains a rough outline
for a new project, including a Maven descriptor `pom.xml` detailing all the
dependencies, an Eclipse project with all the right build settings, and
examples of how to write tests. You'll be up and running in no time.


How to use this template
------------------------

You need a Java 5 environment and Maven 2.0.9 or newer installed.

Download this repository, or clone it with `git`, or merge it into your own
git repository. You will probably want to
search the files for occurrences of `example` and replace them with something
more appropriate.

Out of the box, this template is set up to support the following goals:

* `mvn package` -- Compile, test and bundle into a `war` file for deployment to
  a standard Java web container (e.g. Tomcat).
* `mvn test` -- Just compile and test.
* `mvn jetty:run` -- Compile and run the app in an embedded Jetty server
  on `http://localhost:8080/`.
* `mvn clean` -- Remove build files

To use the project in Eclipse, you must have the Eclipse Scala plugin installed.
You should also do a full Maven build before using Eclipse, to ensure you have
all the dependencies.
Then you should be able to do "File -> Import -> General -> Existing Projects into
Workspace" and be ready to go. Note that at the time of writing, the Eclipse Scala
plugin appears to have a bug which causes it not to write any class files to the
target directory.

This project includes an example resource called `neo_resource`, which you can use
as basis to get started. Just run `mvn jetty:run` and use [cURL](http://curl.haxx.se/)
to access the REST API:

    $ curl -i -HAccept:application/json -HContent-type:application/json \
        -d'{"name":"my first test node","_out":{"KNOWS":0}}' -XPOST \
        http://localhost:8080/neo_resource

    $ curl -i -HAccept:application/json http://localhost:8080/neo_resource/0

(This example uses the assumption that a node with ID 0 exists, which is Neo's
reference node in the current implementation -- you shouldn't rely on the
reference node having ID 0 though.)


Troubleshooting
---------------

If you're using a Java 6 JDK, you may get an error like "JAXB 2.0 API is being
loaded from the bootstrap classloader, but this RI needs 2.1 API" when executing
`mvn jetty:run`. You can fix this by setting the following environment variable:

    export MAVEN_OPTS="-Djava.endorsed.dirs=$HOME/.m2/repository/javax/xml/bind/jaxb-api/2.1"

Depending on your operating system you may need to to adjust the path above to point
to your Maven repository.


Useful links
------------

The main references you'll need for development:

* [Scala standard library reference](http://www.scala-lang.org/docu/files/api/index.html)
* [Jersey Javadoc](https://jsr311.dev.java.net/nonav/releases/1.0/index.html)
* [Neo4j Javadoc](http://api.neo4j.org/current/)
* [Scalatest](http://www.artima.com/scalatest/doc-0.9.5/index.html)


License
-------

Copyright (c) 2009 Martin Kleppmann.

This program is free software: you can redistribute it and/or modify it under the terms of
the GNU Affero General Public License as published by the Free Software Foundation, version 3.
See `LICENSE.txt` for details.

Please note that the Affero GPL does not allow you to use this software in a closed-source
web application. If distributing the source of your application is not acceptable to you,
please contact [Ept Computing](http://www.eptcomputing.com/) to obtain a commercial license.
