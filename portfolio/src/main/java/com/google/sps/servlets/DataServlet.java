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
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<Comment> commentsList = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String jsonComments = new Gson().toJson(commentsList);
    response.setContentType("application/json;");
    response.getWriter().println(jsonComments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String content = request.getParameter("comment");
    String name = request.getParameter("name");
    
    if (content != null && name != null && content.length() != 0) {
      Comment comment = name.length() == 0 ? new Comment(content) : new Comment(name, content);
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("name", comment.getNAME());
      commentEntity.setProperty("content", comment.getCONTENT());
      commentEntity.setProperty("numLikes", comment.getNumLikes());
      commentEntity.setProperty("timestamp", comment.getTIMESTAMP());
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
      commentsList.add(comment);
    } else {
      // Patch for numLikes (executes when comment is liked).
      int id = Integer.parseInt(request.getParameter("comment-id"));
      int commentInd = getObjectIndex(id);
      if (commentInd >= 0) {
        commentsList.get(commentInd).addLike();
      }
    }
    response.sendRedirect("/comments.html");
  }

  private int getObjectIndex(int id) {
    for (int i = 0; i < commentsList.size(); i++) {
      if (commentsList.get(i).getID() == id) {
        return i;
      }
    }
    return -1;
  }
}
