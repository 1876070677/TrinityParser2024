import React, { useState, useEffect }from 'react';
import { useMovePage } from '../hooks/navigator';
import { SyncLoader } from "react-spinners";
import '../styles/Login.css';
import { VisitorResponse } from '../types/LoginTypes';

interface Response {
  status: string,
  message: string,
  data: {
    name: string,
    roles: string[],
  } | null,
}

export interface ILoginPageProps {
  setLoggedIn: React.Dispatch<React.SetStateAction<boolean>>
}

export default function LoginPage () {
  const movePage = useMovePage();

  const [id, setId] = useState<string>('');
  const [pw, setPw] = useState<string>('');
  const [errMsg, setErrMsg] = useState<string>('');

  const [isLoading, setIsLoading] = useState<boolean>(false);


  // 방문자 수 불러오기.
  const [visitor, setVisitor] = useState<number>(0);
  const checkVisitor = async () => {
    try {
      const res = await fetch(`/manage/requestCnt`, {
        method: "GET",
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      const counter: VisitorResponse = await res.json();
      if(counter.status === "OK"){
        setVisitor(counter.data);
      }
    } catch (err) {
      console.error("방문자 수 불러오기에 실패했습니다.");
    }
  }
  useEffect (() => {
      checkVisitor();
  });

  const handleLogin = async () => {
    try {
      const res = await fetch(`/trinity/login`, {
        method: "POST",
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          trinityId: id,
          password: pw
        }),
        credentials: 'include',
      });

      const data: Response = await res.json();

      if(data.status === "OK" && data.data){
        setIsLoading(false);
        movePage('/sbjtInq');
      } else {
        setIsLoading(false);
        setErrMsg('유효하지 않은 로그인 정보입니다.');
      }
    } catch (err) {
      setIsLoading(false);
      setErrMsg("로그인 중 오류가 발생했습니다.");
    };
  }

  return (
    <div className='wrapper'>
      <div className='title'>
        <h1>TRINITY PARSER</h1>
        <div className='description'>
          <b>{visitor}</b> users have used our service.. <br />
        </div>
      </div>
      <div className="login-container">
          <input 
            type="text" 
            name="id" 
            placeholder="Trintiy ID"
            value={id}
            onChange={(e) => setId(e.target.value)}
          />
          <input 
            type="password" 
            name="password" 
            placeholder="Trinity Password" 
            value={pw}
            onChange={(e) => setPw(e.target.value)}
          />
      </div>

      <div className="error-container">
        {
          isLoading 
          ? <SyncLoader color='white' />
          : errMsg
        }
      </div>
      
      <div className='button-container'>
        <button className="login" onClick={handleLogin}>Login</button>
      </div>
      <div className='footer'>
        Developed by: <a href='https://github.com/1876070677'><b>1876070677</b></a>, <a href='https://github.com/KECO-00'><b>KECO-00</b></a><br />
        Designed by: <a href='https://github.com/Stopone02'><b>Stopone02</b></a>
      </div>
  </div>
  );
}
