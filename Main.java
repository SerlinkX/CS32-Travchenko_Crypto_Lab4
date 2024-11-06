import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.Arrays;

public class Main extends Application {

    // Фіксоване значення α
    private static final double ALPHA_FIXED = 0.00007;
    private static final double ATTACK_THRESHOLD = 1e-3;

    // Функція для обчислення ймовірності успіху атаки
    public static double calculateAttackSuccessProbability(double pM, double pH, double z) {
        return Math.exp(-2 * z * (pH - pM));
    }

    // Обчислення αH через pH та αM через pM
    public static double[] calculateAlphaHAlphaM(double pM, double pH, double alpha) {
        double alphaM = alpha * (pM / (pH + pM));
        double alphaH = alpha * (pH / (pH + pM));
        return new double[]{alphaH, alphaM};
    }

    // Функція для обчислення p' H та p' M
    public static double[] calculateProbabilities(double pH, double alphaM, int DH) {
        double pPrimeH = 1 - Math.exp(-alphaM * DH) * pH;
        double pPrimeM = Math.exp(-alphaM * DH) * pH;
        return new double[]{pPrimeH, pPrimeM};
    }

    // Метод для побудови графіка залежності блоків підтвердження від DH
    private LineChart<Number, Number> buildChart(double pM) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Кількість блоків підтвердження z");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Ймовірність успіху атаки");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Залежність ймовірності успіху атаки від блоків підтвердження");

        // Ітерація по значенням DH
        int[] DHValues = {0, 15, 30, 60, 120, 180};
        double pH = 1 - pM;

        for (int DH : DHValues) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("DH = " + DH);

            // Обчислення αH та αM через pH та pM
            double[] alphaValues = calculateAlphaHAlphaM(pM, pH, ALPHA_FIXED);
            double alphaM = alphaValues[1];

            // Обчислення p' H та p' M
            double[] pPrimeValues = calculateProbabilities(pH, alphaM, DH);
            double pPrimeH = pPrimeValues[0];
            double pPrimeM = pPrimeValues[1];

            // Пошук мінімального z для кожного DH
            for (int z = 0; z <= 100; z++) {
                double attackProbability = calculateAttackSuccessProbability(pPrimeM, pPrimeH, z);
                series.getData().add(new XYChart.Data<>(z, attackProbability));

                if (attackProbability < ATTACK_THRESHOLD) {
                    break;
                }
            }

            lineChart.getData().add(series);
        }

        return lineChart;
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Додамо вибір значення pM через ComboBox
        ComboBox<Double> pMComboBox = new ComboBox<>();
        pMComboBox.getItems().addAll(Arrays.asList(0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4));
        pMComboBox.setValue(0.1);  // Стартове значення

        // Коли вибирається значення pM, будуємо новий графік
        pMComboBox.setOnAction(event -> {
            double selectedPM = pMComboBox.getValue();
            LineChart<Number, Number> newChart = buildChart(selectedPM);
            root.setCenter(newChart);
        });

        // Початковий графік
        LineChart<Number, Number> chart = buildChart(0.1);
        root.setTop(pMComboBox);
        root.setCenter(chart);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Атака подвійної витрати: Візуалізація");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
