import time
import asyncio
import aiohttp
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware

from services.auth import verify_jwt
from services.bus import call_bus
from services.subway import call_subway
from services.bike import call_bike
from models.schemas import WebSocketRequest

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # 모든 도메인 허용 (특정 도메인으로 제한할 수도 있음)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

SESSION_TIMEOUT = 60 * 30  # seconds

@app.websocket("/mymap_ws/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    authenticated = False
    
    try:
        if not authenticated:
            # First message should be the token
            token = await websocket.receive_text()
            payload = verify_jwt(token)
            
            if payload is None:
                await websocket.send_json({"msg": "Authentication failed"})
                await websocket.close()
                return
            else:
                user_id = payload.get("sub")
                await websocket.send_json({"msg": f"Hello ${user_id}, webSocket connected"})
                authenticated = True

        async with aiohttp.ClientSession() as session:
            while True:
                try:
                    # Wait for data with timeout
                    data = await asyncio.wait_for(websocket.receive_json(), timeout=SESSION_TIMEOUT)
                    #last_activity = time.time()
                    print(f"받은 메시지: {data}, 받은 시각: {int(time.time() * 1000)}")
                    
                    # data를 큐에 넣고 호출하는 구조로 만들어야 됨.
                    cluster_name = data.get('clusterName')
                    
                    if "bus" in data:
                        bus_data = data['bus'] # Dict[arsId, List[routes]]
                        for stId, routes in bus_data.items():
                            # data 중 중복 stid 요청이 있으면 캐시에서 가져오는 구조로 만들어야 됨. 그렇지 않으면 call_bus 실행
                            bus_result = await call_bus(session, stId, set(routes))
                            print(f"콜버스 결과: {bus_result}, , 받은 시각: {int(time.time() * 1000)}")
                            await websocket.send_json({
                                'bus': bus_result, 
                                'clusterName': cluster_name
                            })

                    if "sub" in data:
                        sub_list = data['sub'] # List[stationName]
                        for station in sub_list:
                            sub_result = await call_subway(session, station)
                            print(f"콜섭 결과: {sub_result}, 받은 시각: {int(time.time() * 1000)}")
                            await websocket.send_json({
                                'sub': sub_result, 
                                'clusterName': cluster_name
                            })

                    if "bike" in data:
                        bike_list = data['bike'] # List[stationName]
                        for station in bike_list:
                            bike_result = await call_bike(session, station)
                            print(f"콜바이크 결과: {bike_result}, 받은 시각: {int(time.time() * 1000)}")
                            await websocket.send_json({
                                'bike': bike_result, 
                                'clusterName': cluster_name
                            })

                except asyncio.TimeoutError:
                    await websocket.send_json({"msg": "세션이 만료되었습니다."})
                    await websocket.close()
                    break
                
    except WebSocketDisconnect:
        print("클라이언트 클로즈 (정상 종료)")
    except Exception as e:
        print(f"예외 발생: {type(e).__name__} - {e}")
        try:
            await websocket.close()
        except:
            pass
