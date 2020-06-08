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

/** Creates a map and adds it to the page. */
function createMap() {
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
  
  const latLngCoords = new google.maps.LatLng({lat: 40.4417, lng: -74.6829});

  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: latLngCoords, zoom: 2, mapTypeControlOptions: {
          mapTypeIds: ['roadmap', 'satellite', 'styled_map']
        }
      }
    );
  map.mapTypes.set('styled_map', styledMapType);
  map.setMapTypeId('styled_map');
}