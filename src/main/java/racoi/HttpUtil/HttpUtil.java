package racoi.HttpUtil;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.batch.core.configuration.xml.ExceptionElementParser;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private String init_url = "http://kr.lgtvsdp.com/rest/sdp/v13.0/initservices?api_info=auth:Y";
    private String search_url = "http://kr.lgtvsdp.com/rest/sdp/v13.0/search/retrieval?";
    private String search_detail_url = "http://kr.lgtvsdp.com/rest/sdp/v13.0/content_discovery/item-detail?";

    private HashMap<String, String> headers = new HashMap<String, String>();

    public void set_headers_default() {
        headers.put("x-authentication", "GHyflV5ml72mxyc5rWbQDs1gB4A=");
        headers.put("x-device-brand", "LG_LGELECTRONICS_LG");
        headers.put("x-device-contentsqa-flag", "N");
        headers.put("x-device-country", "KR");
        headers.put("x-device-country-group", "KR");
        headers.put("x-device-eco-info", "1");
        headers.put("x-device-eula", "additionalDataAllowed,networkAllowed,chpAllowed,voice2Allowed,generalTermsAllowed,customAdAllowed,voiceAllowed");
        headers.put("x-device-fck", "504");
        headers.put("x-device-fw-version", "00.00.00");
        headers.put("x-device-id", "dsN2U5tIkwWTV87KpkiXo3iAf7k2EbKIt6aOEzC6tz4aZTG4B3576cexk5hG5/ESDUuTOjhLljyBEhCHX2g3LVoFan+fX/SW/cv7dRUsGNMe+voD9bdnqC537RGwcFHM");
        headers.put("x-device-language", "ko-KR");
        headers.put("x-device-locale", "ko-KR");
        headers.put("x-device-model", "HE_DTV_W22O_AFABATAA");
        headers.put("x-device-netcast-platform-version", "7.0.0");
        headers.put("x-device-platform", "W22O");
        headers.put("x-device-product", "webOSTV 7.0");
        headers.put("x-device-publish-flag", "N");
        headers.put("x-device-remote-flag", "N");
        headers.put("x-device-sales-model", "WEBOS22");
        headers.put("x-device-sdk-version", "7.0.0");
        headers.put("x-device-spokenlanguage", "ko-KR");
        headers.put("x-device-subdivision", "A%20%EC%84%9C%EC%9A%B8%20%EA%B0%95%EB%82%A8%EA%B5%AC%20%EB%8F%84%EA%B3%A1%EB%8F%99");
        headers.put("x-device-type", "T01");
        //headers.put("Content-Type","application/json;charset=UTF-8");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept", "*/*");
        headers.put("Connection", "Keep-Alive");
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept-Language", "UTF-8");

    }

    public HttpUtil() {
        set_headers_default();
    }

    public boolean initService(int timeout, int repeat) {

        if (repeat == 0) {
            return false;
        }

        try {
            URL url = new URL(init_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestMethod("POST");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //TimeUnit.SECONDS.sleep(1);
                System.out.println(conn.getResponseCode());
                return initService(timeout, repeat - 1);
            }

            //http 요청 후 응답 받은 데이터를 버퍼에 쌓는다
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String responseData = "";

            while ((responseData = br.readLine()) != null) {
                sb.append(responseData); //StringBuffer에 응답받은 데이터 순차적으로 저장 실시
            }

            //http 요청 응답 코드 확인 실시
            JSONObject res = new JSONObject(sb.toString());
            String sessionID = res.getJSONObject("initServices").getJSONObject("authentication").getString("sessionID");
            System.out.println("call init service");
            System.out.println("sessionID : " + sessionID);
            headers.put("x-authentication", sessionID);
            conn.disconnect();

        } catch (SocketTimeoutException e) {
            System.out.println("timeout");
            return initService(timeout, repeat-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public JSONObject call_searchAPI(String program, int timeout, int repeat) {

        JSONObject res = null;
        if (repeat == 0) {
            return res;
        }

        try {
            String program_ = program.replace(" ", "").replace("%", "");

            String s_url = search_url + "query=" + program_ + "&service=universal&start_index=1&max_results=10&domain=tvshow";
            URL url = new URL(s_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestMethod("POST");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //TimeUnit.SECONDS.sleep(1);
                System.out.println(conn.getResponseCode());
                Thread.sleep(1000);
                return call_searchAPI(program, timeout, repeat - 1);
            }
            //http 요청 후 응답 받은 데이터를 버퍼에 쌓는다
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String responseData = "";

            while ((responseData = br.readLine()) != null) {
                sb.append(responseData); //StringBuffer에 응답받은 데이터 순차적으로 저장 실시
            }

            //http 요청 응답 코드 확인 실시
            res = new JSONObject(sb.toString());
            conn.disconnect();
        } catch (SocketTimeoutException e) {
            System.out.println("timeout");
            return call_searchAPI(program, timeout, repeat-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public JSONObject call_detailAPI(String contents_set_id, String content_id, int timeout, int repeat) {
        JSONObject res = null;

        if (repeat == 0) {
            return res;
        }

        try {
            String s_url = search_detail_url + "item_id=" + contents_set_id + "|" + content_id + "&item_type=CONTS&item_detail_type=advanced&app_id=com.lge.rcmd.api.client";
            URL url = new URL(s_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestMethod("POST");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                conn.setRequestProperty(header.getKey(), header.getValue());
            }

            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                //TimeUnit.SECONDS.sleep(1);
                System.out.println(conn.getResponseCode());
                Thread.sleep(1000);
                return call_detailAPI(contents_set_id, content_id, timeout, repeat - 1);
            }

            //http 요청 후 응답 받은 데이터를 버퍼에 쌓는다
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String responseData = "";

            while ((responseData = br.readLine()) != null) {
                sb.append(responseData); //StringBuffer에 응답받은 데이터 순차적으로 저장 실시
            }

            //http 요청 응답 코드 확인 실시
            res = new JSONObject(sb.toString());
            conn.disconnect();

        } catch (SocketTimeoutException e) {
            System.out.println("timeout");
            return call_detailAPI(contents_set_id, content_id, timeout, repeat-1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
