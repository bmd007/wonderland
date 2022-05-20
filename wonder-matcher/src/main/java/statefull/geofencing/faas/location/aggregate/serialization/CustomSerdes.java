package statefull.geofencing.faas.location.aggregate.serialization;

import statefull.geofencing.faas.common.domain.Mover;
import statefull.geofencing.faas.common.dto.MoverLocationUpdate;

public class CustomSerdes {
    public static final JsonSerde<Mover> MOVER_JSON_SERDE = new JsonSerde<>(Mover.class);
    public static final JsonSerde<MoverLocationUpdate> MOVER_POSITION_UPDATE_JSON_SERDE = new JsonSerde<>(MoverLocationUpdate.class);
}
