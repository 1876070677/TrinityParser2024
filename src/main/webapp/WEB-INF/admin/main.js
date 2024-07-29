const BASE_URL = "https://trinity.dobby.kr"
function clickLogin()
{
    var form = document.getElementById("loginForm");

    let data = {
        "id" : form.elements["id"].value,
        "password" : form.elements["password"].value
    }
    // console.log(data);
    $.ajax
    ({
        type: "Post",
        url: `${BASE_URL}/manage/auth/login`,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data: JSON.stringify(data),
        xhrFields: { 
            withCredentials: true 
        },
        success: function(data) 
        {
            document.getElementById('message').innerText = "로그인 성공";
        },
        error: function()
        {
            document.getElementById('message').innerText = "로그인 실패";
        }
    });
}

function clickLogout()
{
    // console.log(data);
    $.ajax
    ({
        type: "Post",
        url: `${BASE_URL}/manage/auth/logout`,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        xhrFields: { 
            withCredentials: true 
        },
        success: function(data) 
        {
            document.getElementById('message').innerText = "로그아웃 성공";
        }
    });
}

function clickConfig()
{
    var form = document.getElementById("configForm");

    let data = {
        "shtm" : form.elements["shtm"].value,
        "yyyy" : form.elements["yyyy"].value
    }
    // console.log(data);
    $.ajax
    ({
        type: "Post",
        url: `${BASE_URL}/manage/auth/configure`,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data: JSON.stringify(data),
        xhrFields: { 
            withCredentials: true 
        },
        success: function(data) 
        {
            document.getElementById('message').innerText = "설정 변경 완료";
        },
        error: function()
        {
            document.getElementById('message').innerText = "설정 변경 실패";
        }
    });
}

function clickReqcnt()
{
    $.ajax
    ({
        type: "Get",
        url: `${BASE_URL}/manage/requestCnt`,
        contentType: "application/json; charset=utf-8",
        xhrFields: { 
            withCredentials: true 
        },
        success: function(data) 
        {
            let response = JSON.parse(data);
            document.getElementById('reqcntValue').innerText = response.data;
        },
        error: function()
        {
            document.getElementById('message').innerText = "누적 사용자 조회 실패";
        }
    });
}
