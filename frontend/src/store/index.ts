import {configureStore} from "@reduxjs/toolkit";
import lobbyReducer from "./lobby-reducer";
import userReducer from "./user-reducer";

export const store = configureStore({
    reducer: {
        lobbyState: lobbyReducer,
        userState: userReducer,
    },
});

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;