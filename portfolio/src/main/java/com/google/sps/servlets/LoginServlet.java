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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
/** Handles logging in and out. */
public class LoginServlet extends HttpServlet {
  
  private Map<String, String> userInfo = new HashMap<>();
  private final Gson gson = new Gson();
  private final UserService userService = UserServiceFactory.getUserService();
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String logoutUrl = userService.createLogoutURL("/comments.html");
      userInfo.put("userEmail", userEmail);
      userInfo.put("redirectUrl", logoutUrl);
      response.getWriter().println(gson.toJson(userInfo));
    } else {
      String loginUrl = userService.createLoginURL("/comments.html");
      userInfo.put("userEmail", "");
      userInfo.put("redirectUrl", loginUrl);
      response.getWriter().println(gson.toJson(userInfo));
    }
    //response.sendRedirect("/comments.html");
  }
}