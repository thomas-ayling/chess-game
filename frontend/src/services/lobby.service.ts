import {store} from "../store";
import {setCurrentChannel, setSessionId} from "../store/user-reducer";
import {LobbyData} from "./lobby.types";
import {setCurrentUsers} from "../store/lobby-reducer";

class LobbyConnection {
    private webSocketClient: WebSocket | null = null;
    private sessionId: string | null = null;

    init(userId: string) {
        this.webSocketClient = new WebSocket(
            `${process.env.WS_SERVER}?userId=${userId}`
        );

        this.webSocketClient?.addEventListener("open", (e) => {
            store.dispatch(setCurrentChannel("lobby"))
        })

        this.webSocketClient?.addEventListener("message", (event) => {
            if (!Boolean(event.data)) return;
            try {
                const parsedData = JSON.parse(event.data);
                if (parsedData.gameId) {};
                if (!Boolean(parsedData.action)) return;
                const {action, payload} = parsedData as LobbyData;
                switch (action) {
                    case "currentUsers":
                        store.dispatch(setCurrentUsers(payload));
                        break;
                    case "sessionId":
                        store.dispatch(setSessionId(payload));
                        break;
                    default:
                        break;
                }
            } catch (error) {
                console.log("Error in lobby handling", error, event.data);
            }
        })
    }
}


export const LobbyService = new LobbyConnection();