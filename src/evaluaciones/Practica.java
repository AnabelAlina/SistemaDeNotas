package evaluaciones;

import java.time.LocalDate;

// Estudiante 2 - Ayelen Choquecota - Subclase de Evaluacion (herencia). Guarda el numero de la practica
// y sobrescribe getTipo() (polimorfismo).
public class Practica extends Evaluacion {
    private int numero; // practica 1, 2, 3...

    public Practica(String nombre, double peso, LocalDate fecha, int numero) {
        super(nombre, peso, fecha);
        if (numero <= 0) {
            throw new IllegalArgumentException("El numero de la practica debe ser mayor que 0.");
        }
        this.numero = numero;
    }

    public int getNumero() { return numero; }

    @Override
    public String getTipo() {
        return "Practica";
    }
}
