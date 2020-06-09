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

/**
 * Cycles through images automatically.
 */
function slideShow(imgIndex = 0) {

  const slides = document.getElementsByClassName('my-slides');
  
  // If there are no images, end function.
  if (slides.length === 0) {
    return;
  }
  
  for (let slide of slides) {
    slide.style.display = 'none';
  }
  
  // Ensure image index is in bounds.
  if (imgIndex > slides.length - 1) {
    imgIndex = 0;
  }
  
  slides[imgIndex].style.display = '';

  // Delay next function call so image is displayed for 5 seconds.
  imgIndex++;
  setTimeout(function() {
    slideShow(imgIndex);
  }, 5000);

}

/**
 * Displays only courses that are part of a specified class.
 */
function filterCourseDisplay(courseType = 'course') {

  const allCourses = document.getElementsByClassName('course');

  // Display courses in courseType class and hide the rest.
  for (let course of allCourses) {
    if (course.classList.contains(courseType)) {
        course.style.display = '';
    } else {
        course.style.display = 'none';
    }
  }
}

/**
 * Fetch comments from server and display on DOM.
 */
function loadComments() {
  const maxCommentsPerPage = parseInt(document.getElementById('max-num-comments').value) || Number.MAX_VALUE;
  const sortOrder = document.getElementById('sort-order').value;
  let url = '/list-comments?sort-order=' + sortOrder;
  fetch(url)
    .then(response => response.json())
    .then((data) => {
      showNextNComments(data, /* startInd= */ 0, maxCommentsPerPage);
    });
}

/**
 * Display a specified amount of messages starting at a certain index.
 */
function showNextNComments(allComments, startInd, maxNumDisplayed) {
  const display = document.getElementById('comment-container');
    display.innerHTML = '';
    
    for (let i = startInd; i < allComments.length && i < startInd + maxNumDisplayed; i++) {
      const comment = allComments[i];

      const likeButton = createLikeButton();
      likeButton.addEventListener('click', () => sendLike(comment));

      const deleteButton = createDeleteButton();
      deleteButton.addEventListener('click', () => deleteComment(comment));

      const commentElement = createCommentElement(comment);
      commentElement.appendChild(likeButton);
      commentElement.appendChild(deleteButton);
      display.appendChild(commentElement);
    }

    const remainingLeft = startInd > 0;
    const leftButton = createNextButton(/* direction= */ 'l', /* isValid= */ remainingLeft);
    if (remainingLeft) {
      leftButton.addEventListener('click', () => 
          showNextNComments(allComments, startInd - maxNumDisplayed, maxNumDisplayed))
    }
    const remainingRight = startInd + maxNumDisplayed < allComments.length;
    const rightButton = createNextButton(/* direction= */ 'r', /* isValid= */ remainingRight);
    if (remainingRight) {
      rightButton.addEventListener('click', () => 
          showNextNComments(allComments, startInd + maxNumDisplayed, maxNumDisplayed))
    }
    display.appendChild(leftButton);
    display.appendChild(rightButton);
}

/**
 * Send request to delete a specific comment.
 */
function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('comment-key', comment.key);
  fetch('/delete-comment', {method: 'POST', body: params})
    .then(() => loadComments());
}

/**
 * Sends a post request to increment number of likes on a comment.
 */
function sendLike(comment) {
  const params = new URLSearchParams();
  params.append('comment-key', comment.key);
  fetch('/add-like', {method: 'POST', body: params})
    .then(() => loadComments());
}

/**
 * Create HTML display for one comment.
 */
function createCommentElement(comment) {
  const commentContainer = document.createElement('div');
  const extraLineBreak = document.createElement('br');

  const nameElement = document.createElement('p');
  nameElement.innerHTML = 'Posted by: ' + comment.name;

  const contentElement = document.createElement('p');
  contentElement.innerHTML = comment.content;

  const likeElement = document.createElement('p');
  likeElement.innerHTML = comment.numLikes + ' likes';

  const timeElement = document.createElement('p');
  timeElement.innerHTML += 'Posted at ' + comment.timestamp;

  commentContainer.appendChild(extraLineBreak);
  commentContainer.appendChild(nameElement);
  commentContainer.appendChild(contentElement);
  commentContainer.appendChild(likeElement);
  commentContainer.appendChild(timeElement);

  return commentContainer;
}

/**
 * Create a like button for a comment.
 */
function createLikeButton() {
  const likeButton = document.createElement('button');
  likeButton.innerHTML = 'Like';
  likeButton.className = 'btn btn-success btn-sm';
  return likeButton;
}

