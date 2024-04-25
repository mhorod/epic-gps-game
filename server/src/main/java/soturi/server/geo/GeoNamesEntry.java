package soturi.server.geo;

import soturi.model.Position;

import java.util.Optional;

// the comments are from readme.txt contained in any GeoNames zip file
record GeoNamesEntry(
    int geonameid,              // integer id of record in geonames database
    String name,                // name of geographical point (utf8) varchar(200)
    String asciiname,           // name of geographical point in plain ascii characters, varchar(200)
    String alternatenames,      // alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
    double latitude,            // latitude in decimal degrees (wgs84)
    double longitude,           // longitude in decimal degrees (wgs84)
    char feature_class,         // see http://www.geonames.org/export/codes.html, char(1)
    String feature_code,        // see http://www.geonames.org/export/codes.html, varchar(10)
    String country_code,        // ISO-3166 2-letter country code, 2 characters
    String cc2,                 // alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
    String admin1_code,         // fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
    String admin2_code,         // code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
    String admin3_code,         // code for third level administrative division, varchar(20)
    String admin4_code,         // code for fourth level administrative division, varchar(20)
    long population,            // bigint (8 byte int)
    String elevation,           // in meters, integer
    int dem,                    // digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
    String timezone,            // the iana timezone id (see file timeZone.txt) varchar(40)
    String modification_date    // date of last modification in yyyy-MM-dd format
) {
    public static GeoNamesEntry parse(String line) {
        String[] split = line.split("\t");
        return new GeoNamesEntry(
            Integer.parseInt(split[0]),
            split[1],
            split[2],
            split[3],
            Double.parseDouble(split[4]),
            Double.parseDouble(split[5]),
            split[6].charAt(0),
            split[7],
            split[8],
            split[9],
            split[10],
            split[11],
            split[12],
            split[13],
            Long.parseLong(split[14]),
            split[15],
            Integer.parseInt(split[16]),
            split[17],
            split[18]
        );
    }

    public Optional<City> toCity() {
        if (feature_class != 'P' || population <= 5000)
            return Optional.empty();
        Position position = new Position(latitude, longitude);
        return Optional.of(new City(asciiname, population, position));
    }
}
