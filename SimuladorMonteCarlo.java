import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimuladorMonteCarlo {
    private static final Random random = new Random();

    // Calcula la probabilidad de ganar la mano (0.0 a 1.0) si el bot juega una carta específica
    public static double estimarProbabilidadVictoria(
            Carta cartaAJugar,
            List<Carta> manoRestanteBot,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano
    ) {
        int iteraciones = 250; // Muestras Monte Carlo
        int victorias = 0;

        List<Carta> mazoRestante = obtenerCartasDesconocidas(manoRestanteBot, cartaAJugar, cartasVisiblesMesa);

        for (int i = 0; i < iteraciones; i++) {
            if (simularRollout(cartaAJugar, manoRestanteBot, mazoRestante, rondaActual, victoriasPrevias, botEsMano)) {
                victorias++;
            }
        }

        return (double) victorias / iteraciones;
    }

    // Calcula la probabilidad de ganar la mano en general con la mano actual
    public static double estimarProbabilidadGlobal(
            List<Carta> manoBot,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano
    ) {
        if (manoBot.isEmpty()) return 0.0;
        double maxP = 0.0;
        for (Carta c : manoBot) {
            List<Carta> resto = new ArrayList<>(manoBot);
            resto.remove(c);
            double p = estimarProbabilidadVictoria(c, resto, cartasVisiblesMesa, rondaActual, victoriasPrevias, botEsMano);
            if (p > maxP) maxP = p;
        }
        return maxP;
    }

    private static boolean simularRollout(
            Carta cartaJugada,
            List<Carta> manoRestanteBot,
            List<Carta> mazoDesconocido,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano
    ) {
        // Barajamos cartas desconocidas para este escenario hipotético
        List<Carta> pool = new ArrayList<>(mazoDesconocido);
        Collections.shuffle(pool, random);

        int cartasRivalNecesarias = 4 - rondaActual; // En ronda 1 necesita 3, en ronda 2 necesita 2...
        if (pool.size() < cartasRivalNecesarias) return false;

        List<Carta> manoRival = new ArrayList<>();
        for (int j = 0; j < cartasRivalNecesarias; j++) {
            manoRival.add(pool.get(j));
        }

        List<Carta> miManoVirtual = new ArrayList<>(manoRestanteBot);
        int[] vics = new int[3];
        vics[0] = victoriasPrevias[0];
        vics[1] = victoriasPrevias[1];
        vics[2] = victoriasPrevias[2];

        int victEq1 = 0, victEq2 = 0;
        for (int r = 0; r < rondaActual - 1; r++) {
            if (vics[r] == 1) victEq1++;
            else if (vics[r] == 2) victEq2++;
        }

        // Simular ronda actual
        manoRival.sort((a, b) -> Integer.compare(b.getJerarquiaTruco(), a.getJerarquiaTruco()));
        Carta cartaRival = manoRival.remove(0); // El rival virtual juega su mejor respuesta

        int comp = ArbitroRonda.compararCartas(cartaJugada, cartaRival);
        int ganadorRonda = (comp == 1) ? 2 : (comp == 2 ? 1 : 0); // 2 = Bot (Eq2), 1 = Humano (Eq1)
        vics[rondaActual - 1] = ganadorRonda;
        if (ganadorRonda == 2) victEq2++;
        else if (ganadorRonda == 1) victEq1++;

        if (victEq2 == 2) return true;
        if (victEq1 == 2) return false;

        // Simular rondas restantes
        for (int r = rondaActual; r < 3; r++) {
            if (miManoVirtual.isEmpty() || manoRival.isEmpty()) break;
            miManoVirtual.sort((a, b) -> Integer.compare(b.getJerarquiaTruco(), a.getJerarquiaTruco()));
            Carta miC = miManoVirtual.remove(0);
            Carta rivC = manoRival.remove(0);

            int res = ArbitroRonda.compararCartas(miC, rivC);
            int g = (res == 1) ? 2 : (res == 2 ? 1 : 0);
            vics[r] = g;
            if (g == 2) victEq2++;
            else if (g == 1) victEq1++;

            if (victEq2 == 2) return true;
            if (victEq1 == 2) return false;
            if (r == 1 && vics[0] == 0 && (victEq2 == 1 || victEq1 == 1)) {
                return victEq2 == 1;
            }
        }

        int ganadorFinal = ArbitroRonda.definirGanadorMano(vics, victEq1, victEq2, botEsMano ? 2 : 1);
        return ganadorFinal == 2;
    }

    private static List<Carta> obtenerCartasDesconocidas(List<Carta> mano, Carta jugada, List<Carta> visibles) {
        List<Carta> barajaTotal = new ArrayList<>();
        for (Palo p : Palo.values()) {
            for (int n = 1; n <= 12; n++) {
                if (n == 8 || n == 9) continue;
                barajaTotal.add(new Carta(n, p));
            }
        }

        List<Carta> excluidas = new ArrayList<>(mano);
        if (jugada != null) excluidas.add(jugada);
        if (visibles != null) excluidas.addAll(visibles);

        List<Carta> desconocidas = new ArrayList<>();
        for (Carta c : barajaTotal) {
            boolean estaExcluida = false;
            for (Carta exc : excluidas) {
                if (exc.getNumero() == c.getNumero() && exc.getPalo() == c.getPalo()) {
                    estaExcluida = true;
                    break;
                }
            }
            if (!estaExcluida) {
                desconocidas.add(c);
            }
        }
        return desconocidas;
    }
}