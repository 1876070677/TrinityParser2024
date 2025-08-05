import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login';
import './styles/App.css';
import Main from './pages/Main';


function App() {

  return (
    <BrowserRouter basename='/fe'>
      <div className='imgWrapper'><img src='/fe/logo.png'></img></div>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path='/sbjtInq' element={<Main mode="sbjtInq" />} />
        <Route path='/grade' element={<Main mode="grade" />} />
        <Route path='/board' element={<Main mode="board" />} />
        <Route path='/fnq' element={<Main mode="faq" />} />
        
        {/* 없는 URL 접근 시 /login으로 리디렉션 */}
        <Route path="*" element={<Navigate to ="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
