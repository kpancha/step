// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for adding and retrieving regions on a map. */
@WebServlet("/map-regions")
public class MapRegionsServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private final Gson gson = new Gson();
  private static final Logger LOGGER = Logger.getLogger(MapRegionsServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String region = request.getParameter("region");
    String stringifiedKey = request.getParameter("key");

    Entity regionEntity = new Entity("Region");;
    int numVisits = 0;
    try {
      if (stringifiedKey != null) {
        regionEntity = datastore.get(KeyFactory.stringToKey(stringifiedKey));
        numVisits = (int) (long) regionEntity.getProperty("numVisits");
      }
    } catch (EntityNotFoundException e) {
      LOGGER.log(Level.WARNING, "Entity could not be found in datastore: " + e.getMessage());
    }
    numVisits++;
    regionEntity.setProperty("regionName", region);
    regionEntity.setProperty("numVisits", numVisits);
    datastore.put(regionEntity);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Region");
    PreparedQuery results = datastore.prepare(query);

    // Maps the name of each region to a map containing the number of users who have visited
    // and the key for that region in the database.
    Map<String, Map<String, String>> regionsMap = new HashMap<>();
    Iterable<Entity> resultsIterable = results.asIterable();

    for(Entity entity : resultsIterable) {
      String regionName = (String) entity.getProperty("regionName");
      int numVisits = (int) (long) entity.getProperty("numVisits");
      Key key = entity.getKey();
      Map<String, String> regionsInfo = new HashMap<>();
      regionsInfo.put("numVisits", Integer.toString(numVisits));
      regionsInfo.put("key", KeyFactory.keyToString(key));
      regionsMap.put(regionName, regionsInfo);
    }
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(regionsMap));
  }
}