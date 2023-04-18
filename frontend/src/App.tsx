import React from 'react';
import './App.css';
import ChessUi from './chess/ChessUi';
import PhoneSlider from './chess/PhoneSlider';
import Index from "./pages";
import {Provider} from "react-redux";
import {store} from "./store";

function App() {
    return (
        <Provider store={store}>
            <div className='App'>
                <header className='App-header'>
                    <Index/>
                    {/*<PhoneSlider/>*/}
                </header>
            </div>
        </Provider>
    );
}

export default App;
