import React, { useState, useEffect } from 'react';
import { SyncLoader } from "react-spinners";
import '../../styles/Sbjt.css';
import { useMovePage } from '../../hooks/navigator';
import { Button, Input, Skeleton } from 'antd';
import { RetweetOutlined, StarFilled, StarOutlined } from '@ant-design/icons';
import { MessageInstance } from 'antd/es/message/interface';

interface SugangResponse {
    status: string;
    message: string;
    data: ResultType | null;
}

type ResultType = {
    tlsnAplyRcnt: string | null;
    tlsnLmtRcnt: string | null;
    sbjtKorNm: string | null;
    classNo: string | null;
    sbjtNo: string | null;
    extraCnt: string ;
}

interface Prop {
    messageApi: MessageInstance;
}

const SbjtInq: React.FC<Prop> = ({ messageApi }) => {
    const [sbjtNo, setSbjtNo] = useState<string>('');
    const [classNo, setClassNo] = useState<string>('');
    const [errMsg, setErrMsg] = useState<string>('');
    const [initialLoading, setInitialLoading] = useState<boolean>(true);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [currentLoading, setCurrentLoading] = useState<boolean>(false);
    const [bookmarkLoading, setBookmarkLoading] = useState<{sbjtNo: string, classNo: string}>({sbjtNo: '', classNo: ''});

    const [results, setResults] = useState<ResultType[]>([]);
    const [currentResult, setCurrentResult] = useState<ResultType | null>(null);

    const movePage = useMovePage();

    const addBookmark = () => {
        if (currentResult) {
            setResults((prev) => {
                const isDuplicate = prev.some(
                    (item) =>
                    item.sbjtNo === currentResult.sbjtNo &&
                    item.classNo === currentResult.classNo
                );

                if (isDuplicate) {
                    return prev; // 중복이면 추가 안 함
                }
                return [currentResult, ...prev]; // 중복 아니면 추가
            });

            setCurrentResult(null);
        }
    }

    const removeBookmark = (e: React.MouseEvent<HTMLButtonElement>) => {
        const id = e?.currentTarget?.id || "";
        const [sbjtNo, classNo] = id.split(" ");

        setResults((prev) =>
        prev.filter(
            (item) => !(item.sbjtNo === sbjtNo && item.classNo === classNo)
            )
        );
    }

    const handleRefresh = async (e: React.MouseEvent<HTMLButtonElement>) => {
        setErrMsg('');
        try {
            const id = e?.currentTarget?.id || "";
            const [sbjtNo, classNo] = id.split(" ");
            setBookmarkLoading({sbjtNo: sbjtNo, classNo: classNo});

            setIsLoading(true);
            const res = await fetch(`/trinity/auth/sujtInq?sujtNo=${sbjtNo}&classNo=${classNo}`, {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            const data: SugangResponse = await res.json();
            if(data.status === "OK" && data.data) {
                const result = {
                    sbjtNo: sbjtNo,
                    sbjtKorNm: data.data.sbjtKorNm,
                    tlsnAplyRcnt: data.data.tlsnAplyRcnt,
                    tlsnLmtRcnt: data.data.tlsnLmtRcnt,
                    extraCnt: data.data.extraCnt,
                    classNo: classNo,
                }
                if (result) {
                    setResults((prev) => {

                    const index = prev.findIndex(item => item.sbjtNo === result.sbjtNo && item.classNo === result.classNo);

                    if (index === -1) {
                        return [...prev, result];
                    } else {
                        return prev.map((item, i) => (i === index ? result : item));
                    }
                    });
                }
                setIsLoading(false);
                setBookmarkLoading({sbjtNo: '', classNo: ''});
            } else if(data.status === "UNAUTHORIZED"){
                messageApi.open({
                    type: 'error',
                    content: "로그인이 만료되었습니다.",
                });
                movePage('/');
            } else {
                setErrMsg(data.message);
                setIsLoading(false);
                setBookmarkLoading({sbjtNo: '', classNo: ''});
            }
        } catch (err) {
            console.error("Error get Sugang", err);
        }
    }

    const handleCurrentRefresh = async (e: React.MouseEvent<HTMLButtonElement>) => {
        setErrMsg('');
        try {
            const id = e?.currentTarget?.id || "";
            const [sbjtNo, classNo] = id.split(" ");

            setIsLoading(true);
            setCurrentLoading(true);
            const res = await fetch(`/trinity/auth/sujtInq?sujtNo=${sbjtNo}&classNo=${classNo}`, {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            const data: SugangResponse = await res.json();
            if(data.status === "OK" && data.data) {
                const result = {
                    sbjtNo: sbjtNo,
                    sbjtKorNm: data.data.sbjtKorNm,
                    tlsnAplyRcnt: data.data.tlsnAplyRcnt,
                    tlsnLmtRcnt: data.data.tlsnLmtRcnt,
                    extraCnt: data.data.extraCnt,
                    classNo: classNo,
                }
                if (result) {
                    setCurrentResult(result);
                }
                setIsLoading(false);
                setCurrentLoading(false);
            } else if(data.status === "UNAUTHORIZED"){
                messageApi.open({
                    type: 'error',
                    content: "로그인이 만료되었습니다.",
                });
                movePage('/');
            } else {
                setErrMsg(data.message);
                setIsLoading(false);
                setCurrentLoading(false);
            }
        } catch (err) {
            console.error("Error get Sugang", err);
        }
    }

    const handleGetSugang = async () => {
        setErrMsg('');
        try {
            if (currentResult?.classNo === classNo && currentResult?.sbjtNo === sbjtNo) {
                setErrMsg('이미 조회하고 있는 과목입니다.');
                return;
            }

            let isDuplicated = false;
            results.forEach((result) => {
                if (result.classNo === classNo && result.sbjtNo === sbjtNo) {
                    setErrMsg('이미 조회하고 있는 항목입니다.');
                    isDuplicated = true;
                }
            })

            if (isDuplicated) {
                return;
            }

            setIsLoading(true);
            const res = await fetch(`/trinity/auth/sujtInq?sujtNo=${sbjtNo}&classNo=${classNo}`, {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
            });

            const data: SugangResponse = await res.json();
            if(data.status === "OK" && data.data) {
                const result = {
                    sbjtNo: sbjtNo,
                    sbjtKorNm: data.data.sbjtKorNm,
                    tlsnAplyRcnt: data.data.tlsnAplyRcnt,
                    tlsnLmtRcnt: data.data.tlsnLmtRcnt,
                    extraCnt: data.data.extraCnt,
                    classNo: classNo,
                }
                if (result) {
                    setCurrentResult(result);
                }
                setIsLoading(false);
            } else if(data.status === "UNAUTHORIZED"){
                messageApi.open({
                    type: 'error',
                    content: "로그인이 만료되었습니다.",
                });
                movePage('/');
            } else {
                setErrMsg(data.message);
                setIsLoading(false);
            }
        } catch (err) {
            console.error("Error get Sugang", err);
        }
    }

    useEffect(() => {
        setTimeout(() => {
            setInitialLoading(false);
        }, 400);
    }, [])

    return (
        <>
        <div className='sbjt-container'>
            { initialLoading ? <Skeleton active title={false} paragraph={{ rows: 4 }} /> :
            <div className='sbjt-form'>
                <div className='sbjt-input'>
                    <label>Subject No.</label>
                    <Input className='sbjtNo' placeholder='과목 코드' value={sbjtNo} status={errMsg !== '' ? 'error': ''}
                        onChange={(e) => {setSbjtNo(e.target.value)}} />
                </div>
                <div className='sbjt-input bottom-input'>
                    <label>Class No.</label>
                    <Input className='classNo' placeholder='분반' value={classNo} status={errMsg !== '' ? 'error': ''}
                        onChange={(e) => {setClassNo(e.target.value)}}/>
                </div>
                <div className="sbjt-error-container">
                    {
                    isLoading 
                    ? <SyncLoader size={6} color="#0C2E87" />
                    : errMsg
                    }
                </div>
                <div className='sbjt-input'>
                    <label></label>
                    <Button className='search' onClick={handleGetSugang}>Search</Button>
                </div>
            </div>
            }
        </div>
        <div className='sbjt-results'>
            {/* 현재 조회중인 과목 정보. */}
            { isLoading && currentLoading ? <Skeleton active paragraph={{ rows: 3 }} style={{ margin: '0 10px' }}/> :
                <>
                {currentResult !== null ? 
                <div className='sbjt-result'>
                    <div className='sbjt-result-title'>
                        <h3>{currentResult.sbjtKorNm}</h3>
                    </div>
                    <div className='sbjt-buttons'>
                        <button onClick={addBookmark}><StarOutlined style={{fontSize: '16px'}} /></button>
                        <button id={currentResult.sbjtNo !== null ? `${currentResult.sbjtNo} ${currentResult.classNo}`: ''} onClick={(e) => handleCurrentRefresh(e)} ><RetweetOutlined style={{fontSize: '16px'}} /></button>
                    </div>
                    <table>
                        <thead>
                        <tr>
                            <th>분반</th>
                            <th>제한 인원</th>
                            <th>현재 신청 인원</th>
                            <th>여석</th>
                        </tr>
                        <tr>
                            <th>{currentResult.classNo}</th>
                            <th>{currentResult.tlsnLmtRcnt}</th>
                            <th>{currentResult.tlsnAplyRcnt}</th>
                            <th>{currentResult.extraCnt}</th>
                        </tr>
                        </thead>
                    </table>
                </div>
                : ''}
                </>
            }

            {results.map(data => {
                return isLoading && bookmarkLoading.sbjtNo === data.sbjtNo && bookmarkLoading.classNo === data.classNo ? <Skeleton active paragraph={{ rows: 3 }} style={{ margin: '0 10px' }}/> : (
                <div key={`${data.sbjtNo}+${data.classNo}`} className='sbjt-result'>
                    <div className='sbjt-result-title'>
                        <h3>{data.sbjtKorNm}</h3>
                    </div>
                    <div className='sbjt-buttons'>
                        <button id={data.sbjtNo !== null ? `${data.sbjtNo} ${data.classNo}`: ''} onClick={(e) => removeBookmark(e)}><StarFilled style={{fontSize: '16px'}} /></button>
                        <button id={data.sbjtNo !== null ? `${data.sbjtNo} ${data.classNo}`: ''} onClick={(e) => handleRefresh(e)} ><RetweetOutlined style={{fontSize: '16px'}} /></button>
                    </div>
                    <table>
                        <thead>
                        <tr>
                            <th>분반</th>
                            <th>제한 인원</th>
                            <th>현재 신청 인원</th>
                            <th>여석</th>
                        </tr>
                        <tr>
                            <th>{data.classNo}</th>
                            <th>{data.tlsnLmtRcnt}</th>
                            <th>{data.tlsnAplyRcnt}</th>
                            <th>{data.extraCnt}</th>
                        </tr>
                        </thead>
                    </table>
                </div>
            )})}
        </div>
        </>
    );
}

export default SbjtInq;