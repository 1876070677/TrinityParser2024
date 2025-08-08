import { useEffect } from 'react';
import "../styles/Main.css";
import { useMovePage } from '../hooks/navigator';
import { Mode } from '../types/Main';
import Navbar from '../components/Navbar';
import SbjtInq from './MainContext/SbjtInq';
import Board from './MainContext/Board';
import Grade from './MainContext/Grade';
import FnQ from './MainContext/FaQ';
import { MessageInstance } from 'antd/es/message/interface';

interface authorizeResonse {
  status: string;
  message: string;
  data: null;
};

interface Prop {
    mode: Mode;
    messageApi: MessageInstance;
}

export default function Main ({ mode, messageApi }: Prop) {
    const movePage = useMovePage();
    let Context;

    if (mode === 'sbjtInq') {
         Context = SbjtInq;
    } else if (mode === 'board') {
        Context = Board;
    } else if (mode === 'grade') {
        Context = Grade;
    } else if (mode === 'faq') {
        Context = FnQ;
    }

    const checkLoggedIn = async () => {
        try {
            const res = await fetch(`/trinity/auth/authorize`, {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

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
        <Navbar mode={mode} messageApi={messageApi} />
        {Context ? <Context /> : null}
    </>
    );
}
