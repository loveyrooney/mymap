window.onload = function () {
    const token = document.querySelector("meta[name='_csrf']").content;
    const header = document.querySelector("meta[name='_csrf_header']").content;
    // 사용자가 들어오면 웹소켓 객체 생성
    const webSocket = new WebSocket("ws://localhost:8080/mymap_ws/ws");

    webSocket.onopen = function(event) {
        console.log("Connected to WebSocket server.");
        // 클라이언트의 웹소켓 오픈 시, 기존 대화 리스트 받아오기
        fetch("/params",{
                method: "POST",
                headers: {
                    'X-Requested-With': "XMLHttpRequest",
                    'Accept': 'application/json',
                    'X-XSRF-Token': token,
                    Content-Type': 'application/json'
                }
        }).then(res=>res.json())
            .then((data)=>{

            }).then(()=>{
                webSocket.send();
            }).catch(error=>{
            console.log("error: ",error);
        });
    };
    // 채팅 메시지의 요소를 메시지 영역에 동적 추가
    webSocket.onmessage = function(event) {
        let data = event.data;

    };

    webSocket.onclose = function(event) {
        console.log("Connection closed.");
    };

    webSocket.onerror = function(event) {
        console.error("WebSocket error: " + event.data);
    };
    // 엔터 버튼을 누르거나, 전송 버튼을 클릭 시 채팅 메시지 서버로 전송
    const chatmsg = document.getElementsByName("content")[0];
    chatmsg.onkeydown=function (e) {
        if(chatmsg.value !==''){
            if(e.key==='Enter'){
                webSocket.send(chatmsg.value);
                chatmsg.value = '';
            }
        }
    };
    document.getElementById("chatBtn").onclick=function () {
        if(chatmsg.value !=='') {
            webSocket.send(chatmsg.value);
            chatmsg.value = '';
        }
    };


}