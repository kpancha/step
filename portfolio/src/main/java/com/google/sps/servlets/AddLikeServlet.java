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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet for adding a like to a specific comment. */
@WebServlet("/add-like")
public class AddLikeServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final Logger LOGGER = Logger.getLogger(AddLikeServlet.class.getName());

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String stringifiedKey = request.getParameter("comment-key");

    Key key = KeyFactory.stringToKey(stringifiedKey);
    try {
      Entity retrievedComment = datastore.get(key);
      int numLikes = (int)(long) retrievedComment.getProperty("numLikes");
      numLikes++;
      retrievedComment.setProperty("numLikes", numLikes);
      datastore.put(retrievedComment);
    } catch (EntityNotFoundException e) {
      LOGGER.log(Level.WARNING, "Entity could not be found in datastore: " + e.getMessage());
    }
    response.sendRedirect("/comments.html");
  }
}
