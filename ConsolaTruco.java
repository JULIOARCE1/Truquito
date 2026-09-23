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
        System.out.println("  ¡¡¡ GANÓ " + ganador.toUpperCase() + " !!!");
        System.out.println("==================================================");
    }

    @Override
    public void mostrarTanteador(String eq1, String pts1, String eq2, String pts2, int limite) {
        System.out.println("\n=================== TANTEADOR ===================");
        dibujarLineaEquipo(eq1, pts1, limite);
        dibujarLineaEquipo(eq2, pts2, limite);
        System.out.println("=================================================");
    }

    private void dibujarLineaEquipo(String nombre, String ptsTexto, int limite) {
        int total = extraerPuntosNumericos(ptsTexto);
        System.out.println(nombre + " [" + total + " pts]:");

        if (limite == 30) {
            int malas = Math.min(total, 15);
            int buenas = Math.max(0, total - 15);
            System.out.println("  Malas  (" + String.format("%2d", malas) + "/15):  " + renderizarFosforos(malas));
            System.out.println("  Buenas (" + String.format("%2d", buenas) + "/15): " + renderizarFosforos(buenas));
        } else {
            System.out.println("  Puntos (" + String.format("%2d", total) + "/" + limite + "): " + renderizarFosforos(total));
        }
    }

    private String renderizarFosforos(int puntos) {
        if (puntos <= 0) return "[ Sin puntos ]";
        StringBuilder sb = new StringBuilder();
        int completos = puntos / 5;
        int resto = puntos % 5;

        // Cada paquete cerrado contiene exactamente 5 fósforos
        for (int i = 0; i < completos; i++) {
            sb.append("[/////] ");
        }

        // El paquete en curso muestra los que van y completa hasta 5 con espacios
        if (resto > 0) {
            sb.append("[");
            for (int i = 0; i < resto; i++) {
                sb.append("/");
            }
            for (int i = resto; i < 5; i++) {
                sb.append(" ");
            }
            sb.append("] ");
        }
        return sb.toString().trim();
    }

    private int extraerPuntosNumericos(String ptsTexto) {
        try {
            if (ptsTexto.contains("[Total: ")) {
                int ini = ptsTexto.indexOf("[Total: ") + 8;
                int fin = ptsTexto.indexOf("]", ini);
                return Integer.parseInt(ptsTexto.substring(ini, fin).trim());
            }
            String limpio = ptsTexto.replaceAll("[^0-9]", " ").trim();
            if (limpio.isEmpty()) return 0;
            return Integer.parseInt(limpio.split("\\s+")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void mostrarNuevaMano(String manoNombre, String detalleEquipo) {
        mostrarSeparador();
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