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

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that contains HTML for the navigation bar. */
@WebServlet("/navbar")
public class NavBarServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String navbarHTML = "<nav class=\"navbar navbar-expand-lg navbar-light\">" + 
        "<div class=\"navbar-collapse bg-light\" id=\"navbarColor03\">" + 
        "<ul class=\"navbar-nav mr-auto\">";

    // TODO: add page as param in request body
    String currPage = request.getParameter("page");
    String liClass = "\"nav-item\"";
    String[] pages = new String[] {"index", "about", "projects", "courses", "comments"};
    Map<String, String> pageClasses = new HashMap<>();

    pageClasses.put(currPage, liClass + " active");
    for (String page : pages) {
      if (!pageClasses.containsKey(page)) {
        pageClasses.put(page, liClass);
      }
      String pageTitle = page.equals("index") ? "HOME" : page.toUpperCase();
      navbarHTML += "<li class=" + pageClasses.get(page) + ">" +
          "<a class=\"nav-link\" href=\"" + page + ".html\">" + pageTitle + "</a></li>";
    }
    
    navbarHTML += "<div class=\"icon-bar\">" +
        "<a href=\"https://github.com/kpancha\" class=\"github\"><i class=\"fa fa-github\"></i></a>" +
        "<a href=\"mailto:kirakpancha@gmail.com\" class=\"email\"><i class=\"fa fa-envelope\"></i></a>" +
        "<a href=\"https://www.linkedin.com/in/kira-pancha-7ab886190/\" class=\"linkedin\"><i class=\"fa fa-linkedin\"></i></a>" +
        "</div></div></nav>";
    
    response.setContentType("text/html;");
    response.getWriter().println(navbarHTML);
  }
}