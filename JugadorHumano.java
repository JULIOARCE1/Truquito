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

        int opcion = -1;
        while (opcion < 1 || opcion > mano.size()) {
            System.out.print("Elegí una carta (1-" + mano.size() + "): ");
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
            } else {
                scanner.next();
            }
        }
        return mano.remove(opcion - 1);
    }
}
