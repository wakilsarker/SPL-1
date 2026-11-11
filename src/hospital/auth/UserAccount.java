package hospital.auth;


public final class UserAccount {
    private final String username;
    private final String role;

    public UserAccount(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String username() { 
        return username; 
    }
    public String role() {
        return role; 
    }
    
}
