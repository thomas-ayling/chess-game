import {User} from "../types/user";

export type LobbyData = |{
    action: 'currentUsers';
    payload: User[];
}|{
    action: 'sessionId';
    payload: string;
}