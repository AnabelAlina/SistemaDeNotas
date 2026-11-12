package calculos;

import java.util.ArrayList;
import evaluaciones.Evaluacion;

// Estudiante 2 - Ayelen Choquecota - Estrategia de promedio ponderado (implementa EstrategiaPromedio, patron Strategy).
// Es la que usa Curso por defecto.
// Promedio ponderado: suma(nota x peso) / suma(pesos), solo de las evaluaciones rendidas.
// Asi el promedio "actual" refleja lo rendido hasta ahora, aunque falten evaluaciones.
public class PromedioPonderado implements EstrategiaPromedio {
    @Override
    public double calcular(ArrayList<Evaluacion> evaluaciones) {
        double suma = 0;
        double pesoRendido = 0;
        for (Evaluacion e : evaluaciones) {
            if (e.isRendida()) {
                suma += e.getNota() * e.getPeso();
                pesoRendido += e.getPeso();
            }
        }
        return pesoRendido == 0 ? 0 : suma / pesoRendido;
    }
}
