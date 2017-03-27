    var map;
    var interval;
    var bounds;
    var markers = [];
    var infowindow; 

    function loadMap(){
      var styledMap = [{"elementType":"geometry","stylers":[{"hue":"#ff4400"},{"saturation":-68},{"lightness":-4},{"gamma":0.72}]},{"featureType":"road","elementType":"labels.icon"},{"featureType":"landscape.man_made","elementType":"geometry","stylers":[{"hue":"#0077ff"},{"gamma":3.1}]},{"featureType":"water","stylers":[{"hue":"#00ccff"},{"gamma":0.44},{"saturation":-33}]},{"featureType":"poi.park","stylers":[{"hue":"#44ff00"},{"saturation":-23}]},{"featureType":"water","elementType":"labels.text.fill","stylers":[{"hue":"#007fff"},{"gamma":0.77},{"saturation":65},{"lightness":99}]},{"featureType":"water","elementType":"labels.text.stroke","stylers":[{"gamma":0.11},{"weight":5.6},{"saturation":99},{"hue":"#0091ff"},{"lightness":-86}]},{"featureType":"transit.line","elementType":"geometry","stylers":[{"lightness":-48},{"hue":"#ff5e00"},{"gamma":1.2},{"saturation":-23}]},{"featureType":"transit","elementType":"labels.text.stroke","stylers":[{"saturation":-64},{"hue":"#ff9100"},{"lightness":16},{"gamma":0.47},{"weight":2.7}]}];

      var mapOptions = {
          mapTypeControlOptions: {
              mapTypeIds: [google.maps.MapTypeId.ROADMAP, "Styled"] 
          },
          zoom: 14,
          center: new google.maps.LatLng(49.251004, -123.002571),
          streetViewControl: false
      }
      map = new google.maps.Map(document.getElementById('map_div'), mapOptions );

      var styledMapType = new google.maps.StyledMapType(styledMap, {name: "Styled"});
      map.mapTypes.set("Styled", styledMapType);
      map.setMapTypeId('Styled');

      bounds = new google.maps.LatLngBounds();
      infowindow = new google.maps.InfoWindow();
    }
    
    function removeMarkers(){
        //Loop through all the markers and remove
        for (var i = 0; i < markers.length; i++) {
            markers[i].marker.setMap(null);
        }
        markers = [];
    }

    function addMarker(index, lat, long, username){
        var marker;
        var pos = new google.maps.LatLng(lat, long);
        bounds.extend(pos);
        marker = new google.maps.Marker({
            position: pos,
            map: map
        });
        google.maps.event.addListener(marker, 'click', (function(marker) {
            return function() {
                infowindow.setContent(username);
                infowindow.open(map, marker);
            }
        })(marker));
        markers[index].markers.push(marker);
    }

    function loadLocations(){
      aja()
      .url('locations')
      .on('success', function(data){
        var locations = JSON.parse(data);
        console.log(locations[0]);
        for (var i = 0; i < locations.length; i++) {
            var found = -1;
            $.map(markers, function(elem, index) {
              if(elem.username == locations[i].username)
                found = index;
            });
            console.log(found);
            if(found < 0) {
              var index = markers.length;
              console.log(locations[i].locations);
              markers.push({"username": locations[i].username, "markers" : []})
              for(var j = 0; j < locations[i].locations.length; j++){
                addMarker(index, locations[i].locations[j].latitude, locations[i].locations[j].longitude, locations[i].username);
              }
              map.fitBounds(bounds);
            }
            else{
                if(markers[found].markers.length < locations[i].locations.length){
                    for(var k = markers[found].markers.length; k < locations[i].locations.length; k++){
                        addMarker(found, locations[i].locations[k].latitude, locations[i].locations[k].longitude, locations[i].username);
                    }
                }
            }
        }
      })
      .go();
    }

    $(document).ready(function(){
      loadMap();
      loadLocations();
      interval = setInterval(loadLocations,3000); 
    });
