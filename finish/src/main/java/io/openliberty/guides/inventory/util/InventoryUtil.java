// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
 // end::copyright[]
package io.openliberty.guides.inventory.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
// JSON-P
import javax.json.JsonObject;
// JAX-RS
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

public class InventoryUtil {

    // Constants for building URI to the system service.
    private static final int PORT = 9080;
    private static final String PROTOCOL = "http";
    private static final String SYSTEM_PROPERTIES = "/system/properties";

    /**
     * <p>Retrieves the JVM system properties of a particular host.</p>
     * 
     * <p>NOTE: the host must expose its JVM's system properties via the 
     * system service; ie. the system service must be running on that host.</p>
     * 
     * @param hostname - name of host.
     * @return JSON Java object containing the system properties of the host's JVM.
     */
    public static JsonObject getProperties(String hostname) {
        Client client = ClientBuilder.newClient();
        URI propURI = InventoryUtil.buildUri(hostname);
        JsonObject properties = client.target(propURI).request().get(JsonObject.class);
        
        System.out.println(properties.toString());
        
        
        // Add timestamp to identify when system properties was requested
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String timestamp = dateFormat.format(date); //2016/11/16 12:08:43
        
        System.out.println(timestamp); 
        
        JsonObject systemProp = Json.createObjectBuilder()
                                       .add("Time", timestamp)
                                       .add("Properties", properties)
                                       .build();
        System.out.println(systemProp.toString()); 
        return systemProp;
    }

    /**
     * <p>Returns whether or not a particular host is exposing its JVM's system properties.
     * In other words, returns whether or not the system service is running on a 
     * particular host.</p>
     * 
     * @param hostname - name of host.
     * @return true if the host is currently running the system service and false otherwise.
     */
    public static boolean responseOk(String hostname) {
        try {
            URL target = new URL(buildUri(hostname).toString());
            HttpURLConnection http = (HttpURLConnection) target.openConnection();
            http.setConnectTimeout(50);
            int response = http.getResponseCode();
            return (response != 200) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * <p>Builds the URI to the system service for a particular host. This is just a helper method.</p>
     * 
     * @param hostname - name of host.
     * @return URI object representation of the URI to the system properties service.
     */
    private static URI buildUri(String hostname) {
        return UriBuilder.fromUri(SYSTEM_PROPERTIES)
                .host(hostname)
                .port(PORT)
                .scheme(PROTOCOL)
                .build();
    }

}
