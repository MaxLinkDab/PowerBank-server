package com.td.common_service.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.td.common_service.annotation.HttpClientLog;
import com.td.common_service.model.DeviceInfo;
import com.td.common_service.service.jedis.RedisService;
import com.td.util.UtilTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@Component
@Slf4j
public class HttpClientUtils {
    @Autowired
    RedisService redisService;

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtils.class);

    private RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(10000)
            .setSocketTimeout(10000)
            .setConnectTimeout(10000)
            .build();
    @Async
    public void callback(DeviceInfo info, JSONObject obj) {
        /**
         * ???????????????
         * deviceNo+callback???{string1:0,string2:0,...} 0:??????????????????????????????????????????1:???????????????????????????????????????
         */
        String deviceUuid = info.getDeviceUuid();
        String s = obj.toJSONString();

        // ?????????MD5??????
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // ?????????????????????
        byte[] bs = md.digest(s.getBytes());
        /*
         * BASE64Encoder???????????????????????????Eclipse?????? ???Java Build Path??????
         * Libraries?????????JRE??????????????? ???????????????Access rules???????????????????????????add???????????????????????????
         * Accessible???????????????**????????????????????????????????????
         *
         * BASE64Encoder??????????????????????????????????????????
         */
        // ??????????????????????????????????????????????????????
        BASE64Encoder base = new BASE64Encoder();
        // ???????????????
        String key = base.encode(bs);
        Integer i = (Integer)redisService.hget(deviceUuid+"callback", key);

        //???????????????????????????????????????????????????????????????????????????????????????
        if(UtilTool.isNull(i)){
            redisService.hset(deviceUuid+"callback",key,0);
            //??????????????????????????????????????????30s???????????????30s?????????????????????????????????
            String result;
            obj.put("key",key);
            obj.put("execTime",new Date().getTime());
            while(true){
                result = doPostJson(info.getUrl(), obj.toJSONString());
                if(true || StringUtils.isNotBlank(result)){
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String key1 ;//= (String)jsonObject.get("key");
                    key1=obj.getString("key");
                    String deviceUuid1 = //(String)jsonObject.get("deviceUuid");
                    deviceUuid1=obj.getString("deviceUuid");
                    if(StringUtils.isNotBlank(key1) && StringUtils.isNotBlank(deviceUuid1)){
                        Integer i1 = (Integer)redisService.hget(deviceUuid + "callback", key);
                        if(!UtilTool.isNull(i1)){
                            redisService.hset(deviceUuid1+"callback",key1,1);
                        }else{
                            log.info("key?????????");
                        }
                        try{
                            //5s????????????
                            Thread.sleep(1000*5);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        Integer i2 = (Integer)redisService.hget(deviceUuid + "callback", key);
                        if(i2 == 1){
                            //????????????????????????1??????????????????????????????
                            redisService.hdel(deviceUuid+"callback", key);
                            break;
                        }
                    }
                }
            }
        }else{
            log.info("---------------------???????????????????????????----------------------");
        }
    }
    /**
     * post????????????map??????
     */
    public String sendPostDataByMap(String url, Map<String, String> map, String encoding) throws ClientProtocolException, IOException {
        String result = "";

        // ??????httpclient??????
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // ??????post??????????????????
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        // ????????????
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if (map != null) {
            for (Entry<String, String> entry : map.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        // ??????????????????????????????
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, encoding));

        // ??????header??????
        // ??????????????????Content-type?????????User-Agent???
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        // ??????????????????????????????????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);
        // ??????????????????
        // ???????????????????????????????????????(0--200????????????)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        // ????????????
        response.close();

        return result;
    }

    /**
     * <p>Title: doPostJson</p>
     * <p>Description: </p>
     *
     * @param url
     * @param json
     * @return
     */
    @HttpClientLog//todo ?? ?????????????? ??????
    public String doPostJson(String url, String json) {
        // ??????Httpclient??????
        return "";
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = null;
//        String resultString = "";
//        try {
//            // ??????Http Post??????
//            HttpPost httpPost = new HttpPost(url);
//            httpPost.setConfig(requestConfig);
//            // ??????????????????
//            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
//            httpPost.setEntity(entity);
//            // ??????http??????
//            response = httpClient.execute(httpPost);
//            resultString = EntityUtils.toString(response.getEntity(), "utf-8");
//        } catch (Exception e) {
//            LOG.error("????????????, ?????????:{}, ???????????????:{}", json, e.getMessage());
//           /* HttpClientError httpClientError = new HttpClientError();
//            httpClientError.setUrl(url);
//            httpClientError.setParameter(json);
//            httpClientError.setException(e.getMessage());
//            httpClientError.setCreatedTime(new Date());
//            HttpClientErrorService httpClientErrorService = SpringUtils.getBean(HttpClientErrorService.class);
//            httpClientErrorService.insert(httpClientError);*/
//        } finally {
//            try {
//                if (response != null) {
//                    response.close();
//                }
//                if (httpClient != null) {
//                    httpClient.close();
//                }
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//        return resultString;
    }

    //???????????????
    public String getTimeStamp() {
        Date parse = null;
        try {
            parse = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    .parse(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                            .format(new Date(System.currentTimeMillis())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse);
        long inMillis = calendar.getTimeInMillis();
        String timestamp = String.valueOf(inMillis).substring(0, 10);
        return timestamp;
    }


    /**
     * post????????????json??????
     */
    public String sendPostDataByJson(String url, String json, String encoding) throws ClientProtocolException, IOException {
        String result = "";
        // ??????post??????????????????
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);

        Map apiCase = JSON.parseObject(json);

        Set<String> keys = apiCase.keySet();
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (String key : keys) {
            formparams.add(new BasicNameValuePair(key, (String) apiCase.get(key)));
        }
        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("form???????????????");
            e.printStackTrace();
        }
        httpPost.setEntity(entity);

//        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        httpPost.addHeader("X-Powered-By", "PHP/5.5.12");
        httpPost.addHeader("Server", "Apache/2.4.9 (Win32) PHP/5.5.12");
        httpPost.addHeader("Keep-Alive", "timeout=5, max=100");
        httpPost.addHeader("Connection", "Keep-Alive");

        // ??????????????????????????????????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);

        // ??????????????????
        // ???????????????????????????????????????(0--200????????????)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        // ????????????
        response.close();

        return result;
    }


    /**
     * get??????????????????
     */
    public String sendGetData(String url, String encoding) throws ClientProtocolException, IOException {
        String result = "";

        // ??????httpclient??????
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // ??????get??????????????????
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.addHeader("Content-type", "application/x-www-form-urlencoded");
//        httpGet.addHeader("Content-type", "application/json; charset=UTF-8");
        // ????????????????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        // ??????????????????
        // ???????????????????????????????????????(0--200????????????)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        // ????????????
        response.close();

        return result;
    }



    public String sendGetData(String url, String encoding, String appToken) throws ClientProtocolException, IOException {
        String result = "";

        // ??????httpclient??????
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // ??????get??????????????????
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        httpGet.addHeader("Content-type", "application/json");
        httpGet.addHeader("app-token", appToken);
        // ????????????????????????????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        // ??????????????????
        // ???????????????????????????????????????(0--200????????????)
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            result = EntityUtils.toString(response.getEntity(), "utf-8");
        }
        // ????????????
        response.close();

        return result;
    }

    public String sendPostDataByJsonA(String url, String jsonParam, boolean flag) throws Exception {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (!flag)
            configureHttpClient(httpClientBuilder);
        org.apache.http.client.HttpClient httpClient = httpClientBuilder.build();

        StringEntity requestEntity = new StringEntity(jsonParam, "utf-8");
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("content-type", "application/json;charset=UTF-8");
        httpPost.addHeader("Accept", "application/json");
        httpPost.setEntity(requestEntity);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        InputStream inputStream = httpEntity.getContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String s;
        StringBuilder stringBuilder = new StringBuilder();
        while ((s = bufferedReader.readLine()) != null) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();


    }

    /**
     * ????????????SSL??????
     *
     * @param clientBuilder
     */
    public void configureHttpClient(HttpClientBuilder clientBuilder) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
                return true;
            }
        }).build();
        //NoopHostNameVerifer ?????????????????????SSL???????????????????????????
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        clientBuilder.setSSLSocketFactory(sslsf);
    }

//    @org.junit.jupiter.api.Test
    public void testSendPostDataByMap() throws ClientProtocolException, IOException {
        String url = "http://localhost:8080/httpService/sendPostDataByMap";
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "wyj");
        map.put("city", "??????");
        String body = sendPostDataByMap(url, map, "utf-8");
        System.out.println("???????????????" + body);
    }

//    @org.junit.jupiter.api.Test
    public void testSendPostDataByJson() throws ClientProtocolException, IOException {
        String url = "http://localhost:8080/httpService/sendPostDataByJson";
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "wyj");
        map.put("city", "??????");
        String body = sendPostDataByJson(url, JSON.toJSONString(map), "utf-8");
        System.out.println("???????????????" + body);
    }

//    @org.junit.jupiter.api.Test
    public void testSendGetData() throws ClientProtocolException, IOException {
        String url = "http://localhost:8080/httpService/sendGetData?name=wyj&city=??????";
        String body = sendGetData(url, "utf-8");
        System.out.println("???????????????" + body);
    }
}
