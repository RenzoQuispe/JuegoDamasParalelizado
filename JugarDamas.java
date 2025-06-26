import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import javax.swing.*;

public class JugarDamas extends JFrame{

    private char colorJugador;
    private char colorComputadora;
    private char[][] tablero = new char[8][8];
    /*
    'B': Fichas blancas
    'N': Fichas negras
    ' ' y '*' : casillas vacía(pero solo pueden moverse en las csaillas donde es *)
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
        Image icono = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/icono.png"));
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
        // coronacion
        if (d[0]==0 && colorJugador=='B') {
            tablero[d[0]][d[1]] = '1';
        }
        if (d[0]==7 && colorJugador=='N') {
            tablero[d[0]][d[1]] = '0';
        }
        actualizarVista();
        return true;
    }

    public String moverComputadora() {
        ForkJoinPool pool = new ForkJoinPool();
        List<String> movimientos = generarMovimientosPosibles();
        if (movimientos.isEmpty()) return "No hay movimientos posibles";
        // lista para guardar resultados evaluados para cada movimiento
        List<MovimientoEvaluado> evaluaciones = Collections.synchronizedList(new ArrayList<>());
        pool.submit(() -> movimientos.parallelStream().forEach(mov -> {
            System.out.println("Evaluando :"+convertirCoordenadasAPosicionTablero(mov));
            //Tablero simulado con el movimiento
            char[][] copiaTablero = copiarTablero(tablero);
            char[][] movSimulacion = aplicarMovimiento(copiaTablero, mov);
            //Puntaje de dicha simulacion
            int puntaje = evaluarTablero(movSimulacion, colorComputadora);
            System.out.println(" - Diferencia puntaje: "+puntaje);
            //Riesgo de dicha simulacion
            int riesgo = evaluarRiesgo(movSimulacion, colorComputadora);
            System.out.println(" - Riesgo: "+riesgo);
            //guardar puntaje final
            double puntajeFinal = puntaje - 1.5 * riesgo;
            System.out.println(" - Puntaje final: "+puntajeFinal);
            evaluaciones.add(new MovimientoEvaluado(mov, puntajeFinal));
        })).join();
        //elegir el movimiento con mayor puntaje
        MovimientoEvaluado mejor = evaluaciones.stream().max(Comparator.comparingDouble(MovimientoEvaluado::getScore)).orElse(null);
        if (mejor == null) return "No hay movimientos posibles";
        //hacer movimiento, actualizar tablero
        tablero = aplicarMovimiento(tablero, mejor.getMovimiento());
        //actualizar vista del tablero
        actualizarVista();
        return convertirCoordenadasAPosicionTablero(mejor.getMovimiento()); // retornara descripcion del movimiento hecho
    }

    private boolean esValido(int fila, int col) {
        return fila >= 0 && fila < 8 && col >= 0 && col < 8;
    }

    public String convertirCoordenadasAPosicionTablero(String movimiento) { // para convertir los movimientos de tipo "21 a 32 a 45" o "54 a 56" a ubicacion de tablero (A1,C3,F2,D6,etc)
        List<String> listaCoordenadas = new ArrayList<>();
        String[] partes = movimiento.split("\\s*a\\s*"); // divide por 'a' con posibles espacios
        for (String parte : partes) {
            if (parte.matches("\\d{2}")) { // asegura que tenga 2 dígitos
                listaCoordenadas.add(parte);
            }
        }
        //Procesar esos numeros
        List<String>  listaCoordenadasProcesada = new ArrayList<>();
        for(String coordenadas: listaCoordenadas){
            int fila = 8 - Character.getNumericValue(coordenadas.charAt(0));
            char columna =(char) ( 'A' + Character.getNumericValue(coordenadas.charAt(1)));
            String coordenadaProcesada = "" + columna + fila;
            listaCoordenadasProcesada.add(coordenadaProcesada); 
        }
        
        //Concatenarlos
        StringBuilder stringFinal = new StringBuilder();
        for (int i = 0; i < listaCoordenadasProcesada.size(); i++) {
            stringFinal.append(listaCoordenadasProcesada.get(i));
            if (i < listaCoordenadasProcesada.size() - 1) {
                stringFinal.append(" a ");
            }
        }
        return stringFinal.toString();
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
    Funciones para evaluar los movimientos posibles 
    */
    public int evaluarTablero(char[][] tablero, char colorComputadora) { //medicion de la ventaja material
        int score = 0;
        char fichaNormal = colorComputadora;
        char dama = (colorComputadora == 'B') ? '1' : '0';
        char fichaNormalJugador = (colorComputadora == 'B') ? 'N' : 'B';
        char damaJugador = (colorComputadora == 'B') ? '0' : '1';
    
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                char p = tablero[i][j];
                if (p == fichaNormal) score += 3;
                else if (p == dama) score += 5;
                else if (p == fichaNormalJugador) score -= 3;
                else if (p == damaJugador) score -= 5;
            }
        }
        return score;
    }
    public int evaluarRiesgo(char[][] tablero, char colorComputadora) {
        int riesgo = 0;
        char enemigo = (colorComputadora == 'B') ? 'N' : 'B';
        char damaEnemiga = (enemigo == 'B') ? '1' : '0';
        
        for (int fila = 0; fila < 8; fila++) {
            for (int col = 0; col < 8; col++) {
                char pieza = tablero[fila][col];
                if (pieza == enemigo || pieza == damaEnemiga) {
                    if (pieza == enemigo) {
                        riesgo += buscarCapturasNormalesDesde(tablero, fila, col, colorComputadora).size();
                    } else {
                        riesgo += buscarCapturasDamaDesde(tablero, fila, col, colorComputadora).size();
                    }
                }
            }
        }

        return riesgo;
    }
    private List<String> buscarCapturasNormalesDesde(char[][] tab, int fila, int col, char objetivo) {
        List<String> capturas = new ArrayList<>();
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};
        char damaObjetivo = (objetivo == 'B') ? '1' : '0';

        for (int i = 0; i < 4; i++) {
            int midFila = fila + dFila[i];
            int midCol = col + dCol[i];
            int destFila = fila + 2 * dFila[i];
            int destCol = col + 2 * dCol[i];

            if (dentroTablero(destFila, destCol)
                && (tab[midFila][midCol] == objetivo || tab[midFila][midCol] == damaObjetivo)
                && tab[destFila][destCol] == '*') {
                capturas.add("" + fila + col + " a " + destFila + destCol);
            }
        }
        return capturas;
    }

    private List<String> buscarCapturasDamaDesde(char[][] tab, int fila, int col, char objetivo) {
        List<String> capturas = new ArrayList<>();
        char damaObjetivo = (objetivo == 'B') ? '1' : '0';
        int[] dFila = {-1, -1, 1, 1};
        int[] dCol = {-1, 1, -1, 1};

        for (int dir = 0; dir < 4; dir++) {
            int f = fila + dFila[dir];
            int c = col + dCol[dir];
            boolean enemigoVisto = false;

            while (dentroTablero(f, c)) {
                if (tab[f][c] == '*') {
                    if (enemigoVisto) {
                        capturas.add("" + fila + col + " a " + f + c);
                        break;
                    }
                } else if (tab[f][c] == objetivo || tab[f][c] == damaObjetivo) {
                    if (enemigoVisto) break;
                    enemigoVisto = true;
                } else break;

                f += dFila[dir];
                c += dCol[dir];
            }
        }

        return capturas;
    }
    // funcion para aplicar movimiento a un tablero(original o copia)
    public char[][] aplicarMovimiento(char[][] tablero, String mov) {
        char[][] copia = copiarTablero(tablero);
        String[] partes = mov.split(" a ");
        
        for (int i = 0; i < partes.length - 1; i++) {
            String desde = partes[i];
            String hacia = partes[i + 1];

            int filaDesde = Character.getNumericValue(desde.charAt(0));
            int colDesde = Character.getNumericValue(desde.charAt(1));
            int filaHacia = Character.getNumericValue(hacia.charAt(0));
            int colHacia = Character.getNumericValue(hacia.charAt(1));

            char ficha = copia[filaDesde][colDesde];
            copia[filaDesde][colDesde] = '*';

            // si es una captura, eliminar ficha intermedia
            if (Math.abs(filaDesde - filaHacia) == 2) {
                int filaMedio = (filaDesde + filaHacia) / 2;
                int colMedio = (colDesde + colHacia) / 2;
                copia[filaMedio][colMedio] = '*';
            }

            copia[filaHacia][colHacia] = ficha;
            // posible coronacion negras
            if(filaHacia==7 && colorComputadora == 'N'){
                copia[filaHacia][colHacia] = '0';
            }
            // posible coronacion blancas
            if (filaHacia==0 && colorComputadora == 'B') {
                copia[filaHacia][colHacia] = '1';
            }
        }

        return copia;
    }

    /*
        Funciones para generar todos los movimiento posibles de la computadora
    */
    public List<String> generarMovimientosPosibles() {
        // Movimiento posibles dados como coordenadas de tablero
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
        int[] dFila = {1, 1}; // computadora usa fichas negras
        if (colorComputadora == 'B') { // computadora usa fichas blancas
            dFila = new int[] {-1, -1}; 
        }
        int[] dCol = {-1, 1};

        for (int i = 0; i < 2; i++) {
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
    