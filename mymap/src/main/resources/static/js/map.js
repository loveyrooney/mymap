// 지도 관련 변수
let bounds = new kakao.maps.LatLngBounds();
let current = { marker: "", lat: "", lon: "", tooltip: "" };
let registerForm = {};

// 지도 생성
const mapContainer = document.getElementById("map"),
  mapOption = {
    center: new kakao.maps.LatLng(37.566844470986226, 126.97862961491931),
    level: 3,
  };
const map = new kakao.maps.Map(mapContainer, mapOption);

// 마커 생성
function addMarker(position) {
  const marker = new kakao.maps.Marker({
    map: map,
    position: position,
  });
  //map.setBounds(bounds);
  return marker;
}

// 툴팁 생성
function addTooltip(position, content) {
  let customOverlay = new kakao.maps.CustomOverlay({
    position: position,
    content: content,
    xAnchor: 0.5, // 컨텐츠의 x 위치
    yAnchor: 0, // 컨텐츠의 y 위치
  });
  return customOverlay;
}

// 맵에서 클릭해서 좌표 찾기
kakao.maps.event.addListener(map, "click", function (mouseEvent) {
  let latlng = mouseEvent.latLng;
  let position = new kakao.maps.LatLng(latlng.getLat(), latlng.getLng());
  let marker = addMarker(position);
  let content =
    '<div class="infoBox">' +
    `<span>여기에 등록하시겠습니까?</span>` +
    "<span>삭제하려면 마커를 클릭</span>" +
    "</div>";
  let tooltip = addTooltip(position, content);
  let message = "클릭한 위치의 위도는 " + latlng.getLat() + " 이고, ";
  message += "경도는 " + latlng.getLng() + " 입니다";
  console.log(message);
  tooltip.setMap(map);
  current.marker = marker;
  current.lat = latlng.getLat();
  current.lon = latlng.getLng();
  current.tooltip = tooltip;
  //마커 클릭 이벤트로 마커 및 툴팁삭제
  kakao.maps.event.addListener(marker, "click", function () {
    marker.setMap(null);
    tooltip.setMap(null);
  });
});

// 주소로 좌표 찾기
const geocoder = new kakao.maps.services.Geocoder();
const searchBtn = document.querySelector("#searchBtn");
const addr = document.querySelector("#addr");
searchBtn.addEventListener("click", function () {
  let keyword = addr.value;
  geocoder.addressSearch(keyword, function (result, status) {
    if (status === kakao.maps.services.Status.OK) {
      let position = new kakao.maps.LatLng(result[0].y, result[0].x);
      let marker = addMarker(position);
      let content =
        '<div class="infoBox">' +
        `<span>여기에 등록하시겠습니까?</span>` +
        "<span>삭제하려면 마커를 클릭</span>" +
        "</div>";
      let tooltip = addTooltip(position, content);
      let message = "검색한 위치의 위도는 " + result[0].y + " 이고, ";
      message += "경도는 " + result[0].x + " 입니다";
      console.log(message);
      tooltip.setMap(map);
      current.marker = marker;
      current.lat = result[0].y;
      current.lon = result[0].x;
      current.tooltip = tooltip;
      map.setCenter(position);
      addr.value = "";
      //마커 클릭 이벤트로 마커 및 툴팁삭제
      kakao.maps.event.addListener(marker, "click", function () {
        marker.setMap(null);
        tooltip.setMap(null);
      });
    }
  });
});

// refresh token auth
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

// call transfer id list
async function callTransfer(token, vehicle, lat, lon) {
  console.log(token);
  try {
    const response = await fetch(`/api/transfer`, {
      method: "POST",
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ vehicle: vehicle, lat: lat, lon: lon }),
    });
    const data = await response.json();
    if (!response.ok) throw Error(data.msg);
    return data;
  } catch (error) {
    console.log(error);
    return null;
  }
}

