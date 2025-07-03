# TrinityParser API

## 24.02.06. 방명록 기능 개발 완료

## 배포 완료!!
https://dobby123.notion.site/CUKProject001-TrinityParser-940036c8dd7f47438b9b82cc0894ca90?pvs=4

웹 페이지: https://parser.dobby.kr

## TrinityParser API Docs
with Swagger:
https://trinity.dobby.kr/swagger-ui/index.html

with Notion:
https://dobby123.notion.site/TrinityParser-API-Document-40991a604ec94f349261b288f0d0faa1?pvs=4

login, logout은 Spring security에 의해 관리되므로 Swagger에 보이지 않습니다. 해당 기능은 Notion을 참고해주세요.

## 사용법
모든 내용은 "배포 완료!!" 부분의 노션 페이지에서 확인가능합니다.
1) 수강 신청 현황 확인: https://dobby123.notion.site/66be1006c023483d97dc2e06c66db5b9
2) 금학기 성적 조회: https://dobby123.notion.site/812cc79673f3477cad1d4eb6fe64022a

# Setting
1. src/main/resources/config/database.properties 파일을 생성해주세요.
2. 아래와 같이 작성해주세요
```properties
redis.address=redis ip 주소
redis.port=redis 포트

db.url=db 주소 
db.user=db 사용자 이름
db.password=db 사용자 비밀번호
```
3. backend는 빌드를 해야합니다.
```shell
mvn install
```
# MySQL
1. root 프로젝트에 총 3가지의 파일을 생성합니다.
   1. db/conf.d/{이름}.cnf
   2. db/initdb.d/create_table.sql
   3. db/.env
2. 각 파일들은 아래와 같이 설정합니다.
```
# {이름}.cnf
[mysqld]
default-character-set = utf8mb4
```
```sql
# create_table.sql
CREATE TABLE VisitLog (
  id int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  context varchar(200) NOT NULL,
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  visible BOOLEAN DEFAULT 1,
  likes int DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```
```
# .env
MYSQL_DATABASE=데이터베이스 이름
MYSQL_USER=데이터베이스 사용자
MYSQL_PASSWORD=데이터베이스 비밀번호
MYSQL_ROOT_PASSWORD=루트 사용자 비밀번호
TZ=Asia/Seoul
```
4. 생성한 데이터베이스 이름에 맞게, applicationContext.xml에서 dataSource 정보를 수정합니다.

# 로깅
/usr/local/tomcat/webapps/logs/subject-query-history.{date}.log 형식으로 로그가 저장됩니다.
이 로그 파일을 외부에서 접근할 수 있도록 하기 위해서 프로젝트 최상단에 logs 폴더를 생성하여 위의 경로와 마운트했습니다.
따라서, 루트 폴더에서 `mkdir logs`로 logs 폴더를 생성합니다.

# Docker-compose
최초 실행 시
``` shell
docker-compose up
```

특정 컨테이너만 빌드할 시
```shell
docker-compose up -d redis // Redis
또는
docker-compose up -d backend // API Server
```

특정 컨테이너를 아예 새롭게 빌드하는 경우 (소스코드 수정 후 war빌드 반영을 위함)
```shell
docker-compose up -d --build backend
```

~특정 컨테이너의 scale 조정. 대신 compose.yml에서 포트가 하나 이상으로 바인딩 되어야 됨~
```shell
docker-compose scale backend=2
```

# 보안 우려
1. Provider에서 아이디, 패스워드가 일치하여 트리니티에 로그인이 완료가 되면 id, password에 ""를 저장함으로써 중요한 개인정보인 아이디 패스워드를 유지하지 않음
2. SecurityTrinityUser 객체가 인증된 사용자 객체인데, 요청에 불필요한 정보들은 저장하지 않음을 원칙으로 함
3. SessionFixation을 통해서 인증되기 이전의 사용자 세션 ID가 탈취되더라도 쓸모가 없도록 함
4. 인증된 사용자 정보는 서버에서만 유지, 클라이언트는 내 정보에 대해서 알 수 없음
5. 허가받지 않은 URL 요청에 대해서는 접근할 수 없음