/**
 * Create a delete button for a comment.
 */
function createDeleteButton() {
  const deleteButton = document.createElement('button');
  deleteButton.innerHTML = 'Delete';
  deleteButton.className = 'btn btn-danger btn-sm';
  return deleteButton;
}

/**
 * Create a next button to display more comments.
 */
function createNextButton(direction, isValid) {
  const nextButton = document.createElement('button');
  nextButton.className = isValid ? 'btn btn-secondary' : 'btn btn-secondary disabled';
  nextButton.innerHTML = direction == 'r' ? '&raquo;' : '&laquo;';
  return nextButton;
}

/**
 * Fetch states mapped to latitudes and longitudes.
 */
function fetchStates() {
  fetch('/random-state').then(response => response.json()).then((states) => {
    displayNextState( /* stateCoordMap= */ states, /* stateInd= */ 0);
  });
}

/**
 * Displays the next state from a JSON Object on the DOM.
 */
function displayNextState(stateCoordMap, stateInd) {
  const stateNames = Object.keys(stateCoordMap);
  const stateElement = document.getElementById('state-name');
  stateElement.innerHTML = '';

  if (stateInd >= stateNames.length) {
    alert('Game Over!');
    stateElement.innerHTML = 'Game Over!';
    return;
  }
  const currState = stateNames[stateInd];
  const currCoords = stateCoordMap[currState];
  createInteractiveMap(currCoords);
  
  const nameDisplay = document.createElement('h6');
  nameDisplay.innerHTML = currState;
  stateElement.appendChild(nameDisplay);

  const nextButton = document.createElement('button');
  nextButton.className = 'btn btn-outline-primary btn-sm';
  nextButton.innerHTML = 'next';
  nextButton.addEventListener('click', () => displayNextState(stateCoordMap, ++stateInd));
  stateElement.appendChild(nextButton);
}

/**
 * Creates a marker on the interactive map.
 */
function createGameMarker(lat, lng, targetCoords, map) {
  let isInBounds = false;
  if (targetCoords != null) {
    const diffLat = Math.abs(lat - parseFloat(targetCoords['lat']));
    const diffLng = Math.abs(lng - parseFloat(targetCoords['lng']));
    isInBounds = diffLat < 0.25 && diffLng < 0.25;
  }
  const greenIcon = 'images/green-icon.png';
  const marker = new google.maps.Marker({position: {lat, lng}, map});
  // If the user clicks on the correct capital, a green location marker is used.
  // Otherwise, the default red marker is used.
  if (isInBounds) {
    marker.setIcon(greenIcon);
    alert('Correct!');
  } else {
    alert("Try again.");
  }
}

/**
 * Creates an interactive map.
 */
function createInteractiveMap(targetCoords=null) {
  const latLngCoords = createLatLng(/* lat= */ 40, /* lng= */ -100);
  const mapTypeControlOptions = {mapTypeIds: ['roadmap', 'satellite']};
  const map = createMap('interactive-map', latLngCoords, /* zoom= */ 4, mapTypeControlOptions);
  
  map.addListener('click', (event) => {
    createGameMarker(event.latLng.lat(), event.latLng.lng(), targetCoords, map);
  });
}

/** 
 * Adds a map to a specific HTML element.
*/
function createMap(mapContainer, center, zoom, mapTypeControlOptions) {
  const map = new google.maps.Map(document.getElementById(mapContainer), {
    center, zoom, mapTypeControlOptions
  });
  return map;
}

