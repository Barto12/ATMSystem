package com.example.atmsystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;

public class ATMSystem extends Application {

    private Map<String, Account> accounts = new HashMap<>();
    private Account currentAccount;
    private Stage primaryStage;
    private Scene loginScene, atmManagementScene, adminScene;
    private ListView<String> transactionHistory;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Sistema de Gestión de ATM");

        initializeAccounts();
        createLoginScene();
        createATMManagementScene();
        createAdminScene();

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void initializeAccounts() {
        accounts.put("admin", new Account("admin", "1234", 0.0, true));
        accounts.put("user1", new Account("user1", "1111", 1000.0, false));
        accounts.put("user2", new Account("user2", "2222", 1500.0, false));
    }

    private void createLoginScene() {
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        Label userLabel = new Label("Nombre de usuario:");
        loginGrid.add(userLabel, 0, 0);

        TextField userTextField = new TextField();
        loginGrid.add(userTextField, 1, 0);

        Label pwLabel = new Label("Contraseña:");
        loginGrid.add(pwLabel, 0, 1);

        PasswordField pwBox = new PasswordField();
        loginGrid.add(pwBox, 1, 1);

        Button btnLogin = new Button("Iniciar sesión");
        loginGrid.add(btnLogin, 1, 2);

        Label loginMessage = new Label();
        loginGrid.add(loginMessage, 1, 3);

        btnLogin.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();

            if (accounts.containsKey(username) && accounts.get(username).validatePassword(password)) {
                currentAccount = accounts.get(username);
                if (currentAccount.isAdmin()) {
                    updateAdminScene();
                    primaryStage.setScene(adminScene);
                } else {
                    updateATMManagementScene();
                    primaryStage.setScene(atmManagementScene);
                }
            } else {
                loginMessage.setText("Credenciales inválidas");
            }
        });

        loginScene = new Scene(loginGrid, 300, 200);
    }

    private void createATMManagementScene() {
        VBox atmManagementBox = new VBox(10);
        atmManagementBox.setAlignment(Pos.CENTER);
        atmManagementBox.setPadding(new Insets(25, 25, 25, 25));

        Label balanceLabel = new Label();
        TextField amountField = new TextField();
        amountField.setPromptText("Cantidad");

        Button depositButton = new Button("Depositar");
        Button withdrawButton = new Button("Retirar");
        Button logoutButton = new Button("Cerrar sesión");

        transactionHistory = new ListView<>();

        depositButton.setOnAction(e -> {
            double amount = Double.parseDouble(amountField.getText());
            currentAccount.deposit(amount);
            updateATMManagementScene();
            amountField.clear();
        });

        withdrawButton.setOnAction(e -> {
            double amount = Double.parseDouble(amountField.getText());
            if (currentAccount.withdraw(amount)) {
                updateATMManagementScene();
            } else {
                balanceLabel.setText("Fondos insuficientes");
            }
            amountField.clear();
        });

        logoutButton.setOnAction(e -> primaryStage.setScene(loginScene));

        atmManagementBox.getChildren().addAll(balanceLabel, amountField, depositButton, withdrawButton, new Label("Historial de transacciones:"), transactionHistory, logoutButton);

        atmManagementScene = new Scene(atmManagementBox, 400, 400);
    }

    private void createAdminScene() {
        VBox adminBox = new VBox(10);
        adminBox.setAlignment(Pos.CENTER);
        adminBox.setPadding(new Insets(25, 25, 25, 25));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nombre de usuario");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");
        TextField balanceField = new TextField();
        balanceField.setPromptText("Saldo inicial");

        Button addAccountButton = new Button("Agregar cuenta");
        Button removeAccountButton = new Button("Eliminar cuenta");
        Button logoutButton = new Button("Cerrar sesión");

        addAccountButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            double balance = Double.parseDouble(balanceField.getText());
            accounts.put(username, new Account(username, password, balance, false));
            usernameField.clear();
            passwordField.clear();
            balanceField.clear();
        });

        removeAccountButton.setOnAction(e -> {
            String username = usernameField.getText();
            accounts.remove(username);
            usernameField.clear();
        });

        logoutButton.setOnAction(e -> primaryStage.setScene(loginScene));

        adminBox.getChildren().addAll(usernameField, passwordField, balanceField, addAccountButton, removeAccountButton, logoutButton);

        BorderPane adminRoot = new BorderPane();
        adminRoot.setTop(adminBox);
        adminRoot.setCenter(createChartPane());

        adminScene = new Scene(adminRoot, 800, 600);
    }

    private void updateATMManagementScene() {
        Label balanceLabel = (Label) atmManagementScene.lookup(".label");
        balanceLabel.setText("Saldo de la cuenta: $" + currentAccount.getBalance());
        transactionHistory.getItems().clear();
        transactionHistory.getItems().addAll(currentAccount.getTransactionHistory());
    }

    private void updateAdminScene() {
        BorderPane adminRoot = (BorderPane) adminScene.getRoot();
        adminRoot.setCenter(createChartPane());
    }

    private TabPane createChartPane() {
        TabPane tabPane = new TabPane();

        Tab dailyTab = new Tab("Diario", new StackPane(createChart("Diario")));
        Tab weeklyTab = new Tab("Semanal", new StackPane(createChart("Semanal")));
        Tab monthlyTab = new Tab("Mensual", new StackPane(createChart("Mensual")));
        Tab yearlyTab = new Tab("Anual", new StackPane(createChart("Anual")));

        tabPane.getTabs().addAll(dailyTab, weeklyTab, monthlyTab, yearlyTab);

        return tabPane;
    }

    private BarChart<String, Number> createChart(String period) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        xAxis.setLabel("Tiempo");
        yAxis.setLabel("Número de transacciones");

        XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
        dataSeries.setName("Transacciones " + period);

        Map<String, Integer> data = getDataForPeriod(period);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            dataSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(dataSeries);

        return barChart;
    }

    private Map<String, Integer> getDataForPeriod(String period) {
        Map<String, Integer> data = new HashMap<>();
        LocalDate today = LocalDate.now();

        switch (period) {
            case "Diario":
                for (int i = 0; i < 7; i++) {
                    LocalDate date = today.minusDays(i);
                    data.put(date.toString(), getRandomNumber());
                }
                break;
            case "Semanal":
                for (int i = 0; i < 4; i++) {
                    LocalDate date = today.minusWeeks(i);
                    data.put("Semana " + date.toString(), getRandomNumber());
                }
                break;
            case "Mensual":
                for (int i = 0; i < 12; i++) {
                    LocalDate date = today.minusMonths(i);
                    data.put(date.getMonth().toString(), getRandomNumber());
                }
                break;
            case "Anual":
                for (int i = 0; i < 5; i++) {
                    LocalDate date = today.minusYears(i);
                    data.put(String.valueOf(date.getYear()), getRandomNumber());
                }
                break;
        }

        return data;
    }

    private int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(100);
    }

    class Account {
        private String username;
        private String password;
        private double balance;
        private boolean isAdmin;
        private List<String> transactionHistory;

        public Account(String username, String password, double balance, boolean isAdmin) {
            this.username = username;
            this.password = password;
            this.balance = balance;
            this.isAdmin = isAdmin;
            this.transactionHistory = new ArrayList<>();
            addTransaction("Cuenta creada con saldo inicial de $" + balance);
        }

        public boolean validatePassword(String password) {
            return this.password.equals(password);
        }

        public double getBalance() {
            return balance;
        }

        public void deposit(double amount) {
            this.balance += amount;
            addTransaction("Depósito de $" + amount + ". Saldo actual: $" + balance);
        }

        public boolean withdraw(double amount) {
            if (amount <= balance) {
                this.balance -= amount;
                addTransaction("Retiro de $" + amount + ". Saldo actual: $" + balance);
                return true;
            } else {
                addTransaction("Intento de retiro fallido de $" + amount + ". Fondos insuficientes.");
                return false;
            }
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public List<String> getTransactionHistory() {
            return transactionHistory;
        }

        private void addTransaction(String transaction) {
            transactionHistory.add(transaction);
        }
    }
}
