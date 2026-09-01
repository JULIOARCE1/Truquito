public class Carta {
    private final int numero;
    private final Palo palo;
    private final int jerarquiaTruco;
    private final int valorEnvido;

    public Carta(int numero, Palo palo) {
        this.numero = numero;
        this.palo = palo;
        this.jerarquiaTruco = calcularJerarquiaTruco(numero, palo);
        this.valorEnvido = (numero >= 10) ? 0 : numero;
    }

    private int calcularJerarquiaTruco(int n, Palo p) {
        if (n == 1 && p == Palo.ESPADA) return 14;
        if (n == 1 && p == Palo.BASTO) return 13;
        if (n == 7 && p == Palo.ESPADA) return 12;
        if (n == 7 && p == Palo.ORO) return 11;
        if (n == 3) return 10;
        if (n == 2) return 9;
        if (n == 1) return 8; // Copa u Oro
        if (n == 12) return 7;
        if (n == 11) return 6;
        if (n == 10) return 5;
        if (n == 7) return 4; // Copa o Basto
        if (n == 6) return 3;
        if (n == 5) return 2;
        return 1; // 4s
    }

    public int getNumero() { return numero; }
    public Palo getPalo() { return palo; }
    public int getJerarquiaTruco() { return jerarquiaTruco; }
    public int getValorEnvido() { return valorEnvido; }

    @Override
    public String toString() {
        return numero + " de " + palo;
    }
}