/** 
 * Creates a retro themed map with markers and adds it to the page. 
*/
function createStaticMap() {
  const styledMapType = new google.maps.StyledMapType([
      {"elementType": "geometry","stylers": [{"color": "#ebe3cd"}]},
      {"elementType": "labels.text.fill","stylers": [{"color": "#523735"}]},
      {"elementType": "labels.text.stroke","stylers": [{"color": "#f5f1e6"}]},
      {"featureType": "administrative","elementType": "geometry.stroke","stylers": [{"color": "#c9b2a6"}]},
      {"featureType": "administrative.land_parcel","elementType": "geometry.stroke","stylers": [{"color": "#dcd2be"}]},
      {"featureType": "administrative.land_parcel","elementType": "labels.text.fill","stylers": [{"color": "#ae9e90"}]},
      {"featureType": "landscape.natural","elementType": "geometry","stylers": [{"color": "#dfd2ae"}]},
      {"featureType": "poi","elementType": "geometry","stylers": [{"color": "#dfd2ae"}]},
      {"featureType": "poi","elementType": "labels.text.fill","stylers": [{"color": "#93817c"}]},
      {"featureType": "poi.park","elementType": "geometry.fill","stylers": [{"color": "#a5b076"}]},
      {"featureType": "poi.park","elementType": "labels.text.fill","stylers": [{"color": "#447530"}]},
      {"featureType": "road","elementType": "geometry","stylers": [{"color": "#f5f1e6"}]},
      {"featureType": "road.arterial","elementType": "geometry","stylers": [{"color": "#fdfcf8"}]},
      {"featureType": "road.highway","elementType": "geometry","stylers": [{"color": "#f8c967"}]},
      {"featureType": "road.highway","elementType": "geometry.stroke","stylers": [{"color": "#e9bc62"}]},
      {"featureType": "road.highway.controlled_access","elementType": "geometry","stylers": [{"color": "#e98d58"}]},
      {"featureType": "road.highway.controlled_access","elementType": "geometry.stroke","stylers": [{"color": "#db8555"}]},
      {"featureType": "road.local","elementType": "labels.text.fill","stylers": [{"color": "#806b63"}]},
      {"featureType": "transit.line","elementType": "geometry","stylers": [{"color": "#dfd2ae"}]},
      {"featureType": "transit.line","elementType": "labels.text.fill","stylers": [{"color": "#8f7d77"}]},
      {"featureType": "transit.line","elementType": "labels.text.stroke","stylers": [{"color": "#ebe3cd"}]},
      {"featureType": "transit.station","elementType": "geometry","stylers": [{"color": "#dfd2ae"}]},
      {"featureType": "water","elementType": "geometry.fill","stylers": [{"color": "#b9d3c2"}]},
      {"featureType": "water","elementType": "labels.text.fill","stylers": [{"color": "#92998d"}]}],
      {name: 'Retro Map'});
  
  const latLngCoords = createLatLng(/* lat= */ 40, /* lng= */ -100);
  const mapTypeControlOptions = {mapTypeIds: ['roadmap', 'satellite', 'styled_map']};

  const map = createMap('map', latLngCoords, /* zoom= */ 2, mapTypeControlOptions);
  map.mapTypes.set('styled_map', styledMapType);
  map.setMapTypeId('styled_map');

  // Set markers with corresponding icons
  const placesLivedMarkers = getPlacesLivedMarkers();
  const houseIcon = 'images/house.svg'
  setMarkers(map, placesLivedMarkers, houseIcon);

  const travelMarkers = getTravelMarkers();
  setMarkers(map, travelMarkers);

  const danceLocationMarkers = getDanceCompMarkers();
  const danceIcon = 'images/dancer.svg';
  setMarkers(map, danceLocationMarkers, danceIcon);
}

/**
 * Puts all markers from an array onto a map with the same icon.
 */
function setMarkers(map, markers, icon=null) {
  for (let marker of markers) {
    marker.setIcon(icon);
    marker.setMap(map);
  }
}

/**
 * Returns a Marker with a given LatLng position and title.
 */
function createMarker(position, title, infoWindow) {
  const marker = new google.maps.Marker({position, title});
  marker.addListener('click', function() {
    infoWindow.open(map, marker);
  });
  return marker;
}

/**
 * Returns a LatLng element with a specific latitude and longitude.
 */
function createLatLng(lat, lng) {
  return new google.maps.LatLng({lat, lng});
}

/**
 * Returns an InfoWindow element with content.
 */
function createInfoWindow(content) {
  return new google.maps.InfoWindow({content});
}

/**
 * Returns an array of markers for places lived.
 */
function getPlacesLivedMarkers() {
  const placesLivedMarkers = [];

  const montyLatlng = createLatLng(/* lat= */ 40.44, /* lng= */ -74.66);
  const montyInfo = createInfoWindow('I\'ve lived in Montgomery since I was 7.');
  const montyMarker = createMarker(montyLatlng, 'My hometown!', montyInfo);
  placesLivedMarkers.push(montyMarker);

  const gtLatlng = createLatLng(/* lat= */ 33.78, /* lng= */ -84.40);
  const gtInfo = createInfoWindow('I am a 3rd year at Georgia Tech.');
  const gtMarker = createMarker(gtLatlng, 'Go jackets', gtInfo);
  placesLivedMarkers.push(gtMarker);

  const sfLatlng = createLatLng(/* lat= */ 37.77, /* lng= */ -122.42);
  const sfInfo = createInfoWindow('I lived with my cousins in SF during Summer 2019.');
  const sfMarker = createMarker(sfLatlng, 'I ran a half marathon here!', sfInfo);
  placesLivedMarkers.push(sfMarker);

  return placesLivedMarkers;
}

