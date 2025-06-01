import java.io.*;
import java.util.*;

// Basic DataType Enum
enum DataType {
    INTEGER,
    STRING,
    FLOAT
}

// Represents a column in a table
class Column implements Serializable {
    String name;
    DataType type;

    Column(String name, DataType type) {
        this.name = name;
        this.type = type;
    }
}

// Represents a row in a table
class Row implements Serializable {
    List<Object> values = new ArrayList<>();

    Row(List<Object> values) {
        this.values = values;
    }
}

// Represents a table
class Table implements Serializable {
    String name;
    List<Column> columns = new ArrayList<>();
    List<Row> rows = new ArrayList<>();

    Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    void insert(List<Object> values) {
        if (values.size() != columns.size()) {
            throw new IllegalArgumentException("Value count doesn't match column count");
        }
        rows.add(new Row(values));
    }

    void selectAll() {
        System.out.println("--- " + name + " ---");
        for (Column c : columns) {
            System.out.print(c.name + "\t");
        }
        System.out.println();
        for (Row r : rows) {
            for (Object v : r.values) {
                System.out.print(v + "\t");
            }
            System.out.println();
        }
    }

    void deleteWhere(String columnName, Object value) {
        int idx = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name.equals(columnName)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return;
        rows.removeIf(row -> row.values.get(idx).equals(value));
    }

    void updateWhere(String columnName, Object oldValue, Object newValue) {
        int idx = -1;
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name.equals(columnName)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return;
        for (Row row : rows) {
            if (row.values.get(idx).equals(oldValue)) {
                row.values.set(idx, newValue);
            }
        }
    }
}

// Main Database Class
class Database implements Serializable {
    Map<String, Table> tables = new HashMap<>();

    void createTable(String name, List<Column> columns) {
        if (tables.containsKey(name)) {
            throw new IllegalArgumentException("Table already exists");
        }
        tables.put(name, new Table(name, columns));
    }

    Table getTable(String name) {
        return tables.get(name);
    }

    void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    static Database loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Database) in.readObject();
        }
    }
}

// Sample Query Parser (very minimal)
class QueryParser {
    Database db;

    QueryParser(Database db) {
        this.db = db;
    }

    void parseAndExecute(String query) {
        String[] tokens = query.split(" ");
        if (tokens.length < 2) return;

        String command = tokens[0].toUpperCase();
        String table = tokens[1];

        switch (command) {
            case "INSERT": {
                String[] values = query.substring(query.indexOf("(") + 1, query.indexOf(")")).split(",");
                List<Object> vals = new ArrayList<>();
                for (String v : values) {
                    v = v.trim();
                    if (v.matches("\d+")) {
                        vals.add(Integer.parseInt(v));
                    } else if (v.matches("\d+\.\d+")) {
                        vals.add(Float.parseFloat(v));
                    } else {
                        vals.add(v.replace("\"", ""));
                    }
                }
                db.getTable(table).insert(vals);
                break;
            }
            case "SELECT": {
                db.getTable(table).selectAll();
                break;
            }
            case "DELETE": {
                if (tokens.length < 5 || !tokens[2].equalsIgnoreCase("WHERE")) return;
                String col = tokens[3];
                String val = tokens[4];
                Object obj = val.matches("\d+") ? Integer.parseInt(val) : val.replace("\"", "");
                db.getTable(table).deleteWhere(col, obj);
                break;
            }
            case "UPDATE": {
                if (tokens.length < 7 || !tokens[2].equalsIgnoreCase("SET")) return;
                String col = tokens[3];
                String newVal = tokens[4];
                String whereCol = tokens[5];
                String oldVal = tokens[6];
                Object newObj = newVal.matches("\d+") ? Integer.parseInt(newVal) : newVal.replace("\"", "");
                Object oldObj = oldVal.matches("\d+") ? Integer.parseInt(oldVal) : oldVal.replace("\"", "");
                db.getTable(table).updateWhere(whereCol, oldObj, newObj);
                break;
            }
            default:
                System.out.println("Unknown command");
        }
    }
}

// Main app to test everything
public class SimpleJavaDatabase {
    public static void main(String[] args) {
        Database db = new Database();

        // Define columns
        List<Column> columns = Arrays.asList(
                new Column("id", DataType.INTEGER),
                new Column("name", DataType.STRING),
                new Column("salary", DataType.FLOAT)
        );

        db.createTable("employees", columns);

        QueryParser parser = new QueryParser(db);

        // Insert sample data
        parser.parseAndExecute("INSERT employees (1, \"Alice\", 3000.5)");
        parser.parseAndExecute("INSERT employees (2, \"Bob\", 2500.0)");
        parser.parseAndExecute("INSERT employees (3, \"Charlie\", 4000.75)");

        // Display all
        parser.parseAndExecute("SELECT employees");

        // Update salary
        parser.parseAndExecute("UPDATE employees SET salary 5000 salary 2500.0");
        parser.parseAndExecute("SELECT employees");

        // Delete Bob
        parser.parseAndExecute("DELETE employees WHERE name \"Bob\"");
        parser.parseAndExecute("SELECT employees");

        // Save and load test
        try {
            db.saveToFile("mydb.ser");
            Database loadedDb = Database.loadFromFile("mydb.ser");
            System.out.println("Loaded from file:");
            loadedDb.getTable("employees").selectAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}