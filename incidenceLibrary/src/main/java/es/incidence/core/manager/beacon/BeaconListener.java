package es.incidence.core.manager.beacon;


import java.util.ArrayList;

import es.incidence.core.domain.Beacon;

public interface BeaconListener {
    void didEnterRegion(String region);
    void onBeaconsDetected(ArrayList<Beacon> beacons);
}