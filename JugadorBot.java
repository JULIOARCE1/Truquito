import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class JugadorBot extends Jugador {
    private final Random random = new Random();
    private SenaTruco senaCompanero = SenaTruco.NINGUNA;

    public JugadorBot(String nombre) {
        super(nombre);
    }

    public void recibirSenaCompanero(SenaTruco sena) {
        this.senaCompanero = (sena != null) ? sena : SenaTruco.NINGUNA;
    }

    public SenaTruco emitirSena() {
        return SenaTruco.determinarMejorSena(getMano());
    }

    @Override
    public Carta jugarCarta() {
        return jugarCartaInteligente(null, true);
    }

    public Carta jugarCartaInteligente(
            Carta cartaMesa,
            boolean esPrimeraDeRonda,
            boolean ganaCompanero,
            List<Carta> cartasVisiblesMesa,
            int rondaActual,
            int[] victoriasPrevias,
            boolean botEsMano
    ) {
        List<Carta> mano = getMano();
        if (mano.isEmpty()) return null;
        if (mano.size() == 1) return mano.remove(0);

        if (ganaCompanero) {
            mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
            return mano.remove(0);
        }

        // Si mi compañero me cantó una carta brava (jerarquía >= 10: 3 o mayor) y yo salgo en primera, aflojo
        if (rondaActual == 1 && senaCompanero != SenaTruco.NINGUNA && senaCompanero != SenaTruco.CIEGO) {
            if (senaCompanero.getJerarquiaMinima() >= 10 && esPrimeraDeRonda) {
                mano.sort(Comparator.comparingInt(Carta::getJerarquiaTruco));
                return mano.remove(0);
            }
        }

        Carta mejorCarta = null;
        double maxProb = -1.0;

        for (Carta c : mano) {
            List<Carta> resto = new ArrayList<>(mano);
            resto.remove(c);

            double prob = SimuladorMonteCarlo.estimarProbabilidadVictoria(
                    c, resto, cartasVisiblesMesa, rondaActual, victoriasPrevias, botEsMano, 0, 200
            );

            if (cartaMesa != null && !cartaMesa.isTapada()) {
                if (c.getJerarquiaTruco() > cartaMesa.getJerarquiaTruco()) {
                    prob += 0.05;
                }
            }

            if (prob > maxProb) {
                maxProb = prob;
                mejorCarta = c;
            }
        }

        if (mejorCarta != null) {
            mano.remove(mejorCarta);
            return mejorCarta;
        }

        return mano.remove(0);
    }

    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esPrimeraDeRonda, boolean ganaCompanero) {
        return jugarCartaInteligente(cartaMesa, esPrimeraDeRonda, ganaCompanero, new ArrayList<>(), 1, new int[3], false);
    }

    public Carta jugarCartaInteligente(Carta cartaMesa, boolean esPrimeraDeRonda) {
        return jugarCartaInteligente(cartaMesa, esPrimeraDeRonda, false);
    }

    public boolean quiereAbrirEnvido(int tanto, boolean esMano) {
        if (esMano) {
            if (tanto >= 27) return true;
            return (tanto < 24 && random.nextInt(100) < 10);
        } else {
            return tanto >= 29;
        }
    }

    public int responderEnvido(int tanto, int tipoEnvido, boolean soyMano) {
        int bonusMano = soyMano ? 1 : 0;
        int umbral = (tipoEnvido == 1) ? 26 : 29;

        if ((tanto + bonusMano) >= 31) {
            if (tipoEnvido == 1 && random.nextInt(100) < 65) return 3;
            return 1;
        }

        if ((tanto + bonusMano) >= umbral) return 1;
        if (tanto >= 24 && random.nextInt(100) < 8) return 1;

        return 2;
    }

    public boolean quiereCantarTruco(
            int nivelActual,
            List<Carta> cartasVisibles,
            int rondaActual,
            int[] vics,
            boolean botEsMano
    ) {
        double prob = SimuladorMonteCarlo.estimarProbabilidadGlobal(
                getMano(), cartasVisibles, rondaActual, vics, botEsMano, 0, 200
        );

        if (nivelActual == 1) {
            if (prob >= 0.58) return true;
            return (prob <= 0.25 && random.nextInt(100) < 10);
        }
        if (nivelActual == 2) return prob >= 0.68;
        if (nivelActual == 3) return prob >= 0.82;

        return false;
    }

    public boolean quiereCantarTruco(int nivelActual) {
        return quiereCantarTruco(nivelActual, new ArrayList<>(), 1, new int[3], false);
    }

    public int responderTruco(
            int nivelPropuesto,
            int puntosQueridos,
            int puntosNoQueridos,
            List<Carta> cartasVisibles,
            int rondaActual,
            int[] vics,
            boolean botEsMano
    ) {
        double pWin = SimuladorMonteCarlo.estimarProbabilidadGlobal(
                getMano(), cartasVisibles, rondaActual, vics, botEsMano, 0, 200
        );

        double evQuiero = (pWin * puntosQueridos) - ((1.0 - pWin) * puntosQueridos);
        double evNoQuiero = -puntosNoQueridos;

        if (pWin >= 0.78 && nivelPropuesto < 4 && random.nextInt(100) < 55) {
            return 3;
        }

        if (evQuiero >= evNoQuiero) {
            return 1;
        }

        if (random.nextInt(100) < 8) return 1;

        return 2;
    }

    public int responderTruco(int nivelPropuesto) {
        int queridos = nivelPropuesto;
        int noQueridos = (nivelPropuesto == 2) ? 1 : nivelPropuesto - 1;
        return responderTruco(nivelPropuesto, queridos, noQueridos, new ArrayList<>(), 1, new int[3], false);
    }

    public int cantarFrenteAFlor(int tanto) {
        return (tanto >= 34) ? 2 : 1;
    }

    public int responderContraflorAlResto(int tanto) {
        return (tanto >= 33) ? 1 : 2;
    }
}