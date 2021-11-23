chrome.runtime.onConnect.addListener(function(port) {
	if (port.name === "popup") {
		console.log("Popup opened.");
		
		var socket = new WebSocket("ws://localhost:8888/ws");
		socket.addEventListener("open", function(event) {
			port.postMessage({open: 1});
		});
		
		socket.addEventListener("error", function (event) {
			port.postMessage({error: 1});
		});
		
		socket.addEventListener("message", function (event) { // Message received from server
			port.postMessage({message: event.data});
		});
		
		port.onMessage.addListener(function(response) {
			if (response.payload) {
				socket.send(response.payload); // Send message to server
			}
		});
		
		port.onDisconnect.addListener(function() {
			console.log("Popup closed.");
			socket.close();
		});
	}
});