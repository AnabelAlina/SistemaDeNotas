package calculos;

import java.util.ArrayList;
import evaluaciones.Evaluacion;

// Estudiante 2 - Ayelen Choquecota - Estrategia de promedio simple (implementa EstrategiaPromedio, patron Strategy).
// Promedio simple: suma de notas entre la cantidad de evaluaciones rendidas (ignora los pesos).
public class PromedioSimple implements EstrategiaPromedio {
    @Override
    public double calcular(ArrayList<Evaluacion> evaluaciones) {
        double suma = 0;
        int cantidad = 0;
        for (Evaluacion e : evaluaciones) {
            if (e.isRendida()) {
                suma += e.getNota();
                cantidad++;
            }
        }
        return cantidad == 0 ? 0 : suma / cantidad;
    }
}
