import { faqs } from '../../Const';
import { useState, useEffect } from 'react';
import { Collapse, Skeleton } from 'antd';
import '../../styles/FaQ.css';

const FnQ: React.FC = () => {
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const items = faqs.map((faq, index) => ({
        key: String(index),
        label: faq.question,
        children: <p>{faq.answer}</p>,
    }));

    useEffect(() => {
        setTimeout(() => {
            setIsLoading(false);
        }, 400);
    }, []);

    return (
        <div className="faq-wrapper">
            {isLoading ? <Skeleton active title={false} paragraph={{ rows: 4 }} /> :
                <>
                    <h3 style={{ marginBottom: '15px' }}>자주 묻는 질문...</h3>
                    <Collapse accordion items={items} />
                </>
            }
        </div>
    )
}

export default FnQ;