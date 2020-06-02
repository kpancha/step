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
 * Fetch data from server and display on DOM.
 */
function getAndDisplayComments() {
  fetch('/data')
  .then(response => response.json())
  .then((data) => {
    // Display each comment in a block with a like button .
    for (let comment of data) {
      const commentContainer = document.createElement('div');
      commentContainer.class = 'comment-container';
      commentContainer.innerHTML = '';
      commentContainer.innerHTML += 'Posted by: ' + comment.name + '<br>';
      commentContainer.innerHTML += comment.content + '<br>';
      commentContainer.innerHTML += comment.numLikes + ' likes<br>';
      commentContainer.innerHTML += 'Posted at ' + comment.timestamp + '<br>';
      // TODO: use methods to create button.
      const likeButtonHTML = '<form action="/data" method="POST">' + 
        '<input type="hidden" name="comment-id" value="' + comment.id + '" />' + 
        '<input type="submit" value="Like" class="btn btn-danger btn-sm" /></form>';
      commentContainer.innerHTML += likeButtonHTML;
      commentContainer.innerHTML += '<br><br>';
      document.getElementById('content').appendChild(commentContainer);
    }
  });
}