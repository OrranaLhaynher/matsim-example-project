package org.matsim.project.events;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.Vehicle;

public class VehicleLinkTravelTimeHandler implements LinkEnterEventHandler, LinkLeaveEventHandler {

    private final Map<Id<Vehicle>, Map<Id<Link>, Double>> vehicleLinkTravelTimes;

    public VehicleLinkTravelTimeHandler(Map<Id<Vehicle>, Map<Id<Link>, Double>> agentTravelTimes) {
        this.vehicleLinkTravelTimes = agentTravelTimes;
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        Map<Id<Link>, Double> travelTimes = this.vehicleLinkTravelTimes.get(event.getVehicleId());
        if (travelTimes == null) {
            travelTimes = new HashMap<Id<Link>, Double>();
            this.vehicleLinkTravelTimes.put(event.getVehicleId(), travelTimes);
        }
        travelTimes.put(event.getLinkId(), Double.valueOf(event.getTime()));
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        Map<Id<Link>, Double> travelTimes = this.vehicleLinkTravelTimes.get(event.getVehicleId());
        if (travelTimes != null) {
            Double d = travelTimes.get(event.getLinkId());
            if (d != null) {
                double time = event.getTime() - d.doubleValue();
                travelTimes.put(event.getLinkId(), Double.valueOf(time));
            }
        }
    }

    @Override
    public void reset(int iteration) {
    }
}