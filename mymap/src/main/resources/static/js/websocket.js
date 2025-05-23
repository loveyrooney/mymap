/* 지도 관련 변수 */
let bounds = new kakao.maps.LatLngBounds();

/* 지도 생성 */
const mapContainer = document.getElementById("map"),
  mapOption = {
    center: new kakao.maps.LatLng(37.566844470986226, 126.97862961491931),
    level: 1,
  };
const map = new kakao.maps.Map(mapContainer, mapOption);

/* 마커 추가 */
function addMarker(position, role) {
  let imageSrc, imageSize;
  if (role === "dp") {
    imageSrc = "/images/departure.png";
    imageSize = new kakao.maps.Size(50, 50); // 마커 이미지의 크기
  } else if (role === "ar") {
    imageSrc = "/images/arrive.png";
    imageSize = new kakao.maps.Size(50, 50);
  } else {
    imageSrc = "/images/marker.png";
    imageSize = new kakao.maps.Size(45, 45);
  }
  let markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize),
    marker = new kakao.maps.Marker({
      position: position, // 마커의 위치
      image: markerImage,
    });

  marker.setMap(map); // 지도 위에 마커를 표출합니다.
  map.setBounds(bounds); // 저장된 좌표위치를 기준으로 지도 범위 재설정
  return marker;
}

/* 툴팁 추가 */
function addTooltip(position, data) {
  let vehicle = [];
  let bus = "";
  if (data.bus) {
    bus = "<span>BUS</span>";
    vehicle.push("bus");
  }
  let sub = "";
  if (data.sub) {
    sub = "<span>SUBWAY</span>";
    vehicle.push("sub");
  }
  let bike = "";
  if (data.bike) {
    bike = "<span>BIKE</span>";
    vehicle.push("bike");
  }
  let content =
    '<div class="infoBox">' +
    '<span style="width:100%; font-size:0.7rem; font-weight: 500;">' +
    data.clusterName +
    "</span>" +
    '<div class="flex_center badges">' +
    bus +
    sub +
    bike +
    "</div>" +
    "</div>";
  let customOverlay = new kakao.maps.CustomOverlay({
    position: position,
    content: content,
    xAnchor: 0.5, // 컨텐츠의 x 위치
    yAnchor: 0, // 컨텐츠의 y 위치
  });
  return { tooltip: customOverlay, vehicles: vehicle };
}

/* refresh token auth */
async function refreshCall() {
  try {
    const res = await fetch("/auth/refresh", {
      method: "GET",
      credentials: "include",
    });
    const refreshData = await res.json();
    console.log("리프레시", refreshData);
    if (res.ok) {
      sessionStorage.setItem("token", refreshData.accessToken);
    }
    return refreshData;
  } catch (e) {
    console.log(e.msg);
    return null;
  }
}

/* 클러스터 마커 api call 함수 */
async function displayPlaces() {
  //console.log(window.location.pathname.split("/")[3]);
  try {
    const response = await fetch(
      `/api/map_geom/${window.location.pathname.split("/")[3]}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${sessionStorage.getItem("token")}`,
        },
      }
    );
    const data = await response.json();
    if (response.status === 401) {
      let refresh = await refreshCall();
      console.log(refresh);
      if (refresh.accessToken) {
        window.location.href =
          "/view/map/" + window.location.pathname.split("/")[3];
      } else throw new Error(refresh.msg);
    }
    if (!response.ok) throw new Error(data.msg);
    return data;
  } catch (error) {
    console.log(error);
    //window.location.href = "/";
  }
}

// 토스트 팝업 open close
const callWrap = document.querySelector("#call_board_wrap");
const callWarpClose = document.querySelector("#call_wrap_close");
const clusterTitle = document.querySelector("#cluster_tit");
const bList = document.querySelector("#bus_bike_list");
const sList = document.querySelector("#sub_list");
const bBtn = document.querySelector("#bBtn");
const sBtn = document.querySelector("#sBtn");
const bBox = document.querySelector("#call_bus_bike");
const sBox = document.querySelector("#call_sub");
//const callOverlay = document.querySelector("#call_wrap_overlay");
function targetOpen(clusterName, vehicles) {
  //   callOverlay.classList.remove("hidden");
  //   callOverlay.classList.add("call_overlay");
  if (!vehicles.includes("bus")) {
    bBox.className = "hidden";
    sBox.className = "call_sub";
    sBtn.className = "call_btn_action";
    bBtn.className = "";
  } else {
    bBox.className = "call_bus_bike";
    sBox.className = "hidden";
    bBtn.className = "call_btn_action";
    sBtn.className = "";
  }
  callWrap.className = "call_wrap";
  clusterTitle.textContent = clusterName;
  bList.replaceChildren();
  sList.replaceChildren();
}

