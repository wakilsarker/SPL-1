package hospital;

import hospital.menu.MainMenu;
import hospital.network.LoginClient;
import hospital.network.LoginServer;

/** Entry point for the Console-Based Hospital Management System. */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && "server".equalsIgnoreCase(args[0])) {
            LoginServer.start(port(args, 1));
            return;
        }
        if (args.length > 0 && "standalone".equalsIgnoreCase(args[0])) {
            new MainMenu("Local user", "STANDALONE").start();
            return;
        }

        String host = "127.0.0.1";
        int port = LoginServer.DEFAULT_PORT;
        if (args.length > 0 && "client".equalsIgnoreCase(args[0])) {
            if (args.length > 1) host = args[1];
            if (args.length > 2) port = port(args, 2);
        }

        try (LoginClient.Session session = LoginClient.login(host, port)) {
            if (session != null) new MainMenu(session.username(), session.role()).start();
        }
    }

    private static int port(String[] args, int index) {
        if (args.length <= index) return LoginServer.DEFAULT_PORT;
        try {
            int port = Integer.parseInt(args[index]);
            if (port >= 1 && port <= 65535) return port;
        } catch (NumberFormatException ignored) {
        }
        System.out.println("Invalid port. Using " + LoginServer.DEFAULT_PORT + ".");
        return LoginServer.DEFAULT_PORT;
    }
}
