import java.util.List;

public interface VistaJuego {
    void mostrarMensaje(String mensaje);
    void mostrarAlerta(String mensaje);
    void mostrarSeparador();
    void mostrarInicioPartida(int limite, int totalJugadores, String eq1, String eq2);
    void mostrarFinPartida(String ganador);
    void mostrarTanteador(String eq1, String pts1, String eq2, String pts2, int limite);
    void mostrarNuevaMano(String manoNombre, String detalleEquipo);
    void mostrarCartasPropias(List<Carta> cartas, int tantoEnvido);
    void mostrarCartasCompanero(String nombreCompanero, List<Carta> cartas, int tantoEnvido);
    
    int pedirOpcion(String titulo, List<String> opciones, int min, int max);
    boolean confirmarContinuar();
}