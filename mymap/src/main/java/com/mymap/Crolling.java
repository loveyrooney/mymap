package com.mymap;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class Crolling {
    public static void crollJsoup() {
        try {
            // 크롤링할 웹 페이지 URL
            String url = "https://topis.seoul.go.kr/notice/openNoticeList.do"; // 실제 URL로 변경

            // Jsoup을 사용해 웹 페이지를 Document 객체로 가져오기
            Document document = Jsoup.connect(url).get();

            // 페이지의 제목을 출력
            System.out.println("Page Title: " + document.title());

            // 특정 HTML 요소를 선택하여 출력 (예: <h1> 태그)
//            Elements headings = document.select("h1");
//            for (Element heading : headings) {
//                System.out.println("Heading: " + heading.text());
//            }

            // 특정 클래스가 적용된 모든 링크를 찾기
            Elements links = document.select("a[href]"); // 모든 <a> 태그의 href 속성
            for (Element link : links) {
                System.out.println("Link: " + link.attr("href") + " Text: " + link.text());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void crollSelenium() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // 헤드리스 모드로 실행

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://topis.seoul.go.kr/notice/openNoticeList.do");  // 크롤링할 사이트 URL

        // 게시글이 나열된 요소를 찾고 제목을 추출
        List<String> hrefs = driver.findElements(By.className("tit-ell"))
                        .stream()
                        .map(element -> element.getText())
                        .collect(Collectors.toList());
        for(String h : hrefs){
            System.out.println(h);
        }
        driver.quit();
    }

    public static void callApi() {
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
