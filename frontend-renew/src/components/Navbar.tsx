import React from 'react';
import { Link} from 'react-router-dom';
import '../styles/Navbar.css';
import { Mode } from '../types/Main';
import { useMovePage } from '../hooks/navigator';
import { MessageInstance } from 'antd/es/message/interface';

const tabs = [
  {key: 'sbjtInq', value: '수강 인원', path: '/sbjtInq'},
  {key: 'grade', value: '성적', path: '/grade'},
  {key: 'board', value: '게시판', path: '/board'},
  {key: 'faq', value: 'F&Q', path: '/fnq'}
];

interface Prop {
  mode: Mode;
  messageApi: MessageInstance;
}

interface LogoutResponse {
  status: string,
  message: string,
  data: null;
}

const Navbar: React.FC<Prop> = ({ mode, messageApi }) => {
  const movePage = useMovePage();

  const handleLogout = async () => {
    const res = await fetch(`/trinity/logout`, {
      method: "POST",
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    });

    const data: LogoutResponse = await res.json();

    if(data.status === "OK"){
      messageApi.open({
        type: 'success',
        content: 'Logged out successfully.',
      });
      movePage('/');
    } else {
        messageApi.open({
          type: 'error',
          content: 'Error!!',
        });
        movePage('/');
    }
  }

  return (
    <>
    <div className='navbar-container'>
      {tabs.map((tab) => (
        <div
          key={tab.key}
          className={mode === tab.key ? 'tab selected': 'tab'}
        >
          <Link to={tab.path}>{tab.value}</Link>
        </div>
      ))}
      <div style={{cursor: "pointer"}}>
          <a onClick={handleLogout}>Logout</a>
        </div>
    </div>
    <hr />
    </>
  );
}

export default Navbar;