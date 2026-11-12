package excepciones;
// Se lanza cuando una nota no está entre 0 y 20.
public class NotaInvalidaException extends Exception {
    public NotaInvalidaException(String mensaje) {
        super(mensaje);
    }
}