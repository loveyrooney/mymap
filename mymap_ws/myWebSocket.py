import os, aiohttp, xmltodict, json
from dotenv import load_dotenv
from fastapi import FastAPI, WebSocket
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()
load_dotenv()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080"],  # 모든 도메인 허용 (특정 도메인으로 제한할 수도 있음)
    allow_credentials=True,
    allow_methods=["*"],  # 모든 HTTP 메소드 허용
    allow_headers=["*"],  # 모든 헤더 허용
)

@app.websocket("/mymap_ws/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    first_msg = False
    try:
        while True:
            # 메시지 수신
            data = await websocket.receive_json()
            print(f"받은 메시지: {data}")
            for key in data['bus'].keys():
                resultCallBus = await callBus(key)
                print(resultCallBus)
            if not first_msg :
                first_msg = True
            # (선택) 응답을 보낼 수 있음
            await websocket.send_text(f"서버가 받은 메시지: {data}")
    except Exception as e:
        print(f"연결 종료 또는 예외 발생: {e}")

async def callBus(arsId):
    api_key = os.getenv("API_KEY")
    url = f"http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?serviceKey={api_key}&arsId={arsId}"
    #params ={'serviceKey' : api_key, 'arsId' : arsId }
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            #print(f"Final URL: {response.url}")
            if response.status == 200:
                data = await response.text()  
                return xmltodict.parse(data)
            else:
                return response.status