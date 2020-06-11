/**
 * Loads Google Charts and draws chart on callback
 */
function loadGCharts() {
  google.charts.load('current', {'packages':['geochart'], 'mapsApiKey': 'AIzaSyBBqtlu5Y3Og7lzC1WI9SFHZr2gJ4iDdTc'});
  google.charts.setOnLoadCallback(drawChart);
}

/** Creates a chart and adds it to the page. */
function drawChart() {
  const data = new google.visualization.arrayToDataTable([
    ['Country', 'Popularity'],
    ['Germany', 200],
    ['United States', 300],
    ['Brazil', 400],
    ['Canada', 500],
    ['France', 600],
    ['RU', 700]
  ]);

  const options = {};

  const chart = new google.visualization.GeoChart(
      document.getElementById('chart-container'));
  google.visualization.events.addListener(chart, 'regionClick', (event) => {
    document.getElementById('region-name').innerHTML = event.region;
  });
  chart.draw(data, options);
}