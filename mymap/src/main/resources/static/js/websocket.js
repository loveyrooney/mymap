window.onload = function () {
    // 사용자가 들어오면 웹소켓 객체 생성
    const webSocket = new WebSocket("ws://localhost:8090/mymap_ws/ws");

    webSocket.onopen = function(event) {
        console.log("Connected to WebSocket server.");
        // 지도 UI 터치 시 해당 역의 param 요청 조건 추가해야됨
        fetch("/api/params",{
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${sessionStorage.getItem('token')}`,
                }
        }).then(res => {
            if (!res.ok) throw new Error("인증 실패 또는 권한 없음",res.status);
            return res.json();
          })
          .then(data => {
            console.log("유저 정보:", data);  // 백엔드에서 토큰 검증 후 응답
          })
          .catch(err => {
            console.error("API 요청 실패:", err);
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
//    // 엔터 버튼을 누르거나, 전송 버튼을 클릭 시 채팅 메시지 서버로 전송
//    const chatmsg = document.getElementsByName("content")[0];
//    chatmsg.onkeydown=function (e) {
//        if(chatmsg.value !==''){
//            if(e.key==='Enter'){
//                webSocket.send(chatmsg.value);
//                chatmsg.value = '';
//            }
//        }
//    };
//    document.getElementById("chatBtn").onclick=function () {
//        if(chatmsg.value !=='') {
//            webSocket.send(chatmsg.value);
//            chatmsg.value = '';
//        }
//    };


}