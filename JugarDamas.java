import java.awt.*;
import javax.swing.*;

public class JugarDamas extends JFrame{

    private char colorJugador;
    private char[][] tablero = new char[8][8];
    
    public JugarDamas(char colorJugador) {
        
        this.colorJugador = colorJugador;
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

    public void moverComputadora() {
        // Mueve la primera pieza que encuentre hacia adelante si puede
        char colorPC = (colorJugador == 'B') ? 'N' : 'B';
        int direccion = (colorPC == 'B') ? -1 : 1;

        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                if (tablero[fila][col] == colorPC) {
                    int nuevaFila = fila + direccion;
                    if (nuevaFila >= 0 && nuevaFila < 8) {
                        if (col > 0 && tablero[nuevaFila][col - 1] == '*') {
                            tablero[nuevaFila][col - 1] = colorPC;
                            tablero[fila][col] = '*';
                            actualizarVista();
                            return;
                        }
                        if (col < 7 && tablero[nuevaFila][col + 1] == '*') {
                            tablero[nuevaFila][col + 1] = colorPC;
                            tablero[fila][col] = '*';
                            actualizarVista();
                            return;
                        }
                    }
                }
            }
        }
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

}