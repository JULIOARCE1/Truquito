import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        VistaJuego vista = new ConsolaTruco(scanner);

        vista.mostrarMensaje("========================================");
        vista.mostrarMensaje("       TRUCO ARGENTINO DE CONSOLA       ");
        vista.mostrarMensaje("========================================");

        int mod = vista.pedirOpcion("Seleccioná modalidad:", 
                Arrays.asList("[1] Mano a mano (1 vs 1)", "[2] En parejas (2 vs 2)"), 1, 2);

        int ptsOpcion = vista.pedirOpcion("\nSeleccioná el puntaje límite:", 
                Arrays.asList("[1] 15 puntos", "[2] 30 puntos"), 1, 2);
        int pts = (ptsOpcion == 1) ? 15 : 30;

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
        Partida partida = new Partida(mesa, pts, vista);
        partida.iniciar();

        scanner.close();
    }
}