/**
 * Returns an array of markers for places traveled to.
 */
function getTravelMarkers() {
  const travelMarkers = []

  const rehobothLatlng = createLatLng(/* lat= */ 38.72, /* lng= */ -75.08);
  const rehobothInfo = createInfoWindow('I love to go to my grandparents\' beach house.');
  const rehobothMarker = createMarker(rehobothLatlng, 'My favorite beach', rehobothInfo);
  travelMarkers.push(rehobothMarker);

  const vermontLatlng = createLatLng(/* lat= */ 44.59, /* lng= */ -72.79);
  const vermontInfo = createInfoWindow('My cousins live in Vermont.');
  const vermontMarker = createMarker(vermontLatlng, 'The BEST skiing slopes', vermontInfo);
  travelMarkers.push(vermontMarker);

  const drLatlng = createLatLng(/* lat= */ 18.74, /* lng= */ -70.16);
  const drInfo = createInfoWindow(
      'I\'ve been here twice! Punta Cana for my 15th birthday and ' + 
      'Monte Cristi for a service trip with Outreach360.'
  );
  const drMarker = createMarker(drLatlng, 'My first solo international flight', drInfo);
  travelMarkers.push(drMarker);

  const torontoLatlng = createLatLng(/* lat= */ 43.65, /* lng= */ -79.38);
  const torontoInfo = createInfoWindow('We went here after going to Niagra Falls.');
  const torontoMarker = createMarker(torontoLatlng, 'Climbed the CNN tower!', torontoInfo);
  travelMarkers.push(torontoMarker);

  const costaRicaLatlng = createLatLng(/* lat= */ 9.75, /* lng= */ -83.75);
  const costaRicaInfo = createInfoWindow(
      'My Girl Scout troop went on a trip to Costa Rica in high school.' + 
      'We stayed in a lodge in the rainforest that was only accessible by raft!'
  );
  const costaRicaMarker = createMarker(costaRicaLatlng, 'Pura vida', costaRicaInfo);
  travelMarkers.push(costaRicaMarker);

  const caymanLatlng = createLatLng(/* lat= */ 19.32, /* lng= */ -81.24);
  const caymanInfo = createInfoWindow(
      'My mom and I went to Grand Cayman for a weekend for my 13th birthday.'
  );
  const caymanMarker = createMarker(caymanLatlng, 'The clearest water I\'ve ever seen!', caymanInfo);
  travelMarkers.push(caymanMarker);

  const alaskaLatlng = createLatLng(/* lat= */ 64.20, /* lng= */ -149.49);
  const alaskaInfo = createInfoWindow(
      'Alaska is my favorite U.S. state I\'ve been to!' + 
      ' We went to Denali, Anchorage, Skagway, and Juneau.'
  );
  const alaskaMarker = createMarker(alaskaLatlng, 'My first and only cruise', alaskaInfo);
  travelMarkers.push(alaskaMarker);

  const chennaiLatlng = createLatLng(/* lat= */ 13.08, /* lng= */ 80.27);
  const chennaiInfo = createInfoWindow(
      'My family tries to go to India every other year.' + 
      ' We stay in the same house that my dad grew up in.'
  );
  const chennaiMarker = createMarker(chennaiLatlng, 'I\'ve been here 8 times!', chennaiInfo);
  travelMarkers.push(chennaiMarker);

  const beijingLatlng = createLatLng(/* lat= */ 39.90, /* lng= */ 116.41);
  const beijingInfo = createInfoWindow(
      '7-year-old Kira wanted to take a slide down from the Great Wall,' +
      'but we ended up taking the steps. How boring.'
  );
  const beijingMarker = createMarker(beijingLatlng, 'We were here a few months before the Olympics', beijingInfo);
  travelMarkers.push(beijingMarker);

  const shanghaiLatlng = createLatLng(/* lat= */ 31.23, /* lng= */ 121.47);
  const shanghaiInfo = createInfoWindow('My favorite parts of Shanghai were the Pearl Tower, Maglev, and cable cars.');
  const shanghaiMarker = createMarker(shanghaiLatlng, 'We rode the fastest train in the world', shanghaiInfo);
  travelMarkers.push(shanghaiMarker);

  const hongKongLatlng = createLatLng(/* lat= */ 22.32, /* lng= */ 114.17);
  const hongKongInfo = createInfoWindow('I had the best dim sum of my life right after landing in Hong Kong.');
  const hongKongMarker = createMarker(hongKongLatlng, 'The most jetlagged I\'ve ever been', hongKongInfo);
  travelMarkers.push(hongKongMarker);

  const dubaiLatlng = createLatLng(/* lat= */ 25.20, /* lng= */ 55.27);
  const dubaiInfo = createInfoWindow('We got stuck here for a few days during a layover because of Hurricane Irene.');
  const dubaiMarker = createMarker(dubaiLatlng, 'It was 110 degrees outside!', dubaiInfo);
  travelMarkers.push(dubaiMarker);

  const parisLatlng = createLatLng(/* lat= */ 48.85, /* lng= */ 2.35);
  const parisInfo = createInfoWindow(
      'We were only in Paris for a weekend, but it happened to be the weekend of Bastille Day and ' +
      'the World Cup final! We walked about 10 miles each day while we were there.'
  );
  const parisMarker = createMarker(parisLatlng, 'I was here the day France won the world cup!', parisInfo);
  travelMarkers.push(parisMarker);

  const bordeauxLatlng = createLatLng(/* lat= */ 44.84, /* lng= */ 0.58);
  const bordeauxInfo = createInfoWindow('We went to this beautiful city for a week the summer before I left for college.');
  const bordeauxMarker = createMarker(bordeauxLatlng, 'Donna traveled here in Mamma Mia 2', bordeauxInfo);
  travelMarkers.push(bordeauxMarker);

  const barcaLatlng = createLatLng(/* lat= */ 41.38, /* lng= */ 2.17);
  const barcaInfo = createInfoWindow('My favorite part of Barcelona was the Sagrada Familia.');
  const barcaMarker = createMarker(barcaLatlng, 'The sun didn\'t come out the whole trip :(', barcaInfo);
  travelMarkers.push(barcaMarker);

  const switzLatlng = createLatLng(/* lat= */ 46.82, /* lng= */ 8.23);
  const switzInfo = createInfoWindow(
      'I travelled to Switzerland once with my family and then 12 years later on a school trip!' +
      ' It is one of the most beautiful places I have ever been to.'  
  );
  const switzMarker = createMarker(switzLatlng, 'I got to go here twice!', switzInfo);
  travelMarkers.push(switzMarker);

  const portugalLatlng = createLatLng(/* lat= */ 39.40, /* lng= */ -8.22);
  const portugalInfo = createInfoWindow(
      'I was 6 when we went to Portugal. All I remember was the bakery on the beach' + 
      ' where we would get these delicious chocolate croissants.'
  );
  const portugalMarker = createMarker(portugalLatlng, 'I accidentally nearly drowned my uncle here', portugalInfo);
  travelMarkers.push(portugalMarker);

  return travelMarkers;
}

