package com.eptcomputing.neo4j.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.JerseyTest;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * Sadly, we cannot yet write Jersey client tests in Scala because of a bug in the compiler:
 * http://lampsvn.epfl.ch/trac/scala/ticket/1539
 */
public class NeoResourceTest extends JerseyTest {

    public NeoResourceTest() throws Exception {
        super("com.eptcomputing.neo4j.rest.test");
    }

    /**
     * Helper which creates a new entity via the API, and returns its ID.
     */
    private long createEntity(JSONObject entity) {
        ClientResponse created = webResource.path("/neo_resource").type("application/json").
            post(ClientResponse.class, entity);
        assertEquals(201, created.getStatus());
        return Long.parseLong(created.getLocation().getPath().replaceAll(".*/", ""));
    }

    @Test
    public void testCreateEntity() throws JSONException {
        long id = createEntity(new JSONObject().put("key", "value"));
        JSONObject read = webResource.path(String.format("/neo_resource/%d", id)).get(JSONObject.class);
        assertEquals("value", read.get("key"));
    }

    @Test
    public void testUpdateProperties() throws JSONException {
        // Create new entity
        long id = createEntity(new JSONObject().put("one", 1).put("two", 2).put("three", 3));

        // Delete one, update two, leave three unchanged, add four
        JSONObject updated = new JSONObject().put("two", 22).put("three", 3).put("four", 4);
        JSONObject readBack = webResource.path(String.format("/neo_resource/%d", id)).type("application/json").
            put(JSONObject.class, updated);

        // Also do a separate read, and make sure both have the right contents
        JSONObject readSeparate = webResource.path(String.format("/neo_resource/%d", id)).get(JSONObject.class);
        JSONObject[] reads = {readBack, readSeparate};
        for (JSONObject read : reads) {
            try {
                read.getInt("one");
                fail("JSON object has a value for key \"one\", but none was expected");
            } catch (JSONException e) {}
            assertEquals(22, read.getInt("two"));
            assertEquals(3, read.getInt("three"));
            assertEquals(4, read.getInt("four"));
        }
    }

    @Test
    public void testUpdateRelationships() throws JSONException {
        // 1 <-- 2 <--> 3  and  1 <-- 3
        long one = createEntity(new JSONObject());
        long two = createEntity(new JSONObject().put("_out",
            new JSONObject().put("ONE_TWO", one)
        ));
        long three = createEntity(new JSONObject().put("_in",
            new JSONObject().put("TWO_THREE", two)
        ).put("_out",
            new JSONObject().put("TWO_THREE", two).put("ONE_TWO",
                new JSONObject().put("_end", one).put("foo", "bar")
            )
        ));
        long four = createEntity(new JSONObject());

        // Update to: 1 <--> 2 --> 3  and  1 <-- 3  and  2 --> 4
        JSONObject twoUpdate = new JSONObject().put("_in",
            new JSONObject().put("ONE_TWO",
                new JSONObject().put("_start", one).put("foo", "bar")
            )
        ).put("_out",
            new JSONObject().put("ONE_TWO", one).put("TWO_THREE", new JSONArray().put(three).put(four))
        );
        JSONObject readBack = webResource.path(String.format("/neo_resource/%d", two)).type("application/json").
            put(JSONObject.class, twoUpdate);

        // Also do a separate read, and make sure both have the right contents
        JSONObject readSeparate = webResource.path(String.format("/neo_resource/%d", two)).get(JSONObject.class);
        JSONObject[] reads = {readBack, readSeparate};
        for (JSONObject read : reads) {
            JSONObject in = read.getJSONObject("_in");
            JSONObject out = read.getJSONObject("_out");
            assertEquals(one, in.getJSONObject("ONE_TWO").getInt("_start"));
            assertEquals("bar", in.getJSONObject("ONE_TWO").getString("foo"));
            try {
                in.getJSONObject("TWO_THREE");
                fail("JSON object has an incoming ONE_TWO relationship, but none was expected");
            } catch (JSONException e) {}
            assertEquals(one, out.getJSONObject("ONE_TWO").getInt("_end"));
            try {
                out.getJSONObject("ONE_TWO").getString("foo");
                fail("Outgoing ONE_TWO relationship's foo property should have been deleted");
            } catch (JSONException e) {}
            assertThat(new long[] {
                out.getJSONArray("TWO_THREE").getJSONObject(0).getInt("_end"),
                out.getJSONArray("TWO_THREE").getJSONObject(1).getInt("_end")
            }, is(anyOf(equalTo(new long[] {three, four}), equalTo(new long[] {four, three}))));
        }
    }

    @Test
    public void testDeleteEntity() throws JSONException {
        // Create two new entities with a relationship
        long id = createEntity(new JSONObject().put("key", "value"));
        createEntity(new JSONObject().put("something", "else").put("_out",
            new JSONObject().put("KNOWS", id)
        ));

        // Delete the first and check that it has gone
        JSONObject response = webResource.path(String.format("/neo_resource/%d", id)).delete(JSONObject.class);
        assertEquals("value", response.get("key"));
        try {
            webResource.path(String.format("/neo_resource/%d", id)).get(JSONObject.class);
            fail("Accessing an entity after it has been deleted should return 404");
        } catch (UniformInterfaceException e) {
            assertEquals(404, e.getResponse().getStatus());
        }
    }
}
