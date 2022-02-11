package wonderland.driving.license.test.finder;

import java.time.Duration;
import java.util.List;

public record AvailableExamsResponse(Data data, int status, String url){
    public boolean isOk(){
        return status==200;
    }
}

record Data(List<Bundle> bundles, int searchedMonths){ }

record Bundle(List<Occasion> occasions, String cost){ }

record Occasion (
        String examinationId,
        int examinationCategory,
        Duration DurationObject,
        int examinationTypeId,
        int locationId,
        int occasionChoiceId,
        int vehicleTypeId,
        int languageId,
        int tachographTypeId,
        String name,
        String properties,
        String date,
        String time,
        String locationName,
        String cost,
        String costText,
        boolean increasedFee,
        String isEducatorBooking,
        boolean isLateCancellation,
        boolean isOutsideValidDuration,
        boolean isUsingTaxiKnowledgeValidDuration,
        String placeAddress,
        String placeCoordinate) {

    boolean isInStockholmCity(){
        return locationId == 1000140;
    }

    boolean isInUppsala(){
        return locationId == 1000071;
    }
}

