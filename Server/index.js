    var map;
    var interval;
    var bounds;
    var users = [];
    var infowindow;
    var markerColor = {"start": "blue", "recent": "red"};
    var lineColor = ["orange", "green", "purple", "gray"];


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

    function addMarker(index, location, username, markerName){
        var marker;
        var pos = new google.maps.LatLng(location.latitude, location.longitude);
        var date = new Date(location.time * 1000);
        var dateString = date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate() + ' ' + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();

        var deviceIP = location.deviceIP;

        marker = new google.maps.Marker({
            position: pos,
            map: map,
            icon: 'http://www.google.com/intl/en_us/mapfiles/ms/micons/' + markerColor[markerName] + '-dot.png'
        });
        google.maps.event.addListener(marker, 'click', (function(marker) {
            return function() {
                infowindow.setContent("<h2>" + markerName + "</h2><b>UserName</b> : " + username + "<br><b>Time</b> : " + dateString + "<br><b>deviceIP</b> : " + deviceIP);
                infowindow.open(map, marker);
            }
        })(marker));
        if(users[index].hasOwnProperty(markerName))
            delete users[index][markerName]; 
        users[index][markerName] = marker;
    }

    function loadLocations(){
      aja()
      .url('locations')
      .on('success', function(params){
        var data = JSON.parse(params);
        for (var i = 0; i < data.length; i++) {
            var userData = data[i];
            var found = -1;

            $.map(users, function(elem, index) {
              if(elem.username == userData.username)
                found = index;
            });

            if(found < 0) {
              var routes = [];

              for(var j = 0; j < userData.locations.length; j++){
                var pos = new google.maps.LatLng(userData.locations[j].latitude, userData.locations[j].longitude);
                bounds.extend(pos);
                routes.push(pos);
              }

              users.push({
                  "username": userData.username,
                  "polyline": new google.maps.Polyline({
                        path: routes,
                        strokeColor: lineColor[i],
                        strokeWeight: 1,
                        map: map
              })})

              addMarker(users.length - 1, userData.locations[0], userData.username, "start");
              if(userData.locations.length>1)
                addMarker(users.length - 1, userData.locations[userData.locations.length - 1], userData.username, "recent");
              map.fitBounds(bounds);
            }
            else{
                var pathLen = users[found].polyline.getPath().getLength();
                if(pathLen < userData.locations.length){
                    for(var k = pathLen; k < userData.locations.length; k++){
                        var pos = new google.maps.LatLng(userData.locations[k].latitude, userData.locations[k].longitude);
                        users[found].polyline.getPath().push(pos);
                    }
                    if(userData.locations.length>1){
                        if(users[found].hasOwnProperty("recent"))
                            users[found].recent.setMap(null);
                        addMarker(found, userData.locations[userData.locations.length - 1], users[i].username, "recent");
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
