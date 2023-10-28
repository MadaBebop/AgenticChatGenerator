//Non possono essere usate variabili non dichiarate qui (strict mode)
'use strict';


//----------------------------------- ELEMENTS VARIABLES ---------------------------------------------------------------
// Stomp WS Endpoint: "/wss"
let stompEndpoint = null

//NB document.querySelector: seleziona il primo elemento/tipo css definito dal selettore
//info message di "chat-page" > html: <div class = connecting...>
let infoMessageConnection = document.querySelector('.connecting');


//Elementi dello User
let username = null;
let userPage = document.querySelector('#user-page'); //html: <div = user-page
let userForm = document.querySelector('#user-form'); //html: <form = user-form

//Pagina Chat (inizialmente invisibile)
let chatPage = document.querySelector('#chat-page');

//Form message
let messageForm = document.querySelector('#message-form');
let messageInput = document.querySelector('#message');
let messageArea = document.querySelector('#message-area');


//-------------------------------CONNECTION HANDLERS FUNCTIONS----------------------------------------------------------

function connect(event) {
    username = document.querySelector('#name').value.trim();

    //Se lo user esiste...
    if(username) {
        //rimuoviamo la pagina user form/container
        userPage.classList.add('hidden');
        //facciamo comparire la pagina della chat
        chatPage.classList.remove('hidden');

        //Stabilisco connessione
        stompEndpoint = Stomp.over(new SockJS('/wss'));
        stompEndpoint.connect({}, onConnected, onError); //In caso di errore -> al metodo "onError"
    }
    event.preventDefault();
}


function onConnected() {
    // Sottoscrive al canale pubblico "/uniud"
    stompEndpoint.subscribe('/uniud/public', onMessageReceived);

    // ApplicationDestinationPrefix + message mapping ("adduser") del metodo 'sendMessage'
    stompEndpoint.send(
        "/app/adduser",
        {},
        JSON.stringify({
            sender: username,
            type: 'JOIN'
        })
    )

    infoMessageConnection.classList.add('hidden');
}

//Messaggio di errore
function onError() {
    infoMessageConnection.textContent = 'Non è stato possibile connettersi al server websocket. Per favore ricarica la pagina e prova di nuovo!';
    infoMessageConnection.style.color = 'red';
}


//-----------------------------MESSAGES HANDLERS FUNCTIONS--------------------------------------------------------------

function sendMessage(event) {

    let messageContent = messageInput.value.trim();

    if(messageContent && stompEndpoint) {
        let message = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompEndpoint.send("/app/sendmessage", {}, JSON.stringify(message));
        //Reset message content
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {

    //Creiamo un elemento html tipo lista (linea messaggio chat)
    let messageObject = document.createElement('li');
    //Il payload ricordiamo essere un oggetto di tipo "ChatMessage"
    let messageToShow = JSON.parse(payload.body);

    //Inseriamo la notifica dello stato degli utenti in chat (left || joined || chatting)
    if(messageToShow.type === 'JOIN') {
        messageObject.classList.add('event-message');
        messageToShow.content = messageToShow.sender + ' si è unito a noi!';
    } else if (messageToShow.type === 'LEAVE') {
        messageObject.classList.add('event-message');
        messageToShow.content = messageToShow.sender + ' ha lasciato la chat!';
    } else {

        messageObject.classList.add('chat-message');

        //La prima lettera del nome dell'utente sarà la sua icona profilo
        const avatarObject = document.createElement('i');
        const avatarLetter = document.createTextNode(messageToShow.sender[0]);
        avatarObject.appendChild(avatarLetter);

        //Creazione del colore dell'icona in base al nome utente
        avatarObject.style['background-color'] = stringToColor(messageToShow.sender);
        messageObject.appendChild(avatarObject);

        //Span per colorare una parte di testo
        let usernameElement = document.createElement('span');
        let usernameText = document.createTextNode(messageToShow.sender);

        usernameElement.appendChild(usernameText);
        messageObject.appendChild(usernameElement);

        //NB appendChild, metodo dell'interfaccia DOM node, usato per aggiungere un elemento (nodo) al "padre"
    }

    //Paragrafo
    let textElement = document.createElement('p');
    let messageText = document.createTextNode(messageToShow.content); //Attributo message di "ChatMessage"

    textElement.appendChild(messageText);
    messageObject.appendChild(textElement);

    messageArea.appendChild(messageObject);
    //"scroll" della chat
    messageArea.scrollTop = messageArea.scrollHeight;
}

//----------------------------------------------------------------------------------------------------------------------
function stringToColor(seed) {
    // Calcola un hash univoco basato su una stringa di input
    let hashCode = 0;
    for (let i = 0; i < seed.length; i++) {
        hashCode = seed.charCodeAt(i) + ((hashCode << 5) - hashCode);
    }

    // Genera il colore esadecimale
    const color = (hashCode & 0x00FFFFFF).toString(16).toUpperCase();

    // Aggiungi zeri iniziali se necessario per ottenere un colore a 6 cifre
    const hexColor = "00000".substring(0, 6 - color.length) + color;

    return `#${hexColor}`;
}

//Event listeners
userForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)

