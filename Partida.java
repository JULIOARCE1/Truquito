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

            vista.mostrarMensaje("\n--------------------------------------------------");
            vista.mostrarMensaje("RESUMEN DE ESTA MANO:");
            vista.mostrarMensaje("  " + mesa.getEquipo1().getNombre() + ": +" + (mesa.getEquipo1().getPuntos() - iniEq1) + " pt(s)");
            vista.mostrarMensaje("  " + mesa.getEquipo2().getNombre() + ": +" + (mesa.getEquipo2().getPuntos() - iniEq2) + " pt(s)");

            vista.mostrarTanteador(mesa.getEquipo1().getNombre(), formatearPuntos(mesa.getEquipo1().getPuntos()),
                                   mesa.getEquipo2().getNombre(), formatearPuntos(mesa.getEquipo2().getPuntos()), puntajeLimite);

            if (!hayGanador() && !vista.confirmarContinuar()) {
                vista.mostrarMensaje("\nPartida cancelada.");
                return;
            }
        }

        vista.mostrarFinPartida(mesa.getEquipo1().getPuntos() >= puntajeLimite ? mesa.getEquipo1().getNombre() : mesa.getEquipo2().getNombre());
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
        if (mesa.getTotalJugadores() == 4) {
            Jugador compa = mesa.getJugador(2);
            vista.mostrarCartasCompanero(compa.getNombre(), compa.getMano(), CalculadorEnvido.calcular(compa.getMano()));
        } else {
            vista.mostrarMensaje("-------------------");
        }

        boolean huboFlor = arbitroFlor.gestionarFaseFlor();
        if (hayGanador()) return;

        if (huboFlor) gestorEnvido.marcarResuelto();
        rondaTruco.jugarRondas();
    }

    private String formatearPuntos(int pts) {
        if (puntajeLimite == 15) return pts + " pts";
        if (pts <= 15) return pts + " (Malas)";
        return (pts - 15) + " (Buenas) [Total: " + pts + "]";
    }
}