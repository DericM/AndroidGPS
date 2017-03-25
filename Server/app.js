//app.js
var express = require('express');  
var app = express();  
var server = require('http').createServer(app);  
var io = require('socket.io')(server);
var _ = require('lodash-node');
var fs = require('fs');


var users = [];

app.use(express.static(__dirname + '/node_modules'));  
app.get('/', function(req, res,next) {  
	res.sendFile(__dirname + '/index.html');
});
app.get('/locations', function(req, res,next) { 

	var data = '';

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

		//socket.emit('connect_successful', _.pluck(users, ''));
		//socket.broadcast.emit('online', name);
	    console.log('DeviceID : ' + device.id + ', IP : ' + device.ipAddress+ ' connected');
	});
	socket.on('disconnect', function (){
		var index = _.findIndex(users, { socket: socket.id });
		if(index !== -1){
			console.log(users[index].device.id + ' disconnected');

			users.splice(index, 1);
		}
	});
});

server.listen(4200); 