/**
 * Returns an array of markers for dance competition locations.
 */
function getDanceCompMarkers() {
  const danceLocationMarkers = [];

  const dallasLatlng = createLatLng(/* lat= */ 32.99, /* lng= */ -96.75);
  const dallasInfo = createInfoWindow('My first dance competition was at UT Dallas.');
  const dallasMarker = createMarker(dallasLatlng, 'Aaja Nachle', dallasInfo);
  danceLocationMarkers.push(dallasMarker);

  const minneapolisLatlng = createLatLng(/* lat= */ 44.97, /* lng= */ -93.23);
  const minnapolisInfo = createInfoWindow('Minneapolis is my favorite place I have been for dance.');
  const minneapolisMarker = createMarker(minneapolisLatlng, 'Jazba', minnapolisInfo);
  danceLocationMarkers.push(minneapolisMarker);

  const marylandLatlng = createLatLng(/* lat= */ 38.99, /* lng= */ -76.94);
  const marylandInfo = createInfoWindow('My parents and best friend came to watch me perform at this comp!');
  const marylandMarker = createMarker(marylandLatlng, 'Minza', marylandInfo);
  danceLocationMarkers.push(marylandMarker);

  const scLatlng = createLatLng(/* lat= */ 33.99, /* lng= */ -81.03);
  const scInfo = createInfoWindow('Our car broke down on the way to this comp.');
  const scMarker = createMarker(scLatlng, 'Aag Ki Raat', scInfo);
  danceLocationMarkers.push(scMarker);

  return danceLocationMarkers;
}