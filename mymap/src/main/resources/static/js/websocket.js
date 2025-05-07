window.onload = await function () {
    let dataSet;
    const token = sessionStorage.getItem('token');
    async function fetchData(){
        try {
            const response = await fetch("/api/map_msg",{
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body : JSON.stringify({ jno : window.location.pathname.split("/")[3] })
            });
            const data = await response.json();
            //const jsonStr = JSON.stringify(data);
            //const sizeInBytes = new TextEncoder().encode(jsonStr).length;
            //console.log(`JSON size: ${sizeInBytes} bytes`);
            return data;
        } catch (error) {
            console.log(error);
            return null;
        }
    }

    function removeNulls(obj) {
      if (Array.isArray(obj)) {
        return obj
          .map(removeNulls)              // 배열 내부 요소도 재귀 처리
          .filter(item => item !== null && item !== undefined);
      } else if (obj !== null && typeof obj === 'object') {
        return Object.entries(obj)
          .reduce((acc, [key, value]) => {
            const cleaned = removeNulls(value); // 재귀 처리
            if (cleaned !== null && cleaned !== undefined) {
              acc[key] = cleaned;
            }
            return acc;
          }, {});
      } else {
        return obj;
      }
    }

    fetchData().then(data => {
        dataSet = removeNulls(data);
        if(dataSet==undefined)
            throw new Error("web authentication failed");
    })
    .then(()=>{
        // 사용자가 들어오면 웹소켓 객체 생성
        const webSocket = new WebSocket("ws://localhost:8090/mymap_ws/ws");

        webSocket.onopen = function(event) {
            console.log("Connected to WebSocket server.");
            //webSocket.send("hello");
            webSocket.send(token);
        };
        // 채팅 메시지의 요소를 메시지 영역에 동적 추가
        webSocket.onmessage = function(event) {
            let data = event.data;
            console.log(data);
        };

        webSocket.onclose = function(event) {
            console.log("Connection closed.");
        };

        webSocket.onerror = function(event) {
            console.error("WebSocket error: " + event.data);
        };
        // 집 버튼 클릭 시 메시지 전송
        const house = document.querySelector("#house");
        house.addEventListener("click", function (e) {
            console.log(dataSet);
            webSocket.send(JSON.stringify(dataSet["용산"]));
        });
    })
    .catch(e=> console.log(e));

}