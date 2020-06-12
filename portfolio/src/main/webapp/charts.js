/**
 * Loads Google Charts and draws chart on callback
 */
function loadGCharts() {
  google.charts.load('current', {'packages':['geochart'], 'mapsApiKey': 'AIzaSyBBqtlu5Y3Og7lzC1WI9SFHZr2gJ4iDdTc'});
  google.charts.setOnLoadCallback(getRegionsData);
}

/** Fetches number of visiters and key for regions. */
function getRegionsData() {
  const regionsData = [
    ['Country', 'Number of Users Visited'],
  ];
  const regionsKeys = new Map();
  fetch('/map-regions').then(response => response.json()).then((data) => {
    const regions = Object.keys(data);
    for (let region of regions) {
      const currRegionData = [];
      const numVisits = data[region]['numVisits'];
      const key = data[region]['key'];
      regionsKeys.set(region, key);
      currRegionData.push(region);
      currRegionData.push(Number(numVisits));
      regionsData.push(currRegionData);
    };
    drawChart(regionsData, regionsKeys);
  });
}

/** Creates a chart and adds it to the page. */
function drawChart(regionsData, regionsKeys, colors=['OldLace', 'SeaGreen']) {
  const data = new google.visualization.arrayToDataTable(regionsData);
  const options = {colorAxis: {colors}};
  const chart = new google.visualization.GeoChart(document.getElementById('chart-container'));

  google.visualization.events.addListener(chart, 'regionClick', (event) => {
    params = new URLSearchParams();
    params.append('region', event.region);
    const key = regionsKeys.get(event.region);
    if (key) { 
      params.append('key', key); 
    }
    fetch('/map-regions', {method: 'POST', body: params}).then(() => getRegionsData());
  });
  chart.draw(data, options);
  addColorButton(regionsData, regionsKeys, /* currColor= */ colors[1]);
}

/**
 * Adds a button that on click changes the color of the geochart.
 */
function addColorButton(regionsData, regionsKeys, currColor) {
  const colorButton = document.createElement('button');
  colorButton.className = 'btn btn-primary btn-sm';
  colorButton.innerHTML = 'Click to see the map in a different color!';
  const colorOptions = ['Tomato', 'SandyBrown', 'SeaGreen', 'SteelBlue', 'DarkOrchid', 'HotPink', 'Maroon'];
  colorButton.addEventListener('click', () => {
    // Pick a random color from array that is different from the current color.
    const randInd = Math.floor(Math.random()*colorOptions.length);
    const color = colorOptions[randInd] != currColor ? 
        colorOptions[randInd] : colorOptions[(randInd + 1) % colorOptions.length];
    drawChart(regionsData, regionsKeys, ['OldLace', color]);
  });
  const buttonContainer = document.getElementById('color-button');
  buttonContainer.innerHTML = '';
  buttonContainer.appendChild(colorButton);
}