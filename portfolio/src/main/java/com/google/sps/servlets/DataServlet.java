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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final Logger LOGGER = Logger.getLogger(DataServlet.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    PreparedQuery results = datastore.prepare(query);

    List<Comment> commentsList = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      Key key = entity.getKey();
      String name = (String) entity.getProperty("name");
      String content = (String) entity.getProperty("content");
      int numLikes = (int)(long) entity.getProperty("numLikes");
      Date timestamp = (Date) entity.getProperty("timestamp");

      Comment comment = new Comment(name, content, numLikes, timestamp, KeyFactory.keyToString(key));
      commentsList.add(comment);
    }
    
    String jsonComments = new Gson().toJson(commentsList);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String content = request.getParameter("comment");
    String name = request.getParameter("name");
    String stringifiedKey = request.getParameter("comment-key");

    /* 
      First block executes when a new comment is posted.
      Second block updates an existing comment by incrementing the number of likes.
    */
    if (content != null && name != null && content.length() != 0) {
      name = name.length() == 0 ? "anonymous" : name;
      Date timestamp = new Date();
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("name", name);
      commentEntity.setProperty("content", content);
      commentEntity.setProperty("numLikes", 0);
      commentEntity.setProperty("timestamp", timestamp);
      datastore.put(commentEntity);
    } else if (stringifiedKey != null) {
      // Patch for numLikes (executes when comment is liked).
      Key key = KeyFactory.stringToKey(stringifiedKey);
      try {
        Entity retrievedComment = datastore.get(key);
        long numLikes = (long) retrievedComment.getProperty("numLikes");
        numLikes++;
        retrievedComment.setProperty("numLikes", numLikes);
        datastore.put(retrievedComment);
      } catch (EntityNotFoundException e) {
        LOGGER.log(Level.WARNING, "Entity could not be found in datastore: " + e.getMessage());
      }
    }
    response.sendRedirect("/comments.html");
  }
}
