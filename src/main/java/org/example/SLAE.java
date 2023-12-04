package org.example;


public class SLAE {
    private int n;
    private double[][] A;
    private double[] B;
    private int[] permut;
    private double epsilon;

    public SLAE(int n, double epsilon) {
        this.n = n;
        this.epsilon = epsilon;
        A = new double[n][n];
        B = new double[n];
        permut = new int[n];

        // Initialize A, B, and permut arrays
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
        // Check convergence condition
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    sum += Math.abs(get(j, i));
                }
            }
            if (sum >= Math.abs(get(i, i))) {
                // Convergence condition not satisfied
                return -1;
            }
        }

        // Initialize X0 = B
        System.arraycopy(B, 0, X, 0, n);

        int iterations = 0;
        while (!isZero(discrepancy(X))) {
            // Iteration
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
                System.out.printf("%+f x%d%f", get(x, y), (x + 1),(x == 3));
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
                -16.0, 1.0, 9.0, 4.0,
                9.0, 14.0, 2.0, -1.0,
                -7.0, 1.0, 13.0, 4.0,
                3.0, 0.0, -8.0, -12.0
        };
        double[] B = {34.0, 67.0, 38.0, -68.0};
        double[] X = new double[4];

        SLAE system = new SLAE(4, 0.001);
        system.setA(A);
        system.setB(B);

        for (double p = 0.0; p <= 1.0; p += 0.01) {
            System.out.printf("p=%.2f\n", p);
            int iter = system.solveZeidel(X, p, 100);
            if (iter != -1) {
                System.out.printf("\tSolution obtained in %d iterations\n", iter);
                System.out.printf("\tSolution: [%f %f %f %f]\n", X[0], X[1], X[2], X[3]);
                System.out.printf("\tDiscrepancy norm: %f\n", system.discrepancy(X));
            } else {
                System.out.println("\tConvergence not observed");
            }
        }
    }
}
