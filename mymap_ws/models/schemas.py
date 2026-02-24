from pydantic import BaseModel, Field
from typing import List, Optional, Dict

class BusArrivalItem(BaseModel):
    arrmsg1: str
    arrmsg2: str
    busRouteAbrv: str
    busRouteId: str
    busType1: str
    busType2: str
    congestion1: str
    congestion2: str
    deTourAt: str
    isLast1: str
    isLast2: str
    nxtStn: str
    routeType: str
    rtNm: str
    stNm: str
    staOrd: str

class SubwayArrivalItem(BaseModel):
    subwayId: str
    updnLine: str
    trainLineNm: str
    subwayHeading: Optional[str] = None # Some APIs might not return this
    statnNm: Optional[str] = None
    bstatnNm: Optional[str] = None
    recptnDt: str
    arvlMsg2: str
    arvlMsg3: str
    arvlCd: str
    lstcarAt: str # 0: not last, 1: last

class BikeStatusItem(BaseModel):
    stationName: str
    parkingBikeTotCnt: str

class WebSocketRequest(BaseModel):
    clusterName: str
    bus: Optional[Dict[str, List[str]]] = None # arsId -> list of routeAbrv
    sub: Optional[List[str]] = None # list of station names
    bike: Optional[List[str]] = None # list of station names
