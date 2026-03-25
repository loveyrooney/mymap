import aiohttp
import xmltodict
import time
from typing import List, Set, Dict, Any
from config import settings
from models.schemas import BusArrivalItem

# bus routes filter
BUS_KEYSET = { 'stNm', 'busRouteId', 'rtNm', 'busRouteAbrv', 'routeType', 'staOrd', 'isLast1', 'busType1', 'isLast2', 'busType2', 'arrmsg1', 'arrmsg2', 'nxtStn', 'deTourAt', 'congestion1', 'congestion2'}
GG_BUS_KEYSET = {'crowded1', 'crowded2','locatioinNo1','locationNo2', 'predictTime1','predictTime2','remainSeatCnt1','remainSeatCnt2','routeDestName','routeId','routeName','routeTypeCd', 'stationId'}

def bus_routes_filter(item: dict, routes: Set[str]) -> dict:
    print(f"hello busroutefilter : {item}, {routes}")
    #if 'busRouteAbrv' in item and item['busRouteAbrv'] in routes:
    if 'busRouteId' in item and item.get('busRouteId') in routes:
        return {k: item[k] for k in item if k in BUS_KEYSET}
    elif 'routeId' in item and str(item.get('routeId')) in routes:
        if (item.get('predictTime1') != None or item.get('predictTime1') != '') or (item.get('predictTime2') != None or item.get('predictTime2') != ''):
            return {k: item[k] for k in item if k in GG_BUS_KEYSET}
    return {}

async def call_bus(arsId: str, routes: Set[str]) -> List[Any]:
    # routes에서 첫번째 자리가 1이면 서울, 2면 경기
    api_key = settings.BUS_API_KEY
    ggurl = f"https://apis.data.go.kr/6410000/busarrivalservice/v2/getBusArrivalListv2?serviceKey={api_key}&stationId={arsId}&format=json"
    url = f"http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?serviceKey={api_key}&arsId={arsId}"
    #params ={'serviceKey' : api_key, 'arsId' : arsId }  이걸 지정하고 session.get(url, param=param)으로 하면 키내용이 바뀐다..
    async with aiohttp.ClientSession() as session:
        if(len(arsId) > 5) :
            async with session.get(ggurl) as response:
                if response.status == 200:
                    data = await response.json()
                    if(data.get("response",{}).get("msgBody",{}) == None):
                        return []
                    itemLists = data.get("response",{}).get("msgBody",{}).get("busArrivalList",[])
                    print(f"api 받은 시각: {int(time.time() * 1000)}")
                    print(f"itemLists===> {itemLists}")
                    print(f"len list===> {len(itemLists)}, {isinstance(itemLists, list)}")
                    if isinstance(itemLists, list):
                        return [bus_routes_filter(item,routes) for item in itemLists if bus_routes_filter(item,routes)]
                    else:
                        return {k: itemLists[k] for k in itemLists if k in GG_BUS_KEYSET}
                else:
                    return response.status
        else:
            async with session.get(url) as response:
                if response.status == 200:
                    data = await response.text()
                    if(xmltodict.parse(data).get("ServiceResult",{}).get("msgBody",{}) == None):
                        return []
                    itemLists = xmltodict.parse(data).get("ServiceResult",{}).get("msgBody",{}).get("itemList",[])
                    print(f"api 받은 시각: {int(time.time() * 1000)}")
                    print(f"itemLists===> {itemLists}")
                    print(f"len list===> {len(itemLists)}, {isinstance(itemLists, list)}")
                    if isinstance(itemLists, list):
                        return [bus_routes_filter(item,routes) for item in itemLists if bus_routes_filter(item,routes)]
                    else:
                        return {k: itemLists[k] for k in itemLists if k in BUS_KEYSET}
                else:
                    return response.status
