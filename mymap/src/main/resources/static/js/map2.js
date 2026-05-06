// 네이버 지도 관련 변수
let bounds = new naver.maps.LatLngBounds();
let current = { marker: null, lat: "", lon: "", tooltip: null };
let registerForm = {};

// 지도 생성
const mapContainer = document.getElementById("map");
let map;

/* 커스텀 오버레이 정의 */
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
    pane.appendChild(this._element);
  };
  CustomOverlay.prototype.draw = function() {
    if (!this.getMap()) return;
    const projection = this.getProjection();
    const pixelAt = projection.fromCoordToOffset(this._position);
    this._element.style.left = (pixelAt.x - (this._element.offsetWidth / 2)) + 'px';
    this._element.style.top = (pixelAt.y - this._element.offsetHeight - 50) + 'px';
  };
  CustomOverlay.prototype.onRemove = function() {
    this._element.parentNode.removeChild(this._element);
  };
  return CustomOverlay;
}
let CustomOverlay;

naver.maps.onJSContentLoaded = function() {
    const mapOption = {
        center: new naver.maps.LatLng(37.5668444, 126.9786296),
        zoom: 15,
    };
    map = new naver.maps.Map(mapContainer, mapOption);

    // 맵에서 클릭해서 좌표 찾기
    naver.maps.Event.addListener(map, "click", function (e) {
      let latlng = e.coord;
      let position = new naver.maps.LatLng(latlng.lat(), latlng.lng());
      
      if (current.marker) current.marker.setMap(null);
      if (current.tooltip) current.tooltip.setMap(null);

      let marker = addMarker(position);
      let content =
        '<div class="infoBox">' +
        `<span>여기에 등록하시겠습니까?</span>` +
        "<span>삭제하려면 마커를 클릭</span>" +
        "</div>";
      let tooltip = addTooltip(position, content);
      
      current.marker = marker;
      current.lat = latlng.lat();
      current.lon = latlng.lng();
      current.tooltip = tooltip;

      // 마커 클릭 이벤트로 삭제
      naver.maps.Event.addListener(marker, "click", function () {
        marker.setMap(null);
        tooltip.setMap(null);
        current.marker = null;
        current.tooltip = null;
        current.lat = "";
        current.lon = "";
      });
    });
};

// 마커 생성 함수
function addMarker(position) {
  const marker = new naver.maps.Marker({
    map: map,
    position: position,
  });
  return marker;
}

// 툴팁 생성 함수 (CustomOverlay 사용)
function addTooltip(position, content) {
  if (!CustomOverlay) CustomOverlay = createCustomOverlay();
  let overlay = new CustomOverlay({
    position: position,
    content: content,
    map: map
  });
  return overlay;
}

// 주소로 좌표 찾기 (네이버 Geocoder 서비스 사용)
const searchBtn = document.querySelector("#searchBtn");
const addrInput = document.querySelector("#addr");

searchBtn.addEventListener("click", function () {
  let keyword = addrInput.value;
  if (!keyword) return;

  naver.maps.Service.geocode({
    query: keyword
  }, function(status, response) {
    if (status !== naver.maps.Service.Status.OK) {
      return alert('주소 검색 결과가 없습니다.');
    }

    let result = response.v2.addresses[0];
    let position = new naver.maps.LatLng(result.y, result.x);
    
    if (current.marker) current.marker.setMap(null);
    if (current.tooltip) current.tooltip.setMap(null);

    let marker = addMarker(position);
    let content =
      '<div class="infoBox">' +
      `<span>여기에 등록하시겠습니까?</span>` +
      "<span>삭제하려면 마커를 클릭</span>" +
      "</div>";
    let tooltip = addTooltip(position, content);
    
    current.marker = marker;
    current.lat = result.y;
    current.lon = result.x;
    current.tooltip = tooltip;
    
    if(map) map.setCenter(position);
    addrInput.value = "";

    naver.maps.Event.addListener(marker, "click", function () {
      marker.setMap(null);
      tooltip.setMap(null);
    });
  });
});

// ... (이후 로직 생략 없이 websocket_naver.js와 동일하게 유지)

// refresh token auth
async function refreshCall() {
  try {
    const res = await fetch("/auth/refresh", {
      method: "GET",
      credentials: "include",
    });
    const refreshData = await res.json();
    if (res.ok) {
      sessionStorage.setItem("token", refreshData.accessToken);
    }
    return refreshData;
  } catch (e) {
    return null;
  }
}