// 카테고리 선택 이벤트
const categ_select = document.querySelector("#categ_select");
const categ_choose = document.querySelector("#categ_choose");
const fromTo = document.querySelector("#from_to_box");
const tfBox = document.querySelector("#transfer_box");
const tfSelect = document.querySelector("#tf_select");
categ_choose.addEventListener("click", async function () {
  let val = categ_select.value;
  console.log(val);
  if (val == "") {
    alert("카테고리를 선택하세요.");
  } else if (current.lat == "") {
    alert("등록할 좌표 위치를 지정하세요.");
  } else if (val == "fromName" || val == "toName") {
    if (tfBox.className != "hidden") {
      tfBox.classList.remove("transfer_box");
      tfBox.classList.add("hidden");
    }
    fromTo.classList.remove("hidden");
    fromTo.classList.add("from_to_box");
  } else {
    if (fromTo.className != "hidden") {
      fromTo.classList.remove("from_to_box");
      fromTo.classList.add("hidden");
    }
    tfBox.classList.remove("hidden");
    tfBox.classList.add("transfer_box");
    let token = sessionStorage.getItem("token");
    let tfLists = await callTransfer(token, val, current.lat, current.lon);
    if (tfLists == null) {
      let newToken = await refreshCall();
      tfLists = await callTransfer(
        newToken.accessToken,
        val,
        current.lat,
        current.lon
      );
    }
    tfSelect.replaceChildren();
    tfLists.forEach((tf) => {
      let op = document.createElement("option");
      op.textContent = `${tf.tfId}_${tf.stName}`;
      op.setAttribute("value", `${tf.tfId}_${tf.stName}`);
      tfSelect.appendChild(op);
    });
  }
});

// 좌표 정보 등록 이벤트
const fromToInput = document.querySelector("#fromTo_input");
const depthSelect = document.querySelector("#depth_select");
const fromToRegiBtn = document.querySelector("#fromTo_regi_btn");
const tfRegiBtn = document.querySelector("#tf_regi_btn");
fromToRegiBtn.addEventListener("click", function () {
  if (fromToInput.value == "") alert("출발/도착지 이름을 입력하세요");
  else {
    let geoms = categ_select.value.includes("from") ? "fromGeoms" : "toGeoms";
    registerForm[categ_select.value] = fromToInput.value;
    registerForm[geoms] = [current.lat, current.lon];
    console.log("등록후", registerForm);
    let position = new kakao.maps.LatLng(current.lat, current.lon);
    let newMarker = addMarker(position);
    let content =
      '<div class="infoBox">' +
      `<span id="${categ_select.value}_${geoms}">${fromToInput.value}</span>` +
      "<span>삭제하려면 마커를 클릭</span>" +
      "</div>";
    console.log(current);
    current.marker.setMap(null);
    current.tooltip.setMap(null);
    let newTooltip = addTooltip(position, content);
    newTooltip.setMap(map);
    current.tooltip = newTooltip;
    fromToInput.value = "";
    fromTo.classList.remove("from_to_box");
    fromTo.classList.add("hidden");
    //마커 클릭 이벤트로 마커 및 툴팁삭제
    kakao.maps.event.addListener(newMarker, "click", function () {
      console.log("삭제전", registerForm);
      console.log(newTooltip.cc);
      if (!newTooltip.cc.includes("여기에 등록하시겠습니까?")) {
        let delKey = [];
        let keys = Object.keys(registerForm);
        for (let i = 0; i < keys.length; i++) {
          if (newTooltip.cc.includes(keys[i])) {
            console.log(keys[i]);
            delKey.push(keys[i]);
          }
        }
        console.log(delKey);
        delKey.forEach((k) => {
          delete registerForm[k];
        });
        console.log("삭제후", registerForm);
      }
      newMarker.setMap(null);
      newTooltip.setMap(null);
    });
  }
});
tfRegiBtn.addEventListener("click", function () {
  if (tfSelect.value == "") alert("정류장을 선택하세요");
  else if (depthSelect.value == "") alert("출발/환승/도착역 여부를 선택하세요");
  else {
    let tfKey = `${depthSelect.value}${categ_select.value}`;
    let tfVal =
      categ_select.value == "Sub"
        ? tfSelect.value.split("_")[1]
        : tfSelect.value.split("_")[0];
    console.log(tfVal);
    if (tfKey in registerForm) registerForm[tfKey].push(tfVal);
    else registerForm[tfKey] = [tfVal];
    console.log("등록후", registerForm);
    let position = new kakao.maps.LatLng(current.lat, current.lon);
    let newMarker = addMarker(position);
    let content =
      '<div class="infoBox">' +
      `<span id="${depthSelect.value}${categ_select.value}">${tfSelect.value}</span>` +
      "<span>삭제하려면 마커를 클릭</span>" +
      "</div>";
    console.log(current);
    current.marker.setMap(null);
    current.tooltip.setMap(null);
    let newTooltip = addTooltip(position, content);
    newTooltip.setMap(map);
    current.tooltip = newTooltip;
    tfBox.classList.remove("transfer_box");
    tfBox.classList.add("hidden");
    //마커 클릭 이벤트로 마커 및 툴팁삭제
    kakao.maps.event.addListener(newMarker, "click", function () {
      console.log("삭제전", registerForm);
      console.log(newTooltip.cc);
      if (!newTooltip.cc.includes("여기에 등록하시겠습니까?")) {
        let delKey;
        let keys = Object.keys(registerForm);
        for (let i = 0; i < keys.length; i++) {
          if (newTooltip.cc.includes(keys[i])) {
            console.log(keys[i]);
            delKey = keys[i];
            break;
          }
        }
        console.log(delKey);
        delete registerForm[`${delKey}`];
        console.log("삭제후", registerForm);
      }
      newMarker.setMap(null);
      newTooltip.setMap(null);
    });
  }
});

