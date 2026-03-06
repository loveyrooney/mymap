package com.mymap.controller;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Crawling {
    
    // In a real scenario, we might want to inject URL from properties
    // @Value("${topis.url}")
    // private String topisUrl;
    private static final String TOPIS_URL_OPEN_NOTICE = "https://topis.seoul.go.kr/notice/openNoticeList.do";
    private static final String TOPIS_URL_SELECT_NOTICE = "https://topis.seoul.go.kr/notice/selectNoticeList.do";

    public void crawlJsoup() {
        try {
            Document document = Jsoup.connect(TOPIS_URL_OPEN_NOTICE).get();

            log.info("Page Title: {}", document.title());

            Elements links = document.select("a[href]");
            for (Element link : links) {
                log.info("Link: {} Text: {}", link.attr("href"), link.text());
            }

        } catch (IOException e) {
            log.error("Crawl error", e);
        }
    }

    public List<String> crawlSelenium() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); 

        WebDriver driver = new ChromeDriver(options);
        driver.get(TOPIS_URL_OPEN_NOTICE); 

        List<String> hrefs = driver.findElements(By.className("tit-ell"))
                        .stream()
                        .map(element -> element.getText())
                        .collect(Collectors.toList());
        
        driver.quit();
        return hrefs;
    }

    public void callApi() {
        try {
            URL url = new URL(TOPIS_URL_SELECT_NOTICE);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setDoOutput(true);

            String jsonInputString = "{\"name\":\"John\", \"age\":30}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int statusCode = connection.getResponseCode();
            log.info("Response Code: {}", statusCode);

            if (statusCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray arr = jsonResponse.getJSONArray("rows");
                for(int i=0; i<50; i++){
                    // Using direct index access might be risky if array size < 50
                    // But keeping original logic for now, wrapped in try/catch loop if needed
                    // Actually original code looped to 50 hardcoded.
                    if (i < arr.length()) {
                       log.info(arr.getJSONObject(i).toString());
                    }
                }

            } else {
                log.error("Request failed. HTTP code: {}", statusCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            log.error("API call error", e);
        }
    }
}
