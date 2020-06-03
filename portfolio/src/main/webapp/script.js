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
  fetch('/list-comments')
  .then(response => response.json())
  .then((data) => {
    const display = document.getElementById('content');
    for (let comment of data) {
      display.appendChild(createCommentElement(comment));
    }
  });
}

/**
 * Create HTML display for one comment.
 */
function createCommentElement(comment) {
  const commentContainer = document.createElement('div');

  const commentText = document.createElement('p');
  commentText.innerHTML += 'Posted by: ' + comment.name + '<br>';
  commentText.innerHTML += comment.content + '<br>';
  commentText.innerHTML += comment.numLikes + ' likes<br>';
  commentText.innerHTML += 'Posted at ' + comment.timestamp;

  const likeButton = createLikeButton(comment.key);

  commentContainer.appendChild(commentText);
  commentContainer.appendChild(likeButton);
  commentContainer.innerHTML += '<br><br>';

  return commentContainer;
}

/**
 * Create a like button for a comment given the comment's unique key.
 */
function createLikeButton(key) {
  const likeForm = document.createElement('form');
  likeForm.action = '/add-like';
  likeForm.method = 'POST';

  const keyInput = document.createElement('input');
  keyInput.type = 'hidden';
  keyInput.name = 'comment-key';
  keyInput.value = key;

  const likeButton = document.createElement('input');
  likeButton.type = 'submit';
  likeButton.value = "Like";
  likeButton.className = 'btn btn-danger btn-sm';

  likeForm.appendChild(keyInput);
  likeForm.appendChild(likeButton);

  return likeForm;
}
