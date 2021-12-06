var id;
var port;

chrome.runtime.sendMessage({}, function(response) {
	id = response.id;
	port = chrome.runtime.connect({name: "search"});
	port.onMessage.addListener(handleServerResponse);
	
	document.getElementById("title").innerHTML = "Searching from: " + response.url;
});

buildButton.addEventListener("click", async () => {
	port.postMessage({build: true, id: id});
});

searchButton.addEventListener("click", async () => {
	port.postMessage({query: document.getElementById("query").value, id: id});
});

topicButton.addEventListener("click", async () => {
	port.postMessage({num_topics: document.getElementById("k").value, prob_background: document.getElementById("lambda").value, id: id});
});

function handleServerResponse(response) {
	document.getElementById("output").innerHTML = response;
}