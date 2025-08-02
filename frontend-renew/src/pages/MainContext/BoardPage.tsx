import React, { useEffect, useRef, useState } from "react";
import '../../styles/Board.css';

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

const BoardPage: React.FC = () => {
    const [boardList, setBoardList] = useState<BoardType[]>([]);
    const [inputValue, setInputValue] = useState<string>("");
    const [records, setRecords] = useState<number>(0);
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

            if(records < data.data[0].total_records){
                setBoardList((prev) => [...prev, ...data.data]);
                setRecords(data.data.length);
                setHasMore(true);
            } else {
                setHasMore(false);
            }
            setIsLoading(false);

            if (data.data.length > 0) {
                setLastId(data.data[data.data.length - 1].id);
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
        const observer = new IntersectionObserver(
            (entries) => {
                if(entries[0].isIntersecting && !isLoading && hasMore) {
                    if(lastId == "1"){
                        observer.disconnect();
                        setHasMore(false);
                        return;
                    }

                    getBoard(generateRandomString() + btoa(lastId));
                }
            }, 
            { threshold: 1.0 }
        );

        if(bottomDivRef.current) observer.observe(bottomDivRef.current);
        
        return () => observer.disconnect();
    }, [lastId, isLoading, hasMore]); 
    
    return (
        <>
            <div className="guestbook-container">
                <form onSubmit={handleSubmit} className="guestbook-form" >
                    <div className="guestbook-textarea">
                        <textarea 
                            placeholder="내용을 입력하세요..." 
                            className="guestbook-input"
                            value={inputValue}
                            maxLength={200}
                            onChange={(e) => setInputValue(e.target.value)} 
                        />
                    </div>
                    <div className="guestbook-btn">
                        <button type="submit" className="guestbook-submit-btn">Submit</button>
                    </div>
                </form>
                <div className="entries-container">
                    {boardList.filter((entry) => entry.visible === true).map((entry, index) => (
                        <div key={`${entry.id} - ${index}`} className={`entry-card${entry.isAdmin ? ' Admin-entry' : ''}`}>
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
                        </div>
                    ))}
                    <div ref={bottomDivRef} style={{
                        height: "1px",
                    }} />
                    { !hasMore && <div className="endpage-cmt"><p>마지막 방명록입니다.</p></div>}
                </div>
            </div>
        </>
    )
}

export default BoardPage;