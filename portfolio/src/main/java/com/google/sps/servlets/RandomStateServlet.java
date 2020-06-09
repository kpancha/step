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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/** Handles fetching a random state and its capital. */
@WebServlet("/random-state")
public class RandomStateServlet extends HttpServlet {

  private Map<String,Map<String, Double>> stateCapitalCoords = new HashMap<>();
  private Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    readStateCoords();
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(stateCapitalCoords));
  }

  private void readStateCoords() throws IOException, ServletException {
    InputStream stateCoords = getServletContext().getResourceAsStream("/WEB-INF/state_capitals.csv");
    InputStreamReader inputReader = new InputStreamReader(stateCoords);
    BufferedReader reader = new BufferedReader(inputReader);
    String data;
    while ((data = reader.readLine()) != null) {
      String[] dataSegments = data.split(",");
      String stateName = dataSegments[0];
      double lat = Double.parseDouble(dataSegments[1]);
      double lng = Double.parseDouble(dataSegments[2]);
      Map<String, Double> coords = new HashMap<>();
      coords.put("lat", lat);
      coords.put("lng", lng);
      stateCapitalCoords.put(stateName, coords);
    }
  }
}