package com.netknife.tools.digitalfootprint.check;

import com.netknife.common.dto.CheckStatus;

/**
 * Traduce una categoria de metadato extraido a un nivel de riesgo y una
 * explicacion en lenguaje llano de por que importa. Logica pura, sin
 * dependencias, para poder testearla sin pasar por Tika.
 */
public final class MetadataRiskClassifier {

    public record Classification(CheckStatus status, String explanation) {
    }

    private MetadataRiskClassifier() {
    }

    public static Classification classify(String category) {
        return switch (category) {
            case "Ubicacion GPS" -> new Classification(CheckStatus.PELIGRO,
                    "Revela la ubicacion fisica exacta donde se tomo la foto. Si compartes esta imagen, cualquiera "
                            + "puede ver en un mapa donde estabas.");
            case "Autor" -> new Classification(CheckStatus.ATENCION,
                    "Puede revelar tu nombre real o el de otra persona que edito el archivo.");
            case "Organizacion" -> new Classification(CheckStatus.ATENCION,
                    "Puede revelar el nombre de tu empresa u organizacion.");
            case "Software usado" -> new Classification(CheckStatus.OK,
                    "Indica que programa se uso para crear el archivo. Normalmente no es sensible por si solo.");
            case "Fecha de creacion", "Fecha de modificacion" -> new Classification(CheckStatus.OK,
                    "Indica cuando se creo o edito el archivo por ultima vez. Normalmente no es sensible por si solo.");
            default -> new Classification(CheckStatus.OK, "Metadato informativo, normalmente no sensible.");
        };
    }
}
