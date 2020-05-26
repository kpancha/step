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
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Cycles through images automatically
 */
function slideShow(ind = 0) {

  if (ind > 6) {
      ind = 0;
  } 

  // Choose image based on index
  const imgUrl = '/images/places-' + ind + '.jpg';
  const imgElement = document.createElement('img');
  imgElement.src = imgUrl;
  imgElement.style.width = '400px';
  imgElement.style.height = '300px';

  // Remove existing image and add new image to page
  const imgContainer = document.getElementById('image-slideshow-container');
  imgContainer.innerHTML = '';
  imgContainer.appendChild(imgElement);

  // Delay next function call so image is displayed for 5 seconds
  ind++;
  setTimeout(function() {
      slideShow(ind);
  }, 5000);
}