import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PartidaParejas {
    private final Jugador[] jugadores;
    private final Mazo mazo;
    private int puntosEquipo1; // Humano (0) + Bot Compañero (2)
    private int puntosEquipo2; // Bot Rival 1 (1) + Bot Rival 2 (3)
    private final int puntajeLimite;
    private int manoTurno;
    private final Scanner scanner;

    private int nivelTruco;
    private int puntosNoQueridoTruco;
    private int equipoQueCantoTruco;

    public PartidaParejas(int puntajeLimite, Scanner scanner) {
        this.puntajeLimite = puntajeLimite;
        this.scanner = scanner;
        this.mazo = new Mazo();
        this.puntosEquipo1 = 0;
        this.puntosEquipo2 = 0;
        this.manoTurno = 0;

        jugadores = new Jugador[4];
        jugadores[0] = new JugadorHumano("Tú", scanner);
        jugadores[1] = new JugadorBot("Rival Este");
        jugadores[2] = new JugadorBot("Tu Compañero");
        jugadores[3] = new JugadorBot("Rival Oeste");
    }

    public void iniciar() {
        System.out.println("\n==================================================");
        System.out.println("  MODALIDAD 2 VS 2 - PARTIDA A " + puntajeLimite + " PUNTOS");
        System.out.println("  Equipo 1: Tú y Tu Compañero");
        System.out.println("  Equipo 2: Rival Este y Rival Oeste");
        System.out.println("  Regla: Compañero con iniciativa para cantar y responder");
        System.out.println("==================================================");

        sorteoInicialRey();

        while (!hayGanador()) {
            jugarMano();
            manoTurno = (manoTurno + 1) % 4;
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
        if (puntosEquipo1 >= puntajeLimite) {
            System.out.println("¡GANASTE LA PARTIDA JUNTO A TU COMPAÑERO!");
        } else {
            System.out.println("EL EQUIPO RIVAL GANÓ LA PARTIDA.");
        }
        System.out.println("==================================================");
    }

    private void sorteoInicialRey() {
        System.out.println("\nSorteando dador y mano (primer 12)...");
        mazo.reiniciar();
        int t = 0;
        while (true) {
            Carta c = mazo.robar();
            System.out.println("  " + jugadores[t].getNombre() + " saca: " + c);
            if (c.getNumero() == 12) {
                System.out.println("-> " + jugadores[t].getNombre() + " sacó el 12 (Dador).");
                this.manoTurno = (t + 1) % 4;
                System.out.println("-> Mano inicial: " + jugadores[manoTurno].getNombre());
                break;
            }
            t = (t + 1) % 4;
        }
    }

    private boolean hayGanador() {
        return puntosEquipo1 >= puntajeLimite || puntosEquipo2 >= puntajeLimite;
    }

    private int getEquipo(int jugadorIndex) {
        return (jugadorIndex == 0 || jugadorIndex == 2) ? 1 : 2;
    }

    private void jugarMano() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("NUEVA MANO - Es mano: " + jugadores[manoTurno].getNombre());

        mazo.reiniciar();
        for (Jugador j : jugadores) {
            j.limpiarMano();
            for (int i = 0; i < 3; i++) {
                j.recibirCarta(mazo.robar());
            }
        }

        nivelTruco = 1;
        puntosNoQueridoTruco = 1;
        equipoQueCantoTruco = 0;

        mostrarCartasEquipo();

        boolean huboFlor = gestionarFaseFlor();
        if (hayGanador()) return;

        gestionarRondas(huboFlor);
    }

    private void mostrarCartasEquipo() {
        System.out.println("\n--- CARTAS DE TU EQUIPO ---");
        System.out.println("  Tus cartas:        " + jugadores[0].getMano());
        System.out.println("  Tu Compañero (" + CalculadorEnvido.calcular(jugadores[2].getMano()) + " de tanto): " + jugadores[2].getMano());
        System.out.println("---------------------------");
    }

    private boolean gestionarFaseFlor() {
        int mejorFlorEq1 = 0, mejorFlorEq2 = 0;
        for (int i = 0; i < 4; i++) {
            if (CalculadorFlor.tieneFlor(jugadores[i].getMano())) {
                int tanto = CalculadorFlor.calcularTantoFlor(jugadores[i].getMano());
                if (getEquipo(i) == 1) mejorFlorEq1 = Math.max(mejorFlorEq1, tanto);
                else mejorFlorEq2 = Math.max(mejorFlorEq2, tanto);
            }
        }

        if (mejorFlorEq1 == 0 && mejorFlorEq2 == 0) return false;

        System.out.println("\n--- FASE DE FLOR ---");
        if (mejorFlorEq1 > 0 && mejorFlorEq2 == 0) {
            System.out.println("-> Tu equipo tiene Flor (+3 pts).");
            puntosEquipo1 += 3;
        } else if (mejorFlorEq2 > 0 && mejorFlorEq1 == 0) {
            System.out.println("-> Los rivales cantan Flor (+3 pts).");
            puntosEquipo2 += 3;
        } else {
            System.out.println("-> Ambos equipos tienen Flor (+6 pts).");
            if (mejorFlorEq1 >= mejorFlorEq2) {
                System.out.println("-> Tu equipo gana la Flor (" + mejorFlorEq1 + " vs " + mejorFlorEq2 + ").");
                puntosEquipo1 += 6;
            } else {
                System.out.println("-> El equipo rival gana la Flor (" + mejorFlorEq2 + " vs " + mejorFlorEq1 + ").");
                puntosEquipo2 += 6;
            }
        }
        return true;
    }

    private void gestionarRondas(boolean huboFlor) {
        int[] victorias = new int[3];
        int victEq1 = 0, victEq2 = 0;
        int turnoLanzador = manoTurno;
        boolean envidoJugado = huboFlor;
        boolean usuarioTiroCarta = false;

        for (int ronda = 1; ronda <= 3; ronda++) {
            System.out.println("\n-- RONDA " + ronda + " --");
            System.out.println("Cartas restantes de tu compañero: " + jugadores[2].getMano());

            // Opción del usuario de cantar truco
            if (equipoQueCantoTruco != 1 && nivelTruco < 4) {
                System.out.print("¿Deseás cantar " + siguienteCanto(nivelTruco) + "? (1: Sí, 0: No): ");
                if (leerOpcion(0, 1) == 1) {
                    boolean sigue = procesarCantoTrucoEquipo1();
                    if (!sigue) return;
                }
            }

            Carta mejorCartaRonda = null;
            int ganadorDeRonda = -1;

            for (int k = 0; k < 4; k++) {
                int actual = (turnoLanzador + k) % 4;

                // Turno de envido restringido a los últimos dos en la ronda 1
                if (ronda == 1 && !envidoJugado && k == 2) {
                    int jugador3 = actual;
                    int jugador4 = (turnoLanzador + 3) % 4;
                    resolverEnvidoUltimosDos(jugador3, jugador4);
                    envidoJugado = true;
                    if (hayGanador()) return;
                }

                // Iniciativa de Truco de los bots antes de tirar
                if (actual == 2 && equipoQueCantoTruco != 1 && nivelTruco < 4) {
                    JugadorBot compa = (JugadorBot) jugadores[2];
                    if (compa.quiereCantarTruco(nivelTruco)) {
                        System.out.println("\n¡Tu Compañero toma la iniciativa y canta " + siguienteCanto(nivelTruco) + "!");
                        boolean sigue = procesarCantoTrucoEquipo1();
                        if (!sigue) return;
                    }
                } else if ((actual == 1 || actual == 3) && equipoQueCantoTruco != 2 && nivelTruco < 4) {
                    JugadorBot rival = (JugadorBot) jugadores[actual];
                    if (rival.quiereCantarTruco(nivelTruco)) {
                        System.out.println("\n¡" + rival.getNombre() + " canta " + siguienteCanto(nivelTruco) + "!");
                        boolean sigue = procesarRespuestaTrucoRivales(actual);
                        if (!sigue) return;
                    }
                }

                Carta c;
                if (actual == 0) {
                    c = jugadores[0].jugarCarta();
                    if (c == null) {
                        int pts = (nivelTruco > 1) ? nivelTruco : (usuarioTiroCarta ? 1 : 2);
                        System.out.println("\nTu equipo se va al mazo.");
                        System.out.println("-> El equipo rival gana la mano (+" + pts + " pts).");
                        puntosEquipo2 += pts;
                        return;
                    }
                    usuarioTiroCarta = true;
                } else {
                    JugadorBot botActual = (JugadorBot) jugadores[actual];
                    c = botActual.jugarCartaInteligente(mejorCartaRonda, (k == 0));
                }

                System.out.println("  " + jugadores[actual].getNombre() + " tira: " + c);

                if (mejorCartaRonda == null) {
                    mejorCartaRonda = c;
                    ganadorDeRonda = actual;
                } else {
                    int comp = ArbitroRonda.compararCartas(c, mejorCartaRonda);
                    if (comp == 1) {
                        mejorCartaRonda = c;
                        ganadorDeRonda = actual;
                    } else if (comp == 0) {
                        if (getEquipo(actual) != getEquipo(ganadorDeRonda)) {
                            ganadorDeRonda = -1;
                        }
                    }
                }
            }

            if (ganadorDeRonda == -1) {
                System.out.println("-> Baza parda.");
                victorias[ronda - 1] = 0;
            } else {
                int eqGanador = getEquipo(ganadorDeRonda);
                System.out.println("-> Ronda para el Equipo " + eqGanador + " (" + jugadores[ganadorDeRonda].getNombre() + ")");
                victorias[ronda - 1] = eqGanador;
                if (eqGanador == 1) victEq1++;
                else victEq2++;
                turnoLanzador = ganadorDeRonda;
            }

            if (victEq1 == 2 || victEq2 == 2) break;
            if (ronda == 2 && victorias[0] == 0 && (victEq1 == 1 || victEq2 == 1)) break;
        }

        int eqMano = getEquipo(manoTurno);
        int ganadorFinal = ArbitroRonda.definirGanadorMano(victorias, victEq1, victEq2, eqMano);
        int pts = (nivelTruco == 1) ? 1 : nivelTruco;

        if (ganadorFinal == 1) {
            System.out.println("\n>>> Tu equipo gana la mano del Truco (+" + pts + " pts).");
            puntosEquipo1 += pts;
        } else {
            System.out.println("\n>>> El equipo rival gana la mano del Truco (+" + pts + " pts).");
            puntosEquipo2 += pts;
        }
    }

    private boolean procesarCantoTrucoEquipo1() {
        int proximoNivel = (nivelTruco == 1) ? 2 : nivelTruco + 1;
        equipoQueCantoTruco = 1;
        JugadorBot r1 = (JugadorBot) jugadores[1];

        int resp = r1.responderTruco(proximoNivel);
        if (resp == 1) {
            System.out.println("Rivales responden: ¡QUIERO!");
            puntosNoQueridoTruco = (proximoNivel == 2) ? 1 : proximoNivel - 1;
            nivelTruco = proximoNivel;
            return true;
        } else if (resp == 2) {
            System.out.println("Rivales responden: ¡NO QUIERO!");
            puntosEquipo1 += puntosNoQueridoTruco;
            return false;
        } else {
            int sube = proximoNivel + 1;
            System.out.println("Rivales responden: ¡QUIERO Y " + textoNivel(sube) + "!");
            equipoQueCantoTruco = 2;
            System.out.print("¿Aceptan para su equipo? (1: Quiero, 2: No Quiero): ");
            if (leerOpcion(1, 2) == 1) {
                puntosNoQueridoTruco = proximoNivel;
                nivelTruco = sube;
                return true;
            } else {
                puntosEquipo2 += proximoNivel;
                return false;
            }
        }
    }

    private boolean procesarRespuestaTrucoRivales(int quienCanto) {
        int proximoNivel = (nivelTruco == 1) ? 2 : nivelTruco + 1;
        equipoQueCantoTruco = 2;

        // Si es el turno inmediato de tu compañero, él puede responder o darte la decisión
        JugadorBot compa = (JugadorBot) jugadores[2];
        int respCompa = compa.responderTruco(proximoNivel);

        if (respCompa == 3 && proximoNivel < 4) {
            System.out.println("Tu Compañero se adelanta y grita: ¡QUIERO Y " + textoNivel(proximoNivel + 1) + "!");
            equipoQueCantoTruco = 1;
            JugadorBot rival = (JugadorBot) jugadores[quienCanto];
            if (rival.responderTruco(proximoNivel + 1) == 1) {
                System.out.println("Rivales responden: ¡QUIERO!");
                puntosNoQueridoTruco = proximoNivel;
                nivelTruco = proximoNivel + 1;
                return true;
            } else {
                System.out.println("Rivales responden: ¡NO QUIERO!");
                puntosEquipo1 += proximoNivel;
                return false;
            }
        }

        System.out.print("¿Qué responden? (1: Quiero, 2: No Quiero" + (proximoNivel < 4 ? ", 3: " + textoNivel(proximoNivel + 1) : "") + "): ");
        int r = leerOpcion(1, proximoNivel < 4 ? 3 : 2);

        if (r == 1) {
            puntosNoQueridoTruco = (proximoNivel == 2) ? 1 : proximoNivel - 1;
            nivelTruco = proximoNivel;
            return true;
        } else if (r == 2) {
            puntosEquipo2 += puntosNoQueridoTruco;
            return false;
        } else {
            int sube = proximoNivel + 1;
            equipoQueCantoTruco = 1;
            JugadorBot rival = (JugadorBot) jugadores[quienCanto];
            if (rival.responderTruco(sube) == 1) {
                System.out.println("Rivales responden: ¡QUIERO!");
                puntosNoQueridoTruco = proximoNivel;
                nivelTruco = sube;
                return true;
            } else {
                System.out.println("Rivales responden: ¡NO QUIERO!");
                puntosEquipo1 += proximoNivel;
                return false;
            }
        }
    }

    private void resolverEnvidoUltimosDos(int penultimo, int ultimo) {
        System.out.println("\n--- TURNO DE ENVIDO (SOLO LOS ÚLTIMOS 2) ---");
        System.out.println("Habilitados para cantar: " + jugadores[penultimo].getNombre() + " y " + jugadores[ultimo].getNombre());

        int tantoCompa = CalculadorEnvido.calcular(jugadores[2].getMano());
        int e1 = Math.max(CalculadorEnvido.calcular(jugadores[0].getMano()), tantoCompa);
        int e2 = Math.max(CalculadorEnvido.calcular(jugadores[1].getMano()), CalculadorEnvido.calcular(jugadores[3].getMano()));

        boolean canto = false;

        // Si el penúltimo sos vos
        if (penultimo == 0) {
            System.out.print("Sos el penúltimo. ¿Deseás cantar Envido? (1: Sí, 0: No): ");
            if (leerOpcion(0, 1) == 1) {
                canto = true;
                evaluarRespuestaEnvido(e1, e2, true);
            }
        } else if (penultimo == 2) {
            // El penúltimo es tu compañero
            JugadorBot compa = (JugadorBot) jugadores[2];
            if (compa.quiereAbrirEnvido(tantoCompa, false)) {
                System.out.println("¡Tu Compañero canta: ENVIDO!");
                canto = true;
                evaluarRespuestaEnvido(e1, e2, true);
            }
        } else if (getEquipo(penultimo) == 2) {
            JugadorBot rival = (JugadorBot) jugadores[penultimo];
            int tantoRival = CalculadorEnvido.calcular(rival.getMano());
            if (rival.quiereAbrirEnvido(tantoRival, false)) {
                System.out.println(rival.getNombre() + " canta: ¡ENVIDO!");
                canto = true;
                evaluarRespuestaEnvido(e1, e2, false);
            }
        }

        // Si el penúltimo pasó, decide el último (el pie)
        if (!canto) {
            if (ultimo == 0) {
                System.out.print("Sos el pie (último). ¿Deseás cantar Envido? (1: Sí, 0: No): ");
                if (leerOpcion(0, 1) == 1) {
                    evaluarRespuestaEnvido(e1, e2, true);
                } else {
                    System.out.println("Los últimos dos pasaron sin cantar Envido.");
                }
            } else if (ultimo == 2) {
                JugadorBot compa = (JugadorBot) jugadores[2];
                if (compa.quiereAbrirEnvido(tantoCompa, false)) {
                    System.out.println("¡Tu Compañero (pie) canta: ENVIDO!");
                    evaluarRespuestaEnvido(e1, e2, true);
                } else {
                    System.out.println("Los últimos dos pasaron sin cantar Envido.");
                }
            } else if (getEquipo(ultimo) == 2) {
                JugadorBot rival = (JugadorBot) jugadores[ultimo];
                int tantoRival = CalculadorEnvido.calcular(rival.getMano());
                if (rival.quiereAbrirEnvido(tantoRival, false)) {
                    System.out.println(rival.getNombre() + " (pie) canta: ¡ENVIDO!");
                    evaluarRespuestaEnvido(e1, e2, false);
                } else {
                    System.out.println("Los últimos dos pasaron sin cantar Envido.");
                }
            }
        }
    }

    private void evaluarRespuestaEnvido(int e1, int e2, boolean cantoEquipo1) {
        if (cantoEquipo1) {
            JugadorBot rival = (JugadorBot) jugadores[1];
            if (rival.responderEnvido(e2, 1, false) == 1) {
                System.out.println("Equipo Rival responde: ¡QUIERO!");
                definirGanadorEnvido(e1, e2);
            } else {
                System.out.println("Equipo Rival responde: ¡NO QUIERO!");
                puntosEquipo1 += 1;
            }
        } else {
            System.out.print("El rival cantó Envido. ¿Aceptás para tu equipo? (1: Quiero, 0: No Quiero): ");
            if (leerOpcion(0, 1) == 1) {
                definirGanadorEnvido(e1, e2);
            } else {
                puntosEquipo2 += 1;
            }
        }
    }

    private void definirGanadorEnvido(int e1, int e2) {
        System.out.println("Resolución Envido: Tu Equipo (" + e1 + ") vs Equipo Rival (" + e2 + ")");
        if (e1 > e2 || (e1 == e2 && getEquipo(manoTurno) == 1)) {
            System.out.println("-> Tu equipo gana el envido (+2 pts).");
            puntosEquipo1 += 2;
        } else {
            System.out.println("-> El equipo rival gana el envido (+2 pts).");
            puntosEquipo2 += 2;
        }
    }

    private String siguienteCanto(int nivel) {
        if (nivel == 1) return "TRUCO";
        if (nivel == 2) return "RETRUCO";
        return "VALE CUATRO";
    }

    private String textoNivel(int nivel) {
        if (nivel == 2) return "TRUCO";
        if (nivel == 3) return "RETRUCO";
        return "VALE CUATRO";
    }

    private void mostrarTanteador() {
        System.out.println("\n==================================================");
        System.out.println("TANTEADOR (A " + puntajeLimite + " PUNTOS):");
        System.out.println("  Tu Equipo: " + formatearPuntos(puntosEquipo1));
        System.out.println("  Equipo Rival: " + formatearPuntos(puntosEquipo2));
        System.out.println("==================================================");
    }

    private String formatearPuntos(int pts) {
        if (puntajeLimite == 15) return pts + " pts";
        if (pts <= 15) return pts + " (Malas)";
        return (pts - 15) + " (Buenas) [Total: " + pts + "]";
    }

    private int leerOpcion(int min, int max) {
        while (true) {
            if (scanner.hasNextInt()) {
                int op = scanner.nextInt();
                scanner.nextLine();
                if (op >= min && op <= max) return op;
            } else {
                scanner.nextLine();
            }
            System.out.print("Opción inválida (" + min + "-" + max + "): ");
        }
    }
}