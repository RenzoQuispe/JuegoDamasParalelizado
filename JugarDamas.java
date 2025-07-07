import java.util.List;

public interface JugarDamas {
    List<String> generarMovimientosPosibles(char color);
    boolean moverJugador(String movimiento);
    String moverComputadora();
    String convertirCoordenadasAPosicionTablero(String movimiento);
}
