document.getElementById("buildButton").disabled = true;
document.getElementById("status").innerHTML = "Connecting to server...";

var port = chrome.runtime.connect({name: "popup"});

port.onMessage.addListener(function(response) {
	if (response.open) {
		document.getElementById("buildButton").disabled = false;
		document.getElementById("status").innerHTML = "Connected to server.";
	}
	
	if (response.error) {
		document.getElementById("status").innerHTML = "Unable to connect to server.";
	}
	
	if (response.message) {
		console.log(response.message);
	}
});

buildButton.addEventListener("click", async () => {
	let [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
	port.postMessage({payload: tab.url});
});