// 토스트 팝업 닫기
callWarpClose.addEventListener("click", (e) => {
  //console.log(e.target);
  callWrap.className = "hidden";
});

// 토스트 팝업 내 버스 지하철 display 토글
bBtn.addEventListener("click", () => {
  bBox.className = "call_bus_bike";
  sBox.className = "hidden";
  bBtn.className = "call_btn_action";
  sBtn.className = "";
});
sBtn.addEventListener("click", () => {
  bBox.className = "hidden";
  sBox.className = "call_sub";
  sBtn.className = "call_btn_action";
  bBtn.className = "";
});

// 새로고침 버튼 클릭 시 메시지 전송
const callRefresh = document.querySelector("#call_refresh");
callRefresh.addEventListener("click", function (e) {
  console.log(dataSet);
  bList.replaceChildren();
  sList.replaceChildren();
  webSocket.send(JSON.stringify(dataSet[clusterTitle.textContent]));
});

/* websocket 관련 */
const webSocket = new WebSocket("ws://localhost:8090/mymap_ws/ws");
let dataSet;

async function callCrawling() {
  try {
    const response = await fetch("/api/crawling", {
      method: "GET",
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${sessionStorage.getItem("token")}`,
      },
    });
    const data = await response.json();
    if (!response.ok) throw Error(data.msg);
    return data;
  } catch (error) {
    console.log("crawling error:", error);
    return null;
  }
}

async function fetchData() {
  try {
    const response = await fetch("/api/map_msg", {
      method: "POST",
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${sessionStorage.getItem("token")}`,
      },
      body: JSON.stringify({
        jno: window.location.pathname.split("/")[3],
        direction: new URLSearchParams(window.location.search).get("direction"),
      }),
    });
    const data = await response.json();
    if (!response.ok) throw Error(data.msg);
    return data;
  } catch (error) {
    console.log("map_msg error:", error);
    return null;
  }
}

