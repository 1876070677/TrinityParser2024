package cuk.api.Trinity;

import cuk.api.Trinity.Entities.CurrentGradeInfo;
import cuk.api.Trinity.Entities.TrinityInfo;
import cuk.api.Trinity.Entities.TrinityUser;
import cuk.api.Trinity.Request.SubjtNoRequest;
import cuk.api.Trinity.Response.GradesResponse;
import cuk.api.Trinity.Response.SujtResponse;
import okhttp3.*;
import okhttp3.MediaType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TrinityRepository {
    private final OkHttpClient client;
    private final JSONParser parser;
    private final static String BASE_PATH = "https://uportal.catholic.ac.kr";
    @Autowired
    public TrinityRepository(OkHttpClient okHttpClient, JSONParser parser) {
        this.client = okHttpClient;
        this.parser = parser;
    }

    public TrinityUser loginForm(TrinityUser trinityUser) throws Exception {
        Request request = new Request.Builder()
                .url(BASE_PATH + "/sso/jsp/sso/ip/login_form.jsp")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:72.0) Gecko/20100101 Firefox/72.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public TrinityUser auth(TrinityUser trinityUser) throws Exception {
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

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public TrinityUser login(TrinityUser trinityUser) throws Exception {
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

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public TrinityUser getUserInfo(TrinityUser trinityUser) throws Exception {
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
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public TrinityUser getSchoolInfo(TrinityUser trinityUser) throws Exception {
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
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

    public GradesResponse getGrades(TrinityUser trinityUser) throws Exception {
        TrinityInfo info = trinityUser.getTrinityInfo();
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
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {

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
            throw new Exception("Request Failed");
        }

        return gradesResponse;
    }

    public SujtResponse getSujtNo(TrinityUser trinityUser, SubjtNoRequest subjtNoRequest) throws Exception {
        TrinityInfo info = trinityUser.getTrinityInfo();
        RequestBody formBody = new FormBody.Builder()
                .add("quatFg", "INQ")
                .add("posiFg", info.getShtmFg())
                .add("openYyyy", info.getShtmYyyy())
                .add("openShtm", info.getShtmFg())
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
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject data = (JSONObject) parser.parse(response.body().string());

                JSONArray subjects = (JSONArray) data.get("DS_CURR_OPSB010");
                for (Object obj : subjects) {
                    JSONObject subject = (JSONObject) obj;
                    if (subject.get("sbjtNo").equals(subjtNoRequest.getSujtNo()) && subject.get("clssNo").equals(subjtNoRequest.getClassNo())) {
                        sujtResponse.setTlsnAplyRcnt(subject.get("tlsnAplyRcnt").toString());
                        sujtResponse.setTlsnLmtRcnt(subject.get("tlsnLmtRcnt").toString());
                        sujtResponse.setSbjtKorNm(subject.get("sbjtKorNm").toString());
                        break;
                    }
                }
            }
        } catch (NullPointerException e) {
            throw new Exception("Request 헤더 또는 바디에 필요한 정보가 담겨있지 않습니다.");
        } catch (Exception e) {
            throw new Exception("Request Failed");
        }

        return sujtResponse;
    }
}
