var tabsUrl = {};

chrome.runtime.onConnect.addListener(function(port) {
	if (port.name === "search") {
		port.onMessage.addListener(function(message) {
			url = tabsUrl[message.id];
			
			var socket = new WebSocket("ws://localhost:8888/ws");
			
			socket.addEventListener("open", function(event) {
				socket.addEventListener("message", function(event) {
					port.postMessage(event.data);
				});
				message.url = url;
				socket.send(JSON.stringify(message));
			});
			
			socket.addEventListener("error", function(event) {
				port.postMessage("Error: cannot connect to server");
			});
		});
	}
});

chrome.runtime.onMessage.addListener(function(message, sender, sendResponse) {
	if ("url" in message) { // Message from popup.js
		chrome.tabs.create({ url: chrome.runtime.getURL("search.html") }, function(tab) {
				tabsUrl[tab.id] = message.url;
		});
	}
	else { // Message from search.js
		sendResponse({id: sender.tab.id, url: tabsUrl[sender.tab.id]}); 
	}
});


chrome.tabs.onRemoved.addListener(function(tabId, removeInfo) {
	console.log("On Removed: " + tabId);
	if (tabId in tabsUrl) {
		delete tabsUrl[tabId];
	}
});