import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login';
import './styles/App.css';
import Main from './pages/Main';
import { ConfigProvider } from 'antd';
import { message } from 'antd';
import FullScreenSpinner from './components/Spinner';
import { LoadingProvider, useLoading } from './contexts/LoadingContext';

function GlobalSpinnerWrapper() {
  const { loading } = useLoading();
  return <>{loading && <FullScreenSpinner />}</>;
}

function App() {
  const [messageApi, contextHolder] = message.useMessage();

  return (
    <LoadingProvider>
      <ConfigProvider
        theme={{
          components: {
            Button: {
              defaultHoverColor: "#ffffff",
              defaultHoverBg: "#0c2f879e",
              defaultBg: "#0C2E87",
              defaultActiveBorderColor: "#0C2E87",
              defaultHoverBorderColor: "#0C2E87",
              defaultColor: "#ffffff",
              defaultActiveColor: "#0C2E87",
            },
            Input: {
              hoverBorderColor: "#0C2E87",
              activeShadow: "	0 0 0 2px rgba(62, 105, 222, 0.1)",
              borderRadius: 0,
              activeBorderColor: "#0C2E87",
            }
          }
        }}
      >
        {contextHolder}
        <GlobalSpinnerWrapper />
        <BrowserRouter basename='/fe'>
          <div className='imgWrapper'><img src='/fe/logo.png'></img></div>
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path='/main' element={<Main messageApi={messageApi} />} />
            <Route path='/spinner' element={<FullScreenSpinner />} />
            
            {/* 없는 URL 접근 시 /login으로 리디렉션 */}
            <Route path="*" element={<Navigate to ="/" replace />} />
          </Routes>
        </BrowserRouter>
      </ConfigProvider>
    </LoadingProvider>
  )
}

export default App
