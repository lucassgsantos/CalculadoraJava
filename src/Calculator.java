import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class Calculator {
    private static final int WINDOW_WIDTH = 360;
    private static final int WINDOW_HEIGHT = 540;

    private static final Color LIGHT_GRAY = new Color(212, 212, 210);
    private static final Color DARK_GRAY = new Color(80, 80, 80);
    private static final Color BLACK = new Color(28, 28, 28);
    private static final Color ORANGE = new Color(255, 149, 0);

    private static final String[] BUTTON_LABELS = {
        "AC", "+/-", "%", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "√", "="
    };
    private static final String[] RIGHT_OPERATORS = {"÷", "×", "-", "+", "="};
    private static final String[] TOP_OPERATORS = {"AC", "+/-", "%"};

    private JLabel displayLabel = new JLabel();
    private JButton activeOperatorButton = null;

    private String firstOperand = "0";
    private String operator = null;
    private boolean waitingForSecondOperand = false;

    public Calculator() {
        JFrame frame = new JFrame("Calculadora");
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        displayLabel.setBackground(BLACK);
        displayLabel.setForeground(Color.WHITE);
        displayLabel.setFont(new Font("Arial", Font.PLAIN, 72));
        displayLabel.setHorizontalAlignment(JLabel.RIGHT);
        displayLabel.setText("0");
        displayLabel.setOpaque(true);
        displayLabel.setPreferredSize(new Dimension(WINDOW_WIDTH, 120));
        displayLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 16));
        frame.add(displayLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(5, 4));
        buttonsPanel.setBackground(BLACK);
        frame.add(buttonsPanel);

        for (String label : BUTTON_LABELS) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 28));
            button.setFocusable(false);
            button.setBorder(new LineBorder(BLACK));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (Arrays.asList(TOP_OPERATORS).contains(label)) {
                button.setBackground(LIGHT_GRAY);
                button.setForeground(BLACK);
            } else if (Arrays.asList(RIGHT_OPERATORS).contains(label)) {
                button.setBackground(ORANGE);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(DARK_GRAY);
                button.setForeground(Color.WHITE);
            }

            buttonsPanel.add(button);
            button.addActionListener(e -> handleButtonClick(label, button));
        }

        addKeyboardSupport(frame);

        frame.setVisible(true);
    }

    private void handleButtonClick(String value, JButton source) {
        if (Arrays.asList(RIGHT_OPERATORS).contains(value)) {
            handleOperator(value, source);
        } else if (Arrays.asList(TOP_OPERATORS).contains(value)) {
            handleTopOperator(value);
        } else if (value.equals("√")) {
            handleSquareRoot();
        } else {
            handleDigit(value);
        }
    }

    private void handleOperator(String value, JButton source) {
        if (value.equals("=")) {
            if (operator != null) {
                String secondOperand = displayLabel.getText();
                double numA = Double.parseDouble(firstOperand);
                double numB = Double.parseDouble(secondOperand);
                double result = calculate(numA, numB, operator);

                if (Double.isInfinite(result) || Double.isNaN(result)) {
                    displayLabel.setText("Erro");
                    clearState();
                    adjustFontSize();
                    return;
                }

                displayLabel.setText(formatResult(result));
                clearState();
            }
            clearActiveOperator();
        } else {
            if (operator != null && !waitingForSecondOperand) {
                String secondOperand = displayLabel.getText();
                double numA = Double.parseDouble(firstOperand);
                double numB = Double.parseDouble(secondOperand);
                double result = calculate(numA, numB, operator);

                if (Double.isInfinite(result) || Double.isNaN(result)) {
                    displayLabel.setText("Erro");
                    clearState();
                    adjustFontSize();
                    return;
                }

                displayLabel.setText(formatResult(result));
                firstOperand = formatResult(result);
            } else {
                firstOperand = displayLabel.getText();
            }
            operator = value;
            waitingForSecondOperand = true;

            clearActiveOperator();
            source.setBackground(Color.WHITE);
            source.setForeground(ORANGE);
            activeOperatorButton = source;
        }
        adjustFontSize();
    }

    private void handleTopOperator(String value) {
        switch (value) {
            case "AC":
                clearState();
                displayLabel.setText("0");
                clearActiveOperator();
                break;
            case "+/-":
                if (displayLabel.getText().equals("Erro")) return;
                double negValue = Double.parseDouble(displayLabel.getText()) * -1;
                displayLabel.setText(formatResult(negValue));
                break;
            case "%":
                if (displayLabel.getText().equals("Erro")) return;
                double percentValue = Double.parseDouble(displayLabel.getText()) / 100;
                displayLabel.setText(formatResult(percentValue));
                break;
        }
        adjustFontSize();
    }

    private void handleSquareRoot() {
        if (displayLabel.getText().equals("Erro")) return;
        double current = Double.parseDouble(displayLabel.getText());
        if (current < 0) {
            displayLabel.setText("Erro");
            clearState();
        } else {
            double result = Math.sqrt(current);
            displayLabel.setText(formatResult(result));
        }
        adjustFontSize();
    }

    private void handleDigit(String value) {
        clearActiveOperator();

        if (displayLabel.getText().equals("Erro")) {
            displayLabel.setText("0");
        }

        if (value.equals(".")) {
            if (!displayLabel.getText().contains(".")) {
                displayLabel.setText(displayLabel.getText() + ".");
            }
        } else if (waitingForSecondOperand) {
            displayLabel.setText(value);
            waitingForSecondOperand = false;
        } else if (displayLabel.getText().equals("0")) {
            displayLabel.setText(value);
        } else {
            displayLabel.setText(displayLabel.getText() + value);
        }
        adjustFontSize();
    }

    private double calculate(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "×": return a * b;
            case "÷":
                if (b == 0) return Double.NaN;
                return a / b;
            default: return 0;
        }
    }

    private void clearState() {
        firstOperand = "0";
        operator = null;
        waitingForSecondOperand = false;
    }

    private void clearActiveOperator() {
        if (activeOperatorButton != null) {
            activeOperatorButton.setBackground(ORANGE);
            activeOperatorButton.setForeground(Color.WHITE);
            activeOperatorButton = null;
        }
    }

    private String formatResult(double number) {
        if (number % 1 == 0 && Math.abs(number) < 1e15) {
            return Long.toString((long) number);
        }
        String result = Double.toString(number);
        if (result.length() > 12) {
            result = String.format("%.6g", number);
        }
        return result;
    }

    private void adjustFontSize() {
        int length = displayLabel.getText().length();
        if (length <= 6) {
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 72));
        } else if (length <= 9) {
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 52));
        } else if (length <= 12) {
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 40));
        } else {
            displayLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        }
    }

    private void addKeyboardSupport(JFrame frame) {
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = e.getKeyChar();
                int code = e.getKeyCode();

                if (key >= '0' && key <= '9') {
                    handleDigit(String.valueOf(key));
                } else if (key == '.') {
                    handleDigit(".");
                } else if (key == '+') {
                    handleButtonClick("+", null);
                } else if (key == '-') {
                    handleButtonClick("-", null);
                } else if (key == '*') {
                    handleButtonClick("×", null);
                } else if (key == '/') {
                    handleButtonClick("÷", null);
                } else if (key == '\n' || key == '=') {
                    handleButtonClick("=", null);
                } else if (code == KeyEvent.VK_BACK_SPACE) {
                    String text = displayLabel.getText();
                    if (text.length() > 1) {
                        displayLabel.setText(text.substring(0, text.length() - 1));
                    } else {
                        displayLabel.setText("0");
                    }
                    adjustFontSize();
                } else if (code == KeyEvent.VK_ESCAPE) {
                    handleButtonClick("AC", null);
                }
            }
        });
    }
}
