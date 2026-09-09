import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("       TRUCO ARGENTINO DE CONSOLA       ");
        System.out.println("========================================");
        System.out.println("Selecciona la modalidad de jugadores:");
        System.out.println("  [1] Mano a mano (1 vs 1)");
        System.out.println("  [2] En parejas (2 vs 2)");

        int modalidad = 1;
        while (true) {
            System.out.print("Elige modalidad (1 o 2): ");
            if (scanner.hasNextInt()) {
                modalidad = scanner.nextInt();
                scanner.nextLine();
                if (modalidad == 1 || modalidad == 2) break;
            } else {
                scanner.nextLine();
            }
            System.out.println("Opcion invalida.");
        }

        System.out.println("\nSelecciona el puntaje limite:");
        System.out.println("  [1] 15 puntos");
        System.out.println("  [2] 30 puntos");

        int puntos = 30;
        while (true) {
            System.out.print("Elige puntaje (1 o 2): ");
            if (scanner.hasNextInt()) {
                int op = scanner.nextInt();
                scanner.nextLine();
                if (op == 1) { puntos = 15; break; }
                if (op == 2) { puntos = 30; break; }
            } else {
                scanner.nextLine();
            }
            System.out.println("Opcion invalida.");
        }

        if (modalidad == 1) {
            Partida partida = new Partida(puntos, scanner);
            partida.iniciar();
        } else {
            PartidaParejas partidaP = new PartidaParejas(puntos, scanner);
            partidaP.iniciar();
        }

        scanner.close();
    }
}
