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
package io.openliberty.guides.inventory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// CDI
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
// JSON-P
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.eclipse.microprofile.faulttolerance.Fallback;

import io.openliberty.guides.common.JsonMessages;
import io.openliberty.guides.inventory.util.InventoryUtil;

@ApplicationScoped
public class InventoryManager {

  @Inject SystemProperty systemProp;
  private ConcurrentMap<String, JsonObject> inv = new ConcurrentHashMap<>();
  
  //counter for number of tries
  private int counter = 0; 

  /*
   * CURRENT APPROACH: fallback on inventory when system property already exists
   * Things to do: 
   * 1. Add a system property to trigger fallback
   * 2. Add a new service to count the number of retries (for testing purpose), ie, CounterResource.java
   */
  //@Retry(retryOn = Exception.class, maxRetries = 2)
  @Fallback(fallbackMethod = "fallbackForGet")
  public JsonObject get(String hostname) throws Exception {
    System.out.println(counter);
    System.out.println(systemProp.getFallbackBoolean());
    ++counter;
    if (inv.get(hostname) == null) {
      if (InventoryUtil.responseOk(hostname)) {
        System.out.println("response OK");
        inv.putIfAbsent(hostname, InventoryUtil.getProperties(hostname));
      } else {
        System.out.println("response not ok");
        return JsonMessages.SERVICE_UNREACHABLE.getJson();
      }
    } else if (systemProp.getFallbackBoolean()){
      System.out.println("system property already exist, do fallback");
      throw new Exception("system property already exist in inventory!");
    }
    return null; //should not get here
  }

  public JsonObject fallbackForGet(String hostname) {
    System.out.println("fallback");
    counter = 0; //reset counter
    return inv.get(hostname);
  }

  public void add(String hostname, JsonObject systemProps) {
    inv.putIfAbsent(hostname, systemProps);
  }

  public JsonObject list() {
    JsonObjectBuilder systems = Json.createObjectBuilder();
    inv.forEach((host, props) -> {
      JsonObject systemProps = Json.createObjectBuilder()
                                   .add("os.name", props.getString("os.name"))
                                   .add("user.name",
                                        props.getString("user.name"))
                                   .build();
      systems.add(host, systemProps);
    });
    systems.add("hosts", systems);
    systems.add("total", inv.size());
    return systems.build();
  }

  
  
  public int getCounter() {
    return counter;
  }
  
}
