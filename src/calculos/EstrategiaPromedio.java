package calculos;

import java.io.Serializable;
import java.util.ArrayList;
import evaluaciones.Evaluacion;

public interface EstrategiaPromedio extends Serializable {

    double calcular(ArrayList<Evaluacion> evaluaciones);
}