function removeNulls(obj) {
  if (Array.isArray(obj)) {
    return obj
      .map(removeNulls) // 배열 내부 요소도 재귀 처리
      .filter((item) => item !== null && item !== undefined);
  } else if (obj !== null && typeof obj === "object") {
    return Object.entries(obj).reduce((acc, [key, value]) => {
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

/* 실행부 */
document.addEventListener("DOMContentLoaded", async function () {
  //call msg
  const call_msg = await fetchData();
  dataSet = removeNulls(call_msg);
  if (dataSet == undefined) console.error("dataSet failed");
  console.log(dataSet);
  // call geom
  let geoms = await displayPlaces();
  console.log("geoms", geoms);
  geoms.forEach((geom) => {
    let placePosition = new kakao.maps.LatLng(geom.lat, geom.lon);
    //console.log("placePosition", placePosition);
    let marker = addMarker(placePosition, geom.group);
    let resultTooltip = addTooltip(placePosition, dataSet[geom.clusterName]);
    resultTooltip.tooltip.setMap(map);
    kakao.maps.event.addListener(marker, "click", function () {
      targetOpen(geom.clusterName, resultTooltip.vehicles);
      webSocket.send(JSON.stringify(dataSet[geom.clusterName]));
    });
    bounds.extend(placePosition); // 검색된 좌표 위치 저장
  });
  //call crawling
  const call_crawling = await callCrawling();
  console.log(call_crawling);
  const slider = document.querySelector("#crawling_data");
  if (call_crawling.length > 0) {
    let index = 0;
    call_crawling.forEach((d, i) => (call_crawling[i] = d.slice(5)));
    setInterval(() => {
      slider.textContent = call_crawling[index];
      index = (index + 1) % call_crawling.length;
    }, 3000);
  }
});

function createCongetionMsg(c) {
  if (c == "0") return "없음";
  else if (c == "3") return "여유";
  else if (c == "4") return "보통";
  else if (c == "5") return "혼잡";
  else if (c == "6") return "매우혼잡";
  else return "";
}

function abstractBusRouteType(routeType) {
  if (routeType == 1) {
    return "air_bus";
  } else if (routeType == 2) {
    return "town_bus";
  } else if (routeType == 3) {
    return "blue_bus";
  } else if (routeType == 4) {
    return "green_bus";
  } else if (routeType == 5) {
    return "yellow_bus";
  } else {
    return "far_bus";
  }
}

function abstractSubRouteType(routeType) {
  if (routeType == "1001") {
    return "line1";
  } else if (routeType == "1002") {
    return "line2";
  } else if (routeType == "1003") {
    return "line3";
  } else if (routeType == "1004") {
    return "line4";
  } else if (routeType == "1005") {
    return "line5";
  } else if (routeType == "1006") {
    return "line6";
  } else if (routeType == "1007") {
    return "line7";
  } else if (routeType == "1008") {
    return "line8";
  } else if (routeType == "1009") {
    return "line9";
  } else if (routeType == "1063") {
    return "central_line";
  } else if (routeType == "1065") {
    return "air_line";
  } else if (routeType == "1067") {
    return "itx_line";
  } else if (routeType == "1075") {
    return "suin_line";
  } else if (routeType == "1077") {
    return "new_bundang_line";
  } else if (routeType == "1092") {
    return "uiee_line";
  } else if (routeType == "1093") {
    return "west_line";
  } else if (routeType == "1094") {
    return "sinrim_line";
  } else if (routeType == "1081") {
    return "gg_line";
  } else if (routeType == "1032") {
    return "gtxa_line";
  } else {
    return "";
  }
}

webSocket.onopen = function (event) {
  console.log("Connected to WebSocket server.");
  webSocket.send(sessionStorage.getItem("token"));
};

// 웹소켓 통신 결과 동적 추가
webSocket.onmessage = async function (event) {
  let data = await JSON.parse(event.data);
  console.log(data);
  if (data.bus) {
    data.bus.forEach((d) => {
      //console.log(d);
      let routeType = abstractBusRouteType(d.routeType);
      let li = document.createElement("li");
      li.className = "flex_evenly route_li";
      let title = document.createElement("span");
      title.className = `route_title ${routeType}`;
      title.textContent = `${d.rtNm} ${d.deTourAt == "11" ? "(우회)" : ""}`;
      let div = document.createElement("div");
      div.className = "route_box";
      let r1 = document.createElement("span");
      if (d.arrmsg1.includes("분")) {
        r1.textContent = `${d.arrmsg1.split("분")[0]}분 ${d.arrmsg1.slice(
          d.arrmsg1.indexOf("[") + 1,
          d.arrmsg1.indexOf("]")
        )} ${createCongetionMsg(d.congestion1)} ${
          d.isLast1 == "1" ? "막차" : ""
        }`;
      } else {
        r1.textContent = `${d.arrmsg1} ${createCongetionMsg(d.congestion1)} ${
          d.isLast1 == "1" ? "막차" : ""
        }`;
      }
      let r2 = document.createElement("span");
      if (d.arrmsg2.includes("분")) {
        r2.textContent = `${d.arrmsg2.split("분")[0]}분 ${d.arrmsg2.slice(
          d.arrmsg2.indexOf("[") + 1,
          d.arrmsg2.indexOf("]")
        )} ${createCongetionMsg(d.congestion2)} ${
          d.isLast2 == "1" ? "막차" : ""
        }`;
      } else {
        r2.textContent = `${d.arrmsg2} ${createCongetionMsg(d.congestion2)} ${
          d.isLast2 == "1" ? "막차" : ""
        }`;
      }
      div.append(r1, r2);
      li.append(title, div);
      bList.appendChild(li);
    });
  }
  if (data.bike) {
    let li = document.createElement("li");
    li.className = "flex_evenly";
    let title = document.createElement("span");
    title.textContent = data.bike.name;
    title.className = "bike_tit";
    let span = document.createElement("span");
    span.textContent = `${data.bike.count}대`;
    span.className = "bike_content";
    li.append(title, span);
    bList.appendChild(li);
  }
  if (data.sub) {
    data.sub.forEach((d) => {
      //console.log(d);
      let routeType = abstractSubRouteType(d.subwayId);
      let li = document.createElement("li");
      li.className = "flex_evenly s_route_li";
      let titdiv = document.createElement("div");
      titdiv.className = `s_route_box ${routeType}`;
      let title1 = document.createElement("span");
      let title2 = document.createElement("span");
      let tit = d.trainLineNm.split("-");
      title1.textContent = tit[1];
      title2.textContent = tit[0];
      titdiv.append(title1, title2);
      let div = document.createElement("div");
      div.className = "s_route_content";
      let s1 = document.createElement("span");
      let s1txt = d.arvlMsg2.replace(/[\[\]]/g, "");
      s1.textContent = s1txt.split("분")[1]
        ? s1txt.split("분")[0] + "분"
        : s1txt;
      let s2 = document.createElement("span");
      s2.textContent = d.arvlMsg3.replace(/[\[\]]/g, "");
      div.append(s1, s2);
      li.append(titdiv, div);
      sList.appendChild(li);
    });
  }
};

webSocket.onclose = function (event) {
  console.log("Connection closed.");
  alert("서버 세션이 만료되었습니다. 다시 로그인 해주세요!");
  window.location.href = "/";
};

webSocket.onerror = function (event) {
  console.error("WebSocket error: " + event.data);
};
