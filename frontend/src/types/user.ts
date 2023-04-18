
export interface UserDetails {
    name: string | null;
    elo: string | null;
}

export interface User extends UserDetails{
    userId: string | null;
    sessionId: string | null;
    isLoggedIn: boolean;
    currentChannel: string | null;
}
