/*------------------------------------------------------------------------------------------------------------------
* SOURCE FILE: app.js
*
* PROGRAM: node.js server for AndroidGPS
*
* DATE: March 27, 2017
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
*
* PROGRAMMER: Terry Kang
*
* NOTES:
*   This is the node.js server that that uses socket-io to establish socket connection to client 
*	and receive coordinates of client's locations. Then, server also serves as http server to show the goole map 
*	and plot the coordinates recevied from the clients via express library.
----------------------------------------------------------------------------------------------------------------------*/
var express = require('express');  
var app = express();  
var session = require('express-session')({
    secret: 'COMP4985_AndoridGPS',
    resave: true,
    saveUninitialized: true
});
var server = require('http').createServer(app);  
var io = require('socket.io')(server);
var _ = require('lodash-node');
var fs = require('fs');
var bodyParser = require("body-parser");


app.use(express.static(__dirname + '/node_modules')); 
app.use(session);
app.use(bodyParser.urlencoded());
app.use(bodyParser.json());


/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: auth
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: function(req, res, next)
*
* RETURN: originally requested page if authorized, otherwise, render login page
*
* NOTES:
*   authentication middleware that checks if this session is authorized and if it is, render  originally requested page,
*	otherwise, render login page.
----------------------------------------------------------------------------------------------------------------------*/
var auth = function(req, res, next) {
  if (req.session && req.session.athenticated)
    return next();
  else
	res.sendFile(__dirname + '/login.html');
};

var users = [];
//User's login info (username and password)
var loginData = JSON.parse(fs.readFileSync(__dirname + '/loginData.json', 'utf8'));
console.log(loginData);


/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/', auth, function(req, res,next)
*
* RETURN: index page if authorized, otherwise, render login page
*
* NOTES:
*   Render root page after checking authentication via middleware
----------------------------------------------------------------------------------------------------------------------*/
app.get('/', auth, function(req, res,next) {  
	res.sendFile(__dirname + '/index.html');
});

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/login')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/login', function(req, res,next)
*
* RETURN: render login page
*
* NOTES:
*   Render login page
----------------------------------------------------------------------------------------------------------------------*/
app.get('/login', function(req, res,next) {  
	res.sendFile(__dirname + '/login.html');
});

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/doLogin')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/doLogin', auth, function(req, res,next)
*
* RETURN: render login page
*
* NOTES:
*   Render index page if user access to /doLogin by mistake
----------------------------------------------------------------------------------------------------------------------*/
app.get('/doLogin', auth, function(req, res,next) {  
	res.sendFile(__dirname + '/index.html');
});

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.post('/doLogin')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.post('/doLogin',function(req,res)
*
* RETURN: index page if authorized, otherwise, render login page
*
* NOTES:
*   Receives username and password as parameters via post request and then if recevied username and password match with
*	the login information stored, update session as authorized and then render index page. otherwise, render login page.
----------------------------------------------------------------------------------------------------------------------*/
app.post('/doLogin',function(req,res) {
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

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/locations')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/locations', function(req, res,next)
*
* RETURN: the coordinates data received from clients
*
* NOTES:
*   Called when receives get request of '/locations' via http. Read a json file that stores the coordinates received
*	from the client applications, then returns it.
----------------------------------------------------------------------------------------------------------------------*/
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

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/:file')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/:file', auth, function(req, res, next)
*
* RETURN: the coordinates data received from clients
*
* NOTES:
*   This is called when requested to access file in the server's directory from server web page.
----------------------------------------------------------------------------------------------------------------------*/
app.get('/:file', auth, function(req, res, next) {  
	res.sendFile(__dirname + '/' + req.params.file );
});

/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: app.get('/:dir/:file')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: app.get('/:dir/:file', auth, function(req, res, next)
*
* RETURN: the coordinates data received from clients
*
* NOTES:
*   This is called when requested to access file in the server's directory from server web page.
----------------------------------------------------------------------------------------------------------------------*/
app.get('/:dir/:file', auth, function(req, res, next) {  
	console.log(req.params.file);
	res.sendFile(__dirname + '/' + req.params.dir + '/' + req.params.file);
});


/*------------------------------------------------------------------------------------------------------------------
* FUNCTION: io.on('connection')
*
* DATE: March 27, 2017
* REVISIONS: (Date and Description)
*
* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
* PROGRAMMER: Terry Kang
*
* INTERFACE: io.on('connection', function (socket)
*				socket : the socket information
*
* RETURN: void
*
* NOTES:
*   This is the listener for socket connection via socket.io. This function waits for socket connection from client.
*	After making the socket connection, it waits for and handles all the messages between server and client via the socket.
----------------------------------------------------------------------------------------------------------------------*/
io.on('connection', function (socket) {
	
	/*------------------------------------------------------------------------------------------------------------------
	* FUNCTION: socket.on('disconnect')
	*
	* DATE: March 27, 2017
	* REVISIONS: (Date and Description)
	*
	* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
	* PROGRAMMER: Terry Kang
	*
	* INTERFACE: socket.on('disconnect', function ()
	*
	* RETURN: void
	*
	* NOTES:
	*   Called when user disconnect. Remove the user from the array of current users.
	----------------------------------------------------------------------------------------------------------------------*/
	socket.on('disconnect', function (){
		var index = _.findIndex(users, { socket: socket.id });
		if(index !== -1){
			console.log(users[index].username + ' disconnected');
			users.splice(index, 1);
		}
	});

	/*------------------------------------------------------------------------------------------------------------------
	* FUNCTION: socket.on('login')
	*
	* DATE: March 27, 2017
	* REVISIONS: (Date and Description)
	*
	* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
	* PROGRAMMER: Terry Kang
	*
	* INTERFACE: socket.on('login', function (userdata)
	*				userdata : username and password
	*
	* RETURN: void
	*
	* NOTES:
	*   Called when user requests to login and receives username and password. if recevied username and password match with
	*	the login information stored, save the user data into the array of current user and send success message to the client.
	----------------------------------------------------------------------------------------------------------------------*/
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
		console.log("login_success : " + userdata.username);
	});

	/*------------------------------------------------------------------------------------------------------------------
	* FUNCTION: socket.on('location')
	*
	* DATE: March 27, 2017
	* REVISIONS: (Date and Description)
	*
	* DESIGNER: Jackob Frank / Mark Tattrie / Deric Mccadden
	* PROGRAMMER: Terry Kang
	*
	* INTERFACE: socket.on('location', function (data)
	*				data : locations data (deviceID, deviceIP, username, latitude, longitude, time, accuracy)
	*
	* RETURN: void
	*
	* NOTES:
	*   Called when client sends the coordinates of current location. Stores it into a json file to be used 
	*	for map page to plot the coordinates on the google map.
	----------------------------------------------------------------------------------------------------------------------*/
	socket.on('location', function (newLocation){
		console.log("location received");
		var index = _.findIndex(users, { socket: socket.id });
		if(index == -1){
			console.log('Connection lost, reconnect.');
		}
		
		if (!fs.existsSync(__dirname + '/locations.json')) { 
        	fs.writeFileSync(__dirname + '/locations.json', '[]');
		} 

		var file = fs.readFileSync(__dirname + '/locations.json');
		var object = JSON.parse(file);
		console.log(newLocation);
		var index2 = _.findIndex(object, { username: users[index].username });
		if(index2 == -1){
			object.push(
				{
					"username" : username,
					"locations" : [newLocation]
				}
			);
		}else{
			object[index2].locations.push(newLocation);
		}
		var newJson = JSON.stringify(object);
		fs.writeFileSync('./locations.json', newJson);
	});
});

server.listen(4200); 
