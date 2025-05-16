/* refresh token auth */
async function refreshCall() {
  try {
    const res = await fetch("/auth/refresh", {
      method: "POST",
      credentials: "include",
    });
    if (!res.ok) {
      throw new Error("refresh call error: " + res.status);
    }
    const refreshData = await res.json();
    console.log("리프레시", refreshData);
    sessionStorage.setItem("token", refreshData.accessToken);
    return refreshData;
  } catch (e) {
    console.log(e);
    return null;
  }
}

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

/* 클러스터 마커 api call 함수 */
async function displayPlaces() {
  console.log(window.location.pathname.split("/")[3]);
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

    if (response.status === 401) {
      refreshCall();
      return null;
    } else if (!response.ok) throw new Error("map_geom call failed");
    const data = await response.json();
    return data;
  } catch (error) {
    console.log(error);
    return null;
  }
}

function targetOpen(clusterName) {
  let callWrap = document.querySelector("#call_board_wrap");
  callWrap.classList.remove("hidden");
  callWrap.classList.add("call_wrap");
  document.querySelector("#cluster_tit").textContent = clusterName;
}

let geoms = await displayPlaces();
if (geoms == null) geoms = await displayPlaces();
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
  });
  bounds.extend(placePosition); // 검색된 좌표 위치 저장
});
