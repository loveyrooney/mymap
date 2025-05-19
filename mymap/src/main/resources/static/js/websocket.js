/* 지도 관련 변수 */
let bounds = new kakao.maps.LatLngBounds();

/* 지도 생성 */
const mapContainer = document.getElementById("map"),
  mapOption = {
    center: new kakao.maps.LatLng(33.450701, 126.570667),
    level: 1,
  };
const map = new kakao.maps.Map(mapContainer, mapOption);

/* 마커 추가 */
function addMarker(position, role) {
  let imageSrc, imageSize;
  if (role === "dp") {
    imageSrc = "/images/human.png";
    imageSize = new kakao.maps.Size(50, 50); // 마커 이미지의 크기
  } else if (role === "ar") {
    imageSrc = "/images/school.png";
    imageSize = new kakao.maps.Size(50, 50);
  } else {
    imageSrc = "/images/subway.png";
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

/* 인포윈도우 추가 */
function displayInfowindow(geom) {
  let infowindow = new kakao.maps.InfoWindow({ zIndex: 1 });
  let content =
    '<div class="infoBox">' +
    '<i class="fa-solid fa-xmark"></i>' +
    "<p>" +
    geom.clusterName +
    "</p>" +
    "</div>";
  infowindow.setContent(content);
  return infowindow;
}

/* 인포윈도우 표시 */
function infoOpen(info, marker) {
  console.log(info);
  //console.log(info.a, info.Xh)
  //console.log("infoOpen", infos, infos.indexOf(info));
  info.open(map, marker);
  //infos.push(info);
  infoClose(info);
}

/* 닫기 버튼으로 해당 인포윈도우 닫기 */
function infoClose(info) {
  document.querySelectorAll(".infoBox").forEach((el) => {
    //console.log(el.parentElement.parentElement);
    if (el.parentElement.parentElement == info.a) {
      el.querySelector("i").addEventListener("click", () => {
        info.close();
      });
    }
  });
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

const callWrap = document.querySelector("#call_board_wrap");
const callOverlay = document.querySelector("#call_wrap_overlay");
function targetOpen(clusterName) {
  callOverlay.classList.remove("hidden");
  callOverlay.classList.add("call_overlay");
  callWrap.classList.remove("hidden");
  callWrap.classList.add("call_wrap");
  document.querySelector("#cluster_tit").textContent = clusterName;
}

// callOverlay.addEventListener("click", (e) => {
//   console.log(e.target);
//   if (!callWrap.contains(e.target)) {
//     callOverlay.classList.remove("call_overlay");
//     callOverlay.classList.add("hidden");
//     callWrap.classList.remove("call_wrap");
//     callWrap.classList.add("hidden");
//   }
// });

const bBtn = document.querySelector("#bBtn");
const sBtn = document.querySelector("#sBtn");
const bBox = document.querySelector("#call_bus_bike");
const sBox = document.querySelector("#call_sub");
bBtn.addEventListener("click", () => {
  bBox.classList.remove("hidden");
  bBox.classList.add("call_bus_bike");
  sBox.classList.remove("call_sub");
  sBox.classList.add("hidden");
});
sBox.addEventListener("click", () => {
  bBox.classList.remove("call_bus_bike");
  bBox.classList.add("hidden");
  sBox.classList.remove("hidden");
  sBox.classList.add("call_sub");
});

/* websocket 관련 */
const bList = document.querySelector("#bus_bike_list");
const sList = document.querySelector("#sub_list");
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

document.addEventListener("DOMContentLoaded", async function () {
  let geoms = await displayPlaces();
  console.log("geoms", geoms);
  console.log(geoms[0]["lon"]);
  //학교 신청 리스트 반복문
  geoms.forEach((geom) => {
    // 좌표 설정 후 마커를 생성, 지도에 표시
    let placePosition = new kakao.maps.LatLng(geom.lat, geom.lon);
    console.log("placePosition", placePosition);
    let marker = addMarker(placePosition, geom.group);
    let info = displayInfowindow(geom);
    kakao.maps.event.addListener(marker, "click", function () {
      //removeInfo(info);
      infoOpen(info, marker);
      targetOpen(geom.clusterName);
      webSocket.send(JSON.stringify(dataSet[geom.clusterName]));
    });
    bounds.extend(placePosition); // 검색된 좌표 위치 저장
  });
  const call_msg = await fetchData();
  dataSet = removeNulls(call_msg);
  if (dataSet == undefined) console.error("dataSet failed");
  const call_crawling = await callCrawling();
  console.log(call_crawling);
});

function createCongetionMsg(c) {
  if (c == "0") return "없음";
  else if (c == "3") return "여유";
  else if (c == "4") return "보통";
  else if (c == "5") return "혼잡";
  else if (c == "6") return "매우혼잡";
  else return "";
}

webSocket.onopen = function (event) {
  console.log("Connected to WebSocket server.");
  webSocket.send(sessionStorage.getItem("token"));
};

// 집 버튼 클릭 시 메시지 전송
const house = document.querySelector("#house");
house.addEventListener("click", function (e) {
  console.log(dataSet);
  webSocket.send(JSON.stringify(dataSet["용산"]));
});

// 웹소켓 통신 결과 동적 추가
webSocket.onmessage = async function (event) {
  let data = await JSON.parse(event.data);
  console.log(data);
  bList.replaceChildren();
  sList.replaceChildren();
  if (data.bus) {
    data.bus.forEach((d) => {
      //console.log(d);
      let li = document.createElement("li");
      li.className = "flex_evenly route_li";
      let title = document.createElement("span");
      title.className = "route_title";
      title.textContent = `${d.rtNm} ${d.deTourAt == "11" ? "(우회)" : ""}`;
      let div = document.createElement("div");
      div.className = "route_box";
      let r1 = document.createElement("span");
      if (d.arrmsg1.includes("[")) {
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
      if (d.arrmsg2.includes("[")) {
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
    let span = document.createElement("span");
    span.textContent = data.bike.count;
    li.append(title, span);
    bList.appendChild(li);
  }
  if (data.sub) {
    data.sub.forEach((d) => {
      console.log(d);
      let li = document.createElement("li");
      li.className = "flex_evenly s_route_li";
      let titdiv = document.createElement("div");
      titdiv.className = "s_route_box";
      let title1 = document.createElement("span");
      let title2 = document.createElement("span");
      let tit = d.trainLineNm.split("-");
      title1.textContent = tit[0];
      title2.textContent = tit[1];
      titdiv.append(title1, title2);
      let div = document.createElement("div");
      div.className = "s_route_box";
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
};

webSocket.onerror = function (event) {
  console.error("WebSocket error: " + event.data);
};
