public class ArbitroRonda {

    public static int compararCartas(Carta c1, Carta c2) {
        if (c1.isTapada() && c2.isTapada()) return 0;
        if (c1.isTapada()) return 2;
        if (c2.isTapada()) return 1;

        if (c1.getJerarquiaTruco() > c2.getJerarquiaTruco()) return 1;
        if (c2.getJerarquiaTruco() > c1.getJerarquiaTruco()) return 2;
        return 0;
    }

    public static int definirGanadorMano(int[] resultados, int victoriasJ1, int victoriasJ2, int mano) {
        if (victoriasJ1 >= 2) return 1;
        if (victoriasJ2 >= 2) return 2;

        // Reglas oficiales de parda
        if (resultados[0] == 0 && resultados[1] != 0) return resultados[1];
        if (resultados[0] == 0 && resultados[1] == 0 && resultados[2] != 0) return resultados[2];
        if (resultados[0] != 0 && resultados[1] == 0) return resultados[0];
        if (resultados[0] != 0 && resultados[2] == 0) return resultados[0];

        // Triple parda: gana el mano
        return mano;
    }
}