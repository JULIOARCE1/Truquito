import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Partida {
    private final Mesa mesa;
    private final Mazo mazo;
    private final int puntajeLimite;
    private final VistaJuego vista;

    private final GestorTruco gestorTruco;
    private final GestorEnvido gestorEnvido;

    public Partida(Mesa mesa, int puntajeLimite, VistaJuego vista) {
        this.mesa = mesa;
        this.puntajeLimite = puntajeLimite;
        this.vista = vista;
        this.mazo = new Mazo();
        this.gestorTruco = new GestorTruco();
        this.gestorEnvido = new GestorEnvido();
    }

    public void iniciar() {
        vista.mostrarInicioPartida(puntajeLimite, mesa.getTotalJugadores(), 
                                   mesa.getEquipo1().getNombre(), mesa.getEquipo2().getNombre());

        sorteoInicialRey();

        while (!hayGanador()) {
            jugarMano();
            mesa.rotarMano();
            mostrarTanteador();

            if (!hayGanador()) {
                if (!vista.confirmarContinuar()) {
                    vista.mostrarMensaje("\nPartida cancelada.");
                    return;
                }
            }
        }

        String ganador = (mesa.getEquipo1().getPuntos() >= puntajeLimite) 
                         ? mesa.getEquipo1().getNombre() 
                         : mesa.getEquipo2().getNombre();
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
        String detalle = (mesa.getTotalJugadores() == 2) ? null : mesa.getEquipoDe(mano).getNombre();
        vista.mostrarNuevaMano(mano.getNombre(), detalle);

        mazo.reiniciar();
        for (Jugador j : mesa.getAsientos()) {
            j.limpiarMano();
            for (int i = 0; i < 3; i++) {
                j.recibirCarta(mazo.robar());
            }
        }

        gestorTruco.reiniciar();
        gestorEnvido.reiniciar();

        mostrarEstadoEquipoHumano();

        boolean huboFlor = gestionarFaseFlor();
        if (hayGanador()) return;

        gestionarRondas(huboFlor);
    }

    private void mostrarEstadoEquipoHumano() {
        Jugador humano = mesa.getJugador(0);
        int tantoHumano = CalculadorEnvido.calcular(humano.getMano());
        vista.mostrarCartasPropias(humano.getMano(), tantoHumano);
        if (mesa.getTotalJugadores() == 4) {
            Jugador compa = mesa.getJugador(2);
            vista.mostrarCartasCompanero(compa.getNombre(), compa.getMano(), CalculadorEnvido.calcular(compa.getMano()));
        } else {
            vista.mostrarMensaje("-------------------");
        }
    }

    private boolean gestionarFaseFlor() {
        boolean florEq1 = mesa.getEquipo1().tieneAlgunaFlor();
        boolean florEq2 = mesa.getEquipo2().tieneAlgunaFlor();
        int tantoEq1 = mesa.getEquipo1().getMejorTantoFlor();
        int tantoEq2 = mesa.getEquipo2().getMejorTantoFlor();

        if (!florEq1 && !florEq2) return false;

        vista.mostrarMensaje("\n--- FASE DE FLOR ---");
        if (florEq1 && !florEq2) {
            vista.mostrarAlerta(mesa.getEquipo1().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo1().sumarPuntos(3);
            return true;
        }
        if (!florEq1 && florEq2) {
            vista.mostrarAlerta(mesa.getEquipo2().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo2().sumarPuntos(3);
            return true;
        }

        vista.mostrarAlerta("¡Ambos equipos tienen Flor!");
        int resto = puntosAlResto();

        if (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1()) {
            vista.mostrarMensaje("Tu equipo canta: ¡FLOR!");
            resolverDobleFlor(tantoEq1, tantoEq2, resto, true);
        } else {
            vista.mostrarMensaje(mesa.getEquipo2().getNombre() + " canta: ¡FLOR!");
            resolverDobleFlor(tantoEq1, tantoEq2, resto, false);
        }
        return true;
    }

    private void resolverDobleFlor(int t1, int t2, int resto, boolean cantaEq1) {
        if (cantaEq1) {
            JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r1.cantarFrenteAFlor(t2) == 2) {
                vista.mostrarMensaje("Rival responde: ¡CONTRAFLOR AL RESTO!");
                int op = vista.pedirOpcion("¿Qué respondés?", Arrays.asList("[1] Con flor quiero", "[2] Con flor me achico"), 1, 2);
                if (op == 1) {
                    definirGanadorFlor(t1, t2, resto + 6);
                } else {
                    mesa.getEquipo2().sumarPuntos(4);
                }
            } else {
                vista.mostrarMensaje("Rival responde: ¡FLOR!");
                definirGanadorFlor(t1, t2, 6);
            }
        } else {
            int op = vista.pedirOpcion("¿Qué respondés?", Arrays.asList("[1] ¡Flor!", "[2] ¡Contraflor al resto!"), 1, 2);
            if (op == 1) {
                definirGanadorFlor(t1, t2, 6);
            } else {
                JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                if (r1.responderContraflorAlResto(t2) == 1) {
                    vista.mostrarMensaje("Rival responde: ¡CON FLOR QUIERO!");
                    definirGanadorFlor(t1, t2, resto + 6);
                } else {
                    vista.mostrarMensaje("Rival responde: ¡CON FLOR ME ACHICO!");
                    mesa.getEquipo1().sumarPuntos(4);
                }
            }
        }
    }

    private void definirGanadorFlor(int t1, int t2, int pts) {
        vista.mostrarMensaje("\n>> Resolución Flor: " + mesa.getEquipo1().getNombre() + " (" + t1 + ") vs " + 
                             mesa.getEquipo2().getNombre() + " (" + t2 + ")");
        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        if (t1 > t2 || (t1 == t2 && manoEsEq1)) {
            vista.mostrarAlerta("Gana la flor " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            vista.mostrarAlerta("Gana la flor " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private void gestionarRondas(boolean huboFlor) {
        int[] victorias = new int[3];
        int victEq1 = 0, victEq2 = 0;
        int turnoLanzador = mesa.getIndiceMano();
        boolean humanoTiroCarta = false;
        int totalJugadores = mesa.getTotalJugadores();

        if (huboFlor) gestorEnvido.marcarResuelto();

        for (int ronda = 1; ronda <= 3; ronda++) {
            vista.mostrarMensaje("\n-- RONDA " + ronda + " --");

            Carta mejorCartaRonda = null;
            int ganadorDeRonda = -1;

            for (int k = 0; k < totalJugadores; k++) {
                int actual = (turnoLanzador + k) % totalJugadores;
                Jugador jActual = mesa.getJugador(actual);

                if (ronda == 1 && !gestorEnvido.isResuelto()) {
                    if (totalJugadores == 2 && k == 0) {
                        if (actual == 0) {
                            int miTanto = mesa.getEquipo1().getMejorTantoEnvido();
                            int c = vista.pedirOpcion("Sos mano (tenés " + miTanto + " de envido). ¿Querés cantar Envido?",
                                    Arrays.asList("[1] Envido", "[2] Real Envido", "[3] Falta Envido", "[0] Paso"), 0, 3);
                            if (c > 0) {
                                resolverEnvidoIniciadoPorHumano(c, miTanto, mesa.getEquipo2().getMejorTantoEnvido());
                                gestorEnvido.marcarResuelto();
                                if (hayGanador()) return;
                            } else {
                                vista.mostrarMensaje("Paso.");
                            }
                        } else {
                            JugadorBot botRival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                            int t2 = mesa.getEquipo2().getMejorTantoEnvido();
                            int t1 = mesa.getEquipo1().getMejorTantoEnvido();
                            if (botRival.quiereAbrirEnvido(t2, true)) {
                                vista.mostrarMensaje("\n" + botRival.getNombre() + " (mano) canta: ¡ENVIDO!");
                                resolverEnvidoIniciadoPorRival(t1, t2);
                                gestorEnvido.marcarResuelto();
                                if (hayGanador()) return;
                            } else {
                                vista.mostrarMensaje(botRival.getNombre() + " (mano): Paso.");
                            }
                        }
                    } else if (totalJugadores == 2 && k == 1) {
                        if (actual == 0) {
                            int miTanto = mesa.getEquipo1().getMejorTantoEnvido();
                            int c = vista.pedirOpcion("El rival pasó (tenés " + miTanto + " de envido). ¿Querés cantar Envido de pie?",
                                    Arrays.asList("[1] Envido", "[2] Real Envido", "[3] Falta Envido", "[0] Paso"), 0, 3);
                            if (c > 0) {
                                resolverEnvidoIniciadoPorHumano(c, miTanto, mesa.getEquipo2().getMejorTantoEnvido());
                                if (hayGanador()) return;
                            } else {
                                vista.mostrarMensaje("Paso.");
                            }
                            gestorEnvido.marcarResuelto();
                        } else {
                            JugadorBot botRival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                            int t2 = mesa.getEquipo2().getMejorTantoEnvido();
                            int t1 = mesa.getEquipo1().getMejorTantoEnvido();
                            if (botRival.quiereAbrirEnvido(t2, false)) {
                                vista.mostrarMensaje("\n" + botRival.getNombre() + " canta de pie: ¡ENVIDO!");
                                resolverEnvidoIniciadoPorRival(t1, t2);
                                if (hayGanador()) return;
                            }
                            gestorEnvido.marcarResuelto();
                        }
                    } else if (totalJugadores == 4 && k == 2) {
                        gestionarEnvidoParejas(actual, (turnoLanzador + 3) % totalJugadores);
                        gestorEnvido.marcarResuelto();
                        if (hayGanador()) return;
                    }
                }

                if (jActual instanceof JugadorHumano) {
                    if (gestorTruco.puedeCantar(mesa.getEquipo1())) {
                        int c = vista.pedirOpcion("¿Deseás cantar " + gestorTruco.getProximoCanto() + " antes de tirar?",
                                Arrays.asList("[1] Sí", "[0] No"), 0, 1);
                        if (c == 1) {
                            if (!procesarCantoTrucoHumano(ronda)) return;
                            gestorEnvido.marcarResuelto();
                        }
                    }
                } else if (jActual instanceof JugadorBot botActual) {
                    Equipo eqBot = mesa.getEquipoDe(botActual);
                    int nivelNum = gestorTruco.getNivelActual().getPuntosQuerido();
                    if (gestorTruco.puedeCantar(eqBot) && botActual.quiereCantarTruco(nivelNum)) {
                        vista.mostrarMensaje("\n¡" + botActual.getNombre() + " canta " + gestorTruco.getProximoCanto() + "!");
                        if (!procesarCantoTrucoBot(eqBot, ronda)) return;
                        gestorEnvido.marcarResuelto();
                    }
                }

                Carta c;
                if (jActual instanceof JugadorHumano) {
                    c = jActual.jugarCarta();
                    if (c == null) {
                        irseAlMazo(humanoTiroCarta, mesa.getEquipo2());
                        return;
                    }
                    humanoTiroCarta = true;
                } else {
                    JugadorBot botActual = (JugadorBot) jActual;
                    c = botActual.jugarCartaInteligente(mejorCartaRonda, (k == 0));
                    vista.mostrarMensaje("  " + botActual.getNombre() + " tira: " + c);
                }

                if (mejorCartaRonda == null) {
                    mejorCartaRonda = c;
                    ganadorDeRonda = actual;
                } else {
                    int comp = ArbitroRonda.compararCartas(c, mejorCartaRonda);
                    if (comp == 1) {
                        mejorCartaRonda = c;
                        ganadorDeRonda = actual;
                    } else if (comp == 0) {
                        if (mesa.getEquipoDe(jActual) != mesa.getEquipoDe(mesa.getJugador(ganadorDeRonda))) {
                            ganadorDeRonda = -1;
                        }
                    }
                }
            }

            if (ronda == 1) gestorEnvido.marcarResuelto();

            if (ganadorDeRonda == -1) {
                vista.mostrarAlerta("Baza parda.");
                victorias[ronda - 1] = 0;
            } else {
                Equipo eqG = mesa.getEquipoDe(mesa.getJugador(ganadorDeRonda));
                vista.mostrarAlerta("Ronda para " + eqG.getNombre() + " (" + mesa.getJugador(ganadorDeRonda).getNombre() + ")");
                int cod = (eqG == mesa.getEquipo1()) ? 1 : 2;
                victorias[ronda - 1] = cod;
                if (cod == 1) victEq1++; else victEq2++;
                turnoLanzador = ganadorDeRonda;
            }

            if (victEq1 == 2 || victEq2 == 2) break;
            if (ronda == 2 && victorias[0] == 0 && (victEq1 == 1 || victEq2 == 1)) break;
        }

        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        int ganadorFinal = ArbitroRonda.definirGanadorMano(victorias, victEq1, victEq2, manoEsEq1 ? 1 : 2);
        int pts = gestorTruco.getPuntosEnJuego();

        if (ganadorFinal == 1) {
            vista.mostrarMensaje("\n>>> Ganó la mano " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            vista.mostrarMensaje("\n>>> Ganó la mano " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private void irseAlMazo(boolean tiroCarta, Equipo ganador) {
        int pts = gestorTruco.estaActivo() ? gestorTruco.getPuntosEnJuego() : (tiroCarta ? 1 : 2);
        vista.mostrarMensaje("\nTe fuiste al mazo.");
        vista.mostrarAlerta(ganador.getNombre() + " gana la mano (+" + pts + " pts).");
        ganador.sumarPuntos(pts);
    }

    private void gestionarEnvidoParejas(int penultimo, int ultimo) {
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
    }

    private void evaluarEnvidoParejas(int t1, int t2, boolean cantoEq1) {
        gestorEnvido.registrarCanto(1);
        if (cantoEq1) {
            JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r.responderEnvido(t2, 1, false) == 1) {
                vista.mostrarMensaje("Rivales responden: ¡QUIERO!");
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                vista.mostrarMensaje("Rivales responden: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        } else {
            int op = vista.pedirOpcion("El rival cantó Envido (tu equipo tiene " + t1 + " de envido). ¿Aceptás para tu equipo?",
                    Arrays.asList("[1] Quiero", "[0] No Quiero"), 0, 1);
            if (op == 1) {
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        }
    }

    private void resolverEnvidoIniciadoPorHumano(int tipo, int t1, int t2) {
        gestorEnvido.registrarCanto(tipo);
        JugadorBot rival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int resp = rival.responderEnvido(t2, tipo, mesa.getIndiceMano() != 0);

        boolean quiereTrucoBot = (!gestorTruco.estaActivo() && rival.quiereCantarTruco(1));

        if (resp == 1) {
            int pts = gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite);
            if (quiereTrucoBot) {
                vista.mostrarMensaje("Rival responde: ¡QUIERO Y TRUCO!");
                definirGanadorEnvido(t1, t2, pts);
                gestorEnvido.marcarResuelto();
                if (hayGanador()) return;
                procesarCantoTrucoBot(mesa.getEquipo2(), 1);
            } else {
                vista.mostrarMensaje("Rival responde: ¡QUIERO!");
                definirGanadorEnvido(t1, t2, pts);
                gestorEnvido.marcarResuelto();
            }
        } else if (resp == 2) {
            int ptsNo = gestorEnvido.getPuntosNoQuiero();
            if (quiereTrucoBot) {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO Y TRUCO!");
                mesa.getEquipo1().sumarPuntos(ptsNo);
                gestorEnvido.marcarResuelto();
                if (hayGanador()) return;
                procesarCantoTrucoBot(mesa.getEquipo2(), 1);
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
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
            gestorEnvido.marcarResuelto();
        }
    }

    private void resolverEnvidoIniciadoPorRival(int t1, int t2) {
        gestorEnvido.registrarCanto(1);
        int r = vista.pedirOpcion("¿Aceptás? (Tenés " + t1 + " de envido)",
                Arrays.asList("[1] Quiero", "[2] No Quiero", "[3] Real Envido", "[4] ¡Quiero y Truco!", "[5] ¡No Quiero y Truco!"), 1, 5);

        if (r == 1) {
            definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
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
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
            gestorEnvido.marcarResuelto();
        } else if (r == 4) {
            vista.mostrarMensaje("Cantaste: ¡QUIERO Y TRUCO!");
            definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return;
            vista.mostrarMensaje("\nAhora el rival responde a tu canto de TRUCO:");
            procesarCantoTrucoHumano(1);
        } else if (r == 5) {
            vista.mostrarMensaje("Cantaste: ¡NO QUIERO Y TRUCO!");
            mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return;
            vista.mostrarMensaje("\nAhora el rival responde a tu canto de TRUCO:");
            procesarCantoTrucoHumano(1);
        }
    }

    private void definirGanadorEnvido(int t1, int t2, int pts) {
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

    private boolean procesarCantoTrucoHumano(int ronda) {
        gestorTruco.proponerAumento(mesa.getEquipo1());
        JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();

        // El bot sólo puede decir "el envido está primero" si NO fue resuelto aún
        if (ronda == 1 && !gestorEnvido.isResuelto() && r.quiereAbrirEnvido(t2, false)) {
            vista.mostrarMensaje("Rival responde: ¡EL ENVIDO ESTÁ PRIMERO! Cantó: ¡ENVIDO!");
            resolverEnvidoIniciadoPorRival(t1, t2);
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return false;
            vista.mostrarMensaje("\nQuedó pendiente tu canto de " + gestorTruco.getProximoCanto() + "...");
        }

        int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;
        int resp = r.responderTruco(proximoNivelNum);

        if (resp == 1) {
            vista.mostrarMensaje("Rival responde: ¡QUIERO!");
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
            return true;
        } else if (resp == 2) {
            vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
            mesa.getEquipo1().sumarPuntos(gestorTruco.getPuntosRechazo());
            return false;
        } else {
            gestorTruco.aceptarAumento();
            vista.mostrarMensaje("Rival responde: ¡QUIERO Y " + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo2());
            int op = vista.pedirOpcion("¿Aceptás?", Arrays.asList("[1] Quiero", "[2] No Quiero"), 1, 2);
            if (op == 1) {
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
                return true;
            } else {
                mesa.getEquipo2().sumarPuntos(gestorTruco.getPuntosRechazo());
                return false;
            }
        }
    }

    private boolean procesarCantoTrucoBot(Equipo eqBot, int ronda) {
        gestorTruco.proponerAumento(eqBot);
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();

        boolean opcionEnvidoPrimero = (ronda == 1 && !gestorEnvido.isResuelto());
        boolean puedeSubir = (gestorTruco.getNivelActual() != GestorTruco.Nivel.RETRUCO && 
                              gestorTruco.getNivelActual() != GestorTruco.Nivel.VALE_CUATRO);

        List<String> opciones = new ArrayList<>();
        opciones.add("[1] Quiero");
        opciones.add("[2] No Quiero");
        if (puedeSubir) opciones.add("[3] " + gestorTruco.getProximoCanto());
        if (opcionEnvidoPrimero) opciones.add("[4] ¡El Envido está primero!");

        int maxOp = opcionEnvidoPrimero ? 4 : (puedeSubir ? 3 : 2);
        int r = vista.pedirOpcion("¿Qué respondés?", opciones, 1, maxOp);

        if (opcionEnvidoPrimero && r == 4) {
            vista.mostrarMensaje("Cantaste: ¡EL ENVIDO ESTÁ PRIMERO!");
            resolverEnvidoIniciadoPorHumano(1, t1, t2);
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return false;

            List<String> opsPost = new ArrayList<>(Arrays.asList("[1] Quiero", "[2] No Quiero"));
            if (puedeSubir) opsPost.add("[3] " + gestorTruco.getProximoCanto());
            r = vista.pedirOpcion("\nAhora debés responder al Truco del rival:", opsPost, 1, puedeSubir ? 3 : 2);
        }

        if (r == 1) {
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
            return true;
        } else if (r == 2) {
            eqBot.sumarPuntos(gestorTruco.getPuntosRechazo());
            return false;
        } else {
            gestorTruco.aceptarAumento();
            vista.mostrarMensaje("Cantaste: ¡" + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo1());
            JugadorBot rBot = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;

            if (rBot.responderTruco(proximoNivelNum) == 1) {
                vista.mostrarMensaje("Rival responde: ¡QUIERO!");
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
                return true;
            } else {
                vista.mostrarMensaje("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorTruco.getPuntosRechazo());
                return false;
            }
        }
    }

    private int puntosAlResto() {
        int lider = Math.max(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos());
        return puntajeLimite - lider;
    }

    private void mostrarTanteador() {
        vista.mostrarTanteador(mesa.getEquipo1().getNombre(), formatearPuntos(mesa.getEquipo1().getPuntos()),
                               mesa.getEquipo2().getNombre(), formatearPuntos(mesa.getEquipo2().getPuntos()),
                               puntajeLimite);
    }

    private String formatearPuntos(int pts) {
        if (puntajeLimite == 15) return pts + " pts";
        if (pts <= 15) return pts + " (Malas)";
        return (pts - 15) + " (Buenas) [Total: " + pts + "]";
    }
}