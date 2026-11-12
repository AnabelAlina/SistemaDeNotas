package evaluaciones;

import java.time.LocalDate;

// Estudiante 2 - Ayelen Choquecota - Subclase de Evaluacion (herencia). Indica si es el examen sustitutorio
// y sobrescribe getTipo() (polimorfismo).
public class ExamenFinal extends Evaluacion {
    private boolean sustitutorio; // true si es el examen sustitutorio

    public ExamenFinal(String nombre, double peso, LocalDate fecha, boolean sustitutorio) {
        super(nombre, peso, fecha);
        this.sustitutorio = sustitutorio;
    }

    public boolean isSustitutorio() { return sustitutorio; }

    @Override
    public String getTipo() {
        return "Examen Final";
    }
}
