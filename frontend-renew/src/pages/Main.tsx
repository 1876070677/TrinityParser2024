import { useEffect, useState } from 'react';
import "../styles/Main.css";
import { useMovePage } from '../hooks/navigator';
import SbjtInq from './MainContext/SbjtInq';
import Board from './MainContext/Board';
import Grade from './MainContext/Grade';
import FnQ from './MainContext/FaQ';
import { MessageInstance } from 'antd/es/message/interface';
import { ConfigProvider, Flex, FloatButton, Tabs } from 'antd';
import { LoginOutlined, UserOutlined } from '@ant-design/icons';
import useFetchUtil from '../hooks/fetch';

interface authorizeResonse {
  status: string;
  message: string;
  data: null;
};

interface LogoutResponse {
  status: string,
  message: string,
  data: null;
}

interface Prop {
    messageApi: MessageInstance;
}

export default function Main ({ messageApi }: Prop) {
    const movePage = useMovePage();
    const [open, setOpen] = useState<boolean>(false);
    const fetchUtil = useFetchUtil();

    const tabsForAntd = [
        {key: 'sbjtInq', label: '수강 인원', children: <SbjtInq messageApi={messageApi} />},
        {key: 'grade', label: '성적', children: <Grade messageApi={messageApi} />},
        {key: 'board', label: '게시판', children: <Board messageApi={messageApi} />},
        {key: 'faq', label: 'F&Q', children: <FnQ />},
    ]

    const handleLogout = async () => {
        const res = await fetchUtil('request_logout', `/trinity/logout`, 'POST', null);

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

    const checkLoggedIn = async () => {
        try {
            const res = await fetchUtil('request_check_login', `/trinity/auth/authorize`, 'GET', null);

            const data: authorizeResonse = await res.json();
            if(data.status === "UNAUTHORIZED") {
                movePage('/');
                return;
            }
        } catch (err) {
            console.error("Error get login info", err);
        }
    }

    useEffect(() => {
        checkLoggedIn();
    })
    return (
    <>
    <Flex justify='center'>
        <FloatButton.Group
            open={open}
            trigger="click"
            style={{ insetInlineEnd: 24 }}
            icon={<UserOutlined />}
            onOpenChange={() => setOpen(!open)}
        >
            <FloatButton icon={<LoginOutlined />} 
                tooltip='Logout'
                onClick={handleLogout}/>
        </FloatButton.Group>
        <ConfigProvider
            theme={{
                components: {
                    Tabs: {
                        itemActiveColor: "#0C2E87",
                        itemHoverColor: "#0C2E87",
                        itemSelectedColor: "#0C2E87",
                        inkBarColor: "#0C2E87"
                    }
                }
            }} >
            <Tabs style={{ maxWidth: '360px', minWidth: '360px' }}
                animated={{ tabPane: true }}
                destroyOnHidden
                defaultActiveKey='sbjtInq' items={tabsForAntd} />
        </ConfigProvider>
    </Flex>
    </>
    );
}
