import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimuladorMonteCarlo {
    private static final Random random = new Random();

    public static double estimarProbabilidadVictoria(
            Carta cartaAJugar,
            List<Carta> manoRestanteBot,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano,
            int tantoConocidoRival,
            int iteraciones
    ) {
        int victorias = 0;
        List<Carta> mazoRestante = obtenerCartasDesconocidas(manoRestanteBot, cartaAJugar, cartasVisiblesMesa);

        for (int i = 0; i < iteraciones; i++) {
            if (simularRollout(cartaAJugar, manoRestanteBot, mazoRestante, cartasVisiblesMesa, rondaActual, victoriasPrevias, botEsMano, tantoConocidoRival)) {
                victorias++;
            }
        }

        return (double) victorias / iteraciones;
    }

    public static double estimarProbabilidadGlobal(
            List<Carta> manoBot,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano,
            int tantoConocidoRival,
            int iteraciones
    ) {
        if (manoBot.isEmpty()) return 0.0;
        double maxP = 0.0;
        for (Carta c : manoBot) {
            List<Carta> resto = new ArrayList<>(manoBot);
            resto.remove(c);
            double p = estimarProbabilidadVictoria(c, resto, cartasVisiblesMesa, rondaActual, victoriasPrevias, botEsMano, tantoConocidoRival, iteraciones);
            if (p > maxP) maxP = p;
        }
        return maxP;
    }

    private static boolean simularRollout(
            Carta cartaJugada,
            List<Carta> manoRestanteBot,
            List<Carta> mazoDesconocido,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano,
            int tantoConocidoRival
    ) {
        int cartasRivalNecesarias = 4 - rondaActual;
        List<Carta> manoRival = generarManoRivalBayesiana(mazoDesconocido, cartasVisiblesMesa, cartasRivalNecesarias, tantoConocidoRival);
        if (manoRival.size() < cartasRivalNecesarias) return false;

        List<Carta> miManoVirtual = new ArrayList<>(manoRestanteBot);
        int[] vics = new int[]{victoriasPrevias[0], victoriasPrevias[1], victoriasPrevias[2]};

        int victEq1 = 0, victEq2 = 0;
        for (int r = 0; r < rondaActual - 1; r++) {
            if (vics[r] == 1) victEq1++;
            else if (vics[r] == 2) victEq2++;
        }

        // Ronda actual: el rival juega su mejor carta frente a la nuestra
        manoRival.sort((a, b) -> Integer.compare(b.getJerarquiaTruco(), a.getJerarquiaTruco()));
        Carta cartaRival = manoRival.remove(0);

        int comp = ArbitroRonda.compararCartas(cartaJugada, cartaRival);
        int ganadorRonda = (comp == 1) ? 2 : (comp == 2 ? 1 : 0); // 2 = Bot, 1 = Rival
        vics[rondaActual - 1] = ganadorRonda;
        if (ganadorRonda == 2) victEq2++;
        else if (ganadorRonda == 1) victEq1++;

        if (victEq2 == 2) return true;
        if (victEq1 == 2) return false;

        // Rondas restantes
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

    // Inferencia Bayesiana: Restringe el universo muestral de la mano oculta según el tanto
    private static List<Carta> generarManoRivalBayesiana(
            List<Carta> mazoDesconocido,
            List<Carta> cartasVisibles,
            int cantidadNecesaria,
            int tantoConocido
    ) {
        List<Carta> pool = new ArrayList<>(mazoDesconocido);
        Collections.shuffle(pool, random);
        List<Carta> mano = new ArrayList<>();

        if (tantoConocido >= 20 && cantidadNecesaria >= 1) {
            int sumaFiguras = tantoConocido - 20;

            // Hipótesis 1: ¿El rival ya tiró una carta visible del palo del tanto?
            Carta cartaVisibleCoincidente = null;
            if (cartasVisibles != null) {
                for (Carta cv : cartasVisibles) {
                    int val = cv.getValorEnvido();
                    int valorBuscado = sumaFiguras - val;
                    if (valorBuscado >= 0 && valorBuscado <= 7) {
                        for (Carta c : pool) {
                            if (c.getPalo() == cv.getPalo() && c.getValorEnvido() == valorBuscado) {
                                cartaVisibleCoincidente = c;
                                break;
                            }
                        }
                    }
                    if (cartaVisibleCoincidente != null) break;
                }
            }

            if (cartaVisibleCoincidente != null) {
                mano.add(cartaVisibleCoincidente);
                pool.remove(cartaVisibleCoincidente);
            } else if (cantidadNecesaria >= 2) {
                // Hipótesis 2: Muestrear un par de cartas en mano que sumen exactamente el tanto
                Carta c1 = null, c2 = null;
                for (int i = 0; i < pool.size(); i++) {
                    for (int j = i + 1; j < pool.size(); j++) {
                        Carta a = pool.get(i);
                        Carta b = pool.get(j);
                        if (a.getPalo() == b.getPalo() && (a.getValorEnvido() + b.getValorEnvido() == sumaFiguras)) {
                            c1 = a;
                            c2 = b;
                            break;
                        }
                    }
                    if (c1 != null) break;
                }
                if (c1 != null) {
                    mano.add(c1);
                    mano.add(c2);
                    pool.remove(c1);
                    pool.remove(c2);
                }
            }
        }

        // Rellenar las cartas restantes con el pool no restringido
        while (mano.size() < cantidadNecesaria && !pool.isEmpty()) {
            mano.add(pool.remove(0));
        }

        return mano;
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
            if (!estaExcluida) desconocidas.add(c);
        }
        return desconocidas;
    }
}