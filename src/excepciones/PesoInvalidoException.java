package excepciones;
// Se lanza cuando los pesos de las evaluaciones no son válidos o no suman 100%.
public class PesoInvalidoException extends Exception {
    public PesoInvalidoException(String mensaje) {
        super(mensaje);
    }
}