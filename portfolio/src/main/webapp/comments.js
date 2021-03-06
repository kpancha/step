/**
 * Fetch login status from server and return JSON response.
 */
async function fetchLoginStatus() {
  const response = await fetch('/login');
    return await response.json();
}

/**
 * Display login or log out button depending on login status.
 */
function getLoginStatusAndButton() {
  fetchLoginStatus().then((user) => {
    if (user.userEmail) {
      displayCommentInputField(user.userEmail);
      displayLoginButton(user.redirectUrl, /* logout= */ true);
    } else {
      displayLoginButton(user.redirectUrl);
    }
  });
}

/**
 * Displays a button that redirects to login page.
 */
function displayLoginButton(redirectUrl, logout=false) {
  const loginContainer = document.getElementById('login-container');
  loginContainer.innerHTML = '';
  const loginButton = document.createElement('a');
  loginButton.type = 'button';
  loginButton.className = logout ? 
      'btn btn-outline-primary btn-sm' : 'btn btn-outline-primary';
  loginButton.innerHTML = logout ? 'Log out' : 'Log in to leave a comment';
  loginButton.href = redirectUrl;
  loginContainer.appendChild(loginButton);
}

/**
 * Displays text area to leave comment.
 */
function displayCommentInputField(userEmail) {
  const commentFormContainer = document.getElementById('comment-form');
  commentFormContainer.innerHTML = '';
  const breakElement = document.createElement('br');

  const commentForm = document.createElement('form');
  commentForm.className = 'center';
  commentForm.action = '/new-comment';
  commentForm.method = 'POST';

  const commentTextArea = document.createElement('textarea');
  commentTextArea.id = 'comment-input';
  commentTextArea.name = 'comment';
  commentTextArea.rows = 3;
  commentTextArea.required = true;

  const commentLabel = document.createElement('label');
  commentLabel.htmlFor = 'comment-input';
  commentLabel.innerHTML = 'Leave a comment!';

  const nameTextArea = document.createElement('textarea');
  nameTextArea.id = 'name-input';
  nameTextArea.name = 'name';
  nameTextArea.rows = 1;
  nameTextArea.placeholder = 'Your Name (Optional)';

  const nameLabel = document.createElement('label');
  nameLabel.htmlFor = 'name-input';
  nameLabel.hidden = true;
  nameLabel.innerHTML = 'Your Name (Optional)';

  const emailParam = document.createElement('input');
  emailParam.hidden = true;
  emailParam.name = 'email';
  emailParam.value = userEmail;

  const submitButton = document.createElement('button');
  submitButton.type = 'submit';
  submitButton.innerHTML = 'Post';
  submitButton.className = 'btn btn-outline-secondary';

  commentForm.appendChild(commentLabel);
  commentForm.appendChild(breakElement);
  commentForm.appendChild(commentTextArea);
  commentForm.appendChild(breakElement.cloneNode(false));
  commentForm.appendChild(nameLabel);
  commentForm.appendChild(nameTextArea);
  commentForm.appendChild(breakElement.cloneNode(false));
  commentForm.appendChild(emailParam);
  commentForm.appendChild(submitButton);
  commentFormContainer.appendChild(commentForm);
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
      const commentElement = createCommentElement(comment);

      fetchLoginStatus().then((user) => {
        if (user.userEmail) {
          const isDisabled = comment.userLikes && comment.userLikes.includes(user.userEmail);
          const likeButton = createLikeButton(isDisabled);
          likeButton.addEventListener('click', () => sendLike(comment, user));
          commentElement.appendChild(likeButton);
          if (user.userEmail == comment.email) {
            const deleteButton = createDeleteButton();
            deleteButton.addEventListener('click', () => deleteComment(comment));
            commentElement.appendChild(deleteButton);
          }
        }
      });

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
function sendLike(comment, user) {
  const params = new URLSearchParams();
  params.append('comment-key', comment.key);
  params.append('user-email', user.userEmail);
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
  nameElement.innerHTML = 'Posted by: ';
  nameElement.innerHTML += comment.name == 'anonymous' && comment.email ? 
      comment.email : comment.name;
  const contentElement = document.createElement('p');
  contentElement.innerHTML = comment.content;

  const likeElement = document.createElement('p');
  likeElement.innerHTML = comment.numLikes + ' &#10084;&#65039;';

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
function createLikeButton(isDisabled=false) {
  const likeButton = document.createElement('button');
  likeButton.innerHTML = 'Like';
  likeButton.className = 'btn btn-success btn-sm';
  likeButton.disabled = isDisabled;
  return likeButton;
}

/**
 * Create a delete button for a comment.
 */
function createDeleteButton() {
  const deleteButton = document.createElement('button');
  deleteButton.innerHTML = 'Delete my comment';
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