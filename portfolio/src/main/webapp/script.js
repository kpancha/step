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
  const max = parseInt(document.getElementById('max-num-comments').value) || Number.MAX_VALUE;
  const sortOrder = document.getElementById('sort-order').value;
  let url = '/list-comments?sort-order=' + sortOrder;
  fetch(url)
    .then(response => response.json())
    .then((data) => {
      showNextNComments(data, 0, max);
    });
}

/**
 * Display a specified amount of messages starting at a certain index.
 */
function showNextNComments(data, startInd, numComments) {
  const display = document.getElementById('comment-container');
    display.innerHTML = '';
    
    for (let i = startInd; i < data.length && i < startInd + numComments; i++) {
      const comment = data[i];

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
    const leftButton = createNextButton('l', remainingLeft);
    if (remainingLeft) {
      leftButton.addEventListener('click', () => 
          showNextNComments(data, startInd - numComments, numComments))
    }
    const remainingRight = startInd + numComments < data.length;
    const rightButton = createNextButton('r', remainingRight);
    if (remainingRight) {
      rightButton.addEventListener('click', () => 
          showNextNComments(data, startInd + numComments, numComments))
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