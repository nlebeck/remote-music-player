// The server will define the variables ipAddress and port with the right
// values at the top of this script when sending it to the client.
//
// This might be a terrible practice. If it is, I would like to learn the
// right way of passing this kind of information into a JavaScript script and
// then modify this script appropriately.
var webSocket = new WebSocket("ws://" + ipAddress + ":" + port);

webSocket.addEventListener("open", event => {
    webSocket.send(JSON.stringify({ command: "connect"}));
});

webSocket.addEventListener("message", function (event) {
    redrawScreen(JSON.parse(event.data));
});

function redrawScreen(responseJson) {
    let statusDiv = document.getElementById("status");
    // Based on the following StackOverflow answer:
    // https://stackoverflow.com/a/3955238.
    while (statusDiv.firstChild) {
        statusDiv.removeChild(statusDiv.firstChild);
    }
    
    let currentDirParagraph = document.createElement("p");
    currentDirParagraph.innerText = "Current directory: " + responseJson.currentDir;
    statusDiv.appendChild(currentDirParagraph);
    
    let controlsDiv = document.getElementById("controls");
    while (controlsDiv.firstChild) {
        controlsDiv.removeChild(controlsDiv.firstChild);
    }
    
    let prevButton = document.createElement("button");
    prevButton.innerText = "Prev";
    prevButton.addEventListener("click", event => {
        prev();
    });
    controlsDiv.appendChild(prevButton);
    if (responseJson.paused) {
        let unpauseButton = document.createElement("button");
        unpauseButton.innerText = "Unpause";
        unpauseButton.addEventListener("click", event => {
            unpause();
        });
        controlsDiv.appendChild(unpauseButton);
    }
    else {
        let pauseButton = document.createElement("button");
        pauseButton.innerText = "Pause";
        pauseButton.addEventListener("click", event => {
            pause();
        });
        controlsDiv.appendChild(pauseButton);
    }
    let nextButton = document.createElement("button");
    nextButton.innerText = "Next";
    nextButton.addEventListener("click", event => {
        next();
    });
    controlsDiv.appendChild(nextButton);
    
    let navigationDiv = document.getElementById("navigation");
    while (navigationDiv.firstChild) {
        navigationDiv.removeChild(navigationDiv.firstChild);
    }
    
    if (responseJson.currentDir != "") {
        let upButton = document.createElement("button");
        upButton.innerText = "Up";
        upButton.addEventListener("click", event => {
            navigateUp();
        });
        navigationDiv.appendChild(upButton);
        navigationDiv.appendChild(document.createElement("p"));
    }
    
    for (let childDir of responseJson.childDirs) {
        let button = document.createElement("button");
        button.innerText = childDir;
        button.addEventListener("click", event => {
            changeDir(childDir);
        });
        navigationDiv.appendChild(button);
        let paragraph = document.createElement("p");
        navigationDiv.appendChild(paragraph);
    }
    
    let songsDiv = document.getElementById("songs");
    while(songsDiv.firstChild) {
        songsDiv.removeChild(songsDiv.firstChild);
    }
    
    for (let song of responseJson.songs) {
        let button = document.createElement("button");
        button.innerText = song;
        button.addEventListener("click", event => {
            play(song);
        });
        songsDiv.appendChild(button);
        let paragraph = document.createElement("p");
        songsDiv.appendChild(paragraph);
    }
}

function changeDir(childDir) {
    webSocket.send(JSON.stringify({ command: "navigate", argument: childDir }));
}

function navigateUp() {
    webSocket.send(JSON.stringify({ command: "navigateUp" }));
}

function pause() {
    webSocket.send(JSON.stringify({ command: "pause" }));
}

function unpause() {
    webSocket.send(JSON.stringify({ command: "unpause" }));
}

function prev() {
    webSocket.send(JSON.stringify({ command: "prev" }));
}

function next() {
    webSocket.send(JSON.stringify({ command: "next" }));
}

function play(song) {
    webSocket.send(JSON.stringify({ command: "play", argument: song }));
}