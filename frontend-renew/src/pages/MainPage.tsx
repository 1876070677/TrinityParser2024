import { useEffect } from 'react';
import "../styles/MainPage.css";
import { useMovePage } from '../hooks/navigator';
import { Mode } from '../types/Main';
import Navbar from '../components/Navbar';
import SbjtInq from './MainContext/SbjtInq';
import BoardPage from './MainContext/BoardPage';
import GradePage from './MainContext/GradePage';
import FnQ from './MainContext/FaQ';

interface authorizeResonse {
  status: string;
  message: string;
  data: null;
};

interface Prop {
    mode: Mode;
}

export default function MainPage ({ mode }: Prop) {
    const movePage = useMovePage();
    let Context;

    if (mode === 'sbjtInq') {
         Context = SbjtInq;
    } else if (mode === 'board') {
        Context = BoardPage;
    } else if (mode === 'grade') {
        Context = GradePage;
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
        <Navbar mode={mode} />
        {Context ? <Context /> : null}
    </>
    );
}
