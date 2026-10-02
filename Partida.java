import java.util.Arrays;
import java.util.List;

public class Partida {
    private final Mesa mesa;
    private final Mazo mazo;
    private final int puntajeLimite;
    private final VistaJuego vista;
    private final GestorTruco gestorTruco;
    private final GestorEnvido gestorEnvido;
    private final ArbitroEnvido arbitroEnvido;
    private final ArbitroFlor arbitroFlor;
    private final RondaTruco rondaTruco;

    public Partida(Mesa mesa, int puntajeLimite, VistaJuego vista) {
        this.mesa = mesa;
        this.puntajeLimite = puntajeLimite;
        this.vista = vista;
        this.mazo = new Mazo();
        this.gestorTruco = new GestorTruco();
        this.gestorEnvido = new GestorEnvido();
        this.arbitroEnvido = new ArbitroEnvido(mesa, gestorEnvido, gestorTruco, vista, puntajeLimite);
        this.arbitroFlor = new ArbitroFlor(mesa, vista, puntajeLimite);
        this.rondaTruco = new RondaTruco(mesa, gestorTruco, gestorEnvido, arbitroEnvido, vista);
    }

    public void iniciar() {
        vista.mostrarInicioPartida(puntajeLimite, mesa.getTotalJugadores(), mesa.getEquipo1().getNombre(), mesa.getEquipo2().getNombre());
        sorteoInicialRey();

        while (!hayGanador()) {
            int iniEq1 = mesa.getEquipo1().getPuntos();
            int iniEq2 = mesa.getEquipo2().getPuntos();

            jugarMano();
            mesa.rotarMano();

            int delta1 = mesa.getEquipo1().getPuntos() - iniEq1;
            int delta2 = mesa.getEquipo2().getPuntos() - iniEq2;

            vista.mostrarMensaje("\n--------------------------------------------------");
            vista.mostrarMensaje("RESUMEN DE ESTA MANO:");
            vista.mostrarMensaje("  " + mesa.getEquipo1().getNombre() + ": +" + delta1 + " pt(s)");
            vista.mostrarMensaje("  " + mesa.getEquipo2().getNombre() + ": +" + delta2 + " pt(s)");

            vista.mostrarTanteador(mesa.getEquipo1().getNombre(), formatearPuntos(mesa.getEquipo1().getPuntos()),
                                   mesa.getEquipo2().getNombre(), formatearPuntos(mesa.getEquipo2().getPuntos()), puntajeLimite);

            if (!hayGanador() && !vista.confirmarContinuar()) {
                vista.mostrarMensaje("\nPartida cancelada.");
                return;
            }
        }

        String ganador = (mesa.getEquipo1().getPuntos() >= puntajeLimite) ? mesa.getEquipo1().getNombre() : mesa.getEquipo2().getNombre();
        vista.mostrarFinPartida(ganador);
    }

    private void sorteoInicialRey() {
        vista.mostrarMensaje("\nSorteando dador y mano (primer 12)...");
        mazo.reiniciar();
        int t = 0;
        while (true) {
            Carta c = mazo.robar();
            Jugador actual = mesa.getJugador(t);
            vista.mostrarMensaje("  " + actual.getNombre() + " saca: " + c);
            if (c.getNumero() == 12) {
                vista.mostrarAlerta("¡" + actual.getNombre() + " sacó el 12 (Dador)!");
                mesa.setIndiceMano((t + 1) % mesa.getTotalJugadores());
                vista.mostrarAlerta("Mano inicial: " + mesa.getJugador(mesa.getIndiceMano()).getNombre());
                break;
            }
            t = (t + 1) % mesa.getTotalJugadores();
        }
    }

    private boolean hayGanador() {
        return mesa.getEquipo1().getPuntos() >= puntajeLimite || mesa.getEquipo2().getPuntos() >= puntajeLimite;
    }

    private void jugarMano() {
        Jugador mano = mesa.getJugador(mesa.getIndiceMano());
        vista.mostrarNuevaMano(mano.getNombre(), mesa.getTotalJugadores() == 2 ? null : mesa.getEquipoDe(mano).getNombre());

        mazo.reiniciar();
        for (Jugador j : mesa.getAsientos()) {
            j.limpiarMano();
            for (int i = 0; i < 3; i++) j.recibirCarta(mazo.robar());
        }

        gestorTruco.reiniciar();
        gestorEnvido.reiniciar();

        Jugador humano = mesa.getJugador(0);
        vista.mostrarCartasPropias(humano.getMano(), CalculadorEnvido.calcular(humano.getMano()));

        // En 2 vs 2 se ocultan las cartas del compañero y se juega la fase de señas
        if (mesa.getTotalJugadores() == 4) {
            gestionarFaseSenas();
        } else {
            vista.mostrarMensaje("-------------------");
        }

        boolean huboFlor = arbitroFlor.gestionarFaseFlor();
        if (hayGanador()) return;

        if (huboFlor) gestorEnvido.marcarResuelto();
        rondaTruco.jugarRondas();
    }

    private void gestionarFaseSenas() {
        JugadorBot compaBot = (JugadorBot) mesa.getJugador(2);
        SenaTruco senaCompa = compaBot.emitirSena();

        vista.mostrarMensaje("\n--- COMUNICACIÓN POR SEÑAS (2 vs 2) ---");
        vista.mostrarAlerta("Tu compañero te hace la seña: " + senaCompa.getAccion() + 
                           " (Significa: " + senaCompa.getSignificado() + ")");

        List<String> opciones = Arrays.asList(
                "[1] Guiñar el ojo (1 de Espada)",
                "[2] Mover la comisura de la boca (1 de Basto)",
                "[3] Levantar las cejas (7 de Espada)",
                "[4] Fruncir la nariz (7 de Oro)",
                "[5] Trompita / Beso (Un 3)",
                "[6] Morder labio / Boca abierta (Un 2)",
                "[7] Cerrar los ojos (Ciego / Sin cartas)",
                "[0] No hacer señas"
        );

        int elegida = vista.pedirOpcion("¿Qué seña querés pasarle a tu compañero?", opciones, 0, 7);
        SenaTruco miSena;
        switch (elegida) {
            case 1 -> miSena = SenaTruco.ANCHO_ESPADA;
            case 2 -> miSena = SenaTruco.ANCHO_BASTO;
            case 3 -> miSena = SenaTruco.SIETE_ESPADA;
            case 4 -> miSena = SenaTruco.SIETE_ORO;
            case 5 -> miSena = SenaTruco.TRES;
            case 6 -> miSena = SenaTruco.DOS;
            case 7 -> miSena = SenaTruco.CIEGO;
            default -> miSena = SenaTruco.NINGUNA;
        }

        compaBot.recibirSenaCompanero(miSena);
        if (miSena != SenaTruco.NINGUNA) {
            vista.mostrarMensaje("Le pasaste a tu compañero la seña: " + miSena.getAccion());
        } else {
            vista.mostrarMensaje("Decidiste no pasar señas.");
        }
        vista.mostrarMensaje("----------------------------------------");
    }

    private String formatearPuntos(int pts) {
        if (puntajeLimite == 15) return pts + " pts";
        if (pts <= 15) return pts + " (Malas)";
        return (pts - 15) + " (Buenas) [Total: " + pts + "]";
    }
}