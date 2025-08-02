import * as React from 'react';
import { useState, useEffect } from 'react';
import "../styles/Grade.css";
import { useMovePage } from '../hooks/navigator';
import { SyncLoader } from 'react-spinners';

interface Grade {
    details: string[];
    centesScorAdm: string;
    estiYn: string;
    grdAdm: string;
    sbjtKorNm: string;
    sbjtNo: string;
}

interface GradeResponse {
    status: string,
    message: string,
    data: {
        grades: Grade[],
    } | null
}

const GradePage: React.FC = () => {
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [grades, setGrades] = useState<Grade[]>([]);
    const [selectedGrade, setSelectedGrade] = useState<Grade | null>(null);
    const [errorMsg, setErrorMsg] = useState<string>('');
    const movePage = useMovePage();

    const handleGradeClick = (grade: Grade) => {
        setSelectedGrade(grade);
    };

    useEffect(() => {
        const getGrade = async () => {
            setIsLoading(true);
            try{
                const res = await fetch(`/trinity/auth/grade`, {
                    method: "GET",
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    credentials: 'include',
                });

                const data: GradeResponse = await res.json();
                if(data.status === "UNAUTHORIZED"){
                    alert("로그인이 만료되었습니다.");
                    movePage('/');
                } else if (data.data !== null) {
                    setGrades(data.data.grades);
                    setIsLoading(false);
                } else {
                    setErrorMsg("휴학생 또는 졸업생의 경우, 조회가 불가능합니다.")
                    setIsLoading(false);
                }
            } catch(err){
                console.error("Error get Sugang", err);
            }
        }

        getGrade();
        
    }, []);

    return (
        <div className='grade-wrapper'>
            <h3>이번 학기 성적 조회</h3>

            <table className="grade-table">
                {
                    errorMsg !== ""
                    ? (<div className='error-wrapper'>{errorMsg}</div>)
                    : (
                        isLoading
                        ? <div style={{textAlign: "center"}}><SyncLoader size={6} color="#0C2E87" /></div>
                        : (
                            <>
                                <thead>
                                    <tr style={{ backgroundColor: "#ffffff", color:"#0C2E87"}}>
                                        <th><b>과목명</b></th>
                                        <th><b>성적</b></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {grades.map((grade) => (
                                        <tr key={grade.sbjtNo} onClick={() => handleGradeClick(grade)}>
                                            <th>{grade.sbjtKorNm}</th>
                                            <th>{grade.grdAdm}</th>
                                        </tr>
                                    ))}
                                </tbody>
                            </>
                        )
                    )
                }
            </table>
            <p className="description">표에서 조회하고 싶은 과목 혹은 성적 클릭 시, <br/> 해당 과목의 세부 정보를 확인할 수 있습니다.</p>
            {selectedGrade !== null && selectedGrade && 
                <div className='grade-detail-wrapper'>
                    <h3>Details...</h3>
                    <table>
                        <thead>
                            <tr>
                                <th>총점</th>
                                <th>성적</th>
                                <th>세부 점수</th>
                                <th>평가 여부</th>
                            </tr>
                        </thead>
                        <tbody>
                            <th>{selectedGrade.centesScorAdm}</th>
                            <th>{selectedGrade.grdAdm}</th>
                            <th>
                                {selectedGrade.details.map((detail, index) => (
                                    <>
                                        <span key={index}>
                                            세부항목 {index + 1}: {detail}
                                        </span><br></br>
                                    </>
                                ))}
                            </th>
                            <th>{selectedGrade.estiYn}</th>
                        </tbody>
                    </table>
                </div>
            }
        </div>
    );
};

export default GradePage;