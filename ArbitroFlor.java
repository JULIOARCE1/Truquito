import java.util.Arrays;

public class ArbitroFlor {
    private final Mesa mesa;
    private final VistaJuego vista;
    private final int puntajeLimite;

    public ArbitroFlor(Mesa mesa, VistaJuego vista, int puntajeLimite) {
        this.mesa = mesa;
        this.vista = vista;
        this.puntajeLimite = puntajeLimite;
    }

    public boolean gestionarFaseFlor() {
        boolean f1 = mesa.getEquipo1().tieneAlgunaFlor();
        boolean f2 = mesa.getEquipo2().tieneAlgunaFlor();

        if (!f1 && !f2) return false;

        vista.mostrarMensaje("\n--- FASE DE FLOR ---");
        if (f1 && !f2) {
            vista.mostrarAlerta(mesa.getEquipo1().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo1().sumarPuntos(3);
            return true;
        }
        if (!f1 && f2) {
            vista.mostrarAlerta(mesa.getEquipo2().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo2().sumarPuntos(3);
            return true;
        }

        vista.mostrarAlerta("¡Ambos equipos tienen Flor!");
        resolverDobleFlor();
        return true;
    }

    private void resolverDobleFlor() {
        int t1 = mesa.getEquipo1().getMejorTantoFlor();
        int t2 = mesa.getEquipo2().getMejorTantoFlor();
        int resto = calcularResto();
        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());

        if (manoEsEq1) {
            vista.mostrarMensaje("Tu equipo canta: ¡FLOR!");
            JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r1.cantarFrenteAFlor(t2) == 2) {
                vista.mostrarMensaje("Rival responde: ¡CONTRAFLOR AL RESTO!");
                int op = vista.pedirOpcion("¿Qué respondés?", Arrays.asList("[1] Con flor quiero", "[2] Con flor me achico"), 1, 2);
                if (op == 1) {
                    definirGanadorFlor(t1, t2, resto + 6, true);
                } else {
                    mesa.getEquipo2().sumarPuntos(4);
                }
            } else {
                vista.mostrarMensaje("Rival responde: ¡FLOR!");
                definirGanadorFlor(t1, t2, 6, true);
            }
        } else {
            vista.mostrarMensaje(mesa.getEquipo2().getNombre() + " canta: ¡FLOR!");
            int op = vista.pedirOpcion("¿Qué respondés?", Arrays.asList("[1] ¡Flor!", "[2] ¡Contraflor al resto!"), 1, 2);
            if (op == 1) {
                definirGanadorFlor(t1, t2, 6, false);
            } else {
                JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                if (r1.responderContraflorAlResto(t2) == 1) {
                    vista.mostrarMensaje("Rival responde: ¡CON FLOR QUIERO!");
                    definirGanadorFlor(t1, t2, resto + 6, false);
                } else {
                    vista.mostrarMensaje("Rival responde: ¡CON FLOR ME ACHICO!");
                    mesa.getEquipo1().sumarPuntos(4);
                }
            }
        }
    }

    private void definirGanadorFlor(int t1, int t2, int pts, boolean manoEsEq1) {
        vista.mostrarMensaje("\n>> Resolución Flor: " + mesa.getEquipo1().getNombre() + " (" + t1 + ") vs " + 
                             mesa.getEquipo2().getNombre() + " (" + t2 + ")");
        if (t1 > t2 || (t1 == t2 && manoEsEq1)) {
            vista.mostrarAlerta("Gana la flor " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            vista.mostrarAlerta("Gana la flor " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private int calcularResto() {
        int lider = Math.max(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos());
        return puntajeLimite - lider;
    }
}