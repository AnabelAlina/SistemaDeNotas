package calculos;

import java.util.ArrayList;
import java.util.Map;
import evaluaciones.Evaluacion;

// Estrategia de promedio ponderado (implementa EstrategiaPromedio, patron Strategy).
// Es la que usa Curso por defecto.
public class PromedioPonderado implements EstrategiaPromedio {

    private static final long serialVersionUID = 1L;

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