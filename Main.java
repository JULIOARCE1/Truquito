import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("       TRUCO ARGENTINO DE CONSOLA       ");
        System.out.println("========================================");
        System.out.println("Seleccioná modalidad:");
        System.out.println("  [1] Mano a mano (1 vs 1)");
        System.out.println("  [2] En parejas (2 vs 2)");

        int mod = 1;
        while (true) {
            System.out.print("Elegí modalidad (1 o 2): ");
            if (scanner.hasNextInt()) {
                mod = scanner.nextInt();
                scanner.nextLine();
                if (mod == 1 || mod == 2) break;
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida.");
        }

        System.out.println("\nSeleccioná el puntaje límite:");
        System.out.println("  [1] 15 puntos");
        System.out.println("  [2] 30 puntos");

        int pts = 30;
        while (true) {
            System.out.print("Elegí puntaje (1 o 2): ");
            if (scanner.hasNextInt()) {
                int op = scanner.nextInt();
                scanner.nextLine();
                if (op == 1) { pts = 15; break; }
                if (op == 2) { pts = 30; break; }
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida.");
        }

        Equipo e1 = new Equipo("Tu Equipo");
        Equipo e2 = new Equipo("Equipo Rival");

        if (mod == 1) {
            e1.agregarJugador(new JugadorHumano("Tú", scanner));
            e2.agregarJugador(new JugadorBot("Bot Rival"));
        } else {
            e1.agregarJugador(new JugadorHumano("Tú", scanner));
            e2.agregarJugador(new JugadorBot("Rival Este"));
            e1.agregarJugador(new JugadorBot("Tu Compañero"));
            e2.agregarJugador(new JugadorBot("Rival Oeste"));
        }

        Mesa mesa = new Mesa(e1, e2);
        Partida partida = new Partida(mesa, pts, scanner);
        partida.iniciar();

        scanner.close();
    }
}