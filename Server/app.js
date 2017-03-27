//app.js
var express = require('express');  
var app = express();  
var session = require('express-session')({
    secret: 'COMP4985_AndoridGPS',
    resave: true,
    saveUninitialized: true
});
//var sharedsession = require("express-socket.io-session");
var server = require('http').createServer(app);  
var io = require('socket.io')(server);
var _ = require('lodash-node');
var fs = require('fs');
var bodyParser     =        require("body-parser");
//Here we are configuring express to use body-parser as middle-ware.

// Authentication and Authorization Middleware
var auth = function(req, res, next) {
  if (req.session && req.session.athenticated)
    return next();
  else
	res.sendFile(__dirname + '/login.html');
};

var users = [];
var loginData = JSON.parse(fs.readFileSync(__dirname + '/loginData.json', 'utf8'));
console.log(loginData);

app.use(express.static(__dirname + '/node_modules')); 
app.use(session);
app.use(bodyParser.urlencoded());
app.use(bodyParser.json());

//io.use(sharedsession(session));


app.get('/', auth, function(req, res,next) {  
	res.sendFile(__dirname + '/index.html');
});
app.get('/login', function(req, res,next) {  
	res.sendFile(__dirname + '/login.html');
});
app.post('/doLogin',function(req,res){
	if(req.body.length == 0 || !req.body.username || !req.body.password){
		res.sendFile(__dirname + '/login.html');
		return;
	}
	var index = _.findIndex(loginData, { username: req.body.username });
	if(index == -1){
		res.sendFile(__dirname + '/login.html');
		return;
	}
	if(loginData[index].password == req.body.password){
		req.session.athenticated = true;
		res.sendFile(__dirname + '/index.html');
	}else{
		res.sendFile(__dirname + '/login.html');
	}
});
app.get('/locations', function(req, res,next) { 

	var data = '';

	if (!fs.existsSync(__dirname + '/locations.json')) { 
		fs.writeFileSync(__dirname + '/locations.json', '[]');
	} 
	var readStream = fs.createReadStream(__dirname + '/locations.json', 'utf8');

	readStream.on('data', function(chunk) {  
		data += chunk;
	}).on('end', function() {
		res.json(data);
	});
});
app.get('/:file', function(req, res, next) {  
	res.sendFile(__dirname + '/' + req.params.file );
});
app.get('/:dir/:file', function(req, res, next) {  
	console.log(req.params.file);
	res.sendFile(__dirname + '/' + req.params.dir + '/' + req.params.file);
});

io.on('connection', function (socket) {
	socket.on('connected', function (device) {
		// if this socket is already connected,
		// send a failed login message
		if (_.findIndex(users, { socket: socket.id }) !== -1) {
			socket.emit('connect_error', 'You are already connected.');
		}

	    if (_.findIndex(users.device, { id: device.id }) !== -1) {
			socket.emit('connect_error', 'This name already exists.');
		    return; 
		}

	    users.push({ 
		    device: device,
		    socket: socket.id
		});
	});

	socket.on('disconnect', function (){
		var index = _.findIndex(users, { socket: socket.id });
		if(index !== -1){
			console.log(users[index].username + ' disconnected');
			users.splice(index, 1);
		}
	});

	socket.on("login", function(userdata){
		console.log("login");
		if(!userdata.username || !userdata.password){
			socket.emit('login_error', 'Uername and password is not received.');
			return;
		}

		var index = _.findIndex(loginData, { username: userdata.username });
		if(index == -1){
			socket.emit('login_error', 'Uername is not registered.');
			return;
		}
		if(loginData[index].password != userdata.password){
			socket.emit('login_error', 'Password is not correct.');
			return;
		}

		if (_.findIndex(users, { socket: socket.id }) !== -1) {
			socket.emit('login_error', 'You are already connected.');
			retrun;
		}

	    if (_.findIndex(users.username, { username: userdata.username }) !== -1) {
			socket.emit('login_error', 'You are already logged in.');
		    return; 
		}

	    users.push({ 
		    username: userdata.username,
		    socket: socket.id
		});
		socket.emit('login_success', 'Succefully logged in.');
		console.log("login_success");
	});

	socket.on('location', function (data){
		console.log("location received");
		var index = _.findIndex(users, { socket: socket.id });
		if(index == -1){
			console.log('Connection lost, reconnect.');
		}
		
		var deviceID  = data.deviceID;
		var deviceIP  = data.deviceIP;
		var username  =	data.username;
		var latitude  = data.latitude;
		var longitude = data.longitude;
		var time      = data.time;
		//var ip        = location.ip;
		
		if (!fs.existsSync(__dirname + '/locations.json')) { 
        	fs.writeFileSync(__dirname + '/locations.json', '[]');
		} 
		var file = fs.readFileSync(__dirname + '/locations.json');
		var object = JSON.parse(file);
		var index = _.findIndex(object, { username: data.username });
		var newLocation = {
			"deviceID":deviceID,
			"deviceIP":deviceIP,
			"latitude":latitude,
			"longitude":longitude,
			"time":time
		};
		if(index == -1){
			object.push(
				{
					"username" : username,
					"locations" : [newLocation]
				}
			);
		}else{
			object[index].locations.push(newLocation);
		}
		var newJson = JSON.stringify(object);
		fs.writeFileSync('./locations.json', newJson);
	});
});

server.listen(4200); 
