import React, { useEffect, useRef, useState } from "react";
import '../../styles/Board.css';
import { Button, Card, Flex, Skeleton } from "antd";
import TextArea from "antd/es/input/TextArea";
import { ClockCircleOutlined, GithubOutlined, HeartFilled } from "@ant-design/icons";
import { MessageInstance } from "antd/es/message/interface";

interface Response {
    status: string,
    message: string,
    data: null | string,
  }

export interface BoardType {
    id: string;
    context: string;
    created_time: string;
    visible: boolean;
    likes: number;
    total_records: number;
    isAdmin: boolean;
}

export interface BoardEntry {
    status: string;
    message: string;
    data: BoardType[];
}

interface Prop {
    messageApi: MessageInstance;
}

const Board: React.FC<Prop> = ({ messageApi }) => {
    const [boardList, setBoardList] = useState<BoardType[]>([]);
    const [inputValue, setInputValue] = useState<string>("");
    const [lastId, setLastId] = useState<string>("0");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [initialLoading, setInitialLoading] = useState<boolean>(true);
    const [hasMore, setHasMore] = useState<boolean>(true);

    const bottomDivRef = useRef<HTMLDivElement | null>(null);

    const getBoard = async (cursor: string) => {
        setHasMore(false);
        setIsLoading(true);
        try {
            const res = await fetch(`/trinity/auth/vl?cursor=${cursor}`, {
                method: "GET",
                headers: {
                    'Content-Type': 'application/json',
                    'mode': 'no-cors'
                },
                credentials: 'include',
            });

            const data: BoardEntry = await res.json();
            if(data.status === "Bad Request"){
                messageApi.open({
                    type: 'error',
                    content: data.message,
                });
            }

            if(boardList.length < data.data[0].total_records){
                setBoardList((prev) => [...prev, ...data.data]);
                setHasMore(true);
            } else {
                setHasMore(false);
            }

            if (data.data.length > 0) {
                setLastId(String(data.data[data.data.length - 1].id));
            }
        }catch(err) {
            messageApi.open({
                type: 'error',
                content: '죄송해요 뭔가 이상하네요, 잠시 다른 창을 봐주세요..',
            });
        } finally {
            setIsLoading(false);
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지
        if (inputValue.trim() === "") {
            messageApi.open({
                type: 'error',
                content: '내용을 입력해주세요!',
            });
          return;
        }

        try {
            const res = await fetch(`/trinity/auth/vl`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    context: inputValue,
                }),
                credentials: 'include',
            });

            const result: Response = await res.json();
            if(result.status === "Bad Request"){
                messageApi.open({
                    type: 'error',
                    content: `${result.message}`,
                });
            } else {
                setInputValue(""); // 입력 필드 초기화
                messageApi.open({
                    type: 'success',
                    content: "댓굴이 작성되었습니다.",
                });
            }
            
            setIsLoading(true);
            const latestRes = await fetch(`/trinity/auth/vl?cursor=${generateRandomString()}${btoa("0")}`, {
                method: "GET",
                headers: {
                  "Content-Type": "application/json",
                },
                credentials: "include",
              });
              
              setIsLoading(false);
              const latestData: BoardEntry = await latestRes.json();
              if (latestData.status === "Bad Request") {
                messageApi.open({
                    type: 'error',
                    content: latestData.message,
                });
              } else {
                // 최신 데이터를 기존 목록의 맨 위에 추가
                setInputValue('');
                const lastest: BoardType = latestData.data[0];
                setBoardList((prev) => [lastest, ...prev]);
              }
            

        }catch (err) {
            console.error(err);
        }
    };

      const handleLike = async (id:string) => {
        try {
            const targetEntry = boardList.find((entry) => entry.id === id);
            if(!targetEntry){
                return;
            }

            if(targetEntry.likes  >= 99) {
                messageApi.open({
                    type: 'error',
                    content: "좋아요는 최대 99까지 가능합니다.",
                });
                return;
            }
            const res = await fetch(`/trinity/auth/vl/likes/${id}`, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "mode": "no-cors"
                },
                credentials: "include",
            });

            if(!res) throw new Error("좋아요 요청 실패");

            setBoardList((prevBoardList) => 
                prevBoardList.map((entry) => 
                    entry.id === id ? { ...entry, likes: entry.likes + 1} : entry
                )
            );
        } catch (err) {
            console.error(err);
        }
      };

      const generateRandomString = (length: number = 8): string => {
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        let result = '';
        for (let i = 0; i < length; i++) {
          const randomIndex = Math.floor(Math.random() * characters.length);
          result += characters[randomIndex];
        }
        return result;
      };
      

    const formatCreatedTime = (createdTime: string): string => {
        const createdDate = new Date(createdTime.replace(" ", "T"));
        const curTime = new Date();
        const timeDiff = curTime.getTime() - createdDate.getTime();

        const minutes = Math.floor(timeDiff / (1000 * 60));
        const hours = Math.floor(timeDiff / (1000 * 60 * 60));
        const days = Math.floor(timeDiff / (1000 * 60 * 60 * 24));

        if (minutes < 1){
            return "방금 전";
        } else if (minutes < 60){
            return `${minutes}분 전`;
        } else if (hours < 24) {
            return `${hours}시간 전`;
        } else {
            return `${days}일 전`;
        }
    };

    useEffect(() => {
        getBoard(generateRandomString() + btoa(lastId)); // 초기 데이터 로드
        setTimeout(() => {
            setInitialLoading(false);
        }, 400);
    }, []);
    
    useEffect(() => {
        const target = bottomDivRef.current;
        if (!target) return;
        const observer = new IntersectionObserver(
            (entries) => {
                if(entries[0].isIntersecting && !isLoading && hasMore) {
                    if(lastId === '1'){
                        observer.unobserve(target);
                        setHasMore(false);
                        return;
                    }

                    getBoard(generateRandomString() + btoa(lastId));
                }
            }, 
            { threshold: 1.0 }
        );

        observer.observe(target);
        
        return () => {
            if (target) {
                observer.unobserve(target)
                observer.disconnect();
            };
        }
    }, [lastId, isLoading, hasMore, initialLoading]); 
    
    return (
        <>
        {initialLoading ? <Skeleton active title={false} paragraph={{ rows: 4 }} /> :
            <div className="guestbook-container">
                <form onSubmit={handleSubmit}>
                    <Flex gap={10} justify="center" align="center" style={{ padding:'10px', marginBottom: '25px' }}>
                        <TextArea
                            showCount
                            maxLength={200}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="내용을 입력하세요..." 
                            style={{ height: "100px", resize: 'none' }}
                            allowClear
                        />
                        <Button className='guestbook-submit-btn' color="default" onClick={handleSubmit} >Submit</Button>
                    </Flex>
                </form>
                <div className="entries-container">
                    {boardList.filter((entry) => entry.visible === true).map((entry, index) => (
                        <Card
                            key={`${entry.id} - ${index}`} className={`entry-card${entry.isAdmin ? ' Admin-entry' : ''}`}
                        >
                            { entry.isAdmin &&
                            <p><GithubOutlined /> 운영진</p>
                            }
                            <p className="entry-context">{entry.context}</p>
                            <div className="entry-footer">
                                <span className="entry-date"><ClockCircleOutlined /> {formatCreatedTime(entry.created_time)}</span>
                                <button className="like-btn" onClick={() => handleLike(entry.id)}>
                                    <span className="entry-likes" ><HeartFilled style={{color: '#ff4d4f'}} /> {entry.likes}</span>
                                </button>
                            </div>
                        </Card>
                    ))}
                    <div ref={bottomDivRef} style={{
                        height: "5px",
                    }} />
                    { !hasMore && <div className="endpage-cmt" style={{ fontSize: '12px' }}><p>마지막 방명록입니다.</p></div>}
                </div>
            </div>
        }
        </>
    )
}

export default Board;