import React, {useState} from 'react';
import {store} from "../store";
import {logUserIn, setDetails, setUserId} from "../store/user-reducer";

const Login = () => {
    const [name, setName] = useState<string>("")
    const [elo, setElo] = useState<string>("");

    const login = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        const userId = crypto.randomUUID();
        store.dispatch(setUserId(userId));
        store.dispatch(setDetails({name: name, elo: elo}));
        store.dispatch(logUserIn());
    }

    return (
        <div>
            <form onSubmit={(e) => login(e)} className="flex flex-col gap-y-2">
                <input type={"text"} placeholder={"Name:"} className="text-input"
                       onChange={(e) => setName(e.target.value)}/>
                <input type={"text"} placeholder={"ELO:"} className="text-input"
                       onChange={(e) => setElo(e.target.value)}/>
                <input type={"submit"} className="button"/>
            </form>
        </div>
    );
};

export default Login;