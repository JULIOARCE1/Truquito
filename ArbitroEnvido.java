import java.util.Arrays;

public class ArbitroEnvido {
    private final Mesa mesa;
    private final GestorEnvido gestorEnvido;
    private final GestorTruco gestorTruco;
    private final VistaJuego vista;
    private final int puntajeLimite;

    public ArbitroEnvido(Mesa mesa, GestorEnvido gestorEnvido, GestorTruco gestorTruco, VistaJuego vista, int puntajeLimite) {
        this.mesa = mesa;
        this.gestorEnvido = gestorEnvido;
        this.gestorTruco = gestorTruco;
        this.vista = vista;
        this.puntajeLimite = puntajeLimite;
    }

    public boolean gestionarEnvido1v1(int posTiro) {
        if (gestorEnvido.isResuelto()) return false;
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        JugadorBot botRival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);

        if (posTiro == 0) {
            if (mesa.getIndiceMano() == 0) {
                int c = vista.pedirOpcion("Sos mano (tenés " + t1 + " de envido). ¿Querés cantar Envido?",
                        Arrays.asList("[1] Envido", "[2] Real Envido", "[3] Falta Envido", "[0] Paso"), 0, 3);
                if (c > 0) {
                    resolverIniciadoPorHumano(c, t1, t2);
                    gestorEnvido.marcarResuelto();
                    return hayGanador();
                } else {
                    vista.mostrarMensaje("Paso.");
                }
            } else {
                if (botRival.quiereAbrirEnvido(t2, true)) {
                    vista.mostrarMensaje("\n" + botRival.getNombre() + " (mano) canta: ¡ENVIDO!");
                    resolverIniciadoPorRival(t1, t2);
                    gestorEnvido.marcarResuelto();
                    return hayGanador();
                } else {
                    vista.mostrarMensaje(botRival.getNombre() + " (mano): Paso.");
                }
            }
        } else if (posTiro == 1) {
            if (mesa.getIndiceMano() == 1) {
                int c = vista.pedirOpcion("El rival pasó (tenés " + t1 + " de envido). ¿Querés cantar Envido de pie?",
                        Arrays.asList("[1] Envido", "[2] Real Envido", "[3] Falta Envido", "[0] Paso"), 0, 3);
                if (c > 0) {
                    resolverIniciadoPorHumano(c, t1, t2);
                } else {
                    vista.mostrarMensaje("Paso.");
                }
                gestorEnvido.marcarResuelto();
                return hayGanador();
            } else {
                if (botRival.quiereAbrirEnvido(t2, false)) {
                    vista.mostrarMensaje("\n" + botRival.getNombre() + " canta de pie: ¡ENVIDO!");
                    resolverIniciadoPorRival(t1, t2);
                }
                gestorEnvido.marcarResuelto();
                return hayGanador();
            }
        }
        return false;
    }

    public boolean gestionarEnvidoParejas(int penultimo, int ultimo) {
        if (gestorEnvido.isResuelto()) return false;
        vista.mostrarMensaje("\n--- TURNO DE ENVIDO (ÚLTIMOS 2 JUGADORES) ---");
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        boolean canto = false;

        Jugador jPen = mesa.getJugador(penultimo);
        Jugador jUlt = mesa.getJugador(ultimo);

        if (jPen instanceof JugadorHumano) {
            int op = vista.pedirOpcion("Sos el penúltimo (tu equipo tiene " + t1 + " de envido). ¿Deseás cantar Envido?",
                    Arrays.asList("[1] Sí", "[0] No"), 0, 1);
            if (op == 1) {
                canto = true;
                evaluarEnvidoParejas(t1, t2, true);
            }
        } else if (jPen instanceof JugadorBot bot) {
            if (bot.quiereAbrirEnvido(CalculadorEnvido.calcular(bot.getMano()), false)) {
                vista.mostrarMensaje(bot.getNombre() + " canta: ¡ENVIDO!");
                canto = true;
                evaluarEnvidoParejas(t1, t2, mesa.getEquipoDe(bot) == mesa.getEquipo1());
            }
        }

        if (!canto) {
            if (jUlt instanceof JugadorHumano) {
                int op = vista.pedirOpcion("Sos el pie (tu equipo tiene " + t1 + " de envido). ¿Deseás cantar Envido?",
                        Arrays.asList("[1] Sí", "[0] No"), 0, 1);
                if (op == 1) {
                    evaluarEnvidoParejas(t1, t2, true);
                } else {
                    vista.mostrarMensaje("Los últimos dos pasaron sin cantar Envido.");
                }
            } else if (jUlt instanceof JugadorBot bot) {
                if (bot.quiereAbrirEnvido(CalculadorEnvido.calcular(bot.getMano()), false)) {
                    vista.mostrarMensaje(bot.getNombre() + " canta: ¡ENVIDO!");
                    evaluarEnvidoParejas(t1, t2, mesa.getEquipoDe(bot) == mesa.getEquipo1());
                } else {
                    vista.mostrarMensaje("Los últimos dos pasaron sin cantar Envido.");
                }
            }
        }
        gestorEnvido.marcarResuelto();
        return hayGanador();
    }

    private void evaluarEnvidoParejas(int t1, int t2, boolean cantoEq1) {
        gestorEnvido.registrarCanto(1);
        if (cantoEq1) {
            JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r.responderEnvido(t2, 1, false) == 1) {
                vista.mostrarMensaje("Rivales responden: ¡QUIERO!");
                definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                vista.mostrarMensaje("Rivales responden: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        } else {
            int op = vista.pedirOpcion("El rival cantó Envido (tu equipo tiene " + t1 + " de envido). ¿Aceptás para tu equipo?",
                    Arrays.asList("[1] Quiero", "[0] No Quiero"), 0, 1);
            if (op == 1) {
                definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        }
    }

    public void resolverIniciadoPorHumano(int tipo, int t1, int t2) {
        gestorEnvido.registrarCanto(tipo);
        JugadorBot rival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int resp = rival.responderEnvido(t2, tipo, mesa.getIndiceMano() != 0);

        boolean quiereTrucoBot = (!gestorTruco.estaActivo() && rival.quiereCantarTruco(1));

        if (resp == 1) {
            int pts = gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite);
            if (quiereTrucoBot) {
                vista.mostrarMensaje("Rival responde: ¡QUIERO Y TRUCO!");
                definirGanador(t1, t2, pts);
                gestorEnvido.marcarResuelto();
                if (hayGanador()) return;
                gestorTruco.proponerAumento(mesa.getEquipo2());
            } else {
                vista.mostrarMensaje("Rival responde: ¡QUIERO!");
                definirGanador(t1, t2, pts);
                gestorEnvido.marcarResuelto();
            }
        } else if (resp == 2) {
            int ptsNo = gestorEnvido.getPuntosNoQuiero();
            if (quiereTrucoBot) {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO Y TRUCO!");
                mesa.getEquipo1().sumarPuntos(ptsNo);
                gestorEnvido.marcarResuelto();
                if (hayGanador()) return;
                gestorTruco.proponerAumento(mesa.getEquipo2());
            } else {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(ptsNo);
                gestorEnvido.marcarResuelto();
            }
        } else {
            vista.mostrarMensaje("Rival responde: ¡QUIERO Y REAL ENVIDO!");
            gestorEnvido.registrarCanto(2);
            int op = vista.pedirOpcion("¿Aceptás? (Tenés " + t1 + " de envido)",
                    Arrays.asList("[1] Quiero", "[2] No Quiero"), 1, 2);
            if (op == 1) {
                definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
            gestorEnvido.marcarResuelto();
        }
    }

    public void resolverIniciadoPorRival(int t1, int t2) {
        gestorEnvido.registrarCanto(1);
        int r = vista.pedirOpcion("¿Aceptás? (Tenés " + t1 + " de envido)",
                Arrays.asList("[1] Quiero", "[2] No Quiero", "[3] Real Envido", "[4] ¡Quiero y Truco!", "[5] ¡No Quiero y Truco!"), 1, 5);

        if (r == 1) {
            definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            gestorEnvido.marcarResuelto();
        } else if (r == 2) {
            mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            gestorEnvido.marcarResuelto();
        } else if (r == 3) {
            vista.mostrarMensaje("Cantaste: ¡REAL ENVIDO!");
            gestorEnvido.registrarCanto(2);
            JugadorBot rival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (rival.responderEnvido(t2, 2, false) == 1) {
                vista.mostrarMensaje("Rival responde: ¡QUIERO!");
                definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
            gestorEnvido.marcarResuelto();
        } else if (r == 4) {
            vista.mostrarMensaje("Cantaste: ¡QUIERO Y TRUCO!");
            definirGanador(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return;
            gestorTruco.proponerAumento(mesa.getEquipo1());
        } else if (r == 5) {
            vista.mostrarMensaje("Cantaste: ¡NO QUIERO Y TRUCO!");
            mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return;
            gestorTruco.proponerAumento(mesa.getEquipo1());
        }
    }

    private void definirGanador(int t1, int t2, int pts) {
        vista.mostrarMensaje("\n>> Resolución Envido: " + mesa.getEquipo1().getNombre() + " (" + t1 + ") vs " + 
                             mesa.getEquipo2().getNombre() + " (" + t2 + ")");
        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        if (t1 > t2 || (t1 == t2 && manoEsEq1)) {
            vista.mostrarAlerta(mesa.getEquipo1().getNombre() + " gana el envido (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            vista.mostrarAlerta(mesa.getEquipo2().getNombre() + " gana el envido (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private boolean hayGanador() {
        return mesa.getEquipo1().getPuntos() >= puntajeLimite || mesa.getEquipo2().getPuntos() >= puntajeLimite;
    }
}