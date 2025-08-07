import { faqs } from '../../Const';
import { Collapse } from 'antd';
import '../../styles/FaQ.css';

const FnQ: React.FC = () => {

    const items = faqs.map((faq, index) => ({
        key: String(index),
        label: faq.question,
        children: <p>{faq.answer}</p>,
    }));

    return (
        <div className="faq-wrapper">
            <h3 style={{ marginBottom: '15px' }}>자주 묻는 질문...</h3>
            <Collapse accordion items={items} />
        </div>
    )
}

export default FnQ;