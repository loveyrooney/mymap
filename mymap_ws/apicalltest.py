import requests, os, time
from dotenv import load_dotenv

load_dotenv()

# 환경변수 사용
api_key = os.getenv("API_KEY")

url = 'http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid'
params ={'serviceKey' : api_key, 'stSrch' : '02004' }
arsids = ['13550','13168','13028','01136','02004','02006','03004','03132','03144','16972',
          '08113','92604','92702','03502','22793','03252','18210','09137','22816','16131',
          '19153','14203','20934','11208','24290','36601','04721','13513','22028','15363',
          '01282','12390','04509','20017','24426','01101','02222','15229','92630','40025']

for i in range(1):
    params['stSrch'] = arsids[i]
    response = requests.get(url, params=params)
    print(int(time.time() * 1000))
    print(response.content)
