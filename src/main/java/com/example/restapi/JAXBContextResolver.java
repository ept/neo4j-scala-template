package com.example.restapi;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

@Provider   
public class JAXBContextResolver implements ContextResolver<JAXBContext> {
    
    private Class<?>[] classes = null;
    private JSONJAXBContext context = null;

    public JAXBContextResolver() {
        classes = new Class<?>[2];
        try {
            classes[0] = Class.forName("com.example.models.Moo");
            classes[1] = Class.forName("com.example.models.Test");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            context = new JSONJAXBContext(JSONConfiguration.natural().build(), classes);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
    
    public JAXBContext getContext(Class<?> arg0) {
        return context;
    }

}
