package easypaisa;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class EasyPaisaApp extends Application {

    private Stage primaryStage;
    private ArrayList<String> transactionLogs = new ArrayList<>(); 		// stores all transaction history.
    private ArrayList<UserProfile> registeredUsers = new ArrayList<>();  // Stores all users.
    private UserProfile loggedInUser = null; // Tracks who is currently using the app....Stores currently logged-in user
    private final String DATA_FILE = "easypaisa_data.txt";		// File name where users and logs are saved.

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;		// Stores window in variable
        loadData(); 			// Loads users + transactions from file.
        showLoginScreen();		// Opens login page first.
        primaryStage.setTitle("Easypaisa App ");
        primaryStage.show();	// Shows app window
    }
    // Saves users + history in file.
    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) // Creates file writer
        {
            writer.println(registeredUsers.size());    // Saves number of users.
            for (UserProfile u : registeredUsers) {
                writer.println(u.name + "," + u.phone + "," + u.pin + "," + u.balance);
            }
            writer.println(transactionLogs.size());		// Stores number of logs.
            for (String log : transactionLogs) {
                writer.println(log);
            }
        } catch (IOException e) {
            System.err.println("Save Error: " + e.getMessage());
        }
    }

    private void loadData() {
        File file = new File(DATA_FILE);		//Creates file object.
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file)) // Reads file.
        {
            if (scanner.hasNextLine()) {
                int userCount = Integer.parseInt(scanner.nextLine());		// Reads total users.
                for (int i = 0; i < userCount; i++) {
                    String[] p = scanner.nextLine().split(",");
                    if (p.length == 4) {
                        UserProfile u = new UserProfile(p[0], p[1], p[2]);    // Creates user
                        u.balance = Double.parseDouble(p[3]);		// Loads saved balance.
                        registeredUsers.add(u);
                    }
                }
            }
            if (scanner.hasNextLine()) {
                int logCount = Integer.parseInt(scanner.nextLine());
                for (int i = 0; i < logCount; i++) transactionLogs.add(scanner.nextLine());
            }
        } catch (Exception e) {
            System.err.println("Load Error.");
        }
    }

    //	Creates login page.
    public void showLoginScreen() {
        loggedInUser = null;
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.TOP_CENTER);

        StackPane header = new StackPane();
        header.setMinHeight(200);
        header.setStyle("-fx-background-color: #00B943; -fx-background-radius: 0 0 40 40;");
        Label logo = new Label("easypaisa");
        logo.setTextFill(Color.WHITE); logo.setFont(Font.font("System", FontWeight.BOLD, 45));
        header.getChildren().add(logo);

        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30, 40, 20, 40));

        TextField phoneField = new TextField(); phoneField.setPromptText("Mobile Number");
        applyFieldStyle(phoneField);
        PasswordField pinField = new PasswordField();		// PIN hidden input
        pinField.setPromptText("5-Digit PIN");
        applyFieldStyle(pinField);

        Button loginBtn = new Button("Login");
        applyButtonStyle(loginBtn);

        loginBtn.setOnAction(e -> {
            String phone = phoneField.getText().trim();
            String pin = pinField.getText().trim();

            for (UserProfile u : registeredUsers) {
                if (u.phone.equals(phone) && u.pin.equals(pin)) {
                    loggedInUser = u; // Set the current session user
                    break;
                }
            }

            if (loggedInUser != null) showDashboard();
            else showAlert(Alert.AlertType.ERROR, "Failed", "Incorrect Mobile Number or PIN.");
        });

        Hyperlink createAccLink = new Hyperlink("Create New Account");
        createAccLink.setTextFill(Color.web("#00B943"));
        createAccLink.setOnAction(e -> showRegisterScreen());

        form.getChildren().addAll(phoneField, pinField, loginBtn, createAccLink);
        root.getChildren().addAll(header, form);
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    // REGISTER SCREEN
    public void showRegisterScreen() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox header = new VBox(5);
        header.setMinHeight(180);
        header.setPadding(new Insets(0, 30, 0, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #00B943; -fx-background-radius: 0 0 40 40;");
        Label title = new Label("Create Account");
        title.setTextFill(Color.WHITE); title.setFont(Font.font("System", FontWeight.BOLD, 28));
        header.getChildren().add(title);

        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40, 0, 20, 0));

        TextField nameField = new TextField(); nameField.setPromptText("Full Name");
        applyFieldStyle(nameField);
        TextField phoneField = new TextField(); phoneField.setPromptText("Mobile Number");
        applyFieldStyle(phoneField);
        PasswordField pinField = new PasswordField(); pinField.setPromptText("5-Digit PIN");
        applyFieldStyle(pinField);

        Button regBtn = new Button("Register");
        applyButtonStyle(regBtn);
        regBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String pin = pinField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || pin.length() != 5) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid registration details.");
            } else {
                registeredUsers.add(new UserProfile(name, phone, pin));
                saveData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created!");
                showLoginScreen();
            }
        });

        Button backBtn = createBackButton();
        backBtn.setOnAction(e -> showLoginScreen());

        formContainer.getChildren().addAll(nameField, phoneField, pinField, regBtn, backBtn);
        root.getChildren().addAll(header, formContainer);
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    // DASHBOARD
    public void showDashboard() {
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: #F8F9FA;");
        root.setAlignment(Pos.TOP_CENTER);

        VBox header = new VBox(10);
        header.setMinHeight(200);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #00B943; -fx-background-radius: 0 0 40 40;");

        // Show balance of the logged-in user only
        Label bal = new Label(String.format("Rs. %.2f", loggedInUser.balance));
        bal.setTextFill(Color.WHITE); bal.setFont(Font.font("System", FontWeight.BOLD, 36));
        header.getChildren().addAll(new Label("Welcome, " + loggedInUser.name) {{ setTextFill(Color.WHITE); }}, bal, new Label("Available Balance") {{ setTextFill(Color.WHITE); }});

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20));

        Button s = createMenuButton("Send Money", "💸");
        s.setStyle("-fx-font-size: 15.5;");
        Button b = createMenuButton("Bill Payment", "📄");
        b.setStyle("-fx-font-size: 15.5;");
        Button a = createMenuButton("Add Money", "➕");
        a.setStyle("-fx-font-size: 15.5;");
        Button h = createMenuButton("History", "🕒");
        h.setStyle("-fx-font-size: 15.5;");

        s.setOnAction(e -> showSendMoney());
        b.setOnAction(e -> showBillPayment());
        a.setOnAction(e -> showAddMoney());
        h.setOnAction(e -> showTransactionHistory());

        grid.add(s, 0, 0); grid.add(b, 1, 0); grid.add(a, 0, 1); grid.add(h, 1, 1);

        Button logout = new Button("Logout");
        logout.setOnAction(e -> { saveData(); showLoginScreen(); });
        root.getChildren().addAll(header, grid, logout);
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    // --- SEND MONEY (THE FIX IS HERE) ---
    public void showSendMoney() {
        VBox root = createActionLayout("Send Money");
        TextField phField = new TextField(); phField.setPromptText("Receiver Mobile Number"); applyFieldStyle(phField);
        TextField amtField = new TextField(); amtField.setPromptText("Amount"); applyFieldStyle(amtField);
        Button sendBtn = new Button("Confirm Transfer"); applyButtonStyle(sendBtn);

        sendBtn.setOnAction(e -> {
            String receiverPhone = phField.getText().trim();
            String amtStr = amtField.getText().trim();

            UserProfile receiver = null;
            for (UserProfile u : registeredUsers) {
                if (u.phone.equals(receiverPhone)) {
                    receiver = u;
                    break;
                }
            }

            try {
                double amt = Double.parseDouble(amtStr);
                if (receiver == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Receiver number not found.");
                } else if (receiver == loggedInUser) {
                    showAlert(Alert.AlertType.WARNING, "Error", "Cannot send money to yourself.");
                } else if (amt > loggedInUser.balance) {
                    showAlert(Alert.AlertType.WARNING, "Low Balance", "Insufficient funds.");
                } else {
                    // DECREASE from sender
                    loggedInUser.balance -= amt;
                    // INCREASE to receiver
                    receiver.balance += amt;

                    transactionLogs.add(loggedInUser.name + " sent Rs." + amt + " to " + receiver.name);
                    saveData();
                    showDashboard();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sent Rs." + amt + " to " + receiver.name);
                }
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount."); }
        });
        root.getChildren().addAll(phField, amtField, sendBtn, createBackButton());
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    public void showBillPayment() {
        VBox root = createActionLayout("Bill Payment");
        ComboBox<String> type = new ComboBox<>(); 
        type.getItems().addAll("Electric", "Gas", "Water");
        type.setPrefWidth(300);
     // New field for Reference Number
        TextField refField = new TextField(); 
        refField.setPromptText("Reference Number (e.g. 12345)"); 
        applyFieldStyle(refField);

        // Field for Amount
        TextField amtF = new TextField(); 
        amtF.setPromptText("Amount"); 
        applyFieldStyle(amtF);

        Button pay = new Button("Pay Now"); 
        applyButtonStyle(pay);
        pay.setOnAction(e -> {
            String reference = refField.getText().trim();
            String amountStr = amtF.getText().trim();
            String selectedType = type.getValue();

            try {
                double val = Double.parseDouble(amountStr);
                
                if (selectedType == null) {
                    showAlert(Alert.AlertType.WARNING, "Error", "Please select a bill type.");
                } else if (reference.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Error", "Please enter a reference number.");
                } else if (val > loggedInUser.balance) {
                    showAlert(Alert.AlertType.WARNING, "Low Balance", "Insufficient funds.");
                } else if (val <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Error", "Enter a valid amount.");
                } else {
                    // Logic to deduct balance
                    loggedInUser.balance -= val;
                    
                    // Added Reference Number to the log string
                    transactionLogs.add(loggedInUser.name + " paid " + selectedType + 
                                       " bill (Ref: " + reference + "): Rs." + val);
                    
                    saveData();
                    showDashboard();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Bill Paid Successfully!");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid numeric amount.");
            }
        });

        root.getChildren().addAll(type, refField, amtF, pay, createBackButton());
        primaryStage.setScene(new Scene(root, 400, 650));
    }
       
    public void showAddMoney() {
        VBox root = createActionLayout("Add Money");
        TextField f = new TextField(); f.setPromptText("Amount"); applyFieldStyle(f);
        Button b = new Button("Deposit"); applyButtonStyle(b);
        b.setOnAction(e -> {
            try {
                double val = Double.parseDouble(f.getText());
                if (val > 0) {
                    loggedInUser.balance += val;
                    transactionLogs.add(loggedInUser.name + " deposited: Rs." + val);
                    saveData();
                    showDashboard();
                }
            } catch (Exception ex) { }
        });
        root.getChildren().addAll(f, b, createBackButton());
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    public void showTransactionHistory() {
        VBox root = createActionLayout("History");
        ListView<String> lv = new ListView<>();
        // Show global logs
        for (int i = transactionLogs.size()-1; i>=0; i--) lv.getItems().add(transactionLogs.get(i));
        root.getChildren().addAll(lv, createBackButton());
        primaryStage.setScene(new Scene(root, 400, 650));
    }

    // --- STYLING HELPERS ---
    private VBox createActionLayout(String t) {
        VBox l = new VBox(20); l.setPadding(new Insets(30)); l.setAlignment(Pos.TOP_CENTER);
        l.setStyle("-fx-background-color: #F8F9FA;");
        Label lbl = new Label(t); lbl.setFont(Font.font("System", FontWeight.BOLD, 24));
        l.getChildren().add(lbl); return l;
    }
    private Button createBackButton() {
        Button b = new Button("Back"); b.setStyle("-fx-text-fill: #00B943; -fx-background-color: transparent;");
        b.setOnAction(e -> showDashboard()); return b;
    }
    private void applyFieldStyle(Control f) {
        f.setPrefHeight(50); f.setMaxWidth(300);
        f.setStyle("-fx-background-color: #F2F2F2; -fx-background-radius: 12; -fx-padding: 10;");
    }
    private void applyButtonStyle(Button b) {
        b.setPrefWidth(300); b.setPrefHeight(50); b.setTextFill(Color.WHITE);
        b.setStyle("-fx-background-color: #00B943; -fx-background-radius: 25; -fx-font-weight: bold;");
    }
    private Button createMenuButton(String t, String i) {
        Button b = new Button(i + "\n" + t); b.setPrefSize(140, 140);
        b.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 4);");
        return b;
    }
    private void showAlert(Alert.AlertType t, String title, String content) {
        Alert a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}

// Updated UserProfile class to hold specific balance
class UserProfile {
    String name, phone, pin;
    double balance;

    public UserProfile(String n, String ph, String pi) {
        this.name = n;
        this.phone = ph;
        this.pin = pi;
        this.balance = 50000.00; // Starting balance for new users
    }
}