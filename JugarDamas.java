import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class JugarDamas extends JFrame{

    private char colorJugador;
    private char colorComputadora;
    private char[][] tablero = new char[8][8];
    /*
    'B': Fichas blancas
    'N': Fichas negras
    ' ' y '*'→ casillas vacía(pero solo pueden moverse en las csaillas donde es *)
    '0' : dama negra
    '1' : dama blanca
    */
    
    public JugarDamas(char colorJugador, char colorComputadora) {
        
        this.colorJugador = colorJugador;
        this.colorComputadora = colorComputadora;
        inicializarTablero();

        setTitle("Damas - Jugador con fichas " + (colorJugador == 'B' ? "BLANCAS" : "NEGRAS"));
        setSize(400, 370);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar en pantalla

        // Establecer el ícono de la ventana
        Image icono = Toolkit.getDefaultToolkit().getImage(getClass().getResource("icono.png"));
        setIconImage(icono);
        
        // Tablero
        JTextArea areaTexto = new JTextArea(generarTextoTablero());
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 22));
        add(new JScrollPane(areaTexto));

        setVisible(true);
    }

    private void inicializarTablero() {
        // Colocar piezas en filas iniciales
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                if ((fila + col) % 2 == 1) {
                    if (fila < 3) {
                        tablero[fila][col] = 'N'; // negras arriba
                    } else if (fila > 4) {
                        tablero[fila][col] = 'B'; // blancas abajo
                    } else {
                        tablero[fila][col] = '*';
                    }
                } else {
                    tablero[fila][col] = ' ';
                }
            }
        }
    }

    public String generarTextoTablero() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n  --- TABLERO DE DAMAS ---\n");

        if (colorJugador == 'B') {
            for (int fila = 0; fila < 8; fila++) {
                sb.append("  ");
                for (int col = 0; col < 8; col++) {
                    sb.append("[").append(tablero[fila][col]).append("]");
                }
                sb.append(" ").append(8 - fila).append("\n");
            }
            sb.append("  ");
            for (char c = 'A'; c <= 'H'; c++) {
                sb.append(" ").append(c).append(" ");
            }
            sb.append("\n");

        } else if (colorJugador == 'N') {
            for (int fila = 7; fila >= 0; fila--) {
                sb.append("  ");
                for (int col = 7; col >= 0; col--) {
                    sb.append("[").append(tablero[fila][col]).append("]");
                }
                sb.append(" ").append(8 -fila).append("\n");
            }
            sb.append("  ");
            for (char c = 'H'; c >= 'A'; c--) {
                sb.append(" ").append(c).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
    public boolean moverJugador(String origen, String destino) {
        int[] o = convertirCoord(origen);
        int[] d = convertirCoord(destino);

        if (o == null || d == null) {
            return false;
        }

        if (tablero[o[0]][o[1]] != colorJugador || tablero[d[0]][d[1]] != '*') {
            return false;
        }

        // Movimiento válido simple (sin captura por ahora)
        tablero[d[0]][d[1]] = colorJugador;
        tablero[o[0]][o[1]] = '*';
        actualizarVista();
        return true;
    }

    public String moverComputadora() {
        char colorPC = (colorJugador == 'B') ? 'N' : 'B';
        int[] direcciones = {-1, 1}; // arriba y abajo

        // Primero buscar capturas posibles
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                if (tablero[fila][col] == colorPC) {
                    for (int df : direcciones) {
                        for (int dc : direcciones) {
                            int medioFila = fila + df;
                            int medioCol = col + dc;
                            int destinoFila = fila + 2 * df;
                            int destinoCol = col + 2 * dc;

                            if (esValido(destinoFila, destinoCol)
                                && tablero[medioFila][medioCol] == colorJugador
                                && tablero[destinoFila][destinoCol] == '*') {

                                // Realizar captura
                                tablero[destinoFila][destinoCol] = colorPC;
                                tablero[fila][col] = '*';
                                tablero[medioFila][medioCol] = '*';
                                actualizarVista();
                                return coordToString(fila, col) + " x " + coordToString(destinoFila, destinoCol);
                            }
                        }
                    }
                }
            }
        }

        // Si no hay capturas, moverse en cualquier dirección
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                if (tablero[fila][col] == colorPC) {
                    for (int df : direcciones) {
                        for (int dc : direcciones) {
                            int nuevaFila = fila + df;
                            int nuevaCol = col + dc;

                            if (esValido(nuevaFila, nuevaCol) && tablero[nuevaFila][nuevaCol] == '*') {
                                tablero[nuevaFila][nuevaCol] = colorPC;
                                tablero[fila][col] = '*';
                                actualizarVista();
                                return coordToString(fila, col) + " a " + coordToString(nuevaFila, nuevaCol);
                            }
                        }
                    }
                }
            }
        }

        return null; // No pudo mover
    }

    private boolean esValido(int fila, int col) {
        return fila >= 0 && fila < 8 && col >= 0 && col < 8;
    }

    private String coordToString(int fila, int col) {
        char letra = (char)('A' + col);
        int numero = 8 - fila;
        return "" + letra + numero;
    }

    private int[] convertirCoord(String input) {
        input = input.toUpperCase().trim();
        if (input.length() != 2) return null;

        char letra = input.charAt(0);
        char numero = input.charAt(1);

        int col = letra - 'A';
        int fila = 8 - Character.getNumericValue(numero);

        if (col < 0 || col > 7 || fila < 0 || fila > 7) return null;

        return new int[]{fila, col};
    }

    private void actualizarVista() {
        JTextArea area = (JTextArea)((JScrollPane)getContentPane().getComponent(0)).getViewport().getView();
        area.setText(generarTextoTablero());
    }

    /*
        Funciones para generar todos los movimiento posibles de la computadora
    */
    public List<String> generarMovimientosPosibles() {
        List<String> capturas = new ArrayList<>();
        List<String> movimientosSimples = new ArrayList<>();

        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                char ficha = tablero[fila][col];
                boolean esDama = (ficha == '0' && colorJugador == 'N') || (ficha == '1' && colorJugador == 'B');

                if (ficha == colorComputadora || (esDama)) {
                    if (esDama) { // Identifico una ficha dama
                        capturas.addAll(buscarCapturasDama(fila, col));
                        if (capturas.isEmpty()) {
                            movimientosSimples.addAll(buscarMovimientosDama(fila, col));
                        }
                    } else {// identifico una ficha normal
                        capturas.addAll(buscarCapturasNormales(fila, col));
                        if (capturas.isEmpty()) {
                            movimientosSimples.addAll(buscarMovimientosNormales(fila, col));
                        }
                    }
                }
            }
        }

        return !capturas.isEmpty() ? capturas : movimientosSimples;
    }

    private List<String> buscarMovimientosNormales(int fila, int col) {
        List<String> movimientos = new ArrayList<>();
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};

        for (int i = 0; i < 4; i++) {
            int nuevaFila = fila + dFila[i];
            int nuevaCol = col + dCol[i];
            if (dentroTablero(nuevaFila, nuevaCol) && tablero[nuevaFila][nuevaCol] == '*') {
                movimientos.add("" + fila + col + " a " + nuevaFila + nuevaCol);
            }
        }
        return movimientos;
    }

    private List<String> buscarCapturasNormales(int fila, int col) {
        List<String> capturas = new ArrayList<>();
        buscarCapturasRec(fila, col,new boolean[8][8], "", capturas);
        return capturas;
    }

    private void buscarCapturasRec(int fila, int col, boolean[][] visitado,String camino, List<String> capturas) {
        boolean capturaEncontrada = false;
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        char oponente = colorJugador;
        char damaOponente = (colorJugador == 'B') ? '1' : '0';

        for (int i = 0; i < 4; i++) {
            int filaMid = fila + dFila[i];
            int colMid = col + dCol[i];
            int filaDestino = fila + 2 * dFila[i];
            int colDestino = col + 2 * dCol[i];

            if (dentroTablero(filaDestino, colDestino) &&
                (tablero[filaMid][colMid] == oponente || tablero[filaMid][colMid] == damaOponente) &&
                tablero[filaDestino][colDestino] == '*' && !visitado[filaMid][colMid]) {

                visitado[filaMid][colMid] = true;

                char[][] copia = copiarTablero(tablero);
                copia[fila][col] = '*';
                copia[filaMid][colMid] = '*';
                copia[filaDestino][colDestino] = colorComputadora;

                String nuevoCamino = camino.isEmpty() ?
                    ("" + fila + col + " a " + filaDestino + colDestino) :
                    (camino + " a " + filaDestino + colDestino);

                buscarCapturasRec(filaDestino, colDestino, visitado, nuevoCamino, capturas);
                capturaEncontrada = true;
                visitado[filaMid][colMid] = false;
            }
        }

        if (!capturaEncontrada && !camino.isEmpty()) {
            capturas.add(camino);
        }
    }

    private List<String> buscarMovimientosDama(int fila, int col) {
        List<String> movimientos = new ArrayList<>();
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        if ((tablero[fila][col] == '0' && colorComputadora == 'N') || (tablero[fila][col] == '1' && colorComputadora == 'B')) {
            for (int i = 0; i < 4; i++) {
                int f = fila + dFila[i];
                int c = col + dCol[i];
                while (dentroTablero(f, c) && tablero[f][c] == '*') {
                    movimientos.add("" + fila + col + " a " + f + c);
                    f += dFila[i];
                    c += dCol[i];
                }
            }            
        }
        return movimientos;
    }

    private List<String> buscarCapturasDama(int fila, int col) {
        List<String> capturas = new ArrayList<>();
        char oponente = colorJugador;
        char damaOponente = (colorJugador == 'B') ? '1' : '0';
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        
        if((tablero[fila][col] == '0' && colorComputadora == 'N') || (tablero[fila][col] == '1' && colorComputadora == 'B')){
            for (int dir = 0; dir < 4; dir++) {
                int f = fila + dFila[dir];
                int c = col + dCol[dir];
                boolean enemigoVisto = false;

                while (dentroTablero(f, c)) {
                    if (tablero[f][c] == '*') {
                        if (enemigoVisto) {
                            capturas.add("" + fila + col + " a " + f + c);
                            break;
                        }
                    } else if (tablero[f][c] == oponente || tablero[f][c] == damaOponente) {
                        if (enemigoVisto) break;
                        enemigoVisto = true;
                    } else {
                        break;
                    }
                    f += dFila[dir];
                    c += dCol[dir];
                }
            }
        }



        return capturas;
    }
    //Verifica si una posicion esta dentro del tablero
    private boolean dentroTablero(int fila, int col) {
        return fila >= 0 && fila < 8 && col >= 0 && col < 8;
    }

    private char[][] copiarTablero(char[][] original) {
        char[][] copia = new char[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(original[i], 0, copia[i], 0, 8);
        }
        return copia;
    }

}
    