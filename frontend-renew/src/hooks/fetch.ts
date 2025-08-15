import { useLoading } from "../contexts/LoadingContext";

function useFetchUtil() {
    const { setLoading } = useLoading();

    const fetchUtil = async (req: string, url: string, method: string, body: string | null) => {
        try {
            // 전역 스피너가 필요한 요청의 경우 등록.
            switch (req) {
            case 'request_login':
            case 'request_post_vl':
                setLoading(true);
                break;
            default:
                break;
            }

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                ...(body && { body: body })
            });

            return response;
        } finally {
            setLoading(false);
        }
    }

    return fetchUtil;
}

export default useFetchUtil;