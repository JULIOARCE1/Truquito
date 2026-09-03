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
        System.out.println("  [T] Tirar una carta tapada");
        System.out.println("  [0] Rendirse / Salir del juego");

        while (true) {
            System.out.print("Elegí una opción: ");
            String entrada = scanner.nextLine().trim();

            if (entrada.equals("0")) {
                System.out.println("\nPartida cancelada por el jugador.");
                System.exit(0);
            }

            if (entrada.equalsIgnoreCase("T")) {
                System.out.print("¿Qué carta querés tirar tapada? (1-" + mano.size() + "): ");
                if (scanner.hasNextInt()) {
                    int cIndex = scanner.nextInt();
                    scanner.nextLine();
                    if (cIndex >= 1 && cIndex <= mano.size()) {
                        Carta c = mano.remove(cIndex - 1);
                        c.setTapada(true);
                        return c;
                    }
                } else {
                    scanner.nextLine();
                }
                System.out.println("Selección inválida.");
                continue;
            }

            try {
                int opcion = Integer.parseInt(entrada);
                if (opcion >= 1 && opcion <= mano.size()) {
                    Carta c = mano.remove(opcion - 1);
                    c.setTapada(false);
                    return c;
                }
            } catch (NumberFormatException ignored) {}

            System.out.println("Opción inválida.");
        }
    }
}