package evaluaciones;

// Estudiante 2 - Ayelen Choquecota - Examen de teoria de un curso UNSA: Examen 1, 2 o 3.
// Hereda de Evaluacion (herencia) y sobrescribe getTipo() (polimorfismo).
// Su peso es el "% examen teoria" que dice el silabo (porcentaje sobre la nota final del curso).
public class Examen extends Evaluacion {
    private int numero; // 1, 2 o 3

    public Examen(int numero, double peso) {
        super("Examen " + validarNumero(numero), peso, null);
        this.numero = numero;
    }

    private static int validarNumero(int numero) {
        if (numero < 1 || numero > 3) {
            throw new IllegalArgumentException("El numero de examen debe ser 1, 2 o 3.");
        }
        return numero;
    }

    public int getNumero() { return numero; }

    @Override
    public String getTipo() {
        return "Examen";
    }
}
