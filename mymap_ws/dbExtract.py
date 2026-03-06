import pandas as pd
from io import StringIO

# txt 파일 읽기
with open('routestation20260304V2.txt', 'r', encoding='utf-8') as f:
    content = f.read()

# ^ 를 줄바꿈으로 변환
content = content.replace('^', '\n')

# | 구분자로 DataFrame 생성
df = pd.read_csv(StringIO(content), sep='|')

print(df.head())
print(df.shape)  # 행/열 수 확인

# ① routeId, routeName 중복 제거
df_route = df[['routeId', 'routeName']].drop_duplicates()
df_route.to_csv('gg_route.csv', index=False, encoding='utf-8')
print(f"route.csv 저장완료: {len(df_route)}행")

# ② stationId, stationName, x, y
df_station = df[['stationId', 'stationName', 'x', 'y']].drop_duplicates()
df_station.to_csv('gg_bus.csv', index=False, encoding='utf-8')
print(f"station.csv 저장완료: {len(df_station)}행")

# CSV로 저장
# df.to_csv('gg_bus.csv', index=False, encoding='utf-8')