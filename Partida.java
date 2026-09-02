import java.util.Scanner;

public class Partida {
    private final Jugador humano;
    private final JugadorBot bot;
    private final Mazo mazo;
    private int puntosHumano;
    private int puntosBot;
    private final int puntajeLimite;
    private int manoTurno;
    private final Scanner scanner;

    // Estado del truco en la mano
    private int nivelTruco; // 1: sin cantar, 2: Truco, 3: Retruco, 4: Vale Cuatro
    private int puntosNoQueridoTruco;
    private int quienCantoTruco; // 1: Humano, 2: Bot, 0: nadie

    public Partida(int puntajeLimite, Scanner scanner) {
        this.puntajeLimite = puntajeLimite;
        this.scanner = scanner;
        this.humano = new JugadorHumano("Jugador", scanner);
        this.bot = new JugadorBot("Bot");
        this.mazo = new Mazo();
        this.puntosHumano = 0;
        this.puntosBot = 0;
    }

    public void iniciar() {
        System.out.println("==================================================");
        System.out.println("    TORNEO DE TRUCO ARGENTINO - PARTIDA A 15 PTS  ");
        System.out.println("==================================================");

        sorteoInicialRey();

        while (!hayGanador()) {
            jugarMano();
            manoTurno = (manoTurno == 1) ? 2 : 1;
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
        if (puntosHumano >= puntajeLimite) {
            System.out.println("¡GANASTE LA PARTIDA!");
        } else {
            System.out.println("EL BOT GANÓ LA PARTIDA.");
        }
        System.out.println("==================================================");
    }

    private void sorteoInicialRey() {
        System.out.println("\nSorteando dador y mano (primer 12)...");
        mazo.reiniciar();
        int t = 1;
        while (true) {
            Carta c = mazo.robar();
            String tirador = (t == 1) ? humano.getNombre() : bot.getNombre();
            System.out.println("  " + tirador + " saca: " + c);
            if (c.getNumero() == 12) {
                System.out.println("-> ¡" + tirador + " es el dador!");
                this.manoTurno = (t == 1) ? 2 : 1;
                System.out.println("-> Mano inicial: " + (manoTurno == 1 ? humano.getNombre() : bot.getNombre()));
                break;
            }
            t = (t == 1) ? 2 : 1;
        }
    }

    private boolean hayGanador() {
        return puntosHumano >= puntajeLimite || puntosBot >= puntajeLimite;
    }

    private void jugarMano() {
        System.out.println("\n--------------------------------------------------");
        System.out.println("NUEVA MANO - Mano: " + (manoTurno == 1 ? humano.getNombre() : bot.getNombre()));

        mazo.reiniciar();
        humano.limpiarMano();
        bot.limpiarMano();
        nivelTruco = 1;
        puntosNoQueridoTruco = 1;
        quienCantoTruco = 0;

        for (int i = 0; i < 3; i++) {
            humano.recibirCarta(mazo.robar());
            bot.recibirCarta(mazo.robar());
        }

        // 1. Fase de Envido (solo en primera ronda antes de jugar cartas de truco)
        gestionarFaseEnvido();
        if (hayGanador()) return;

        // 2. Rondas de Truco
        gestionarFaseTruco();
    }

    private void gestionarFaseEnvido() {
        int tantoH = CalculadorEnvido.calcular(humano.getMano());
        int tantoB = CalculadorEnvido.calcular(bot.getMano());

        System.out.println("\n--- FASE DE ENVIDO ---");
        System.out.println("Tus cartas: " + humano.getMano() + " | Tanto: " + tantoH);

        boolean seCanto = false;

        // Turno del mano para abrir el envido
        if (manoTurno == 1) {
            System.out.print("¿Querés cantar Envido? (1: Envido, 2: Real Envido, 3: Falta Envido, 0: Paso): ");
            int c = leerOpcion(0, 3);
            if (c > 0) {
                seCanto = true;
                resolverEnvidoIniciadoPorHumano(c, tantoH, tantoB);
            } else if (tantoB >= 26) {
                System.out.println("Bot canta: ¡ENVIDO!");
                seCanto = true;
                resolverEnvidoIniciadoPorBot(1, tantoH, tantoB);
            }
        } else {
            if (tantoB >= 26) {
                System.out.println("Bot canta: ¡ENVIDO!");
                seCanto = true;
                resolverEnvidoIniciadoPorBot(1, tantoH, tantoB);
            } else {
                System.out.print("El Bot pasa. ¿Querés cantar Envido? (1: Envido, 2: Real Envido, 3: Falta Envido, 0: Paso): ");
                int c = leerOpcion(0, 3);
                if (c > 0) {
                    seCanto = true;
                    resolverEnvidoIniciadoPorHumano(c, tantoH, tantoB);
                }
            }
        }

        if (!seCanto) {
            System.out.println("Nadie cantó envido.");
        }
    }

    private void resolverEnvidoIniciadoPorHumano(int tipo, int tantoH, int tantoB) {
        int ptsQuiero = (tipo == 1) ? 2 : (tipo == 2 ? 3 : puntosFalta());
        int ptsNoQuiero = 1;

        int respBot = bot.responderEnvido(tantoB, tipo);
        if (respBot == 1) {
            System.out.println("Bot responde: ¡QUIERO!");
            definirGanadorEnvido(tantoH, tantoB, ptsQuiero);
        } else if (respBot == 2) {
            System.out.println("Bot responde: ¡NO QUIERO!");
            puntosHumano += ptsNoQuiero;
        } else {
            System.out.println("Bot responde: ¡QUIERO Y RETRUCO EL ENVIDO (REAL ENVIDO)!");
            System.out.print("¿Aceptás? (1: Quiero, 2: No Quiero): ");
            int r = leerOpcion(1, 2);
            if (r == 1) {
                definirGanadorEnvido(tantoH, tantoB, ptsQuiero + 3);
            } else {
                puntosBot += ptsQuiero;
            }
        }
    }

    private void resolverEnvidoIniciadoPorBot(int tipo, int tantoH, int tantoB) {
        int ptsQuiero = (tipo == 1) ? 2 : (tipo == 2 ? 3 : puntosFalta());
        int ptsNoQuiero = 1;

        System.out.print("¿Aceptás? (1: Quiero, 2: No Quiero, 3: Real Envido): ");
        int r = leerOpcion(1, 3);
        if (r == 1) {
            definirGanadorEnvido(tantoH, tantoB, ptsQuiero);
        } else if (r == 2) {
            puntosBot += ptsNoQuiero;
        } else {
            System.out.println("Cantaste: ¡REAL ENVIDO!");
            int respBot = bot.responderEnvido(tantoB, 2);
            if (respBot == 1) {
                System.out.println("Bot responde: ¡QUIERO!");
                definirGanadorEnvido(tantoH, tantoB, ptsQuiero + 3);
            } else {
                System.out.println("Bot responde: ¡NO QUIERO!");
                puntosHumano += ptsQuiero;
            }
        }
    }

    private void definirGanadorEnvido(int tH, int tB, int pts) {
        System.out.println("\n>> Resolución: " + humano.getNombre() + " (" + tH + ") vs " + bot.getNombre() + " (" + tB + ")");
        if (tH > tB || (tH == tB && manoTurno == 1)) {
            System.out.println("-> " + humano.getNombre() + " gana el envido (+" + pts + " pts).");
            puntosHumano += pts;
        } else {
            System.out.println("-> " + bot.getNombre() + " gana el envido (+" + pts + " pts).");
            puntosBot += pts;
        }
    }

    private int puntosFalta() {
        int lider = Math.max(puntosHumano, puntosBot);
        return puntajeLimite - lider;
    }

    private void gestionarFaseTruco() {
        int[] resultados = new int[3];
        int victH = 0, victB = 0;
        int turno = manoTurno;

        for (int ronda = 1; ronda <= 3; ronda++) {
            System.out.println("\n-- RONDA " + ronda + " --");

            // Comprobación de cantos antes de tirar
            if (quienCantoTruco != 1 && nivelTruco < 4) {
                System.out.print("¿Deseás cantar " + siguienteCanto(nivelTruco) + "? (1: Sí, 0: No): ");
                if (leerOpcion(0, 1) == 1) {
                    boolean sigue = procesarCantoTrucoHumano();
                    if (!sigue) return;
                }
            }

            if (quienCantoTruco != 2 && nivelTruco < 4 && bot.quiereCantarTruco(nivelTruco)) {
                System.out.println("\n¡El Bot canta " + siguienteCanto(nivelTruco) + "!");
                boolean sigue = procesarRespuestaHumanoTruco();
                if (!sigue) return;
            }

            Carta cH, cB;
            if (turno == 1) {
                cH = humano.jugarCarta();
                cB = bot.jugarCarta();
                System.out.println(bot.getNombre() + " juega: " + cB);
            } else {
                cB = bot.jugarCarta();
                System.out.println(bot.getNombre() + " juega: " + cB);
                cH = humano.jugarCarta();
            }

            int res = ArbitroRonda.compararCartas(cH, cB);
            resultados[ronda - 1] = res;

            if (res == 1) {
                System.out.println("-> Ganás la ronda.");
                victH++;
                turno = 1;
            } else if (res == 2) {
                System.out.println("-> El bot gana la ronda.");
                victB++;
                turno = 2;
            } else {
                System.out.println("-> Parda.");
            }

            if (victH == 2 || victB == 2) break;
            if (ronda == 2 && resultados[0] == 0 && (victH == 1 || victB == 1)) break;
        }

        int ganador = ArbitroRonda.definirGanadorMano(resultados, victH, victB, manoTurno);
        int ptsGanados = (nivelTruco == 1) ? 1 : nivelTruco;

        if (ganador == 1) {
            System.out.println("\n>>> Ganaste la mano del truco (+" + ptsGanados + " pts).");
            puntosHumano += ptsGanados;
        } else {
            System.out.println("\n>>> El bot gana la mano del truco (+" + ptsGanados + " pts).");
            puntosBot += ptsGanados;
        }
    }

    private boolean procesarCantoTrucoHumano() {
        int proximoNivel = (nivelTruco == 1) ? 2 : nivelTruco + 1;
        quienCantoTruco = 1;

        int respBot = bot.responderTruco(proximoNivel);
        if (respBot == 1) {
            System.out.println("Bot responde: ¡QUIERO!");
            puntosNoQueridoTruco = (proximoNivel == 2) ? 1 : proximoNivel - 1;
            nivelTruco = proximoNivel;
            return true;
        } else if (respBot == 2) {
            System.out.println("Bot responde: ¡NO QUIERO!");
            puntosHumano += puntosNoQueridoTruco;
            return false;
        } else {
            int subeNivel = proximoNivel + 1;
            System.out.println("Bot responde: ¡QUIERO Y " + textoNivel(subeNivel) + "!");
            quienCantoTruco = 2;
            System.out.print("¿Aceptás? (1: Quiero, 2: No Quiero): ");
            if (leerOpcion(1, 2) == 1) {
                puntosNoQueridoTruco = proximoNivel;
                nivelTruco = subeNivel;
                return true;
            } else {
                puntosBot += proximoNivel;
                return false;
            }
        }
    }

    private boolean procesarRespuestaHumanoTruco() {
        int proximoNivel = (nivelTruco == 1) ? 2 : nivelTruco + 1;
        quienCantoTruco = 2;

        System.out.print("¿Qué respondés? (1: Quiero, 2: No Quiero" + (proximoNivel < 4 ? ", 3: " + textoNivel(proximoNivel + 1) : "") + "): ");
        int r = leerOpcion(1, proximoNivel < 4 ? 3 : 2);

        if (r == 1) {
            puntosNoQueridoTruco = (proximoNivel == 2) ? 1 : proximoNivel - 1;
            nivelTruco = proximoNivel;
            return true;
        } else if (r == 2) {
            puntosBot += puntosNoQueridoTruco;
            return false;
        } else {
            int subeNivel = proximoNivel + 1;
            quienCantoTruco = 1;
            int respBot = bot.responderTruco(subeNivel);
            if (respBot == 1) {
                System.out.println("Bot responde: ¡QUIERO!");
                puntosNoQueridoTruco = proximoNivel;
                nivelTruco = subeNivel;
                return true;
            } else {
                System.out.println("Bot responde: ¡NO QUIERO!");
                puntosHumano += proximoNivel;
                return false;
            }
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
        System.out.println("TANTEADOR (A 15 PUNTOS):");
        System.out.println("  " + humano.getNombre() + ": " + formatearPuntos(puntosHumano));
        System.out.println("  " + bot.getNombre() + ": " + formatearPuntos(puntosBot));
        System.out.println("==================================================");
    }

    private String formatearPuntos(int pts) {
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