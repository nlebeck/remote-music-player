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
    let navigationDiv = document.getElementById("navigation");
    // Based on the following StackOverflow answer:
    // https://stackoverflow.com/a/3955238.
    while (navigationDiv.firstChild) {
        console.log(navigationDiv.firstChild);
        navigationDiv.removeChild(navigationDiv.firstChild);
    }
    
    let navigationHeader = document.createElement("h1");
    navigationHeader.innerText = "Navigation";
    navigationDiv.appendChild(navigationHeader);
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
    
    let songsHeader = document.createElement("h1");
    songsHeader.innerText = "Songs";
    songsDiv.appendChild(songsHeader);
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

function play(song) {
    webSocket.send(JSON.stringify({ command: "play", argument: song }));
}