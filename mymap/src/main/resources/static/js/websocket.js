window.onload = await function () {
    let dataSet;
    let token = sessionStorage.getItem('token');
    async function fetchData(){
        try {
            const response = await fetch("/api/map_msg",{
                method: "POST",
                credentials: "include",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
                body : JSON.stringify({ jno : window.location.pathname.split("/")[3] })
            });
            if (response.status === 401) {
                const res = await fetch("/auth/refresh", {
                     method: 'POST',
                     credentials: 'include'
                });
                if (!res.ok) throw new Error("refresh call error: " + res.status);
                const refreshData = await res.json();
                console.log("두번째",refreshData);
                sessionStorage.setItem("token",refreshData.accessToken);
                return refreshData;
            }
            const data = await response.json();
            //const jsonStr = JSON.stringify(data);
            //const sizeInBytes = new TextEncoder().encode(jsonStr).length;
            //console.log(`JSON size: ${sizeInBytes} bytes`);
            return data;
        } catch (error) {
            console.log("첫번째 fetch 에러",error);
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
        console.log("실행후",data);
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