async function callRegister(token) {
  try {
    const response = await fetch(`/api/journey`, {
      method: "POST",
      credentials: "include",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(registerForm),
    });
    const data = await response.json();
    if (!response.ok) throw Error(data.msg);
    return data;
  } catch (error) {
    console.log(error);
    return null;
  }
}

// 경로등록 유효성 검사
const registerBtn = document.querySelector("#registerBtn");
registerBtn.addEventListener("click", async function () {
  //컨펌창을 띄워서 확인시킨 후 fetch 요청
  const confirm = window.confirm("해당 마커들로 경로를 등록 하시겠습니까?");
  let keys = Object.keys(registerForm);
  let vehicleFilter = keys.filter((key) => {
    !key.includes("Bus") && !key.includes("Bike") && !key.includes("Sub");
  });
  let busFilter = keys.filter((key) => key.includes("Bus"));
  if (confirm) {
    if (registerForm == {}) alert("마커를 등록하세요.");
    else if (!keys.includes("fromName") || !keys.includes("toName"))
      alert("출발지와 도착지는 모두 있어야 합니다.");
    else if (vehicleFilter.length > 0) alert("이동 수단을 등록해 주세요.");
    else if (busFilter.length == 1)
      alert(
        "버스 정류장은 출발/환승/도착 중 2개 영역 이상에 있거나 아예 없어야 합니다."
      );
    else {
      document.querySelector("#modal_wrap").className = "register_modal";
    }
  }
});

// 최종 경로 등록
const createJourney = document.querySelector("#create_journey");
const radio = document.querySelector('input[name="direction"]:checked');
createJourney.addEventListener("click", async function () {
  registerForm["direction"] = radio.value;
  let register = await callRegister(sessionStorage.getItem("token"));
  if (register == null) {
    let newToken = await refreshCall();
    register = await callRegister(newToken);
  } else {
    console.log(register);
    window.location.href = "/view/main";
  }
});
