import java.util.Scanner;

public class EcosystemMain {
    private static Scanner scanner = new Scanner(System.in);
    private static CloudSystem cloudSystem;
    private static Database database;

    public static void main(String[] args) {
        try {
            cloudSystem = new java.io.File("cloud.dat").exists() ? CloudSystem.loadFromDisk("cloud.dat") : new CloudSystem();
        } catch (Exception e) {
            cloudSystem = new CloudSystem();
        }

        database = new Database();
        initDatabase();

        boolean running = true;
        while (running) {
            System.out.println("\n===== ECOSYSTEM MENU =====");
            System.out.println("1. Cloud System");
            System.out.println("2. Database System");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String option = scanner.nextLine();
            switch (option) {
                case "1":
                    runCloudSystem();
                    break;
                case "2":
                    runDatabaseSystem();
                    break;
                case "3":
                    save();
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private static void runCloudSystem() {
        boolean active = true;
        while (active) {
            System.out.println("\n--- Cloud Menu ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Back");
            System.out.print("Option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    System.out.print("Username: ");
                    String user = scanner.nextLine();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine();
                    if (cloudSystem.register(user, pass))
                        System.out.println("Registered.");
                    else
                        System.out.println("User already exists.");
                    break;
                case "2":
                    System.out.print("Username: ");
                    String loginUser = scanner.nextLine();
                    System.out.print("Password: ");
                    String loginPass = scanner.nextLine();
                    if (cloudSystem.authenticate(loginUser, loginPass)) {
                        CloudUser u = cloudSystem.getUser(loginUser);
                        boolean inCloud = true;
                        while (inCloud) {
                            System.out.println("\n[Cloud] " + u.username);
                            System.out.println("1. Upload File");
                            System.out.println("2. List Files");
                            System.out.println("3. Delete File");
                            System.out.println("4. Logout");
                            System.out.print("Choose: ");
                            String cloudOp = scanner.nextLine();
                            switch (cloudOp) {
                                case "1":
                                    System.out.print("Filename: ");
                                    String fname = scanner.nextLine();
                                    System.out.print("Content: ");
                                    String content = scanner.nextLine();
                                    u.uploadFile(new CloudFile(fname, content.getBytes(), u.username));
                                    break;
                                case "2":
                                    for (CloudFile f : u.listFiles()) System.out.println(f.getInfo());
                                    break;
                                case "3":
                                    System.out.print("Filename to delete: ");
                                    u.deleteFile(scanner.nextLine());
                                    break;
                                case "4":
                                    inCloud = false;
                                    break;
                                default:
                                    System.out.println("Invalid.");
                            }
                        }
                    } else {
                        System.out.println("Authentication failed.");
                    }
                    break;
                case "3":
                    active = false;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void runDatabaseSystem() {
        QueryParser parser = new QueryParser(database);

        boolean inDb = true;
        while (inDb) {
            System.out.println("\n--- Database Menu ---");
            System.out.println("1. Run Query");
            System.out.println("2. Back");
            System.out.print("Option: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    System.out.print("SQL> ");
                    String query = scanner.nextLine();
                    try {
                        parser.parseAndExecute(query);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "2":
                    inDb = false;
                    break;
                default:
                    System.out.println("Invalid.");
            }
        }
    }

    private static void initDatabase() {
        if (database.getTable("employees") == null) {
            database.createTable("employees", java.util.Arrays.asList(
                    new Column("id", DataType.INTEGER),
                    new Column("name", DataType.STRING),
                    new Column("salary", DataType.FLOAT)
            ));
        }
    }

    private static void save() {
        try {
            cloudSystem.saveToDisk("cloud.dat");
            System.out.println("Cloud data saved.");
        } catch (Exception e) {
            System.out.println("Could not save cloud data.");
        }
    }
}