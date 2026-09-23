public class Carta {
    private final int numero;
    private final Palo palo;
    private boolean tapada;

    public Carta(int numero, Palo palo) {
        this.numero = numero;
        this.palo = palo;
        this.tapada = false;
    }

    public int getNumero() {
        return numero;
    }

    public Palo getPalo() {
        return palo;
    }

    public boolean isTapada() {
        return tapada;
    }

    public void setTapada(boolean tapada) {
        this.tapada = tapada;
    }

    public int getValorEnvido() {
        if (numero >= 10) return 0;
        return numero;
    }

    public int getJerarquiaTruco() {
        if (numero == 1 && palo == Palo.ESPADA) return 14;
        if (numero == 1 && palo == Palo.BASTO)  return 13;
        if (numero == 7 && palo == Palo.ESPADA) return 12;
        if (numero == 7 && palo == Palo.ORO)    return 11;
        if (numero == 3) return 10;
        if (numero == 2) return 9;
        if (numero == 1) return 8; // 1 de Copa o Oro
        if (numero == 12) return 7;
        if (numero == 11) return 6;
        if (numero == 10) return 5;
        if (numero == 7)  return 4; // 7 de Copa o Basto
        if (numero == 6)  return 3;
        if (numero == 5)  return 2;
        if (numero == 4)  return 1;
        return 0;
    }

    @Override
    public String toString() {
        if (tapada) {
            return "\u001B[90m[Carta Tapada]\u001B[0m";
        }

        String color;
        switch (palo) {
            case ESPADA -> color = "\u001B[94m";  // Azul
            case BASTO  -> color = "\u001B[92m";  // Verde
            case ORO    -> color = "\u001B[93m";  // Amarillo
            case COPA   -> color = "\u001B[91m";  // Rojo
            default     -> color = "\u001B[0m";
        }
        String reset = "\u001B[0m";
        return color + numero + " de " + palo + reset;
    }
}