import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {User, UserDetails} from "../types/user";


const initialState: User = {
    userId: null,
    sessionId: null,
    isLoggedIn: false,
    currentChannel: null,
    name: null,
    elo: null,
}

export const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {
        setUserId: (state, action: PayloadAction<string>) => {
            state.userId = action.payload;
        },
        setSessionId: (state, action: PayloadAction<string>) => {
            state.sessionId = action.payload;
        },
        logUserIn: (state) => {
            state.isLoggedIn = true;
        },
        logUserOut: (state) => {
            state.isLoggedIn = false
        },
        setCurrentChannel:  (state, action: PayloadAction<string>) => {
            state.currentChannel = action.payload;
        },
        setDetails: (state, action: PayloadAction<UserDetails>) => {
            state.name = action.payload.name;
            state.elo = action.payload.elo;
        },
    },
})

export const {setUserId, setSessionId, logUserIn, logUserOut, setCurrentChannel, setDetails} = userSlice.actions;

export default userSlice.reducer;