// call transfer id list
async function callTransfer(token, vehicle, lat, lon) {
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
    if (!response.ok) throw Error(data.error || data.msg || "API request failed");
    return data;
  } catch (error) {
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
      fromTo.className = "hidden";
    }
    tfBox.classList.remove("hidden");
    tfBox.classList.add("transfer_box");
    let token = sessionStorage.getItem("token");
    let tfLists = await callTransfer(token, val, current.lat, current.lon);
    if (tfLists == null) {
      let newToken = await refreshCall();
      tfLists = await callTransfer(newToken.accessToken, val, current.lat, current.lon);
    }
    
    tfSelect.replaceChildren();
    tfLists.forEach((tf) => {
      let op = document.createElement("option");
      op.textContent = `${tf.tfId}_${tf.stName}`;
      tf.stId ? op.setAttribute("value", `${tf.stId}_${tf.stName}`) : op.setAttribute("value", `${tf.tfId}_${tf.stName}`);
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
    
    let position = new naver.maps.LatLng(current.lat, current.lon);
    let newMarker = addMarker(position);
    let content =
      '<div class="infoBox">' +
      `<span id="${categ_select.value}_${geoms}">${fromToInput.value}</span>` +
      "<span>삭제하려면 마커를 클릭</span>" +
      "</div>";
      
    if(current.marker) current.marker.setMap(null);
    if(current.tooltip) current.tooltip.setMap(null);
    
    let newTooltip = addTooltip(position, content);
    
    fromToInput.value = "";
    fromTo.className = "hidden";

    naver.maps.Event.addListener(newMarker, "click", function () {
      let delKey = [];
      let keys = Object.keys(registerForm);
      keys.forEach(k => {
          if(k.includes(categ_select.value)) delKey.push(k);
      });
      delKey.forEach(k => delete registerForm[k]);
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
    let tfVal = categ_select.value == "Sub" ? tfSelect.value.split("_")[1] : tfSelect.value.split("_")[0];
    
    if (tfKey in registerForm) registerForm[tfKey].push(tfVal);
    else registerForm[tfKey] = [tfVal];
    
    let position = new naver.maps.LatLng(current.lat, current.lon);
    let newMarker = addMarker(position);
    let content =
      '<div class="infoBox">' +
      `<span id="${depthSelect.value}${categ_select.value}">${tfSelect.value}</span>` +
      "<span>삭제하려면 마커를 클릭</span>" +
      "</div>";
      
    if(current.marker) current.marker.setMap(null);
    if(current.tooltip) current.tooltip.setMap(null);
    
    let newTooltip = addTooltip(position, content);
    
    tfBox.className = "hidden";

    naver.maps.Event.addListener(newMarker, "click", function () {
      delete registerForm[tfKey];
      newMarker.setMap(null);
      newTooltip.setMap(null);
    });
  }
});

// 경로 등록 API 호출
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
    if (!response.ok) throw Error(data.error || data.msg || "API request failed");
    return data;
  } catch (error) {
    return null;
  }
}

// 최종 경로 등록 버튼
const createJourney = document.querySelector("#create_journey");
const registerBtn = document.querySelector("#registerBtn");

registerBtn.addEventListener("click", async function () {
  const confirm = window.confirm("해당 마커들로 경로를 등록 하시겠습니까?");
  let keys = Object.keys(registerForm);
  if (confirm) {
    if (keys.length === 0) alert("마커를 등록하세요.");
    else if (!keys.includes("fromName") || !keys.includes("toName"))
      alert("출발지와 도착지는 모두 있어야 합니다.");
    else {
      document.querySelector("#modal_wrap").className = "register_modal";
    }
  }
});

createJourney.addEventListener("click", async function () {
  const radio = document.querySelector('input[name="direction"]:checked');
  createJourney.disabled = true;
  registerForm["direction"] = radio.value;
  
  let register = await callRegister(sessionStorage.getItem("token"));
  if (register) {
    window.location.href = "/view/main";
  }
  createJourney.disabled = false;
});

// 네이버 경로 파싱 버튼
const parseNaverBtn = document.getElementById("parseNaverBtn");
if (parseNaverBtn) {
  parseNaverBtn.addEventListener("click", () => {
    const naverShareUrl = document.getElementById("naverShareUrl").value;
    if (!naverShareUrl) {
      alert("공유 링크를 입력해주세요.");
      return;
    }
    
    fetch("/api/parse-naver", {
      method: "POST",
      headers: {
              Accept: "application/json",
              "Content-Type": "application/json",
              Authorization: `Bearer ${sessionStorage.getItem("token")}`,
          },
      body: JSON.stringify({ url: naverShareUrl })
    })
    .then(res => res.json())
    .then(data => {
      if (data.error) {
        alert("파싱 실패: " + data.error);
        return;
      }
      
      if (data.startPoint) {
        registerForm["fromName"] = data.startPoint.name;
        registerForm["fromGeoms"] = [data.startPoint.y, data.startPoint.x];
        let pos = new naver.maps.LatLng(data.startPoint.y, data.startPoint.x);
        addMarker(pos);
        bounds.extend(pos);
      }
      
      if (data.goalPoint) {
        registerForm["toName"] = data.goalPoint.name;
        registerForm["toGeoms"] = [data.goalPoint.y, data.goalPoint.x];
        let pos = new naver.maps.LatLng(data.goalPoint.y, data.goalPoint.x);
        addMarker(pos);
        bounds.extend(pos);
      }
      
      if(map) map.panToBounds(bounds);
      alert("경로 파싱에 성공했습니다!");
    });
  });
}

// 가이드 토글 이벤트
const guideBtn = document.querySelector("#guide");
const guideWrap = document.querySelector("#guide_wrap");
const guideCloseBtn = document.querySelector("#guide_wrap_close");

if (guideBtn && guideWrap) {
  guideBtn.addEventListener("click", () => {
    guideWrap.classList.toggle("hidden");
  });
}
if (guideCloseBtn && guideWrap) {
  guideCloseBtn.addEventListener("click", () => {
    guideWrap.classList.add("hidden");
  });
}
