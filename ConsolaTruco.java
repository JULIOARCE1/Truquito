import java.util.List;
import java.util.Scanner;

public class ConsolaTruco implements VistaJuego {
    private final Scanner scanner;

    public ConsolaTruco(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public void mostrarAlerta(String mensaje) {
        System.out.println("-> " + mensaje);
    }

    @Override
    public void mostrarSeparador() {
        System.out.println("--------------------------------------------------");
    }

    @Override
    public void mostrarInicioPartida(int limite, int totalJugadores, String eq1, String eq2) {
        System.out.println("\n==================================================");
        System.out.println("  INICIO DE PARTIDA A " + limite + " PUNTOS (" + totalJugadores + " JUGADORES)");
        System.out.println("  " + eq1 + " vs " + eq2);
        System.out.println("==================================================");
    }

    @Override
    public void mostrarFinPartida(String ganador) {
        System.out.println("\n==================================================");
        System.out.println("¡GANÓ " + ganador.toUpperCase() + "!");
        System.out.println("==================================================");
    }

    @Override
    public void mostrarTanteador(String eq1, String pts1, String eq2, String pts2, int limite) {
        System.out.println("\n==================================================");
        System.out.println("TANTEADOR (A " + limite + " PUNTOS):");
        System.out.println("  " + eq1 + ": " + pts1);
        System.out.println("  " + eq2 + ": " + pts2);
        System.out.println("==================================================");
    }

    @Override
    public void mostrarNuevaMano(String manoNombre, String detalleEquipo) {
        System.out.println("\n--------------------------------------------------");
        if (detalleEquipo == null || detalleEquipo.isEmpty()) {
            System.out.println("NUEVA MANO - Es mano: " + manoNombre);
        } else {
            System.out.println("NUEVA MANO - Es mano: " + manoNombre + " (" + detalleEquipo + ")");
        }
    }

    @Override
    public void mostrarCartasPropias(List<Carta> cartas, int tantoEnvido) {
        System.out.println("\n--- TUS CARTAS ---");
        System.out.println("  " + cartas + " | Envido total: " + tantoEnvido);
    }

    @Override
    public void mostrarCartasCompanero(String nombreCompanero, List<Carta> cartas, int tantoEnvido) {
        System.out.println("  Tu Compañero (" + nombreCompanero + "): " + cartas + " | Envido total: " + tantoEnvido);
        System.out.println("-------------------");
    }

    @Override
    public int pedirOpcion(String titulo, List<String> opciones, int min, int max) {
        if (titulo != null && !titulo.isEmpty()) {
            System.out.println(titulo);
        }
        for (String op : opciones) {
            System.out.println("  " + op);
        }

        while (true) {
            System.out.print("-> Elegí una opción: ");
            if (scanner.hasNextInt()) {
                int elegido = scanner.nextInt();
                scanner.nextLine();
                if (elegido >= min && elegido <= max) {
                    return elegido;
                }
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida (" + min + "-" + max + ").");
        }
    }

    @Override
    public boolean confirmarContinuar() {
        System.out.print("\nPresioná ENTER para la siguiente mano (o 's' para salir): ");
        String r = scanner.nextLine().trim().toLowerCase();
        return !r.equals("s") && !r.equals("salir");
    }
}