package evaluaciones;

import java.time.LocalDate;

// Estudiante 2 - Ayelen Choquecota - Subclase de Evaluacion (herencia). Indica si el trabajo es grupal
// y sobrescribe getTipo() (polimorfismo).
public class Trabajo extends Evaluacion {
    private boolean esGrupal;

    public Trabajo(String nombre, double peso, LocalDate fecha, boolean esGrupal) {
        super(nombre, peso, fecha);
        this.esGrupal = esGrupal;
    }

    public boolean isEsGrupal() { return esGrupal; }

    @Override
    public String getTipo() {
        return "Trabajo";
    }
}
