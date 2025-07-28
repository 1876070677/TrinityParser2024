package cuk.api.Trinity;

import cuk.api.Logging.Logging;
import cuk.api.Trinity.Entities.ClassInfo;
import cuk.api.Trinity.Entities.CurrentGradeInfo;
import cuk.api.Trinity.Entities.TrinityInfo;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.SubjtNoRequest;
import cuk.api.Trinity.Response.GradesResponse;
import cuk.api.Trinity.Response.SujtResponse;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okhttp3.MediaType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.*;

/*
    트리니티에 실제로 요청을 보내는 클래스.
 */
@Component
@RequiredArgsConstructor
public class TrinityRepository {
    private final JSONParser parser;
    private final Logging logging;
    private final static String BASE_PATH = "https://uportal.catholic.ac.kr";

    //@{
    //
    // Common 섹션.
    //
    /*
        TrinityUser에 담겨있는 정보를 가지고 OkHttpClinet에 담을 쿠키를 생성.
     */
    public JavaNetCookieJar getCookieJar(CookieManager cookieManager, TrinityUser trinityUser) {
        CookieStore cookieStore = cookieManager.getCookieStore();

        for (Map.Entry<String, HashMap<String, Object>> entry : trinityUser.getCookies().entrySet()) {
            String cookieName = entry.getKey();
            HashMap<String, Object> cookieMap = entry.getValue();
            HttpCookie cookie = new HttpCookie(cookieName, (String) cookieMap.get("value"));
            cookie.setDomain((String) cookieMap.get("domain"));
            cookie.setPath((String) cookieMap.get("path"));
            cookie.setMaxAge((long) cookieMap.get("maxAge"));
            cookie.setSecure((boolean) cookieMap.get("secure"));
            cookie.setHttpOnly((boolean) cookieMap.get("httpOnly"));
            cookieStore.add(URI.create((String) cookieMap.get("domain") + (String) cookieMap.get("path")), cookie);
        }

        JavaNetCookieJar javaNetCookieJar = new JavaNetCookieJar(cookieManager);
        return javaNetCookieJar;
    }

    /*
        Response로부터 받은 Cookie를 TrinityUser 객체에 저장.
        다음 Request에 getCookieJar()을 활용해 Cookie를 실어서 보냄.
     */
    public void updateCookies(CookieStore cookieStore, TrinityUser trinityUser) {
        List<HttpCookie> cookies = cookieStore.getCookies();
        for (HttpCookie cookie : cookies) {
            HashMap<String, Object> cookieMap = new HashMap<>();
            cookieMap.put("value", cookie.getValue());
            cookieMap.put("domain", cookie.getDomain());
            cookieMap.put("path", cookie.getPath());
            cookieMap.put("maxAge", cookie.getMaxAge());
            cookieMap.put("secure", cookie.getSecure());
            cookieMap.put("httpOnly", cookie.isHttpOnly());
            trinityUser.addCookie(cookie.getName(), cookieMap);
        }
    }

    /*
        OkHttpClinet에서 커넥션을 유지하게 되면 객체가 오래 살아있기 때문에 강제로 커넥션을 끊어줌.
     */
    public void clientClear(OkHttpClient client) {
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdown();
    }
    //@}

    //@{
    //
    // Login 섹션.
    //
    /*
        로그인 과정의 첫 번째 단계.
        로그인 폼을 불러오면서 samlRequest 정보를 가져옴.
        samlRequest가 있어야지만, 다음 auth 과정 진행 가능.
     */
    public TrinityUser loginForm(TrinityUser trinityUser, CookieManager cookieManager, OkHttpClient httpClient) throws Exception {
        Request request = new Request.Builder()
                .url(BASE_PATH + "/sso/jsp/sso/ip/login_form.jsp")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                Document document = Jsoup.parse(response.body().string());
                Elements body = document.getElementsByTag("input");
                for (Element element : body) {
                    if ("samlRequest".equals(element.attr("name"))) {
                        trinityUser.setSamlRequest(element.attr("value"));
                        break;
                    }
                }
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }

