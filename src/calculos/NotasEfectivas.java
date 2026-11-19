package calculos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;

// Estudiante 2 - Ayelen Choquecota - Calcula la nota que realmente cuenta de cada evaluacion rendida,
// aplicando la regla del sustitutorio UNSA:
//   si hay sustitutorio rendido, reemplaza la nota mas baja entre el Examen 1 y el Examen 2
//   (nunca el Examen 3), y solo si el sustitutorio la mejora. El sustitutorio no aparece en el resultado.
// Lo usan PromedioSimple, PromedioPonderado, EstadisticasNotas y SimuladorNotas.
public class NotasEfectivas {

    // Evaluaciones opcionales: no cuentan como "pendientes" (no tienen peso propio)
    public static boolean esOpcional(Evaluacion e) {
        return e instanceof ExamenSustitutorio;
    }

    // Evaluacion -> nota que cuenta, solo de las rendidas y en el orden de la lista
    public static LinkedHashMap<Evaluacion, Double> calcular(ArrayList<Evaluacion> evaluaciones) {
        LinkedHashMap<Evaluacion, Double> notas = new LinkedHashMap<>();
        ExamenSustitutorio sustitutorio = null;
        for (Evaluacion e : evaluaciones) {
            if (!e.isRendida()) {
                continue;
            }
            if (e instanceof ExamenSustitutorio) {
                sustitutorio = (ExamenSustitutorio) e;
            } else {
                notas.put(e, e.getNota());
            }
        }
        if (sustitutorio != null) {
            Examen masBajo = null;
            for (Evaluacion e : notas.keySet()) {
                if (e instanceof Examen && ((Examen) e).getNumero() <= 2) {
                    if (masBajo == null || e.getNota() < masBajo.getNota()) {
                        masBajo = (Examen) e;
                    }
                }
            }
            if (masBajo != null && sustitutorio.getNota() > masBajo.getNota()) {
                notas.put(masBajo, sustitutorio.getNota());
            }
        }
        return notas;
    }
}
