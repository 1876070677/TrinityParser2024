import { useState } from 'react';
import { faqs } from '../../Const';
import '../../styles/FaQ.css';

const FnQ: React.FC = () => {
    const [openIndex, setOpenIndex] = useState<null | number>(null);

    const toggle = (index: number) => {
        setOpenIndex((prev) => (prev === index ? null : index));
    };

    return (
        <div className="faq-wrapper">
            <h3>자주 묻는 질문...</h3>
            {faqs.map((faq, index) => (
                <div key={index} className="faq-item" onClick={() => toggle(index)}>
                        <b className='QA'>Q. </b>{faq.question}
                    {openIndex === index && (
                        <div className="faq-answer"><b className='QA'>A. </b>{faq.answer}</div>
                    )}
                </div>
            ))}
        </div>
    )
}

export default FnQ;