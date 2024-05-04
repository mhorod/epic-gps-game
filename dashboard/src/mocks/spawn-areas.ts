import { http, HttpResponse } from "msw";

const spawnAreaResponse = [
  {
    dimensions: {
      lowerLatitude: 50.06143,
      upperLatitude: 51.1456,
      lowerLongitude: 20.47418,
      upperLongitude: 21.01178,
      center: {
        latitude: 50.603515,
        longitude: 20.742980000000003,
      },
    },
    difficulty: 3,
  },
  {
    dimensions: {
      lowerLatitude: 51.1456,
      upperLatitude: 52.22977,
      lowerLongitude: 20.47418,
      upperLongitude: 21.01178,
      center: {
        latitude: 51.687685,
        longitude: 20.742980000000003,
      },
    },
    difficulty: 4,
  },
  {
    dimensions: {
      lowerLatitude: 50.06143,
      upperLatitude: 50.603515,
      lowerLongitude: 19.93658,
      upperLongitude: 20.205379999999998,
      center: {
        latitude: 50.3324725,
        longitude: 20.07098,
      },
    },
    difficulty: 1,
  },
  {
    dimensions: {
      lowerLatitude: 50.06143,
      upperLatitude: 50.603515,
      lowerLongitude: 20.205379999999998,
      upperLongitude: 20.47418,
      center: {
        latitude: 50.3324725,
        longitude: 20.339779999999998,
      },
    },
    difficulty: 1,
  },
  {
    dimensions: {
      lowerLatitude: 50.603515,
      upperLatitude: 51.1456,
      lowerLongitude: 19.93658,
      upperLongitude: 20.205379999999998,
      center: {
        latitude: 50.8745575,
        longitude: 20.07098,
      },
    },
    difficulty: 1,
  },
  {
    dimensions: {
      lowerLatitude: 50.603515,
      upperLatitude: 51.1456,
      lowerLongitude: 20.205379999999998,
      upperLongitude: 20.47418,
      center: {
        latitude: 50.8745575,
        longitude: 20.339779999999998,
      },
    },
    difficulty: 2,
  },
];

const spawnAreasHandlers = [
  http.get(window.location.origin + "/v1/areas", () =>
    HttpResponse.json(spawnAreaResponse),
  ),
];

export default spawnAreasHandlers;
