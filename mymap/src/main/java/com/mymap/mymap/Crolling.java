package com.mymap.mymap;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
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
}
