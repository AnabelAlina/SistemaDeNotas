package evaluaciones;

import java.time.LocalDate;

// Estudiante 2 - Ayelen Choquecota - Subclase de Evaluacion (herencia). Indica si el parcial tiene recuperacion
// y sobrescribe getTipo() (polimorfismo).
public class Parcial extends Evaluacion {
    private boolean tieneRecuperacion;

    public Parcial(String nombre, double peso, LocalDate fecha, boolean tieneRecuperacion) {
        super(nombre, peso, fecha);
        this.tieneRecuperacion = tieneRecuperacion;
    }

    public boolean isTieneRecuperacion() { return tieneRecuperacion; }

    @Override
    public String getTipo() {
        return "Parcial";
    }
}
