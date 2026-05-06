/* 지도 관련 변수 */
let bounds = new naver.maps.LatLngBounds();

/* 지도 생성 */
const mapContainer = document.getElementById("map");
let map;

naver.maps.onJSContentLoaded = function() {
  const mapOption = {
    center: new naver.maps.LatLng(37.566844470986226, 126.97862961491931),
    zoom: 16,
  };
  map = new naver.maps.Map(mapContainer, mapOption);
  
  // 지도가 로드된 후 데이터 초기화 실행
  initMapData();
};

/* 마커 추가 */
function addMarker(position, role) {
  let imageSrc, imageSize;
  if (role === "dp") {
    imageSrc = "/images/departure.png";
    imageSize = new naver.maps.Size(50, 50);
  } else if (role === "ar") {
    imageSrc = "/images/arrive.png";
    imageSize = new naver.maps.Size(50, 50);
  } else {
    imageSrc = "/images/marker.png";
    imageSize = new naver.maps.Size(45, 45);
  }
  
  let marker = new naver.maps.Marker({
    position: position,
    map: map,
    icon: {
      url: imageSrc,
      size: imageSize,
      scaledSize: imageSize,
      origin: new naver.maps.Point(0, 0),
      anchor: new naver.maps.Point(imageSize.width / 2, imageSize.height)
    }
  });

  return marker;
}

/* 커스텀 오버레이 정의 (카카오 CustomOverlay 대응) */
function createCustomOverlay() {
  function CustomOverlay(options) {
    this._element = document.createElement('div');
    this._element.style.position = 'absolute';
    this._element.innerHTML = options.content;
    this.setPosition(options.position);
    this.setMap(options.map || null);
  }

  CustomOverlay.prototype = new naver.maps.OverlayView();
  CustomOverlay.prototype.constructor = CustomOverlay;

  CustomOverlay.prototype.setPosition = function(position) {
    this._position = position;
    this.draw();
  };

  CustomOverlay.prototype.onAdd = function() {
    const pane = this.getPanes().overlayLayer;
    this._element.style.display = 'block';
    pane.appendChild(this._element);
  };

  CustomOverlay.prototype.draw = function() {
    if (!this.getMap()) return;
    const projection = this.getProjection();
    const pixelAt = projection.fromCoordToOffset(this._position);
    this._element.style.left = (pixelAt.x - (this._element.offsetWidth / 2)) + 'px';
    this._element.style.top = (pixelAt.y - this._element.offsetHeight - 50) + 'px'; // 마커 위쪽 배치
  };

  CustomOverlay.prototype.onRemove = function() {
    this._element.parentNode.removeChild(this._element);
  };
  
  return CustomOverlay;
}

let CustomOverlay;

/* 툴팁 추가 (커스텀 오버레이 사용) */
function addTooltip(position, data) {
  if (!CustomOverlay) CustomOverlay = createCustomOverlay();
  
  let vehicle = [];
  let bus = data.bus ? "<span>BUS</span>" : "";
  if(data.bus) vehicle.push("bus");
  let sub = data.sub ? "<span>SUBWAY</span>" : "";
  if(data.sub) vehicle.push("sub");
  let bike = data.bike ? "<span>BIKE</span>" : "";
  if(data.bike) vehicle.push("bike");

  let content =
    '<div class="infoBox">' +
    '<span style="width:100%; font-size:0.7rem; font-weight: 500;">' + data.clusterName + "</span>" +
    '<div class="flex_center badges">' + bus + sub + bike + "</div>" +
    "</div>";

  let overlay = new CustomOverlay({
    position: position,
    content: content,
    map: map
  });
  
  return { tooltip: overlay, vehicles: vehicle };
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
    return null;
  }
}

/* 클러스터 마커 api call 함수 */
async function displayPlaces() {
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
        window.location.href = "/view/map/" + window.location.pathname.split("/")[3];
      } else throw new Error(refresh.error || refresh.msg || "Refresh failed");
    }
    if (!response.ok) throw new Error(data.error || data.msg || "Geom failed");
    return data;
  } catch (error) {
    console.log(error);
  }
}

// UI 요소 제어 변수들
const callWrap = document.querySelector("#call_board_wrap");
const callWarpClose = document.querySelector("#call_wrap_close");
const clusterTitle = document.querySelector("#cluster_tit");
const bList = document.querySelector("#bus_bike_list");
const sList = document.querySelector("#sub_list");
const bBtn = document.querySelector("#bBtn");
const sBtn = document.querySelector("#sBtn");
const bBox = document.querySelector("#call_bus_bike");
const sBox = document.querySelector("#call_sub");

