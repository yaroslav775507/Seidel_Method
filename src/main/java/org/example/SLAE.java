package org.example;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;

public class SLAE {
    private final int n;
    private final double[][] A;
    private final double[] B;
    private final int[] permut;
    private final double epsilon;

    public SLAE(int n, double epsilon) {
        this.n = n;
        this.epsilon = epsilon;
        A = new double[n][n];
        B = new double[n];
        permut = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = 0.0;
            }
            B[i] = 0.0;
            permut[i] = i;
        }
    }

    public void setA(double[] values) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = values[n * i + j];
            }
        }
    }

    public void setB(double[] values) {
        System.arraycopy(values, 0, B, 0, n);
    }

    public int solveZeidel(double[] X, double p, int maxIterations) {
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    sum += Math.abs(get(j, i));
                }
            }
            if (sum >= Math.abs(get(i, i))) {
                return -1;
            }
        }
        System.arraycopy(B, 0, X, 0, n);

        int iterations = 0;
        while (!isZero(discrepancy(X))) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (j != i) {
                        sum += get(j, i) * X[j];
                    }
                }
                X[i] = (1.0 + p) * (B[i] - sum) / get(i, i) - p * X[i];
            }

            iterations++;
            if (iterations > maxIterations) {
                break;
            }
        }

        if (isZero(discrepancy(X))) {
            return iterations;
        } else {
            return -1;
        }
    }

    public void print() {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                System.out.printf("%+f x%d%f", get(x, y), (x + 1), (x == 3));
            }
            System.out.printf("= %f\n", B[y]);
        }
    }

    private double discrepancy(double[] X) {
        double ret = 0.0;
        for (int i = 0; i < n; i++) {
            double y = 0.0;
            for (int j = 0; j < n; j++) {
                y += A[i][j] * X[j];
            }
            ret += Math.abs(y - B[i]);
        }
        return ret;
    }

    private double get(int x, int y) {
        return A[y][permut[x]];
    }

    private boolean isZero(double v) {
        return v <= epsilon && v >= -epsilon;
    }

    public static void main(String[] args) {
        double[] A = {
                15.0, 7.0, 2.0, 1.0,
                -3.0, 23.0, 6.0, 2.0,
                2.0, -5.0, -21.0, 7.0,
                -2.0, 1.0, 7.0, 20.0
        };
        double[] B = {19.0, 0.0, -44.0, 21.0};
        double[] X = new double[4];

        SLAE system = new SLAE(4, 0.001);
        system.setA(A);
        system.setB(B);

        List<Double> pValues = new ArrayList<>();
        List<Integer> iterationsValues = new ArrayList<>();
        List<Double> normValues = new ArrayList<>();
        for (double p = -1.0; p <= 1.0; p += 0.01) {
            System.out.printf("p=%.2f\n", p);
            int iter = system.solveZeidel(X, p, 100);
            double norm = system.discrepancy(X);
            double roundedP = Math.round(p * 10.0) / 10.0;
            pValues.add(roundedP);
            //pValues.add(p);
            iterationsValues.add(iter);
            normValues.add(norm);

            if (iter != -1) {
                System.out.printf("\tРешение получено за %d итераций\n", iter);
                System.out.printf("\tРешение: [%f %f %f %f]\n", X[0], X[1], X[2], X[3]);
                System.out.printf("\tНорма невязки: %f\n", norm);
            } else {
                System.out.println("\tСходимости не наблюдается");
            }

        }
        SwingUtilities.invokeLater(() -> {
            XYChart chart = new XYChartBuilder().width(1000).height(1000).title("Зависимость итераций от ускоряющего элемента").xAxisTitle("Ускоряющий параметр P").yAxisTitle("Количество итераций").build();

            chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
            chart.getStyler().setChartTitleVisible(true);
            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
// TODO: 04.12.2023 Округлить метод pValues
            XYSeries series = chart.addSeries("Iterations vs. p", pValues, iterationsValues);

            JFrame frame = new JFrame("Chart");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new XChartPanel<>(chart));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
