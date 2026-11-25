package evaluaciones;

import java.io.Serializable;
import excepciones.NotaInvalidaException;

// Un trabajo dentro de una Continua (tarea, practica, exposicion...).
// Su peso es un porcentaje INTERNO de la continua (0 a 100): los trabajos de una continua suman 100%.
// No es una Evaluacion del curso: vive dentro de Continua (composicion).
public class TrabajoContinua implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private double peso; // % dentro de la continua
    private double nota;
    private boolean rendido;
    private boolean esGrupal;

    public TrabajoContinua(String nombre, double peso, boolean esGrupal) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del trabajo no puede estar vacio.");
        }
        if (peso <= 0 || peso > 100) {
            throw new IllegalArgumentException("El peso del trabajo debe estar entre 0 y 100.");
        }
        this.nombre = nombre.trim();
        this.peso = peso;
        this.esGrupal = esGrupal;
        this.rendido = false;
    }

    public String getNombre() { return nombre; }
    public double getPeso() { return peso; }
    public double getNota() { return nota; }
    public boolean isRendido() { return rendido; }
    public boolean isEsGrupal() { return esGrupal; }

    // Valida 0 a 20 (lanza NotaInvalidaException) y marca el trabajo como rendido
    public void setNota(double nota) throws NotaInvalidaException {
        if (Double.isNaN(nota) || nota < Evaluacion.NOTA_MINIMA || nota > Evaluacion.NOTA_MAXIMA) {
            throw new NotaInvalidaException("La nota debe estar entre " + (int) Evaluacion.NOTA_MINIMA
                    + " y " + (int) Evaluacion.NOTA_MAXIMA + ". Recibido: " + nota);
        }
        this.nota = nota;
        this.rendido = true;
    }

    @Override
    public String toString() {
        return nombre + " (" + peso + "% de la continua" + (esGrupal ? ", grupal" : "") + "): "
                + (rendido ? "nota " + nota : "pendiente");
    }
}