function targetOpen(clusterName, vehicles) {
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

callWarpClose.addEventListener("click", () => {
  callWrap.className = "hidden";
});

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

const callRefresh = document.querySelector("#call_refresh");
callRefresh.addEventListener("click", function () {
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
    if (!response.ok) throw Error(data.error || data.msg || "Crawling failed");
    return data;
  } catch (error) {
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
    if(response.status == 401){
      alert(data.message);
      window.location.href = "/view/main";
    }
    return data;
  } catch (error) {
    return null;
  }
}

async function callGGStNm(stId, routeId, staOrder) {
    try {
      const response = await fetch(`/api/bus_station/${stId}?routeId=${routeId}&staOrder=${staOrder}`, {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${sessionStorage.getItem("token")}`,
        },
      });
      return await response.json();
    } catch (error) {
      return null;
    }
}

function removeNulls(obj) {
  if (Array.isArray(obj)) {
    return obj
      .map(removeNulls)
      .filter((item) => item !== null && item !== undefined);
  } else if (obj !== null && typeof obj === "object") {
    return Object.entries(obj).reduce((acc, [key, value]) => {
      const cleaned = removeNulls(value);
      if (cleaned !== null && cleaned !== undefined) {
        acc[key] = cleaned;
      }
      return acc;
    }, {});
  } else {
    return obj;
  }
}

/* 지도 데이터 초기화 함수 */
async function initMapData() {
  const call_msg = await fetchData();
  dataSet = removeNulls(call_msg);
  if (dataSet == undefined) console.error("dataSet failed");
  console.log(dataSet);
  
  let geoms = await displayPlaces();
  console.log("geoms", geoms);
  if (geoms) {
    geoms.forEach((geom) => {
      let placePosition = new naver.maps.LatLng(geom.lat, geom.lon);
      let marker = addMarker(placePosition, geom.group);
      let resultTooltip = addTooltip(placePosition, dataSet[geom.clusterName]);
      
      // resultTooltip.tooltip.open(map, marker); // InfoWindow용 메서드 제거
      
      naver.maps.Event.addListener(marker, "click", function () {
        targetOpen(geom.clusterName, resultTooltip.vehicles);
        webSocket.send(JSON.stringify(dataSet[geom.clusterName]));
      });
      bounds.extend(placePosition);
    });
    if(map) map.panToBounds(bounds);
  }

  const call_crawling = await callCrawling();
  console.log(call_crawling);
  const slider = document.querySelector("#crawling_data");
  if (call_crawling && call_crawling.length > 0) {
    let index = 0;
    call_crawling.forEach((d, i) => (call_crawling[i] = d.slice(5)));
    setInterval(() => {
      slider.textContent = call_crawling[index];
      index = (index + 1) % call_crawling.length;
    }, 3000);
  }
}

// 실행 대기
document.addEventListener("DOMContentLoaded", () => {
    // onJSContentLoaded에서 지도가 생성된 후 initMapData가 호출됨
});

/* 각종 UI 생성 함수들 (websocket.js 복제) */
function createGGCongetionMsg(c) {
  if (c == "1") return "여유";
  else if (c == "2") return "보통";
  else if (c == "3") return "혼잡";
  else if (c == "4") return "매우혼잡";
  else return "";
}

function createCongetionMsg(c) {
  if (c == "3") return "여유";
  else if (c == "4") return "보통";
  else if (c == "5") return "혼잡";
  else if (c == "6") return "매우혼잡";
  else return "";
}

function abstractGGBusRouteType(routeType) {
  if (routeType == 11 || routeType == 21) return "far_bus";
  else if (routeType == 12 || routeType == 22) return "seat_bus";
  else if (routeType == 13 || routeType == 23) return "general_bus";
  else if (routeType == 14) return "m_bus";
  else if (routeType == 30) return "yellow_bus";
  else if (routeType == 41 || routeType == 42 || routeType == 43) return "purple_bus";
  else if (routeType == 51 || routeType == 52 || routeType == 53) return "air_bus";
  else return "etc_bus";
}

function abstractBusRouteType(routeType) {
  if (routeType == 1) return "air_bus";
  else if (routeType == 2) return "town_bus";
  else if (routeType == 3) return "blue_bus";
  else if (routeType == 4) return "green_bus";
  else if (routeType == 5) return "yellow_bus";
  else return "far_bus";
}

function abstractSubRouteType(routeType) {
  const lineMap = {
    "1001": "line1", "1002": "line2", "1003": "line3", "1004": "line4",
    "1005": "line5", "1006": "line6", "1007": "line7", "1008": "line8",
    "1009": "line9", "1063": "central_line", "1065": "air_line",
    "1067": "itx_line", "1075": "suin_line", "1077": "new_bundang_line",
    "1092": "uiee_line", "1093": "west_line", "1094": "sinrim_line",
    "1081": "gg_line", "1032": "gtxa_line"
  };
  return lineMap[routeType] || "";
}

webSocket.onopen = function () {
  webSocket.send(sessionStorage.getItem("token"));
};

async function dynamicGGBusUI (d) {
  let routeType = abstractGGBusRouteType(d.routeTypeCd);
  let li = document.createElement("li");
  li.className = "flex_evenly route_li";
  let title = document.createElement("div");
  title.className = `route_title ${routeType}`;
  let lineNm = document.createElement("span");
  lineNm.className = "route_title_lineNm";
  lineNm.textContent = `${d.routeName}`;
  let stNm = document.createElement("span");
  stNm.className = "route_title_stNm";
  let nxtNm = document.createElement("span");
  nxtNm.className = "route_title_nxtStn";
  nxtNm.textContent = `종착역 : ${d.routeDestName}`;

  callGGStNm(d.stationId, d.routeId, d.staOrder).then(data => {
    if(data) {
      stNm.textContent = data.currentSt;
      if (data.nextSt !== '데이터 없음') {
        nxtNm.textContent = `다음역 : ${data.nextSt}`;
      }
    }
  });

  title.append(lineNm, stNm, nxtNm);
  let div = document.createElement("div");
  div.className = "route_box";
  let r1 = document.createElement("span");
  if(d.predictTime1){
    r1.textContent = `${d.predictTime1}분 ${d.locationNo1 ? d.locationNo1+"번째 전" : ""} ${d.crowded1 ? createGGCongetionMsg(d.crowded1) : ""}`;
  }
  let r2 = document.createElement("span");
  if(d.predictTime2){
    r2.textContent = `${d.predictTime2}분 ${d.locationNo2 ? d.locationNo2+"번째 전" : ""} ${d.crowded2 ? createGGCongetionMsg(d.crowded2) : ""}`;
  }
  div.append(r1, r2);
  li.append(title, div);
  bList.appendChild(li);
}

function dynamicBusUI (d) {
  let routeType = abstractBusRouteType(d.routeType);
  let li = document.createElement("li");
  li.className = "flex_evenly route_li";
  let title = document.createElement("div");
  title.className = `route_title ${routeType}`;
  let lineNm = document.createElement("span");
  lineNm.className = "route_title_lineNm";
  lineNm.textContent = `${d.rtNm} ${d.deTourAt == "11" ? "(우회)" : ""}`;
  let stNm = document.createElement("span");
  stNm.className = "route_title_stNm";
  stNm.textContent = `${d.stNm}`;
  let nxtNm = document.createElement("span");
  nxtNm.className = "route_title_nxtStn";
  nxtNm.textContent = `다음역 : ${d.nxtStn}`;
  title.append(lineNm, stNm, nxtNm);
  let div = document.createElement("div");
  div.className = "route_box";
  let r1 = document.createElement("span");
  if (d.arrmsg1.includes("분")) {
    r1.textContent = `${d.arrmsg1.split("분")[0]}분 ${d.arrmsg1.slice(d.arrmsg1.indexOf("[") + 1, d.arrmsg1.indexOf("]"))} ${createCongetionMsg(d.congestion1)} ${d.isLast1 == "1" ? "막차" : ""}`;
  } else {
    r1.textContent = `${d.arrmsg1} ${createCongetionMsg(d.congestion1)} ${d.isLast1 == "1" ? "막차" : ""}`;
  }
  let r2 = document.createElement("span");
  if (d.arrmsg2.includes("분")) {
    r2.textContent = `${d.arrmsg2.split("분")[0]}분 ${d.arrmsg2.slice(d.arrmsg2.indexOf("[") + 1, d.arrmsg2.indexOf("]"))} ${createCongetionMsg(d.congestion2)} ${d.isLast2 == "1" ? "막차" : ""}`;
  } else {
    r2.textContent = `${d.arrmsg2} ${createCongetionMsg(d.congestion2)} ${d.isLast2 == "1" ? "막차" : ""}`;
  }
  div.append(r1, r2);
  li.append(title, div);
  bList.appendChild(li);
}

// 웹소켓 통신 결과 동적 UI 업데이트
webSocket.onmessage = async function (event) {
  let data = JSON.parse(event.data);
  console.log(data);
  if (data.bus) {
    if(Array.isArray(data.bus)){
      data.bus.forEach((d) => { d.routeId ? dynamicGGBusUI(d) : dynamicBusUI(d); });
    } else {
      data.bus.routeId ? dynamicGGBusUI(data.bus) : dynamicBusUI(data.bus);
    }
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
      let routeType = abstractSubRouteType(d.subwayId);
      let li = document.createElement("li");
      li.className = "flex_evenly s_route_li";
      let titdiv = document.createElement("div");
      titdiv.className = `s_route_box ${routeType}`;
      let tit = d.trainLineNm.split("-");
      let title1 = document.createElement("span");
      title1.textContent = tit[1];
      let title2 = document.createElement("span");
      title2.textContent = tit[0];
      titdiv.append(title1, title2);
      let div = document.createElement("div");
      div.className = "s_route_content";
      let s1 = document.createElement("span");
      let s1txt = d.arvlMsg2.replace(/[\[\]]/g, "");
      s1.textContent = s1txt.split("분")[1] ? s1txt.split("분")[0] + "분" : s1txt;
      let s2 = document.createElement("span");
      s2.textContent = d.arvlMsg3.replace(/[\[\]]/g, "");
      div.append(s1, s2);
      li.append(titdiv, div);
      sList.appendChild(li);
    });
  }
};

webSocket.onclose = () => {
  alert("연결이 종료되었습니다.");
  //window.location.href = "/";
};

webSocket.onerror = function (event) {
  console.error("WebSocket error: " + event.data);
};
