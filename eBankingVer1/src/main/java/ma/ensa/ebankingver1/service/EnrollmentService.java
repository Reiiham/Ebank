package ma.ensa.ebankingver1.service;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;
import ma.ensa.ebankingver1.DTO.*;
import ma.ensa.ebankingver1.model.*;
import ma.ensa.ebankingver1.repository.*;
import ma.ensa.ebankingver1.util.EncryptionUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Random;
@Service
public class EnrollmentService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MailtrapClient mailtrapClient;

    // Constantes pour la génération de tokens
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // Constantes existantes
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int USERNAME_LETTERS = 3;
    private static final int USERNAME_DIGITS = 4;
    private static final Random RANDOM = new Random();
    private static final String ACCOUNT_PREFIX = "ACC";
    private static final String SUPERVISOR_CODE = "supervisor";

    // URL de base de votre application frontend
    @Value("${app.frontend.url:http://98.71.187.176:8090}")
    private String frontendUrl;

    public EnrollmentService(UserRepository userRepository,
                             AccountRepository accountRepository,
                             TransactionRepository transactionRepository,
                             MailtrapClient mailtrapClient,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mailtrapClient = mailtrapClient;
        this.passwordEncoder = passwordEncoder;
    }

    public long countClients() {
        return userRepository.countClients();
    }

    public long countAccounts() {
        return accountRepository.count();
    }

    public boolean validateSupervisor(String code) {
        return SUPERVISOR_CODE.equals(code);
    }

    @Transactional
    public void enrollClient(EnrollmentRequest dto) {
        // Création de l'utilisateur
        User client = new User();
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setTel(dto.getTel());
        client.setBirth_Date(LocalDate.parse(dto.getBirthDate()));
        client.setRole(Role.CLIENT);
        client.setCin(dto.getCin());
        client.setDocumentsComplets(dto.getDocumentsComplets() != null && dto.getDocumentsComplets());
        client.setCompteBloque(dto.getCompteBloque() != null && dto.getCompteBloque());


        // Username auto-généré
        String generatedUsername = generateUniqueUsername();
        client.setUsername(generatedUsername);

        // Générer un token sécurisé pour le changement de mot de passe
        String resetToken = generateSecureToken();
        client.setToken(resetToken);
        client.setTokenExpiry(LocalDateTime.now().plusHours(24)); // Token valide 24h

        // Pas de mot de passe initial - l'utilisateur devra le créer
        client.setPassword(null);
        client.setMustChangePassword(true);

        client = userRepository.save(client);

        // Génération du numéro lisible et chiffrement
        String rawAccNum = generateReadableAccountNumber();
        String encryptedAccNum = EncryptionUtil.encrypt(rawAccNum);

        // Création du compte
        BankAccount account = new BankAccount();
        account.setRawAccountNumber(rawAccNum);
        account.setAccountNumber(encryptedAccNum);
        account.setRib(generateRib());
        account.setType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setUser(client);

        System.out.println("Transaction PIN: " + account.getTransactionPin());

        accountRepository.save(account);

        // Créer le lien de changement de mot de passe
        String resetLink = frontendUrl + "/set-password?token=" + resetToken;

        // Envoi de l'email avec le lien d'activation
        String message = String.format("""
        Bonjour %s,
        
        Votre compte bancaire a été créé avec succès.
        
        Informations de votre compte :
        Nom d'utilisateur : %s
        Numéro de compte : %s
        RIB : %s
        
        Pour activer votre compte, veuillez cliquer sur le lien ci-dessous pour définir votre mot de passe :
        %s
        
        Ce lien est valide pendant 24 heures.
        
        Si vous n'arrivez pas à cliquer sur le lien, copiez-collez l'URL complète dans votre navigateur.
        
        Cordialement,
        L'équipe bancaire
        """, client.getFirstName(), generatedUsername, rawAccNum, account.getRib(), resetLink);

        sendEmail(client.getEmail(), "Activation de votre compte bancaire", message);

        System.out.println("Compte client créé → username: " + generatedUsername + ", token envoyé par email");
    }

    /**
     * Ajouter un nouveau compte bancaire pour un client existant
     */
    @Transactional
    public BankAccount addBankAccount(AddAccountRequest dto) {
        // Vérifier que le client existe
        Optional<User> optUser = userRepository.findById(dto.getClientId());
        if (optUser.isEmpty()) {
            throw new RuntimeException("Client introuvable avec l'ID : " + dto.getClientId());
        }

        User client = optUser.get();

        // Vérifier que le client n'est pas bloqué
        if (client.getCompteBloque() != null && client.getCompteBloque()) {
            throw new RuntimeException("Impossible de créer un compte pour un client bloqué");
        }

        // Génération du numéro lisible et chiffrement
        String rawAccNum = generateReadableAccountNumber();
        String encryptedAccNum = EncryptionUtil.encrypt(rawAccNum);

        // Création du nouveau compte
        BankAccount newAccount = new BankAccount();
        newAccount.setRawAccountNumber(rawAccNum);
        newAccount.setAccountNumber(encryptedAccNum);
        newAccount.setRib(generateRib());
        newAccount.setType(dto.getType());
        newAccount.setBalance(dto.getBalance());
        newAccount.setUser(client);

        // Sauvegarde
        BankAccount savedAccount = accountRepository.save(newAccount);

        System.out.println("Nouveau compte créé pour le client " + client.getFirstName() + " " + client.getLastName()
                + " → Numéro: " + rawAccNum + ", PIN: " + newAccount.getTransactionPin());

        return savedAccount;
    }

    // Méthode pour envoyer l'email (similaire à EmployeeService)
    // Méthode pour envoyer l'email
    public void sendEmail(String to, String subject, String content) {
        try {
            final MailtrapMail mail = MailtrapMail.builder()
                    .from(new Address("hello@demomailtrap.co", "Équipe Bancaire"))
                    .to(List.of(new Address(to)))
                    .subject(subject)
                    .text(content)
                    .category("Client Registration")
                    .build();

            System.out.println("Sending email response: " + mailtrapClient.send(mail));
            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    // Méthode pour définir le mot de passe avec le token
    public void setPasswordWithToken(String token, String newPassword) {
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou expiré"));

        // Vérifier si le token n'est pas expiré
        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        // Définir le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setToken(null);
        user.setTokenExpiry(null);
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    // Méthode pour valider un token
    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            Optional<User> userOpt = userRepository.findByToken(token);

            if (!userOpt.isPresent()) {
                return false;
            }

            User user = userOpt.get();

            if (user.getTokenExpiry() == null) {
                return false;
            }

            return user.getTokenExpiry().isAfter(LocalDateTime.now());

        } catch (Exception e) {
            return false;
        }
    }

    // Méthode pour renvoyer un lien d'activation si nécessaire
    public void resendActivationLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        // Générer un nouveau token
        String resetToken = generateSecureToken();
        user.setToken(resetToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Renvoyer l'email
        String resetLink = frontendUrl + "/set-password?token=" + resetToken;
        String message = String.format("""
        Bonjour %s,
        
        Voici votre nouveau lien d'activation de compte bancaire :
        %s
        
        Ce lien est valide pendant 24 heures.
        
        Cordialement,
        L'équipe bancaire
        """, user.getFirstName(), resetLink);

        sendEmail(user.getEmail(), "Nouveau lien d'activation", message);
    }

    // Générer un token sécurisé pour le reset de mot de passe
    private String generateSecureToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(SECURE_RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }

    private String generateUniqueUsername() {
        String username;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < USERNAME_LETTERS; i++) {
                sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            for (int i = 0; i < USERNAME_DIGITS; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            username = sb.toString();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }

    // Générateur de numéro de compte au format ACCXXXXXXX
    private String generateReadableAccountNumber() {
        int number = new Random().nextInt(9000000) + 1000000;
        return "ACC" + number;
    }

    private String generateRib() {
        String bankCode = "123456";
        String branchCode = "67890";
        String accountNumberPart = String.format("%011d", (long)(Math.random() * 1_000_000_000L));
        String key = String.format("%02d", (int)(Math.random() * 100));
        return bankCode + branchCode + accountNumberPart + key;
    }

    @Transactional
    public boolean updateClient(ClientUpdateRequest dto) {
        Optional<User> opt = userRepository.findById(dto.getClientId());
        if (opt.isEmpty()) return false;

        User client = opt.get();

        if (dto.getNewFirstName() != null) client.setFirstName(dto.getNewFirstName());
        if (dto.getNewLastName() != null) client.setLastName(dto.getNewLastName());
        if (dto.getNewEmail() != null) client.setEmail(dto.getNewEmail());
        if (dto.getNewTel() != null) client.setTel(dto.getNewTel());
        if (dto.getDocumentsComplets() != null) client.setDocumentsComplets(dto.getDocumentsComplets());
        if (dto.getCompteBloque() != null) client.setCompteBloque(dto.getCompteBloque());


        userRepository.save(client);
        return true;
    }

    @Transactional
    public boolean deleteClient(Long clientId) {
        Optional<User> opt = userRepository.findById(clientId);
        if (opt.isEmpty()) return false;

        if (!transactionRepository.findByUserId(clientId).isEmpty()) return false;

        User client = opt.get();
        accountRepository.deleteByUserId(clientId);
        userRepository.delete(client);
        return true;
    }

}
