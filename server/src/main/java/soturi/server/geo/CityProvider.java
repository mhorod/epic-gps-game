package soturi.server.geo;

import java.util.List;

public interface CityProvider {
    List<City> getCities();
    void reloadCities();
}
