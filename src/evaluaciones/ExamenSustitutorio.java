package evaluaciones;

// Estudiante 2 - Ayelen Choquecota - Examen sustitutorio. No tiene peso propio (peso 0): si se rinde,
// reemplaza la nota mas baja entre el Examen 1 y el Examen 2 (nunca el 3), siempre que la mejore.
// La regla se aplica en calculos.NotasEfectivas. Es opcional: no cuenta como evaluacion pendiente.
//
// NOTA para Estudiante 1 (Ani): Curso.agregarEvaluacion() rechaza peso <= 0. Para poder agregar el
// sustitutorio hay que permitir peso 0 SOLO para esta clase:
//   if (evaluacion.getPeso() <= 0 && !(evaluacion instanceof ExamenSustitutorio)) { ...excepcion... }
public class ExamenSustitutorio extends Evaluacion {

    public ExamenSustitutorio() {
        super("Examen sustitutorio", 0, null);
    }

    @Override
    public String getTipo() {
        return "Sustitutorio";
    }
}
