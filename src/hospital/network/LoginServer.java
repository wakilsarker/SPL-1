package hospital.network;

import hospital.auth.AuthService;
import hospital.auth.UserAccount;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/** Coordinator process for independently launched terminal clients. */
public final class LoginServer {
    public static final int DEFAULT_PORT = 5050;
    private final AuthService auth = new AuthService();
    private final Set<String> activeUsers = new HashSet<>();

    private LoginServer() { }

    public static void start(int port) {
        new LoginServer().serve(port);
    }

    private void serve(int port) {
        auth.seedDefaultAccounts();
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("CarePlus login server is listening on port " + port + ".");
            System.out.println("Demo accounts: admin/admin123, doctor/doctor123, reception/reception123");
            while (true) {
                Socket client = server.accept();
                Thread handler = new Thread(() -> handle(client), "careplus-login-client");
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("Could not start login server: " + e.getMessage());
        }
    }

    private void handle(Socket socket) {
        String activeUsername = null;
        try (Socket client = socket;
             DataInputStream input = new DataInputStream(client.getInputStream());
             DataOutputStream output = new DataOutputStream(client.getOutputStream())) {
            if (!"LOGIN".equals(input.readUTF())) return;
            String username = input.readUTF().trim();
            String password = input.readUTF();
            UserAccount account = auth.authenticate(username, password);
            if (account == null) {
                reply(output, false, "Invalid username or password.", "");
                return;
            }
            synchronized (activeUsers) {
                if (activeUsers.contains(account.username().toLowerCase())) {
                    reply(output, false, "This account is already logged in.", "");
                    return;
                }
                activeUsers.add(account.username().toLowerCase());
                activeUsername = account.username().toLowerCase();
            }
            reply(output, true, "Login successful.", account.role());
            System.out.println(account.username() + " logged in from " + client.getRemoteSocketAddress());
            while (true) if ("LOGOUT".equals(input.readUTF())) break;
        } catch (IOException ignored) {
            // A closed terminal ends its session; cleanup occurs below.
        } finally {
            if (activeUsername != null) {
                synchronized (activeUsers) { activeUsers.remove(activeUsername); }
                System.out.println(activeUsername + " logged out.");
            }
        }
    }

    private void reply(DataOutputStream output, boolean success, String message, String role) throws IOException {
        output.writeBoolean(success);
        output.writeUTF(message);
        output.writeUTF(role);
        output.flush();
    }
}
