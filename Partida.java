import java.util.Scanner;

public class Partida {
    private final Jugador humano;
    private final Jugador bot;
    private final Mazo mazo;
    private int puntosHumano;
    private int puntosBot;
    private final int puntajeLimite;
    private int manoTurno;
    private final Scanner scanner;

    public Partida(int puntajeLimite, Scanner scanner) {
        this.puntajeLimite = puntajeLimite;
        this.scanner = scanner;
        this.humano = new JugadorHumano("Jugador", scanner);
        this.bot = new JugadorBot("Bot");
        this.mazo = new Mazo();
        this.puntosHumano = 0;
        this.puntosBot = 0;
        this.manoTurno = 1;
    }

    public void iniciar() {
        System.out.println("=== INICIO DE PARTIDA A " + puntajeLimite + " PUNTOS ===");

        while (!hayGanador()) {
            jugarMano();
            manoTurno = (manoTurno == 1) ? 2 : 1;
            mostrarTanteador();

            if (!hayGanador()) {
                System.out.print("\nPresioná ENTER para la siguiente mano (o escribí 's' para salir): ");
                String respuesta = scanner.nextLine().trim().toLowerCase();
                if (respuesta.equals("s") || respuesta.equals("salir")) {
                    System.out.println("\nPartida finalizada por el usuario.");
                    return;
                }
            }
        }

        // Anuncio final
        System.out.println("\n===========================================");
        if (puntosHumano >= puntajeLimite) {
            System.out.println("¡FELICITACIONES! LLEGASTE A " + puntosHumano + " PUNTOS Y GANASTE LA PARTIDA.");
        } else {
            System.out.println("EL BOT LLEGÓ A " + puntosBot + " PUNTOS Y GANÓ LA PARTIDA.");
        }
        System.out.println("===========================================");
    }

    private boolean hayGanador() {
        return puntosHumano >= puntajeLimite || puntosBot >= puntajeLimite;
    }

    private void jugarMano() {
        System.out.println("\n-------------------------------------------");
        System.out.println("NUEVA MANO - Mano: " + (manoTurno == 1 ? humano.getNombre() : bot.getNombre()));

        mazo.reiniciar();
        humano.limpiarMano();
        bot.limpiarMano();

        for (int i = 0; i < 3; i++) {
            humano.recibirCarta(mazo.robar());
            bot.recibirCarta(mazo.robar());
        }

        // 1. Fase de Envido
        int envidoHumano = CalculadorEnvido.calcular(humano.getMano());
        int envidoBot = CalculadorEnvido.calcular(bot.getMano());
        System.out.println("[Tanto Envido] " + humano.getNombre() + ": " + envidoHumano + " | " + bot.getNombre() + ": " + envidoBot);

        if (envidoHumano > envidoBot || (envidoHumano == envidoBot && manoTurno == 1)) {
            System.out.println("-> " + humano.getNombre() + " gana el envido (+1 pt).");
            puntosHumano += 1;
        } else {
            System.out.println("-> " + bot.getNombre() + " gana el envido (+1 pt).");
            puntosBot += 1;
        }

        // Si alguien llegó a 15 con el envido, se corta la mano de inmediato
        if (hayGanador()) {
            System.out.println("\n¡Partida definida en el envido!");
            return;
        }

        // 2. Rondas de cartas (Truco)
        int[] resultados = new int[3];
        int victoriasH = 0;
        int victoriasB = 0;
        int turnoActual = manoTurno;

        for (int ronda = 1; ronda <= 3; ronda++) {
            System.out.println("\n-- Ronda " + ronda + " --");
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
                System.out.println("Ganás la ronda.");
                victoriasH++;
                turnoActual = 1;
            } else if (res == 2) {
                System.out.println("El bot gana la ronda.");
                victoriasB++;
                turnoActual = 2;
            } else {
                System.out.println("Parda.");
            }

            if (victoriasH == 2 || victoriasB == 2) break;
            if (ronda == 2 && resultados[0] == 0 && (victoriasH == 1 || victoriasB == 1)) break;
        }

        int ganador = ArbitroRonda.definirGanadorMano(resultados, victoriasH, victoriasB);
        if (ganador == 1) {
            System.out.println("\n>>> Ganaste el truco (+1 pt).");
            puntosHumano += 1;
        } else {
            System.out.println("\n>>> El bot gana el truco (+1 pt).");
            puntosBot += 1;
        }
    }

    private void mostrarTanteador() {
        System.out.println("\n[TABLERO] " + humano.getNombre() + ": " + puntosHumano + " pts | " + bot.getNombre() + ": " + puntosBot + " pts");
    }
}