import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("       TRUCO ARGENTINO DE CONSOLA       ");
        System.out.println("========================================");
        System.out.println("Seleccioná la modalidad de la partida:");
        System.out.println("  [1] Partida a 15 puntos");
        System.out.println("  [2] Partida a 30 puntos");

        int puntajeLimite = 30;
        while (true) {
            System.out.print("Elegí una opción (1 o 2): ");
            if (scanner.hasNextInt()) {
                int op = scanner.nextInt();
                scanner.nextLine();
                if (op == 1) {
                    puntajeLimite = 15;
                    break;
                } else if (op == 2) {
                    puntajeLimite = 30;
                    break;
                }
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida.");
        }

        Partida partida = new Partida(puntajeLimite, scanner);
        partida.iniciar();
        scanner.close();
    }
}