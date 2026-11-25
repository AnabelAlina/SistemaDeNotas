package evaluaciones;

// Examen sustitutorio. No tiene peso propio (peso 0): si se rinde,
// reemplaza la nota mas baja entre el Examen 1 y el Examen 2 (nunca el 3), siempre que la mejore.
// La regla se aplica en calculos.NotasEfectivas. Es opcional: no cuenta como evaluacion pendiente.
public class ExamenSustitutorio extends Evaluacion {

    private static final long serialVersionUID = 1L;

    public ExamenSustitutorio() {
        super("Examen sustitutorio", 0, null);
    }

    @Override
    public String getTipo() {
        return "Sustitutorio";
    }
}