        return trinityUser;
    }

    /*
        사용자로부터 받은 id, password를 바탕으로 실제 로그인 진행.
        SAMLResponse 정보를 받아옴.
        SAMLResponse를 Request body에 실어보내야지만, 다음 Login 과정 진행 가능.
     */
    public TrinityUser auth(TrinityUser trinityUser, CookieManager cookieManager, OkHttpClient httpClient) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("userId", trinityUser.getTrinityId())
                .add("password", trinityUser.getPassword())
                .add("samlRequest", trinityUser.getSamlRequest())
                .build();

        Request request = new Request.Builder()
                .url(BASE_PATH + "/sso/processAuthnResponse.do")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                Document document = Jsoup.parse(response.body().string());
                Elements body = document.getElementsByTag("input");
                for (Element element : body) {
                    if ("SAMLResponse".equals(element.attr("name"))) {
                        trinityUser.setSAMLResponse(element.attr("value"));
                    }
                }
                if (trinityUser.getSAMLResponse() == null) {
                    throw new Exception("아이디 또는 비밀번호를 잘못 입력했습니다.");
                }
            }
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }

        return trinityUser;
    }

    /*
        SAMLResponse를 보내고, csrf 토큰을 받아옴.
        앞으로의 요청에서 csrf 토큰을 가지고 사용자의 요청이 유효함을 판단.
     */
    public TrinityUser login(TrinityUser trinityUser, CookieManager cookieManager, OkHttpClient httpClient) throws Exception {

        RequestBody formBody = new FormBody.Builder()
                .add("SAMLResponse", trinityUser.getSAMLResponse())
                .build();

        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/portal/login/login.ajax")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                Document document = Jsoup.parse(response.body().string());
                Elements body = document.getElementsByTag("meta");
                for (Element element : body) {
                    if ("_csrf".equals(element.attr("id"))) {
                        trinityUser.set_csrf(element.attr("content"));
                        break;
                    }
                }
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }

        return trinityUser;
    }
    //@}

    //@{
    //
    // 사용자 정보 관리 섹션.
    //
    public TrinityUser getUserInfo(TrinityUser trinityUser, CookieManager cookieManager, OkHttpClient httpClient) throws Exception {
        RequestBody emptyBody = RequestBody.create("", MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/portal/menu/myInformation.ajax")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/json")
                .addHeader("x-csrf-token", trinityUser.get_csrf())
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/portal/main.do")
                .post(emptyBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONObject modelAndView = (JSONObject) data.get("modelAndView");
                JSONObject model = (JSONObject) modelAndView.get("model");
                JSONArray result = (JSONArray) model.get("result");

                JSONObject userInfo = (JSONObject) result.get(0);
                TrinityInfo trinityInfo = new TrinityInfo();
                trinityInfo.setTrinityInfo(userInfo);
                trinityUser.setTrinityInfo(trinityInfo);
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }
        return trinityUser;
    }

    public TrinityUser getSchoolInfo(TrinityUser trinityUser, CookieManager cookieManager, OkHttpClient httpClient) throws Exception {
        RequestBody emptyBody = RequestBody.create("", MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/portal/portlet/P044/shtmData.ajax")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/json")
                .addHeader("x-csrf-token", trinityUser.get_csrf())
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/portal/main.do")
                .post(emptyBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONObject modelAndView = (JSONObject) data.get("modelAndView");
                JSONObject model = (JSONObject) modelAndView.get("model");
                JSONObject result = (JSONObject) model.get("result");

                TrinityInfo trinityInfo = trinityUser.getTrinityInfo();

                trinityInfo.setSchoolInfo(result);
                trinityUser.setTrinityInfo(trinityInfo);
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }
        return trinityUser;
    }
    //@}

    /*
        성적 확인.
     */
    public GradesResponse getGrades(TrinityUser trinityUser) throws Exception {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        JavaNetCookieJar javaNetCookieJar = getCookieJar(cookieManager, trinityUser);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(javaNetCookieJar)
                .followRedirects(true)
                .build();

        TrinityInfo info = trinityUser.getTrinityInfo();
        if (info.getShtmFg() == null) {
            throw new Exception("휴학생 또는 졸업생의 경우, 조회가 불가능합니다.");
        }
        RequestBody formBody = new FormBody.Builder()
                .add("campFg", info.getCampFg())
                .add("tlsnYyyy", info.getShtmYyyy())
                .add("tlsnShtm", info.getShtmFg())
                .add("stdNo", info.getUserNo())
                .build();

        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/stw/scsr/ssco/findSninLectureScore.json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .addHeader("x-csrf-token", trinityUser.get_csrf())
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/stw/scsr/ssco/sscoSemesterGradesInq.do")
                .post(formBody)
                .build();

        GradesResponse gradesResponse = new GradesResponse();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONArray Scores = (JSONArray) data.get("DS_COUR_TALA010");
                for (Object obj : Scores) {
                    JSONObject score = (JSONObject) obj;

                    CurrentGradeInfo cgi = new CurrentGradeInfo();
                    cgi.setGradeInfo(score);
                    gradesResponse.addGrade(cgi);
                }
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더 또는 바디에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            clientClear(httpClient);
            httpClient = null;
        }

        return gradesResponse;
    }

    /*
        최대 수강 가능 인원.
        현재 수강 신청 인원.
     */
    public SujtResponse getSujtNo(TrinityUser trinityUser, SubjtNoRequest subjtNoRequest, CookieManager cookieManager, OkHttpClient httpClient, String shtm, String yyyy) throws Exception {

        TrinityInfo info = trinityUser.getTrinityInfo();
        RequestBody formBody = new FormBody.Builder()
                .add("quatFg", "INQ")
                .add("posiFg", "10")
                .add("openYyyy", yyyy)
                .add("openShtm", shtm)
                .add("campFg", info.getCampFg())
                .add("sustCd", "%")
                .add("corsCd", "|")
                .add("danFg", "")
                .add("pobtFgCd", "%")
                .build();
        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/stw/scsr/scoo/findOpsbOpenSubjectInq.json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .addHeader("x-csrf-token", trinityUser.get_csrf())
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/stw/scsr/scoo/scooOpsbOpenSubjectInq.do")
                .post(formBody)
                .build();

        SujtResponse sujtResponse = new SujtResponse();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONArray subjects = (JSONArray) data.get("DS_CURR_OPSB010");
                for (Object obj : subjects) {
                    JSONObject subject = (JSONObject) obj;
                    if (subject.get("sbjtNo").equals(subjtNoRequest.getSujtNo()) && subject.get("clssNo").equals(subjtNoRequest.getClassNo())) {
                        sujtResponse.setTlsnAplyRcnt(subject.get("tlsnAplyRcnt").toString());
                        try {
                            sujtResponse.setTlsnLmtRcnt(subject.get("tlsnLmtRcnt").toString());
                        } catch (NullPointerException e) {
                            sujtResponse.setTlsnLmtRcnt("-");
                        }
                        sujtResponse.setSbjtKorNm(subject.get("sbjtKorNm").toString());
                        sujtResponse.setSustCd(subject.get("sustCd").toString());
                        sujtResponse.setSujtNo(subjtNoRequest.getSujtNo());
                        sujtResponse.setClassNo(subjtNoRequest.getClassNo());

                        logging.enqueue(new ClassInfo(LocalDateTime.now(ZoneId.of("Asia/Seoul")), subject.get("sbjtKorNm").toString(), subjtNoRequest.getSujtNo(), subjtNoRequest.getClassNo()));
                        break;
                    }
                }

                if (sujtResponse.getSbjtKorNm() == null) {
                    throw new Exception("과목 코드 또는 분반이 유효하지 않습니다.");
                }
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더 또는 바디에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return sujtResponse;
    }

    /*
        여석 확인.
     */
    public SujtResponse getRemainNo(TrinityUser trinityUser, SujtResponse sujtResponse, CookieManager cookieManager, OkHttpClient httpClient, String shtm, String yyyy) throws Exception {

        TrinityInfo info = trinityUser.getTrinityInfo();

        RequestBody formBody = new FormBody.Builder()
                .add("posiFg", "10")
                .add("openYyyy", yyyy)
                .add("openShtm", shtm)
                .add("sustCd", sujtResponse.getSustCd())
                .add("corsCd", "")
                .add("majCd", "%")
                .add("grade", "%")
                .build();
        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/stw/scsr/scoo/findTalaLessonApplicationOpsb.json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .addHeader("x-csrf-token", trinityUser.get_csrf())
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/stw/scsr/scoo/scooLessonApplicationStudentReg.do")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                updateCookies(cookieManager.getCookieStore(), trinityUser);

                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONArray subjects = (JSONArray) data.get("DS_COUR_TALA010");
                for (Object obj : subjects) {
                    JSONObject subject = (JSONObject) obj;
                    if (subject.get("sbjtNo").equals(sujtResponse.getSujtNo()) && subject.get("clssNo").equals(sujtResponse.getClassNo())) {
                        sujtResponse.setExtraCnt(subject.get("extraCnt").toString());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return sujtResponse;
    }

    //@{
    //
    // Logout 섹션.
    //
    public void logout(TrinityUser trinityUser) throws Exception {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        JavaNetCookieJar javaNetCookieJar = getCookieJar(cookieManager, trinityUser);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(javaNetCookieJar)
                .followRedirects(true)
                .build();

        TrinityInfo info = trinityUser.getTrinityInfo();
        RequestBody formBody = new FormBody.Builder()
                .add("_csrf", trinityUser.get_csrf())
                .build();
        Request request = new Request.Builder()
                .url("https://uportal.catholic.ac.kr/portal/login/logout.do")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .addHeader("Content-type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Host", "uportal.catholic.ac.kr")
                .addHeader("Origin", "https://uportal.catholic.ac.kr")
                .addHeader("Referer", "https://uportal.catholic.ac.kr/portal/main.do")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()){
            // empty block
            ;
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더 또는 바디에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        } finally {
            clientClear(httpClient);
            httpClient = null;
        }
    }
    //@}

}
// End of TrinityRepository.java