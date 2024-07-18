# Trinity API

## Trouble-shooting
|                              문제                               | 해결 방안                                                                                                                                                                                                                                                                                                                 |                                                                관련 내용 포스팅                                                                 |
|:-------------------------------------------------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------:|
|                     Spring 프로젝트 버전 호환성 문제                     | Spring 6.X부터는 Servlet 6.0 (jakarta servlet-api)로 변경되었고, Tomcat 10.1 이상 JDK 17 이상에서 작동 가능. 익숙한 Tomcat 8.X에서 사용하기 위해서 Spring 5.X 그리고 JDK 17을 기준으로 프로그래밍 함                                                                                                                                                               |                                                                    -                                                                     |
|               docker-compose에서 도커 네트워크 간 통신 문제                | 도커 컨테이너의 외부 포트가 아니라 도커 컨테이너의 내부 포트를 사용해야 한다는 사실을 알았음. 시놀로지에서 돌릴 경우 도커 컨테이너 IP 대역의 방화벽을 열어줘야지 통신 가능.                                                                                                                                                                                                                   |                                                                    -                                                                     |
|                      Swagger 설정이 안되는 문제                       | Spring boot에서와 같이 자동으로 설정이 안되어서 Swagger 관련 정적 리소스들의 위치를 직접 설정해주어야 했음.                                                                                                                                                                                                                                                 |                                     [게시글 보기](https://velog.io/@1876060677/Spring-MVC에-Swagger-적용하기)                                      |
|                       Spring session 설정                       | 초기에는 MySQL을 공유 세션으로 썼으나, 세션과 같이 접근 빈도 수가 높은 경우 Redis와 같이 inmemory 저장소가 좋다고 판단하여 변경하였음. 이렇게 공유 공간을 외부로 뺀 이유는 세션 클러스터링 때문인데, Nginx와 docker-compose를 활용해 L4 로드밸런싱을 구축하기 위함. 현재는 MySQL 관련 게시글만 존재. 추후 Redis 설정도 업로드 할 예정.                                                                                                 |                                  [게시글 보기](https://velog.io/@1876060677/Spring-MVC에-Spring-session-추가하기)                                  
| Spring security 설정 관련 문제 (SpringSecrutiyFilterChain 빈 못찾는 문제) | 어노테이션 관련 설정들과 빈 스캔 관련 설정들을 servlet-dispatcher.xml에 적어둠. Security의 경우 가장 최초 ApplicationContext가 초기화 될 때 설정되는데, Security 관련 빈들이 추후 등록되기 때문에 빈을 못찾음. 빈 관련 설정들을 applciationContext.xml로 옮겨서 해결 완료.                                                                                                                        |                                 [게시글 보기](https://velog.io/@1876060677/Spring-API에-Spring-security-적용기-1)                                 |
|              SessionID fixation 문제 (세션 고정 보호 문제)              | 시큐리티에서 세션 고정 보호를 특정 정책으로 설정해두어도 세션 아이디가 변경이 안됨. 이건 시큐리티가 제공해주는 로그인을 사용하지 않고, 임의로 로그인 필터를 등록해서 사용하는 경우 발생하는 문제인데, AuthenticationProcessingFilter를 구현해서 사용할 때 기본적으로 세션 고정 보호가 false이므로 setter를 통해서 특정 정책으로 바꾸어줘야됨.                                                                                                      |                 [게시글 보기](https://velog.io/@1876060677/Spring-API에-Spring-security-적용기-JSON-login-구현-2-Spring-Boot-아닙니다)                  |
|            시큐리티 로그인 커스텀 필터를 거칠 때 세션에 값이 담기지 않는 문제             | 만약 커스텀 필터의 successfulAuthentication() 메소드를 오버라이딩 하는 경우 발생하는데, 이 메소드에서 응답을 생성해버리면 원래 뒤에 연결된 메소드들이 실행이 안됨. 만약 이 메소드를 계속 오버라이딩 해서 사용하려면, SecurityContext에 정보를 담는 로직을 내가 직접 추가해야됨. 그러기 싫으면 AuthenticationSuccessHandler 인터페잇그를 상속받는 클래스를 생성하고 onAuthenticationSuccess() 메소드에서 응답을 생성하면 됨. 그리고 이 핸들러를 커스텀 필터에 등록해주면 설정 완료. |                                                                    -                                                                     |
|           로드 밸런싱 과정에서 OkHttpClient의 쿠키가 공유가 안되는 문제            | 현재 API에서는 다시 외부로 요청을 보내야하는데, 이때 쿠키 정보가 필요한데 로드 밸런싱으로 두 개의 WAS에 요청을 분산하다보니 외부 API를 사용해서 인증 과정을 거친 WAS에만 쿠키가 남아있음. 그래서 Nginx에서 로드 밸런싱을 할 때 IP_HASH; 즉 IP 주소를 기준으로 특정 WAS만 사용하도록 설정했음.                                                                                                                                   |                                                                    -                                                                     |
|         RestTemplate에서 https -> http로의 리다이렉션이 안되는 문제          | RestTemplate로 외부 API를 사용했으나 30X 응답을 받을 때, 리다이렉션이 이루어지지 않는 문제가 있었음. 서칭을 통해서 OkHttpClient를 사용하면 쉽게 된다고 하여 실제로 구현 및 문제를 해결할 수 있었음.                                                                                                                                                                                       |                                                                    -                                                                     |
|                     CORS 에러와 쿠키 발급 관련 문제                      | API를 배포한 상태에서 프론트를 localhost에서 개발하다보니 쿠키를 주고받을 때 어려움이 있었음. CORS로 localhost의 특정 포트에 대해 허용을 해주고, 쿠키의 경우 Same-site, Secure 설정을 수정하여 정상적으로 개발할 수 있도록 조치하였음. Secure 설정이 있는 쿠키더라도 http를 사용하는 localhost에서는 쿠키를 받을 수 있다는 사실을 알았음. 관련 내용 게시글로 업로드했음.                                                                           | [게시글 보기](https://velog.io/@1876060677/Spring-Spring-Security가-적용된-Spring-API에서-CORS과-쿠키-same-site-secure-문제-해결-백엔드에서-테스트-해보는-꿀-Tip도-있어요) |

## TrinityParser API Docs
with Swagger:
https://trinity.dobby.kr/swagger-ui/index.html

with Notion: 
https://seelab.notion.site/Gajang-API-Document-db2e6852c2a84ccc9dbc0ff6ee77e12d?pvs=4

login, logout은 Spring security에 의해 관리되므로 Swagger에 보이지 않습니다. 해당 기능은 Notion을 참고해주세요.

# Setting
1. src/main/resources/config/database.properties 파일을 생성해주세요.
2. 아래와 같이 작성해주세요
```properties
redis.address=redis ip 주소
redis.port=redis 포트
```
# Docker-compose
최초 실행 시
``` shell
docker-compose up
```

특정 컨테이너만 빌드할 시
```shell
docker-compose up -d redis // MySQL
또는
docker-compose up -d backend // API Server
```

특정 컨테이너를 아예 새롭게 빌드하는 경우 (소스코드 수정 후 war빌드 반영을 위함)
```shell
docker-compose up -d --build backend
```

특정 컨테이너의 scale 조정. 대신 compose.yml에서 포트가 하나 이상으로 바인딩 되어야 됨
```shell
docker-compose scale backend=2
```

# 보안 우려
1. Provider에서 아이디, 패스워드가 일치하여 트리니티에 로그인이 완료가 되면 id, password에 ""를 저장함으로써 중요한 개인정보인 아이디 패스워드를 유지하지 않음
2. SecurityTrinityUser 객체가 인증된 사용자 객체인데, 요청에 불필요한 정보들은 저장하지 않음을 원칙으로 함
3. SessionFixation을 통해서 인증되기 이전의 사용자 세션 ID가 탈취되더라도 쓸모가 없도록 함
4. 인증된 사용자 정보는 서버에서만 유지, 클라이언트는 내 정보에 대해서 알 수 없음
5. 허가받지 않은 URL 요청에 대해서는 접근할 수 없음