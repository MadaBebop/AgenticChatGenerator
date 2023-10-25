//Non possono essere usate variabili non dichiarate qui (strict mode)
'use strict';
//document.querySelector: seleziona il primo elemento css definito nel selettore

// Stomp WS Endpoint: "/wss"
let stompEndpoint = Stomp.over(new SockJS('/wss'));

//info message di "chat-page"
const infoMessageConnection = document.querySelector('.connecting'); //html: <div class = connecting

//Elementi dello User
let username = null;
const userPage = document.querySelector('#user-page'); //html: <div = user-page
let userForm = document.querySelector('#user-form'); //html: <form = user-form
    userForm.addEventListener('submit', connect, true)

//Elemento Chat (inizialmente invisibile)
const chatPage = document.querySelector('#chat-page');

//Form message
const messageForm = document.querySelector('#message-form');
messageForm.addEventListener('submit', sendMessage, true)
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#message-area');

//----------------------------------------------------------------

//Connection
function connect(event) {
    username = document.querySelector('#name').value.trim();

    //Se lo user esiste...
    if(username) {
        //rimuoviamo la pagina user form/container
        userPage.classList.add('hidden');
        //facciamo comparire la pagina della chat
        chatPage.classList.remove('hidden');
        stompEndpoint.connect({}, onConnected, onError); //In caso di errore -> al metodo "onError"
    }
    event.preventDefault();
}

function onConnected() {
    // Sottoscrive al canale pubblico "/uniud"
    stompEndpoint.subscribe('/uniud/public', onMessageReceived);

    // ApplicationDestinationPrefix + message mapping ("adduser") del metodo 'sendMessage'
    stompEndpoint.send("/app/adduser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )
    stompEndpoint.
    connectingElement.classList.add('hidden');
}

//Messaggio di errore
function onError() {
    infoMessageConnection.textContent = 'Non è stato possibile connettersi al server websocket. Per favore ricarica la pagina e prova di nuovo!';
    infoMessageConnection.style.color = 'red';
}


//------------------------------------------------------------------------------------------------
function sendMessage(event) {

    var messageContent = messageInput.value.trim();

    if(messageContent && stompEndpoint) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompEndpoint.send("/app/chat.sendMessage",
            {},
            JSON.stringify(chatMessage)
        );
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {

    //Creiamo un elemento html tipo lista
    var messageObject = document.createElement('li');
    //Prendiamo l'oggetto "ChatMessage" dal body
    var messageToShow = JSON.parse(payload.body);

    if(messageToShow.type === 'JOIN') {
        messageObject.classList.add('event-message');
        messageToShow.content = messageToShow.sender + ' joined!';
    } else if (messageToShow.type === 'LEAVE') {
        messageObject.classList.add('event-message');
        messageToShow.content = messageToShow.sender + ' left!';
    } else {
        messageObject.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(messageToShow.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(messageToShow.sender);

        messageObject.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(messageToShow.sender);
        usernameElement.appendChild(usernameText);
        messageObject.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(messageToShow.content);
    textElement.appendChild(messageText);

    messageObject.appendChild(textElement);

    messageArea.appendChild(messageObject);
    messageArea.scrollTop = messageArea.scrollHeight;
}
