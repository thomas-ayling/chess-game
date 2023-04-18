import React, {useEffect} from 'react';
import Login from "./login";
import Lobby from "./lobby";
import {useAppSelector} from "../store/hooks";

const Index = () => {
    const userIsLoggedIn = useAppSelector((state) => state.userState.isLoggedIn);

    useEffect(() => {

    }, [userIsLoggedIn]);

    return (
        userIsLoggedIn ?  <Lobby/> : <Login />
    );
};

export default Index;