"use strict";

var webSocket = new WebSocket("ws://localhost:8081");

webSocket.addEventListener("message", function (event) {
    console.log("Got message from server: ", event.data);
});

function testFunction() {
    webSocket.send("Hello server!");
}