package TalentHunt.TalentHunt.service;

import TalentHunt.TalentHunt.model.Connection;
import TalentHunt.TalentHunt.model.User;
import TalentHunt.TalentHunt.model.VerificationToken;
import TalentHunt.TalentHunt.repository.ConnectionRepository;
import TalentHunt.TalentHunt.repository.UserRepository;
import TalentHunt.TalentHunt.repository.VerificationTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String email = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userRepository.findByEmail(email); // Fetch the user by email from the database
        }
        return null;
    }


    // private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email) != null;
    }

    public boolean authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        return user != null && new BCryptPasswordEncoder().matches(password, user.getPassword());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public TalentHunt.TalentHunt.model.User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    public Optional<User> getUserById1(Long id) {
        return userRepository.findById(id);  // Return Optional<User> directly
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    public String generateProfileUrlSlug(User user) {
        String namePart = user.getFirstName().toLowerCase() + "-" + user.getLastName().toLowerCase();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return namePart + "-" + uniqueId;
    }

    public User getUserByProfileUrlSlug(String slug) {
        return userRepository.findByProfileUrlSlug(slug);
    }

    // Method to generate a unique profile URL slug
    public String generateProfileUrlSlug(String firstName, String lastName) {
        String baseSlug = (firstName + "-" + lastName).toLowerCase().replaceAll("[^a-z0-9]", "-");
        String uniqueSuffix = generateRandomSuffix();
        return baseSlug + "-" + uniqueSuffix;
    }

    // Helper method to generate a random suffix
    private String generateRandomSuffix() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // Example usage in save method
    public void saveUser(User user) {
        if (user.getProfileUrlSlug() == null || user.getProfileUrlSlug().isEmpty()) {
            user.setProfileUrlSlug(generateProfileUrlSlug(user.getFirstName(), user.getLastName()));
        }
        // Save user to the database
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // Check if it's already encoded
            user.setPassword(encoder.encode(user.getPassword()));
        }
        user.setEnabled(false);
        userRepository.save(user);

        // Check for existing verification token
        VerificationToken existingToken = tokenRepository.findByUser(user);
        if (existingToken != null && existingToken.getExpiryDate().isAfter(LocalDateTime.now())) {
            // Token exists and is valid, you can choose to return or update it
            return; // Or update the existing token if needed
        }

        // Generate a new verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Expires in 24 hours
        tokenRepository.save(verificationToken);

        // Send verification email
        String verificationUrl = "http://localhost:8080/register/verify?token=" + token;
        String emailContent = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Email Verification</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4; }"
                + ".container { width: 100%; padding: 20px; background-color: #f1f1f1; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); }"
                + ".header { background-color: #C70039; color: #ffffff; padding: 10px 20px; text-align: center; border-top-left-radius: 8px; border-top-right-radius: 8px; }"
                + ".content { padding: 20px; font-size: 16px; color: #333333; }"
                + ".content p { margin: 0 0 15px; line-height: 1.5; }"
                + ".content a { display: inline-block; padding: 10px 20px; background-color: #FF5733; color: #ffffff; text-decoration: none; border-radius: 4px; }"
                + ".footer { padding: 10px; text-align: center; color: #888888; font-size: 12px; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4;'>"
                + "  <div class='container'>"
                + "    <div class='header'>"
                + "      <h1>Welcome to JOBHIVE</h1>"
                + "    </div>"
                + "    <div class='content'>"
                + "      <p>Dear" + user.getFirstName() + " " + user.getLastName() + ",</p>"
                + "      <p>Thank you for registering with <strong>JOBHIVE</strong>. We're excited to have you on board!</p>"
                + "      <p>Please click the button below to verify your email address and complete your registration:</p>"
                + "      <p style='text-align: center;'><a href='" + verificationUrl + "'>Verify your email</a></p>"
                + "      <p>If the button doesn't work, you can also verify your account using the following link:</p>"
                + "      <p><a href='" + verificationUrl + "'>" + verificationUrl + "</a></p>"
                + "    </div>"
                + "  </div>"
                + "  <div class='footer'>"
                + "    <p>JOBHIVE (Professional Networking and Job Search Platform)</p>"
                + "    <p>Need help? Contact us at <a href='mailto:keertidvcorai@gmail.com'>support@jobhive.com</a></p>"
                + "  </div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getEmail(), "Email Verification", emailContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifyToken(String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null || verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false; // Token is invalid or expired
        }

        // Enable the user
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        return true;
    }

    // Method to get all users except the current user
    public List<User> getAllUsersExceptCurrentUser(Long currentUserId) {
        return userRepository.findAllByUserIdNot(currentUserId);
    }

    public long getConnectionsCount(Long userId) {
        return userRepository.countConnectionsByUserId(userId);
    }

    public List<User> getConnectionsForUser(Long userId) {
        List<Connection> connections = connectionRepository.findConnectionsByUserId(userId);
        List<User> users = new ArrayList<>();

        for (Connection connection : connections) {
            if (connection.getUser1().getUserId().equals(userId)) {
                users.add(connection.getUser2());
            } else {
                users.add(connection.getUser1());
            }
        }

        return users;
    }

}
