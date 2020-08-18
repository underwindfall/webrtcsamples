'use strict';

//const TYPE_CREATE_OFFER = "create_offer"
const TYPE_SEND_OFFER = "send_offer"
const TYPE_SEND_ANSWER = "send_answer"
const TYPE_SEND_CANDIDATE = "send_candidate"

var isChannelReady = false;
var isInitiator = false;
var isStarted = false;
var localStream;
var pc;
var remoteStream;
var turnReady;

var pcConfig = {
  'iceServers': [{
    'urls': 'stun:stun.l.google.com:19302'
  }]
};

// Set up audio and video regardless of what devices are present.
var sdpConstraints = {
  offerToReceiveAudio: true,
  offerToReceiveVideo: true
};

/////////////////////////////////////////////

var room = prompt('Enter room name:');
var server = io("http://192.168.1.29:8080")
var socket = server.connect();

if (room !== '') {
  socket.emit('create or join', room);
  console.log('Attempted to create or  join room', room);
}

socket.on('ipaddr', function (room) {
  console.log("ipaddr" + room)
});

socket.on('created', function (room) {
  console.log('Created room ' + room);
  isInitiator = true;
});

socket.on('full', function (room) {
  console.log('Room ' + room + ' is full');
});

socket.on('join', function (room) {
  console.log('Another peer made a request to join room ' + room);
  console.log('This peer is the initiator of room ' + room + '!');
  isChannelReady = true;
  doCall();
});

socket.on('joined', function (room) {
  console.log('joined: ' + room);
  isChannelReady = true;
});

socket.on('log', function (array) {
  console.log.apply(console, array);
});

// This client receives a message
socket.on('message', function (message) {
  console.log('Client received message:', message);

  switch (message.type) {
    // case TYPE_CREATE_OFFER:
    //   doCall();
    //   break;
    case TYPE_SEND_OFFER:
      console.log("Process TYPE_SEND_OFFER message", message);
      pc.setRemoteDescription(new RTCSessionDescription({
        'type': 'offer',
        'sdp': message.sdp
      }));
      doAnswer();
      break;
    case TYPE_SEND_ANSWER:
      console.log("Process TYPE_SEND_ANSWER message", message);
      pc.setRemoteDescription(new RTCSessionDescription({
        'type': 'answer',
        'sdp': message.sdp
      }));
      break;
    case TYPE_SEND_CANDIDATE:
      console.log('Remote candidate received: ', message.candidate);
      var candidate = new RTCIceCandidate({
        sdpMLineIndex: message.label,
        candidate: message.candidate
      });
      pc.addIceCandidate(candidate);
      break;
    default:
      break;
  }
});

socket.on('close', function (message) {
  handleRemoteHangup();
});

////////////////////////////////////////////////

function sendMessage(message) {
  console.log('Client sending message: ', message);
  socket.emit('message', message);
}



////////////////////////////////////////////////////

var localVideo = document.querySelector('#localVideo');
var remoteVideo = document.querySelector('#remoteVideo');

navigator.mediaDevices.getUserMedia({
  audio: false,
  video: true
})
  .then(gotStream)
  .catch(function (e) {
    alert('getUserMedia() error: ' + e.name);
  });

function gotStream(stream) {
  console.log('Adding local stream.');
  localStream = stream;
  localVideo.srcObject = stream;
  //sendMessage({ type: TYPE_CREATE_OFFER });
  maybeStart();
}

var constraints = {
  video: true
};

console.log('Getting user media with constraints', constraints);


function maybeStart() {
  console.log('>>>>>>> maybeStart() ', localStream, isChannelReady);
  if (typeof localStream !== 'undefined') {
    console.log('>>>>>> creating peer connection');
    createPeerConnection();
    pc.addStream(localStream);
    console.log('isInitiator', isInitiator);
  }
}

window.onbeforeunload = function () {
  socket.emit('leave');
};

/////////////////////////////////////////////////////////

function createPeerConnection() {
  try {
    pc = new RTCPeerConnection(null);
    pc.onicecandidate = handleIceCandidate;
    pc.onaddstream = handleRemoteStreamAdded;
    pc.onremovestream = handleRemoteStreamRemoved;
    console.log('Created RTCPeerConnnection');
  } catch (e) {
    console.log('Failed to create PeerConnection, exception: ' + e.message);
    alert('Cannot create RTCPeerConnection object.');
    return;
  }
}

function handleIceCandidate(event) {
  console.log('icecandidate event: ', event);
  if (event.candidate) {
    sendMessage({
      type: TYPE_SEND_CANDIDATE,
      label: event.candidate.sdpMLineIndex,
      id: event.candidate.sdpMid,
      candidate: event.candidate.candidate
    });
  } else {
    console.log('End of candidates.');
  }
}

function handleCreateOfferError(event) {
  console.log('createOffer() error: ', event);
}

function doCall() {
  console.log('Sending offer to peer isInitiator', isInitiator, isChannelReady);
 // if (isInitiator && isChannelReady) {
    pc.createOffer(setLocalAndSendMessageOffer, handleCreateOfferError);
 // }
}

function doAnswer() {
  console.log('Sending answer to peer.');
  pc.createAnswer().then(
    setLocalAndSendMessageAnswer,
    onCreateSessionDescriptionError
  );
}

function setLocalAndSendMessageAnswer(sessionDescription) {
  pc.setLocalDescription(sessionDescription);
  console.log('setLocalAndSendMessageAnswer sending message', sessionDescription);
  sendMessage({
    type: TYPE_SEND_ANSWER,
    sdp: sessionDescription.sdp
  });
}

function setLocalAndSendMessageOffer(sessionDescription) {
  pc.setLocalDescription(sessionDescription);
  console.log('setLocalAndSendMessageOffer sending message', sessionDescription);
  sendMessage({
    type: TYPE_SEND_OFFER,
    sdp: sessionDescription.sdp
  });
}

function onCreateSessionDescriptionError(error) {
  trace('Failed to create session description: ' + error.toString());
}


function handleRemoteStreamAdded(event) {
  console.log('Remote stream added.');
  remoteStream = event.stream;
  remoteVideo.srcObject = remoteStream;
}

function handleRemoteStreamRemoved(event) {
  console.log('Remote stream removed. Event: ', event);
}

function hangup() {
  console.log('Hanging up.');
  stop();
  socket.emit('leave');
}

function handleRemoteHangup() {
  console.log('Session terminated.');
  stop();
  isInitiator = false;
}

function stop() {
  isStarted = false;
  pc.close();
  pc = null;
}
