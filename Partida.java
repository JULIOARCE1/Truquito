import java.util.Scanner;

public class Partida {
    private final Jugador humano;
    private final Jugador bot;
    private final Mazo mazo;
    private int puntosHumano;
    private int puntosBot;
    private final int puntajeLimite; // 30 puntos oficiales
    private int manoTurno; // 1: Humano, 2: Bot
    private final Scanner scanner;

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
        System.out.println("    TORNEO DE TRUCO ARGENTINO - PARTIDA A 30 PTS  ");
        System.out.println("==================================================");

        sorteoInicialRey();

        while (!hayGanador()) {
            jugarMano();
            manoTurno = (manoTurno == 1) ? 2 : 1;
            mostrarTanteador();

            if (!hayGanador()) {
                System.out.print("\nPresioná ENTER para la siguiente mano (o escribí 's' para salir): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                if (respuesta.equals("s") || respuesta.equals("salir")) {
                    System.out.println("\nPartida cancelada.");
                    return;
                }
            }
        }

        System.out.println("\n==================================================");
        if (puntosHumano >= puntajeLimite) {
            System.out.println("¡GANASTE LA PARTIDA DE 30 PUNTOS!");
        } else {
            System.out.println("EL BOT GANÓ LA PARTIDA DE 30 PUNTOS.");
        }
        System.out.println("==================================================");
    }

    // Regla 5º: Sorteo de Rey (12) para definir dador y mano
    private void sorteoInicialRey() {
        System.out.println("\nSorteando dador y mano (primero en sacar un 12)...");
        mazo.reiniciar();
        int turno = 1;
        while (true) {
            Carta c = mazo.robar();
            String tirador = (turno == 1) ? humano.getNombre() : bot.getNombre();
            System.out.println("  " + tirador + " saca: " + c);

            if (c.getNumero() == 12) {
                System.out.println("\n-> ¡" + tirador + " sacó Rey (12) y es el dador!");
                // El que empieza (mano) es el contrario
                this.manoTurno = (turno == 1) ? 2 : 1;
                System.out.println("-> Mano de la primera ronda: " + (this.manoTurno == 1 ? humano.getNombre() : bot.getNombre()));
                break;
            }
            turno = (turno == 1) ? 2 : 1;
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

        for (int i = 0; i < 3; i++) {
            humano.recibirCarta(mazo.robar());
            bot.recibirCarta(mazo.robar());
        }

        // 1. Fase de Envido
        resolverFaseEnvido();
        if (hayGanador()) return;

        // 2. Fase de Truco (Cartas)
        resolverFaseTruco();
    }

    private void resolverFaseEnvido() {
        int tantoHumano = CalculadorEnvido.calcular(humano.getMano());
        int tantoBot = CalculadorEnvido.calcular(bot.getMano());

        System.out.println("\n--- FASE DE ENVIDO ---");
        System.out.println("Tus cartas para evaluar: " + humano.getMano());
        System.out.println("Tu tanto calculado: " + tantoHumano);

        int puntosEnJuego = 0;
        int ganadorEnvido = 0;

        if (manoTurno == 1) {
            System.out.print("¿Querés cantar Envido? (1: Envido, 2: Real Envido, 3: Falta Envido, 0: Paso): ");
            int canto = leerOpcion(0, 3);
            if (canto > 0) {
                puntosEnJuego = procesarCantoHumano(canto, tantoBot);
            }
        } else {
            // El bot evalúa si canta
            if (tantoBot >= 27) {
                System.out.println("Bot canta: ¡ENVIDO!");
                puntosEnJuego = responderEnvidoUsuario(2, 1);
            }
        }

        if (puntosEnJuego > 0) {
            System.out.println("\nResolución del Envido:");
            System.out.println("  " + humano.getNombre() + " canta: " + tantoHumano);
            System.out.println("  " + bot.getNombre() + " canta: " + tantoBot);

            if (tantoHumano > tantoBot || (tantoHumano == tantoBot && manoTurno == 1)) {
                System.out.println("-> " + humano.getNombre() + " gana el envido (+" + puntosEnJuego + " pts).");
                puntosHumano += puntosEnJuego;
            } else {
                System.out.println("-> " + bot.getNombre() + " gana el envido (+" + puntosEnJuego + " pts).");
                puntosBot += puntosEnJuego;
            }
        }
    }

    private int procesarCantoHumano(int tipo, int tantoBot) {
        int maxLider = Math.max(puntosHumano, puntosBot);
        int puntosFalta = puntajeLimite - maxLider;

        if (tipo == 1) { // Envido
            if (tantoBot >= 26) {
                System.out.println("Bot responde: ¡QUIERO!");
                return 2;
            } else {
                System.out.println("Bot responde: ¡NO QUIERO!");
                puntosHumano += 1;
                return 0;
            }
        } else if (tipo == 2) { // Real Envido
            if (tantoBot >= 29) {
                System.out.println("Bot responde: ¡QUIERO!");
                return 3;
            } else {
                System.out.println("Bot responde: ¡NO QUIERO!");
                puntosHumano += 1;
                return 0;
            }
        } else { // Falta Envido
            if (tantoBot >= 31) {
                System.out.println("Bot responde: ¡QUIERO!");
                return puntosFalta;
            } else {
                System.out.println("Bot responde: ¡NO QUIERO!");
                puntosHumano += 1;
                return 0;
            }
        }
    }

    private int responderEnvidoUsuario(int ptsSiQuiero, int ptsSiNoQuiero) {
        System.out.print("¿Aceptás el envido? (1: Quiero, 2: No Quiero): ");
        int resp = leerOpcion(1, 2);
        if (resp == 1) {
            return ptsSiQuiero;
        } else {
            puntosBot += ptsSiNoQuiero;
            return 0;
        }
    }

    private void resolverFaseTruco() {
        int valorTruco = 1; // Por defecto sin cantar vale 1 pt
        int[] resultados = new int[3];
        int victoriasH = 0;
        int victoriasB = 0;
        int turnoActual = manoTurno;

        for (int ronda = 1; ronda <= 3; ronda++) {
            System.out.println("\n-- RONDA " + ronda + " --");
            Carta cartaH, cartaB;

            if (turnoActual == 1) {
                cartaH = humano.jugarCarta();
                cartaB = bot.jugarCarta();
                System.out.println(bot.getNombre() + " juega: " + cartaB);
            } else {
                cartaB = bot.jugarCarta();
                System.out.println(bot.getNombre() + " juega: " + cartaB);
                cartaH = humano.jugarCarta();
            }

            int res = ArbitroRonda.compararCartas(cartaH, cartaB);
            resultados[ronda - 1] = res;

            if (res == 1) {
                System.out.println("-> Ganás la ronda.");
                victoriasH++;
                turnoActual = 1;
            } else if (res == 2) {
                System.out.println("-> El bot gana la ronda.");
                victoriasB++;
                turnoActual = 2;
            } else {
                System.out.println("-> Parda.");
            }

            if (victoriasH == 2 || victoriasB == 2) break;
            if (ronda == 2 && resultados[0] == 0 && (victoriasH == 1 || victoriasB == 1)) break;
        }

        int ganador = ArbitroRonda.definirGanadorMano(resultados, victoriasH, victoriasB, manoTurno);
        if (ganador == 1) {
            System.out.println("\n>>> Ganaste la mano del truco (+" + valorTruco + " pt).");
            puntosHumano += valorTruco;
        } else {
            System.out.println("\n>>> El bot gana la mano del truco (+" + valorTruco + " pt).");
            puntosBot += valorTruco;
        }
    }

    private void mostrarTanteador() {
        System.out.println("\n==================================================");
        System.out.println("TANTEADOR (A 30 PUNTOS):");
        System.out.println("  " + humano.getNombre() + ": " + formatearPuntos(puntosHumano));
        System.out.println("  " + bot.getNombre() + ": " + formatearPuntos(puntosBot));
        System.out.println("==================================================");
    }

    private String formatearPuntos(int pts) {
        if (pts <= 15) {
            return pts + " (Malas)";
        } else {
            return (pts - 15) + " (Buenas) [Total: " + pts + "]";
        }
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