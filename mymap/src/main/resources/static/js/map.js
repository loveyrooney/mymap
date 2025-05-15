document.addEventListener("DOMContentLoaded", function() {
    const mapContainer = document.getElementById("map"), // 지도를 표시할 div
       mapOption = {
           center: new kakao.maps.LatLng(33.450701, 126.570667),
           level: 1, // 지도의 default 확대 레벨
       };
    const map = new kakao.maps.Map(mapContainer, mapOption); // 지도 생성
});
