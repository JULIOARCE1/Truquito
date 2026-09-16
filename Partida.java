import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Partida {
    private final Mesa mesa;
    private final Mazo mazo;
    private final int puntajeLimite;
    private final Scanner scanner;

    private final GestorTruco gestorTruco;
    private final GestorEnvido gestorEnvido;
    private Equipo equipoQueCantoTruco;

    public Partida(Mesa mesa, int puntajeLimite, Scanner scanner) {
        this.mesa = mesa;
        this.puntajeLimite = puntajeLimite;
        this.scanner = scanner;
        this.mazo = new Mazo();
        this.gestorTruco = new GestorTruco();
        this.gestorEnvido = new GestorEnvido();
    }

    public void iniciar() {
        System.out.println("\n==================================================");
        System.out.println("  INICIO DE PARTIDA A " + puntajeLimite + " PUNTOS (" + mesa.getTotalJugadores() + " JUGADORES)");
        System.out.println("  " + mesa.getEquipo1().getNombre() + " vs " + mesa.getEquipo2().getNombre());
        System.out.println("==================================================");

        sorteoInicialRey();

        while (!hayGanador()) {
            jugarMano();
            mesa.rotarMano();
            mostrarTanteador();

            if (!hayGanador()) {
                System.out.print("\nPresioná ENTER para la siguiente mano (o 's' para salir): ");
                String r = scanner.nextLine().trim().toLowerCase();
                if (r.equals("s") || r.equals("salir")) {
                    System.out.println("\nPartida cancelada.");
                    return;
                }
            }
        }

        System.out.println("\n==================================================");
        if (mesa.getEquipo1().getPuntos() >= puntajeLimite) {
            System.out.println("¡GANÓ " + mesa.getEquipo1().getNombre().toUpperCase() + "!");
        } else {
            System.out.println("¡GANÓ " + mesa.getEquipo2().getNombre().toUpperCase() + "!");
        }
        System.out.println("==================================================");
    }

    private void sorteoInicialRey() {
        System.out.println("\nSorteando dador y mano (primer 12)...");
        mazo.reiniciar();
        int t = 0;
        while (true) {
            Carta c = mazo.robar();
            Jugador actual = mesa.getJugador(t);
            System.out.println("  " + actual.getNombre() + " saca: " + c);
            if (c.getNumero() == 12) {
                System.out.println("-> ¡" + actual.getNombre() + " sacó el 12 (Dador)!");
                mesa.setIndiceMano((t + 1) % mesa.getTotalJugadores());
                System.out.println("-> Mano inicial: " + mesa.getJugador(mesa.getIndiceMano()).getNombre());
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
        System.out.println("\n--------------------------------------------------");
        if (mesa.getTotalJugadores() == 2) {
            System.out.println("NUEVA MANO - Es mano: " + mano.getNombre());
        } else {
            System.out.println("NUEVA MANO - Es mano: " + mano.getNombre() + " (" + mesa.getEquipoDe(mano).getNombre() + ")");
        }

        mazo.reiniciar();
        for (Jugador j : mesa.getAsientos()) {
            j.limpiarMano();
            for (int i = 0; i < 3; i++) {
                j.recibirCarta(mazo.robar());
            }
        }

        gestorTruco.reiniciar();
        gestorEnvido.reiniciar();
        equipoQueCantoTruco = null;

        mostrarEstadoEquipoHumano();

        boolean huboFlor = gestionarFaseFlor();
        if (hayGanador()) return;

        gestionarRondas(huboFlor);
    }

    private void mostrarEstadoEquipoHumano() {
        Jugador humano = mesa.getJugador(0);
        int tantoHumano = CalculadorEnvido.calcular(humano.getMano());
        System.out.println("\n--- TUS CARTAS ---");
        System.out.println("  " + humano.getMano() + " | Envido total: " + tantoHumano);
        if (mesa.getTotalJugadores() == 4) {
            Jugador compa = mesa.getJugador(2);
            System.out.println("  Tu Compañero (" + compa.getNombre() + "): " + compa.getMano() + 
                               " | Envido total: " + CalculadorEnvido.calcular(compa.getMano()));
        }
        System.out.println("-------------------");
    }

    private boolean gestionarFaseFlor() {
        boolean florEq1 = mesa.getEquipo1().tieneAlgunaFlor();
        boolean florEq2 = mesa.getEquipo2().tieneAlgunaFlor();
        int tantoEq1 = mesa.getEquipo1().getMejorTantoFlor();
        int tantoEq2 = mesa.getEquipo2().getMejorTantoFlor();

        if (!florEq1 && !florEq2) return false;

        System.out.println("\n--- FASE DE FLOR ---");
        if (florEq1 && !florEq2) {
            System.out.println("-> " + mesa.getEquipo1().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo1().sumarPuntos(3);
            return true;
        }
        if (!florEq1 && florEq2) {
            System.out.println("-> " + mesa.getEquipo2().getNombre() + " tiene Flor (+3 pts).");
            mesa.getEquipo2().sumarPuntos(3);
            return true;
        }

        System.out.println("-> ¡Ambos equipos tienen Flor!");
        int resto = puntosAlResto();

        if (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1()) {
            System.out.println("Tu equipo canta: ¡FLOR!");
            resolverDobleFlor(tantoEq1, tantoEq2, resto, true);
        } else {
            System.out.println(mesa.getEquipo2().getNombre() + " canta: ¡FLOR!");
            resolverDobleFlor(tantoEq1, tantoEq2, resto, false);
        }
        return true;
    }

    private void resolverDobleFlor(int t1, int t2, int resto, boolean cantaEq1) {
        if (cantaEq1) {
            JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r1.cantarFrenteAFlor(t2) == 2) {
                System.out.println("Rival responde: ¡CONTRAFLOR AL RESTO!");
                System.out.println("¿Qué respondés?");
                System.out.println("  [1] Con flor quiero");
                System.out.println("  [2] Con flor me achico");
                if (leerOpcion(1, 2) == 1) {
                    definirGanadorFlor(t1, t2, resto + 6);
                } else {
                    mesa.getEquipo2().sumarPuntos(4);
                }
            } else {
                System.out.println("Rival responde: ¡FLOR!");
                definirGanadorFlor(t1, t2, 6);
            }
        } else {
            System.out.println("¿Qué respondés?");
            System.out.println("  [1] ¡Flor!");
            System.out.println("  [2] ¡Contraflor al resto!");
            if (leerOpcion(1, 2) == 1) {
                definirGanadorFlor(t1, t2, 6);
            } else {
                JugadorBot r1 = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                if (r1.responderContraflorAlResto(t2) == 1) {
                    System.out.println("Rival responde: ¡CON FLOR QUIERO!");
                    definirGanadorFlor(t1, t2, resto + 6);
                } else {
                    System.out.println("Rival responde: ¡CON FLOR ME ACHICO!");
                    mesa.getEquipo1().sumarPuntos(4);
                }
            }
        }
    }

    private void definirGanadorFlor(int t1, int t2, int pts) {
        System.out.println("\n>> Resolución Flor: " + mesa.getEquipo1().getNombre() + " (" + t1 + ") vs " + 
                           mesa.getEquipo2().getNombre() + " (" + t2 + ")");
        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        if (t1 > t2 || (t1 == t2 && manoEsEq1)) {
            System.out.println("-> Gana la flor " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            System.out.println("-> Gana la flor " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
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
            System.out.println("\n-- RONDA " + ronda + " --");

            Carta mejorCartaRonda = null;
            int ganadorDeRonda = -1;

            for (int k = 0; k < totalJugadores; k++) {
                int actual = (turnoLanzador + k) % totalJugadores;
                Jugador jActual = mesa.getJugador(actual);

                // --- 1. APERTURA DE ENVIDO EN RONDA 1 ---
                if (ronda == 1 && !gestorEnvido.isResuelto()) {
                    if (totalJugadores == 2 && k == 0) {
                        if (actual == 0) {
                            int miTanto = mesa.getEquipo1().getMejorTantoEnvido();
                            System.out.println("Sos mano (tenés " + miTanto + " de envido). ¿Querés cantar Envido?");
                            System.out.println("  [1] Envido");
                            System.out.println("  [2] Real Envido");
                            System.out.println("  [3] Falta Envido");
                            System.out.println("  [0] Paso");
                            int c = leerOpcion(0, 3);
                            if (c > 0) {
                                resolverEnvidoIniciadoPorHumano(c, miTanto, mesa.getEquipo2().getMejorTantoEnvido());
                                gestorEnvido.marcarResuelto();
                                if (hayGanador()) return;
                            } else {
                                System.out.println("Paso.");
                            }
                        } else {
                            JugadorBot botRival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                            int t2 = mesa.getEquipo2().getMejorTantoEnvido();
                            int t1 = mesa.getEquipo1().getMejorTantoEnvido();
                            if (botRival.quiereAbrirEnvido(t2, true)) {
                                System.out.println("\n" + botRival.getNombre() + " (mano) canta: ¡ENVIDO!");
                                resolverEnvidoIniciadoPorRival(t1, t2);
                                gestorEnvido.marcarResuelto();
                                if (hayGanador()) return;
                            } else {
                                System.out.println(botRival.getNombre() + " (mano): Paso.");
                            }
                        }
                    } else if (totalJugadores == 2 && k == 1) {
                        if (actual == 0) {
                            int miTanto = mesa.getEquipo1().getMejorTantoEnvido();
                            System.out.println("El rival pasó (tenés " + miTanto + " de envido). ¿Querés cantar Envido de pie?");
                            System.out.println("  [1] Envido");
                            System.out.println("  [2] Real Envido");
                            System.out.println("  [3] Falta Envido");
                            System.out.println("  [0] Paso");
                            int c = leerOpcion(0, 3);
                            if (c > 0) {
                                resolverEnvidoIniciadoPorHumano(c, miTanto, mesa.getEquipo2().getMejorTantoEnvido());
                                if (hayGanador()) return;
                            } else {
                                System.out.println("Paso.");
                            }
                            gestorEnvido.marcarResuelto();
                        } else {
                            JugadorBot botRival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
                            int t2 = mesa.getEquipo2().getMejorTantoEnvido();
                            int t1 = mesa.getEquipo1().getMejorTantoEnvido();
                            if (botRival.quiereAbrirEnvido(t2, false)) {
                                System.out.println("\n" + botRival.getNombre() + " canta de pie: ¡ENVIDO!");
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

                // --- 2. CANTO DE TRUCO (EN EL TURNO DE CADA UNO) ---
                if (jActual instanceof JugadorHumano) {
                    if (gestorTruco.puedeCantar(mesa.getEquipo1())) {
                        System.out.println("¿Deseás cantar " + gestorTruco.getProximoCanto() + " antes de tirar?");
                        System.out.println("  [1] Sí");
                        System.out.println("  [0] No");
                        if (leerOpcion(0, 1) == 1) {
                            if (!procesarCantoTrucoHumano(ronda)) return;
                            gestorEnvido.marcarResuelto();
                        }
                    }
                } else if (jActual instanceof JugadorBot botActual) {
                    Equipo eqBot = mesa.getEquipoDe(botActual);
                    int nivelNum = gestorTruco.getNivelActual().getPuntosQuerido();
                    if (gestorTruco.puedeCantar(eqBot) && botActual.quiereCantarTruco(nivelNum)) {
                        System.out.println("\n¡" + botActual.getNombre() + " canta " + gestorTruco.getProximoCanto() + "!");
                        if (!procesarCantoTrucoBot(eqBot, ronda)) return;
                        gestorEnvido.marcarResuelto();
                    }
                }

                // --- 3. TIRADA DE CARTA ---
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
                    System.out.println("  " + botActual.getNombre() + " tira: " + c);
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
                System.out.println("-> Baza parda.");
                victorias[ronda - 1] = 0;
            } else {
                Equipo eqG = mesa.getEquipoDe(mesa.getJugador(ganadorDeRonda));
                System.out.println("-> Ronda para " + eqG.getNombre() + " (" + mesa.getJugador(ganadorDeRonda).getNombre() + ")");
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
            System.out.println("\n>>> Ganó la mano " + mesa.getEquipo1().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            System.out.println("\n>>> Ganó la mano " + mesa.getEquipo2().getNombre() + " (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private void irseAlMazo(boolean tiroCarta, Equipo ganador) {
        int pts = gestorTruco.estaActivo() ? gestorTruco.getPuntosEnJuego() : (tiroCarta ? 1 : 2);
        System.out.println("\nTe fuiste al mazo.");
        System.out.println("-> " + ganador.getNombre() + " gana la mano (+" + pts + " pts).");
        ganador.sumarPuntos(pts);
    }

    private void gestionarEnvidoParejas(int penultimo, int ultimo) {
        System.out.println("\n--- TURNO DE ENVIDO (ÚLTIMOS 2 JUGADORES) ---");
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        boolean canto = false;

        Jugador jPen = mesa.getJugador(penultimo);
        Jugador jUlt = mesa.getJugador(ultimo);

        if (jPen instanceof JugadorHumano) {
            System.out.println("Sos el penúltimo (tu equipo tiene " + t1 + " de envido). ¿Deseás cantar Envido?");
            System.out.println("  [1] Sí");
            System.out.println("  [0] No");
            if (leerOpcion(0, 1) == 1) {
                canto = true;
                evaluarEnvidoParejas(t1, t2, true);
            }
        } else if (jPen instanceof JugadorBot bot) {
            if (bot.quiereAbrirEnvido(CalculadorEnvido.calcular(bot.getMano()), false)) {
                System.out.println(bot.getNombre() + " canta: ¡ENVIDO!");
                canto = true;
                evaluarEnvidoParejas(t1, t2, mesa.getEquipoDe(bot) == mesa.getEquipo1());
            }
        }

        if (!canto) {
            if (jUlt instanceof JugadorHumano) {
                System.out.println("Sos el pie (tu equipo tiene " + t1 + " de envido). ¿Deseás cantar Envido?");
                System.out.println("  [1] Sí");
                System.out.println("  [0] No");
                if (leerOpcion(0, 1) == 1) {
                    evaluarEnvidoParejas(t1, t2, true);
                } else {
                    System.out.println("Los últimos dos pasaron sin cantar Envido.");
                }
            } else if (jUlt instanceof JugadorBot bot) {
                if (bot.quiereAbrirEnvido(CalculadorEnvido.calcular(bot.getMano()), false)) {
                    System.out.println(bot.getNombre() + " canta: ¡ENVIDO!");
                    evaluarEnvidoParejas(t1, t2, mesa.getEquipoDe(bot) == mesa.getEquipo1());
                } else {
                    System.out.println("Los últimos dos pasaron sin cantar Envido.");
                }
            }
        }
    }

    private void evaluarEnvidoParejas(int t1, int t2, boolean cantoEq1) {
        gestorEnvido.registrarCanto(1);
        if (cantoEq1) {
            JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (r.responderEnvido(t2, 1, false) == 1) {
                System.out.println("Rivales responden: ¡QUIERO!");
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                System.out.println("Rivales responden: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        } else {
            System.out.println("El rival cantó Envido (tu equipo tiene " + t1 + " de envido). ¿Aceptás para tu equipo?");
            System.out.println("  [1] Quiero");
            System.out.println("  [0] No Quiero");
            if (leerOpcion(0, 1) == 1) {
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
                System.out.println("Rival responde: ¡QUIERO Y TRUCO!");
                definirGanadorEnvido(t1, t2, pts);
                if (hayGanador()) return;
                procesarCantoTrucoBot(mesa.getEquipo2(), 1);
            } else {
                System.out.println("Rival responde: ¡QUIERO!");
                definirGanadorEnvido(t1, t2, pts);
            }
        } else if (resp == 2) {
            int ptsNo = gestorEnvido.getPuntosNoQuiero();
            if (quiereTrucoBot) {
                System.out.println("Rival responde: ¡NO QUIERO Y TRUCO!");
                mesa.getEquipo1().sumarPuntos(ptsNo);
                if (hayGanador()) return;
                procesarCantoTrucoBot(mesa.getEquipo2(), 1);
            } else {
                System.out.println("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(ptsNo);
            }
        } else {
            System.out.println("Rival responde: ¡QUIERO Y REAL ENVIDO!");
            gestorEnvido.registrarCanto(2);
            System.out.println("¿Aceptás? (Tenés " + t1 + " de envido)");
            System.out.println("  [1] Quiero");
            System.out.println("  [2] No Quiero");
            if (leerOpcion(1, 2) == 1) {
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        }
    }

    private void resolverEnvidoIniciadoPorRival(int t1, int t2) {
        gestorEnvido.registrarCanto(1);
        System.out.println("¿Aceptás? (Tenés " + t1 + " de envido)");
        System.out.println("  [1] Quiero");
        System.out.println("  [2] No Quiero");
        System.out.println("  [3] Real Envido");
        System.out.println("  [4] ¡Quiero y Truco!");
        System.out.println("  [5] ¡No Quiero y Truco!");
        int r = leerOpcion(1, 5);

        if (r == 1) {
            definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
        } else if (r == 2) {
            mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
        } else if (r == 3) {
            System.out.println("Cantaste: ¡REAL ENVIDO!");
            gestorEnvido.registrarCanto(2);
            JugadorBot rival = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            if (rival.responderEnvido(t2, 2, false) == 1) {
                System.out.println("Rival responde: ¡QUIERO!");
                definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            } else {
                System.out.println("Rival responde: ¡NO QUIERO!");
                mesa.getEquipo1().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            }
        } else if (r == 4) {
            System.out.println("Cantaste: ¡QUIERO Y TRUCO!");
            definirGanadorEnvido(t1, t2, gestorEnvido.calcularPuntosQueridos(mesa.getEquipo1().getPuntos(), mesa.getEquipo2().getPuntos(), puntajeLimite));
            if (hayGanador()) return;
            System.out.println("\nAhora el rival responde a tu canto de TRUCO:");
            procesarCantoTrucoHumano(1);
        } else if (r == 5) {
            System.out.println("Cantaste: ¡NO QUIERO Y TRUCO!");
            mesa.getEquipo2().sumarPuntos(gestorEnvido.getPuntosNoQuiero());
            if (hayGanador()) return;
            System.out.println("\nAhora el rival responde a tu canto de TRUCO:");
            procesarCantoTrucoHumano(1);
        }
    }

    private void definirGanadorEnvido(int t1, int t2, int pts) {
        System.out.println("\n>> Resolución Envido: " + mesa.getEquipo1().getNombre() + " (" + t1 + ") vs " + 
                           mesa.getEquipo2().getNombre() + " (" + t2 + ")");
        boolean manoEsEq1 = (mesa.getEquipoDe(mesa.getJugador(mesa.getIndiceMano())) == mesa.getEquipo1());
        if (t1 > t2 || (t1 == t2 && manoEsEq1)) {
            System.out.println("-> " + mesa.getEquipo1().getNombre() + " gana el envido (+" + pts + " pts).");
            mesa.getEquipo1().sumarPuntos(pts);
        } else {
            System.out.println("-> " + mesa.getEquipo2().getNombre() + " gana el envido (+" + pts + " pts).");
            mesa.getEquipo2().sumarPuntos(pts);
        }
    }

    private boolean procesarCantoTrucoHumano(int ronda) {
        gestorTruco.proponerAumento(mesa.getEquipo1());
        JugadorBot r = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
        int t2 = mesa.getEquipo2().getMejorTantoEnvido();
        int t1 = mesa.getEquipo1().getMejorTantoEnvido();

        if (ronda == 1 && !gestorEnvido.isResuelto() && r.quiereAbrirEnvido(t2, false)) {
            System.out.println("Rival responde: ¡EL ENVIDO ESTÁ PRIMERO! Cantó: ¡ENVIDO!");
            resolverEnvidoIniciadoPorRival(t1, t2);
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return false;
            System.out.println("\nQuedó pendiente tu canto de " + gestorTruco.getProximoCanto() + "...");
        }

        int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;
        int resp = r.responderTruco(proximoNivelNum);

        if (resp == 1) {
            System.out.println("Rival responde: ¡QUIERO!");
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
            return true;
        } else if (resp == 2) {
            System.out.println("Rival responde: ¡NO QUIERO!");
            mesa.getEquipo1().sumarPuntos(gestorTruco.getPuntosRechazo());
            return false;
        } else {
            gestorTruco.aceptarAumento(); // Se consolida el nivel previo
            System.out.println("Rival responde: ¡QUIERO Y " + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo2());
            System.out.println("¿Aceptás?");
            System.out.println("  [1] Quiero");
            System.out.println("  [2] No Quiero");
            if (leerOpcion(1, 2) == 1) {
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

        System.out.println("¿Qué respondés?");
        System.out.println("  [1] Quiero");
        System.out.println("  [2] No Quiero");
        if (puedeSubir) System.out.println("  [3] " + gestorTruco.getProximoCanto());
        if (opcionEnvidoPrimero) System.out.println("  [4] ¡El Envido está primero!");

        int maxOp = opcionEnvidoPrimero ? 4 : (puedeSubir ? 3 : 2);
        int r = leerOpcion(1, maxOp);

        if (opcionEnvidoPrimero && r == 4) {
            System.out.println("Cantaste: ¡EL ENVIDO ESTÁ PRIMERO!");
            resolverEnvidoIniciadoPorHumano(1, t1, t2);
            gestorEnvido.marcarResuelto();
            if (hayGanador()) return false;

            System.out.println("\nAhora debés responder al Truco del rival:");
            System.out.println("  [1] Quiero");
            System.out.println("  [2] No Quiero");
            if (puedeSubir) System.out.println("  [3] " + gestorTruco.getProximoCanto());
            r = leerOpcion(1, puedeSubir ? 3 : 2);
        }

        if (r == 1) {
            gestorTruco.aceptarAumento();
            gestorTruco.setEquipoConElQuiero(mesa.getEquipo1());
            return true;
        } else if (r == 2) {
            eqBot.sumarPuntos(gestorTruco.getPuntosRechazo());
            return false;
        } else {
            gestorTruco.aceptarAumento(); // Se consolida
            System.out.println("Cantaste: ¡" + gestorTruco.getProximoCanto() + "!");
            gestorTruco.proponerAumento(mesa.getEquipo1());
            JugadorBot rBot = (JugadorBot) mesa.getEquipo2().getIntegrantes().get(0);
            int proximoNivelNum = gestorTruco.getNivelActual().getPuntosQuerido() + 1;

            if (rBot.responderTruco(proximoNivelNum) == 1) {
                System.out.println("Rival responde: ¡QUIERO!");
                gestorTruco.aceptarAumento();
                gestorTruco.setEquipoConElQuiero(mesa.getEquipo2());
                return true;
            } else {
                System.out.println("Rival responde: ¡NO QUIERO!");
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
        System.out.println("\n==================================================");
        System.out.println("TANTEADOR (A " + puntajeLimite + " PUNTOS):");
        System.out.println("  " + mesa.getEquipo1().getNombre() + ": " + formatearPuntos(mesa.getEquipo1().getPuntos()));
        System.out.println("  " + mesa.getEquipo2().getNombre() + ": " + formatearPuntos(mesa.getEquipo2().getPuntos()));
        System.out.println("==================================================");
    }

    private String formatearPuntos(int pts) {
        if (puntajeLimite == 15) return pts + " pts";
        if (pts <= 15) return pts + " (Malas)";
        return (pts - 15) + " (Buenas) [Total: " + pts + "]";
    }

    private int leerOpcion(int min, int max) {
        while (true) {
            System.out.print("-> Elegí una opción: ");
            if (scanner.hasNextInt()) {
                int op = scanner.nextInt();
                scanner.nextLine();
                if (op >= min && op <= max) return op;
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida (" + min + "-" + max + ").");
        }
    }
}