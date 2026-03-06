import aiohttp
import time
from typing import List, Any
from config import settings
from models.schemas import SubwayArrivalItem

# subway filter
SUB_KEYSET = {'subwayId', 'updnLine', 'trainLineNm', 'trnsitCo', 'subwayList', 'btrainSttus', 'recptnDt', 'arvlMsg2', 'arvlMsg3', 'arvlCd', 'lstcarAt'}

def subway_filter(item: dict) -> dict:
    # if 'updnLine' in item and item['updnLine'] == direction:
    #     return {k: item[k] for k in item if k in SUB_KEYSET}
    # return {}
    return {k: item[k] for k in item if k in SUB_KEYSET}

async def call_subway(st_name: str) -> List[Any]:
    api_key = settings.SUB_API_KEY
    url = f"http://swopenAPI.seoul.go.kr/api/subway/{api_key}/json/realtimeStationArrival/0/15/{st_name}"
    
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            #print(f"Final URL: {response.url}")
            if response.status == 200:
                data = await response.json() 
                itemLists = data.get("realtimeArrivalList",[])
                print(f"api 받은 시각: {int(time.time() * 1000)}")
                #print(f"itemLists===> {itemLists}")
                filtered = [subway_filter(item) for item in itemLists if subway_filter(item)]
                filtered.sort(key=lambda item: (item["subwayId"], item["updnLine"]))
                return filtered
            else:
                return response.status
