import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import App from './App';
import './style.css';

const theme = createTheme({ palette: { mode: 'light', primary: { main: '#325cf4' }, background: { default: '#f6f8fc' }, text: { primary: '#18233b', secondary: '#75809a' } }, typography: { fontFamily: 'Inter, ui-sans-serif, system-ui, sans-serif', button: { textTransform: 'none', fontWeight: 650 } }, shape: { borderRadius: 14 } });
ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><ThemeProvider theme={theme}><CssBaseline/><BrowserRouter><App/></BrowserRouter></ThemeProvider></React.StrictMode>);
