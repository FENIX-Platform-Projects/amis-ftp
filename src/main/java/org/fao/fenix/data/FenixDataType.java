package org.fao.fenix.data;

public enum FenixDataType {
    SingleDate,
    SingleText,
    Range,
    quantity,
    quality,
    text,
    date,
    range,
    indicator,
    firstIndicator,
    secondIndicator,
    featureCode,
    commodityCode,
    measurementUnit,
    measurementUnitSystem,
    measurementUnitSystemRegion,
    baseDateFrom,
    baseDateTo,
    aggregate;

    private FenixDataType() {
    }

    public static org.fao.fenix.data.FenixDataType valueOfIgnoreCase(String name) {
        org.fao.fenix.data.FenixDataType[] arr$ = values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            org.fao.fenix.data.FenixDataType type = arr$[i$];
            if(type.toString().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
