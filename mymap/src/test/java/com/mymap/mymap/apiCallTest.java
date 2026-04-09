// Test rerun forced 
package com.mymap.mymap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

import org.json.XML;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class apiCallTest {
    @Value("${topis.key}")
    private String topisKey;
    

    private Set<String> getRouteIds(String stId, String arsId, boolean isSeoul) throws Exception{
        StringBuilder url = new StringBuilder();
        if(isSeoul){
            url.append("http://ws.bus.go.kr/api/rest/stationinfo/getRouteByStation");
            url.append("?serviceKey="+topisKey+"&arsId="+arsId);
        } else {
            url.append("https://apis.data.go.kr/6410000/busarrivalservice/v2/getBusArrivalListv2");
            url.append("?serviceKey="+topisKey+"&stationId="+stId+"&format=json");
        }
        URL realurl = new URL(url.toString());
        HttpURLConnection conn = (HttpURLConnection) realurl.openConnection();
        conn.setConnectTimeout(3000); 
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        String response = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)).readLine();
        //System.out.println("response: "+response);
        JSONObject jsonObject = isSeoul ? XML.toJSONObject(response) : new JSONObject(response);
        String jsonPrettyPrintString = jsonObject.toString(4);
        System.out.println("JSON Response:\n" + jsonPrettyPrintString);
        JSONObject msgBody = jsonObject.getJSONObject(isSeoul ? "ServiceResult" : "response").getJSONObject("msgBody");
        JSONArray busArrivalList = msgBody.optJSONArray("busArrivalList");
        if(busArrivalList==null){
            busArrivalList = new JSONArray();
            busArrivalList.put(msgBody.getJSONObject("busArrivalList"));
        }
        //System.out.println("busArrivalList: " + busArrivalList);
        Set<String> busRouteIds = new HashSet<>();
        for (int i = 0; i < busArrivalList.length(); i++) {
            String routeId = Integer.toString(busArrivalList.getJSONObject(i).getInt(isSeoul ? "busRouteId" : "routeId")); 
            busRouteIds.add(routeId);
        }
        return busRouteIds;
    }

    @Test
    public void testApiCall() {
        HashMap<String,String> stToArs = new HashMap<>();
        stToArs.put("220000018","37500"); //2인데 arsid 서울인거 
        stToArs.put("114900082","15501"); // 아예 서울인거 
        stToArs.put("200000068","01002"); // arsid 같은 거 중에 경기인거 
        stToArs.put("100000002","01002"); // arsid 같은 거 중에 서울인거 

        Map<String,Set<String>> routes = new ConcurrentHashMap<>();
        for(String stId : stToArs.keySet()){
            boolean isSeoul = stId.charAt(0)=='1';
            try {
                Set<String> busRouteIds = getRouteIds(stId, stToArs.get(stId), isSeoul);
                log.info("stid, busRouteIds: {}, {}",stId,busRouteIds);
                routes.put(stId,busRouteIds);
            } catch (Exception e){
                System.out.println("error: \n"+e);
                try{
                    Set<String> busRouteIds = getRouteIds(stId, stToArs.get(stId), !isSeoul);
                    log.info("retry stid, busRouteIds: {}, {}",stId,busRouteIds);
                    routes.put(stId,busRouteIds);
                } catch (Exception e2){
                    System.out.println("retry error: \n"+e2);
                }
            }
        }
        log.info("routes : {}",routes);
    }

    // xml xpath 를 이용한 레거시 
            // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // DocumentBuilder builder = factory.newDocumentBuilder();
            // InputSource inputSource = new InputSource(new StringReader(response));
            // Document document = builder.parse(inputSource);
            // // // 특정 xml 태그 abstract
            // XPath xpath = XPathFactory.newInstance().newXPath();
            // System.out.println("bus filter call line 99: "+ document.getTextContent());
            // XPathExpression expr = xpath.compile("//itemList/busRouteId");
            // NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            // Set<String> busRouteIds = new HashSet<>();
            // for (int i = 0; i < nodes.getLength(); i++) {
            //     String routeId = nodes.item(i).getTextContent();
            //     System.out.println("routeId: "+routeId);
            //     //busRouteIds.add(routeId);
            //     //System.out.println(id+","+routeId);
            // }
}
