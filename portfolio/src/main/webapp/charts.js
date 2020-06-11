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
function drawChart(regionsData,regionsKeys) {
  const data = new google.visualization.arrayToDataTable(regionsData);
  const options = {};
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
}
