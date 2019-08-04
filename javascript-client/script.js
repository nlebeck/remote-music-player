// The server will define the variables ipAddress and port with the right
// values at the top of this script when sending it to the client.
//
// This might be a terrible practice. If it is, I would like to learn the
// right way of passing this kind of information into a JavaScript script and
// then modify this script appropriately.
var webSocket = new WebSocket("ws://" + ipAddress + ":" + port);

webSocket.addEventListener("message", function (event) {
    console.log("Got message from server: ", event.data);
});

function testFunction() {
    webSocket.send("Hello server!");
}