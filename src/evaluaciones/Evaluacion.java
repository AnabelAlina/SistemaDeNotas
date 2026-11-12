package evaluaciones;
import java.time.LocalDate;
import excepciones.NotaInvalidaException;
// STUB: el Estudiante 2 completa validaciones y lógica
public abstract class Evaluacion {
    private String nombre;
    private double peso;
    private double nota;
    private LocalDate fecha;
    private boolean rendida;
    public Evaluacion(String nombre, double peso, LocalDate fecha) {
        this.nombre = nombre;
        this.peso = peso;
        this.fecha = fecha;
    }
    public String getNombre() { return nombre; }
    public double getPeso() { return peso; }
    public double getNota() { return nota; }
    public LocalDate getFecha() { return fecha; }
    public boolean isRendida() { return rendida; }
    public void setNota(double nota) throws NotaInvalidaException {
        // TODO (Estudiante 2): validar 0 a 20 y marcar rendida
    }
    public double getNotaPonderada() { return 0; } // TODO (Estudiante 2)
    public abstract String getTipo();
}