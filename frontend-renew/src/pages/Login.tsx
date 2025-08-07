import React, { useState, useEffect }from 'react';
import { useMovePage } from '../hooks/navigator';
import { SyncLoader } from "react-spinners";
import '../styles/Login.css';
import { Button, Input, Statistic, StatisticProps } from 'antd';
import CountUp from 'react-countup';

interface VisitorResponse {
  status: string;
  message: string;
  data: number;
}

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

export default function Login () {
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
      setErrMsg('');
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

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
        handleLogin();
    }
  }

  const formatter: StatisticProps['formatter'] = (value) => (
    <CountUp end={value as number} separator="," />
  );

  return (
    <div className='wrapper'onKeyDown={handleKeyDown}>
      <div className='title'>
        <h1>TRINITY PARSER</h1>
        <div className='description'>
          <Statistic title="Total Users" value={visitor} formatter={formatter} />
        </div>
      </div>
      <div className="login-container">
          <Input
            name="id"
            className='login-id'
            placeholder="Trintiy ID"
            value={id}
            status={errMsg !== '' ? 'error' : ''}
            onChange={(e) => {setId(e.target.value)}}
          />
          <Input.Password 
            name="password" 
            className='login-pw'
            placeholder="Trinity Password" 
            value={pw}
            status={errMsg !== '' ? 'error' : ''}
            onChange={(e) => {setPw(e.target.value)}}
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
        <Button className='login' color="default" onClick={handleLogin}>
            Login
        </Button>
        {/* <button className="login" onClick={handleLogin}>Login</button> */}
      </div>
      <div className='footer'>
        Developed by: <a href='https://github.com/1876070677'><b>1876070677</b></a>, <a href='https://github.com/KECO-00'><b>KECO-00</b></a><br />
        Designed by: <a href='https://github.com/Stopone02'><b>Stopone02</b></a>
      </div>
  </div>
  );
}
