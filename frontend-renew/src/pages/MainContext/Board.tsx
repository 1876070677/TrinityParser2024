import React, { useEffect, useRef, useState } from "react";
import '../../styles/Board.css';
import { Button, Card, Flex } from "antd";
import TextArea from "antd/es/input/TextArea";

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

const Board: React.FC = () => {
    const [boardList, setBoardList] = useState<BoardType[]>([]);
    const [inputValue, setInputValue] = useState<string>("");
    const [lastId, setLastId] = useState<string>("0");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [hasMore, setHasMore] = useState<boolean>(false);

    const bottomDivRef = useRef<HTMLDivElement | null>(null);

    const getBoard = async (cursor: string) => {
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
                alert(data.message);
            }

            if(boardList.length < data.data[0].total_records){
                setBoardList((prev) => [...prev, ...data.data]);
                // setRecords(data.data[0].total_records);
                setHasMore(true);
            } else {
                setHasMore(false);
            }
            setIsLoading(false);

            if (data.data.length > 0) {
                setLastId(String(data.data[data.data.length - 1].id));
                console.log(data.data[data.data.length - 1].id);
            }
        }catch(err) {
            alert("죄송해요 뭔가 이상하네요, 잠시 다른 창을 봐주세요")
        } finally {
            setIsLoading(false);
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault(); // 기본 폼 제출 동작 방지
        if (inputValue.trim() === "") {
          alert("내용을 입력해주세요!");
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
                alert(`${result.message}`);
            } else {
                setInputValue(""); // 입력 필드 초기화
                alert("댓굴이 작성되었습니다.");
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
                alert(latestData.message);
              } else {
                // 최신 데이터를 기존 목록의 맨 위에 추가
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
                console.error("해당 댓글을 찾을 수 없습니다.");
                return;
            }

            if(targetEntry.likes  >= 99) {
                alert("좋아요는 최대 99까지 가능합니다.");
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
    }, [lastId, isLoading, hasMore]); 
    
    return (
        <>
            <div className="guestbook-container">
                <form onSubmit={handleSubmit}>
                    <Flex wrap gap={30} justify="center" vertical align="center" style={{ padding:'10px' }}>
                        <TextArea
                            showCount
                            maxLength={200}
                            onChange={(e) => setInputValue(e.target.value)}
                            placeholder="내용을 입력하세요..." 
                            style={{ height: "100px", resize: 'none' }}
                            allowClear
                        />
                        <Flex justify="end" wrap style={{ width: '100%' }} >
                            <Button className='guestbook-submit-btn' color="default" onClick={handleSubmit} >Submit</Button>
                        </Flex>
                    </Flex>
                </form>
                <div className="entries-container">
                    {boardList.filter((entry) => entry.visible === true).map((entry, index) => (
                        <Card
                            key={`${entry.id} - ${index}`} className={`entry-card${entry.isAdmin ? ' Admin-entry' : ''}`}
                        >
                            { entry.isAdmin &&
                               <p>🛡️ 운영진</p>
                            }
                            <p className="entry-context">{entry.context}</p>
                            <div className="entry-footer">
                                <span className="entry-date">{formatCreatedTime(entry.created_time)}</span>
                                <button className="like-btn" onClick={() => handleLike(entry.id)}>
                                    <span className="entry-likes" >❤️ {entry.likes}</span>
                                </button>
                            </div>
                        </Card>
                    ))}
                    <div ref={bottomDivRef} style={{
                        height: "5px",
                    }} />
                    { !hasMore && <div className="endpage-cmt"><p>마지막 방명록입니다.</p></div>}
                </div>
            </div>
        </>
    )
}

export default Board;