package hospital.network;

import hospital.util.Input;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/** Client-side login protocol used before a terminal can open the hospital menu. */
public final class LoginClient {
    private LoginClient() { }

    public static Session login(String host, int port) {
        String username = Input.text("Username: ");
        String password = Input.text("Password: ");
        try {
            Socket socket = new Socket(host, port);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());
            output.writeUTF("LOGIN");
            output.writeUTF(username);
            output.writeUTF(password);
            output.flush();
            boolean success = input.readBoolean();
            String message = input.readUTF();
            String role = input.readUTF();
            System.out.println(message);
            if (!success) {
                socket.close();
                return null;
            }
            return new Session(socket, output, username, role);
        } catch (IOException e) {
            System.out.println("Could not reach login server at " + host + ":" + port + ". Start it first with: java -cp bin hospital.Main server");
            return null;
        }
    }

    public static final class Session implements AutoCloseable {
        private final Socket socket;
        private final DataOutputStream output;
        private final String username;
        private final String role;

        private Session(Socket socket, DataOutputStream output, String username, String role) {
            this.socket = socket;
            this.output = output;
            this.username = username;
            this.role = role;
        }

        public String username() { return username; }
        public String role() { return role; }

        @Override public void close() {
            try {
                output.writeUTF("LOGOUT");
                output.flush();
            } catch (IOException ignored) { }
            try { socket.close(); }
            catch (IOException ignored) { }
        }
    }
}
