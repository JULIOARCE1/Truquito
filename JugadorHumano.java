import java.util.Scanner;

public class JugadorHumano extends Jugador {
    private final Scanner scanner;

    public JugadorHumano(String nombre, Scanner scanner) {
        super(nombre);
        this.scanner = scanner;
    }

    @Override
    public Carta jugarCarta() {
        System.out.println("\nTus cartas:");
        for (int i = 0; i < mano.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + mano.get(i));
        }
        System.out.println("  [0] Salir del juego");

        int opcion = -1;
        while (opcion < 0 || opcion > mano.size()) {
            System.out.print("Elegí una opción (0-" + mano.size() + "): ");
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                if (opcion == 0) {
                    System.out.println("\nPartida cancelada por el jugador. ¡Nos vemos!");
                    System.exit(0);
                }
            } else {
                scanner.next();
            }
        }
        return mano.remove(opcion - 1);
    }
}