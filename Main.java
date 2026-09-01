import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Partida partida = new Partida(15, scanner);
        partida.iniciar();
        scanner.close();
    }
}