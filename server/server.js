'use strict';
const port = 8080;
const os = require('os');
const express = require('express');
const cors = require('cors');
const socketIO = require('socket.io');
const server = express().use(cors());
const ifaces = require('os').networkInterfaces();

let roomId = ""

const getLocalExternalIP = () => [].concat(...Object.values(ifaces))
    .find((details) => details.family === 'IPv4' && !details.internal)
    .address

const app = server
    .listen(port, function () {
        console.log(`Server listening on ip : ${getLocalExternalIP()} port ${port}!`);
    })
    .on('error', function (error) {
        if (error.code === 'EADDRINUSE') {
            console.error(`Current ${port} Address in use, retrying......`);
            setTimeout(() => {
                app.close();
                app.listen(port);
            }, 1000);
        }
    });

const io = socketIO.listen(app);
io.on('connection', function (socket) {
    // convenience function to log server messages on the client
    function log() {
        let array = ['Message from server:'];
        array.push.apply(array, arguments);
        socket.emit('log', array);
        console.log("Server log", array);
    }

    socket.on('message', function (message) {
        log('Client said: ', message);
        socket.to(roomId).emit('message', message);
    });

    socket.on('create or join', function (room) {
        log('Received request to create or join room ' + room);

        const clientsInRoom = io.sockets.adapter.rooms[room];
        const numClients = clientsInRoom ? Object.keys(clientsInRoom.sockets).length : 0;
        log('Room ' + room + ' now has ' + numClients + ' client(s)');

        if (numClients === 0) {
            socket.join(room);
            log('Client ID ' + socket.id + ' created room ' + room);
            roomId = room;
            socket.emit('created', room, socket.id);
        } else {
            log('Client ID ' + socket.id + ' joined room ' + room);
            io.in(room).emit('join', room);
            socket.join(room);
            socket.emit('joined', room, socket.id);
            io.in(room).emit('ready');
        }
    });


    socket.on('ipaddr', function () {
        const ifaces = os.networkInterfaces();
        for (let dev in ifaces) {
            ifaces[dev].forEach(function (details) {
                if (details.family === 'IPv4' && details.address !== '127.0.0.1') {
                    socket.emit('ipaddr', details.address);
                }
            });
        }
    });

    socket.on('leave', function () {
        log(`Client ${socket.id} want to leave room ${roomId}`)
        socket.leave(roomId)
        io.in(roomId).emit('close', roomId)
    });
});
