import { useNavigate } from 'react-router-dom';

export const useMovePage = () => {
    const navigate = useNavigate();

    return (page: string) => {
        navigate(page);
    };
};