package com.mymap.mymap.controller;

import com.mymap.mymap.Crolling;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        Crolling.crollSelenium();
        //Crolling.crollJsoup();
        //call();
        return "index";
    }

    public void call() {
        try {
            // 외부 API 엔드포인트 URL
            URL url = new URL("https://topis.seoul.go.kr/notice/selectNoticeList.do");

            // HTTP 연결 객체 생성
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 요청 메소드 설정 (GET, POST 등)
            connection.setRequestMethod("GET");

            // 요청 헤더 설정 (예시: Content-Type, Authorization 등)
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

            // 출력 스트림 활성화 (POST 요청에 필요한 경우)
            connection.setDoOutput(true);

            // JSON 데이터나 다른 데이터를 전송할 경우 (예시로 JSON 데이터 전송)
            String jsonInputString = "{\"name\":\"John\", \"age\":30}";

            // 요청 본문에 데이터 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
            int statusCode = connection.getResponseCode();
            System.out.println("Response Code: " + statusCode);

            // 응답이 성공적일 경우 JSON 응답을 처리
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // 응답 스트림 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // 응답 본문을 JSON 객체로 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());

                // 특정 키의 값 추출 (예: "name" 키의 값)
                JSONArray arr = jsonResponse.getJSONArray("rows");
                JSONObject first = arr.getJSONObject(0);
                for(int i=0; i<50; i++){
                    System.out.println(arr.getJSONObject(i).toString());
                }
                //System.out.println(first.toString());

            } else {
                System.out.println("Request failed. HTTP code: " + statusCode);
            }

        // 연결 종료
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
