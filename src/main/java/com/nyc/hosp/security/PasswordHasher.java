import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String plain1 = "user1password";
        String plain2 = "user2password";
        String plain3 = "adminpassword";

        System.out.println("User 1: " + encoder.encode(plain1));
        System.out.println("User 2: " + encoder.encode(plain2));
        System.out.println("Admin : " + encoder.encode(plain3));
    }
}
