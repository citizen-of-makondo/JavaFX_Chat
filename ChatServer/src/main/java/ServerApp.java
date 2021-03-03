import chat.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

class ServerApp{

    private static final Logger LOGGER = LogManager.getLogger(ServerApp.class);

    private static final int DEFAULT_PORT = 8888;

    public static void main(String[] args) {

        int port = DEFAULT_PORT;

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            new MyServer(port).start();
        } catch (IOException e) {
            LOGGER.error("Ошибка запуска сервера!", e);
            System.exit(1);
        }
    }
}