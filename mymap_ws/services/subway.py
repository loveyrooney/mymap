import aiohttp
from typing import List, Any
from config import settings
from models.schemas import SubwayArrivalItem

SUB_KEYSET = {'subwayId', 'updnLine', 'trainLineNm', 'trnsitCo', 'subwayList', 'btrainSttus', 'recptnDt', 'arvlMsg2', 'arvlMsg3', 'arvlCd', 'lstcarAt'}

def subway_filter(item: dict) -> dict:
    return {k: item[k] for k in item if k in SUB_KEYSET}

async def call_subway(st_name: str) -> List[Any]:
    api_key = settings.SUB_API_KEY
    url = f"http://swopenAPI.seoul.go.kr/api/subway/{api_key}/json/realtimeStationArrival/0/15/{st_name}"
    
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as response:
            if response.status == 200:
                try:
                    data = await response.json()
                    itemLists = data.get("realtimeArrivalList", [])
                    
                    filtered = [subway_filter(item) for item in itemLists]
                    # Sort by subwayId and updnLine
                    filtered.sort(key=lambda item: (item.get("subwayId",""), item.get("updnLine","")))
                    return filtered
                except Exception as e:
                    print(f"Subway Parse Error: {e}")
                    return []
            else:
                print(f"Subway API Error: {response.status}")
                return []
