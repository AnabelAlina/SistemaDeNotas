package calculos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import evaluaciones.Evaluacion;

// Estudiante 2 - Ayelen Choquecota - Estrategia de promedio simple (implementa EstrategiaPromedio, patron Strategy).
// Promedio simple: suma de notas entre la cantidad de evaluaciones rendidas (ignora los pesos).
// Usa las notas efectivas, asi que ya considera el sustitutorio.
public class PromedioSimple implements EstrategiaPromedio {
    @Override
    public double calcular(ArrayList<Evaluacion> evaluaciones) {
        LinkedHashMap<Evaluacion, Double> notas = NotasEfectivas.calcular(evaluaciones);
        double suma = 0;
        for (double nota : notas.values()) {
            suma += nota;
        }
        return notas.isEmpty() ? 0 : suma / notas.size();
    }
}
