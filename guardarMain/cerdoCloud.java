import java.io.*;
import java.util.*;

// Represents a file stored in the cloud
class CloudFile implements Serializable {
    String filename;
    byte[] content;
    String owner;
    Date uploadDate;

    public CloudFile(String filename, byte[] content, String owner) {
        this.filename = filename;
        this.content = content;
        this.owner = owner;
        this.uploadDate = new Date();
    }

    public String getInfo() {
        return String.format("File: %s | Owner: %s | Size: %d bytes | Uploaded: %s",
                filename, owner, content.length, uploadDate);
    }
}

// Represents a user in the cloud system
class CloudUser implements Serializable {
    String username;
    String password;
    List<CloudFile> files;

    public CloudUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.files = new ArrayList<>();
    }

    public boolean checkPassword(String pass) {
        return this.password.equals(pass);
    }

    public void uploadFile(CloudFile file) {
        files.add(file);
    }

    public void deleteFile(String filename) {
        files.removeIf(f -> f.filename.equals(filename));
    }

    public List<CloudFile> listFiles() {
        return files;
    }
}

// Cloud system to manage users and files
class CloudSystem implements Serializable {
    private Map<String, CloudUser> users;

    public CloudSystem() {
        users = new HashMap<>();
    }

    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new CloudUser(username, password));
        return true;
    }

    public boolean authenticate(String username, String password) {
        CloudUser user = users.get(username);
        return user != null && user.checkPassword(password);
    }

    public CloudUser getUser(String username) {
        return users.get(username);
    }

    public void saveToDisk(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    public static CloudSystem loadFromDisk(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (CloudSystem) in.readObject();
        }
    }
}

// CLI Interface for the Cloud System
public class SimpleJavaCloudSystem {
    private static CloudSystem cloud;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            cloud = new File("cloud.dat").exists() ? CloudSystem.loadFromDisk("cloud.dat") : new CloudSystem();
        } catch (Exception e) {
            cloud = new CloudSystem();
        }

        boolean running = true;
        while (running) {
            System.out.println("Welcome to Simple Cloud System");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    running = false;
                    save();
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (cloud.register(username, password)) {
            System.out.println("Registration successful.");
        } else {
            System.out.println("Username already exists.");
        }
    }

    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (!cloud.authenticate(username, password)) {
            System.out.println("Login failed.");
            return;
        }

        CloudUser user = cloud.getUser(username);
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Cloud Menu ---");
            System.out.println("1. Upload File");
            System.out.println("2. List Files");
            System.out.println("3. Delete File");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            int op = Integer.parseInt(scanner.nextLine());
            switch (op) {
                case 1:
                    uploadFile(user);
                    break;
                case 2:
                    listFiles(user);
                    break;
                case 3:
                    deleteFile(user);
                    break;
                case 4:
                    loggedIn = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void uploadFile(CloudUser user) {
        System.out.print("Enter filename: ");
        String filename = scanner.nextLine();
        System.out.print("Enter file content: ");
        String content = scanner.nextLine();
        CloudFile file = new CloudFile(filename, content.getBytes(), user.username);
        user.uploadFile(file);
        System.out.println("File uploaded successfully.");
    }

    private static void listFiles(CloudUser user) {
        List<CloudFile> files = user.listFiles();
        if (files.isEmpty()) {
            System.out.println("No files uploaded.");
        } else {
            for (CloudFile file : files) {
                System.out.println(file.getInfo());
            }
        }
    }

    private static void deleteFile(CloudUser user) {
        System.out.print("Enter filename to delete: ");
        String filename = scanner.nextLine();
        user.deleteFile(filename);
        System.out.println("File deleted if it existed.");
    }

    private static void save() {
        try {
            cloud.saveToDisk("cloud.dat");
            System.out.println("Data saved.");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }
}