import { BrowserRouter, Routes, Route } from 'react-router-dom'
import LoginPage from './pages/LoginPage';
import './styles/App.css';
import MainPage from './pages/MainPage';


function App() {

  return (
    <BrowserRouter basename='/fe'>
      <div className='imgWrapper'><img src='/fe/logo.png'></img></div>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path='/sbjtInq' element={<MainPage mode="sbjtInq" />} />
        <Route path='/grade' element={<MainPage mode="grade" />} />
        <Route path='/board' element={<MainPage mode="board" />} />
        <Route path='/fnq' element={<MainPage mode="faq" />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
