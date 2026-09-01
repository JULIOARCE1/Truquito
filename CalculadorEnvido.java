import java.util.List;

public class CalculadorEnvido {

    public static int calcular(List<Carta> cartas) {
        if (cartas == null || cartas.isEmpty()) return 0;

        int maxEnvido = 0;

        for (int i = 0; i < cartas.size(); i++) {
            for (int j = i + 1; j < cartas.size(); j++) {
                Carta c1 = cartas.get(i);
                Carta c2 = cartas.get(j);

                if (c1.getPalo() == c2.getPalo()) {
                    int tanto = 20 + c1.getValorEnvido() + c2.getValorEnvido();
                    if (tanto > maxEnvido) {
                        maxEnvido = tanto;
                    }
                }
            }
        }

        if (maxEnvido == 0) {
            for (Carta c : cartas) {
                if (c.getValorEnvido() > maxEnvido) {
                    maxEnvido = c.getValorEnvido();
                }
            }
        }

        return maxEnvido;
    }
}