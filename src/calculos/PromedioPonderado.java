package calculos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import evaluaciones.Evaluacion;

// Estudiante 2 - Ayelen Choquecota - Estrategia de promedio ponderado (implementa EstrategiaPromedio, patron Strategy).
// Es la que usa Curso por defecto.
// Promedio ponderado: suma(nota x peso) / suma(pesos), solo de las evaluaciones rendidas.
// Asi el promedio "actual" refleja lo rendido hasta ahora, aunque falten evaluaciones.
// Usa las notas efectivas, asi que ya considera el sustitutorio.
public class PromedioPonderado implements EstrategiaPromedio {
    @Override
    public double calcular(ArrayList<Evaluacion> evaluaciones) {
        double suma = 0;
        double pesoRendido = 0;
        for (Map.Entry<Evaluacion, Double> par : NotasEfectivas.calcular(evaluaciones).entrySet()) {
            suma += par.getValue() * par.getKey().getPeso();
            pesoRendido += par.getKey().getPeso();
        }
        return pesoRendido == 0 ? 0 : suma / pesoRendido;
    }
}
