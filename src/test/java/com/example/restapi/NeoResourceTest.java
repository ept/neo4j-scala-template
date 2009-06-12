package com.example.restapi;

import com.sun.jersey.test.framework.JerseyTest;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Sadly, we cannot yet write Jersey client tests in Scala because of a bug in the compiler:
 * http://lampsvn.epfl.ch/trac/scala/ticket/1539
 */
public class NeoResourceTest extends JerseyTest {

    public NeoResourceTest() throws Exception {
        super("com.example.restapi");
    }

    @Test
    public void testHelloWorld() {
        String response = webResource.path("/neo_resource/0").get(String.class);
        assertEquals("{\"_id\":0,\"_out\":{},\"_in\":{}}", response);
    }
}
