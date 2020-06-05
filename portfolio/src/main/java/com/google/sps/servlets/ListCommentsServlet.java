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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that lists existing comments. */
@WebServlet("/list-comments")
public class ListCommentsServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String[] sortOption = request.getParameter("sort-order").split("-");
    String sortByAttr = sortOption[0];
    SortDirection direction =
        sortOption[1].equals("ascending") ? SortDirection.ASCENDING : SortDirection.DESCENDING;

    Query query = new Query("Comment").addSort(sortByAttr, direction);

    PreparedQuery results = datastore.prepare(query);

    List<Comment> commentsList = new ArrayList<>();
    FetchOptions defaults = FetchOptions.Builder.withDefaults();
    int resultsLength = results.countEntities(defaults);
    List<Entity> resultsList = results.asList(defaults);

    for (int i = 0; i < resultsLength; i++) {
      Entity entity = resultsList.get(i);
      Key key = entity.getKey();
      String name = (String) entity.getProperty("name");
      String content = (String) entity.getProperty("content");
      int numLikes = (int) (long) entity.getProperty("numLikes");
      Date timestamp = (Date) entity.getProperty("timestamp");

      Comment comment =
          new Comment(name, content, numLikes, timestamp, KeyFactory.keyToString(key));
      commentsList.add(comment);
    }

    String jsonComments = new Gson().toJson(commentsList);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }
}
